package com.novelbio.database.domain.kegg.noGene;

public class KGNIdKeg {
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
	private String attribute;
	/**
	 * 设定KeggID的属性，也就是到底是Drug还是Compound还是其他的
	 * @param atrribute
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	/**
	 * 获得KeggID的属性，也就是到底是Drug还是Compound还是其他的
	 * @return
	 */
	public String getAttribute() {
		return this.attribute;
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
}
