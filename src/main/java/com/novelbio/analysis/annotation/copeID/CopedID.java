package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSTaxID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.TaxInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.service.ServAnno;

/**
 * ��ֻ��һ�������� ר�ŶԻ����ID��һЩ�������<br>
 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b><br>
 * ���Խ������ID�ϲ����������ҽ���ɢ��ID�洢��һ��Hashmap��
 * ��genUniID����ʱ�����Ƿ����ֻ�Ƚ�genUniID��taxID��idType�Ƿ����
 * ��genUniID�����ڣ�accID����ʱ���Ƚ�accID��taxID��idType�Ƿ���� ����������ʱ����Ϊ��ͬ HashCode���趨���������
 * 
 * @author zong0jie
 */
public class CopedID implements CopedIDInt{
	public final static String IDTYPE_ACCID = "accID"; 
	public final static String IDTYPE_GENEID = "geneID";
	public final static String IDTYPE_UNIID = "uniID";

	private CopedIDAbs copedID;

	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public CopedID(String idType, String genUniID, int taxID) {
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni("",idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen("",idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc("",idType, genUniID, taxID);
		}
	}

	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public CopedID(String accID,String idType, String genUniID, int taxID) {
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni(accID,idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen(accID,idType, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc(accID,idType, genUniID, taxID);
		}
	}
	
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID���������䱾�ࡣ
	 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b>
	 * ����ֻ�ܲ���һ��CopedID���������һ��accIDҪ�������geneID����ô����ѡ��getLsCopedID����
	 * @param accID �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 * @param blastType �����accID�Ƿ����� blast�Ľ�����磺dbj|AK240418.1|����ô���AK240418��һ�㶼��false
	 */
	public CopedID(String accID,int taxID,boolean blastType) {
		if (blastType)
			accID = getBlastAccID(accID);
		else
			accID = removeDot(accID);
		ArrayList<String> lsaccID = getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		String tmpGenID = lsaccID.get(2);
		if (idType.equals(IDTYPE_UNIID)) {
			copedID = new CopedIDuni(accID, idType, tmpGenID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			copedID = new CopedIDgen(accID,idType, tmpGenID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			copedID = new CopedIDacc(accID,idType, tmpGenID, taxID);
		}
	}
	
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID�������䱾�ࡣ
	 * @param accID �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 * @param blastType �����accID�Ƿ����� blast�Ľ�����磺dbj|AK240418.1|����ô���AK240418��һ�㶼��false
	 */
	public static ArrayList<CopedID> getLsCopedID(String accID,int taxID,boolean blastType) {
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		if (blastType) 
			accID = accID.split("\\|")[1];
		accID = removeDot(accID);
		
		ArrayList<String> lsaccID = getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		 for (int i = 2 ; i < lsaccID.size(); i++) {
			 String tmpGenID = lsaccID.get(i);
			 CopedID copedID = new CopedID(accID, idType, tmpGenID, taxID);
			 lsCopedIDs.add(copedID);
		 }
		 return lsCopedIDs;
	}
	
	
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ���֤geneUniID�������䱾�ࡣ
	 * ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null
	 * ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param idType ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID ����ID
	 */
	public static CopedID validCopedID(String idType, String genUniID,int taxID) {
		CopedID copedID = null;
		if (idType.equals(IDTYPE_ACCID)) {
			return null;
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			NCBIID ncbiid = ServAnno.getNCBIID(Integer.parseInt(genUniID), taxID);
			if (ncbiid != null) {
				String genUniID2 = ncbiid.getGeneId() + "";
				int taxID2 = ncbiid.getTaxID();
				copedID = new CopedID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			UniProtID uniProtID = ServAnno.getUniProtID(genUniID, taxID);
			if (uniProtID != null) {
				String genUniID2 = uniProtID.getUniID();
				int taxID2 = uniProtID.getTaxID();
				copedID = new CopedID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		return null;
	}
	
	
	@Override
	public BlastInfo setBlastInfo(int StaxID, double evalue) {
		return copedID.setBlastInfo(StaxID, evalue);
	}

	@Override
	public CopedID getBlastCopedID(int StaxID, double evalue) {
		return copedID.getBlastCopedID(StaxID, evalue);
	}

	@Override
	public String getIDtype() {
		return copedID.getIDtype();
	}

	@Override
	public String getAccID() {
		return copedID.getAccID();
	}

	@Override
	public String getGenUniID() {
		return copedID.getGenUniID();
	}

	@Override
	public int getTaxID() {
		return copedID.getTaxID();
	}
	
	@Override
	public String getDescription() {
		return copedID.getDescription();
	}
	
	@Override
	public String getSymbo() {
		return copedID.getSymbo();
	}

	@Override
	public ArrayList<KegEntity> getKegEntity(boolean blast, int StaxID,
			double evalue) {
		return copedID.getKegEntity(blast, StaxID, evalue);
	}



	@Override
	public String getAccIDDBinfo(String dbInfo) {
		return copedID.getAccIDDBinfo(dbInfo);
	}

	@Override
	public String[] getAnno(boolean blast, int StaxID, double evalue) {
		return copedID.getAnno(blast, StaxID, evalue);
	}
	@Override
	public KeggInfo getKeggInfo() {
		return copedID.getKeggInfo();
	}

	@Override
	public ArrayList<CopedID> getBlastLsCopedID() {
		return copedID.getBlastLsCopedID();
	}

	@Override
	public ArrayList<KGpathway> getBlastKegPath() {
		return copedID.getBlastKegPath();
	}

	@Override
	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID) {
		return copedID.getBlastKegPath(copedID);
	}

	@Override
	public ArrayList<KGpathway> getKegPath() {
		return copedID.getKegPath();
	}

	@Override
	public void setBlastLsInfo(double evalue, int... StaxID) {
		copedID.setBlastLsInfo(evalue,StaxID);
	}

	
	
	
	/////////////////////////////  static ����  ////////////////////////////////////
	/**
	 * blast�Ľ����������dbj|AK240418.1|
	 * �������AK240418ץ����������
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}

	/**
	 *  ���ȳ�ȥ�ո�
	 * �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID)
	{
		String tmpGeneID = accID.trim();
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//�������XM_002121.1����
		if (dotIndex>0 && tmpGeneID.length() - dotIndex == 2) {
			tmpGeneID = tmpGeneID.substring(0,dotIndex);
		}
		return tmpGeneID;
	}
	/**
	 * @param collectionAccID �����accIDlist�������ȶ�accIDȥһ���ظ�
	 * @param taxID
	 * @param combineID
	 * @return
	 */
	public static ArrayList<CopedID> getLsCopedID(Collection<String> collectionAccID, int taxID, boolean combineID) {
		ArrayList<CopedID> lsResult = new ArrayList<CopedID>();
		HashSet<CopedID> hashCopedIDs = null; //��hash����ȥ�ظ�����Ϊ�Ѿ���д��hash��
		if (combineID) {
			hashCopedIDs = new HashSet<CopedID>();
		}
		for (String string : collectionAccID) {
			CopedID copedID = new CopedID(string, taxID, false);
			if (combineID) {
				hashCopedIDs.add(copedID);
			}
			else {
				lsResult.add(copedID);
			}
		}
		
		if (combineID) {
			lsResult = new ArrayList<CopedID>();
			for (CopedID copedID : hashCopedIDs) {
				lsResult.add(copedID);
			}
		}
		return lsResult;
	}
	
	/**
	 * @param collectionAccID �����accIDlist�������ȶ�accIDȥһ���ظ�
	 * @param taxID
	 * @return
	 * HashMap-CopedID, ArrayList-String
	 * key CopedID
	 * value ��ͬcopedID��Ӧ�Ĳ�ͬaccID��list
	 */
	public static HashMap<CopedID, ArrayList<String>> getHashCopedID(Collection<String> collectionAccID, int taxID) {
		HashMap<CopedID, ArrayList<String>> hashResult = new HashMap<CopedID, ArrayList<String>>();
		for (String string : collectionAccID) {
			CopedID copedID = new CopedID(string, taxID, false);
			if (hashResult.containsKey(copedID) && !hashResult.get(copedID).contains(string) ) {
				hashResult.get(copedID).add(string);
			}
			else if (!hashResult.containsKey(copedID)){
				ArrayList<String> lsAccID = new ArrayList<String>();
				lsAccID.add(string);
				hashResult.put(copedID, lsAccID);
			}
		}
		return hashResult;
	}
	
/////////////////////// ˽�� static ���� /////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
			if (lsResult.size() <= 2) {
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
	 * ��ȡ���ݿ��е�taxID�������е�species��ȡ����������ΪtaxID,speciesInfo
	 * @return
	 * HashMap - key:Integer taxID
	 * value: 0: Kegg��д 1��������
	 */
	public static HashMap<Integer, String[]> getSpecies() 
	{
		TaxInfo taxInfo = new TaxInfo();
		ArrayList<TaxInfo> lsTaxID = DaoFSTaxID.queryLsTaxInfo(taxInfo);
		HashMap<Integer,String[]> hashTaxID = new HashMap<Integer, String[]>();
		for (TaxInfo taxInfo2 : lsTaxID) {
			if (taxInfo2.getAbbr() == null || taxInfo2.getAbbr().trim().equals("")) {
				continue;
			}
			
			hashTaxID.put( taxInfo2.getTaxID(),new String[]{taxInfo2.getAbbr(),taxInfo2.getLatin()});
		}
		return hashTaxID;
	}

	/////////////////////////////  ��дequals��  ////////////////////////////////////
	/**
	 * ֻҪ����ncbiid��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		CopedID otherObj = (CopedID)obj;
		
		if (
				//geneID��ͬ�Ҷ���Ϊ������������Ϊ����������ͬ
				(!copedID.genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& copedID.genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& copedID.idType.equals(otherObj.getIDtype())
				&& copedID.taxID == otherObj.getTaxID()
				)
				||//geneID��Ϊ""����ô�������accID��ͬ�Ҳ�Ϊ""��Ҳ����Ϊ����������ͬ
				(copedID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& ( !copedID.accID.equals("") && !otherObj.getAccID().equals("") )
				&& copedID.accID.equals(otherObj.getAccID())
				&& copedID.idType.equals(otherObj.getIDtype())
				&& copedID.taxID == otherObj.getTaxID()
				)
				||
				//����geneID��accID��Ϊ""Ҳ������Ϊ����������ͬ
				(copedID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& copedID.accID.equals("") 
				&& otherObj.getAccID().equals("")						
				)
		)
		{
			return true;
		}
		return false;
	}

	/**
	 * ��дhashcode
	 */
	public int hashCode(){
		String hash = "";
		if (!copedID.genUniID.trim().equals("")) {
			hash = copedID.genUniID.trim() + "sep_@@_genUni_" + copedID.idType.trim() + "@@" + copedID.taxID;
		}
		else if (copedID.genUniID.trim().equals("") && !copedID.accID.trim().equals("")) {
			hash = copedID.accID.trim()+"@@accID"+copedID.idType.trim()+"@@"+copedID.taxID;
		}
		else if ( copedID.genUniID.trim().equals("") && copedID.accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}
	
}
