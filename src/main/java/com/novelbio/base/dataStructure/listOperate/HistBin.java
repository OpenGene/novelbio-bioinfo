package com.novelbio.base.dataStructure.listOperate;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;

/**
 * Ƶ��ֱ��ͼ��ÿ��bin
 * @author zongjie
 */
public class HistBin extends ListDetailAbs {
	String description = "";
	/** ������ */
	long number = 0;
	protected HistBin() {
		super("", "", true);
	}
	/**
	 * �趨������������Ҫ����doubleֵ
	 * @param score
	 */
	public void setNumber(long number) {
		this.number = number;
	}
	public void addNumber() {
		this.number++;
	}
	public void addNumber(int addNum) {
		this.number = this.number + addNum;
	}
	
	/**
	 * ��÷�����������Ҫ����ļ���ֵ
	 * @return
	 */
	public long getCountNumber() {
		return number;
	}
	/**
	 * �趨������������Ҫ����stringֵ
	 * @param score
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * ���������������Ҫ�����stringֵ
	 * @param description
	 */
	public String getDescription() {
		return description;
	}
	public HistBin clone() {
		HistBin result = (HistBin) super.clone();
		result.number = number;
		result.description = description;
		return result;
	}

}
