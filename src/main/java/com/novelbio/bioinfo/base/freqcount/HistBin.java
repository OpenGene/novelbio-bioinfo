package com.novelbio.bioinfo.base.freqcount;

import com.novelbio.bioinfo.base.Align;

/**
 * 频率直方图的每个bin
 * @author zongjie
 */
public class HistBin extends Align {
	/** 计数器 */
	long countNumber = 0;
	double binNum = -Double.MAX_VALUE;
	
	String name;
	String parentName;
	
	public HistBin() { }
	public HistBin(Double binNum) {
		if (binNum != null) {
			this.binNum = binNum;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
	public String getName() {
		return name;
	}
	public String getParentName() {
		return parentName;
	}
	/**
	 * 设定分数，根据需要保存double值
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
	 * 如果设定了binNum则返回binNum，没设定的话bin的头减去尾，所以注意是否要四舍五入
	 * @return
	 */
	public double getThisNumber() {
		if (binNum > -Double.MAX_VALUE) {
			return binNum;
		}
		return (getEndAbs() + getStartAbs())/2;
	}
	public int getLength() {
		return Math.abs(getEndAbs() - getStartAbs());
	}
	/**
	 * 获得分数，根据需要保存的计数值
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
