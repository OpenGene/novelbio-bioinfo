package com.novelbio.analysis.seq.chipseq;

import java.util.HashMap;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;

public abstract class PeakCalling {
	public static final String SPECIES_RICE = "os";
	public static final String SPECIES_HUMAN = "hs";
	public static final String SPECIES_C_ELEGAN = "ce";
	public static final String SPECIES_DROSOPHYLIA = "dm";
	public static final String SPECIES_MOUSE = "mm";
	public static final String SPECIES_ARABIDOPSIS = "ath";
	static HashMap<String, Long> hashSpecies2GenomeSize = new HashMap<String, Long>();
	static
	{
		hashSpecies2GenomeSize.put(SPECIES_HUMAN, 3095677436L);
		hashSpecies2GenomeSize.put(SPECIES_RICE, 372317579L);
		hashSpecies2GenomeSize.put(SPECIES_ARABIDOPSIS, 119667757L);
		hashSpecies2GenomeSize.put(SPECIES_MOUSE, 2725749214L);
	}
	double effectiveGenomeSize = 0.85;
	/** 输入的文件 */
	String file = null;
	/** 对照，一般是input control，对照文件必须与实验组格式一致 */
	String controlFile = null;
	/**输出文件路径和前缀 */
	String outPrefix = "";
	public PeakCalling(String file) {
		this.file = file;
	}
	/** 文件格式，使用枚举FileFormat 
	 * 如果输入了不支持的文件格式，则返回false
	 * */
	public abstract boolean setFileFormat(FormatSeq fileformat);
	/** 物种，可以根据物种选择相应的参数，一般是 genome size */
	public abstract void setSpecies(String species);
	/**输出文件路径和前缀 */
	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}
	/** 对照，一般是input control，对照文件必须与实验组格式一致 */
	public void setControlFile(String controlFile) {
		this.controlFile = controlFile;
	}
	/**
	 * 默认85
	 * 当reads一定长度时，如果测饱和数量的reads，由于reads长度的限制，uniqmapping的reads不可能完全覆盖全基因组，一般来说测的越长覆盖的面积约大。
	 * 那么一般25bp覆盖为65%，35bp覆盖75%，50bp 80% 100bp可能更长，这里设置为100进制
	 * @param effectiveGenomeSize
	 */
	public void setEffectiveGenomeSize(int effectiveGenomeSize) {
		this.effectiveGenomeSize = (double)effectiveGenomeSize/100;
	}
	/**
	 * 没有实现，需要子类覆盖
	 * @param bedTreat 实验
	 * @param bedCol 对照
	 * @param species 物种，用于effective genome size，有hs，mm，dm，ce，os
	 * @param outFile 目标文件夹，不用加"/"
	 * @throws Exception 
	 */
	public abstract void peakCallling();
}
