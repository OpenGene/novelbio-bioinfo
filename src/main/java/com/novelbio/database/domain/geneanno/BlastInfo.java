package com.novelbio.database.domain.geneanno;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * <b>导入数据库的时候数据库中必须已经存在了subjectID</b><br>
 * 如果数据库中不存在queryID，而又需要导入，则先导入queryID，再导入blast信息
 * @author zong0jie
 * 按照evalue从小到大排序
 */
@Document(collection = "blastInfo")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "queryID_queryTax_subTax_subID_idx",
    		def = "{'queryID': 1, 'queryTax': -1 , 'subjectTax': 1, 'subjectID': -1}")
 })
public class BlastInfo implements Comparable<BlastInfo> {
	@Id
	private String id;
	private String queryID;
	@Indexed
	private String subjectID;
	
	private String blastDate;

	private double identities = 0;
	private double evalue = 100;
	private int score = 0;
	private int alignLen = 0;
	
	protected int queryTax;
	protected int queryIDtype;
	protected int subjectTax;
	private int subjectIDtype;

	@Transient
	GeneID geneIDS = null;
	
	public BlastInfo() {
		setDate();
	}
	
	/**
	 * 用 <b>-m8</b> 参数跑出来的Blast程序跑出来的结果
	 * @param taxIDQ queryTaxID
	 * @param taxIDS subjectTaxID
	 * @param blastStr blast的具体ID
	 * @param isBlastIDtype 如果subjecdt是accID，具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public BlastInfo(int taxIDQ, int taxIDS, String blastStr, boolean isBlastIDtype) {
		this(taxIDQ, false, taxIDS, false, blastStr, isBlastIDtype);
	}
	
	/**
	 * 用 <b>-m8</b> 参数跑出来的Blast程序跑出来的结果
	 * @param taxIDQ queryTaxID
	 * @param isGeneIDQ queryID是否为accID，一般都是true
	 * @param taxIDS subjectTaxID
	 * @param isGeneIDS subjectID是否为accID，一般都是true
	 * @param blastStr blast的具体ID
	 * @param isBlastIDtype 如果subjecdt是accID，具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public BlastInfo(int taxIDQ, boolean isGeneIDQ, int taxIDS, boolean isGeneIDS, String blastStr, boolean isBlastIDtype) {
		setDate();
		String[] blastInfo = blastStr.split("\t");
		GeneID geneIDQ;
		if (!isGeneIDQ) {
			geneIDQ = new GeneID(blastInfo[0], taxIDQ);
		} else {
			geneIDQ = new GeneID(GeneID.IDTYPE_GENEID, blastInfo[0], taxIDQ);
			if (geneIDQ.getAccID_With_DefaultDB() == null) {
				geneIDQ = new GeneID(GeneID.IDTYPE_UNIID, blastInfo[0], taxIDQ);
			}
		}
		
		if (!isGeneIDS) {
			geneIDS = new GeneID(blastInfo[1], taxIDS, isBlastIDtype);
		} else {
			geneIDS = new GeneID(GeneID.IDTYPE_GENEID, blastInfo[1], taxIDQ);
			if (geneIDS.getAccID_With_DefaultDB() == null) {
				geneIDS = new GeneID(GeneID.IDTYPE_UNIID, blastInfo[1], taxIDQ);
			}
		}
		
		this.queryID = geneIDQ.getGeneUniID();
		this.queryTax = geneIDQ.getTaxID();
		this.queryIDtype = geneIDQ.getIDtype();
		
		this.subjectID = geneIDS.getGeneUniID();
		this.subjectTax = geneIDS.getTaxID();
		this.subjectIDtype = geneIDS.getIDtype();
		
		this.alignLen = Integer.parseInt(blastInfo[3].trim());
		this.evalue = Double.parseDouble(blastInfo[10].trim());
		this.identities = Double.parseDouble(blastInfo[2].trim());
		this.score = Integer.parseInt(blastInfo[11].trim());
	}
	
	/**
	 * 
	 * 仅仅获得geneIDQ的geneUniID, taxID ,IDtype
	 * @param queryID
	 * @param queryTax
	 * @param queryIDtype
	 */
	public void setGeneIDQ(String queryID, int queryTax, int queryIDtype) {
		this.queryID = queryID;
		this.queryTax = queryTax;
		this.queryIDtype = queryIDtype;
	}
	public void setGeneIDS(GeneID geneIDS) {
		this.geneIDS = geneIDS;
		this.subjectID = geneIDS.getGeneUniID();
		this.subjectTax = geneIDS.getTaxID();
		this.subjectIDtype = geneIDS.getIDtype();
	}
	/** mongodb中的id */
	public void setId(String id) {
		this.id = id;
	}
	/** mongodb中的id */
	public String getId() {
		return id;
	}
	
	private void setDate() {
	     blastDate = DateUtil.getDate(); //将日期时间格式化
	}
	
	public GeneID getGeneIDS() {
		if (geneIDS == null) {
			geneIDS = new GeneID(subjectIDtype, subjectID, subjectTax);
		}
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
	
	/** 设置搜到的序列ID */
	public String getQueryID() {
		return this.queryID.trim();
	}
	
	/** 设置查找序列的物种 */
	public int getQueryTax() {
		return this.queryTax;
	}
	
	public int getQueryIDtype() {
		return queryIDtype;
	}
	
	/**
	 * 获得Blast搜索到的序列的ID
	 */
	public String getSubjectID() {
		return this.subjectID;
	}

	/**
	 * 获得Blast搜索到的序列的物种
	 */
	public int getSubjectTax() {
		return this.subjectTax;
	}
	
	public int getSubjectIDtype() {
		return subjectIDtype;
	}
	
	/**
	 * 设置查找的相似度,初值为0
	 */
	public double getIdentities() {
		return this.identities;
	}

	/**
	 * 获得查找的evalue，初值为100
	 */
	public double getEvalue() {
		return this.evalue;
	}

	/**
	 * 获得查找的日期
	 */
	public String getBlastDate() {
		return this.blastDate;
	}

	public int getAlignLen() {
		return alignLen;
	}

	public double getScore() {
		return score;
	}

	/**
	 * 按照相似度排序
	 * -1 表示更可信
	 */
	@Override
	public int compareTo(BlastInfo o) {
		Double evalueThis = evalue;
		Double evalueO = o.getEvalue();
		Double identityThis = identities;
		Double identityO = o.identities;
		Integer scoreThis = score;
		Integer scoreO = o.score;
		int result = -scoreThis.compareTo(scoreO);
		if (result == 0) {
			result = -identityThis.compareTo(identityO);
		}
		if (result == 0) {
			result = evalueThis.compareTo(evalueO);
		}
		return result;
	}

}
