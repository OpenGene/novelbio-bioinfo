package com.novelbio.database.domain.geneanno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServDBInfo;
import com.novelbio.database.service.servgeneanno.ServDBInfoMongo;

/**
 * 
 * @author zong0jie
 * 按照evalue从小到大排序
 */
public class BlastInfo implements Comparable<BlastInfo> {
	@Id
	private String id;
	@Indexed
	protected String queryID;
	@Indexed
	protected String subjectID;
	
	protected String blastDate;
	protected String subjectDBID;
	protected double identities=0;
	protected double evalue = 100;
	protected int subjectTax;
	
	protected String queryDBID;
	protected int queryTax;
	
	private int subjectTab;
	
	@Transient
	GeneID copedIDQ = null;
	@Transient
	GeneID copedIDS = null;
	
	@Transient
	@Autowired
	ServDBInfoMongo servDBInfo;
	
	public BlastInfo() {
		servDBInfo = new ServDBInfoMongo();
		setDate();
	}
	
	private void setDate() {
	     blastDate = DateTime.getDate(); //将日期时间格式化
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
			this.queryID = copedIDQ.getGeneUniID();
			this.queryTax = copedIDQ.getTaxID();
	     }
		
	     if (AccIDS != null && !AccIDS.equals("")) {
	    	 copedIDS = new GeneID(AccIDS, taxIDS);
	    	 this.subjectID = copedIDS.getGeneUniID();
	    	 this.subjectTax = copedIDS.getTaxID();
	    	 this.subjectTab = copedIDS.getIDtype();
	     }
	}
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(String AccIDQ, int taxIDQ , String genUniIDS, int IDType,int taxIDS) {
		setDate();
		if (taxIDQ < 0)
			taxIDQ = 0;
		if (taxIDS < 0)
			taxIDS = 0;
		
	     if (AccIDQ != null && !AccIDQ.equals("")) {
			copedIDQ = new GeneID(AccIDQ, taxIDQ);
			this.queryID = copedIDQ.getGeneUniID();
			this.queryTax = copedIDQ.getTaxID();
	     }
		
	     if (genUniIDS != null && !genUniIDS.equals("")) {
	    	 copedIDS = new GeneID(IDType, genUniIDS, taxIDS);
	    	 this.subjectID = copedIDS.getGeneUniID();
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
			this.queryID = copedIDQ.getGeneUniID();
			this.queryTax = copedIDQ.getTaxID();
		}
		/////////////////////////////////////////////////////////////////////////////
		try {
			copedIDS = new GeneID(GeneID.IDTYPE_GENEID, genUniS, taxIDS);
		} catch (Exception e) {
			copedIDS = new GeneID(GeneID.IDTYPE_UNIID, genUniS, taxIDS);
		}
		
		if (genUniS != null && !genUniS.equals("") && !genUniS.equals("0")) {
			this.subjectID = copedIDS.getGeneUniID();
			this.subjectTax = copedIDS.getTaxID();
			this.subjectTab = copedIDS.getIDtype();
		}
	}
	
	public GeneID getGeneIDQ() {
		return copedIDQ;
	}
	public GeneID getGeneIDS() {
		return copedIDS;
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
	 * @param queryDBInfo
	 * @param subDBInfo
	 */
	public void setQueryDB_SubDB(DBInfo queryDBInfo, DBInfo subDBInfo) {
		this.queryDBID = queryDBInfo.getDbInfoID();
		this.subjectDBID = subDBInfo.getDbInfoID();
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
	public void setQueryDBID(String queryDBID) {
		this.queryDBID = queryDBID;
	}
	/**
	 * 获得查找序列的来源，譬如Agilent
	 */
	public DBInfo getQueryDB() {
		return servDBInfo.findOne(queryDBID);
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
	public void setSubjectTax(int subjectTax) {
		this.subjectTax=subjectTax;
	}
	/**
	 * 获得Blast搜索到的序列的物种
	 */
	public int getSubjectTax() {
		return this.subjectTax;
	}
///////////////////////////////////////////////////////////////////////////////////
	/** 设置Blast搜索到的序列的来源，如agilent */
	public void setSubjectDBID(String subjectDBID) {
		this.subjectDBID = subjectDBID;
	}
	/** 获得Blast搜索到的序列的来源，如agilent */
	public DBInfo getSubjecttDB() {
		return servDBInfo.findOne(subjectDBID);
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
	
	/**
	 * 设置blast得到的数据是基于哪个表的，有NCBIID和UniprotID两个选择
	 */
	public void setSubTab(int subjectTab) {
		this.subjectTab = subjectTab;
	}
	/**
	 * 获得blast得到的数据是基于哪个表的，有NCBIID和UniprotID两个选择
	 */
	public int getSubTab() {
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
