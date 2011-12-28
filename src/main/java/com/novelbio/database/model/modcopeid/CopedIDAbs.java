package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.lsb_catchCallback;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegGenEntryKO;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.service.servgeneanno.ServBlastInfo;
import com.novelbio.database.service.servgeneanno.ServGene2Go;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniGene2Go;
import com.novelbio.database.service.servgeneanno.ServUniGeneInfo;
import com.novelbio.database.service.servgeneanno.ServUniProtID;

public abstract class CopedIDAbs implements CopedIDInt {
	/**
	 * 物种id
	 */
	int taxID = 0;
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	String idType = CopedID.IDTYPE_ACCID;
	
	/**
	 * 具体的accID
	 */
	String accID = "";

	String genUniID = "";
		
	String symbol = null;
	
//	BlastInfo blastInfo = null;
	
	/**
	 * 譬方和多个物种进行blast，然后结合这些物种的信息，取并集
	 */
	ArrayList<BlastInfo> lsBlastInfos = null;
	
	
	
	double evalue = 10;
	
	KegGenEntryKO kegGenEntryKO = null;
	
	AGeneInfo geneInfo = null;
//	ArrayList<AGene2Go> lsGene2Gos = null;
	
	String databaseType = "";
	
	KeggInfo keggInfo;
	
	GOInfoAbs goInfoAbs = null;

	//////////////////////  service 层  ////////////////////////////////////////////////////
	ServBlastInfo servBlastInfo = new ServBlastInfo();
	ServNCBIID servNCBIID = new ServNCBIID();
	ServUniProtID servUniProtID = new ServUniProtID();
	ServGeneInfo servGeneInfo = new ServGeneInfo();
	ServUniGeneInfo servUniGeneInfo = new ServUniGeneInfo();
	ServGene2Go servGene2Go = new ServGene2Go();
	ServUniGene2Go servUniGene2Go = new ServUniGene2Go();
	/////////////////////////////////////////////////////////////////////////////////////////////
	
///////////////////    Blast setting   /////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设定多个物种进行blast
	 * 每次设定后都会刷新
	 * @param evalue
	 * @param StaxID
	 */
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			BlastInfo blastInfoTmp = new BlastInfo();
			blastInfoTmp.setEvalue(evalue);
			blastInfoTmp.setQueryID(genUniID);
			blastInfoTmp.setSubjectTax(i);
			ArrayList<BlastInfo> lsBlastInfosTmp = servBlastInfo.queryLsBlastInfo(blastInfoTmp);
			if (lsBlastInfosTmp != null && lsBlastInfosTmp.size() > 0) 
			{
				Collections.sort(lsBlastInfosTmp);//排序选择最小的一项
				BlastInfo blastInfo = lsBlastInfosTmp.get(0);
				if (blastInfo.getEvalue() <= evalue) {
					lsBlastInfos.add(blastInfo);
				}
			}
		}
	}
	
	/**
	 * 单个物种的blast
	 * 获得本copedID blast到对应物种的第一个copedID，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	@Override
	public CopedID getCopedIDBlast() {
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return null;
		}
		BlastInfo blastInfo = lsBlastInfos.get(0);
		return getBlastCopedID(blastInfo);
	}

	/**
	 * 获得设定的第一个blast的对象，首先要设定blast的目标
	 * @param blastInfo
	 * @return
	 */
	private CopedID getBlastCopedID(BlastInfo blastInfo) {
		if (blastInfo == null) {
			return null;
		}
		String idType = "";
		if (blastInfo.getSubjectTab().equals(BlastInfo.SUBJECT_TAB_NCBIID)) {
			idType = CopedID.IDTYPE_GENEID;
		}
		else if (blastInfo.getSubjectTab().equals(BlastInfo.SUBJECT_TAB_UNIPROTID)) {
			idType = CopedID.IDTYPE_UNIID;
		}
		CopedID copedID = new CopedID(idType,blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		return copedID;
	}
	
	/**
	 * blast多个物种
	 * 首先要设定blast的目标
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist
	 * 如果没有结果，返回一个空的lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	@Override
	public ArrayList<CopedID> getCopedIDLsBlast() {
		ArrayList<CopedID> lsResult = new ArrayList<CopedID>();
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return lsResult;
		}
		for (BlastInfo blastInfo : lsBlastInfos) {
			CopedID copedID = getBlastCopedID(blastInfo);
			if (copedID != null) {
				lsResult.add(copedID);
			}
		}
		return lsResult;
	}
	/**
	 * blast多个物种
	 * 首先要设定blast的目标
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * @return
	 * 返回blast的信息，包括evalue等，该list和getCopedIDLsBlast()得到的list是一一对应的
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}
	////////////////////   normal setting  /////////////////////////////////////////////////////////////////
	/**
	 * idType，必须是IDTYPE中的一种
	 */
	public String getIDtype() {
		return this.idType;
	}
	
	/**
	 * 具体的accID
	 */
	public String getAccID() {
		return this.accID;
	}


	/**
	 * 获得geneID
	 * @return
	 */
	public String getGenUniID()
	{
		return this.genUniID;
	}
	
	public int getTaxID() {
		if (taxID <= 0) {
			taxID = Integer.parseInt(getNCBIUniTax(accID, 0).get(1));
		}
		return taxID;
	}
	
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() {
		setSymbolDescrip();
		if (geneInfo == null || geneInfo.getDescription() == null) {
			return "";
		}
		return geneInfo.getDescription().replaceAll("\"", "");
	}
	/**
	 * 获得该基因的symbol
	 * @return
	 */
	public String getSymbo() {
		setSymbolDescrip();
		return symbol;
	}
	/**
	 * 设定geneInfo信息
	 */
	protected abstract void setGenInfo();
	
	protected void setSymbolDescrip() {
		if (geneInfo != null || symbol != null ) {
			return;
		}
		if ( idType.equals(CopedID.IDTYPE_ACCID)) {
			symbol = "";
		}
		setGenInfo();
		if (geneInfo == null) {
			symbol = getGenName(getGenUniID(),databaseType);
		}
		else {
			symbol = geneInfo.getSymbol().split("//")[0];
		}
		if (symbol.equals("")) {
			symbol = getGenName(getGenUniID(), databaseType);
		}
	}
	/**
	 * 给定基因的NCBIgeneID，和databaseType，获得 accID.
	 * 如果databaseType == null 或 “” 那就随便选一个accID
	 * 如果geneID是0，返回""
	 * @return
	 */
	protected String getGenName(String genUniID, String databaseType)
	{
		AgeneUniID ageneUniID = getGenUniID(genUniID, databaseType);
		if (ageneUniID == null) {
			return "";
		}
		else {
			return ageneUniID.getAccID();
		}
	}
	
	
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo) {
		AgeneUniID genuniID = getGenUniID(getGenUniID(), dbInfo);
		if (genuniID != null) {
			return genuniID.getAccID();
		}
		return null;
	}
	/**
	 * 	 * 指定一个dbInfo，返回该dbInfo所对应的AgeneUniID，没有则返回null
	 * @param dbInfo
	 * @return
	 */
	protected abstract AgeneUniID getGenUniID(String genUniID, String dbInfo);

	/**
	 * 先设定blast的情况
	 * 如果blast * 0:symbol 1:description 2:evalue 3:subjectSpecies 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno(boolean blast) {
		String[] tmpAnno = null;
		if (blast) {
			tmpAnno = new String[6];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbo();
			tmpAnno[1] = getDescription();
			if (getCopedIDLsBlast() != null && getLsBlastInfos() != null
					&& getLsBlastInfos().size() > 0) {
				for (int i = 0; i < getLsBlastInfos().size(); i++) {
					if (tmpAnno[2] == null || tmpAnno[2].trim().equals("")) {
						tmpAnno[2] = CopedID.getHashTaxIDName().get( getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = getLsBlastInfos().get(i).getEvalue() + "";
						tmpAnno[4] = getCopedIDLsBlast().get(i).getSymbo();
						tmpAnno[5] = getCopedIDLsBlast().get(i)
								.getDescription();
					} else {
						tmpAnno[2] = tmpAnno[2] + "//" + CopedID.getHashTaxIDName().get( getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = tmpAnno[3] + "//" + getLsBlastInfos().get(i).getEvalue();
						tmpAnno[4] = tmpAnno[4] + "//" + getCopedIDLsBlast().get(i).getSymbo();
						tmpAnno[5] = tmpAnno[5] + "//" + getCopedIDLsBlast().get(i).getDescription();
					}
				}
			}
		} else {
			tmpAnno = new String[2];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbo();
			tmpAnno[1] = getDescription();
		}
		return tmpAnno;
	}
//////////////////////////////////GOInfo  ////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 设定 goInfoAbs的信息
	 */
	protected abstract void setGoInfo();
	
	/**
	 * 返回该基因所对应的GOInfo信息，不包含Blast
	 * @return
	 */
	protected GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			setGoInfo();
		}
		return goInfoAbs;
	}
	
	/**
	 * 返回该CopedID所对应的Gene2GOInfo
	 * @param GOType
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(String GOType) {
		return getGOInfo().getLsGene2Go(GOType);
	}
	
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的GoInfo
	 */
	public ArrayList<AGene2Go> getGene2GOBlast(String GOType) {
		setGoInfo();
		ArrayList<GOInfoAbs> lsGoInfo = new ArrayList<GOInfoAbs>();
		
		ArrayList<CopedID> lsBlastCopedIDs = getCopedIDLsBlast();
		if (lsBlastCopedIDs != null) {
			for (CopedID copedID : lsBlastCopedIDs) {
				lsGoInfo.add(copedID.getGOInfo());
			}
		}
		return getGOInfo().getLsGen2Go(lsGoInfo, GOType);
	}
//////////////////KEGG      //////////////////////////////////////////////
	/**
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * @param blast 是否blast到相应物种查看
	 * @param StaxID 如果blast为true，那么设定StaxID
	 * @return 如果没有就返回null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast) {
		if (!blast) {
			return setKegGenEntryKO().getLsKGentries();
		}
		else {
			//如果本基因能找到keggID就不进行blast
			if ( setKegGenEntryKO().getLsKGentries() != null) {
				return setKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getCopedIDBlast();
			return ScopedID.getKegEntity(false);
		}
	}
	
	private KegGenEntryKO setKegGenEntryKO()
	{
		if (kegGenEntryKO == null) {
			kegGenEntryKO = new KegGenEntryKO(idType, genUniID, taxID);
		}
		return kegGenEntryKO;
	}
	
	/**
	 * 获得相关的Kegg信息
	 * @return
	 */
	public KeggInfo getKeggInfo() {
		if (keggInfo != null) {
			return keggInfo;
		}
		keggInfo = new KeggInfo(idType, genUniID, taxID);
		return keggInfo;
	}
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		getKeggInfo();
		if (blast) {
			ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
			ArrayList<CopedID> lsBlastCopedIDs = getCopedIDLsBlast();
			if (lsBlastCopedIDs != null) {
				for (CopedID copedID : lsBlastCopedIDs) {
					lskeggInfo.add(copedID.getKeggInfo());
				}
			}
			return keggInfo.getLsKegPath(lskeggInfo);
		}
		else {
			return keggInfo.getLsKegPath();
		}
		
	}
 
	///////////////////  update method //////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 记录可能用于升级数据库的ID
	 * 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	ArrayList<String> lsRefAccID = new ArrayList<String>();
	/**
	 * 记录可能用于升级数据库的ID
	 * 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	public void setUpdateRefAccID(int taxID, String DBInfo, String... refAccID) {
		lsRefAccID.clear();
		for (String string : refAccID) {
		lsRefAccID.add(string);	
		}
	}
	boolean overrideDBinfo = false;
	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * @param taxID
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 */
	public void setUpdateDBinfo(int taxID, String DBInfo, boolean overlapDBinfo) {
		if (taxID != 0) {
			this.taxID = taxID;
		}
		if (!DBInfo.trim().equals("")) {
			this.databaseType = DBInfo;
		}
		this.overrideDBinfo = overlapDBinfo;
	}
	/**
	 * 输入已知的geneUniID和IDtype
	 * @param geneUniID
	 * @param idType 必须是CopedID.IDTYPE_GENEID等，可以不输入
	 */
	public void setUpdateGeneID(String geneUniID, String idType)
	{
		if (geneUniID != null && !genUniID.trim().equals("") && !genUniID.trim().equals("0")) {
			this.genUniID = geneUniID;
		}
		if (idType == null || idType.trim().equals("")) {
			return;
		}
		this.idType = idType;
	}
	/**
	 * 记录升级的GO信息的，每次升级完毕后都清空
	 */
	ArrayList<Gene2Go> lsGOInfoUpdate = new ArrayList<Gene2Go>();
	/**
	 * 依次输入需要升级的GO信息，最后升级
	 * 这里只是先获取GO的信息，最后调用升级method的时候再升级
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef
	 * @param gOQualifiy
	 */
	public void setUpdateGO(String GOID, String GOdatabase, String GOevidence, String GORef, String gOQualifiy) {
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(GOID); gene2Go.setEvidence(GOevidence);
		gene2Go.setDataBase(GOdatabase);
		gene2Go.setQualifier(gOQualifiy);
		gene2Go.setReference(GORef);
		Go2Term go2Term = Go2Term.getHashGo2Term().get(GOID);
		gene2Go.setFunction(go2Term.getGoFunction());
		gene2Go.setGOTerm(go2Term.getGoTerm());
		lsGOInfoUpdate.add(gene2Go);
	}
	
	/**
	 * 输入需要update的geneInfo，注意不需要设定geneUniID
	 * @param geneInfo
	 */
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		this.geneInfo = geneInfo;
	}
	
	public void update(boolean updateUniID)
	{
		
		
		
	}
	/**
	 * 升级GO数据库
	 */
	private void updateGene2Go()
	{
		for (Gene2Go gene2Go : lsGOInfoUpdate) {
			if (idType.equals(CopedID.IDTYPE_GENEID)) {
				servGene2Go.updateGene2Go(genUniID, gene2Go);
			}
			else if (idType.equals(CopedID.IDTYPE_UNIID)) {
				servUniGene2Go.updateUniGene2Go(genUniID, gene2Go);
			}
		}
	}
	
	/**
	 * 升级geneID数据库，并且将geneUniID按照数据库进行重置
	 * <b>只升级第一个获得的geneID</b>
	 * @param 如果在数据库中没有找到对应的ID，是否将ID导入UniID库
	 */
	private void updateGeneID(boolean updateUniID)
	{
		ArrayList<String> lsGenID = getUpdateGenUniID();
		//只升级第一个获得的geneID
		if (lsGenID != null && !lsGenID.get(0).equals(CopedID.IDTYPE_ACCID)) {
			this.idType = lsGenID.get(0);
			
			if (lsGenID.get(0).equals(CopedID.IDTYPE_GENEID)) {
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(accID);
				ncbiid.setDBInfo(this.databaseType);
				ncbiid.setGenUniID(lsGenID.get(2));
				ncbiid.setTaxID(Integer.parseInt(lsGenID.get(1)));
				servNCBIID.updateNCBIID(ncbiid, overrideDBinfo);
			}
			else if (lsGenID.get(0).equals(CopedID.IDTYPE_UNIID)) {
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(accID);
				uniProtID.setDBInfo(this.databaseType);
				uniProtID.setGenUniID(lsGenID.get(2));
				uniProtID.setTaxID(Integer.parseInt(lsGenID.get(1)));
				servUniProtID.updateUniProtID(uniProtID, overrideDBinfo);
			}
		}
	}
	/**
	 * 升级geneID
	 */
	private void updateGeneInfo()
	{
		if (geneInfo == null) {
			return;
		}
		if (idType.equals(CopedID.IDTYPE_UNIID)) {
			servUniGeneInfo.updateUniGenInfo(genUniID,geneInfo);
		}
		else if (idType.equals(CopedID.IDTYPE_GENEID)) {
			servGeneInfo.updateGenInfo(genUniID, geneInfo);
		}
		geneInfo = null;
	}
	
	/////////////////////////   升级 uniGene 的信息   /////////////////////////////////////////////////////
	
	/**
	 * 根据lsRefAccID的信息设定geneUniID和IDtype
	 * 获得所对应的geneUniID
	 * arraylist-string:0:为"geneID"或"uniID"或"accID"，1:taxID，2-之后：具体的geneID 或 UniID或accID
        没查到就返回accID-accID
	 */
	private ArrayList<String> getUpdateGenUniID()
	{
		//保存所有refID--也就是用于查找数据库的refxDB的信息ID，他们所对应的geneUniID
		ArrayList<ArrayList<String>> lsGenUniID = new ArrayList<ArrayList<String>>();
		for (String string : lsRefAccID) {
			ArrayList<String> lsTmpGenUniID = getNCBIUniTax(string, taxID);
			lsGenUniID.add(lsTmpGenUniID);
			if (lsTmpGenUniID.get(0).equals(CopedID.IDTYPE_ACCID)) {
				continue;
			}
			else if (lsTmpGenUniID.get(0).equals(CopedID.IDTYPE_GENEID) && lsTmpGenUniID.size() == 3) {
				genUniID = lsTmpGenUniID.get(3);
				idType = CopedID.IDTYPE_GENEID;
				return lsTmpGenUniID;
			}
		}
		//挑选出最短的geneID
		Collections.sort(lsGenUniID, new Comparator<ArrayList<String>>() {
			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				HashMap<String, Integer> hashAccIDtype2Int = new HashMap<String, Integer>();
				hashAccIDtype2Int.put(CopedID.IDTYPE_ACCID, 300);
				hashAccIDtype2Int.put(CopedID.IDTYPE_UNIID, 200);
				hashAccIDtype2Int.put(CopedID.IDTYPE_GENEID, 100);
				Integer o1Info = hashAccIDtype2Int.get(o1.get(0));
				Integer o2Info = hashAccIDtype2Int.get(o2.get(0));
				int flag = o1Info.compareTo(o2Info);
				if (flag != 0) {
					return flag;
				}
				else {
					Integer o1Len = o1.size();
					Integer o2Len = o2.size();
					return o1Len.compareTo(o2Len);
				}
			}
		});
		if (lsGenUniID.size() == 0) {
			return null;
		}
		return lsGenUniID.get(0);
	}

	///////////////////  static method //////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定一个accessID，如果该access是NCBIID，则返回NCBIID的geneID
	 * 如果是uniprotID，则返回Uniprot的UniID
	 * 如果输入得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目
	 * @param accID 输入的accID,没有内置去空格去点
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return arraylist-string:0:为"geneID"或"uniID"或"accID"，1:taxID，2-之后：具体的geneID 或 UniID或accID<br>
	 * 没查到就返回accID-accID
	 */
	public static ArrayList<String> getNCBIUniTax(String accID,int taxID) {
		ServNCBIID servGeneAnno = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//先查ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0)
		{
			lsResult.add(CopedID.IDTYPE_GENEID); lsResult.add(lsNcbiids.get(0).getTaxID()+"");
			for (NCBIID ncbiid2 : lsNcbiids) {
				if (ncbiid2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
					continue;
				}
				lsResult.add(ncbiid2.getGeneId()+"");
			}
			if (lsResult.size() <= 2) {
				lsResult.add(lsNcbiids.get(0).getGeneId()+"");
			}
			return lsResult;
		}
		//查不到查uniprotID
		else 
		{
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID); uniProtID.setTaxID(taxID);
			lsUniProtIDs = servUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) 
			{
				lsResult.add(CopedID.IDTYPE_UNIID);lsResult.add(lsUniProtIDs.get(0).getTaxID()+"");
				for (UniProtID uniProtID2 : lsUniProtIDs) {
					if (uniProtID2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
						continue;
					}
					lsResult.add(uniProtID2.getUniID());
				}
				if (lsResult.size() <= 2) {
					lsResult.add(lsUniProtIDs.get(0).getUniID()+"");
				}
				return lsResult;
			}
		}
		lsResult.add(CopedID.IDTYPE_ACCID); lsResult.add(taxID+"");
		lsResult.add(accID);
		return lsResult;
	}
	/////////////////////////////  重写equals等  ////////////////////////////////////
	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		CopedID otherObj = (CopedID)obj;
		
		if (
				//geneID相同且都不为“”，可以认为两个基因相同
				(!genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& idType.equals(otherObj.getIDtype())
				&& taxID == otherObj.getTaxID()
				)
				||//geneID都为""，那么如果两个accID相同且不为""，也可认为两个基因相同
				(genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& ( !accID.equals("") && !otherObj.getAccID().equals("") )
				&& accID.equals(otherObj.getAccID())
				&& idType.equals(otherObj.getIDtype())
				&& taxID == otherObj.getTaxID()
				)
				||
				(genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& accID.equals("") 
				&& otherObj.getAccID().equals("")						
				)
		)
		{
			return true;
		}
		return false;
	}

	
	/**
	 * 重写hashcode
	 */
	public int hashCode(){
		String hash = "";
		if (!genUniID.trim().equals("")) {
			hash = genUniID.trim()+"sep_@@_genUni_"+idType.trim()+"@@"+taxID;
		}
		else if (genUniID.trim().equals("") && !accID.trim().equals("")) {
			hash = accID.trim()+"@@accID"+idType.trim()+"@@"+taxID;
		}
		else if ( genUniID.trim().equals("") && accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}
	

	
	
	
	
}
