package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;

public class ServAnno {

	
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
			String[] geneAno2 = getGenInfo(geneID);
			geneAno[0] = geneAno2[0]; geneAno[1] = geneAno2[1];
			ncbiFlag = true;
		}
		else if (lsAccID.get(0).equals("uniID")) {
			String[] geneAno2 = getUniGenInfo(lsAccID.get(1));
			geneAno[0] = geneAno2[0]; geneAno[1] = geneAno2[1];
			uniprotFlag = true;
		}
		
		//blast��ʱ��������taxID
		if (blast) {
			//���ncbiid����
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
	 * @param accID �����accID,û������ȥ�ո�ȥ��
	 * @param taxID ����ID�������֪��������Ϊ0��ֻҪ����symbol������Ϊ0
	 * @return arraylist-string:0:Ϊ"geneID"��"uniID"��"accID"��1:taxID��2-֮�󣺾����geneID �� UniID��accID<br>
	 * û�鵽�ͷ���accID-accID
	 */
	public static ArrayList<String> getNCBIUniTax(String accID,int taxID) {
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		//�Ȳ�ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0)
		{
			lsResult.add(CopedID.IDTYPE_GENEID); lsResult.add(lsNcbiids.get(0).getTaxID()+"");
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
				lsResult.add(CopedID.IDTYPE_UNIID);lsResult.add(lsUniProtIDs.get(0).getTaxID()+"");
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
		lsResult.add(CopedID.IDTYPE_ACCID); lsResult.add(taxID+"");
		lsResult.add(accID);
		return lsResult;
	}
	
	
	
	/**
	 * ����һ��accessID�������access��NCBIID���򷵻�NCBIID��geneID
	 * �����uniprotID���򷵻�Uniprot��UniID
	 * �������õ����������ϵ�accID����ô�������ݿ�ΪDBINFO_SYNONYMS����Ŀ
	 * @param accID �����accID,û������ȥ�ո�ȥ��
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
		String ncbiFlag = CopedID.IDTYPE_GENEID; String uniprotFlag = CopedID.IDTYPE_UNIID; String nothing = CopedID.IDTYPE_ACCID;
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
	 * @param geneID
	 * @param taxID
	 * @return ������ѵ�NCBIID���ͷ��ص�һ��NCBIID������Ѳ������ͷ���null
	 */
	public static NCBIID getNCBIID(long geneID,int taxID) {
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids= DaoFSNCBIID.queryLsNCBIID(ncbiid);
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
	 * @return ������ѵ�UniProtID���ͷ��ص�һ��UniProtID������Ѳ������ͷ���null
	 */
	public static UniProtID getUniProtID(String uniID,int taxID) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(uniID);uniProtID.setTaxID(taxID);
		ArrayList<UniProtID> lsUniProtIDs= DaoFSUniProtID.queryLsUniProtID(uniProtID);
		if (lsUniProtIDs == null || lsUniProtIDs.size() < 1) {
			return null;
		}
		else {
			return lsUniProtIDs.get(0);
		}
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
