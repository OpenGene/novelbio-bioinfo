package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.mapping.MapBwa;
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
	/** 序列文件 */
	String seqFile = "";
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String outPathTmpMapping;
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String outputPrefix = "";

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
	 * @param outputPrefix 输出前缀
	 * @param seqFile 输入的fastq文件
	 */
	public void setSample(String outputPrefix, String fastqFile) {
		if (outputPrefix != null && !outputPrefix.trim().equals("")) {
			if (!outputPrefix.endsWith("_")) {
				this.outputPrefix = outputPrefix.trim() + "_";
			}
		}
		this.seqFile = fastqFile;
	}
	/** 设定输出临时文件夹，必须是文件夹 */
	public void setOutPathTmp(String outPathTmpMapping) {
		this.outPathTmpMapping = FileOperate.addSep(outPathTmpMapping);
	}
	/** 比对miRNA的Sam文件结果 */
	public AlignSeq getOutMiRNAAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileMiRNA, 0)) {
			return new SamFile(samFileMiRNA);
		}
		return null;
	}
	/** 比对rfam的Sam文件结果 */
	public AlignSeq getOutRfamAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileRfam, 0)) {
			return new SamFile(samFileRfam);
		}
		return null;
	}
	/** 比对refseq中的ncRNA的Sam文件结果 */
	public AlignSeq getOutNCRNAAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileNCRNA, 0)) {
			return new SamFile(samFileNCRNA);
		}
		return null;
	}
	/** 比对基因组的Sam文件结果 */
	public AlignSeq getOutGenomeAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileGenome, 0)) {
			return new SamFile(samFileGenome);
		}
		return null;
	}
	/** 比对miRNA的Sam文件结果 */
	public AlignSeq getOutGenomeAllAlignSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileGenomeAll, 0)) {
			return new SamFile(samFileGenomeAll);
		}
		return null;
	}
	/** mapping的流水线 */
	public void mappingPipeline() {
		String outputSam = outPathTmpMapping + outputPrefix;
		samFileMiRNA = outputSam +  "miRNA.sam";
		samFileRfam = outputSam + "rfam.sam";
		samFileNCRNA = outputSam + "ncRna.sam";
		samFileGenome = outputSam + "Genome.sam";
		/** 全部reads mapping至全基因组上 */
		samFileGenomeAll = outputSam + "GenomeAll.sam";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String unMappedFq = "";
		String unMappedMiRNA = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			samFileMiRNA = mapping(fqFile, miRNApreSeq, samFileMiRNA, unMappedFq);
			unMappedMiRNA = unMappedFq;
			if (!mappingAll2Rfam) {
				fqFile = unMappedFq;
			}
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			unMappedFq = outputTmpFinal + "unMap2rfam.fq";
			samFileRfam = mapping(fqFile, rfamSeq, samFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			unMappedFq = outputTmpFinal + "unMap2ncRna.fq";
			samFileNCRNA = mapping(fqFile, ncRNAseq, samFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			unMappedFq = outputTmpFinal + "unMapped.fq";
			samFileGenome = mapping(unMappedMiRNA, genome, samFileGenome, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			unMappedFq = outputTmpFinal + "unMapped.fq";
			samFileGenomeAll = mapping(fqFile, genome, samFileGenomeAll, unMappedFq);
		}
	}
	/** 仅mapping至MiRNA上 */
	public void mappingMiRNA() {
		FileOperate.createFolders(outPathTmpMapping);
		
		String outputSam = outPathTmpMapping + outputPrefix;
		samFileMiRNA = outputSam +  "miRNA.sam";
		/** 全部reads mapping至全基因组上 */
		samFileGenomeAll = outputSam + "GenomeAll.sam";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			samFileMiRNA = mapping(fqFile, miRNApreSeq, samFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	}
	/**
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName 输出sam文件名
	 * @param unMappedFq 没有mapping上的文件输出为fq
	 * @return 实际的输出文件名
	 */
	private String mapping(String fqFile, String chrFile, String samFileName, String unMappedFq) {
		MapBwa mapBwa = new MapBwa(fqFile, samFileName);
		mapBwa.setChrFile(chrFile);
		mapBwa.setExePath(exePath);
		mapBwa.setGapLength(5);
				
		SamFileStatistics samFileStatistics = new SamFileStatistics(FileOperate.getFileNameSep(samFileName)[0]);
		samFileStatistics.initial();
		mapBwa.addAlignmentRecorder(samFileStatistics);

		if (unMappedFq != null && !unMappedFq.equals("")) {
			SamToFastq samToFastq = new SamToFastq();
			samToFastq.setFastqFile(unMappedFq);
			samToFastq.setJustUnMapped(true);
			mapBwa.addAlignmentRecorder(samToFastq);
		}
		
		SamFile samFile = mapBwa.mapReads();
		samFile.close();
		
		samFileStatistics.writeToFile(FileOperate.changeFileSuffix(samFile.getFileName(), "_Statistics", "txt"));
		return samFile.getFileName();
	}
}
