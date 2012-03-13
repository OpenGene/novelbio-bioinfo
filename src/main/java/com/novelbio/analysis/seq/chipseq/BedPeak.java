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
	 * ��readsһ������ʱ������ⱥ��������reads������reads���ȵ����ƣ�uniqmapping��reads��������ȫ����ȫ�����飬һ����˵���Խ�����ǵ����Լ��
	 * ��ôһ��25bp����Ϊ65%��35bp����75%��50bp 80% 100bp���ܸ�������������Ϊ100����
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
	 * û��ʵ�֣���Ҫ���า��
	 * @param bedTreat ʵ��
	 * @param bedCol ����
	 * @param species ���֣�����effective genome size����hs��mm��dm��ce��os
	 * @param outFile Ŀ���ļ��У����ü�"/"
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
	 * �趨����Ķ�λ������Ϣ
	 * @param tssUpBp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ3000bp
	 * @param tssDownBp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 * @param geneEnd3UTR �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 */
	public void setRegion(int tssUpBp, int tssDownBp, int geneEnd3UTR)
	{
		gffChrAnno.setGeneRange(tssUpBp, tssDownBp, geneEnd3UTR);
	}
	/**
	 * �趨Tss,annotation�õ�
	 * @param filterTss Ĭ��-1500+1500
	 * @param filterGenEnd Ĭ��null
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
	 * @param lsIn ��һ���Ǳ�����
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
	 * @param txtPeakFile �ı�����title
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
