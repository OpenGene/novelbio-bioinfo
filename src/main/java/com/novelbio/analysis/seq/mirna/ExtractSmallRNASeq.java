package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 从各个文件中提取ncRNA序列，用于做mapping的
 * @author zong0jie
 *
 */
public class ExtractSmallRNASeq {	
	public static void main(String[] args) {
		ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
		extractSmallRNASeq.setRNAdata("/media/winE/Bioinformatics/DataBase/sRNA/miRNA.dat", "HSA");
		extractSmallRNASeq.setOutPathPrefix("/media/winE/Bioinformatics/DataBase/sRNA/test");
		extractSmallRNASeq.getSeq();
		
	}
	
	String RNAdataFile = "";
	String RNAdataRegx = "";
	
	/** 提取ncRNA的正则表达式 */
	String regxNCrna  = "NR_\\d+|XR_\\d+";
	/** refseq的序列文件，要求是NCBI下载的文件 */
	String refseqFile = "";
	/** 从RefSeq中提取的ncRNA序列 */
	String outNcRNA = "";

	/** Rfam的正则 */
	String regRfam = "";
	/** Rfam的名字regx */
	String regxRfamWrite = "(?<=\\>)\\S+";
	/** rfam的文件 */
	String rfamFile = "";
	/** rfam的文件 */
	String outRfamFile = "";
	
	/** 提取到的目标文件夹和前缀 */
	String outPathPrefix = "";
	/**
	 * 设定输出文件夹和前缀
	 * @param outPathPrefix
	 */
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	
	public void setRNAdata(String rnaDataFile, String rnaDataRegx) {
		this.RNAdataFile = rnaDataFile;
		this.RNAdataRegx = rnaDataRegx;
	}
	/**
	 * 待提取的NCBI上下载的refseq文件
	 * @param refseqFile
	 */
	public void setRefseqFile(String refseqFile) {
		this.refseqFile = refseqFile;
	}
	/**
	 * 待提取某物中的rfam文件
	 * @param rfamFile
	 * @param regx rfam的物种名
	 */
	public void setRfamFile(String rfamFile, String regx) {
		this.rfamFile = rfamFile;
		this.regRfam = regx;
	}
	/**
	 * 提取序列
	 */
	public void getSeq() {
		if (FileOperate.isFileExist(refseqFile)) {
			outNcRNA = outPathPrefix + "_ncRNA.fa";
			extractNCRNA(refseqFile, outNcRNA, regxNCrna);
		}
		
		if (FileOperate.isFileExist(RNAdataFile)) {
			String outHairpinRNA = outPathPrefix + "_hairpin.fa";
			String outMatureRNA = outPathPrefix + "_mature.fa";
			extractMiRNASeqFromRNAdata(RNAdataFile, RNAdataRegx, outHairpinRNA, outMatureRNA);
		}
		
		if (FileOperate.isFileExist(rfamFile)) {
			outRfamFile = outPathPrefix + "_rfam.fa"; 
			extractRfam(rfamFile, outRfamFile, regRfam);
		}
	}
	/**
	 * 从NCBI的refseq.fa文件中提取NCRNA
	 * @param refseqFile
	 * @param outNCRNA
	 * @param regx 类似 "NR_\\d+|XR_\\d+";
	 */
	private void extractNCRNA(String refseqFile, String outNCRNA, String regx) {
		 SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false, false);
		 seqFastaHash.writeToFile( regx ,outNCRNA );
	}
	/**
	 * 从miRBase的RNAdata文件中提取miRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractMiRNASeqFromRNAdata(String rnaDataFile, String rnaDataRegx, String rnaHairpinOut, String rnaMatureOut) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnaDataFile, false);
		TxtReadandWrite txtHairpin = new TxtReadandWrite(rnaHairpinOut, true);
		TxtReadandWrite txtMature = new TxtReadandWrite(rnaMatureOut, true);

		StringBuilder block = new StringBuilder();
		for (String string : txtRead.readlines()) {
			if (string.startsWith("//")) {
				ArrayList<SeqFasta> lsseqFastas = getSeqFromRNAdata(block.toString(), rnaDataRegx);
				if (lsseqFastas.size() == 0) {
					block = new StringBuilder();
					continue;
				}
				txtHairpin.writefileln(lsseqFastas.get(0).toStringNRfasta());
				for (int i = 1; i < lsseqFastas.size(); i++) {
					txtMature.writefileln(lsseqFastas.get(i).toStringNRfasta());
				}
				block = new StringBuilder();
				continue;
			}
			block.append( string + TxtReadandWrite.ENTER_LINUX);
		}
		
		txtRead.close();
		txtHairpin.close();
		txtMature.close();
	}
	/**
	 * 给定RNAdata文件的一个block，将其中的序列提取出来
	 * @param rnaDataBlock
	 * @return list-seqfasta 0: 前体序列
	 * 后面为成熟体序列
	 */
	private ArrayList<SeqFasta> getSeqFromRNAdata(String rnaDataBlock, String regex) {
		ArrayList<SeqFasta> lSeqFastas = new ArrayList<SeqFasta>();
		
		String[] ss = rnaDataBlock.split(TxtReadandWrite.ENTER_LINUX);
		String[] ssID = ss[0].split(" +");
		if (!ss[0].startsWith("ID") || !ssID[4].contains(regex)) {
			return lSeqFastas;
		}
		String miRNAhairpinName = ssID[1]; //ID   cel-lin-4         standard; RNA; CEL; 94 BP.
		ArrayList<ListDetailBin> lsSeqLocation = getLsMatureMirnaLocation(ss);
		String finalSeq = getHairpinSeq(ss);
		
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		SeqFasta seqFasta = new SeqFasta(miRNAhairpinName, finalSeq);
		seqFasta.setDNA(true);
		lsResult.add(seqFasta);
		for (ListDetailBin listDetailBin : lsSeqLocation) {
			SeqFasta seqFastaMature = new SeqFasta();
			seqFastaMature.setName(listDetailBin.getName());
			seqFastaMature.setSeq(finalSeq.substring(listDetailBin.getStartAbs()-1, listDetailBin.getEndAbs()));
			seqFastaMature.setDNA(true);
			lsResult.add(seqFastaMature);
		}
		return lsResult;
	}
	private ArrayList<ListDetailBin> getLsMatureMirnaLocation(String[] block) {
		ArrayList<ListDetailBin> lsResult = new ArrayList<ListDetailBin>();
		ListDetailBin lsMiRNAhairpin = null;
		for (String string : block) {
			String[] sepInfo = string.split(" +");
			if (sepInfo[0].equals("FT")) {
				if (sepInfo[1].equals("miRNA")) {
					lsMiRNAhairpin = new ListDetailBin();
					String[] loc = sepInfo[2].split("\\.\\.");
					lsMiRNAhairpin.setStartAbs(Integer.parseInt(loc[0]));
					lsMiRNAhairpin.setEndAbs(Integer.parseInt(loc[1]));
				}
				if (sepInfo[1].contains("product")) {
					String accID = sepInfo[1].split("=")[1];
					accID = accID.replace("\"", "");
					lsMiRNAhairpin.setName(accID);
					lsResult.add(lsMiRNAhairpin);
				}
			}
		}
		return lsResult;
	}
	private String getHairpinSeq(String[] block) {
		String finalSeq = "";
		boolean seqFlag = false;
		for (String string : block) {
			if (string.startsWith("SQ")) {
				seqFlag = true;
				continue;
			}
			if (seqFlag) {
				String[] ssA = string.trim().split(" +");
				finalSeq = finalSeq + string.replace(ssA[ssA.length - 1], "").replace(" ", "");
			}
		}
		return finalSeq;
	}
	/**
	 * 从miRBase的hairpinFile文件中提取miRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractRfam(String rfamFile, String outRfam, String regxSearch) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outRfam, true);
		PatternOperate patSearch = new PatternOperate(regxSearch, false);
		 SeqFastaHash seqFastaHash = new SeqFastaHash(rfamFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 ArrayList<SeqFasta> lsSeqfasta = seqFastaHash.getSeqFastaAll();
		 for (SeqFasta seqFasta : lsSeqfasta) {
			if (patSearch.getPat(seqFasta.getSeqName()).size() > 0 ) {
				SeqFasta seqFastaNew = seqFasta.clone();
				String name = seqFasta.getSeqName().split("\t")[0];
				name = name.replace(";", "//");
				seqFastaNew.setName(name);
				txtOut.writefileln(seqFasta.toStringNRfasta());
			}
		}
		 txtOut.close();
	}
}
