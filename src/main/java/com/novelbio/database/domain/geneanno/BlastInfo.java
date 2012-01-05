package com.novelbio.database.domain.geneanno;

import java.util.Date;

/**
 * 
 * @author zong0jie
 * 按照evalue从小到大排序
 */
public class BlastInfo implements Comparable<BlastInfo>{
	/**
	 * blast的subject来自于NCBIID表
	 */
	public static String SUBJECT_TAB_NCBIID = "NCBIID";
	/**
	 * blast的subject来自于UniprotID表
	 */
	public static String SUBJECT_TAB_UNIPROTID = "UniprotID";
	
	private String queryID;
	/**
	 * 设置查找的序列ID
	 */
	public void setQueryID(String queryID) 
	{
		this.queryID=queryID.trim();
	}
	/**
	 * 设置搜到的序列ID
	 */
	public String getQueryID() {
		return this.queryID.trim();
	}
///////////////////////////////////////////////////////////////////////////////////
	private int queryTax;
	/**
	 * 设置查找序列的物种
	 */
	public void setQueryTax(int queryTax) 
	{
		this.queryTax=queryTax;
	}
	/**
	 * 设置查找序列的物种
	 */
	public int getQueryTax() {
		return this.queryTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String queryDB;
	/**
	 * 设置查找序列的来源，譬如Agilent
	 */
	public void setQueryDB(String queryDB) 
	{
		this.queryDB=queryDB;
	}
	/**
	 * 获得查找序列的来源，譬如Agilent
	 */
	public String getQueryDB() {
		return this.queryDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectID;
	/**
	 * 设置Blast搜索到的序列的ID
	 */
	public void setSubjectID(String subjectID) 
	{
		this.subjectID=subjectID;
	}
	/**
	 * 获得Blast搜索到的序列的ID
	 */
	public String getSubjectID() {
		return this.subjectID;
	}
///////////////////////////////////////////////////////////////////////////////////
	private int subjectTax;
	/**
	 * 设置Blast搜索到的序列的物种
	 */
	public void setSubjectTax(int subjectTax) 
	{
		this.subjectTax=subjectTax;
	}
	/**
	 * 获得Blast搜索到的序列的物种
	 */
	public int getSubjectTax() {
		return this.subjectTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectDB;
	/**
	 * 设置Blast搜索到的序列的来源，如agilent
	 */
	public void setSubjectDB(String subjectDB) 
	{
		this.subjectDB=subjectDB;
	}
	/**
	 * 获得Blast搜索到的序列的来源，如agilent
	 */
	public String getSubjecttDB() {
		return this.subjectDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	private double identities=0;
	/**
	 * 设置查找的相似度,初值为0
	 */
	public void setIdentities(double identities) 
	{
		this.identities=identities;
	}
	/**
	 * 设置查找的相似度,初值为0
	 */
	public double getIdentities() {
		return this.identities;
	}
///////////////////////////////////////////////////////////////////////////////////
	private double evalue = 100;
	/**
	 * 设置查找的evalue，初值为100
	 */
	public void setEvalue(double evalue) 
	{
		this.evalue=evalue;
	}
	/**
	 * 获得查找的evalue，初值为100
	 */
	public double getEvalue() {
		return this.evalue;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String blastDate;
	/**
	 * 设置查找的日期
	 */
	public void setBlastDate(String blastDate) 
	{
		this.blastDate=blastDate;
	}
	/**
	 * 获得查找的日期
	 */
	public String getBlastDate() {
		return this.blastDate;
	}
///////////////////////////////////////////////////////////////////////////////////
	private String subjectTab;
	/**
	 * 设置blast得到的数据是基于哪个表的，有NCBIID和UniprotID两个选择
	 */
	public void setSubjectTab(String subjectTab) 
	{
		this.subjectTab=subjectTab;
	}
	/**
	 * 获得blast得到的数据是基于哪个表的，有NCBIID和UniprotID两个选择
	 */
	public String getSubjectTab() {
		return this.subjectTab;
	}
	/**
	 * 按照evalue从小到大排序
	 */
	@Override
	public int compareTo(BlastInfo o) {
		Double evalueThis = evalue;
		Double evalueO = o.getEvalue();
		return evalueThis.compareTo(evalueO);
	}

}
