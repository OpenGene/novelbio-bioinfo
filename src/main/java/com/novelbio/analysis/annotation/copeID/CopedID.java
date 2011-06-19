package com.novelbio.analysis.annotation.copeID;


import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegGenEntryKO;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.ServBlastInfo;

/**
 * ר�ŶԻ����ID��һЩ�������<br>
 * <b>��������IDtype��accID����ô�û���ܿ��ܲ����ڣ���ô����blast�������Ϣ�����blastҲû�У���ô�Ͳ�������</b><br>
 * ���Խ������ID�ϲ����������ҽ���ɢ��ID�洢��һ��Hashmap��
 * ��genUniID����ʱ�����Ƿ����ֻ�Ƚ�genUniID��taxID��idType�Ƿ����
 * ��genUniID�����ڣ�accID����ʱ���Ƚ�accID��taxID��idType�Ƿ����
 * ����������ʱ����Ϊ��ͬ
 * HashCode���趨���������
 * @author zong0jie
 *
 */
public class CopedID {
	public static String IDTYPE_ACCID = "accID"; 
	public static String IDTYPE_GENEID = "geneID"; 
	public static String IDTYPE_UNIID = "uniID"; 

	/**
	 * ����id
	 */
	int taxID = 0;
	/**
	 * idType��������IDTYPE�е�һ��
	 */
	String idType = IDTYPE_ACCID;
	
	/**
	 * �����accID
	 */
	String accID = "";

	String genUniID = "";
	
	String description = null;
	
	String symbol = null;
	
	BlastInfo blastInfo = null;
	
	KegGenEntryKO kegGenEntryKO = null;
	/**
	 * �ⲿ��Ҫͨ���÷���new
	 */
	protected CopedID() {
	}
	/**
	 * �趨��ʼֵ�����Զ�ȥ���ݿ����accID���������䱾�ࡣ
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
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		String tmpGenID = lsaccID.get(2);
		setInfo(accID, idType, taxID, tmpGenID);
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
	public CopedID(String idType, String genUniID, int taxID) {
		this.accID = "";
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
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
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUniTax(accID, taxID);
		String idType = lsaccID.get(0); taxID = Integer.parseInt(lsaccID.get(1));
		 for (int i = 2 ; i < lsaccID.size(); i++) {
			 CopedID copedID = new CopedID();
			 String tmpGenID = lsaccID.get(i);
			 copedID.setInfo(accID, idType, taxID, tmpGenID);
			 lsCopedIDs.add(copedID);
		 }
		 return lsCopedIDs;
	}
	
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�blastInfo��Ϣ��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public BlastInfo getBlastInfo(int StaxID,double evalue) {
		if (blastInfo == null) {
			String[] genInfo = new String[3];
			genInfo[0] = getIDtype();
			genInfo[1] = getAccID();
			genInfo[2] = getGenUniID();
			blastInfo = ServBlastInfo.getBlastInfo(genInfo, getTaxID(), StaxID, evalue);
		}
		return blastInfo;
	}
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�copedID��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getBlastCopedID(int StaxID,double evalue) {
		BlastInfo blastInfo = getBlastInfo(StaxID, evalue);
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
	 * �趨��ʼֵ�����Զ�ȥ���ݿ���֤geneUniID�������䱾�ࡣ
	 * ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null
	 * ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param idType ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID ����ID
	 */
	public static CopedID validCopedID(String idType, String genUniID,int taxID) {
		CopedID copedID = new CopedID();
		copedID.accID = "";
		if (idType.equals(IDTYPE_ACCID)) {
			return null;
		}
		else if (idType.equals(IDTYPE_GENEID)) {
			NCBIID ncbiid = ServAnno.getNCBIID(Integer.parseInt(genUniID), taxID);
			if (ncbiid != null) {
				copedID.genUniID = ncbiid.getGeneId() + "";
				copedID.idType = IDTYPE_GENEID;
				copedID.taxID = ncbiid.getTaxID();
				return copedID;
			}
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			UniProtID uniProtID = ServAnno.getUniProtID(genUniID, taxID);
			if (uniProtID != null) {
				copedID.genUniID = uniProtID.getUniID();
				copedID.idType = IDTYPE_UNIID;
				copedID.taxID = uniProtID.getTaxID();
				return copedID;
			}
		}
		return null;
	}
	/**
	 * �趨���ֲ����ģ�����Ҫ�ⲿ�趨
	 * @param accID
	 * @param idType
	 * @param taxID
	 * @param genUniID
	 */
	protected void setInfo(String accID,String idType, int taxID, String genUniID) {
		this.accID = accID;
		this.taxID = taxID;
		this.genUniID = genUniID;
		this.idType = idType;
	}
	
	/**
	 * idType��������IDTYPE�е�һ��
	 */
	public String getIDtype() {
		return this.idType;
	}
	
	/**
	 * �����accID
	 */
	public String getAccID() {
		return this.accID;
	}


	/**
	 * ���geneID
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
	 * ��øû����description
	 * @return
	 */
	public String getDescription() {
		setSymbDesp();
		return description;
	}
	/**
	 * ��øû����symbol
	 * @return
	 */
	public String getSymbo() {
		setSymbDesp();
		return symbol;
	}
	
	private void setSymbDesp() {
		if (description != null && symbol != null) {
			return;
		}
		if (idType.equals(IDTYPE_ACCID)) {
			description = "";
			symbol = "";
		}
		else if(idType.equals(IDTYPE_GENEID)){
			String[]  symbDesp = ServAnno.getGenInfo(Integer.parseInt(getGenUniID()));
			description = symbDesp[1];
			symbol = symbDesp[0];
		}
		else if (idType.equals(IDTYPE_UNIID)) {
			String[]  symbDesp = ServAnno.getUniGenInfo(getGenUniID());
			description = symbDesp[1];
			symbol = symbDesp[0];
		}
	}
	/**
	 * ��ø�CopeID��List-KGentry,���û�л�Ϊ�գ��򷵻�null
	 * @param blast �Ƿ�blast����Ӧ���ֲ鿴
	 * @param StaxID ���blastΪtrue����ô�趨StaxID
	 * @return ���û�оͷ���null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) {
		if (!blast) {
			return getKegGenEntryKO().getLsKGentries();
		}
		else {
			//������������ҵ�keggID�Ͳ�����blast
			if ( getKegGenEntryKO().getLsKGentries() != null) {
				return getKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getBlastCopedID(StaxID,evalue);
			return ScopedID.getKegEntity(false, 0, 0);
		}
	}
	
	private KegGenEntryKO getKegGenEntryKO()
	{
		if (kegGenEntryKO == null) {
			kegGenEntryKO = new KegGenEntryKO(idType, genUniID, taxID);
		}
		return kegGenEntryKO;
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
				(!genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("") 
				&& genUniID.trim().equals(otherObj.getGenUniID().trim())	
				&& idType.equals(otherObj.getIDtype())
				&& taxID == otherObj.getTaxID()
				)
				||//geneID��Ϊ""����ô�������accID��ͬ�Ҳ�Ϊ""��Ҳ����Ϊ����������ͬ
				(genUniID.trim().equals("")
				&& otherObj.getGenUniID().trim().equals("") 
				&& !accID.equals("") && !otherObj.getAccID().equals("")
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
	 * ���blast * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description �����blast 0:symbol 1:description
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
				tmpAnno[3] = evalue + "";
				tmpAnno[4] = copedIDBlast.getSymbo();
				tmpAnno[5] = copedIDBlast.getDescription();
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
	 * ��дhashcode
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
	
}
