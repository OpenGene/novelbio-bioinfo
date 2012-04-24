package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.tree.ExpandVetoException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;


import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.blastZJ.Cell;
import com.novelbio.analysis.seq.blastZJ.LongestCommonSubsequence;
import com.novelbio.analysis.seq.blastZJ.SmithWaterman;
import com.novelbio.analysis.seq.chipseq.BedPeak;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.BedPeakSicer;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.mapping.FastQMapAbs;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.analysis.seq.mapping.SAMtools;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.analysis.seq.reseq.LastzAlign;
import com.novelbio.analysis.seq.reseq.ModifySeq;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.analysis.tools.formatConvert.bedFormat.Soap2Bed;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.Rplot;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.generalConf.Species;
 import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import de.erichseifert.gral.plots.axes.Axis;

public class mytest {

	private static Logger logger = Logger.getLogger(mytest.class);
	/**
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		SamFile saMtoolsNBC = new SamFile("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA_RealnDeDup.bam");
		SamFile saMtoolsSBC = new SamFile("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/SBCmapping/A.sam");
//		System.out.println("NBCreads:"+saMtoolsNBC.getReadsNum(true));
		System.out.println("SBCreads:"+saMtoolsSBC.getReadsNum(true));
//		System.out.println(saMtoolsNBC.getReads("7:7856:10367"));
	}
	
	
	private static int getAllPeakLen(String txtFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtFile, false);
		int oldEnd = 0; String oldChr = "";
		int length = 0;
		for (String string : txtRead.readlines(2)) {
			String[] ss = string.split("\t");
			int start = Integer.parseInt(ss[1]); int end = Integer.parseInt(ss[2]);
			if (ss[0].equals(oldChr) && start < oldEnd) {
				if (end <= oldEnd) {
					continue;
				}
				else {
					length = length + end - oldEnd;
				}
			}
			else {
				length = length + end - start + 1;
			}
		}
		return length;
	}
	
	private void GATK() {
		/**
		 *  java -Xmx4g -jar GenomeAnalysisTK.jar \
   -R /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa \
   -knownSites /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/snp/Mills_Devine_2hit.indels.hg19.vcf \
   -I /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_1_BWA.bam \
   -T CountCovariates \
   -cov ReadGroupCovariate \
   -cov QualityScoreCovariate \
   -cov CycleCovariate \
   -cov DinucCovariate \
   -recalFile /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/my_reads.recal_data.csv

		 */
		ArrayList<String> lsCmd = new ArrayList<String>();
		lsCmd.add("-R");
		lsCmd.add("/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		lsCmd.add("-knownSites");
		lsCmd.add("/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/snp/Mills_Devine_2hit.indels.hg19.vcf");
		lsCmd.add("-I");
		lsCmd.add("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_BWA.bam");
		lsCmd.add("-T");
		lsCmd.add("CountCovariates");

		lsCmd.add("-cov");
		lsCmd.add("ReadGroupCovariate");
		lsCmd.add("-cov");
		lsCmd.add("QualityScoreCovariate");
		lsCmd.add("-cov");
		lsCmd.add("CycleCovariate");
		lsCmd.add("-cov");
		lsCmd.add("DinucCovariate");
		lsCmd.add("-recalFile");
		lsCmd.add("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/my_reads.recal_data.csv");
		String[] argv = lsCmd.toArray(new String[1]);
		CommandLineGATK.main(argv);
	}
	
	/**
	 * 开国银的solexa是单双端放在一起，而且是fasta格式，要将其转变为fastq格式
	 * @throws Exception 
	 */
	private static void getFastQ(String fasta,String outFile) throws Exception {
		String outFile1 = FileOperate.changeFileSuffix(outFile, "_1", null);
		String outFile2 = FileOperate.changeFileSuffix(outFile, "_2", null);
		TxtReadandWrite txtWrite1 = new TxtReadandWrite(outFile1, true);
		TxtReadandWrite txtWrite2 = new TxtReadandWrite(outFile2, true);
		
		
		
		TxtReadandWrite txtRead = new TxtReadandWrite(fasta, false);
		String content = "";
		BufferedReader reader = txtRead.readfile();
		String tmp1 = ""; String tmp2 = ""; int count = 0;
		while ((content = reader.readLine()) != null) {
			if (count == 0) {
				tmp1 = content.replace("length=75", "").trim().replace(" ", "_").replace(">", "@");
				if (tmp1.endsWith(".1")) {
					tmp1 = tmp1.substring(0,tmp1.length() - 2)+"/1";
				}
				else {
					System.out.println(content);
				}
			}
			else if (count == 1) {
				tmp1 = tmp1 + "\r\n"+content;
			}
			else if (count == 2) {
				tmp1 = tmp1 + content + "\r\n+\r\nggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg";
			}
			else if (count == 3) {
				tmp2 = content.replace("length=75", "").trim().replace(" ", "_").replace(">", "@");
				if (tmp2.endsWith(".2")) {
					tmp2 = tmp2.substring(0,tmp2.length() - 2)+"/2";
				}
				else {
					System.out.println(content);
				}
			}
			else if (count == 4) {
				tmp2 = tmp2 + "\r\n"+content;
			}
			
			else if (count == 5) {
				tmp2 = tmp2 + content + "\r\n+\r\nggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg";
				txtWrite1.writefileln(tmp1);
				txtWrite2.writefileln(tmp2);
				count = 0;
				continue;
			}
			count ++;
		}
	}
	
	
	
	/**
	 * 测序到bed文件的pipline
	 */
	public static void pipline() {
		
//		name();
		try {
			String seqFile1 = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/2.rawseq/2.rawseq.txt";
			FastQMapBwa fastQ = new FastQMapBwa(seqFile1,FastQ.QUALITY_MIDIAN,"" +
					"/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/2.bwa_tigr6.1_norednuntcdna_withnopolya.sam" ,true);
//			
			fastQ.setTrimPolyA(true,false);
			fastQ.setAdaptorRight("TCGTATGCCGTCTTCTGTT");
			fastQ.setReadsLenMin(15);
			fastQ.setAdapterParam(6, 3);
			fastQ = fastQ.filterReads("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/2.rawseq/2.filterAll.txt");
			String chrFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1_NoRendunt.all.cDNA";
			fastQ.setFilePath("", chrFile);		
			fastQ.setMapQ(5);
//			fastQ2.mapReads();
			
			BedSeq bedSeq = fastQ.getBedFileSE("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/2.bwa_all.bed");
//			bedSeq = new BedSeq("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/1.bwa_all.bed");
			bedSeq.sortBedFile("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/2.bwa_all_sort.bed");
			System.out.println(bedSeq.getSeqNum());
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String seqFile1 = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/3.rawseq/3.rawseq.txt";
			FastQMapBwa fastQ = new FastQMapBwa(seqFile1,FastQ.QUALITY_MIDIAN,"" +
					"/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/3.bwa_tigr6.1_norednuntcdna_withnopolya.sam" ,true);
//			
			fastQ.setTrimPolyA(true,false);
			fastQ.setAdaptorRight("TCGTATGCCGTCTTCTGTT");
			fastQ.setReadsLenMin(15);
			fastQ.setAdapterParam(6, 3);
			fastQ = fastQ.filterReads("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/3.rawseq/3.filterAll.txt");

			String chrFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1_NoRendunt.all.cDNA";
			fastQ.setFilePath("", chrFile);		
			fastQ.setMapQ(5);
//			fastQ2.mapReads();
			
			BedSeq bedSeq = fastQ.getBedFileSE("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/3.bwa_all.bed");
//			bedSeq = new BedSeq("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/1.bwa_all.bed");
			bedSeq.sortBedFile("/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/3.bwa_all_sort.bed");
			System.out.println(bedSeq.getSeqNum());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		
		
		
		
	}
	
	public static void name() {
		SeqFastaHash seqHash = new SeqFastaHash("/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1all.cDNA", "LOC_Os\\d{2}g\\d{5}", false, false);
		seqHash.writeFileSep("/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/", "TIGRrice6.1_NoRendunt.all.cDNA", new int[]{-1,-1}, false, 100);
	}
	private static void testFdrFunction() throws Exception {
		ArrayList<Double> lsinput = new ArrayList<Double>();
		ExcelOperate exOperate = new ExcelOperate();
		exOperate
				.openExcel("/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/杨克差异基因8-14.xls");
		String[][] pvalue = exOperate.ReadExcel(2, 6, exOperate.getRowCount(),
				6);
		for (int i = 0; i < pvalue.length; i++) {
			lsinput.add(Double.parseDouble(pvalue[i][0]));
		}

		ArrayList<Double> ls1 = MathComput.pvalue2Fdr(lsinput);
		ArrayList<Double> ls2 = MathComput.pvalue2FdrR(lsinput);
		TxtReadandWrite txtLs1 = new TxtReadandWrite(
				"/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/ls1.txt", true);
		TxtReadandWrite txtLs2 = new TxtReadandWrite(
				"/media/winE/NBC/Project/QPCR_YK110803/5vs5vs5/ls2.txt", true);
		txtLs1.writefile(ls1);
		txtLs2.writefile(ls2);
		txtLs1.close();
		txtLs2.close();
		System.out.println("ok");
	}

	public static void AthIntron() throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite
				.setParameter(
						"/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/s_3_sep_filter_high.fasta_1",
						false, true);
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
		SmithWaterman smithWaterman = new SmithWaterman(sequence1, sequence2,
				1, -2, -2, 1.1);
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
		FastQ fastQ = new FastQ(seq1, seq2, FastQ.FASTQ_ILLUMINA_OFFSET,
				FastQ.QUALITY_HIGM);
		try {
			fastQ.filterBarcode(
					"/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/sf/barcodTest.fastq",
					1, "GTCAT", "GTCAT", "CATGT", "CATGT", "TGACT", "TGACT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// /**
	// * 从tigr中提取序列，看提取格式如何
	// * @throws Exception
	// */
	// public static void getSeq() throws Exception {
	// ChrStringHash chrStringHash = new
	// ChrStringHash(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
	// GffHashUCSCgene gffHashUCSC = new
	// GffHashUCSCgene(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
	// GffDetailGene gffDetailGene = (GffDetailGene)
	// gffHashUCSC.getLocHashtable().get("NM_018216");
	// gffDetailGene.setCoord(gffDetailGene.getIsolist("NM_018216").getATGSsite());
	// GffGeneIsoSearch gffGeneIsoSearch =
	// gffDetailGene.getCoordSearchName("NM_018216");
	// ArrayList<int[]> lsIso =
	// gffGeneIsoSearch.getRangeIso(gffGeneIsoSearch.getATGSsite(),
	// gffGeneIsoSearch.getUAGsite());
	// String a = chrStringHash.getSeq(gffDetailGene.getCis5to3(),
	// gffDetailGene.getChrID(), lsIso , true);
	// System.out.println(a);
	// }
	//

	/**
	 * 看看bed文件里面有没有外显子剪接信息
	 * 
	 * @throws Exception
	 */
	public static void testBam2Bed() throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite
				.setParameter(
						"/media/winE/NBC/Project/RNASeq_GF110614/resultTmp/tophatResult/accepted.bed",
						false, true);
		BufferedReader reader = txtReadandWrite.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			if (!ss[1].equals(ss[6])) {
				System.out.println(content);
			}
		}
	}

	/**
	 * 统计5UTR的长度
	 * 
	 * @throws Exception
	 */
	public void calUTR5length() throws Exception {
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int geneNum = 0;
		int gene20bp = 0;
		int geneNun = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene
					.getLocHashtable().get(geneID);
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
		System.out.println("all Gene: " + geneNum);
		System.out.println(" Gene20: " + gene20bp);
		System.out.println("Gene0: " + geneNun);
	}

	/**
	 * 统计Intron的长度
	 * 
	 * @throws Exception
	 */
	public static void calIntron() throws Exception {
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		ArrayList<Integer> lsIntron = new ArrayList<Integer>();
		TreeSet<Integer> treeIntron = new TreeSet<Integer>();
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene
					.getLocHashtable().get(geneID);
			if (gffDetailGene.getLongestSplit().ismRNA()) {
				int a = gffDetailGene.getLongestSplit().getIsoInfo().size();
				for (int i = 0; i < a - 1; i++) {
					lsIntron.add(gffDetailGene.getLongestSplit().getLenIntron(
							i + 1));
					treeIntron.add(gffDetailGene.getLongestSplit()
							.getLenIntron(i + 1));
					if (gffDetailGene.getLongestSplit().getLenIntron(i + 1) == 1043911) {
						System.out.println(gffDetailGene.getName());
						System.out.println(i + 1);
					}
				}
			}
		}
		System.out.println(treeIntron.first());
		System.out.println(treeIntron.last());
	}

	/**
	 * 检查韩燕的mapping结果多少符合mRNA转录方向
	 * 
	 * @throws Exception
	 */
	public static void testHanyan() throws Exception {
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite
				.setParameter(
						"/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/mappingSoap/sort/large_Treat_macs_Sort.bed",
						false, true);
		BufferedReader reader = txtReadandWrite.readfile();
		String content = "";
		int count = 0;
		int count2 = 0;
		int count3 = 0;
		String old = "";
		String oldChr = "";
		while ((content = reader.readLine()) != null) {

			String[] ss = content.split("\t");
			if (ss[1].equals(old) && ss[0].equals(oldChr)) {
				continue;
			}
			old = ss[1];
			oldChr = ss[0];
			count3++;
			// if ( (ss[1].equals("33478276") || ss[1].equals("33478277") ||
			// ss[1].equals("33478275"))&& ss[0].equals("chr17")) {
			// continue;
			// }
			GffCodGene gffCodGene = (GffCodGene) gffHashUCSCgene
					.searchLocatioClone(ss[0], Integer.parseInt(ss[1]));
			if (gffCodGene != null && gffCodGene.findCod()) {
				if (gffCodGene.getGffDetailThis().isCis5to3() != ss[5]
						.equals("+")) {
					logger.error("坐标与基因转录方向不符合" + content);
					count++;
				} else {
					count2++;
					// System.out.println(content);
				}
				continue;
			}
			GffCodGene gffCodGene2 = (GffCodGene) gffHashUCSCgene
					.searchLocatioClone(ss[0], Integer.parseInt(ss[2]));
			if (gffCodGene2 != null && gffCodGene2.findCod()) {
				if (gffCodGene2.getGffDetailThis().isCis5to3() != ss[5]
						.equals("+")) {
					// logger.error("坐标与基因转录方向不符合"+content);
					count++;
				} else {
					count2++;
					// System.out.println(content);
				}
				continue;
			}
		}
		System.out.println("有多少不符合mRNA方向：" + count);
		System.out.println("有多少符合：" + count2);
		System.out.println("有多少reads：" + count3);
	}

	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * 
	 * @throws Exception
	 */
	public static void getRefSeq() throws Exception {

		SeqFastaHash seqFastaHash = new SeqFastaHash();
		seqFastaHash.readfile(
				"/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rna.fa",
				true, "\\w{2}_\\d+", false);

		ChrStringHash chrStringHash = new ChrStringHash(
				NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int seqNum = 0;
		int consensusSeq = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene
					.getLocHashtable().get(geneID);
			ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
			String tmpseq = chrStringHash.getSeq(gffDetailGene.isCis5to3(),
					gffDetailGene.getParentName(), iso, false);

			String tmpseq2 = seqFastaHash.getsequence(gffDetailGene
					.getLongestSplit().getName().toLowerCase(), true);
			if (gffDetailGene.getLongestSplit().getName()
					.equals("NM_022834")) {
				System.err.println("aa");
			}
			seqNum++;
			if (tmpseq != null && tmpseq2 != null) {
				if (tmpseq.length() > tmpseq2.length()) {
					tmpseq = tmpseq.substring(0, tmpseq2.length());// 去掉polya
				} else {
					tmpseq2 = tmpseq2.substring(0, tmpseq.length());// 去掉polya
				}
				if (SeqFastaHash.compare2Seq(tmpseq, tmpseq2) < 10) {
					consensusSeq++;
				} else {
					// logger.error(gffDetailGene.getLongestSplit().getIsoName());
				}
			}
		}

		System.out.println("all seq: " + seqNum);
		System.out.println("consus seq: " + consensusSeq);
	}

	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * 
	 * @throws Exception
	 */
	public static void getSeq() throws Exception {

		SeqFastaHash seqFastaHash = new SeqFastaHash();
		seqFastaHash.readfile(
				"/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rna.fa",
				true, "\\w{2}_\\d+", false);

		ChrStringHash chrStringHash = new ChrStringHash(
				NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene
				.getLocHashtable().get("NM_032526");
		ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
		String tmpseq = chrStringHash.getSeq(gffDetailGene.isCis5to3(),
				gffDetailGene.getParentName(), iso, false);
		System.out.println(tmpseq.toUpperCase());
	}

	/**
	 * 将genome根据reafseq输出为refseq的文件，中间与人类标准refseq进行比较
	 * 
	 * @throws Exception
	 */
	public static void getRefSeqFromChr(String filepath) throws Exception {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(filepath, true, false);

		ChrStringHash chrStringHash = new ChrStringHash(
				NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		GffHashGeneUCSC gffHashUCSCgene = new GffHashGeneUCSC();
		gffHashUCSCgene
				.ReadGff(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		ArrayList<String> lsGene = gffHashUCSCgene.getLOCChrHashIDList();
		int seqNum = 0;
		int consensusSeq = 0;
		for (String string : lsGene) {
			String geneID = string.split("/")[0];
			GffDetailGene gffDetailGene = (GffDetailGene) gffHashUCSCgene
					.getLocHashtable().get(geneID);
			ArrayList<int[]> iso = gffDetailGene.getLongestSplit().getIsoInfo();
			String tmpseq = chrStringHash.getSeq(gffDetailGene.isCis5to3(),
					gffDetailGene.getParentName(), iso, false);
			if (tmpseq != null) {
				txtReadandWrite.writefileln(">"
						+ gffDetailGene.getLongestSplit().getName());
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
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile
					+ "TSC2KOBarcode_small.fq", FastQ.QUALITY_MIDIAN,
					parentFile + "TSC2KOsmallMap.fq", "soap", IndexFile, false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile + "TSC2KOsmall.bed",
					parentFile + "nouse.bed", "aaa");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile
					+ "TSC2KOBarcode_mid.fq", FastQ.QUALITY_MIDIAN, parentFile
					+ "TSC2KOmidMap.fq", "soap", IndexFile, false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile + "TSC2KOmid.bed", parentFile
					+ "nouse.bed", "aaa");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FastQSoapMap fastQSoapMap = new FastQSoapMap(parentFile
					+ "TSC2KOBarcode_large.fq", FastQ.QUALITY_MIDIAN,
					parentFile + "TSC2KOlargeMap.fq", "soap", IndexFile, false);
			fastQSoapMap.mapReads();
			fastQSoapMap.copeSope2Bed(parentFile + "TSC2KOlarge.bed",
					parentFile + "nouse.bed", "aaa");
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
			} else if (Double.parseDouble(ss[14]) < 0.2) {
				txtOut.writefileln(content);
			}
		}
		txtFile.close();
		txtOut.close();
	}

}


