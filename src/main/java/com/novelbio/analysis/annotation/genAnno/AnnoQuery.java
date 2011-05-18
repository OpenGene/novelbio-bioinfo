package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.FriceDAO.DaoFCGene2GoInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;

public class AnnoQuery {
	/**
	 * 
	 * 给arraytools的结果添加geneID,没有geneID则将本accID附加上去
	 * 添加在第colNum列的后面，直接写入excel文件
	 * <b>第一行一定是标题行</b>
	 * 首先将指定accID列的每一项用regx切割，然后将结果查找geneID或者uniID，只找第一个含有geneID或uniID的项目，然后将该geneID或uniID装入excel
	 * @param excelFile
	 * @param taxID
	 * @param colNum 实际列
	 * @param regx正则表达式 如果为""则不切割
	 */
	public static void annoGeneID(String excelFile, int taxID,int colNum,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//全部读取，第一行为title
		String[][] geneInfo = excelAnno.ReadExcel(1, 1, excelAnno.getRowCount(), excelAnno.getColCount(2));
		ArrayList<String[]> lsgenAno = new ArrayList<String[]>();
		for (int i = 1; i < geneInfo.length; i++) {
			String[] accID = null;
			if (regx.equals("")) {
				accID = new String[1];
				accID[0] =CopeID.removeDot(geneInfo[i][colNum]);
			}
			else {
				accID = geneInfo[i][colNum].split(regx);
			}
			String thisaccID = accID[0];
			for (int j = 0; j < accID.length; j++) {
				ArrayList<String> lsTmpaccID = getNCBIUni(CopeID.removeDot(accID[j]), taxID);
				if (!lsTmpaccID.get(0).equals("accID")) {
					thisaccID = lsTmpaccID.get(1);
					break;
				}
			}
			String[] tmpAno = new String[1];
			tmpAno[0] =thisaccID; 
			lsgenAno.add(tmpAno);
		}
		String[][] geneAno = new String[geneInfo.length][lsgenAno.get(0).length];
		geneAno[0][0] = "geneID/uniID";
		for (int i = 1; i < geneAno.length; i++) {
			for (int j = 0; j < geneAno[0].length; j++) {
				geneAno[i][j] = lsgenAno.get(i-1)[j];
			}
		}
		String[][] dataResult = ArrayOperate.combArray(geneInfo, geneAno, colNum+1);
		excelAnno.WriteExcel(1, 1, dataResult);
	}	
	/**
	 * 给arraytools的结果添加annotation
	 * 添加在第colNum列的后面，直接写入excel文件
	 * <b>第一行一定是标题行</b>
	 * @param excelFile
	 * @param taxID
	 * @param colNum 实际列
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param regx 如果为""则不切割
	 */
	public static void anno(String excelFile, int taxID,int colNum,boolean blast,int StaxID,double evalue,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//全部读取，第一行为title
		String[][] geneInfo = excelAnno.ReadExcel(1, 1, excelAnno.getRowCount(), excelAnno.getColCount(2));
		ArrayList<String[]> lsgenAno = new ArrayList<String[]>();
		for (int i = 1; i < geneInfo.length; i++) {
			String[] accID = null;
			if (regx.equals("")) {
				accID = new String[1];
				accID[0] =CopeID.removeDot(geneInfo[i][colNum]);
			}
			else {
				accID = geneInfo[i][colNum].split(regx);
			}
			String thisaccID = geneInfo[i][colNum];
			for (int j = 0; j < accID.length; j++) {
				ArrayList<String> lsTmpaccID = getNCBIUni(CopeID.removeDot(accID[j]), taxID);
				if (!lsTmpaccID.get(0).equals("accID")) {
					thisaccID = accID[j];
					break;
				}
			}
			String[] tmpAno = getAnno(thisaccID,taxID, blast, StaxID, evalue);
			lsgenAno.add(tmpAno);
		}
		String[][] geneAno = new String[geneInfo.length][lsgenAno.get(0).length];
		geneAno[0][0] = "Symbol";geneAno[0][1] = "Description";
		if (blast) {
			geneAno[0][2] = "subjectTaxID"; geneAno[0][3] = "evalue";
			geneAno[0][4] = "symbol"; geneAno[0][5] = "description";
		}
		for (int i = 1; i < geneAno.length; i++) {
			for (int j = 0; j < geneAno[0].length; j++) {
				geneAno[i][j] = lsgenAno.get(i-1)[j];
			}
		}
		String[][] dataResult = ArrayOperate.combArray(geneInfo, geneAno, colNum+1);
		excelAnno.WriteExcel(1, 1, dataResult);
	}
	

	
	
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
			geneAno = getGenInfo(geneID);
			ncbiFlag = true;
		}
		else if (lsAccID.get(0).equals("uniID")) {
			geneAno = getUniGenInfo(lsAccID.get(1));
			uniprotFlag = true;
		}
		
		//blast的时候不用设置taxID
		if (blast) {
			//如果ncbiid存在
			BlastInfo blastInfo = new BlastInfo();
			if (ncbiFlag || uniprotFlag) {
				blastInfo.setQueryID(lsAccID.get(1));
			}
			else {
				blastInfo.setQueryID(accID);
				blastInfo.setQueryTax(taxID);
				blastInfo.setSubjectTax(StaxID);
			}
				ArrayList<BlastInfo> lsSblastInfos = DaoFSBlastInfo.queryLsBlastInfo(blastInfo);
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
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);
		//ncbiid.setDBInfo("");
		if (ncbiid.getGeneId()>0) {
			ArrayList<NCBIID> lsncbiidsub = DaoFSNCBIID.queryLsNCBIID(ncbiid);
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
			ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
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
	 * @param accID 输入的accID
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return arraylist-string:0:为"geneID"或"uniID"或"accID"，1-之后：具体的geneID 或 UniID或accID<br>
	 * 没查到就返回accID-accID
	 */
	public static ArrayList<String> getNCBIUni(String accID,int taxID) {
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//装入结果的string0信息
		String ncbiFlag = "geneID"; String uniprotFlag = "uniID"; String nothing = "accID";
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
			lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniProtID);
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
		GeneInfo geneInfo2 = DaoFSGeneInfo.queryGeneInfo(geneInfo);
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
		uniGeneInfo.setGeneID(uniID);
		UniGeneInfo uniGeneInfo2 = DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
		
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
	 * @param ncbiid
	 * 注意ncbiid中必须含有geneID，因为是用这个去查找blast信息的
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(NCBIID ncbiid,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(ncbiid.getGeneId()+"");
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = DaoFSBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	/**
	 * @param uniProtID
	 * 注意uniProtID中必须含有uniID，因为是用这个去查找blast信息的
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(UniProtID uniProtID,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(uniProtID.getUniID());
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = DaoFSBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 * @param accID 直接用accID去做blast
	 * @param evalue
	 * @param StaxID 需要比较到的物种ID，如果为0，则不考虑物种ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(String accID,double evalue,int StaxID) {
		//开始查询
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(accID);
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = DaoFSBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	
	/**
	 * 用ncbiid的geneID去搜索
	 * @param ncbiid
	 * @return
	 */
	public static GeneInfo getGenInfo(NCBIID ncbiid) {
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setGeneID(ncbiid.getGeneId());
		return DaoFSGeneInfo.queryGeneInfo(geneInfo);
	}
	/**
	 * 用uniProtID的uniID去搜索
	 * @param ncbiid
	 * @return
	 */
	public static UniGeneInfo getUniGenInfo(UniProtID uniProtID) {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setGeneID(uniProtID.getUniID());
		return DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
}
