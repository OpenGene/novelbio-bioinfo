package com.novelbio.analysis.seq.reseq;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;

public class RunInfo {
	public static void main(String [] args) throws Exception
	{
//		writeAssemblySeqSep();
		runLastz();
//		runLastzFile() ;
	}
	/**
	 * 将大于10000bp的序列分开写入文本
	 */
	public static void writeAssemblySeqSep() {
		String assemblySeqFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer65.contig";
		String resultFilePath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapmer65";
		
		SeqFastaHash seqFastaHash = new SeqFastaHash(assemblySeqFile);
		int[] len = new int[]{8000,-1};
		seqFastaHash.writeFileSep(resultFilePath, "Soapmer65", len, true, 100);
	}
	/**
	 * 将小于10000bp的序列合起来写入文本
	 */
	public static void writeAssemblySeqAll() {
		String assemblySeqFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer60.scafSeq";
		String resultFilePath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapmer60";
		
		SeqFastaHash seqFastaHash = new SeqFastaHash(assemblySeqFile);
		int[] len = new int[]{200,10000};
		seqFastaHash.writeFileSep(resultFilePath, "Soapkmer60Short", len, false, 100);
	}
	
	public static void runSNPandIndel() {
		String geneFile = "/media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/NC_009443.fna";
		String soapsnpFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/snp/TGACTsoapsnpOut";
		String dindelFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/indel/variantCalls.VCF";
		
		String parentFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/";
		String fastaFileNewSNP = parentFile + "snpChange.fa";
		String fastaFileNewIndel = parentFile + "snpandIndelChange.fa";

		ModifySeq modifySeq = new ModifySeq();
		modifySeq.setSeqFasta(geneFile);
		modifySeq.readPathSoapsnp(soapsnpFile);
		modifySeq.writeFastaFile(fastaFileNewSNP);
		modifySeq.readPathDindel(dindelFile);
		modifySeq.writeFastaFile(fastaFileNewIndel);
	}
	
	public static void runLastz() {
		
		String geneFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seqWithScalFoldSoapmer60_Velvet_5th.txt";
		
		String pathInfo = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/lastzInfo";
		String scalfold = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer65.contig";
		
		String parentFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/";
		
		String modifySeqInfoFile = parentFile + "modifyInfo6th.txt";
		String outNoModifySeqNameFile = parentFile + "noModifyInfo6th.txt";
		String outScalFold = parentFile + "seqWithScalFoldSoapmer60_Velvet_Soap_65_6th.txt";
		String statistic = parentFile + "statistic_6th.txt";
		ModifySeq modifySeq = new ModifySeq();
		modifySeq.setSeqFasta(geneFile,null);
		
		modifySeq.readPathLastZ(scalfold, pathInfo,outScalFold ,outNoModifySeqNameFile, modifySeqInfoFile,statistic);
		
	}
	
	public static void runLastzFile() {
		
		String geneFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seqWithScalFoldSoapmer60_3rd.txt";
		
		String pathInfo = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/lastzInfo/Soapkmer60Short.Info";
//		String scalfold = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer60.scafSeq";
		String scalfold = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapmer60/Soapkmer60Short.fasta";
		String parentFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/";
		
		String modifySeqInfoFile = parentFile + "modifyInfo4th.txt";
		String outNoModifySeqNameFile = parentFile + "noModifyInfo4th.txt";
		String outScalFold = parentFile + "seqWithScalFoldSoapmer60_4th.txt";
		
		ModifySeq modifySeq = new ModifySeq();
		modifySeq.setSeqFasta(geneFile,null);
		
		modifySeq.readFileLastZ(scalfold, pathInfo,outScalFold ,outNoModifySeqNameFile, modifySeqInfoFile);
	}
	
	
	
}
