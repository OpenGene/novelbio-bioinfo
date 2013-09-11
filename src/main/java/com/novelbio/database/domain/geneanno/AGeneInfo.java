package com.novelbio.database.domain.geneanno;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.base.SepSign;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
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
	@Id
	String id;
	
	private String typeOfGene;
	
	private Map<String, String> mapSymbol = new HashMap<String, String>();
	private Map<String, String> mapDescription = new HashMap<String, String>();

	private Set<String> setSynonyms = new HashSet<String>();
	private Set<String> setDbXrefs = new HashSet<String>();
	/** Symbol_from_nomenclature_authority */
	private Set<String> setSymNome = new HashSet<String>();
	/** 文献中的全名 */
	private Set<String> setFullNameNome = new HashSet<String>();
	
	private String modDate;
	private Set<String> setPubmedIDs = new HashSet<String>();
	
	@Indexed
	private int taxID = 0;
	
	@Transient
	private DBInfo dbInfo;
	@Transient
	ManageDBInfo manageDBInfo = new ManageDBInfo();
	
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
	 * 必须第一时间设定，按照不同的物种
	 * @param fromDB
	 */
	public void setDBinfo(String dbInfo) {
		this.dbInfo = manageDBInfo.findByDBname(dbInfo);
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
	 * 可以连续不断的设定好几次
	 * 有几篇文献就设定几次
	 * @param pubmedID
	 */
	public void addPubID(Collection<String> colPubmedID) {
		if (colPubmedID != null) {
			for (String pubmedID : colPubmedID) {
				setPubmedIDs.add(pubmedID.trim());
			}
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
		String symbol = null;
		if (mapSymbol == null || mapSymbol.size() == 0) {
			return "";
		}
		if (dbInfo != null) {
			symbol = mapSymbol.get(dbInfo.getDbNameLowcase());
		}
		if (symbol == null || symbol.equals("")) {
			symbol = mapSymbol.values().iterator().next();
		}
		if (symbol != null) {
			return symbol.replaceAll("\"", "");
		}
		return "";
	}
	
	/**
	 * 故意名字起的和symbol不一样，这样可以防止自动注入
	 * @param symbol
	 */
	public void setSymb(String symbol) {
		mapSymbol.put(dbInfo.getDbNameLowcase(), symbol);
	}
	/**
	 * synonyms
	 * @return
	 */
	public Set<String> getSynonym() {
		return setSynonyms;
	}
	
	public void addSynonym(String synonyms) {
		if (synonyms == null || synonyms.equals("") || synonyms.equals("-")) {
			return;
		}
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
		String descrip = null;
		if (mapDescription == null || mapDescription.size() == 0) {
			return "";
		}
		if (dbInfo != null) {
			descrip = mapDescription.get(dbInfo.getDbNameLowcase());
		}
		if (descrip == null || descrip.equals("")) {
			int length = 0;
			for (String descripion : mapDescription.values()) {
				if (descripion.length() > length) {
					descrip = descripion;
				}
			}
		}
		if (descrip != null) {
			descrip = descrip.replaceAll("\"", "");
			descrip = descrip.replaceAll("@@", " \\\\\\\\ ");
			return descrip;
		}
		return "";
	}
	
	public void setDescrp(String description) {
		if (description == null) return;
		description = description.replaceAll("\"", "").trim();
		if (description.equals("") || description.equals("-")) return;
		
		mapDescription.put(dbInfo.getDbNameLowcase(), description);
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
	/** Symbol_from_nomenclature_authority */
	public void addSymNom(String symNome) {
		if (symNome == null || symNome.equals("") || symNome.equals("-")) return;
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
		if (fullNameFromNomenclature == null || fullNameFromNomenclature.equals("") || fullNameFromNomenclature.equals("-")) return;
		this.setFullNameNome.add(fullNameFromNomenclature);
	}

	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}
	
	/** 如果是同一个数据库，则覆盖式的保存 */
	public boolean addInfo(AGeneInfo geneInfo) {
		boolean update = false;
		for (String dbInfoName : geneInfo.mapDescription.keySet()) {
			//如果不含有该注释，新加入的注释信息更全面，或者两个注释不一样
			if (!mapDescription.containsKey(dbInfoName)
					||
					(geneInfo.mapDescription.get(dbInfoName).toLowerCase().contains(mapDescription.get(dbInfoName).toLowerCase()) 
							&& geneInfo.mapDescription.get(dbInfoName).length() > mapDescription.get(dbInfoName).length())
					) {
				mapDescription.put(dbInfoName, geneInfo.mapDescription.get(dbInfoName));
				update = true;
			} else if(!mapDescription.get(dbInfoName).toLowerCase().equals(geneInfo.mapDescription.get(dbInfoName).toLowerCase()) ) {
				mapDescription.put(dbInfoName, geneInfo.mapDescription.get(dbInfoName));
				update = true;
			}
		}
		for (String dbInfoName : geneInfo.mapSymbol.keySet()) {
			if (!mapSymbol.containsKey(dbInfoName)) {
				mapSymbol.put(dbInfoName, geneInfo.mapSymbol.get(dbInfoName));
				update = true;
			} else {
				String symbolOld = mapSymbol.get(dbInfoName);
				String symbolThis = geneInfo.mapSymbol.get(dbInfoName);
				if (symbolThis != null && !symbolThis.equals("")) {
					if (symbolOld == null || !symbolThis.equals(symbolOld)) {
						mapSymbol.put(dbInfoName, symbolThis);
						update = true;
					}
				}
			}
		}
		update = addInfo(setDbXrefs, geneInfo.setDbXrefs) || update;
		update = addInfo(setFullNameNome, geneInfo.setFullNameNome) || update;
		update = addInfo(setPubmedIDs, geneInfo.setPubmedIDs) || update;
		update = addInfo(setSymNome, geneInfo.setSymNome) || update;
		update = addInfo(setSynonyms, geneInfo.setSynonyms) || update;
		return update;
	}
	/** 浅层复制，不复制geneID */
	public void copeyInfo(AGeneInfo geneInfo) {
		this.dbInfo = geneInfo.dbInfo;
		this.mapDescription = geneInfo.mapDescription;
		this.mapSymbol = geneInfo.mapSymbol;
		this.modDate = geneInfo.modDate;
		this.setDbXrefs = geneInfo.setDbXrefs;
		this.setFullNameNome = geneInfo.setFullNameNome;
		this.setPubmedIDs = geneInfo.setPubmedIDs;
		this.setSymNome = geneInfo.setSymNome;
		this.setSynonyms = geneInfo.setSynonyms;
		this.taxID = geneInfo.taxID;
		this.typeOfGene = geneInfo.typeOfGene;
		
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
	
	/**
	 * 将SetOther装到setThis里面，如果setOther中出现了新的item，则返回true
	 * 否则返回false
	 * @param <K>
	 * @param setThis
	 * @param setOther
	 * @return
	 */
	protected static<T, K>  boolean addInfo(Map<T, K> mapThis, Map<T, K> mapOther) {
		boolean add = false;
		for (T t : mapOther.keySet()) {
			if (!mapThis.containsKey(t)) {
				mapThis.put(t, mapOther.get(t));
				add = true;
			}
		}
		return add;
	}
}
