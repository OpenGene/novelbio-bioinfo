package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 计算每个miRNA的表达，无法获得总表达值，只能获得每个表达值
 * @author zong0jie
 *
 */
public class MiRNACount extends RunProcess<MiRNACount.MiRNAcountProcess>{
	public static void main(String[] args) {
		String bedFile = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNAbed/_miRNA.bed";
		String hairpairMirna = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNA/hairpin.fa";
		String matureMirna = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNA/mature.fa";
		String rnadatFile = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/run/output.mrd";
		String outFilePrefix = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/predict_deep_count";
		
		MiRNACount miRNACount = new MiRNACount();
		miRNACount.setBedSeqMiRNA(bedFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_MIRDEEP, new Species(9606), rnadatFile);
		miRNACount.writeResultToOut(outFilePrefix);
	}
	Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** 获得miRNA定位信息 */
	ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
	/** miRNA前体 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA成熟体 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** 比对的bed文件 */
	BedSeq bedSeqMiRNA = null;
	/** Mapping至前体但是没到成熟体的序列的后缀 */
	String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";
	/**
	 * 成熟体
	 * key: mirName
	 * value: mirMatureList
	 */
	HashMap<String, ArrayList<String[]>> hashMiRNAname2LsMatureName_Value = new HashMap<String, ArrayList<String[]>>();
	/**
	 * 成熟体, 用于结果中
	 */
	HashMap<String, Double> mapMirMaturename2Value = new HashMap<String, Double>();
	/** 前体 */
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	
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
	public void setBedSeqMiRNA(String bedFile) {
		bedSeqMiRNA = new BedSeq(bedFile);
		countMiRNA = false;
	}

	/**
	 * 输出文件前缀，就是miRNA的计数
	 * @param outFilePrefix
	 */
	public void writeResultToOut(String outFilePrefix) {
		countMiRNA();
		String outMirValue = outFilePrefix + "MirValue";
		String outMirMatureValue = outFilePrefix + "MirMatureValue";
		
		System.out.println(outMirValue);
		System.out.println(outMirValue);
		
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue().intValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAname2LsMatureName_Value.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				if (getSeq(entry.getKey(), strings[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(strings[1]);
				txtMirMatureValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + (int)countNum + "\t"+ getSeq(entry.getKey(), strings[0]));
			}
		}
		txtMirValue.close();
		txtMirMatureValue.close();
	}
	
	/**
	 * 给定miRNA成熟体名字，从前体中获得序列
	 * @param ID
	 * @return
	 */
	private String getSeq(String mirID, String matureID) {
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()).toString();
		}
		ListDetailBin listDetailBin = listMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = listMiRNALocation.getMapChrID2LsGff().get(matureID);
			if (lsInfo != null) {
				listDetailBin = listMiRNALocation.getMapChrID2LsGff().get(matureID).get(0);
			}
			else {
				if (!matureID.endsWith(flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix)) {
					logger.error("出现未知ID：" + mirID + " "  + matureID);
				}
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID.toLowerCase(), listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		System.out.println(matureID);
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	@Override
	protected void running() {
		hashMiRNAname2LsMatureName_Value.clear();
		hashMiRNAvalue.clear();
		mapMirMaturename2Value.clear();
		countMiRNA();
	}
	/**
	 * 无所谓排不排序
	 *读取bed文件，然后在mirDat中查找信息，并确定数量
	 * @param outTxt
	 */
	public void countMiRNA() {
		if (countMiRNA)
			return;
		countMiRNA = true;
		int countLoop = 0;
		for (BedRecord bedRecord : bedSeqMiRNA.readLines()) {
			copeBedRecord(bedRecord);
			
			suspendCheck();
			if (flagStop) break;
			countLoop++;
			if (countLoop % 500 == 0) {
				MiRNAcountProcess miRNAcountProcess = new MiRNAcountProcess();
				miRNAcountProcess.setReadsNum(countLoop);
				if (runGetInfo != null) {
					runGetInfo.setRunningInfo(miRNAcountProcess);
				}
			}
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAname2LsMatureName_Value.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				if (getSeq(entry.getKey(), strings[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(strings[1]);
				mapMirMaturename2Value.put(entry.getKey() + SepSign.SEP_ID + strings[0], countNum);
			}
		}
	}
	/** 一行一行处理 */
	private void copeBedRecord(BedRecord bedRecord) {
		String subName = listMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStartAbs(), bedRecord.getEndAbs());
		//找不到名字的在后面添加
		if (subName == null) {
			subName = bedRecord.getRefID() + flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix;
		}
		double value = (double)1/bedRecord.getMappingNum();
		addMiRNACount(bedRecord.getRefID(), value);
		addMiRNAMatureCount(bedRecord.getRefID(), subName, value);
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname
	 * @param value
	 */
	private void addMiRNACount(String miRNAname, double value) {
		if (hashMiRNAvalue.containsKey(miRNAname)) {
			double tmpValue = hashMiRNAvalue.get(miRNAname);
			hashMiRNAvalue.put(miRNAname, value + tmpValue);
		}
		else {
			hashMiRNAvalue.put(miRNAname, value);
		}
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname miRNA的名字
	 * @param miRNADetailname miRNA成熟体的名字
	 * @param value
	 */
	private void addMiRNAMatureCount(String miRNAname,String miRNADetailname, double value) {
		if (hashMiRNAname2LsMatureName_Value.containsKey(miRNAname)) {
			//获得具体成熟miRNA的信息
			ArrayList<String[]> lsTmpResult = hashMiRNAname2LsMatureName_Value.get(miRNAname);
			for (String[] strings : lsTmpResult) {
				if (strings[0].equals(miRNADetailname)) {
					//累加表达数值，加完就跳出
					strings[1] = (Double.parseDouble(strings[1]) + value) + "";
					return;
				}
			}
			//如果没有跳出说明是第一次找到该miRNA
			lsTmpResult.add(new String[]{miRNADetailname, value+""});
		}
		else {
			ArrayList<String[]> lsTmpResult = new ArrayList<String[]>();
			lsTmpResult.add(new String[]{miRNADetailname, value + ""});
			hashMiRNAname2LsMatureName_Value.put(miRNAname, lsTmpResult);
		}
	}
	
	public HashMap<String, Double> getMapMirMaturename2Value() {
		return mapMirMaturename2Value;
	}
	public HashMap<String, Double> getMapMiRNAvalue() {
		return hashMiRNAvalue;
	}
	
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2Value(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2MatureValue(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] title = getTitleMature(mapPrefix2_mapMiRNAMature2Value);
		lsResult.add(title);
		
		HashSet<String> setMirNameAll = getAllMirName(mapPrefix2_mapMiRNAMature2Value);
		
		for (String mirName : setMirNameAll) {
			String[] miRNAinfo = new String[title.length + 2];
			miRNAinfo[0] = mirName;
			for (int i = 2; i < title.length; i++) {
				HashMap<String, Double> mapMirna2Value = mapPrefix2_mapMiRNAMature2Value.get(title[i]);
				Double value = mapMirna2Value.get(mirName);
				if (value == null) {
					miRNAinfo[i] = 0 + "";
				} else {
					miRNAinfo[i] = value.intValue() + "";
				}
			}
			String[] seqName = mirName.split(SepSign.SEP_ID);
			miRNAinfo[miRNAinfo.length - 1] = getSeq(seqName[0], seqName[1]);
			lsResult.add(miRNAinfo);
		}
		return lsResult;
	}

	/** 返回涉及到的所有miRNA的名字 */
	private HashSet<String> getAllMirName(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		LinkedHashSet<String> setMirNameAll = new LinkedHashSet<String>();
		for (HashMap<String, Double> mapMiRNA2Value : mapPrefix2_mapMiRNA2Value.values()) {
			for (String miRNAname : mapMiRNA2Value.keySet()) {
				setMirNameAll.add(miRNAname);
			}
		}
		return setMirNameAll;
	}
	/** 返回涉及到的所有miRNA的名字 */
	private String[] getTitlePre(HashMap<String, ? extends Object> mapPrefix2Info) {
		String[] title = new String[mapPrefix2Info.size() + 2];
		title[0] = TitleFormatNBC.miRNApreName.toString();
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			title[i] = prefix;
			i ++;
		}
		title[title.length - 1] = TitleFormatNBC.mirPreSequence.toString();
		return title;
	}
	/** 返回涉及到的所有miRNA的名字 */
	private String[] getTitleMature(HashMap<String, ? extends Object> mapPrefix2Info) {
		String[] title = new String[mapPrefix2Info.size() + 3];
		title[0] = TitleFormatNBC.miRNApreName.toString();
		title[1] = TitleFormatNBC.miRNAName.toString();
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			title[i] = prefix;
			i ++;
		}
		title[title.length - 1] = TitleFormatNBC.mirSequence.toString();
		return title;
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

class CombMapMirPre2Value extends MirCountGetValueAbs {
	SeqFastaHash seqFastaHashPreMiRNA;
	CombMapMirPre2Value(SeqFastaHash seqFastaHashPreMiRNA) {
		this.seqFastaHashPreMiRNA = seqFastaHashPreMiRNA;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** 返回涉及到的所有miRNA的名字 */
		String[] titleStart = new String[2];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.mirSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
		lsTmpResult.add(seqFastaHashPreMiRNA.getSeqFasta(id).toString());
	}
}
