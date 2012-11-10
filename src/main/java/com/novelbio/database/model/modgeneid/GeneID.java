package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniProtID;

/**
 * <b>ע��blastInfo�е�SubjectTab��QueryTab�����⣬��Ҫ��д</b><br>
 * 
 * ��ֻ��һ�������� ר�ŶԻ����ID��һЩ�������<br>
 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b><br>
 * ���Խ������ID�ϲ����������ҽ���ɢ��ID�洢��һ��Hashmap��
 * ��genUniID����ʱ�����Ƿ����ֻ�Ƚ�genUniID��taxID��idType�Ƿ����
 * ��genUniID�����ڣ�accID����ʱ���Ƚ�accID��taxID��idType�Ƿ���� ����������ʱ����Ϊ��ͬ HashCode���趨���������
 * 
 * @author zong0jie
 */
public class GeneID implements GeneIDInt{
	public final static String IDTYPE_ACCID = "accID"; 
	public final static String IDTYPE_GENEID = "NCBIID";
	public final static String IDTYPE_UNIID = "UniprotID";
	
	private GeneIDabs geneID;
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param idType  ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID ����ID
	 */
	public GeneID(String idType, String genUniID, int taxID) {
		genUniID = genUniID.trim();
		if (genUniID.equals("")) {
			genUniID = null;
		}
		if (idType.equals(IDTYPE_UNIID)) {
			geneID = new GeneIDUni(null, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(null, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(null, genUniID, taxID);
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
	public GeneID(String accID,String idType, String genUniID, int taxID) {
		if (accID != null) {
			accID = accID.replace("\"", "").trim();
			if (accID.equals("")) {
				accID = null;
			}
		}
		if (idType.equals(IDTYPE_UNIID)) {
			geneID = new GeneIDUni(accID, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(accID, genUniID, taxID);
		}
		else if (idType.equals(IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(accID, genUniID, taxID);
		}
	}
	
	
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID���������䱾�ࡣ
	 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b>
	 * ����ֻ�ܲ���һ��CopedID���������һ��accIDҪ�������geneID����ô����ѡ��getLsCopedID����
	 * @param accID ��ȥ���ţ�Ȼ���������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 */
	public GeneID(String accID,int taxID) {
		this(accID,taxID, false);
	}
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID���������䱾�ࡣ
	 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b>
	 * ����ֻ�ܲ���һ��CopedID���������һ��accIDҪ�������geneID����ô����ѡ��getLsCopedID����
	 * @param accID ��ȥ���ţ�Ȼ���������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 * @param blastType �����accID�Ƿ����� blast�Ľ�����磺dbj|AK240418.1|����ô���AK240418��һ�㶼��false
	 */
	public GeneID(String accID,int taxID,boolean blastType) {
		if (blastType) {
			accID = getBlastAccID(accID);
		} else {
			accID = removeDot(accID);
		}
		ArrayList<AgeneUniID> lsaccID = GeneIDabs.getNCBIUniTax(accID, taxID);
		if (lsaccID.size() == 0) {
			geneID = new GeneIDAccID(accID, "0", taxID);
			return;
		}
		AgeneUniID geneUniID = lsaccID.get(0);
		if (geneUniID.getGeneIDtype().equals(IDTYPE_UNIID)) {
			geneID = new GeneIDUni(accID, geneUniID.getGenUniID(), taxID);
		}
		else if (geneUniID.getGeneIDtype().equals(IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(accID, geneUniID.getGenUniID(), taxID);
		}
	}
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID�������䱾�ࡣ
	 * @param accID �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 */
	public static ArrayList<GeneID> createLsCopedID(String accID,int taxID) {
		return createLsCopedID(accID, taxID, false);
	}
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID�������䱾�ࡣ
	 * @param accID �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param taxID
	 * @param blastType �����accID�Ƿ����� blast�Ľ�����磺dbj|AK240418.1|����ô���AK240418��һ�㶼��false
	 */
	public static ArrayList<GeneID> createLsCopedID(String accID,int taxID,boolean blastType) {
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
		if (blastType) 
			accID = accID.split("\\|")[1];
		accID = removeDot(accID);
		
		ArrayList<AgeneUniID> lsaccID = GeneIDabs.getNCBIUniTax(accID, taxID);
		for (AgeneUniID ageneUniID : lsaccID) {
			 GeneID copedID = new GeneID(accID, ageneUniID.getGeneIDtype(), ageneUniID.getGenUniID(), taxID);
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
	@Deprecated
	public static GeneID validCopedID(String idType, String genUniID,int taxID) {
		ServNCBIID servNCBIID = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();
		GeneID copedID = null;
		if (idType.equals(IDTYPE_ACCID)) {
			return null;
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			NCBIID ncbiid = servNCBIID.queryNCBIID(Integer.parseInt(genUniID), taxID);
			if (ncbiid != null) {
				String genUniID2 = ncbiid.getGeneId() + "";
				int taxID2 = ncbiid.getTaxID();
				copedID = new GeneID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			UniProtID uniProtID = servUniProtID.getUniProtID(genUniID, taxID);
			if (uniProtID != null) {
				String genUniID2 = uniProtID.getUniID();
				int taxID2 = uniProtID.getTaxID();
				copedID = new GeneID(idType, genUniID2, taxID2);
				return copedID;
			}
		}
		return null;
	}
	
	//////////////////  Blast setting  ///////////////////////////////////////////
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		geneID.setBlastInfo(evalue,StaxID);
	}
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return geneID.getLsBlastInfos();
	}
///////////////////////   ���blast  copedID  ///////////////////////////////////////////////////////////////////
	@Override
	public ArrayList<GeneID> getLsBlastGeneID() {
		return geneID.getLsBlastGeneID();
	}
	@Override
	public GeneID getGeneIDBlast() {
		return geneID.getGeneIDBlast();
	}
/////////////////////////  ������Ϣ  //////////////////////////////////////////////////////////////
	@Override
	public String getIDtype() {
		return geneID.getIDtype();
	}
	@Override
	public String getAccID() {
		return geneID.getAccID();
	}
	@Override
	public String getAccIDDBinfo() {
		return geneID.getAccIDDBinfo();
	}
	@Override
	public String getGenUniID() {
		return geneID.getGenUniID();
	}
	@Override
	public int getTaxID() {
		return geneID.getTaxID();
	}
	@Override
	public String getDescription() {
		return geneID.getDescription();
	}
	@Override
	public String getSymbol() {
		return geneID.getSymbol();
	}
	@Override
	public String getAccIDDBinfo(String dbInfo) {
		return geneID.getAccIDDBinfo(dbInfo);
	}
	/**
	 * ����geneinfo��Ϣ
	 * @return
	 */
	@Override
	public AGeneInfo getGeneInfo() {
		return geneID.getGeneInfo();
	}
	/**
	 * ��ø�copedID��annotation��Ϣ
	 * @param geneID
	 * @param blast
	 * @return
	 * 	 * blast��<br>
	 * 			blast * 0:symbol 1:description 2:subjectSpecies 3:evalue 4:symbol 5:description <br>
			��blast��<br>
						0:symbol 1:description<br>
	 */
	@Override
	public String[] getAnno(boolean blast) {
		return geneID.getAnno(blast);
	}
	public static String[] getTitleAnno(boolean blast) {
		String[] titleAnno = null;
		if (blast) {
			titleAnno = new String[6];
		}
		else {
			titleAnno = new String[2];
		}
		titleAnno[0] = "Symbol";
		titleAnno[1] = "Description";
		if (blast) {
			titleAnno[2] = "BLast_Species";
			titleAnno[3] = "Evalue";
			titleAnno[4] = "Blast_Symbol";
			titleAnno[5] = "Blast_Description";
		}
		return titleAnno;
	}
/////////////////////////////////////////////////////////////////////

////////////////////   KEGG    /////////////////////////////////////////////////////////
	@Override
	public KeggInfo getKeggInfo() {
		return geneID.getKeggInfo();
	}
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		return geneID.getKegPath(blast);
	}
	@Override
	public ArrayList<KGentry> getKegEntity(boolean blast) {
		return geneID.getKegEntity(blast);
	}
	//////////////  GO ����  ///////////////////////
	@Override
	public ArrayList<AGene2Go> getGene2GO(String GOType) {
		return geneID.getGene2GO(GOType);
 	}
	protected GOInfoAbs getGOInfo() {
		return geneID.getGOInfo();
	}
	@Override
	public ArrayList<AGene2Go> getGene2GOBlast(String GOType) {
		return geneID.getGene2GOBlast(GOType);
	}
	/////////////////////////////  static ����  ////////////////////////////////////
	/**
	 * blast�Ľ����������dbj|AK240418.1|
	 * �������AK240418ץ����������
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		if (blastGenID == null) {
			return null;
		}
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}
	/**
	 *  ���ȳ�ȥ�ո����Ϊ""��-��
	 *  �򷵻�null
	 * �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID) {
		if (accID == null) {
			return null;
		}
		String tmpGeneID = accID.replace("\"", "").trim();
		if (tmpGeneID.equals("") || accID.equals("-")) {
			return null;
		}
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//�������XM_002121.1����
		if (dotIndex>0 && tmpGeneID.length() - dotIndex <= 3) {
			String subIndex = tmpGeneID.substring(dotIndex + 1, tmpGeneID.length());
			try {
				int num = Integer.parseInt(subIndex);
				tmpGeneID = tmpGeneID.substring(0,dotIndex);
			} catch (Exception e) { }
		}
		return tmpGeneID;
	}
	/**
	 * @param collectionAccID �����accIDlist�������ȶ�accIDȥһ���ظ�
	 * @param taxID
	 * @param combineID
	 * @return
	 */
	public static ArrayList<GeneID> getLsGeneID(Collection<String> collectionAccID, int taxID, boolean combineID) {
		ArrayList<GeneID> lsGeneID = new ArrayList<GeneID>();
		for (String string : collectionAccID) {
			GeneID copedID = new GeneID(string, taxID, false);
			lsGeneID.add(copedID);
		}
		if (!combineID) {
			return lsGeneID;
		}
		HashSet<GeneID> setUniqueGeneID = ArrayOperate.removeDuplicate(lsGeneID);
		lsGeneID.clear();
		for (GeneID geneID : setUniqueGeneID) {
			lsGeneID.add(geneID);
		}
		return lsGeneID;
	}
	
	/**
	 * ����һ��geneID��Ӧ���accID�ı�
	 * @param collectionAccID �����accIDlist�������ȶ�accIDȥһ���ظ�
	 * @param taxID
	 * @return
	 * HashMap-geneID, ArrayList-String
	 * key geneID
	 * value ��ͬcopedID��Ӧ�Ĳ�ͬaccID��list
	 */
	public static HashMap<GeneID, ArrayList<String>> getMapGeneID2LsAccID(Collection<String> collectionAccID, int taxID) {
		HashMap<GeneID, ArrayList<String>> hashResult = new HashMap<GeneID, ArrayList<String>>();
		for (String string : collectionAccID) {
			GeneID copedID = new GeneID(string, taxID, false);
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
	
////////   ���� GO �Ĵ���   /////////////////////////////////////
	/**
	 * ��һϵ��CopedID�е�GO����� genUniID goID,goID,goID.....����ʽ
	 * �ڲ�����genUniIDȥ�ظ�
	 * @param lsGeneID һϵ�е�copedID
	 * @param GOType GOInfoAbs�е���Ϣ
	 * @param blast ע��lsCopedID�����copedID����Ҫ���趨��setBlast������
	 * @reture û���򷵻�null
	 */
	public static ArrayList<String[]> getLsGoInfo(ArrayList<GeneID> lsGeneID, String GOType, boolean blast) {
		if (validateListIsEmpty(lsGeneID)) return null;
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashSet<GeneID> setUniqueGeneID = ArrayOperate.removeDuplicate(lsGeneID);
		for (GeneID geneID : setUniqueGeneID) {
			////////////////////////////////////////////////////////////
			ArrayList<AGene2Go> lstmpgo;
			if (blast) {
				lstmpgo = geneID.getGene2GOBlast(GOType);
			}
			else {
				lstmpgo = geneID.getGene2GO(GOType);
			}
			if (validateListIsEmpty(lstmpgo)) continue;
			
			String[] strGene2Go = new String[2];
			strGene2Go[0] = geneID.getGenUniID();
			
			for (AGene2Go aGene2Go : lstmpgo) {
				if (strGene2Go[1].equals("")) {
					strGene2Go[1] = aGene2Go.getGOID();
				}
				else {
					strGene2Go[1] = strGene2Go[1] + ","+aGene2Go.getGOID();
				}
			}
			lsResult.add(strGene2Go);
		}
		return lsResult;
	}
////////���� KEGG �Ĵ���   /////////////////////////////////////
	/**
	 * ��һϵ��CopedID�е�KEGG����� genUniID PathID,pathID,pathID.....����ʽ
	 * �ڲ�����genUniIDȥ�ظ�
	 * @param lsGeneID һϵ�е�copedID
	 * @param GoType GOInfoAbs�е���Ϣ
	 * @param blast ע��lsCopedID�����copedID����Ҫ���趨��setBlast������
	 * @reture û���򷵻�null
	 */
	public static ArrayList<String[]> getLsPathInfo(ArrayList<GeneID> lsGeneID, boolean blast) {
		if (validateListIsEmpty(lsGeneID)) return null;
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		LinkedHashSet<GeneID> hashGenID = ArrayOperate.removeDuplicate(lsGeneID);
		for (GeneID geneID : hashGenID) {
			ArrayList<KGpathway> lstmpgo = geneID.getKegPath(blast);
			if (validateListIsEmpty(lstmpgo))  continue;
			
			String[] strGene2Path = new String[2];
			strGene2Path[0] = geneID.getGenUniID();
			strGene2Path[1] = lstmpgo.get(0).getPathName();
			for (int i = 1; i < lstmpgo.size(); i++) {
				strGene2Path[1] = strGene2Path[1] + ","+lstmpgo.get(i).getPathName();
			}
			lsResult.add(strGene2Path);
		}
		return lsResult;
	}
	/**
	 * GO��ӦGeneOntology��hash��
	 * @return
	 * HashMap - key:GO��д 
	 * value: 0: GOȫ��
	 */
	public static HashMap<String, String> getMapGOAbbr2GOID() {
		HashMap<String, String> hashGOInfo = new HashMap<String, String>();
		hashGOInfo.put(Go2Term.GO_BP, Go2Term.GO_BP);
		hashGOInfo.put(Go2Term.GO_CC, Go2Term.GO_CC);
		hashGOInfo.put(Go2Term.GO_MF, Go2Term.GO_MF);
		return hashGOInfo;
	}
/////////////////////// ˽�� static ���� /////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** �鿴��list�Ƿ������� */
	private static boolean validateListIsEmpty(Collection col) {
		if (col == null || col.size() == 0)
			return true;
		return false;
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
		GeneID otherObj = (GeneID)obj;
		if (
				//geneID��ͬ�Ҷ���Ϊ������������Ϊ����������ͬ
				(!geneID.genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& geneID.genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& geneID.idType.equals(otherObj.getIDtype())
				&& geneID.taxID == otherObj.getTaxID()
				)
				||//geneID��Ϊ""����ô�������accID��ͬ�Ҳ�Ϊ""��Ҳ����Ϊ����������ͬ
				(geneID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& ( !geneID.accID.equals("") && !otherObj.getAccID().equals("") )
				&& geneID.accID.equals(otherObj.getAccID())
				&& geneID.idType.equals(otherObj.getIDtype())
				&& geneID.taxID == otherObj.getTaxID()
				)
				||
				//����geneID��accID��Ϊ""Ҳ������Ϊ����������ͬ
				(geneID.genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& geneID.accID.equals("") 
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
		if (geneID.genUniID != null && !geneID.genUniID.trim().equals("")) {
			hash = geneID.genUniID.trim() + "sep_@@_genUni_" + geneID.idType.trim() + "@@" + geneID.taxID;
		}
		else if ( geneID.genUniID.trim().equals("") && !geneID.accID.trim().equals("")) {
			hash = geneID.accID.trim()+"@@accID"+geneID.idType.trim()+"@@"+geneID.taxID;
		}
		else if ( geneID.genUniID.trim().equals("") && geneID.accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}

	@Override
	public void setUpdateGeneID(String geneUniID, String idType) {
		geneID.setUpdateGeneID(geneUniID, idType);
	}

	@Override
	public void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		geneID.setUpdateGO(GOID, GOdatabase, GOevidence, GORef, gOQualifiy);
	}

	@Override
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		geneID.setUpdateGeneInfo(geneInfo);
	}

	@Override
	public boolean update(boolean updateUniID) {
		return geneID.update(updateUniID);
	}

	@Override
	public void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo) {
		geneID.setUpdateDBinfo(DBInfo, overlapDBinfo);
	}

	@Override
	public void setUpdateRefAccID(String... refAccID) {
		geneID.setUpdateRefAccID(refAccID);
	}
	@Override
	public void addUpdateRefAccID(String... refAccID) {
		geneID.addUpdateRefAccID(refAccID);
	}
	@Override
	public void setUpdateRefAccID(ArrayList<String> lsRefAccID) {
		geneID.setUpdateRefAccID(lsRefAccID);
	}
	@Override
	public void setUpdateBlastInfo(String SubAccID, String subDBInfo,
			int SubTaxID, double evalue, double identities) {
		geneID.setUpdateBlastInfo(SubAccID, subDBInfo, SubTaxID, evalue, identities);
	}

	@Override
	public void setUpdateAccID(String accID) {
		geneID.setUpdateAccID(accID);
	}

	@Override
	public void setUpdateRefAccIDClear(Boolean uniqID) {
		geneID.setUpdateRefAccIDClear(uniqID);
	}

	@Override
	public void setUpdateBlastInfo(String SubGenUniID, String subIDtype,
			String subDBInfo, int SubTaxID, double evalue, double identities) {
		geneID.setUpdateBlastInfo(SubGenUniID, subIDtype, subDBInfo, SubTaxID, evalue, identities);
	}

	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		geneID.setUpdateAccIDNoCoped(accID);
	}

	@Override
	public String getDBinfo() {
		return geneID.getDBinfo();
	}
	
}
