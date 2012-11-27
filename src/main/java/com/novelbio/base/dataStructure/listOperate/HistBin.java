package com.novelbio.base.dataStructure.listOperate;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;

/**
 * Ƶ��ֱ��ͼ��ÿ��bin
 * @author zongjie
 */
public class HistBin extends ListDetailAbs {
	/** ������ */
	long countNumber = 0;
	double binNum = -Double.MAX_VALUE;
	
	
	protected HistBin() {
		super("", "", true);
	}
	protected HistBin(Double binNum) {
		super("", "", true);
		if (binNum != null) {
			this.binNum = binNum;
		}
	}
	/**
	 * �趨������������Ҫ����doubleֵ
	 * @param score
	 */
	public void setNumber(long number) {
		this.countNumber = number;
	}
	public void addNumber() {
		this.countNumber++;
	}
	public void addNumber(int addNum) {
		this.countNumber = this.countNumber + addNum;
	}
	/**
	 * ����趨��binNum�򷵻�binNum��û�趨�Ļ�bin��ͷ��ȥβ������ע���Ƿ�Ҫ��������
	 * @return
	 */
	public double getThisNumber() {
		if (binNum > -Double.MAX_VALUE) {
			return binNum;
		}
		return (getEndAbs() + getStartAbs())/2;
	}
	
	/**
	 * ��÷�����������Ҫ����ļ���ֵ
	 * @return
	 */
	public long getCountNumber() {
		return countNumber;
	}
	public HistBin clone() {
		HistBin result = (HistBin) super.clone();
		result.countNumber = countNumber;
		return result;
	}

}
