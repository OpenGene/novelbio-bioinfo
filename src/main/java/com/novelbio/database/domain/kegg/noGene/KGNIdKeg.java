package com.novelbio.database.domain.kegg.noGene;

public class KGNIdKeg {
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
	private String attribute;
	/**
	 * �趨KeggID�����ԣ�Ҳ���ǵ�����Drug����Compound����������
	 * @param atrribute
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	/**
	 * ���KeggID�����ԣ�Ҳ���ǵ�����Drug����Compound����������
	 * @return
	 */
	public String getAttribute() {
		return this.attribute;
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
}
