package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class BedPeak extends BedSeq {
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
	double effectiveGenomeSize = 0.82;
	/**
	 * 当reads一定长度时，如果测饱和数量的reads，由于reads长度的限制，uniqmapping的reads不可能完全覆盖全基因组，一般来说测的越长覆盖的面积约大。
	 * 那么一般25bp覆盖为65%，35bp覆盖75%，50bp 80% 100bp可能更长，这里设置为100进制
	 * @param effectiveGenomeSize
	 */
	public void setEffectiveGenomeSize(int effectiveGenomeSize) {
		this.effectiveGenomeSize = (double)effectiveGenomeSize/100;
	}
	public BedPeak(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * 没有实现，需要子类覆盖
	 * @param bedTreat 实验
	 * @param bedCol 对照
	 * @param species 物种，用于effective genome size，有hs，mm，dm，ce，os
	 * @param outFile 目标文件夹，不用加"/"
	 * @throws Exception 
	 */
	public abstract void peakCallling(String bedCol,String species, String outFilePath ,String prix);
	
	GffChrAnno gffChrAnno = null;
	public void setGffFile(String gffType, String gffFile)
	{
		gffChrAnno = new GffChrAnno(gffType, gffFile);
		gffChrAnno.setFilterTssTes(new int[]{-1500,1500}, null);
	}
	/**
	 * 设定基因的定位区域信息
	 * @param tssUpBp 设定基因的转录起点上游长度，默认为3000bp
	 * @param tssDownBp 设定基因的转录起点下游长度，默认为2000bp
	 * @param geneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
	 */
	public void setRegion(int tssUpBp, int tssDownBp, int geneEnd3UTR)
	{
		gffChrAnno.setGeneRange(tssUpBp, tssDownBp, geneEnd3UTR);
	}
	/**
	 * 设定Tss,annotation用的
	 * @param filterTss 默认-1500+1500
	 * @param filterGenEnd 默认null
	 */
	public void setFilterTssTes(int[] filterTss, int[] filterGenEnd) {
		gffChrAnno.setFilterTssTes(filterTss, filterGenEnd);
	}
	public void setFilterGeneBody(boolean filterGeneBody, boolean filterExon, boolean filterIntron)
	{
		gffChrAnno.setFilterGeneBody(filterGeneBody, filterExon, filterIntron);
	}
	
	public void setFilterUTR(boolean filter5UTR, boolean filter3UTR)
	{
		gffChrAnno.setFilterUTR(filter5UTR, filter3UTR);
	}
	/**
	 * @param lsIn 第一行是标题行
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outFile
	 */
	protected void annoFilter(ArrayList<String[]> lsIn, int colChrID, int colStart, int colEnd, String outFile) {
		ArrayList<String[]> lsResult = gffChrAnno.getAnno(lsIn, colChrID, colStart, colEnd);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.ExcelWrite(lsResult, "\t", 1, 1);
	}
	/**
	 * @param txtPeakFile 文本包含title
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outFile
	 */
	public void annoFilter(String txtPeakFile, int colChrID, int colStart, int colEnd, String outFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtPeakFile, false);
		ArrayList<String[]> lsIn = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		ArrayList<String[]> lsResult = gffChrAnno.getAnno(lsIn, colChrID, colStart, colEnd);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.ExcelWrite(lsResult, "\t", 1, 1);
	}
}
