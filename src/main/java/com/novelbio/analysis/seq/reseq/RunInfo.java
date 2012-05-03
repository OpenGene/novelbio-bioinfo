package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.LocInfo;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class RunInfo {
	public static void main(String [] args) throws Exception
	{
//		getSeqInfo();
		copeSeq() ;
//		writeAssemblySeqSep();
//		runLastz();
//		runLastzFile() ;
	}
	
	public static void getSeqInfo() {
		String parentPath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/";
		String seqFile = parentPath + "seq12th_manual_coped.txt";
		String txtOut = parentPath + "seq12th_manual_coped_GetSeq.txt";
		GetSeqInfo getSeqInfo = new GetSeqInfo(seqFile);
		getSeqInfo.getSeqInfoAll(2000, 1000, txtOut);
	}
	
	
	
	public static void copeSeq() {
		SeqFastaHash seqFastaHash = new SeqFastaHash("/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seq12th_manual.txt");
		String out = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seq12th_manual_coped.txt";
		String statistic = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seq12th_manual_copedStatistic.txt";
		TxtReadandWrite txtOutSeq = new TxtReadandWrite(out, true);
		txtOutSeq.writefilePerLine(seqFastaHash.getSeqFastaAll().get(0).toString(), 100);
		txtOutSeq.close();
		ArrayList<LocInfo> lsresult = seqFastaHash.getSeqFastaAll().get(0).getSeqInfo();
		TxtReadandWrite txtStatistic = new TxtReadandWrite(statistic, true);

		for (LocInfo locInfo : lsresult) {
			txtStatistic.writefile(locInfo.toString());
		}
		txtStatistic.close();
	}
	
	/**
	 * 将大于10000bp的序列分开写入文本
	 */
	public static void writeAssemblySeqSep() {
		String assemblySeqFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer67.contig";
		String resultFilePath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapmer67";
		
		SeqFastaHash seqFastaHash = new SeqFastaHash(assemblySeqFile);
		int[] len = new int[]{8000,-1};
		seqFastaHash.writeFileSep(resultFilePath, "Soapmer67", len, true, 100);
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
		
		String geneFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/seq11th.txt";
		
		String pathInfo = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/lastzInfo";
		String scalfold = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/lastz/Soapkmer60.scafSeq";
		
		String parentFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT2/resultResq/";
		
		String modifySeqInfoFile = parentFile + "modifyInfoTestth.txt";
		String outNoModifySeqNameFile = parentFile + "noModifyInfoTestth.txt";
		String outScalFold = parentFile + "seqTestth.txt";
		String statistic = parentFile + "statistic_Testth.txt";
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
		String outstatictis = parentFile + "StatisticSeqWithScalFoldSoapmer60_4th.txt";
		ModifySeq modifySeq = new ModifySeq();
		modifySeq.setSeqFasta(geneFile,null);
		
		modifySeq.readFileLastZ(scalfold, pathInfo, outScalFold, outNoModifySeqNameFile, modifySeqInfoFile, outstatictis);
	}
	
	
	
}
