package com.novelbio.database.domain.geneanno;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 
 * @author zong0jie
 * 按照evalue从小到大排序
 */
@Document(collection = "blastInfo")
@CompoundIndexes({
    @CompoundIndex(name = "queryID_queryTax_idx", def = "{'queryID': 1, 'queryTax': -1 , 'subjectTax': 1}")
 })
public class BlastInfo implements Comparable<BlastInfo> {
	@Id
	private String id;
	protected String queryID;
	@Indexed
	protected String subjectID;
	
	protected String blastDate;

	protected double identities=0;
	protected double evalue = 100;
	
	protected int queryTax;
	protected int subjectTax;

	private int subjectTab;
	
	@Transient
	GeneID geneIDQ = null;
	@Transient
	GeneID geneIDS = null;
	
	@DBRef
	protected DBInfo queryDB;
	@DBRef
	protected DBInfo subjectDB;
	
	public BlastInfo() {
		setDate();
	}
	
	private void setDate() {
	     blastDate = DateUtil.getDate(); //将日期时间格式化
	}
	
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(String AccIDQ, int taxIDQ , String AccIDS, int taxIDS) {
		setDate();
		if (taxIDQ < 0) taxIDQ = 0;
		if (taxIDS < 0) taxIDS = 0;
		
	     if (AccIDQ != null && !AccIDQ.equals("")) {
			geneIDQ = new GeneID(AccIDQ, taxIDQ);
			this.queryID = geneIDQ.getGeneUniID();
			this.queryTax = geneIDQ.getTaxID();
	     }
		
	     if (AccIDS != null && !AccIDS.equals("")) {
	    	 geneIDS = new GeneID(AccIDS, taxIDS);
	    	 this.subjectID = geneIDS.getGeneUniID();
	    	 this.subjectTax = geneIDS.getTaxID();
	    	 this.subjectTab = geneIDS.getIDtype();
	     }
	}
	
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(String AccIDQ, int taxIDQ , String genUniIDS, int IDType,int taxIDS) {
		setDate();
		if (taxIDQ < 0) taxIDQ = 0;
		if (taxIDS < 0) taxIDS = 0;
		
	     if (AccIDQ != null && !AccIDQ.equals("")) {
			geneIDQ = new GeneID(AccIDQ, taxIDQ);
			this.queryID = geneIDQ.getGeneUniID();
			this.queryTax = geneIDQ.getTaxID();
	     }
		
	     if (genUniIDS != null && !genUniIDS.equals("")) {
	    	 geneIDS = new GeneID(IDType, genUniIDS, taxIDS);
	    	 this.subjectID = geneIDS.getGeneUniID();
	    	 this.subjectTax = geneIDS.getTaxID();
	    	 this.subjectTab = geneIDS.getIDtype();
	     }
	}
	/**
	 * 如果是要导入数据库，必须用该方式new一个<br>
	 * 还需要设定evalue, identity和queryDB, subjectDB
	 */
	public BlastInfo(int taxIDQ, String genUniQ, int taxIDS, String genUniS) {
		setDate();
		
		if (taxIDQ < 0) taxIDQ = 0;
		if (taxIDS < 0) taxIDS = 0;
		
		try {
			geneIDQ = new GeneID(GeneID.IDTYPE_GENEID, genUniQ, taxIDQ);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (genUniQ != null && !genUniQ.equals("") && !genUniQ.equals("0")) {
			this.queryID = geneIDQ.getGeneUniID();
			this.queryTax = geneIDQ.getTaxID();
		}
		/////////////////////////////////////////////////////////////////////////////
		try {
			geneIDS = new GeneID(GeneID.IDTYPE_GENEID, genUniS, taxIDS);
		} catch (Exception e) {
			geneIDS = new GeneID(GeneID.IDTYPE_UNIID, genUniS, taxIDS);
		}
		
		if (genUniS != null && !genUniS.equals("") && !genUniS.equals("0")) {
			this.subjectID = geneIDS.getGeneUniID();
			this.subjectTax = geneIDS.getTaxID();
			this.subjectTab = geneIDS.getIDtype();
		}
	}
	
	public GeneID getGeneIDQ() {
		return geneIDQ;
	}
	public GeneID getGeneIDS() {
		return geneIDS;
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
	 * 获得查找序列的来源，譬如Agilent
	 */
	public DBInfo getQueryDB() {
		return queryDB;
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
	/** 获得Blast搜索到的序列的来源，如agilent */
	public DBInfo getSubjecttDB() {
		return subjectDB;
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
