package com.novelbio.database.entity.kegg.noGene;

public class KGNCompInfo {
	private String kegID;
	/**
	 * 设定Compound等的KeggID
	 * @param kegID
	 */
	public void setKegID(String kegID) {
		this.kegID = kegID;
	}
	/**
	 * 获得Compound等的KeggID
	 * @return
	 */
	public String getKegID() {
		return this.kegID;
	}
	/**
	 * 常用名，也就是待转换的ID
	 */
	private String usualName;
	/**
	 * 常用名，也就是待转换的ID
	 * @param usualName
	 */
	public void setUsualName(String usualName) {
		this.usualName = usualName;
	}
	/**
	 * 常用名，也就是待转换的ID
	 * @return
	 */
	public String getUsualName() {
		return this.usualName;
	}
	
	String formula;
	/**
	 * 化合物的化学式
	 * @param formula
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
	/**
	 * 化合物的化学式
	 * @return
	 */
	public String getFormula() {
		return this.formula;
	}
	
	double mass = 0;
	/**
	 * 似乎是化合物的分子量
	 * @param mass
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}
	/**
	 * 似乎是化合物的分子量
	 * @return
	 */
	public double getMass() {
		return this.mass;
	}
	
	String remark;
	/**
	 * 别名
	 * @param remark
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 别名
	 * @return
	 */
	public String getRemark() {
		return this.remark;
	}
	
	String comment;
	/**
	 * 别名
	 * @param comment
	 */
	public void setComment(String comment) {
		this.remark = comment;
	}
	/**
	 * 别名
	 * @return
	 */
	public String getComment() {
		return this.comment;
	}
	
}
