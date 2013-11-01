package com.novelbio.analysis.seq.mirna;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamToFastq;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 小RNA比对流水线
 * @author zong0jie
 *
 */
public class MiRNAmapPipline {
	private static final Logger logger = Logger.getLogger(MiRNAmapPipline.class);
	/** 序列文件 */
	String seqFile = "";
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String outPathTmpMapping;
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String prefix = "";

	/** rfam数据库中的序列 */
	String rfamSeq = "";
	/** miRNA序列 */
	String miRNApreSeq = "";
	/** ncRNA序列 */
	String ncRNAseq = "";
	/** 基因组序列 */
	String genome = "";
	/** bwa所在的路径 */
	String exePath = "";
	
	////////////////////// 输出文件名 /////////////////////////////
	String samFileMiRNA = null;
	String samFileRfam = null;
	String samFileNCRNA = null;
	String samFileGenome = null;
	
	/** 是否全部mapping至genome上，默认为true */
	boolean mappingAll2Genome = true;
	/** 是将全部reads mapping到下一步的数据库上还是将上一次剩下的reads mapping到下一步的数据库上 */
	boolean mappingAll2Rfam = true;
	
	/** 全部reads mapping至全基因组上后产生的bed文件 */
	String samFileGenomeAll = null;
	
	/** 比对到miRNA上的Statistics信息 */
	SamFileStatistics samFileStatisticsMiRNA;
	
	int threadNum = 3;
	
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	/** 是否全部mapping至genome上，默认为true */
	public void setMappingAll2Genome(boolean mappingAll2Genome) {
		this.mappingAll2Genome = mappingAll2Genome;
	}
	/** 是将全部reads mapping到下一步的数据库上还是将上一次剩下的reads mapping到下一步的数据库上 */
	public void setMappingAll2Seq(boolean mappingAll2Seq) {
		this.mappingAll2Rfam = mappingAll2Seq;
	}
	
	public void setRfamSeq(String rfamSeq) {
		this.rfamSeq = rfamSeq;
	}
	/** 设定前体序列 */
	public void setMiRNApreSeq(String miRNAseq) {
		this.miRNApreSeq = miRNAseq;
	}
	public void setNcRNAseq(String ncRNAseq) {
		this.ncRNAseq = ncRNAseq;
	}
	public void setGenome(String genome) {
		this.genome = genome;
	}
	/** bwa所在的路径，默认为""，也就是在系统路径下 */
	public void setExePath(String exePath) {
		this.exePath = exePath;
	}
	/**
	 * @param prefix 输出前缀
	 * @param seqFile 输入的fastq文件
	 */
	public void setSample(String prefix, String fastqFile) {
		if (prefix != null && !prefix.trim().equals("")) {
			this.prefix = prefix.trim();
		}
		this.seqFile = fastqFile;
	}
	/** 设定输出临时文件夹，必须是文件夹 */
	public void setOutPathTmp(String outPathTmpMapping) {
		this.outPathTmpMapping = FileOperate.addSep(outPathTmpMapping);
	}
	/** 比对miRNA的Sam文件结果 */
	public SamFile getOutMiRNAAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileMiRNA, 0)) {
			return new SamFile(samFileMiRNA);
		}
		return null;
	}
	/** 比对rfam的Sam文件结果 */
	public SamFile getOutRfamAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileRfam, 0)) {
			return new SamFile(samFileRfam);
		}
		return null;
	}
	/** 比对refseq中的ncRNA的Sam文件结果 */
	public SamFile getOutNCRNAAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileNCRNA, 0)) {
			return new SamFile(samFileNCRNA);
		}
		return null;
	}
	/** 比对基因组的Sam文件结果 */
	public SamFile getOutGenomeAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileGenome, 0)) {
			return new SamFile(samFileGenome);
		}
		return null;
	}
	/** 比对miRNA的Sam文件结果 */
	public SamFile getOutGenomeAllAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileGenomeAll, 0)) {
			return new SamFile(samFileGenomeAll);
		}
		return null;
	}
	/** mapping的流水线 */
	public void mappingPipeline() {
		String outputSam = outPathTmpMapping + prefix + "_";
		samFileMiRNA = outputSam +  "miRNA.sam";
		samFileRfam = outputSam + "rfam.sam";
		samFileNCRNA = outputSam + "ncRna.sam";
		samFileGenome = outputSam + "Genome.sam";
		/** 全部reads mapping至全基因组上 */
		samFileGenomeAll = outputSam + "GenomeAll.sam";
		
		String outputTmpFinal = outPathTmpMapping + prefix + "_";
		String fqFile = seqFile;
		String unMappedFq = "";
		String unMappedMiRNA = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq.gz";
			samFileStatisticsMiRNA = new SamFileStatistics(prefix);
			samFileMiRNA = mappingBowtie2(samFileStatisticsMiRNA, exePath, threadNum, fqFile, miRNApreSeq, samFileMiRNA, unMappedFq);
			unMappedMiRNA = unMappedFq;
			if (!mappingAll2Rfam) {
				fqFile = unMappedFq;
			}
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			unMappedFq = outputTmpFinal + "unMap2rfam.fq.gz";
			samFileRfam = mappingBowtie2(new SamFileStatistics(FileOperate.getFileNameSep(samFileRfam)[0]),
					exePath, threadNum, fqFile, rfamSeq, samFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			unMappedFq = outputTmpFinal + "unMap2ncRna.fq.gz";
			samFileNCRNA = mappingBowtie2(new SamFileStatistics(FileOperate.getFileNameSep(samFileNCRNA)[0]),
					exePath, threadNum, fqFile, ncRNAseq, samFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			unMappedFq = outputTmpFinal + "unMapped.fq.gz";
			samFileGenome = mappingBowtie2(new SamFileStatistics(FileOperate.getFileNameSep(samFileGenome)[0]),
					exePath, threadNum, unMappedMiRNA, genome, samFileGenome, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			unMappedFq = outputTmpFinal + "unMapped.fq.gz";
			samFileGenomeAll = mappingBowtie2(new SamFileStatistics(FileOperate.getFileNameSep(samFileGenomeAll)[0]),
					exePath, threadNum, fqFile, genome, samFileGenomeAll, unMappedFq);
		}
	}
	/** 仅mapping至MiRNA上 */
	public void mappingMiRNA() {
		FileOperate.createFolders(outPathTmpMapping);
		
		String outputSam = outPathTmpMapping + prefix + "_";
		samFileMiRNA = outputSam +  "miRNA.sam";
		/** 全部reads mapping至全基因组上 */
		samFileGenomeAll = outputSam + "GenomeAll.sam";
		
		String outputTmpFinal = outPathTmpMapping + prefix + "_";
		String fqFile = seqFile;
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq.gz";
			samFileStatisticsMiRNA = new SamFileStatistics(prefix);
			samFileMiRNA = mappingBowtie2(samFileStatisticsMiRNA, exePath, threadNum, fqFile, miRNApreSeq, samFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	}
	
	public SamFileStatistics getSamFileStatisticsMiRNA() {
		return samFileStatisticsMiRNA;
	}
	
	/**
	 * @param exePath
	 * @param threadNum
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName 输出sam文件名
	 * @param unMappedFq 没有mapping上的文件输出为fq
	 * @return 实际的sam输出文件名
	 */
	public static String mappingBowtie2(SamFileStatistics samFileStatistics, String exePath, int threadNum, String fqFile, String chrFile, String samFileName, String unMappedFq) {
		MapBowtie mapBowtie = new MapBowtie();
		mapBowtie.setFqFile(new FastQ(fqFile), null);
		mapBowtie.setOutFileName(samFileName);
		mapBowtie.setChrIndex(chrFile);
		mapBowtie.setExePath(exePath);
		mapBowtie.setGapLength(3);
		mapBowtie.setThreadNum(threadNum);
		if (samFileStatistics != null) {
			samFileStatistics.setCorrectChrReadsNum(true);
			samFileStatistics.initial();
			mapBowtie.addAlignmentRecorder(samFileStatistics);
		}

		if (unMappedFq != null && !unMappedFq.equals("")) {
			SamToFastq samToFastq = new SamToFastq();
			samToFastq.setFastqFile(unMappedFq);
			samToFastq.setJustUnMapped(true);
			mapBowtie.addAlignmentRecorder(samToFastq);
		}
		logger.info("start mapping miRNA");
		SamFile samFile = mapBowtie.mapReads();
		logger.info("finish mapping miRNA");
		samFileStatistics.writeToFile(FileOperate.changeFileSuffix(samFile.getFileName(), "_Statistics", "txt"));
		System.out.println();
		return samFile.getFileName();
	}
}
