package com.novelbio.database.domain.geneanno;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.database.service.servgeneanno.ManageDBInfo;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

/**
 * 重写了equal和hash
 * 要两个ncbiid的geneID和GOID都相同，才认为这两个NCBIID相同
 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
 * 	@Override
 */
public abstract class AGene2Go {
	private static Logger logger = Logger.getLogger(AGene2Go.class);
	public static final String EVIDENCE_IEA = "IEA";
	
	private String goID;
	@Indexed
	private int taxID;
	
	private Set<String> setEvid = new HashSet<String>();
	private String qualifier;
	private Set<String> setPubID = new HashSet<String>();
	private Set<DBInfo> setDB = new HashSet<DBInfo>();
	
	@Transient
	ManageGo2Term manageGo2Term = new ManageGo2Term();
	@Transient
	ManageDBInfo manageDBInfo = new ManageDBInfo();
	
	public abstract String getGeneUniId();
	public abstract void setGeneUniID(String geneUniID);
	
	public String getGOID() {
		Go2Term go2Term = getGO2Term();
		if (go2Term == null) {
			return goID;
		}
		return go2Term.getGoID();
	}
	public Go2Term getGO2Term() {
		Go2Term go2Term = manageGo2Term.queryGo2Term(goID);
		if (go2Term == null) {
			logger.error("出现未知GOID：" + goID);
			return null;
		}
		return go2Term;
	}
	
	public void setGOID(String GoID) {
		if (GoID == null || GoID.trim().equals("")) {
			logger.error("GOID未知");
			return;
		}
		GoID = GoID.trim();
		try {
			this.goID = manageGo2Term.queryGo2Term(GoID).getGoID();
		} catch (Exception e) {
			this.goID = GoID;
		}
	}
	
	public void setTaxID(int taxID) {
		if (taxID <= 0) {
			return;
		}
		this.taxID = taxID;
	}
	
	public int getTaxID() {
		return taxID;
	}
	
	public Set<String> getEvidence() {
		return setEvid;
	}
	
	public void addEvidence(String evidence) {
		if (evidence == null || evidence.trim().equals("")) {
			return;
		}
		setEvid.add(evidence.trim());
	}
	
	public String getQualifier() {
		return qualifier;
	}
	
	public void setQualifier(String qualifier) {
		if (qualifier == null) {
			return;
		}
		qualifier = qualifier.trim();
		if (qualifier.equals("") || qualifier.equals("-")) {
			return;
		}
		this.qualifier = qualifier;
	}
	
	public Set<String> getReference() {
		return setPubID;
	}
	
	/**
	 * 根据Go2Term进行了校正
	 * 没有就返回null<br>
	 * FUN_SHORT_BIO_P<br>
	 * FUN_SHORT_CEL_C<br>
	 * FUN_SHORT_MOL_F<br>
	 * @return
	 */
	public GOtype getFunction() {
		try {
			return manageGo2Term.queryGo2Term(goID).getGOtype();
		} catch (Exception e) {
			logger.error("出现未知GOID：" + goID);
			return null;
		}
	}

	public Set<DBInfo> getDataBase() {
		return setDB;
	}
	
	public void addDBName(String dbName) {
		DBInfo dbInfo = manageDBInfo.findByDBname(dbName);
		if (dbInfo != null) {
			setDB.add(dbInfo);
		}
	}
	
	public void addDBID(DBInfo dbInfo) {
		this.setDB.add(dbInfo);
	}
 
	public void addReference(String reference) {
		setPubID.add(reference);
	}

	/**
	 * 将新的gene2Go的信息往这个里面添加，如果true，说明需要update
	 * false不需要update
	 * 添加了Database，Qualifier，Reference和Evidence<br>
	 * geneUniID、GOID、TaxID、Qualifier不添加。
	 * @param gene2Go
	 * @return
	 */
	public boolean addInfo(AGene2Go gene2Go) {
		boolean update = false;
		update = update || GeneInfo.addInfo(setDB, gene2Go.setDB);
		update = update || GeneInfo.addInfo(setEvid, gene2Go.setEvid);
		update = update || GeneInfo.addInfo(setPubID, gene2Go.setPubID);
		if ((qualifier == null || qualifier.equals("")) && gene2Go.qualifier != null && !gene2Go.qualifier.equals("") && !qualifier.equals("-")) {
			qualifier = gene2Go.qualifier;
			update = true;
		}
		return update;
	}
	/**
	 * 浅层复制，set等仅仅引用传递
	 * 但是geneID不复制
	 */
	public void copyInfo(AGene2Go gene2Go) {
		this.goID = gene2Go.goID;
		this.qualifier = gene2Go.qualifier;
		this.setDB = gene2Go.setDB;
		this.setEvid = gene2Go.setEvid;
		this.setPubID = gene2Go.setPubID;
		this.taxID = gene2Go.taxID;
	}
	/**
	 * 只要两个gene2GO的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		AGene2Go otherObj = (AGene2Go)obj;
		
		if(getGeneUniId() == null || getGeneUniId().trim().equals("") 
				|| otherObj.getGeneUniId() == null || otherObj.getGeneUniId().trim().equals("")) {
			return false;
		}
		if (getGeneUniId().equals("0") || otherObj.getGeneUniId().equals("0")) {
			return false;
		}
		
		return getGeneUniId().equals(otherObj.getGeneUniId())&&getGOID().equals(otherObj.getGOID());
	}
	/**
	 * 重写hashcode，也是仅针对geneID
	 */
	public int hashCode(){
		String id = getGeneUniId()+getGOID();
		return id.hashCode(); 
	}
	
}
