package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.RunProcess;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.fileOperate.FileOperate;

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
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_MIRDEEP, 9606, rnadatFile);
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
	public void setMiRNAinfo(int fileType, int taxID, String rnadatFile) {
		listMiRNALocation.setSpecies(taxID);
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
			ListBin<ListDetailBin > lsInfo = listMiRNALocation.getChrhash().get(matureID);
			if (lsInfo != null) {
				listDetailBin = listMiRNALocation.getChrhash().get(matureID).get(0);
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
		for (BedRecord bedRecord : bedSeqMiRNA.readlines()) {
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
	}
	/** 一行一行处理 */
	private void copeBedRecord(BedRecord bedRecord) {
		String subName = listMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
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
			hashMiRNAvalue.put(miRNAname, value+tmpValue);
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
