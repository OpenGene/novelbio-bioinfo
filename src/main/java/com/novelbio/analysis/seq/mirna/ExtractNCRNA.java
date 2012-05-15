package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �Ӹ����ļ�����ȡncRNA���У�������mapping��
 * @author zong0jie
 *
 */
public class ExtractNCRNA {
	public static void main(String[] args) {
		ExtractNCRNA extractNCRNA = new ExtractNCRNA();
		extractNCRNA.pipleLine();
//		SeqFasta seqFasta = new SeqFasta("fes", "TCTCCGACTCAGA");
//		System.out.println(seqFasta.reservecom());
	}
	
	private void pipleLine()
	{
		String refseqFile = "/media/winE/Bioinformatics/GenomeData/pig/RefSeq";
		String outNCRNA = FileOperate.changeFileSuffix(refseqFile, "_ncRNA", "txt");
		String regx = "NR_\\d+|XR_\\d+";
		extractNCRNA(refseqFile, outNCRNA, regx);
		
		String hairpinFile = "/media/winE/Bioinformatics/DataBase/sRNA/mappingDB/hairpin.fa";
		String outMiRNA = FileOperate.changeFileSuffix(hairpinFile, "_pig", null);
		regx = "Sus scrofa";
		extractMiRNAandRfam(hairpinFile, outMiRNA, regx);
		
		String rfamFile = "/media/winE/Bioinformatics/DataBase/sRNA/rfam/Rfam.fasta";
		String outRfam = FileOperate.changeFileSuffix(rfamFile, "_pig", null);
		regx = "Sus scrofa \\(pig\\)";
		extractMiRNAandRfam(rfamFile, outRfam, regx);
		
		String matureRNA = "/media/winE/Bioinformatics/DataBase/sRNA/mappingDB/hairpin_pig.fa";
		String outMatureRNA = FileOperate.changeFileSuffix(matureRNA, "_Final", null);
		extractMiRNA(matureRNA, outMatureRNA);
		
//		String matureRNA = "/media/winE/Bioinformatics/DataBase/sRNA/miRBase/hairpin_human.fa";
//		String outMatureRNA = FileOperate.changeFileSuffix(matureRNA, "_Final", null);
//		extractMiRNA(matureRNA, outMatureRNA);
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

	
	/**
	 * ��miRBase��refseq�ļ�����ȡNCRNA
	 * @param refseqFile
	 * @param outNCRNA
	 * @param regx ���ֵ�Ӣ�ģ��������Homo sapiens
	 */
	private void extractMiRNA(String hairpinFile, String outMiRNA)
	{
		 SeqFastaHash seqFastaHash = new SeqFastaHash(hairpinFile,null,false, false);
		 seqFastaHash.setDNAseq(true);
		 seqFastaHash.writeToFile(" ", 1, outMiRNA);
	}
}
