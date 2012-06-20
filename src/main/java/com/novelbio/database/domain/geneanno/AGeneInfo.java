package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.generalConf.NovelBioConst;
/**
 * ibatis�ڲ������ݿ�ʱ���Զ�ʹ�����е�setter��getter�����Ը�ֵ
 * ������������е���Щ��������ôsetter��getter�����ֲ�Ҫ������һ���ͺ�
 * �磺���ԣ�symbol��������Ϊ��getSymb
 * �����µ����ּ���ʱ����Ҫ����µ�dbinfo��Ϣ
 * ����Ĭ������NCBIID
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
	//////////////////////////////////////////////////
//	public static final String FROMDB_NCBI = "NCBI";
//	public static final String FROMDB_UNIPROT = "UniProt";
//	public static final String FROMDB_TAIR = "tair";
//	public static final String FROMDB_TIGR = "tigr";
	/**
	 * �ض������ֶ�Ӧ�ض������ݿ�
	 */
	static HashMap<Integer, String> hashDBtype = new HashMap<Integer, String>();

	/**
	 * �ض�������ʹ���ض����ݿ����Ϣ
	 * Ʃ��ˮ������TIGR
	 * ���Ͻ����TAIR
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
	 * �趨synonams�ȿ��ܴ��ڵķָ����Ʃ���NCBI���ص�ID�������ݿ��ʱ����ܴ����� | ��Ϊ���synonams�ķָ��
	 * �趨����ȡsynonams��symbol�ȶ�����synonams����
	 * @param sep
	 */
	public void setSep(String sep) {
		this.sep = sep;
	}
	public String getSep() {
		return sep;
	}
	/**
	 * ��������趨��copedID��ȥ�趨
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	public String getDbInfo() {
		return dbInfo;
	}
	/**
	 * �����һʱ���趨
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
		if (symbol != null && symbol.contains(SepSign.SEP_INFO)) {
			symbol = dbInfo + SepSign.SEP_INFO + symbol.split(SepSign.SEP_INFO)[1];
		}
		if (synonyms != null && synonyms.contains(SepSign.SEP_INFO)) {
			synonyms = dbInfo + SepSign.SEP_INFO + synonyms.split(SepSign.SEP_INFO)[1];
		}
		if (dbXrefs != null && dbXrefs.contains(SepSign.SEP_INFO)) {
			dbXrefs = dbInfo + SepSign.SEP_INFO + dbXrefs.split(SepSign.SEP_INFO)[1];
		}
		if (chromosome != null && chromosome.contains(SepSign.SEP_INFO)) {
			chromosome = dbInfo + SepSign.SEP_INFO + chromosome.split(SepSign.SEP_INFO)[1];
		}
		if (mapLocation != null && mapLocation.contains(SepSign.SEP_INFO)) {
			mapLocation = dbInfo + SepSign.SEP_INFO + mapLocation.split(SepSign.SEP_INFO)[1];
		}
		if (description != null && description.contains(SepSign.SEP_INFO)) {
			description = dbInfo + SepSign.SEP_INFO + description.split(SepSign.SEP_INFO)[1];
		}
		if (otherDesign != null && otherDesign.contains(SepSign.SEP_INFO)) {
			otherDesign = dbInfo + SepSign.SEP_INFO + otherDesign.split(SepSign.SEP_INFO)[1];
		}
		if (nomStat != null && nomStat.contains(SepSign.SEP_INFO)) {
			nomStat = dbInfo + SepSign.SEP_INFO + nomStat.split(SepSign.SEP_INFO)[1];
		}
		if (fullNameNome != null && fullNameNome.contains(SepSign.SEP_INFO)) {
			fullNameNome = dbInfo + SepSign.SEP_INFO + fullNameNome.split(SepSign.SEP_INFO)[1];
		}
		if (dbXrefs != null && dbXrefs.contains(SepSign.SEP_INFO)) {
			dbXrefs = dbInfo + SepSign.SEP_INFO + dbXrefs.split(SepSign.SEP_INFO)[1];
		}
		if (locusTag != null && locusTag.contains(SepSign.SEP_INFO)) {
			locusTag = dbInfo + SepSign.SEP_INFO + locusTag.split(SepSign.SEP_INFO)[1];
		}
	}
	
	/**
	 * �����������ϵ��趨�ü���
	 * �м�ƪ���׾��趨����
	 * @param pubmedID
	 */
	public void setPubID(String pubmedID) {
		pubmedID = pubmedID.trim();
		if (hashPubmedIDs.contains(pubmedID)) {
			return;
		}
		hashPubmedIDs.add(pubmedID);
		if (this.pubmedID == null || this.pubmedID.equals("")) {
			this.pubmedID = pubmedID;
		}
		else {
			this.pubmedID = this.pubmedID + SepSign.SEP_INFO + pubmedID;
		}
	}
	/**
	 * ����һ���趨��λ����ȻҲ���������趨
	 * @param pubmedID
	 */
	public void setPubIDs(List<String> lsPubmedID) {
		for (String string : lsPubmedID) {
			setPubID(string);
		}
	}
	/**
	 * ���û��pubmedID���򷵻�һ���յ�arraylist��������foreach���
	 * @return
	 */
	public ArrayList<String> getPubmedIDs() {
		ArrayList<String> lsPubmedIDs = new ArrayList<String>();
		if (pubmedID != null && !pubmedID.equals("")) {
			return lsPubmedIDs;
		}
		String[] ss = pubmedID.split(SepSign.SEP_INFO);
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
	 * CopedID��idTpye
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
	/**
	 * ����������ĺ�symbol��һ�����������Է�ֹ�Զ�ע��
	 * @param symbol
	 */
	public void setSymb(String symbol) {
		this.symbol = validateField(dbInfo, null, symbol,true);
	}
	/**
	 * locusTag
	 * @return
	 */
	public String getLocTag() {
		return getInfoSep(locusTag);
	}
	/**
	 * ����������ĺ�symbol��һ�����������Է�ֹ�Զ�ע��
	 * @param locusTag
	 */
	public void setLocTag(String locusTag) {
		this.locusTag = validateField(dbInfo, null, locusTag,true);
	}
	/**
	 * synonyms
	 * @return
	 */
	public String getSynonym() {
		return getInfoSep(synonyms);
	}
	public void setSynonym(String synonyms) {
		this.synonyms = validateField(dbInfo, null, synonyms,true);
	}
	/**
	 * dbXrefs
	 * @return
	 */
	public String getDbXref() {
		return getInfoSep(dbXrefs);
	}
	public void setDbXref(String dbXrefs) {
		this.dbXrefs = validateField(dbInfo, null, dbXrefs,true);
	}  
	/**
	 * chromosome
	 * @return
	 */
	public String getChrm() {
		return getInfoSep(chromosome);
	}
	public void setChrm(String chromosome) {
		this.chromosome = validateField(dbInfo, null, chromosome,true);
	}  
	/**
	 * mapLocation
	 * @return
	 */
	public String getMapLoc() {
		return getInfoSep(mapLocation);
	}
	public void setMapLoc(String mapLocation) {
		this.mapLocation = validateField(dbInfo, null, mapLocation,true);
	}
	/**
	 * description
	 * @return
	 */
	public String getDescrp() {
		return getInfoSep(description);
	}
	public void setDescrp(String description) {
		this.description = validateField(dbInfo, null,description,true);
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
	 * û����Ϊ"-"����null����""
	 * @return
	 */
	public String getSymNom() {
		return getInfoSep(symNome);
	}
	public void setSymNom(String symNome) {
		this.symNome = validateField(dbInfo, null, symNome,true);
	}

	/**
	 * û����Ϊ"-"����null����""
	 * @return
	 */
	public String getFullName() {
		return getInfoSep(fullNameNome);
	}
	public void setFullName(String fullNameFromNomenclature) {
		this.fullNameNome =  validateField(dbInfo, null, fullNameFromNomenclature,true);
	}

	public String getNomState() {
		return getInfoSep(nomStat);
	}
	public void setNomState(String nomStat) {
		this.nomStat = validateField(dbInfo, null, nomStat, true);
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
		this.otherDesign = validateField(dbInfo, null, otherDesign,true);
	}

	public String getModDate() {
		return modDate;
	}
	public void setModDate(String modDate) {
		this.modDate = modDate;
	}
	
	
	
	/////////////// add /////////////////////////////////////////////
	private void addSymbol(String dbInfo, String symbol) {
		this.symbol = validateField(dbInfo, this.symbol, symbol,true);
	}
	private void addSynonyms(String dbInfo, String synonyms) {
		this.synonyms = validateField(dbInfo, this.synonyms, synonyms,true);
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
			logger.error("������������geneInfo�е�taxID��һ�£�ԭtaxID��"+this.taxID + " ��taxID��" + taxID );
		}
	}
	private void addOtherDesign(String dbInfo, String otherDesign) {
		this.otherDesign = validateField(dbInfo, this.otherDesign,otherDesign,true);
	}
	private void addFullName(String dbInfo, String fullNameFromNomenclature) {
		this.fullNameNome =  validateField(dbInfo, this.fullNameNome,fullNameFromNomenclature,true);
	}
	private void addSymNome(String dbInfo, String symNome) {
		this.symNome = validateField(dbInfo, this.symNome,symNome,true);
	}
	private void addNomStat(String dbInfo, String nomStat) {
		this.nomStat = validateField(dbInfo, this.nomStat, nomStat, true);
	}
	private void addDescription(String dbInfo, String description) {
		this.description = validateField(dbInfo, this.description,description,true);
	}
	private void addMapLocation(String dbInfo, String mapLocation) {
		this.mapLocation = validateField(dbInfo, this.mapLocation, mapLocation,true);
	}
	private void addChromosome(String dbInfo, String chromosome) {
		this.chromosome = validateField(dbInfo, this.chromosome, chromosome,true);
	}
	private void addDbXrefs(String dbInfo, String dbXrefs) {
		this.dbXrefs = validateField(dbInfo, this.dbXrefs, dbXrefs,true);
	}
	private void addLocusTag(String dbInfo, String locusTag) {
		this.locusTag = validateField(dbInfo, this.locusTag, locusTag,true);
	}
	
	private void addPubmedIDs(String pubmedIDs) {
		if (this.pubmedID == null || this.pubmedID.equals("")) {
			this.pubmedID = pubmedIDs;
		}
		else if (pubmedIDs == null || pubmedIDs.trim().equals("-")) {
			return;
		}
		else {
			this.pubmedID = this.pubmedID + SepSign.SEP_ID + pubmedIDs;
		}
		
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * ��֤������������������������������������Ľ��
	 * ���������Ϊ"-", ""�ȣ�ֱ�ӷ���thisField
	 * ������������е��������Ҳ������������ ����NCBI@@ffwefsef@//@NEW_DB@@NEW_DESCRIP
	 * �����������ݿ�����еļ����ص�����������
	 * NCBI@@NEW_DESCRIP//ffwefsef@//@UniProt@@sfesfe
	 * @param thisField ���е���
	 * @param inputField ��������
	 * @return
	 */
	private String validateField(String dbInfo, String thisField, String inputField, boolean sepWithDBinfo)
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
				inputFieldFinal = dbInfo + SepSign.SEP_INFO + inputField;
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
			//������ݿ��Ѿ�������
			else if (thisField.contains(dbInfo)) {
				if (sepWithDBinfo) {
					String result = thisField.replace(dbInfo+SepSign.SEP_INFO, inputFieldFinal + SepSign.SEP_INFO_SAMEDB);
					logger.error("������ͬ���ݿ⵫�ǲ�ͬ��ע�ͣ�"+ result);
					return result;
				}
				else {
					return thisField + SepSign.SEP_ID + inputFieldFinal;
				}
			}
			else {
				return thisField + SepSign.SEP_ID + inputFieldFinal;
			}
		}
	}

	/**
	 * ��NCBIID�ȱ��е�dbinfoת����geneInfo�е�dbinfo
	 */
	static HashMap<Integer, String> hashDBinfo = new HashMap<Integer, String>();
	
	/**
	 * ������Ϣ������Ϣȫ�����ƹ���
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
	 * <b>ֻ����ӵ���geneinfo���������һ������������ݿ���Ϣ��geneInfo</b><br>
	 * ������Ϣ������Ϣȫ�����ƹ��������Ҽ��������ĸ����ݿ⣬������������е���ϢҲ�ḽ����ȥ<br>
	 * ������geneID�����<br>
	 * �����Ϣ�ظ����Ͳ���Ҫ�������򷵻�false<br>
	 * @param geneInfo
	 * @param infoDBfrom AGeneInfo.FROMDB_NCBI��
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
		addChromosome(geneInfo.getDbInfo(), geneInfo.getChrm());
		addDbXrefs(geneInfo.getDbInfo(), geneInfo.getDbXref());
		addDescription(geneInfo.getDbInfo(), geneInfo.getDescrp());
		addFullName(geneInfo.getDbInfo(), geneInfo.getFullName());
		addLocusTag(geneInfo.getDbInfo(), geneInfo.getLocTag());
//		setGeneUniID(geneInfo.getGeneUniID());
		setIDType(geneInfo.getIDType());
		addMapLocation(geneInfo.getDbInfo(), geneInfo.getMapLoc());
		setModDate(geneInfo.getModDate());
		addNomStat(geneInfo.getDbInfo(), geneInfo.getNomState());
		addOtherDesign(geneInfo.getDbInfo(), geneInfo.getOtherDesg());
		addSymbol(geneInfo.getDbInfo(), geneInfo.getSymb());
		addSymNome(geneInfo.getDbInfo(), geneInfo.getSymNom());
		addSynonyms(geneInfo.getDbInfo(), geneInfo.getSynonym());
		setTypeOfGene(geneInfo.getTypeOfGene());
		addPubmedIDs(geneInfo.getPubmedID());
		addTaxID(geneInfo.getTaxID());
		return true;
	}
	/**
	 * �Ƿ���Ҫ����
	 * @param thisField
	 * @param inputField
	 * @return
	 * false����Ҫ����
	 * true ��Ҫ����
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
		if (inputField.contains(SepSign.SEP_INFO)) {
			if (thisField.toLowerCase().replace("-", " ").contains(inputField.split(SepSign.SEP_INFO)[1].toLowerCase().replace("-", " "))) {
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
	 * ���������symbol�ȣ����䰴��ָ�������ݿ⽫��Ϣ��ȡ����
	 * @param info
	 * @return
	 */
	private String getInfoSep(String info) {
		if (info == null || info.equals("")) {
			return null;
		}
		String[] ss = info.split(SepSign.SEP_ID);
		if (ss.length == 1) {
			return ss[0].split(SepSign.SEP_INFO)[1];
		}
		/**
		 * ��������������Ϣ
		 */
		HashMap<String, String> hashInfo = new HashMap<String, String>();
		for (String string : ss) {
			String[] ss2 = string.split(SepSign.SEP_INFO);
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
		return ss[0].split(SepSign.SEP_INFO)[1];
	}
}
