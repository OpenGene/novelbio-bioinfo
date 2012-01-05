package com.novelbio.database.domain.geneanno;

import javax.swing.text.Element;

import org.apache.catalina.ha.util.IDynamicProperty;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

public abstract class AGeneInfo {
	private String symbol;
	private String locusTag;
	
	private String synonyms;
	private String dbXrefs;
	private String chromosome;
	private String mapLocation;
	private String idType;
	private String description;
	private String typeOfGene;
	private String symNome;
	private String fullNameNome; 
	private String nomStat;
	private String otherDesign;
	private String modDate;
	
	private String fromDB = "";
	private String sep = "";
	public abstract String getGeneUniID();
	public abstract void setGeneUniID(String geneUniID);
	/**
	 * 必须第一时间设定
	 * @param fromDB
	 */
	public void setFromDB(String fromDB) {
		this.fromDB = fromDB.trim();
		if (!this.fromDB.equals("") ) {
			this.sep = SEP_INFO;
		}
	}
	
	public String getIDType() {
		return idType;
	}
	/**
	 * CopedID的idTpye
	 * @param idType
	 */
	public void setIDType(String idType) {
		if (idType == null || idType.trim().equals("")) {
			return;
		}
		if (this.idType != null && this.idType.equals(idType)) {
			return;
		}
		this.idType = idType.trim();
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = validateField(null, symbol,true);
	}

	public String getLocusTag() {
		return locusTag;
	}
	public void setLocusTag(String locusTag) {
		this.locusTag = locusTag;
	}
	
	public String getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String synonyms) {
		this.synonyms = validateField(null, synonyms,true);
	}

	public String getDbXrefs() {
		return dbXrefs;
	}
	public void setDbXrefs(String dbXrefs) {
		this.dbXrefs = validateField(null, dbXrefs,true);
	}  

	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = validateField(null, chromosome,true);
	}  

	public String getMapLocation() {
		return mapLocation;
	}
	public void setMapLocation(String mapLocation) {
		this.mapLocation = validateField(null, mapLocation,true);
	}  


	
	public String getDescription() {
		if (description == null) {
			return "";
		}
		return description;
	}
	public void setDescription(String description) {
		this.description = validateField(null,description,true);
	}

	public String getTypeOfGene() {
		return typeOfGene;
	}
	public void setTypeOfGene(String typeOfGene) {
		this.typeOfGene = typeOfGene;
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getSymNome() {
		return symNome;
	}
	public void setSymNome(String symNome) {
		this.symNome = validateField(null, symNome,true);
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getFullName() {
		return fullNameNome;
	}
	public void setFullName(String fullNameFromNomenclature) {
		this.fullNameNome =  validateField(null, fullNameFromNomenclature,true);
	}

	public String getNomStat() {
		return nomStat;
	}
	public void setNomStat(String nomStat) {
		this.nomStat = validateField(null, nomStat, true);
	}

	
	public String getOtherDesign() {
		return otherDesign;
	}
	public void setOtherDesign(String otherDesign) {
		this.otherDesign = validateField(null, otherDesign,true);
	}

	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}
	
	
	
	/////////////// add /////////////////////////////////////////////
	private void addSymbol(String symbol) {
		this.symbol = validateField(this.symbol, symbol,true);
	}
	private void addSynonyms(String synonyms) {
		this.synonyms = validateField(this.synonyms, synonyms,true);
	}
	private void addOtherDesign(String otherDesign) {
		this.otherDesign = validateField(this.otherDesign,otherDesign,true);
	}
	private void addFullName(String fullNameFromNomenclature) {
		this.fullNameNome =  validateField(this.fullNameNome,fullNameFromNomenclature,true);
	}
	private void addSymNome(String symNome) {
		this.symNome = validateField(this.symNome,symNome,true);
	}
	private void addNomStat(String nomStat) {
		this.nomStat = validateField(this.nomStat, nomStat, true);
	}
	private void addDescription(String description) {
		this.description = validateField(this.description,description,true);
	}
	private void addMapLocation(String mapLocation) {
		this.mapLocation = validateField(this.mapLocation, mapLocation,true);
	}
	private void addChromosome(String chromosome) {
		this.chromosome = validateField(this.chromosome, chromosome,true);
	}
	private void addDbXrefs(String dbXrefs) {
		this.dbXrefs = validateField(this.dbXrefs, dbXrefs,true);
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * 验证输入项
	 * @param thisField 已有的项
	 * @param inputField 待输入项
	 * @return
	 */
	private String validateField(String thisField, String inputField, boolean sepWithDBinfo)
	{
		String inputFieldFinal = "";
		if (inputField == null) {
			return thisField;
		}
		inputField = inputField.trim();
		if (inputField.equals("-") || inputField.equals("")) {
			return thisField;
		}
		else {
			if (sepWithDBinfo) {
				inputFieldFinal = fromDB + sep + inputField;
			}
			else {
				inputFieldFinal = inputField;
			}
		}
		if (thisField == null || thisField.equals("")) {
			return inputFieldFinal;
		}
		else {
			if (inputFieldFinal.equals("") || thisField.contains(inputField)) {
				return thisField;
			}
			else {
				return thisField + SEP_ID + inputFieldFinal;
			}
		}
	}
	//////////////////////////////////////////////////
	public static final String FROMDB_NCBI = "NCBI";
	public static final String FROMDB_UNIPROT = "UniProt";
	public static final String FROMDB_TAIR = "tair";
	public static final String FROMDB_TIGR = "tigr";
	/**
	 * 分割两个ID或两个Description
	 */
	public static final String SEP_ID = "@//@";
	/**
	 * 分割 NCBIID的title和内容
	 * 如NCBI@@protein coding
	 */
	public static final String SEP_INFO = "@@";
	/**
	 * 拷贝信息，将信息全部复制过来
	 * @param geneInfo
	 */
	public void copeyInfo(AGeneInfo geneInfo)
	{
		setChromosome(geneInfo.getChromosome());
		setDbXrefs(geneInfo.getDbXrefs());
		setDescription(geneInfo.getDescription());
		setFullName(geneInfo.getFullName());
		setGeneUniID(geneInfo.getGeneUniID());
		setIDType(geneInfo.getIDType());
		setLocusTag(geneInfo.getLocusTag());
		setMapLocation(geneInfo.getMapLocation());
		setModDate(geneInfo.getModDate());
		setNomStat(geneInfo.getNomStat());
		setOtherDesign(geneInfo.getOtherDesign());
		setSymbol(geneInfo.getSymbol());
		setSymNome(geneInfo.getSymNome());
		setSynonyms(geneInfo.getSynonyms());
		setTypeOfGene(geneInfo.getTypeOfGene());
	}
	/**
	 * 增加信息，将信息全部复制过来，并且加上来自哪个数据库，如果本类中已有的信息也会附加上去
	 * 不包括geneID的添加
	 * 如果信息重复，就不需要升级，则返回false
	 * @param geneInfo
	 * @param infoDBfrom AGeneInfo.FROMDB_NCBI等
	 */
	public boolean addInfo(AGeneInfo geneInfo)
	{
		if (!validateUpdate(getChromosome(), geneInfo.getChromosome())
			&&
			!validateUpdate(getDbXrefs(), geneInfo.getDbXrefs())
			&&
			!validateUpdate(getDescription(), geneInfo.getDescription())
			&&
			!validateUpdate(getFullName(), geneInfo.getFullName())
			&&
			!validateUpdate(getIDType(), geneInfo.getIDType())
			&&
			!validateUpdate(getLocusTag(), geneInfo.getLocusTag())
			&&
			!validateUpdate(getMapLocation(), geneInfo.getMapLocation())
			&&
			!validateUpdate(getModDate(), geneInfo.getModDate())
			&&
			!validateUpdate(getNomStat(), geneInfo.getNomStat())
			&&
			!validateUpdate(getOtherDesign(), geneInfo.getOtherDesign())
			&&
			!validateUpdate(getSymbol(), geneInfo.getSymbol())
			&&
			!validateUpdate(getSymNome(), geneInfo.getSymNome())
			&&
			!validateUpdate(getSynonyms(), geneInfo.getSynonyms())
			&&
			!validateUpdate(getTypeOfGene(), geneInfo.getTypeOfGene())
		) {
			return false;
		}
		addChromosome(geneInfo.getChromosome());
		addDbXrefs(geneInfo.getDbXrefs());
		addDescription(geneInfo.getDescription());
		addFullName(geneInfo.getFullName());
//		setGeneUniID(geneInfo.getGeneUniID());
		setIDType(geneInfo.getIDType());
		setLocusTag(geneInfo.getLocusTag());
		addMapLocation(geneInfo.getMapLocation());
		setModDate(geneInfo.getModDate());
		addNomStat(geneInfo.getNomStat());
		addOtherDesign(geneInfo.getOtherDesign());
		addSymbol(geneInfo.getSymbol());
		addSymNome(geneInfo.getSymNome());
		addSynonyms(geneInfo.getSynonyms());
		setTypeOfGene(geneInfo.getTypeOfGene());
		return true;
	}
	/**
	 * 是否需要升级
	 * @param thisField
	 * @param inputField
	 * @return
	 * false不需要升级
	 * true 需要升级
	 */
	private boolean validateUpdate(String thisField, String inputField)
	{
		if (thisField == null) {
			thisField = "";
		}
		if (inputField == null) {
			return false;
		}
		inputField = inputField.trim();
		if (inputField.equals("-") || inputField.equals("")) {
			return false;
		}
		if (thisField.contains(inputField)) {
			return false;
		}
		else {
			return true;
		}
	}
}
