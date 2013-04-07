package com.novelbio.database.domain.geneanno;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
/**
 * ibatis在操作数据库时会自动使用类中的setter和getter给属性赋值
 * 如果不想用类中的这些方法，那么setter和getter的名字不要和属性一样就好
 * 如：属性：symbol，方法则为：getSymb
 * 当有新的物种加入时，需要添加新的dbinfo信息
 * 否则默认是走NCBIID
 * @author zong0jie
 *
 */
public abstract class AGeneInfo {
	private static final Logger logger = Logger.getLogger(AGeneInfo.class);
	
	private String typeOfGene;
	
	private Map<DBInfo, String> mapSymbol = new HashMap<DBInfo, String>();
	private Map<DBInfo, String> mapDescription = new HashMap<DBInfo, String>();

	private Set<String> setSynonyms = new HashSet<String>();
	private Set<String> setDbXrefs = new HashSet<String>();
	private Set<String> setSymNome = new HashSet<String>();
	/** 文献中的全名 */
	private Set<String> setFullNameNome = new HashSet<String>();
	
	private String modDate;
	private Set<String> setPubmedIDs = new HashSet<String>();
	
	@Indexed
	private int taxID = 0;
	
	@Transient
	private DBInfo dbInfo;
	
	public abstract String getGeneUniID();
	public abstract void setGeneUniID(String geneUniID);

	/**
	 * 这个不用设定。copedID会去设定
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	public DBInfo getDbInfo() {
		return dbInfo;
	}
	/**
	 * 必须第一时间设定，按照不同的物种
	 * @param fromDB
	 */
	public void setDBinfo(DBInfo dbInfo) {
		this.dbInfo = dbInfo;
	}
	
	/**
	 * 可以连续不断的设定好几次
	 * 有几篇文献就设定几次
	 * @param pubmedID
	 */
	public void addPubID(String pubmedID) {
		if (pubmedID != null) {
			setPubmedIDs.add(pubmedID.trim());
		}
	}
	
	/**
	 * 如果没有pubmedID，则返回一个空的arraylist，方便用foreach语句
	 * @return
	 */
	public Set<String> getPubmedIDs() {
		return setPubmedIDs;
	}

	public String getSymb() {
		return mapSymbol.values().iterator().next();
	}
	
	public String getSymb(DBInfo dbInfo) {
		return mapSymbol.get(dbInfo);
	}
	/**
	 * 故意名字起的和symbol不一样，这样可以防止自动注入
	 * @param symbol
	 */
	public void setSymb(DBInfo dbInfo, String symbol) {
		mapSymbol.put(dbInfo, symbol);
	}
	/**
	 * synonyms
	 * @return
	 */
	public Set<String> getSynonym() {
		return setSynonyms;
	}
	public void addSynonym(String synonyms) {
		setSynonyms.add(synonyms);
	}
	
	public Set<String> getDbXref() {
		return setDbXrefs;
	}
	public void addDbXref(String dbXrefs) {
		setDbXrefs.add(dbXrefs);
	}  
	/**
	 * description<br>
	 * 没有就返回""
	 * @return
	 */
	public String getDescrp() {
		String descrip = mapDescription.values().iterator().next();
		if (descrip == null) {
			descrip = "";
		}
		return descrip.replaceAll("\"", "");
	}
	
	public void setDescrp(DBInfo dbInfo, String description) {
		if (description == null) return;
		description = description.replaceAll("\"", "").trim();
		if (description.equals("") || description.equals("-")) return;
		
		mapDescription.put(dbInfo, description);
	}

	public String getTypeOfGene() {
		return typeOfGene;
	}
	
	public void setTypeOfGene(String typeOfGene) {
		if (typeOfGene == null) return;
		typeOfGene = typeOfGene.trim();
		if (typeOfGene.equals("") || typeOfGene.equals("-")) return;
		
		this.typeOfGene = typeOfGene.trim();
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public Set<String> getSymNom() {
		return setSymNome;
	}
	
	public void addSymNom(String symNome) {
		if (symNome.equals("") || symNome.equals("-")) return;
		this.setSymNome.add(symNome);
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public Set<String> getFullName() {
		return setFullNameNome;
	}
	public void addFullName(String fullNameFromNomenclature) {
		this.setFullNameNome.add(fullNameFromNomenclature);
	}

	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}
	
	public boolean addInfo(AGeneInfo geneInfo) {
		boolean update = false;
		for (DBInfo dbInfo : geneInfo.mapDescription.keySet()) {
			if (!mapDescription.containsKey(dbInfo)) {
				mapDescription.put(dbInfo, geneInfo.mapDescription.get(dbInfo));
				update = true;
			}
		}
		for (DBInfo dbInfo : geneInfo.mapSymbol.keySet()) {
			if (!mapSymbol.containsKey(dbInfo)) {
				mapSymbol.put(dbInfo, geneInfo.mapSymbol.get(dbInfo));
				update = true;
			}
		}
		update = update || addInfo(setDbXrefs, geneInfo.setDbXrefs);
		update = update || addInfo(setFullNameNome, geneInfo.setFullNameNome);
		update = update || addInfo(setPubmedIDs, geneInfo.setPubmedIDs);
		update = update || addInfo(setSymNome, geneInfo.setSymNome);
		update = update || addInfo(setSynonyms, geneInfo.setSynonyms);
		return update;
	}
	
	/**
	 * 将SetOther装到setThis里面，如果setOther中出现了新的item，则返回true
	 * 否则返回false
	 * @param setThis
	 * @param setOther
	 * @return
	 */
	protected static<T>  boolean addInfo(Set<T> setThis, Set<T> setOther) {
		boolean add = false;
		for (T t : setOther) {
			if (!setThis.contains(t)) {
				setThis.add(t);
				add = true;
			}
		}
		return add;
	}
}
