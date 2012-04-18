package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �Ӹ����ļ�����ȡncRNA����
 * @author zong0jie
 *
 */
public class ExtractNCRNA {
	public static void main(String[] args) {
		ExtractNCRNA extractNCRNA = new ExtractNCRNA();
		extractNCRNA.pipleLine();
	}
	
	private void pipleLine()
	{
		String refseqFile = "/media/winE/Bioinformatics/DataBase/sRNA/rna.fa";
		String outNCRNA = "/media/winE/Bioinformatics/DataBase/sRNA/refseqNCrna.fa";
		String regx = "NR_\\d+|XR_\\d+";
//		extractNCRNA(refseqFile, outNCRNA, regx);
		
		String hairpinFile = "/media/winE/Bioinformatics/DataBase/sRNA/mappingDB/hairpin.fa";
		String outMiRNA = FileOperate.changeFileSuffix(hairpinFile, "_human", null);
		regx = "Homo sapiens";
//		extractMiRNAandRfam(hairpinFile, outMiRNA, regx);
		
		String rfamFile = "/media/winE/Bioinformatics/DataBase/sRNA/Rfam.fasta";
		String outRfam = FileOperate.changeFileSuffix(rfamFile, "_human", null);
		regx = "human";
		extractMiRNAandRfam(rfamFile, outRfam, regx);
	}
	/**
	 * ��NCBI��hairpin.fa�ļ�����ȡNCRNA
	 * @param hairpin
	 * @param outNCRNA
	 * @param regx
	 */
	private void extractNCRNA(String refseqFile, String outNCRNA, String regx)
	{
		 SeqFastaHash seqFastaHash = new SeqFastaHash(refseqFile,regx,false, false);
		 seqFastaHash.writeToFile( regx ,outNCRNA );
	}
	/**
	 * ��miRBase��refseq�ļ�����ȡNCRNA
	 * @param refseqFile
	 * @param outNCRNA
	 * @param regx ���ֵ�Ӣ�ģ��������Homo sapiens
	 */
	private void extractMiRNAandRfam(String hairpinFile, String outMiRNA, String regx)
	{
		 SeqFastaHash seqFastaHash = new SeqFastaHash(hairpinFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 seqFastaHash.writeToFile( regx ,outMiRNA );
	}

}
