package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �Ӹ����ļ�����ȡncRNA���У�������mapping��
 * @author zong0jie
 *
 */
public class ExtractSmallRNASeq {	
	public static void main(String[] args) {
		String aa = ">hsa-mir-100 MI0000102 Homo sapiens miR-100 stem-loop";
		PatternOperate pat = new PatternOperate("(?<=\\>)\\S+", false);
		System.out.println(pat.getPatFirst(aa));
	}
	/** miRNA��hairpinFile��������ʽ */
	String regxHairpinFile = "";
	/** miRNA������regx */
	String regxHairpinWrite = "(?<=\\>)\\S+";
	/** ��mirBase��hairpin.fa����ȡ��ĳ���е�ǰ������ */
	String hairpinFile = "";
	/** ��ȡ�ĳ���miRNA���� */
//	String outHairpinRNA = "";
	
	/** ��ȡncRNA��������ʽ */
	String regxNCrna  = "NR_\\d+|XR_\\d+";
	/** refseq�������ļ���Ҫ����NCBI���ص��ļ� */
	String refseqFile = "";
	/** ��RefSeq����ȡ��ncRNA���� */
	String outNcRNA = "";

	/** Rfam������ */
	String regRfam = "";
	/** Rfam������regx */
	String regxRfamWrite = "(?<=\\>)\\S+";
	/** rfam���ļ� */
	String rfamFile = "";
	/** rfam���ļ� */
	String outRfamFile = "";
	
	/** ��ȡ����Ŀ���ļ��к�ǰ׺ */
	String outPathPrefix = "";
	/**
	 * �趨����ļ��к�ǰ׺
	 * @param outPathPrefix
	 */
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/**
	 * �趨miRNAǰ������
	 * @param hairpinFile
	 * @param regx miRNAǰ�������һ����������
	 */
	public void setHairpinFile(String hairpinFile, String regx) {
		this.hairpinFile = hairpinFile;
		this.regxHairpinFile = regx;
	}
	/**
	 * ����ȡ��NCBI�����ص�refseq�ļ�
	 * @param refseqFile
	 */
	public void setRefseqFile(String refseqFile) {
		this.refseqFile = refseqFile;
	}
	/**
	 * ����ȡĳ���е�rfam�ļ�
	 * @param rfamFile
	 * @param regx rfam��������
	 */
	public void setRfamFile(String rfamFile, String regx) {
		this.rfamFile = rfamFile;
		this.regRfam = regx;
	}
	/**
	 * ��ȡ����
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
	 * ��NCBI��refseq.fa�ļ�����ȡNCRNA
	 * @param refseqFile
	 * @param outNCRNA
	 * @param regx ���� "NR_\\d+|XR_\\d+";
	 */
	private void extractNCRNA(String refseqFile, String outNCRNA, String regx) {
		 SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false, false);
		 seqFastaHash.writeToFile( regx ,outNCRNA );
	}
	/**
	 * ��miRBase��hairpinFile�ļ�����ȡmiRNA����
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx ���ֵ�Ӣ�ģ��������Homo sapiens
	 */
	private void extractMiRNA(String hairpinFile, String outMiRNA, String regxSearch, String regxWrite) {
		 SeqFastaHash seqFastaHash = new SeqFastaHash(hairpinFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 seqFastaHash.writeToFile(regxSearch, regxWrite, outMiRNA);
	}
	/**
	 * ��miRBase��hairpinFile�ļ�����ȡmiRNA����
	 * @param hairpinFile
	 * @param outNCRNA
	 * @param regx ���ֵ�Ӣ�ģ��������Homo sapiens
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
