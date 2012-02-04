package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import antlr.collections.List;

import com.novelbio.analysis.generalConf.NovelBioConst;
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
	private static Logger logger = Logger.getLogger(AGeneInfo.class);
	static ArrayList<String> lsDBinfo = new ArrayList<String>();
	static{
		lsDBinfo.add(NovelBioConst.DBINFO_NCBI_ACC_GenralID);
		lsDBinfo.add(NovelBioConst.DBINFO_UNIPROT_GenralID);
	}
	
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
	private String pubmedID;
	private HashSet<String> hashPubmedIDs = new HashSet<String>();
	private String dbInfo = "";
	public abstract String getGeneUniID();
	public abstract void setGeneUniID(String geneUniID);
	
	
	private int taxID = 0;
	
	private String sep = null;
	/**
	 * 设定synonams等可能存在的分割符，譬如从NCBI下载的ID导入数据库的时候可能存在有 | 作为多个synonams的分割符
	 * 设定后，提取synonams和symbol等都是用synonams进行
	 * @param sep
	 */
	public void setSep(String sep) {
		this.sep = sep;
	}
	public String getSep() {
		return sep;
	}
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
	/**
	 * 必须第一时间设定
			39947, NovelBioConst.DBINFO_RICE_TIGR<br>
			3702, NovelBioConst.DBINFO_ATH_TAIR<br>
			3847, NovelBioConst.DBINFO_GLYMAX_SOYBASE<br>
			4102, NovelBioConst.DBINFO_PLANTGDB_ACC<br>
			NovelBioConst.DBINFO_NCBI_ACC_GenralID<br>
		    NovelBioConst.DBINFO_UNIPROT_GenralID
	 * @param fromDB
	 */
	public void setDBinfo(String fromDB) {
		this.dbInfo = fromDB.trim();
		if (symbol != null && symbol.contains(SEP_INFO)) {
			symbol = dbInfo + SEP_INFO + symbol.split(SEP_INFO)[1];
		}
		if (synonyms != null && synonyms.contains(SEP_INFO)) {
			synonyms = dbInfo + SEP_INFO + synonyms.split(SEP_INFO)[1];
		}
		if (dbXrefs != null && dbXrefs.contains(SEP_INFO)) {
			dbXrefs = dbInfo + SEP_INFO + dbXrefs.split(SEP_INFO)[1];
		}
		if (chromosome != null && chromosome.contains(SEP_INFO)) {
			chromosome = dbInfo + SEP_INFO + chromosome.split(SEP_INFO)[1];
		}
		if (mapLocation != null && mapLocation.contains(SEP_INFO)) {
			mapLocation = dbInfo + SEP_INFO + mapLocation.split(SEP_INFO)[1];
		}
		if (description != null && description.contains(SEP_INFO)) {
			description = dbInfo + SEP_INFO + description.split(SEP_INFO)[1];
		}
		if (otherDesign != null && otherDesign.contains(SEP_INFO)) {
			otherDesign = dbInfo + SEP_INFO + otherDesign.split(SEP_INFO)[1];
		}
		if (nomStat != null && nomStat.contains(SEP_INFO)) {
			nomStat = dbInfo + SEP_INFO + nomStat.split(SEP_INFO)[1];
		}
		if (fullNameNome != null && fullNameNome.contains(SEP_INFO)) {
			fullNameNome = dbInfo + SEP_INFO + fullNameNome.split(SEP_INFO)[1];
		}
		if (dbXrefs != null && dbXrefs.contains(SEP_INFO)) {
			dbXrefs = dbInfo + SEP_INFO + dbXrefs.split(SEP_INFO)[1];
		}
		if (locusTag != null && locusTag.contains(SEP_INFO)) {
			locusTag = dbInfo + SEP_INFO + locusTag.split(SEP_INFO)[1];
		}
	}
	
	/**
	 * 可以连续不断的设定好几次
	 * 有几篇文献就设定几次
	 * @param pubmedID
	 */
	public void setPubmedIDs(String pubmedID) {
		pubmedID = pubmedID.trim();
		if (hashPubmedIDs.contains(pubmedID)) {
			return;
		}
		hashPubmedIDs.add(pubmedID);
		if (this.pubmedID == null || this.pubmedID.equals("")) {
			this.pubmedID = pubmedID;
		}
		else {
			this.pubmedID = this.pubmedID + SEP_INFO + pubmedID;
		}
	}
	/**
	 * 如果没有pubmedID，则返回一个空的arraylist，方便用foreach语句
	 * @return
	 */
	public ArrayList<String> getPubmedIDs() {
		ArrayList<String> lsPubmedIDs = new ArrayList<String>();
		if (pubmedID != null && !pubmedID.equals("")) {
			return lsPubmedIDs;
		}
		String[] ss = pubmedID.split(SEP_INFO);
		for (String string : ss) {
			lsPubmedIDs.add(string);
		}
		return lsPubmedIDs;
	}
	private String getPubmedID() {
		return pubmedID;
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
	/**
	 * symbol
	 * @return
	 */
	public String getSymb() {
		return getInfoSep(symbol);
	}
	public void setSymb(String symbol) {
		this.symbol = validateField(null, symbol,true);
	}
	/**
	 * locusTag
	 * @return
	 */
	public String getLocTag() {
		return getInfoSep(locusTag);
	}
	public void setLocTag(String locusTag) {
		this.locusTag = validateField(null, locusTag,true);
	}
	/**
	 * synonyms
	 * @return
	 */
	public String getSynonym() {
		return getInfoSep(synonyms);
	}
	public void setSynonym(String synonyms) {
		this.synonyms = validateField(null, synonyms,true);
	}
	/**
	 * dbXrefs
	 * @return
	 */
	public String getDbXref() {
		return getInfoSep(dbXrefs);
	}
	public void setDbXref(String dbXrefs) {
		this.dbXrefs = validateField(null, dbXrefs,true);
	}  
	/**
	 * chromosome
	 * @return
	 */
	public String getChrm() {
		return getInfoSep(chromosome);
	}
	public void setChrm(String chromosome) {
		this.chromosome = validateField(null, chromosome,true);
	}  
	/**
	 * mapLocation
	 * @return
	 */
	public String getMapLoc() {
		return getInfoSep(mapLocation);
	}
	public void setMapLoc(String mapLocation) {
		this.mapLocation = validateField(null, mapLocation,true);
	}
	/**
	 * description
	 * @return
	 */
	public String getDescrp() {
		return getInfoSep(description);
	}
	public void setDescrp(String description) {
		this.description = validateField(null,description,true);
	}

	public String getTypeOfGene() {
		return typeOfGene;
	}
	public void setTypeOfGene(String typeOfGene) {
		if (typeOfGene == null)
			return;
		typeOfGene = typeOfGene.trim();
		if (typeOfGene.equals("") || typeOfGene.equals("-")) {
			return;
		}
		this.typeOfGene = typeOfGene.trim();
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getSymNom() {
		return getInfoSep(symNome);
	}
	public void setSymNom(String symNome) {
		this.symNome = validateField(null, symNome,true);
	}

	/**
	 * 没有则为"-"或者null或者""
	 * @return
	 */
	public String getFullName() {
		return getInfoSep(fullNameNome);
	}
	public void setFullName(String fullNameFromNomenclature) {
		this.fullNameNome =  validateField(null, fullNameFromNomenclature,true);
	}

	public String getNomState() {
		return getInfoSep(nomStat);
	}
	public void setNomState(String nomStat) {
		this.nomStat = validateField(null, nomStat, true);
	}

	/**
	 * otherDesig
	 * @return
	 */
	public String getOtherDesg() {
		return getInfoSep(otherDesign);
	}
	/**
	 * 
	 * @param otherDesign
	 */
	public void setOtherDesg(String otherDesign) {
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
	private void addTaxID(int taxID) {
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
	private void addLocusTag(String locusTag) {
		this.locusTag = validateField(this.locusTag, locusTag,true);
	}
	private void addPubmedIDs(String pubmedIDs) {
		if (this.pubmedID == null || this.pubmedID.equals("")) {
			this.pubmedID = pubmedIDs;
		}
		else if (pubmedIDs == null || pubmedIDs.trim().equals("-")) {
			return;
		}
		else {
			this.pubmedID = this.pubmedID + SEP_ID + pubmedIDs;
		}
		
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * 验证输入项，将输入项按照需求修正，并返回修正后的结果
	 * 如果输入项为"-", ""等，直接返回thisField
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
				inputFieldFinal = dbInfo + SEP_INFO + inputField;
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
//	public static final String FROMDB_NCBI = "NCBI";
//	public static final String FROMDB_UNIPROT = "UniProt";
//	public static final String FROMDB_TAIR = "tair";
//	public static final String FROMDB_TIGR = "tigr";
	/**
	 * 特定的物种对应特定的数据库
	 */
	static HashMap<Integer, String> hashDBtype = new HashMap<Integer, String>();

	/**
	 * 特定的物种使用特定数据库的信息
	 * 譬如水稻就用TIGR
	 * 拟南芥就用TAIR
	 * @return
	 */
	private String getDatabaseType() {
		if (hashDBtype.size() == 0) {
			hashDBtype.put(39947, NovelBioConst.DBINFO_RICE_TIGR);
			hashDBtype.put(3702, NovelBioConst.DBINFO_ATH_TAIR);
			hashDBtype.put(3847, NovelBioConst.DBINFO_GLYMAX_SOYBASE);
			hashDBtype.put(4102, NovelBioConst.DBINFO_PLANTGDB_ACC);
		}
		return hashDBtype.get(taxID);
	}
	/**
	 * 将NCBIID等表中的dbinfo转化成geneInfo中的dbinfo
	 */
	static HashMap<Integer, String> hashDBinfo = new HashMap<Integer, String>();
	
	
	
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
		chromosome = geneInfo.chromosome;
		taxID = geneInfo.taxID;
		dbXrefs = geneInfo.dbXrefs;
		description = geneInfo.description;
		fullNameNome = geneInfo.fullNameNome;
		idType = geneInfo.idType;
		locusTag = geneInfo.locusTag;
		mapLocation = geneInfo.mapLocation;
		modDate = geneInfo.modDate;
		nomStat = geneInfo.nomStat;
		otherDesign = geneInfo.otherDesign;
		symbol = geneInfo.symbol;
		symNome = geneInfo.symNome;
		synonyms = geneInfo.synonyms;
		typeOfGene = geneInfo.typeOfGene;
		pubmedID = geneInfo.pubmedID;
		taxID = geneInfo.taxID;
		setGeneUniID(geneInfo.getGeneUniID());
	}
	/**
	 * <b>只能添加单个geneinfo，不能添加一个包含多个数据库信息的geneInfo</b><br>
	 * 增加信息，将信息全部复制过来，并且加上来自哪个数据库，如果本类中已有的信息也会附加上去<br>
	 * 不包括geneID的添加<br>
	 * 如果信息重复，就不需要升级，则返回false<br>
	 * @param geneInfo
	 * @param infoDBfrom AGeneInfo.FROMDB_NCBI等
	 */
	public boolean addInfo(AGeneInfo geneInfo)
	{
		if (!validateUpdate(chromosome, geneInfo.chromosome)
			&&
			!validateUpdate(dbXrefs, geneInfo.dbXrefs)
			&&
			!validateUpdate(description, geneInfo.description)
			&&
			!validateUpdate(fullNameNome, geneInfo.fullNameNome)
			&&
			!validateUpdate(idType, geneInfo.idType)
			&&
			!validateUpdate(locusTag, geneInfo.locusTag)
			&&
			!validateUpdate(mapLocation, geneInfo.mapLocation)
			&&
			!validateUpdate(modDate, geneInfo.modDate)
			&&
			!validateUpdate(nomStat, geneInfo.nomStat)
			&&
			!validateUpdate(otherDesign, geneInfo.otherDesign)
			&&
			!validateUpdate(symbol, geneInfo.symbol)
			&&
			!validateUpdate(symNome, geneInfo.symNome)
			&&
			!validateUpdate(synonyms, geneInfo.synonyms)
			&&
			!validateUpdate(typeOfGene, geneInfo.typeOfGene)
			&&
			!validateUpdate(pubmedID, geneInfo.pubmedID)
		) {
			return false;
		}
		addChromosome(geneInfo.getChrm());
		addDbXrefs(geneInfo.getDbXref());
		addDescription(geneInfo.getDescrp());
		addFullName(geneInfo.getFullName());
		addLocusTag(geneInfo.getLocTag());
//		setGeneUniID(geneInfo.getGeneUniID());
		setIDType(geneInfo.getIDType());
		setLocTag(geneInfo.getLocTag());
		addMapLocation(geneInfo.getMapLoc());
		setModDate(geneInfo.getModDate());
		addNomStat(geneInfo.getNomState());
		addOtherDesign(geneInfo.getOtherDesg());
		addSymbol(geneInfo.getSymb());
		addSymNome(geneInfo.getSymNom());
		addSynonyms(geneInfo.getSynonym());
		setTypeOfGene(geneInfo.getTypeOfGene());
		addPubmedIDs(geneInfo.getPubmedID());
		addTaxID(geneInfo.getTaxID());
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
		if (inputField.contains(SEP_INFO)) {
			if (thisField.contains(inputField.split(SEP_INFO)[1])) {
				return false;
			}
		}
		else {
			if (thisField.contains(inputField)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 给定输出的symbol等，将其按照指定的数据库将信息提取出来
	 * @param info
	 * @return
	 */
	private String getInfoSep(String info) {
		if (info == null || info.equals("")) {
			return null;
		}
		String[] ss = info.split(SEP_ID);
		if (ss.length == 1) {
			return ss[0].split(SEP_INFO)[1];
		}
		/**
		 * 用来保存具体的信息
		 */
		HashMap<String, String> hashInfo = new HashMap<String, String>();
		for (String string : ss) {
			String[] ss2 = string.split(SEP_INFO);
			hashInfo.put(ss2[0], ss2[1]);
		}
		if (getDatabaseType() != null) {
			String result = hashInfo.get(getDatabaseType());
			if (result != null) {
				return result;
			}
		}
		for (String string : lsDBinfo) {
			String result = hashInfo.get(string);
			if (result != null) {
				return result;
			}
		}
		return ss[0].split(SEP_INFO)[1];
	}
}
