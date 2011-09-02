package com.novelbio.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.tree.ExpandVetoException;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.blastZJ.Cell;
import com.novelbio.analysis.seq.blastZJ.LongestCommonSubsequence;
import com.novelbio.analysis.seq.blastZJ.SmithWaterman;

import com.novelbio.analysis.seq.genomeNew.GffChrChIP;
import com.novelbio.analysis.seq.genomeNew.GffChrHanYanChrom;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashUCSCgene;
import com.novelbio.analysis.seq.mapping.FastQSoapMap;
import com.novelbio.analysis.seq.reseq.LastzAlign;
import com.novelbio.analysis.seq.reseq.ModifySeq;
import com.novelbio.analysis.tools.formatConvert.bedFormat.Soap2Bed;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.DAO.FriceDAO.DaoFSGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;




public class mytest {
	
	private static Logger logger = Logger.getLogger(mytest.class);  
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		KeggInfo keggInfo = new KeggInfo(CopedID.IDTYPE_GENEID, "100037287", 9823);
		System.out.println(keggInfo.getKegID());
		System.out.println(keggInfo.getLsKegPath().get(0).getPathName());
	}
	
	private static void testFdrFunction() throws Exception {
		ArrayList<Double> lsinput = new ArrayList<Double>();
		ExcelOperate exOperate = new ExcelOperate();
		exOperate.openExcel("/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/杨克差异基因8-14.xls");
		String[][] pvalue = exOperate.ReadExcel(2, 6, exOperate.getRowCount(), 6);
		for (int i = 0; i < pvalue.length; i++) {
			lsinput.add(Double.parseDouble(pvalue[i][0]));
		}
		
		ArrayList<Double> ls1 = MathComput.pvalue2Fdr(lsinput);
		ArrayList<Double> ls2 = MathComput.pvalue2FdrR(lsinput);
		TxtReadandWrite txtLs1 = new TxtReadandWrite("/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/ls1.txt", true);
		TxtReadandWrite txtLs2 = new TxtReadandWrite("/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/ls2.txt", true);
		txtLs1.writefile(ls1);
		txtLs2.writefile(ls2);
		txtLs1.close();
		txtLs2.close();
		System.out.println("ok");
	}
	public static void AthIntron() throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter("/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/s_3_sep_filter_high.fasta_1", false, true);
		BufferedReader reader = txtReadandWrite.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			if (content.contains("HWUSI-EAS1734:0007:3:2:16063:6943:0/1")) {
				System.out.println(content);
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
				System.out.println(reader.readLine());
			}
		}
	}
	
	public static void blast() {
		String sequence1 = "ATGAACAGCGTGGGCGAGGCGTGCACCGAGCTCAAGCGCGTAAGAGCACTAGAAGTAGGTAACTGATG";
		String sequence2 = "ATGAAAATGGGCGAGGCGTGCACCGAGCTCTTAAACCCTGAATCCTTTCATTGTTTTAAAACATTCTTACTATGAACAGGGCGAGGCGTGCTGCGAGCTC";
		SmithWaterman smithWaterman = new SmithWaterman(sequence1, sequence2, 1, -2, -2,1.1);
		String[] reStrings = smithWaterman.getAlignment();
		System.out.println("ok");
		try {
			smithWaterman.printScoreTable("/media/winE/matrix2");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void WZFfastq() {
		String parentPath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/";
		String seq1 = parentPath + "s_3_sep_filter_high.fasta_1";
		String seq2 = parentPath + "s_3_sep_filter_high.fasta_2";
		FastQ fastQ = new FastQ(seq1, seq2,FastQ.FASTQ_ILLUMINA_OFFSET,FastQ.QUALITY_HIGM);
		try {
			fastQ.filterBarcode("/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/sf/barcodTest.fastq", 1, 
					"GTCAT","GTCAT","CATGT","CATGT","TGACT","TGACT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	/**
//	 * 从tigr中提取序列，看提取格式如何
//	 * @throws Exception 
//	 */
//	public static void getSeq() throws Exception {
//		ChrStringHash chrStringHash = new ChrStringHash(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
//		GffHashUCSCgene gffHashUCSC = new GffHashUCSCgene(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
//		GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSC.getLocHashtable().get("NM_018216");
//		gffDetailGene.setCoord(gffDetailGene.getIsolist("NM_018216").getATGSsite());
//		GffGeneIsoSearch gffGeneIsoSearch = gffDetailGene.getCoordSearchName("NM_018216");
//		ArrayList<int[]> lsIso = gffGeneIsoSearch.getRangeIso(gffGeneIsoSearch.getATGSsite(), gffGeneIsoSearch.getUAGsite());
//		String a = chrStringHash.getSeq(gffDetailGene.getCis5to3(), gffDetailGene.getChrID(), lsIso , true);
//		System.out.println(a);
//	}
//	

	/**
	 * 看看bed文件里面有没有外显子剪接信息
	 * @throws Exception 
	 */
	public static void testBam2Bed() throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter("/media/winE/NBC/Project/RNASeq_GF110614/resultTmp/tophatResult/accepted.bed", false, true);
		BufferedReader reader = txtReadandWrite.readfile();
		String content= "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			if (!ss[1].equals(ss[6])) {
				System.out.println(content);
			}
		}
	}
	
	
	/**
	 * 统计5UTR的长度
	 * @throws Exception 
	 */
	public void calUTR5length() throws Exception {
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int geneNum = 0; int gene20bp = 0; int geneNun = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene.getLocHashtable().get(geneID);
			if (gffDetailGene.getLongestSplit().ismRNA()) {
				geneNum++;
				if (gffDetailGene.getLongestSplit().getLenUTR5() < 20) {
					gene20bp++;
				}
				if (gffDetailGene.getLongestSplit().getLenUTR5() == 0) {
					geneNun++;
				}
			}
			
		}
		System.out.println("all Gene: "+geneNum );
		System.out.println(" Gene20: "+gene20bp );
		System.out.println("Gene0: "+geneNun );
	}
	
	/**
	 * 统计Intron的长度
	 * @throws Exception 
	 */
	public static void calIntron() throws Exception {
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		ArrayList<Integer> lsIntron = new ArrayList<Integer>();
		TreeSet<Integer> treeIntron = new TreeSet<Integer>();
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene.getLocHashtable().get(geneID);
			if (gffDetailGene.getLongestSplit().ismRNA()) {
				int a = gffDetailGene.getLongestSplit().getIsoInfo().size();
				for (int i = 0; i < a - 1; i++) {
					lsIntron.add(gffDetailGene.getLongestSplit().getLenIntron(i+1));
					treeIntron.add(gffDetailGene.getLongestSplit().getLenIntron(i+1));
					if (gffDetailGene.getLongestSplit().getLenIntron(i+1) == 1043911) {
						System.out.println(gffDetailGene.getLocString());
						System.out.println(i+1);
					}
				}
			}
		}
		System.out.println(treeIntron.first());
		System.out.println(treeIntron.last());
	}
	
	
	
	/**
	 * 检查韩燕的mapping结果多少符合mRNA转录方向
	 * @throws Exception 
	 */
	public static void testHanyan() throws Exception {
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter("/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/mappingSoap/sort/large_Treat_macs_Sort.bed", false, true);
		BufferedReader reader = txtReadandWrite.readfile();
		String content = ""; int count = 0; int count2 = 0; int count3 = 0;
		String old = ""; String oldChr = "";
		while ((content = reader.readLine()) != null) {
			
			String[] ss = content.split("\t");
			if (ss[1].equals(old) && ss[0].equals(oldChr)) {
				continue;
			}
			old = ss[1]; oldChr = ss[0];
			count3++;
//			if ( (ss[1].equals("33478276") || ss[1].equals("33478277") || ss[1].equals("33478275"))&& ss[0].equals("chr17")) {
//				continue;
//			}
			GffCodGene gffCodGene = (GffCodGene) gffHashUCSCgene.searchLocation(ss[0], Integer.parseInt(ss[1]));
			if (gffCodGene != null && gffCodGene.findCod()) {
				if (gffCodGene.getGffDetailThis().getCis5to3() != ss[5].equals("+")) {
					logger.error("坐标与基因转录方向不符合"+content);
					count++;
				}
				else {
					count2++;
//					System.out.println(content);
				}
				continue;
			}
			GffCodGene gffCodGene2 = (GffCodGene) gffHashUCSCgene.searchLocation(ss[0], Integer.parseInt(ss[2]));
			if (gffCodGene2 != null && gffCodGene2.findCod()) {
				if (gffCodGene2.getGffDetailThis().getCis5to3() != ss[5].equals("+")) {
//					logger.error("坐标与基因转录方向不符合"+content);
					count++;
				}
				else {
					count2++;
//					System.out.println(content);
				}
				continue;
			}
		}
		System.out.println("有多少不符合mRNA方向："+count);
		System.out.println("有多少符合："+count2);
		System.out.println("有多少reads："+count3);
	}
	
	
	
	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * @throws Exception 
	 */
	public static void getRefSeq() throws Exception {
		
		SeqFastaHash seqFastaHash = new SeqFastaHash();
		seqFastaHash.readfile("/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rna.fa", true, "\\w{2}_\\d+", false);

		ChrStringHash chrStringHash =new ChrStringHash(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int seqNum = 0; int consensusSeq = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene.getLocHashtable().get(geneID);
			ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
			String tmpseq = chrStringHash.getSeq(gffDetailGene.getCis5to3(), gffDetailGene.getChrID(), iso, false);
			
			String tmpseq2 = seqFastaHash.getsequence(gffDetailGene.getLongestSplit().getIsoName().toLowerCase(), true);
			if (gffDetailGene.getLongestSplit().getIsoName().equals("NM_022834")) {
				System.err.println("aa");
			}
			seqNum ++ ;
			if (tmpseq != null && tmpseq2 != null) {
				if (tmpseq.length() > tmpseq2.length()) {
					tmpseq = tmpseq.substring(0, tmpseq2.length());//去掉polya
				}
				else {
					tmpseq2 = tmpseq2.substring(0, tmpseq.length());//去掉polya
				}
				if (SeqFastaHash.compare2Seq(tmpseq, tmpseq2) < 10)
				{
					consensusSeq ++ ;
				}
				else {
//					logger.error(gffDetailGene.getLongestSplit().getIsoName());
				}
			}
		}
 
		System.out.println("all seq: "+seqNum);
		System.out.println("consus seq: "+consensusSeq);
	}
	
	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * @throws Exception 
	 */
	public static void getSeq() throws Exception {
		
		SeqFastaHash seqFastaHash = new SeqFastaHash();
		seqFastaHash.readfile("/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rna.fa", true, "\\w{2}_\\d+", false);

		ChrStringHash chrStringHash =new ChrStringHash(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene.getLocHashtable().get("NM_032526");
		ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
		String tmpseq = chrStringHash.getSeq(gffDetailGene.getCis5to3(), gffDetailGene.getChrID(), iso, false);
		System.out.println(tmpseq.toUpperCase());
	}
	
	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * @throws Exception 
	 */
	public static void getRefSeqFromChr(String filepath) throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(filepath, true, false);
		
		ChrStringHash chrStringHash =new ChrStringHash(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashUCSCgene gffHashUCSCgene = new GffHashUCSCgene();
		gffHashUCSCgene.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int seqNum = 0; int consensusSeq = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene.getLocHashtable().get(geneID);
			ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
			String tmpseq = chrStringHash.getSeq(gffDetailGene.getCis5to3(), gffDetailGene.getChrID(), iso, false);
			if (tmpseq != null) {
				txtReadandWrite.writefileln(">" + gffDetailGene.getLongestSplit().getIsoName());
				txtReadandWrite.writefileln(tmpseq);
			}
		}
		txtReadandWrite.close();
	}
	
	/**
	 * 给韩燕做mapping和后期数据整合分析
	 */
	public void mappingHanyan() {

		String IndexFile = "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_refseq/RefSeqFromChr.fa.index";
		String parentFile = "/media/winE/NBC/Project/Project_HY_Lab/TSC2+KO/";
		try {
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile+"TSC2KOBarcode_small.fq", FastQ.QUALITY_MIDIAN, parentFile +"TSC2KOsmallMap.fq", "soap", IndexFile,false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile+"TSC2KOsmall.bed", parentFile+"nouse.bed", "aaa");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile+"TSC2KOBarcode_mid.fq", FastQ.QUALITY_MIDIAN, parentFile +"TSC2KOmidMap.fq", "soap", IndexFile,false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile+"TSC2KOmid.bed", parentFile+"nouse.bed", "aaa");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile+"TSC2KOBarcode_large.fq", FastQ.QUALITY_MIDIAN, parentFile +"TSC2KOlargeMap.fq", "soap", IndexFile,false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile+"TSC2KOlarge.bed", parentFile+"nouse.bed", "aaa");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void snpfilter() throws Exception {
		String filePath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/sf/";
		String snpPath = filePath + "TGACTsoapsnp";
		String snpOutPath = filePath + "TGACTsoapsnpOut2";
		
		TxtReadandWrite txtFile = new TxtReadandWrite(snpPath, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(snpOutPath, true);
		
		BufferedReader reader = txtFile.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			if (!ss[2].equals(ss[5])) {
				txtOut.writefileln(content);
			}
			else if (Double.parseDouble(ss[14]) < 0.2) {
				txtOut.writefileln(content);
			}
		}
		txtFile.close();
		txtOut.close();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
