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
public class MiRNAmapPipline {
	/** 序列文件 */
	String seqFile = "";
	/** 输出文件夹，主要是bed文件 */
	String outPath;
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String outPathTmp;
	String outputPrefix;

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
	String bedFileMiRNA = null;
	String bedFileRfam = null;
	String bedFileNCRNA = null;
	String bedFileGenome = null;
	
	/** 是否全部mapping至genome上，默认为true */
	boolean mappingAll2Genome = true;
	/** 全部reads mapping至全基因组上后产生的bed文件 */
	String bedFileGenomeAll = null;

	
	/** 是否全部mapping至genome上，默认为true */
	public void setMappingAll2Genome(boolean mappingAll2Genome) {
		this.mappingAll2Genome = mappingAll2Genome;
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
	 * @param seqFile 输出文件夹
	 */
	public void setSample(String outputPrefix, String seqFile) {
		this.outputPrefix = outputPrefix;
		this.seqFile = seqFile;
	}
	/** 设定输出临时文件夹，必须是文件夹 */
	public void setOutPath(String outPath, String outPathTmp) {
		this.outPath = FileOperate.addSep(outPath);
		this.outPathTmp = FileOperate.addSep(outPathTmp);
	}
	/** 比对miRNA的bed文件结果 */
	public String getOutMiRNAbed() {
		return bedFileMiRNA;
	}
	/** 比对rfam的bed文件结果 */
	public String getOutRfambed() {
		return bedFileRfam;
	}
	/** 比对refseq中的ncRNA的bed文件结果 */
	public String getOutNCRNAbed() {
		return bedFileNCRNA;
	}
	/** 比对基因组的bed文件结果 */
	public String getOutGenomebed() {
		return bedFileGenome;
	}
	/** 比对miRNA的bed文件结果 */
	public String getOutGenomeAllbed() {
		return bedFileGenomeAll;
	}
	/** mapping的流水线 */
	public void mappingPipeline() {
		String outputFinal = outPath + outputPrefix;
		bedFileMiRNA = outputFinal +  "_miRNA.bed";
		bedFileRfam = outputFinal + "_rfam.bed";
		bedFileNCRNA = outputFinal + "_ncRna.bed";
		bedFileGenome = outputFinal + "_Genome.bed";
		/** 全部reads mapping至全基因组上 */
		bedFileGenomeAll = outputFinal + "_GenomeAll.bed";
		
		String outputTmpFinal = outPathTmp + outputPrefix;
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outputTmpFinal + "_miRNA.sam";
			unMappedFq = outputTmpFinal + "_unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outputTmpFinal + "_rfam.sam";
			unMappedFq = outputTmpFinal + "_unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outputTmpFinal + "_ncRna.sam";
			unMappedFq = outputTmpFinal + "_unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outputTmpFinal + "_Genome.sam";
			unMappedFq = outputTmpFinal + "_unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outputTmpFinal + "_GenomeAll.sam";
			unMappedFq = outputTmpFinal + "_unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenomeAll, unMappedFq);
		}
	}
	/**
	 * 将小RNAmapping至序列上去
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName
	 * @param bedFile
	 * @param unMappedFq
	 */
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, String unMappedFq) {
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath(exePath, chrFile);
		SamFile samFile = mapBwa.mapReads();
		samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
