package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQOld;
import com.novelbio.analysis.seq.chipseq.pipeline.Pipline;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 小RNA比对流水线
 * @author zong0jie
 *
 */
public class PipeLine {
	/** 序列文件 */
	String seqFile = "";
	/** 输出路径 */
	String outPath = "";
	/** rfam数据库中的序列 */
	String rfamSeq = "";
	/** miRNA序列 */
	String miRNAseq = "";
	/** ncRNA序列 */
	String ncRNAseq = "";
	/** 基因组序列 */
	String genome = "";
	/** 前缀 */
	String prix = "";
	/** GffType */
	String gffType = "";
	String geneGffFile = "";
	String chromFa = "";
	/** 给mireap准备的文件 */
	String outMapFile = "";
	/** 给mireap准备的文件 */
	String outSeqFile = "";
	/**
	 * 设定输入miReap程序的文件
	 * @param outSeqFile
	 * @param outMapFile
	 */
	public void setNovelMiRNAMiReapInputFile(String outSeqFile, String outMapFile) {
		this.outSeqFile = outSeqFile;
		this.outMapFile = outMapFile;
	}
	/**
	 * 设定预测新miRNA所需要的文件，会将mapping至genome上的bed文件整理成miReap所需要的文件
	 * @param gffType
	 * @param geneGffFile
	 * @param chromFa
	 */
	public void setgffInfo(String gffType, String geneGffFile, String chromFa) {
		this.gffType = gffType;
		this.geneGffFile = geneGffFile;
		this.chromFa = chromFa;
	}
	public static void main(String[] args) {
		String fastQfile = "";
		String outPath = "";
		String prix = "";
		String reapAln = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/XSQ/mireap-xxx.aln";
		String outFile =  "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/XSQ/mireap-xxx.fa";
		PipeLine pipline = new PipeLine();
		
		String parentPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/Rawdata/";
		fastQfile = parentPath + "s_6_IDX7_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/";
		prix = "H12";
		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
		
//		fastQfile = parentPath + "s_6_IDX3_1_h_filtered.fq";
//		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX3/";
//		prix = "H6";
//		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
//		
//		fastQfile = parentPath + "s_6_IDX4_1_h_filtered.fq";
//		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/";
//		prix = "N6";
//		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
//		
//		fastQfile = parentPath + "s_6_IDX5_1_h_filtered.fq";
//		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/";
//		prix = "N36";
//		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
		
		fastQfile = parentPath + "s_6_IDX6_1_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/";
		prix = "C";
		pipline.setSample(fastQfile, outPath, prix);
		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
		
//		fastQfile = parentPath + "s_6_IDX8_1_h_filtered.fq";
//		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/";
//		prix = "H36";
//		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
//		
//		fastQfile = parentPath + "s_6_IDX9_1_h_filtered.fq";
//		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX9/";
//		prix = "N12";
//		pipline.setSample(fastQfile, outPath, prix);
//		pipline.mappingNovelMiRNAmireapAln(reapAln, outFile);
		
		
		
		
	}
	public static void main2(String[] args) {
		String rfamSeq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/Rfam_pig.fasta";
		String miRNAseq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/hairpin_pig_Final.fa";
		String ncRNAseq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/RefSeq_ncRNA.txt";
		String genome = "/media/winE/Bioinformatics/GenomeData/pig/Index/bwa/chromAll.fa";
//		genome = "/media/winE/Bioinformatics/GenomeData/pig/Index/bwasus9/pigChromSus9.fa";
		PipeLine pipeLine = new PipeLine();
		pipeLine.setInfo(rfamSeq, miRNAseq, ncRNAseq, genome);
		
		String parentPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/Rawdata/";
		String fastQfile = parentPath + "s_6_IDX7_1_h_filtered.fq";
		String outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/";
		String prix = "H12";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX6_1_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/";
		prix = "C";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX8_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/";
		prix = "H36";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX3_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX3/";
		prix = "H6";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX9_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX9/";
		prix = "N12";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX5_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/";
		prix = "N36";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX4_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/";
		prix = "N6";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
	
	}
	/**
	 * @param rfamSeq
	 * @param miRNAseq
	 * @param ncRNAseq
	 * @param genome
	 */
	public void setInfo(String rfamSeq, String miRNAseq, String ncRNAseq, String genome) {
		this.rfamSeq = rfamSeq;
		this.miRNAseq = miRNAseq;
		this.ncRNAseq = ncRNAseq;
		this.genome = genome;
	}
	/**
	 * @param seqFile 序列
	 * @param outPath 输出文件夹
	 * @param prix 文件前缀
	 */
	public void setSample(String seqFile, String outPath, String prix) {
		this.seqFile = seqFile;
		this.outPath = outPath;
		this.prix = prix;
	}
	
	public void mappingRepeat() {
		outPath = FileOperate.addSep(outPath);
		String fqFile = "";
		String samFile = "";
		String bedFile = "";
		String unMappedFq= "";
		
		fqFile = outPath + prix + "_unMap2ncRna.fq";
		samFile = outPath + prix + "_GenomeSus9.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMappedSus9.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
	}
	
	
	public void mappingPipeline() {
		outPath = FileOperate.addSep(outPath);
		
		String fqFile = seqFile;
		String samFile = outPath + prix + "_miRNA.sam";
		String bedFile = outPath + prix + "_miRNA.bed";
		String unMappedFq = outPath + prix + "_unMap2miRNA.fq";
		mapping(fqFile, miRNAseq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_rfam.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMap2rfam.fq";
		mapping(fqFile, rfamSeq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_ncRna.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMap2ncRna.fq";
		mapping(fqFile, ncRNAseq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_Genome.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMapped.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
		
		fqFile = seqFile;
		samFile = outPath + prix + "_GenomeAll.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMapped.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
	}
	/**
	 * 将miReap生成的文件转化为fasta格式，并且对其做mapping
	 * @param reapAln
	 * @param outFile 产生的fasta格式
	 */
	public void mappingNovelMiRNAmireapAln(String reapAln, String outFile) {
		String outMatue = FileOperate.changeFileSuffix(outFile, "_mature", null);
		String outPre = FileOperate.changeFileSuffix(outFile, "_Pre", null);
//		NovelMiRNAReap.writeNovelMiRNASeq(reapAln, outPre, outMatue);
		String fqFile = seqFile;
		String samFile = outPath + prix + "_miRNA.sam";
		String bedFile = outPath + prix + "_miRNA.bed";
		String unMappedFq = outPath + prix + "_unMap2miRNA.fq";
		
		fqFile = seqFile;
		samFile = outPath + prix + "_NovelMiRNA.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMapped.fq";
		mapping(fqFile, outFile, samFile, bedFile, unMappedFq);
	}
	
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, String unMappedFq) {
		outPath = FileOperate.addSep(outPath);
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath("", chrFile);
		SamFile samFile = mapBwa.mapReads();
		samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
	}
	/**
	 * 将多个样本得到的mapping 至 genome上的bed文件合并，然后作成可以输入mireap的文件
	 * @param outFile
	 * @param bedSeqFile
	 */
	public void novelMiRNAPredict(String outFile, String... bedSeqFile) {
		NovelMiRNAReap novelMiRNA = new NovelMiRNAReap(gffType, geneGffFile, chromFa);
		BedSeq bedSeq = combGenomeBedFile(outFile, bedSeqFile);
		novelMiRNA.setBedSeq(bedSeq.getFileName());
		novelMiRNA.getNovelMiRNASeq( outMapFile, outSeqFile);
	}
	/**
	 * 将所有mapping至genomic上的bed文件合并，然后来预测novel miRNA
	 * @param outFile 输出文件
	 * @param bedSeqFile 输入文件
	 */
	private static BedSeq combGenomeBedFile(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = new BedSeq(outFile, true);
		for (String string : bedSeqFile) {
			BedSeq bedSeq2 = new BedSeq(string);
			for (BedRecord bedRecord : bedSeq2.readlines()) {
				bedSeq.writeBedRecord(bedRecord);
			}
			bedSeq2.closeWrite();
		}
		bedSeq.closeWrite();
		return bedSeq;
	}
}
