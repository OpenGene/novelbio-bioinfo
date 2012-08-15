package com.novelbio.database.domain.geneanno;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 
 * @author zong0jie
 * ����evalue��С��������
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
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     blastDate = formatDate.format(currentDate); //������ʱ���ʽ��
	}
	public BlastInfo() {
		setDate();
	}
	/**
	 * �����Ҫ�������ݿ⣬�����ø÷�ʽnewһ��<br>
	 * ����Ҫ�趨evalue, identity��queryDB, subjectDB
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
	 * �����Ҫ�������ݿ⣬�����ø÷�ʽnewһ��<br>
	 * ����Ҫ�趨evalue, identity��queryDB, subjectDB
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
	 * �����Ҫ�������ݿ⣬�����ø÷�ʽnewһ��<br>
	 * ����Ҫ�趨evalue, identity��queryDB, subjectDB
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
	 * ����һ���趨�ȽϷ���
	 * @param evalue
	 * @param identities
	 */
	public void setEvalue_Identity(double evalue, double identities) {
		this.evalue = evalue;
		this.identities = identities;
	}
	/**
	 * ����һ���趨�ȽϷ���
//	 * @param queryDBInfo
//	 * @param subDBInfo
	 */
	public void setQueryDB_SubDB(String queryDBInfo, String subDBInfo) {
		this.queryDB = queryDBInfo;
		this.subjectDB = subDBInfo;
	}
	/**
	 * ���ò��ҵ�����ID
	 */
	public void setQueryID(String queryID) {
		this.queryID=queryID.trim();
	}
	/**
	 * �����ѵ�������ID
	 */
	public String getQueryID() {
		return this.queryID.trim();
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���ò������е�����
	 */
	public void setQueryTax(int queryTax) {
		this.queryTax=queryTax;
	}
	/**
	 * ���ò������е�����
	 */
	public int getQueryTax() {
		return this.queryTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���ò������е���Դ��Ʃ��Agilent
	 */
	public void setQueryDB(String queryDB) {
		this.queryDB=queryDB;
	}
	/**
	 * ��ò������е���Դ��Ʃ��Agilent
	 */
	public String getQueryDB() {
		return this.queryDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����Blast�����������е�genUniID
	 */
	public void setSubjectID(String subjectID) {
		this.subjectID=subjectID;
	}

	/**
	 * ���Blast�����������е�ID
	 */
	public String getSubjectID() {
		return this.subjectID;
	}
///////////////////////////////////////////////////////////////////////////////////
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
	/**
	 * ����Blast�����������е���Դ����agilent
	 */
	public void setSubjectDB(String subjectDB) {
		this.subjectDB=subjectDB;
	}
	/**
	 * ���Blast�����������е���Դ����agilent
	 */
	public String getSubjecttDB() {
		return this.subjectDB;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���ò��ҵ����ƶ�,��ֵΪ0
	 */
	public void setIdentities(double identities) {
		this.identities=identities;
	}
	/**
	 * ���ò��ҵ����ƶ�,��ֵΪ0
	 */
	public double getIdentities() {
		return this.identities;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���ò��ҵ�evalue����ֵΪ100
	 */
	public void setEvalue(double evalue) {
		this.evalue=evalue;
	}
	/**
	 * ��ò��ҵ�evalue����ֵΪ100
	 */
	public double getEvalue() {
		return this.evalue;
	}
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���ò��ҵ�����
	 */
	public void setBlastDate(String blastDate) {
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
	public void setSubjectTab(String subjectTab) {
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
		Double identityThis = identities;
		Double identityO = o.identities;
		int result = evalueThis.compareTo(evalueO);
		if (result == 0) {
			result = -identityThis.compareTo(identityO);
		}
		return result;
	}

}
