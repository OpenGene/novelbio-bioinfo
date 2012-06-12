package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 从各个文件中提取ncRNA序列，用于做mapping的
 * @author zong0jie
 *
 */
public class ExtractSmallRNASeq {	
	public static void main(String[] args) {
		String aa = ">hsa-mir-100 MI0000102 Homo sapiens miR-100 stem-loop";
		PatternOperate pat = new PatternOperate("(?<=\\>)\\S+", false);
		System.out.println(pat.getPatFirst(aa));
	}
	/** miRNA的hairpinFile的正则表达式 */
	String regxHairpinFile = "";
	/** miRNA的名字regx */
	String regxHairpinWrite = "(?<=\\>)\\S+";
	/** 从mirBase的hairpin.fa中提取的某物中的前体序列 */
	String hairpinFile = "";
	/** 提取的成熟miRNA序列 */
//	String outHairpinRNA = "";
	
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
	/**
	 * 设定miRNA前体序列
	 * @param hairpinFile
	 * @param regx miRNA前体的正则，一般是物种名
	 */
	public void setHairpinFile(String hairpinFile, String regx) {
		this.hairpinFile = hairpinFile;
		this.regxHairpinFile = regx;
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
		
		if (FileOperate.isFileExist(hairpinFile)) {
			outNcRNA = outPathPrefix + "_hairpin.fa"; 
			extractMiRNA(hairpinFile, outNcRNA, regxHairpinFile, regxHairpinWrite);
		}
		
		if (FileOperate.isFileExist(regRfam)) {
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
	 * 从miRBase的hairpinFile文件中提取miRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractMiRNA(String hairpinFile, String outMiRNA, String regxSearch, String regxWrite) {
		 SeqFastaHash seqFastaHash = new SeqFastaHash(hairpinFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 seqFastaHash.writeToFile(regxSearch, regxWrite, outMiRNA);
	}
	/**
	 * 从miRBase的hairpinFile文件中提取miRNA序列
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx 物种的英文，人类就是Homo sapiens
	 */
	private void extractRfam(String rfamFile, String outRfam, String regxSearch) {
		TxtReadandWrite txtOut = new TxtReadandWrite(rfamFile, true);
		PatternOperate patSearch = new PatternOperate(regxSearch, false);
		 SeqFastaHash seqFastaHash = new SeqFastaHash(hairpinFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 ArrayList<SeqFasta> lsSeqfasta = seqFastaHash.getSeqFastaAll();
		 for (SeqFasta seqFasta : lsSeqfasta) {
			if (patSearch.getPat(seqFasta.getSeqName()).size() > 0 ) {
				SeqFasta seqFastaNew = seqFasta.clone();
				String name = seqFasta.getSeqName().split("\t")[0];
				name = name.replace(";", "//");
				seqFastaNew.setName(name);
			}
		}
		 txtOut.close();
	}
}
