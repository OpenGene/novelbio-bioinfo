package com.novelbio.database.entity.kegg.noGene;

public class KGNCompInfo {
	private String kegID;
	/**
	 * �趨Compound�ȵ�KeggID
	 * @param kegID
	 */
	public void setKegID(String kegID) {
		this.kegID = kegID;
	}
	/**
	 * ���Compound�ȵ�KeggID
	 * @return
	 */
	public String getKegID() {
		return this.kegID;
	}
	/**
	 * ��������Ҳ���Ǵ�ת����ID
	 */
	private String usualName;
	/**
	 * ��������Ҳ���Ǵ�ת����ID
	 * @param usualName
	 */
	public void setUsualName(String usualName) {
		this.usualName = usualName;
	}
	/**
	 * ��������Ҳ���Ǵ�ת����ID
	 * @return
	 */
	public String getUsualName() {
		return this.usualName;
	}
	
	String formula;
	/**
	 * ������Ļ�ѧʽ
	 * @param formula
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
	/**
	 * ������Ļ�ѧʽ
	 * @return
	 */
	public String getFormula() {
		return this.formula;
	}
	
	double mass = 0;
	/**
	 * �ƺ��ǻ�����ķ�����
	 * @param mass
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}
	/**
	 * �ƺ��ǻ�����ķ�����
	 * @return
	 */
	public double getMass() {
		return this.mass;
	}
	
	String remark;
	/**
	 * ����
	 * @param remark
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * ����
	 * @return
	 */
	public String getRemark() {
		return this.remark;
	}
	
	String comment;
	/**
	 * ����
	 * @param comment
	 */
	public void setComment(String comment) {
		this.remark = comment;
	}
	/**
	 * ����
	 * @return
	 */
	public String getComment() {
		return this.comment;
	}
	
}
