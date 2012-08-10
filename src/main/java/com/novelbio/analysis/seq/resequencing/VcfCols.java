package com.novelbio.analysis.seq.resequencing;

public class VcfCols {
	int colChrID = -1, colSnpStart = -1, colRefsequence = -1, colThisSequence = -1;
	int colBaseInfo = -1, colQuality = -1, colFiltered = -1, colFlagTitle = -1, colFlagDetail = -1;
	int colSnpDBID = -1;
	
	public VcfCols() {
		colChrID = 0; colSnpStart = 1; colRefsequence = 3; colThisSequence = 4;
		setColInfo(1, 2, 4, 5);
		setColAttr(8, 6, 7, 9, 10);
		setColDBsnp(3);
	}
	/**
	 * @param colDBsnp vcf��3
	 */
	public void setColDBsnp(int colDBsnp) {
		this.colSnpDBID = colDBsnp - 1;
	}
	/**
	 * @param colChrID vcf��1
	 * @param colSnpStart vcf��2
	 * @param colRefsequence vcf��4
	 * @param colThisSequence vcf��5
	 */
	public void setColInfo(int colChrID, int colSnpStart, int colRefsequence, int colThisSequence) {
		this.colChrID = colChrID - 1;
		this.colSnpStart = colSnpStart - 1;
		this.colRefsequence = colRefsequence - 1;
		this.colThisSequence = colThisSequence - 1;
	}
	/**
	 * @param colBaseInfo vcf��8
	 * @param colQuality vcf��6
	 * @param colFiltered vcf��7
	 * @param colFlagTitle vcf��9
	 * @param colFlagDetail vcf��10
	 */
	public void setColAttr(int colBaseInfo, int colQuality, int colFiltered, int colFlagTitle, int colFlagDetail) {
		this.colBaseInfo = colBaseInfo - 1;
		this.colQuality = colQuality - 1;
		this.colFiltered = colFiltered - 1;
		this.colFlagTitle = colFlagTitle - 1;
		this.colFlagDetail = colFlagDetail - 1;
	}
}

