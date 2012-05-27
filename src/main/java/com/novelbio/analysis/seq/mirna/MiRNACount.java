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
public class MiRNACount extends BedSeq {
	Logger logger = Logger.getLogger(MiRNACount.class);
	public static void main(String[] args) {
		String rnadatFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/XSQ/mireap-xxx.gff";
		String hairpairMirna = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/XSQ/mireap-xxx_Pre.fa";
		String matureMirna = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/XSQ/mireap-xxx_mature.fa";
		
		
		String bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX3/H6_NovelMiRNA.bed";
		String out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		
		MiRNACount miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/N6_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/N36_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/C_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/H12_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX9/N12_NovelMiRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, ListMiRNALocation.TYPE_MIREAP,"SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
	}
	public static void main2(String[] args) {
		String rnadatFile = "/media/winE/Bioinformatics/DataBase/sRNA/miRNA.dat";
		String hairpairMirna = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/hairpin_pig_Final.fa";
		String matureMirna = "/media/winE/Bioinformatics/DataBase/sRNA/miRBase/mature_pig_Final.fa";
		
		
		String bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX9/N12_miRNA.bed";
		String out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		
		MiRNACount miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_miRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/H12_miRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/C_miRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/N36_miRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
		
		bedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/N6_miRNA.bed";
		out = FileOperate.changeFileSuffix(bedFile, "_Count", "txt");
		miRNACount = new MiRNACount(bedFile, "SSC", rnadatFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.outResult(out);
	}
	
	/** 获得miRNA定位信息 */
	ListMiRNALocation tmpMiRNALocation = new ListMiRNALocation();
	/**
	 * miRNA前体
	 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/**
	 * miRNA成熟体
	 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
	}
	/**
	 * 
	 * 给定bed文件和物种名
	 * @param bedFile
	 * @param gffType 读取的是miReap的文件还是RNA.dat
	 * @param Species
	 * @param rnadatFile
	 */
	public MiRNACount(String bedFile, int gffType, String Species, String rnadatFile) {
		super(bedFile);
		tmpMiRNALocation.setSpecies(Species);
		tmpMiRNALocation.setReadFileType(gffType);
		tmpMiRNALocation.ReadGffarray(rnadatFile);
	}
	/**
	 * 成熟体
	 * key: mirName
	 * value: mirMatureList
	 */
	HashMap<String, ArrayList<String[]>> hashMiRNADetailValue = new HashMap<String, ArrayList<String[]>>();
	/**
	 * 前体
	 */
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	
	public void outResult(String outFile) {
		countMiRNA();		
		String outMirValue = FileOperate.changeFileSuffix(outFile, "_MirValue", null);
		String outMirDetailValue = FileOperate.changeFileSuffix(outFile, "_MirMatureValue", null);
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirDetailValue = new TxtReadandWrite(outMirDetailValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
//			ListDetailBin lsMiRNA = tmpMiRNALocation.searchLOC(entry.getKey());
			
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNADetailValue.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				txtMirDetailValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + strings[1] + "\t"+ getSeq(entry.getKey(), strings[0]));
			}
		}
		
		txtMirValue.close();
		txtMirDetailValue.close();
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
		for (BedRecord bedRecord : readlines()) {
			String subName = tmpMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
			if (subName == null) {
				subName = bedRecord.getRefID();
			}
			double value = (double)1/bedRecord.getMappingNum();
			addMiRNACount(bedRecord.getRefID(), value);
			addMiRNADetailCount(bedRecord.getRefID(), subName, value);
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
	private void addMiRNADetailCount(String miRNAname,String miRNADetailname, double value) {
		if (hashMiRNADetailValue.containsKey(miRNAname)) {
			//获得具体成熟miRNA的信息
			ArrayList<String[]> lsTmpResult = hashMiRNADetailValue.get(miRNAname);
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
			hashMiRNADetailValue.put(miRNAname, lsTmpResult);
		}
	}
	
	
}
