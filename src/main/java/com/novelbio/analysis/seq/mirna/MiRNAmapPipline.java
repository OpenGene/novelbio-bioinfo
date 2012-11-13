package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
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
	String outPathTmpBed;
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
	public void setOutPathTmp(String outPathTmpMapping, String outPathTmpBed) {
		this.outPathTmpMapping = FileOperate.addSep(outPathTmpMapping);
		this.outPathTmpBed = FileOperate.addSep(outPathTmpBed);
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
		String outputBed = outPathTmpBed + outputPrefix;
		bedFileMiRNA = outputBed +  "miRNA.bed";
		bedFileRfam = outputBed + "rfam.bed";
		bedFileNCRNA = outputBed + "ncRna.bed";
		bedFileGenome = outputBed + "Genome.bed";
		/** 全部reads mapping至全基因组上 */
		bedFileGenomeAll = outputBed + "GenomeAll.bed";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outputTmpFinal + "miRNA.sam";
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outputTmpFinal + "rfam.sam";
			unMappedFq = outputTmpFinal + "unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outputTmpFinal + "ncRna.sam";
			unMappedFq = outputTmpFinal + "unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outputTmpFinal + "Genome.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, false, false, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outputTmpFinal + "GenomeAll.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenomeAll, true, false, unMappedFq);
		}
	}
	/** 仅mapping至MiRNA上 */
	public void mappingMiRNA() {
		FileOperate.createFolders(outPathTmpBed);
		FileOperate.createFolders(outPathTmpMapping);
		
		String outputBed = outPathTmpBed + outputPrefix;
		bedFileMiRNA = outputBed +  "miRNA.bed";
		/** 全部reads mapping至全基因组上 */
		bedFileGenomeAll = outputBed + "GenomeAll.bed";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outputTmpFinal + "miRNA.sam";
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, false, false, unMappedFq);
			fqFile = unMappedFq;
		}
	}
	/**
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName 输出sam文件名
	 * @param bedFile 输出bed文件名
	 * @param uniqueMapping 是否为uniqueMapping
	 * @param uniqueMappedReadsRandomSelectOne 非unique mapping的序列是否只随机抽取一条
	 * @param unMappedFq 没有mapping上的文件输出为fq
	 */
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, boolean uniqueMapping, boolean uniqueMappedReadsRandomSelectOne,String unMappedFq) {
		MapBwa mapBwa = new MapBwa(fqFile, samFileName);
		mapBwa.setChrFile(chrFile);
		mapBwa.setExePath(exePath);
		SamFile samFile = mapBwa.mapReads();
		samFile.setUniqMapping(uniqueMapping);
		samFile.setUniqueRandomSelectOneRead(uniqueMappedReadsRandomSelectOne);
		try { Thread.sleep(1000); } catch (Exception e) { }
		BedSeq bedSeq = samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile);
		bedSeq.close();
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
