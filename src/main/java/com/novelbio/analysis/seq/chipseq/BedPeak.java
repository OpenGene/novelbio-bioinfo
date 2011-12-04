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
