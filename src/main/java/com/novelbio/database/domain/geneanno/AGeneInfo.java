package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import antlr.collections.List;

import com.novelbio.analysis.generalConf.NovelBioConst;
/**
 * 当有新的物种加入时，需要添加新的dbinfo信息
 * 否则默认是走NCBIID
 * @author zong0jie
 *
 */
public abstract class AGeneInfo {
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
	
	/**
	 * 这个不用设定。copedID会去设定
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	
	/**
	 * 可以连续不断的设定好几次
	 * 有几篇文献就设定几次
	 * @param pubmedID
	 */
	public void setPubmedID(String pubmedID) {
		if (hashPubmedIDs.contains(pubmedID)) {
			return;
		}
		hashPubmedIDs.add(pubmedID);
		if (this.pubmedID == null || this.pubmedID.equals("")) {
			this.pubmedID = pubmedID;
		}
		else {
			this.pubmedID = this.pubmedID + SEP_ID + pubmedID;
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
		String[] ss = pubmedID.split(SEP_ID);
		for (String string : ss) {
			lsPubmedIDs.add(string);
		}
		return lsPubmedIDs;
	}
	private String getPubmedID() {
		return pubmedID;
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
		return getInfoSep(symbol);
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
		return getInfoSep(synonyms);
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
		return getInfoSep(description);
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
		return getInfoSep(symNome);
	}
	public void setSymNome(String symNome) {
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
	private String getDatabaseTyep() {
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
		this.pubmedID = geneInfo.pubmedID;
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
			&&
			!validateUpdate(getPubmedID(), geneInfo.getPubmedID())
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
		addPubmedIDs(geneInfo.getPubmedID());
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
	
	/**
	 * 给定输出的symbol等，将其按照指定的数据库将信息提取出来
	 * @param info
	 * @return
	 */
	private String getInfoSep(String info) {
		if (info == null || info.equals("")) {
			return "";
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
		if (getDatabaseTyep() != null) {
			String result = hashInfo.get(getDatabaseTyep());
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
