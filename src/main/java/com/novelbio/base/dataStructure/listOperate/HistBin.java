package com.novelbio.base.dataStructure.listOperate;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;

/**
 * 频率直方图的每个bin
 * @author zongjie
 */
public class HistBin extends ListDetailAbs {
	String description = "";
	/** 计数器 */
	long number = 0;
	protected HistBin() {
		super("", "", true);
	}
	/**
	 * 设定分数，根据需要保存double值
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
	 * 获得分数，根据需要保存的计数值
	 * @return
	 */
	public long getCountNumber() {
		return number;
	}
	/**
	 * 设定描述，根据需要保存string值
	 * @param score
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 获得描述，根据需要保存的string值
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
