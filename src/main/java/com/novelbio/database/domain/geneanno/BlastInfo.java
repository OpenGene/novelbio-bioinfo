package com.novelbio.database.domain.geneanno;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 
 * @author zong0jie
 * 按照evalue从小到大排序
 */
public class BlastInfo implements Comparable<BlastInfo>{
	GeneID copedIDQ = null;
	GeneID copedIDS = null;
	private String blastDate;

	private String subjectDB;
	private double identities=0;
	private double evalue = 100;
	private int subjectTax;
	private String subjectID;
	private String queryDB;
	private int queryTax;
	private String queryID;
	
	private void setDate() {
		SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd");
	     Date currentDate = new Date(); //得到当前系统时间
	     blastDate = formatDate.format(currentDate); //将日期时间格式化
	}
	public BlastInfo() {
		setDate();
	}
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(String AccIDQ, int taxIDQ , String AccIDS, int taxIDS) {
		setDate();
		if (taxIDQ < 0)
			taxIDQ = 0;
		if (taxIDS < 0)
			taxIDS = 0;
		
	     if (AccIDQ != null && !AccIDQ.equals("")) {
			copedIDQ = new GeneID(AccIDQ, taxIDQ);
			this.queryID = copedIDQ.getGenUniID();
			this.queryTax = copedIDQ.getTaxID();
	     }
		
	     if (AccIDS != null && !AccIDS.equals("")) {
	    	 copedIDS = new GeneID(AccIDS, taxIDS);
	    	 this.subjectID = copedIDS.getGenUniID();
	    	 this.subjectTax = copedIDS.getTaxID();
	    	 this.subjectTab = copedIDS.getIDtype();
	     }
	}
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(String AccIDQ, int taxIDQ , String genUniIDS, String IDType,int taxIDS) {
		setDate();
		if (taxIDQ < 0)
			taxIDQ = 0;
		if (taxIDS < 0)
			taxIDS = 0;
		
	     if (AccIDQ != null && !AccIDQ.equals("")) {
			copedIDQ = new GeneID(AccIDQ, taxIDQ);
			this.queryID = copedIDQ.getGenUniID();
			this.queryTax = copedIDQ.getTaxID();
	     }
		
	     if (genUniIDS != null && !genUniIDS.equals("")) {
	    	 copedIDS = new GeneID(IDType, genUniIDS, taxIDS);
	    	 this.subjectID = copedIDS.getGenUniID();
	    	 this.subjectTax = copedIDS.getTaxID();
	    	 this.subjectTab = copedIDS.getIDtype();
	     }
	}
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(int taxIDQ, String genUniQ, int taxIDS, String genUniS) {
		setDate();
		
		if (taxIDQ < 0)
			taxIDQ = 0;
		if (taxIDS < 0)
			taxIDS = 0;
		
		try {
			copedIDQ = new GeneID(GeneID.IDTYPE_GENEID, genUniQ, taxIDQ);
		} catch (Exception e) {
			copedIDQ = new GeneID(GeneID.IDTYPE_UNIID, genUniQ, taxIDQ);
		}
		
		if (genUniQ != null && !genUniQ.equals("") && !genUniQ.equals("0")) {
			this.queryID = copedIDQ.getGenUniID();
			this.queryTax = copedIDQ.getTaxID();
		}
		/////////////////////////////////////////////////////////////////////////////
		try {
			copedIDS = new GeneID(GeneID.IDTYPE_GENEID, genUniS, taxIDS);
		} catch (Exception e) {
			copedIDS = new GeneID(GeneID.IDTYPE_UNIID, genUniS, taxIDS);
		}
		
		if (genUniS != null && !genUniS.equals("") && !genUniS.equals("0")) {
			this.subjectID = copedIDS.getGenUniID();
			this.subjectTax = copedIDS.getTaxID();
			this.subjectTab = copedIDS.getIDtype();
		}
	}
	
	
	/**
	 * 两个一起设定比较方便
	 * @param evalue
	 * @param identities
	 */
	public void setEvalue_Identity(double evalue, double identities) {
		this.evalue = evalue;
		this.identities = identities;
	}
	/**
	 * 两个一起设定比较方便
//	 * @param queryDBInfo
//	 * @param subDBInfo
	 */
	public void setQueryDB_SubDB(String queryDBInfo, String subDBInfo) {
		this.queryDB = queryDBInfo;
		this.subjectDB = subDBInfo;
	}
	/**
	 * 设置查找的序列ID
	 */
	public void setQueryID(String queryID) {
		this.queryID=queryID.trim();
	}
	/**
	 * 设置搜到的序列ID
	 */
	public String getQueryID() {
		return this.queryID.trim();
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置查找序列的物种
	 */
	public void setQueryTax(int queryTax) {
		this.queryTax=queryTax;
	}
	/**
	 * 设置查找序列的物种
	 */
	public int getQueryTax() {
		return this.queryTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置查找序列的来源，譬如Agilent
	 */
	public void setQueryDB(String queryDB) {
		this.queryDB=queryDB;
	}
	/**
	 * 获得查找序列的来源，譬如Agilent
	 */
	public String getQueryDB() {
		return this.queryDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置Blast搜索到的序列的genUniID
	 */
	public void setSubjectID(String subjectID) {
		this.subjectID=subjectID;
	}

	/**
	 * 获得Blast搜索到的序列的ID
	 */
	public String getSubjectID() {
		return this.subjectID;
	}
///////////////////////////////////////////////////////////////////////////////////
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
	/**
	 * 设置Blast搜索到的序列的来源，如agilent
	 */
	public void setSubjectDB(String subjectDB) {
		this.subjectDB=subjectDB;
	}
	/**
	 * 获得Blast搜索到的序列的来源，如agilent
	 */
	public String getSubjecttDB() {
		return this.subjectDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置查找的相似度,初值为0
	 */
	public void setIdentities(double identities) {
		this.identities=identities;
	}
	/**
	 * 设置查找的相似度,初值为0
	 */
	public double getIdentities() {
		return this.identities;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置查找的evalue，初值为100
	 */
	public void setEvalue(double evalue) {
		this.evalue=evalue;
	}
	/**
	 * 获得查找的evalue，初值为100
	 */
	public double getEvalue() {
		return this.evalue;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设置查找的日期
	 */
	public void setBlastDate(String blastDate) {
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
	public void setSubjectTab(String subjectTab) {
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
		Double identityThis = identities;
		Double identityO = o.identities;
		int result = evalueThis.compareTo(evalueO);
		if (result == 0) {
			result = -identityThis.compareTo(identityO);
		}
		return result;
	}

}
