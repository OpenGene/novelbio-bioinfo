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
public class MappingMiRNA {
	/** 序列文件 */
	String seqFile = "";
	/** 输出路径和前缀 */
	String outPath = "";
	
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
	/** 全部reads mapping至全基因组上 */
	String bedFileGenomeAll = null;
	
	/** 是否全部mapping至genome上，默认为true */
	boolean mapping2Genome = true;
	/** 是否全部mapping至genome上，默认为true */
	public void setMapping2Genome(boolean mapping2Genome) {
		this.mapping2Genome = mapping2Genome;
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
	 * @param seqFile 序列
	 * @param outPath 输出文件夹
	 * @param prix 文件前缀
	 */
	public void setSample(String seqFile) {
		this.seqFile = seqFile;
	}
	/** 设定输出文件夹和前缀 */
	public void setOutPath(String outPathPrefix) {
		this.outPath = outPathPrefix;
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
	/**
	 * mapping的流水线
	 */
	public void mappingPipeline() {
		bedFileMiRNA = outPath + "_miRNA.bed";
		bedFileRfam = outPath + "_rfam.bed";
		bedFileNCRNA = outPath + "_ncRna.bed";
		bedFileGenome = outPath + "_Genome.bed";
		/** 全部reads mapping至全基因组上 */
		bedFileGenomeAll = outPath + "_GenomeAll.bed";
		
		
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outPath + "_miRNA.sam";
			unMappedFq = outPath + "_unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outPath + "_rfam.sam";
			unMappedFq = outPath + "_unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outPath + "_ncRna.sam";
			unMappedFq = outPath + "_unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outPath + "_Genome.sam";
			unMappedFq = outPath + "_unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, unMappedFq);
		}
		
		if (mapping2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outPath + "_GenomeAll.sam";
			unMappedFq = outPath + "_unMapped.fq";
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
//		outPath = FileOperate.addSep(outPath);
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath(exePath, chrFile);
		SamFile samFile = mapBwa.mapReads();
		samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
