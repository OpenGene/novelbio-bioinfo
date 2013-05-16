package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 计算每个miRNA的表达，无法获得总表达值，只能获得每个表达值
 * @author zong0jie
 *
 */
public class MiRNACount extends RunProcess<MiRNACount.MiRNAcountProcess>{
	public static void main(String[] args) {
		ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
		listMiRNALocation.setSpecies(new Species(10090));
		listMiRNALocation.setReadFileType(ListMiRNALocation.TYPE_RNA_DATA);
		listMiRNALocation.ReadGffarray("/media/winE/Bioinformatics/genome/sRNA/miRNA.dat");
 
		System.out.println(listMiRNALocation.searchMirName("mmu-mir-16-2", 50, 65));
	
		System.out.println("ok");
	}
	private static final Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** 获得miRNA定位信息 */
	ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
	/** miRNA前体 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA成熟体 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** 比对的bed文件 */
	AlignSeq alignSeqMiRNA = null;
	/** Mapping至前体但是没到成熟体的序列的后缀 */
	String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";
	/**
	 * 成熟体
	 * key: mirName
	 * value: mirMatureList
	 */
	ArrayListMultimap<String, String[]> mapMirPre2LsMature_Value;
	/**
	 * 成熟体, 用于结果中
	 */
	HashMap<String, Double> mapMirMature2Value;
	/** 前体 */
	HashMap<String, Double> mapMiRNApre2Value;
	
	boolean countMiRNA = false;
	/**
	 * 设定miRNA的前体序列和成熟序列
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
		countMiRNA = false;
	}
	/**
	 * 给定miRNA文件和物种名
	 * @param fileType 读取的是miReap的文件还是RNA.dat ListMiRNALocation.TYPE_RNA_DATA 或 ListMiRNALocation.TYPE_MIREAP
	 * @param Species 为miRNA.dat中的物种名，如果文件不是miRNA.dat，那就不用写了
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, Species species, String rnadatFile) {
		listMiRNALocation.setSpecies(species);
		listMiRNALocation.setReadFileType(fileType);
		listMiRNALocation.ReadGffarray(rnadatFile);
		countMiRNA = false;
	}
	/** 设定需要计算表达值的bed文件 */
	public void setAlignFile(AlignSeq alignSeq) {
		alignSeqMiRNA =alignSeq;
		countMiRNA = false;
	}

	/**
	 * 输出文件前缀，就是miRNA的计数
	 * @param outFilePrefix
	 */
	public void writeResultToOut(String outFilePrefix) {
		String outMirValue = outFilePrefix + "MirValue";
		String outMirMatureValue = outFilePrefix + "MirMatureValue";
		
		System.out.println(outMirValue);
		System.out.println(outMirValue);
		
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> miRNApre2Value : mapMiRNApre2Value.entrySet()) {
			txtMirValue.writefileln(miRNApre2Value.getKey() + "\t" + miRNApre2Value.getValue().intValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(miRNApre2Value.getKey() ));
		}
		for (String miRNApreName : mapMirPre2LsMature_Value.keySet()) {
			List<String[]> lsMirMature2value = mapMirPre2LsMature_Value.get(miRNApreName);
			for (String[] mature2Value : lsMirMature2value) {
				String mirMatureSeq = getMiRNAmatureSeq(miRNApreName, mature2Value[0]);
				if (mirMatureSeq == null) {
					continue;
				}
				double valueCountNum = Double.parseDouble(mature2Value[1]);
				txtMirMatureValue.writefileln(miRNApreName + "\t" + mature2Value[0] + "\t" + (int)valueCountNum + "\t"+ mirMatureSeq);
			}
		}
		txtMirValue.close();
		txtMirMatureValue.close();
	}
	
	/**
	 * 给定miRNA成熟体名字，从前体中获得序列
	 * @param mirID miRNA前体名字
	 * @param matureID miRNA成熟体名字
	 * @return
	 */
	protected String getMiRNAmatureSeq(String mirID, String matureID) {
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()).toString();
		}
		ListDetailBin listDetailBin = listMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = listMiRNALocation.getMapChrID2LsGff().get(matureID);
			if (lsInfo != null) {
				listDetailBin = lsInfo.get(0);
			} else {
				if (!matureID.endsWith(flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix)) {
					logger.error("出现未知ID：" + mirID + " "  + matureID);
				}
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID.toLowerCase(), listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	
	@Override
	protected void running() {
		countMiRNA();
	}
	/**
	 * 无所谓排不排序
	 *读取bed文件，然后在mirDat中查找信息，并确定数量
	 * @param outTxt
	 */
	private void countMiRNA() {
		if (countMiRNA)
			return;
		
		mapMirPre2LsMature_Value = ArrayListMultimap.create();
		mapMiRNApre2Value = new HashMap<String, Double>();
		mapMirMature2Value = new HashMap<String, Double>();
		
		countMiRNA = true;
		
		int countLoop = 0;
		for (AlignRecord alignRecord : alignSeqMiRNA.readLines()) {
			if (!alignRecord.isMapped()) {
				continue;
			}
			copeRecordAndFillMap(alignRecord);
			
			suspendCheck();
			if (flagStop) break;
			countLoop++;
//			if (countLoop % 1000 == 0) {
//				MiRNAcountProcess miRNAcountProcess = new MiRNAcountProcess();
//				miRNAcountProcess.setReadsNum(countLoop);
//				if (runGetInfo != null) {
//					runGetInfo.setRunningInfo(miRNAcountProcess);
//				}
//			}
		}
		for (String mirPreName : mapMirPre2LsMature_Value.keySet()) {
			List<String[]> lsMirMature2Value = mapMirPre2LsMature_Value.get(mirPreName);
			for (String[] mirMature2Value : lsMirMature2Value) {
				if (getMiRNAmatureSeq(mirPreName, mirMature2Value[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(mirMature2Value[1]);
				mapMirMature2Value.put(mirPreName + SepSign.SEP_ID + mirMature2Value[0], countNum);
			}
		}
	}
	/** 一行一行处理
	 * 并填充hashmap
	 *  */
	private void copeRecordAndFillMap(AlignRecord alignRecord) {
		if (alignRecord.getRefID() == null) {
			System.out.println();
		}
		String subName = listMiRNALocation.searchMirName(alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
		//找不到名字的在后面添加
		if (subName == null) {
			subName = alignRecord.getRefID() + flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix;
		}
		double value = (double)1/alignRecord.getMappedReadsWeight();
		addMiRNACount(alignRecord.getRefID(), value);
		addMiRNAMatureCount(alignRecord.getRefID(), subName, value);
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname
	 * @param thisMiRNAcount 本次需要累计的miRNAcount，因为一条reads可能mapping至多个miRNA，那么每个miRNA的数量即为1/count
	 */
	private void addMiRNACount(String miRNAname, double thisMiRNAcount) {
		if (mapMiRNApre2Value.containsKey(miRNAname)) {
			double tmpValue = mapMiRNApre2Value.get(miRNAname);
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount + tmpValue);
		} else {
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount);
		}
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname miRNA的名字
	 * @param miRNADetailname miRNA成熟体的名字
	 * @param thisMiRNAcount 本次需要累计的miRNAcount，因为一条reads可能mapping至多个miRNA，那么每个miRNA的数量即为1/count
	 */
	private void addMiRNAMatureCount(String miRNAname,String miRNADetailname, double thisMiRNAcount) {
		if (mapMirPre2LsMature_Value.containsKey(miRNAname)) {
			//获得具体成熟miRNA的信息
			List<String[]> lsTmpResult = mapMirPre2LsMature_Value.get(miRNAname);
			for (String[] strings : lsTmpResult) {
				if (strings[0].equals(miRNADetailname)) {
					//累加表达数值，加完就跳出
					strings[1] = (Double.parseDouble(strings[1]) + thisMiRNAcount) + "";
					return;
				}
			}
			//如果没有跳出说明是第一次找到该miRNA
			lsTmpResult.add(new String[]{miRNADetailname, thisMiRNAcount+""});
		} else {
			mapMirPre2LsMature_Value.put(miRNAname, new String[]{miRNADetailname, thisMiRNAcount + ""});
		}
	}
	
	public HashMap<String, Double> getMapMirMature2Value() {
		return mapMirMature2Value;
	}
	public HashMap<String, Double> getMapMiRNApre2Value() {
		return mapMiRNApre2Value;
	}
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2Value(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2MatureValue(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		CombMapMirMature2Value combMapMirMature2Value = new CombMapMirMature2Value(this);
		return combMapMirMature2Value.combValue(mapPrefix2_mapMiRNAMature2Value);
	}

	public static class MiRNAcountProcess {
		long readsNum;
		public void setReadsNum(long readsNum) {
			this.readsNum = readsNum;
		}
		public long getReadsNum() {
			return readsNum;
		}
	}
}

class CombMapMirPre2Value extends MirCombMapGetValueAbs {
	SeqFastaHash seqFastaHashPreMiRNA;
	CombMapMirPre2Value(SeqFastaHash seqFastaHashPreMiRNA) {
		this.seqFastaHashPreMiRNA = seqFastaHashPreMiRNA;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** 返回涉及到的所有miRNA的名字 */
		String[] titleStart = new String[2];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.mirPreSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
		lsTmpResult.add(seqFastaHashPreMiRNA.getSeqFasta(id).toString());
	}
}

class CombMapMirMature2Value extends MirCombMapGetValueAbs {
	MiRNACount miRNACount;
	CombMapMirMature2Value(MiRNACount miRNACount) {
		this.miRNACount = miRNACount;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** 返回涉及到的所有miRNA的名字 */
		String[] titleStart = new String[3];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.miRNAName.toString();
		titleStart[2] = TitleFormatNBC.mirSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		String[] seqName = id.split(SepSign.SEP_ID);
		lsTmpResult.add(seqName[0]);
		lsTmpResult.add(seqName[1]);
		lsTmpResult.add(miRNACount.getMiRNAmatureSeq(seqName[0], seqName[1]));
	}
}
