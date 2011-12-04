package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;

public class ServAnno {

	
	/**
	 * 给定queryID，返回具体信息,先查找NCBIID表，没找到就查Uniprot表
	 * @param accID queryID
	 * @param taxID 物种ID，如果不是Symbol，可以为0
	 * @param blast 是否blast blast仅针对ncbi数据库
	 * @param StaxID blast的目标物种ID
	 * @param evalue blast的evalue值
	 * @return
	 * 如果blast
	 *  * 0:symbol
			 * 1:description
			 * 2:subjectTaxID
			 * 3:evalue
			 * 4:symbol
			 * 5:description
		如果不blast
	 			* 0:symbol
			 * 1:description
	 */
	public static String[] getAnno(String accID, int taxID,boolean blast,int StaxID,double evalue)
	{
		String[] geneAno = null;
		if (blast) {
			/**
			 * 0:symbol
			 * 1:description
			 * 2:subjectTaxID
			 * 3:evalue
			 * 4:symbol
			 * 5:description
			 */
			geneAno = new String[6];
		}
		else {
			/**
			 * 0:symbol
			 * 1:description
			 */
			geneAno = new String[2];
		}
		//初始化
		for (int i = 0; i < geneAno.length; i++) {
			geneAno[i] = "";
		}
		//能否在NCBIID中找到
		boolean ncbiFlag = false;
		//能否在uniprotID中找到
		boolean uniprotFlag  = false;
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<String> lsAccID = getNCBIUni(accID, taxID);
		if (lsAccID.get(0).equals("geneID")) {
			long geneID = Integer.parseInt(lsAccID.get(1));
			String[] geneAno2 = getGenInfo(geneID);
			geneAno[0] = geneAno2[0]; geneAno[1] = geneAno2[1];
			ncbiFlag = true;
		}
		else if (lsAccID.get(0).equals("uniID")) {
			String[] geneAno2 = getUniGenInfo(lsAccID.get(1));
			geneAno[0] = geneAno2[0]; geneAno[1] = geneAno2[1];
			uniprotFlag = true;
		}
		
		//blast的时候不用设置taxID
		if (blast) {
			//如果ncbiid存在
			BlastInfo blastInfo = new BlastInfo();
			if (ncbiFlag || uniprotFlag) {
				blastInfo.setQueryID(lsAccID.get(1));
				blastInfo.setQueryTax(taxID);
				blastInfo.setSubjectTax(StaxID);
			}
			else {
				blastInfo.setQueryID(accID);
				blastInfo.setQueryTax(taxID);
				blastInfo.setSubjectTax(StaxID);
			}
				ArrayList<BlastInfo> lsSblastInfos = MapBlastInfo.queryLsBlastInfo(blastInfo);
				//如果有blast的结果
				if (lsSblastInfos != null && lsSblastInfos.size()>0 && lsSblastInfos.get(0).getEvalue()<=evalue) 
				{
					long subID = Long.parseLong(lsSblastInfos.get(0).getSubjectID());
					String[] subGenInfo = getGenInfo(subID);
					geneAno[2] = StaxID+""; geneAno[3] =  lsSblastInfos.get(0).getEvalue() + "";
					geneAno[4] = subGenInfo[0];
					geneAno[5] = subGenInfo[1];
				}
			}
		return geneAno;
	}
	
	/**
	 * 给定基因的NCBIgeneID，在没有Symbol的情况下随便选择一个accID.如果geneID是0，返回""
	 * @return
	 */
	public static String getGenName(long geneID) {
		ServNCBIID servAnno = new ServNCBIID();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);
		//ncbiid.setDBInfo("");
		if (ncbiid.getGeneId()>0) {
			ArrayList<NCBIID> lsncbiidsub = servAnno.queryLsNCBIID(ncbiid);
			if (lsncbiidsub!=null && lsncbiidsub.size()>0)  
				return lsncbiidsub.get(0).getAccID();
			else 
				return "";
		}
		else {
			return "";
		}
	}
	/**
	 * 给定基因的uniID，在没有Symbol的情况下随便选择一个accID，如果uniID是""，返回""
	 * @return
	 */
	public static String getUniGenName(String uniID) {
		UniProtID uniProtID = new UniProtID(); uniProtID.setUniID(uniID);
		if (uniProtID.getUniID() != null && !uniProtID.getUniID().equals("")) {
			ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs!=null && lsUniProtIDs.size()>0)
				return lsUniProtIDs.get(0).getAccID();
			else
				return "";
		}
		else {
			return "";
		}
	}
	
	
	/**
	 * 给定一个accessID，如果该access是NCBIID，则返回NCBIID的geneID
	 * 如果是uniprotID，则返回Uniprot的UniID
	 * 如果输入得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目
	 * @param accID 输入的accID,没有内置去空格去点
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return arraylist-string:0:为"geneID"或"uniID"或"accID"，1:taxID，2-之后：具体的geneID 或 UniID或accID<br>
	 * 没查到就返回accID-taxID-accID
	 */
	public static ArrayList<String> getNCBIUniTax(String accID,int taxID) {
		ServNCBIID servAnno = new ServNCBIID();
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servAnno.queryLsNCBIID(ncbiid);
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
			lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
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
	
	
	
	/**
	 * 给定一个accessID，如果该access是NCBIID，则返回NCBIID的geneID
	 * 如果是uniprotID，则返回Uniprot的UniID
	 * 如果输入得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目
	 * @param accID 输入的accID,没有内置去空格去点
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return arraylist-string:0:为"geneID"或"uniID"或"accID"，1-之后：具体的geneID 或 UniID或accID<br>
	 * 没查到就返回accID-accID
	 */
	public static ArrayList<String> getNCBIUni(String accID,int taxID) {
		ServNCBIID servAnno = new ServNCBIID();
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servAnno.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//装入结果的string0信息
		String ncbiFlag = CopedID.IDTYPE_GENEID; String uniprotFlag = CopedID.IDTYPE_UNIID; String nothing = CopedID.IDTYPE_ACCID;
		//先查ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0)
		{
			lsResult.add(ncbiFlag);
			for (NCBIID ncbiid2 : lsNcbiids) {
				if (ncbiid2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
					continue;
				}
				lsResult.add(ncbiid2.getGeneId()+"");
			}
			if (lsResult.size() <= 1) {
				lsResult.add(lsNcbiids.get(0).getGeneId()+"");
			}
			return lsResult;
		}
		//查不到查uniprotID
		else 
		{
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID); uniProtID.setTaxID(taxID);
			lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) 
			{
				lsResult.add(uniprotFlag);
				for (UniProtID uniProtID2 : lsUniProtIDs) {
					if (uniProtID2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
						continue;
					}
					lsResult.add(uniProtID2.getUniID());
				}
				if (lsResult.size() <= 1) {
					lsResult.add(lsUniProtIDs.get(0).getUniID()+"");
				}
				return lsResult;
			}
		}
		lsResult.add(nothing);lsResult.add(accID);
		return lsResult;
	}
	
	/**
	 * 给定geneID，返回该geneID的信息.如果geneID==0，则返回null
	 * 如果没有找到，调用getGenName随便返回一个accID，description留空
	 * @param geneID
	 * @return string[2] 0：symbol 1:description
	 */
	public static String[] getGenInfo(long geneID)
	{
		if (geneID==0) {
			return null;
		}
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setGeneID(geneID);
		GeneInfo geneInfo2 = MapGeneInfo.queryGeneInfo(geneInfo);
		String[] result = new String[2];
		if (geneInfo2 == null) {
			result[0] = getGenName(geneID);
			result[1] = "";
			return result;
		}
		else {
			result[0] = geneInfo2.getSymbol().split("//")[0];
			if (result[0].equals(""))
			{
				result[0] = getGenName(geneID);
			}
			result[1] = geneInfo2.getDescription();
			return result;
		}
	}
	
	/**
	 * 给定uniID，返回该uniID的信息.如果uniID为“”，则返回null
	 * 如果没有找到，调用getGenName随便返回一个accID，description留空
	 * @param uniID 内部会进行trim处理
	 * @return string[2] 0：symbol 1:description
	 */
	public static String[] getUniGenInfo(String uniID)
	{
		uniID = uniID.trim();
		if (uniID.equals("")) {
			return null;
		}
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setUniProtID(uniID);
		UniGeneInfo uniGeneInfo2 = MapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
		
		String[] result = new String[2];
		if (uniGeneInfo2 == null) {
			result[0] = getUniGenName(uniID);
			result[1] = "";
			return result;
		}
		else {
			result[0] = uniGeneInfo2.getSymbol().split("//")[0];
			if (result[0].equals(""))
			{
				result[0] = getUniGenName(uniID);
			}
			result[1] = uniGeneInfo2.getDescription();
			return result;
		}
	}
	/**
	 * @param geneID
	 * @param taxID
	 * @return 如果能搜到NCBIID，就返回第一个NCBIID。如果搜不到，就返回null
	 */
	public static NCBIID getNCBIID(long geneID,int taxID) {
		ServNCBIID servAnno = new ServNCBIID();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids= servAnno.queryLsNCBIID(ncbiid);
		if (lsNcbiids == null || lsNcbiids.size() < 1) {
			return null;
		}
		else {
			return lsNcbiids.get(0);
		}
	}
	/**
	 * 获得指定数据库的accID，譬如可以指定数据库为TAIR等，
	 * 数据库名称：在NovelBioConst类中
	 * @param geneUniID
	 * @param idTpye
	 * @param taxID
	 * @param dbInfo
	 * @return
	 */
	public static String getAccIDInfo(String geneUniID, String idTpye ,int taxID, String dbInfo) {
		if (idTpye.equals(CopedID.IDTYPE_ACCID)) {
			return null;
		}
		else if (idTpye.equals(CopedID.IDTYPE_GENEID)){
			NCBIID ncbiid = getNCBIID(Long.parseLong(geneUniID), taxID, dbInfo);
			if (ncbiid != null) {
				return ncbiid.getAccID();
			}
		}
		else if (idTpye.equals(CopedID.IDTYPE_UNIID)) {
			UniProtID uniProtID = getUniProtID(geneUniID, taxID, dbInfo);
			if (uniProtID != null) {
				return uniProtID.getAccID();
			}
		}
			return null;
	}
	
	
	/**
	 * @param geneID
	 * @param taxID
	 * @return 如果能搜到NCBIID，就返回第一个NCBIID。如果搜不到，就返回null
	 */
	private static NCBIID getNCBIID(long geneID,int taxID,String dbInfo) {
		ServNCBIID servAnno = new ServNCBIID();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);ncbiid.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ncbiid.setDBInfo(dbInfo);
		}
		ArrayList<NCBIID> lsNcbiids= servAnno.queryLsNCBIID(ncbiid);
		if (lsNcbiids == null || lsNcbiids.size() < 1) {
			return null;
		}
		else {
			return lsNcbiids.get(0);
		}
	}
	/**
	 * @param uniID
	 * @param taxID
	 * @return 如果能搜到UniProtID，就返回第一个UniProtID。如果搜不到，就返回null
	 */
	private static UniProtID getUniProtID(String uniID,int taxID, String dbInfo) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(uniID);uniProtID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			uniProtID.setDBInfo(dbInfo);
		}
		ArrayList<UniProtID> lsUniProtIDs= MapUniProtID.queryLsUniProtID(uniProtID);
		if (lsUniProtIDs == null || lsUniProtIDs.size() < 1) {
			return null;
		}
		else {
			return lsUniProtIDs.get(0);
		}
	}
	
	
	/**
	 * @param uniID
	 * @param taxID
	 * @return 如果能搜到UniProtID，就返回第一个UniProtID。如果搜不到，就返回null
	 */
	public static UniProtID getUniProtID(String uniID,int taxID) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(uniID);uniProtID.setTaxID(taxID);
		ArrayList<UniProtID> lsUniProtIDs= MapUniProtID.queryLsUniProtID(uniProtID);
		if (lsUniProtIDs == null || lsUniProtIDs.size() < 1) {
			return null;
		}
		else {
			return lsUniProtIDs.get(0);
		}
	}
	/**
	 * 用ncbiid的geneID去搜索
	 * @param ncbiid
	 * @return
	 */
	public static GeneInfo getGenInfo(NCBIID ncbiid) {
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setGeneID(ncbiid.getGeneId());
		return MapGeneInfo.queryGeneInfo(geneInfo);
	}
	/**
	 * 用uniProtID的uniID去搜索
	 * @param ncbiid
	 * @return
	 */
	public static UniGeneInfo getUniGenInfo(UniProtID uniProtID) {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setUniProtID(uniProtID.getUniID());
		return MapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
}
