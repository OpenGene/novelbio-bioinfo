package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class ListDetailBin extends ListDetailAbs{

	public ListDetailBin(String chrID, String ItemName, Boolean cis5to3) {
		super(chrID, ItemName, cis5to3);
		// TODO Auto-generated constructor stub
	}
	
	public ListDetailBin()
	{}
	
	double score = 0;
	/**
	 * 设定分数，根据需要保存double值
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}
	/**
	 * 获得分数，根据需要保存的double值
	 * @return
	 */
	public double getScore() {
		return score;
	}
	String description = "";
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
	public ListDetailBin clone() {
		ListDetailBin result = (ListDetailBin) super.clone();
		result.score = score;
		return result;
	}
}
