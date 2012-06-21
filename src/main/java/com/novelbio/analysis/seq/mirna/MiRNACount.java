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
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 计算每个miRNA的表达
 * @author zong0jie
 *
 */
public class MiRNACount {
	Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** 获得miRNA定位信息 */
	ListMiRNALocation tmpMiRNALocation = new ListMiRNALocation();
	/** miRNA前体 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA成熟体 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** 比对的bed文件 */
	BedSeq bedSeqMiRNA = null;
	/**
	 * 设定miRNA的前体序列和成熟序列
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
	}
	/**
	 * 给定miRNA文件和物种名
	 * @param fileType 读取的是miReap的文件还是RNA.dat ListMiRNALocation.TYPE_RNA_DATA 或 ListMiRNALocation.TYPE_MIREAP
	 * @param Species 为miRNA.dat中的物种名，如果文件不是miRNA.dat，那就不用写了
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, int taxID, String rnadatFile) {
		tmpMiRNALocation.setSpecies(taxID);
		tmpMiRNALocation.setReadFileType(fileType);
		tmpMiRNALocation.ReadGffarray(rnadatFile);
	}
	/** 设定需要计算表达值的bed文件 */
	public void setBedSeqMiRNA(String bedFile) {
		bedSeqMiRNA = new BedSeq(bedFile);
	}
	/**
	 * 成熟体
	 * key: mirName
	 * value: mirMatureList
	 */
	HashMap<String, ArrayList<String[]>> hashMiRNAmatureValue = new HashMap<String, ArrayList<String[]>>();
	/** 前体 */
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	/**
	 * 输出文件前缀，就是miRNA的计数
	 * @param outFilePrefix
	 */
	public void outResult(String outFilePrefix) {
		countMiRNA();
		String outMirValue = FileOperate.changeFileSuffix(outFilePrefix, "_MirValue", null);
		String outMirMatureValue = FileOperate.changeFileSuffix(outFilePrefix, "_MirMatureValue", null);
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
//			ListDetailBin lsMiRNA = tmpMiRNALocation.searchLOC(entry.getKey());
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAmatureValue.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				txtMirMatureValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + strings[1] + "\t"+ getSeq(entry.getKey(), strings[0]));
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
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID).toString();
		}
		ListDetailBin listDetailBin = tmpMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = tmpMiRNALocation.getChrhash().get(matureID);
			if (lsInfo != null) {
				listDetailBin = tmpMiRNALocation.getChrhash().get(matureID).get(0);
			}
			else {
				logger.error("出现未知ID：" + mirID + " "  + matureID);
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID, listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		System.out.println(matureID);
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	/**
	 * 无所谓排不排序
	 *读取bed文件，然后在mirDat中查找信息，并确定数量
	 * @param outTxt
	 */
	public void countMiRNA() {
		for (BedRecord bedRecord : bedSeqMiRNA.readlines()) {
			String subName = tmpMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
			if (subName == null) {
				subName = bedRecord.getRefID() + "_pre";
			}
			double value = (double)1/bedRecord.getMappingNum();
			addMiRNACount(bedRecord.getRefID(), value);
			addMiRNAMatureCount(bedRecord.getRefID(), subName, value);
		}
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
		if (hashMiRNAmatureValue.containsKey(miRNAname)) {
			//获得具体成熟miRNA的信息
			ArrayList<String[]> lsTmpResult = hashMiRNAmatureValue.get(miRNAname);
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
			hashMiRNAmatureValue.put(miRNAname, lsTmpResult);
		}
	}
}
