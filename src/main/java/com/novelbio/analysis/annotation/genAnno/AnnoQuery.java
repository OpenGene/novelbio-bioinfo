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
	 * ��arraytools�Ľ�����geneID,û��geneID�򽫱�accID������ȥ
	 * ����ڵ�colNum�еĺ��棬ֱ��д��excel�ļ�
	 * <b>��һ��һ���Ǳ�����</b>
	 * ���Ƚ�ָ��accID�е�ÿһ����regx�иȻ�󽫽������geneID����uniID��ֻ�ҵ�һ������geneID��uniID����Ŀ��Ȼ�󽫸�geneID��uniIDװ��excel
	 * @param excelFile
	 * @param taxID
	 * @param colNum ʵ����
	 * @param regx������ʽ ���Ϊ""���и�
	 */
	public static void annoGeneID(String excelFile, int taxID,int colNum,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//ȫ����ȡ����һ��Ϊtitle
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
	 * ��arraytools�Ľ�����annotation
	 * ����ڵ�colNum�еĺ��棬ֱ��д��excel�ļ�
	 * <b>��һ��һ���Ǳ�����</b>
	 * @param excelFile
	 * @param taxID
	 * @param colNum ʵ����
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param regx ���Ϊ""���и�
	 */
	public static void anno(String excelFile, int taxID,int colNum,boolean blast,int StaxID,double evalue,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//ȫ����ȡ����һ��Ϊtitle
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
	 * ����queryID�����ؾ�����Ϣ,�Ȳ���NCBIID��û�ҵ��Ͳ�Uniprot��
	 * @param accID queryID
	 * @param taxID ����ID���������Symbol������Ϊ0
	 * @param blast �Ƿ�blast blast�����ncbi���ݿ�
	 * @param StaxID blast��Ŀ������ID
	 * @param evalue blast��evalueֵ
	 * @return
	 * ���blast
	 *  * 0:symbol
			 * 1:description
			 * 2:subjectTaxID
			 * 3:evalue
			 * 4:symbol
			 * 5:description
		�����blast
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
		//��ʼ��
		for (int i = 0; i < geneAno.length; i++) {
			geneAno[i] = "";
		}
		//�ܷ���NCBIID���ҵ�
		boolean ncbiFlag = false;
		//�ܷ���uniprotID���ҵ�
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
		
		//blast��ʱ��������taxID
		if (blast) {
			//���ncbiid����
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
				//�����blast�Ľ��
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
	 * ���������NCBIgeneID����û��Symbol����������ѡ��һ��accID.���geneID��0������""
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
	 * ���������uniID����û��Symbol����������ѡ��һ��accID�����uniID��""������""
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
	 * ����һ��accessID�������access��NCBIID���򷵻�NCBIID��geneID
	 * �����uniprotID���򷵻�Uniprot��UniID
	 * �������õ����������ϵ�accID����ô�������ݿ�ΪDBINFO_SYNONYMS����Ŀ
	 * @param accID �����accID
	 * @param taxID ����ID�������֪��������Ϊ0��ֻҪ����symbol������Ϊ0
	 * @return arraylist-string:0:Ϊ"geneID"��"uniID"��"accID"��1-֮�󣺾����geneID �� UniID��accID<br>
	 * û�鵽�ͷ���accID-accID
	 */
	public static ArrayList<String> getNCBIUni(String accID,int taxID) {
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//װ������string0��Ϣ
		String ncbiFlag = "geneID"; String uniprotFlag = "uniID"; String nothing = "accID";
		//�Ȳ�ncbiid
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
		//�鲻����uniprotID
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
	 * ����geneID�����ظ�geneID����Ϣ.���geneID==0���򷵻�null
	 * ���û���ҵ�������getGenName��㷵��һ��accID��description����
	 * @param geneID
	 * @return string[2] 0��symbol 1:description
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
	 * ����uniID�����ظ�uniID����Ϣ.���uniIDΪ�������򷵻�null
	 * ���û���ҵ�������getGenName��㷵��һ��accID��description����
	 * @param uniID �ڲ������trim����
	 * @return string[2] 0��symbol 1:description
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
	 * ע��ncbiid�б��뺬��geneID����Ϊ�������ȥ����blast��Ϣ��
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(NCBIID ncbiid,double evalue,int StaxID) {
		//��ʼ��ѯ
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
	 * ע��uniProtID�б��뺬��uniID����Ϊ�������ȥ����blast��Ϣ��
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(UniProtID uniProtID,double evalue,int StaxID) {
		//��ʼ��ѯ
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
	 * @param accID ֱ����accIDȥ��blast
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	public static BlastInfo getBlastInfo(String accID,double evalue,int StaxID) {
		//��ʼ��ѯ
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
	 * ��ncbiid��geneIDȥ����
	 * @param ncbiid
	 * @return
	 */
	public static GeneInfo getGenInfo(NCBIID ncbiid) {
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setGeneID(ncbiid.getGeneId());
		return DaoFSGeneInfo.queryGeneInfo(geneInfo);
	}
	/**
	 * ��uniProtID��uniIDȥ����
	 * @param ncbiid
	 * @return
	 */
	public static UniGeneInfo getUniGenInfo(UniProtID uniProtID) {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setGeneID(uniProtID.getUniID());
		return DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
}
