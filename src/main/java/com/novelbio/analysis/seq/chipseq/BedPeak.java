package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class BedPeak extends BedSeq {
	public BedPeak(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
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
