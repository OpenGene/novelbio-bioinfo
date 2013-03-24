package com.novelbio.database.domain.geneanno;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.service.servgeneanno.ServDBInfo;
import com.novelbio.database.service.servgeneanno.ServDBInfoMongo;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

/**
 * 重写了equal和hash
 * 要两个ncbiid的geneID和GOID都相同，才认为这两个NCBIID相同
 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
 * 	@Override
 */
public abstract class AGene2Go {
	private static Logger logger = Logger.getLogger(AGene2Go.class);
	public static final String EVIDENCE_IEA = "IEA";
	
	@Indexed
	private String goID;
	@Indexed
	private int taxID;
	
	private Set<String> setEvid = new HashSet<String>();
	private String qualifier;
	private Set<String> setPubID = new HashSet<String>();
	private Set<String> setDB = new HashSet<String>();


	
	private ServGo2Term servGo2Term = new ServGo2Term();
	private ServDBInfoMongo servDBInfo = new ServDBInfoMongo();
	

	public abstract String getGeneUniId();
	public abstract void setGeneUniID(String geneUniID);
	
	public String getGOID() {
		Go2Term go2Term = getGO2Term();
		if (go2Term == null) {
			return null;
		}
		return go2Term.getGoID();
	}
	public Go2Term getGO2Term() {
		Go2Term go2Term = servGo2Term.getHashGo2Term().get(goID);
		if (go2Term == null) {
			logger.error("出现未知GOID：" + goID);
			return null;
		}
		return go2Term;
	}
	
	public void setGOID(String GoID) {
		if (GoID == null) {
			logger.error("GOID未知");
			return;
		}
		GoID = GoID.trim();
		if (GoID == null || GoID.trim().equals("")) {
			return;
		}
		try {
			this.goID = servGo2Term.getHashGo2Term().get(GoID).getGoID();
		} catch (Exception e) {
			this.goID = GoID;
		}
	}
	public void setTaxID(int taxID) {
		if (taxID == 0) {
			return;
		}
		if (this.taxID == 0) {
			this.taxID = taxID;
			return;
		}
		if (this.taxID != taxID) {
			logger.error("待拷贝的两个geneInfo中的taxID不一致，原taxID："+this.taxID + " 新taxID：" + taxID );
		}
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
		if (setEvid == null) {
			setEvid = new HashSet<String>();
		}
		setEvid.add(evidence);
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
	/**
	 * 根据Go2Term进行了校正
	 * 没有就返回null
	 * @return
	 */
	public String getGOTerm() {
		try {
			return servGo2Term.getHashGo2Term().get(goID).getGoTerm();
		} catch (Exception e) {
			return null;
		}
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
			return servGo2Term.getHashGo2Term().get(goID).getGOtype();
		} catch (Exception e) {
			logger.error("出现未知GOID：" + goID);
			return null;
		}
	}
	/**
	 * 可能会被sep分割
	 * @return
	 */
	public Set<DBInfo> getDataBase() {
		Set<DBInfo> setDbInfos = new HashSet<DBInfo>();
		for (String dbInfoID : setDB) {
			setDbInfos.add(servDBInfo.findOne(dbInfoID));
		}
		return setDbInfos;
	}
	public void addDBName(String dbName) {
		DBInfo dbInfo = servDBInfo.findByDBname(dbName);
		if (dbInfo != null) {
			setDB.add(dbInfo.getDbInfoID());
		}
	}
	
	public void addDBID(String dbInfoID) {
		this.setDB.add(dbInfoID);
	}
 
	private void addReference(String reference) {
		setPubID.add(reference);
	}
	
	/**
	 * true说明确实有新东西
	 * 如果信息重复，就不需要升级，则返回false
	 * @param gene2Go
	 * @return
	 */
	public void copyInfo(AGene2Go gene2Go) {
		for (DBInfo dbInfo : gene2Go.getDataBase()) {
			addDBID(dbInfo.getDbInfoID());
		}
		for (String evidence : gene2Go.getEvidence()) {
			addEvidence(evidence);
		}
		
		setGeneUniID(gene2Go.getGeneUniId());
		setGOID(gene2Go.getGOID());
		setQualifier(gene2Go.getQualifier());
		for (String pubmedID : gene2Go.getReference()) {
			addReference(pubmedID);
		}
		setTaxID(gene2Go.getTaxID());
	}

	/**
	 * 将新的gene2Go的信息往这个里面添加，如果true，说明需要update
	 * false不需要update
	 * 添加了Database，Qualifier，Reference和Evidence
	 * @param gene2Go
	 * @return
	 */
	public boolean addInfo(AGene2Go gene2Go) {
		boolean update = false;
		for (DBInfo dbInfo : gene2Go.getDataBase()) {
			if (!setDB.contains(dbInfo.getDbInfoID())) {
				setDB.add(dbInfo.getDbInfoID());
				update = true;
			}
		}
		for (String evidence : gene2Go.getEvidence()) {
			if (!setEvid.contains(evidence)) {
				setEvid.add(evidence);
				update = true;
			}
		}
		for (String pubmedID : gene2Go.getReference()) {
			if (!setPubID.contains(pubmedID)) {
				setPubID.add(pubmedID);
				update = true;
			}
		}
		setGeneUniID(gene2Go.getGeneUniId());
		setGOID(gene2Go.getGOID());
		setQualifier(gene2Go.getQualifier());
		for (String pubmedID : gene2Go.getReference()) {
			addReference(pubmedID);
		}
		setTaxID(gene2Go.getTaxID());
		return update;
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
		
		if(getGeneUniId() == null || getGeneUniId().trim().equals("") || otherObj.getGeneUniId() == null || otherObj.getGeneUniId().trim().equals(""))
		{
			return false;
		}
		
		if (getGeneUniId().equals("0") || otherObj.getGeneUniId().equals("0") ) {
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
