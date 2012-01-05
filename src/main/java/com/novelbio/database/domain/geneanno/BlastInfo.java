package com.novelbio.database.domain.geneanno;

import java.util.Date;

/**
 * 
 * @author zong0jie
 * ����evalue��С��������
 */
public class BlastInfo implements Comparable<BlastInfo>{
	/**
	 * blast��subject������NCBIID��
	 */
	public static String SUBJECT_TAB_NCBIID = "NCBIID";
	/**
	 * blast��subject������UniprotID��
	 */
	public static String SUBJECT_TAB_UNIPROTID = "UniprotID";
	
	private String queryID;
	/**
	 * ���ò��ҵ�����ID
	 */
	public void setQueryID(String queryID) 
	{
		this.queryID=queryID.trim();
	}
	/**
	 * �����ѵ�������ID
	 */
	public String getQueryID() {
		return this.queryID.trim();
	}
///////////////////////////////////////////////////////////////////////////////////
	private int queryTax;
	/**
	 * ���ò������е�����
	 */
	public void setQueryTax(int queryTax) 
	{
		this.queryTax=queryTax;
	}
	/**
	 * ���ò������е�����
	 */
	public int getQueryTax() {
		return this.queryTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String queryDB;
	/**
	 * ���ò������е���Դ��Ʃ��Agilent
	 */
	public void setQueryDB(String queryDB) 
	{
		this.queryDB=queryDB;
	}
	/**
	 * ��ò������е���Դ��Ʃ��Agilent
	 */
	public String getQueryDB() {
		return this.queryDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectID;
	/**
	 * ����Blast�����������е�ID
	 */
	public void setSubjectID(String subjectID) 
	{
		this.subjectID=subjectID;
	}
	/**
	 * ���Blast�����������е�ID
	 */
	public String getSubjectID() {
		return this.subjectID;
	}
///////////////////////////////////////////////////////////////////////////////////
	private int subjectTax;
	/**
	 * ����Blast�����������е�����
	 */
	public void setSubjectTax(int subjectTax) 
	{
		this.subjectTax=subjectTax;
	}
	/**
	 * ���Blast�����������е�����
	 */
	public int getSubjectTax() {
		return this.subjectTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectDB;
	/**
	 * ����Blast�����������е���Դ����agilent
	 */
	public void setSubjectDB(String subjectDB) 
	{
		this.subjectDB=subjectDB;
	}
	/**
	 * ���Blast�����������е���Դ����agilent
	 */
	public String getSubjecttDB() {
		return this.subjectDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	private double identities=0;
	/**
	 * ���ò��ҵ����ƶ�,��ֵΪ0
	 */
	public void setIdentities(double identities) 
	{
		this.identities=identities;
	}
	/**
	 * ���ò��ҵ����ƶ�,��ֵΪ0
	 */
	public double getIdentities() {
		return this.identities;
	}
///////////////////////////////////////////////////////////////////////////////////
	private double evalue = 100;
	/**
	 * ���ò��ҵ�evalue����ֵΪ100
	 */
	public void setEvalue(double evalue) 
	{
		this.evalue=evalue;
	}
	/**
	 * ��ò��ҵ�evalue����ֵΪ100
	 */
	public double getEvalue() {
		return this.evalue;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String blastDate;
	/**
	 * ���ò��ҵ�����
	 */
	public void setBlastDate(String blastDate) 
	{
		this.blastDate=blastDate;
	}
	/**
	 * ��ò��ҵ�����
	 */
	public String getBlastDate() {
		return this.blastDate;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectTab;
	/**
	 * ����blast�õ��������ǻ����ĸ���ģ���NCBIID��UniprotID����ѡ��
	 */
	public void setSubjectTab(String subjectTab) 
	{
		this.subjectTab=subjectTab;
	}
	/**
	 * ���blast�õ��������ǻ����ĸ���ģ���NCBIID��UniprotID����ѡ��
	 */
	public String getSubjectTab() {
		return this.subjectTab;
	}
	/**
	 * ����evalue��С��������
	 */
	@Override
	public int compareTo(BlastInfo o) {
		Double evalueThis = evalue;
		Double evalueO = o.getEvalue();
		return evalueThis.compareTo(evalueO);
	}

}
