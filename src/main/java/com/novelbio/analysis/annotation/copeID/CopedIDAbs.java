package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegGenEntryKO;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.AGeneInfo;
import com.novelbio.database.entity.friceDB.AgeneUniID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServBlastInfo;

public abstract class CopedIDAbs implements CopedIDInt{
//	public final static String IDTYPE_ACCID = "accID"; 
//	public final static String IDTYPE_GENEID = "geneID";
//	public final static String IDTYPE_UNIID = "uniID"; 
//	
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
	
	String description = null;
	
	String symbol = null;
	
	BlastInfo blastInfo = null;
	
	/**
	 * 譬方和多个物种进行blast，然后结合这些物种的信息，取并集
	 */
	ArrayList<BlastInfo> lsBlastInfos = null;
	
	
	
	double evalue = 10;
	
	KegGenEntryKO kegGenEntryKO = null;
	
	AGeneInfo geneInfo = null;
	ArrayList<AGene2Go> lsGene2Gos = null;
	
	String databaseType = "";
	
	KeggInfo keggInfo;
	
	/**
	 * 单个物种的blast
	 * 获得本copedID blast到对应物种的blastInfo信息，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
//	@Deprecated
	public BlastInfo setBlastInfo(int StaxID, double evalue) {
		if (blastInfo == null) {
			BlastInfo blastInfoTmp = new BlastInfo();
			blastInfoTmp.setEvalue(evalue);
			blastInfoTmp.setQueryID(genUniID);
			blastInfoTmp.setSubjectTax(StaxID);
			ArrayList<BlastInfo> lsBlastInfos = DaoFSBlastInfo.queryLsBlastInfo(blastInfoTmp);
			if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
			{
				Collections.sort(lsBlastInfos);//排序选择最小的一项
				blastInfo = lsBlastInfos.get(0);
				this.evalue = blastInfo.getEvalue();
			}
		}
		return blastInfo;
	}
	/**
	 * 设定多个物种进行blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastLsInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			BlastInfo blastInfoTmp = new BlastInfo();
			blastInfoTmp.setEvalue(evalue);
			blastInfoTmp.setQueryID(genUniID);
			blastInfoTmp.setSubjectTax(i);
			lsBlastInfos = DaoFSBlastInfo.queryLsBlastInfo(blastInfoTmp);
			if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
			{
				Collections.sort(lsBlastInfos);//排序选择最小的一项
				BlastInfo blastInfo = lsBlastInfos.get(0);
				lsBlastInfos.add(blastInfo);
			}
		}
	}
	
	/**
	 * 单个物种的blast
	 * 获得本copedID blast到对应物种的copedID，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getBlastCopedID(int StaxID,double evalue) {
		BlastInfo blastInfo = setBlastInfo(StaxID, evalue);
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
		CopedID copedID = new CopedID(idType,blastInfo.getSubjectID(), StaxID);
		return copedID;
	}
	
	
	/**
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
	 * 如果没有结果，直接返回null
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public ArrayList<CopedID> getBlastLsCopedID() {
		ArrayList<CopedID> lsResult = new ArrayList<CopedID>();
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return null;
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
		return taxID;
	}
	
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() {
		setSymbolDescrip();
		return description;
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
			description = "";
		}
		setGenInfo();
		if (geneInfo == null) {
			symbol = getGenName(getGenUniID(),databaseType);
			description = "";
		}
		else {
			symbol = geneInfo.getSymbol().split("//")[0];
			description = geneInfo.getDescription().replaceAll("\"", "");
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
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * @param blast 是否blast到相应物种查看
	 * @param StaxID 如果blast为true，那么设定StaxID
	 * @return 如果没有就返回null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) {
		if (!blast) {
			return setKegGenEntryKO().getLsKGentries();
		}
		else {
			//如果本基因能找到keggID就不进行blast
			if ( setKegGenEntryKO().getLsKGentries() != null) {
				return setKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getBlastCopedID(StaxID,evalue);
			return ScopedID.getKegEntity(false, 0, 0);
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
	 * 如果blast * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast, int StaxID, double evalue) {
		String[] tmpAnno = null;
		if (blast) {
			tmpAnno = new String[6];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbo(); tmpAnno[1] = getDescription(); tmpAnno[2] = StaxID + ""; 
			CopedID copedIDBlast = getBlastCopedID(StaxID, evalue);
			if (copedIDBlast != null) {
				tmpAnno[3] = this.evalue + "";
				tmpAnno[4] = copedIDBlast.getSymbo();
				tmpAnno[5] = copedIDBlast.getDescription().replaceAll("\"", "");
			}
		}
		else {
			tmpAnno = new String[2];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbo(); tmpAnno[1] = getDescription(); 
		}
		return tmpAnno;
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
	 * 获得该copedID的KegPath
	 */
	public ArrayList<KGpathway> getKegPath() {
		getKeggInfo();
		return keggInfo.getLsKegPath();
	}
	/**
	 * 	blast多个物种
	 * 首先设定blast的物种
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	public ArrayList<KGpathway> getBlastKegPath() {
		getKeggInfo();
		ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
		ArrayList<CopedID> lsBlastCopedIDs = getBlastLsCopedID();
		for (CopedID copedID : lsBlastCopedIDs) {
			lskeggInfo.add(copedID.getKeggInfo());
		}
		return keggInfo.getLsKegPath(lskeggInfo);
	}
	
	/**
	 * blast单个物种
	 * 给定blast到的copedID，用 getBlastCopedID(int StaxID,double evalue) 方法获得
	 * 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 */
	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID) {
		getKeggInfo();
		ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
		lskeggInfo.add(copedID.getKeggInfo());
		return keggInfo.getLsKegPath(lskeggInfo);
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
