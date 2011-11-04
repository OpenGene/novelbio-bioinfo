package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegGenEntryKO;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.service.AbsGetSpring;
import com.novelbio.database.service.servgeneanno.ServGeneAnno;

public abstract class CopedIDAbs implements CopedIDInt {
//	public final static String IDTYPE_ACCID = "accID"; 
//	public final static String IDTYPE_GENEID = "geneID";
//	public final static String IDTYPE_UNIID = "uniID"; 
//	
////////////  service  ///////////////////////////////////////////////////////////////////////
	static ServGeneAnno servGeneAnno = new ServGeneAnno();
///////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����id
	 */
	int taxID = 0;
	/**
	 * idType��������IDTYPE�е�һ��
	 */
	String idType = CopedID.IDTYPE_ACCID;
	
	/**
	 * �����accID
	 */
	String accID = "";

	String genUniID = "";
	
	String description = null;
	
	String symbol = null;
	
	BlastInfo blastInfo = null;
	
	/**
	 * Ʃ���Ͷ�����ֽ���blast��Ȼ������Щ���ֵ���Ϣ��ȡ����
	 */
	ArrayList<BlastInfo> lsBlastInfos = null;
	
	
	
	double evalue = 10;
	
	KegGenEntryKO kegGenEntryKO = null;
	
	AGeneInfo geneInfo = null;
//	ArrayList<AGene2Go> lsGene2Gos = null;
	
	String databaseType = "";
	
	KeggInfo keggInfo;
	
	GOInfoAbs goInfoAbs = null;
	
	
///////////////////    Blast setting   /////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * �������ֵ�blast
	 * ��ñ�copedID blast����Ӧ���ֵ�blastInfo��Ϣ��û�оͷ���null
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
			ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(blastInfoTmp);
			if (lsBlastInfos != null && lsBlastInfos.size() > 0) 
			{
				Collections.sort(lsBlastInfos);//����ѡ����С��һ��
				blastInfo = lsBlastInfos.get(0);
				this.evalue = blastInfo.getEvalue();
			}
		}
		return blastInfo;
	}
	/**
	 * �趨������ֽ���blast
	 * ÿ���趨�󶼻�ˢ��
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
			ArrayList<BlastInfo> lsBlastInfosTmp = MapBlastInfo.queryLsBlastInfo(blastInfoTmp);
			if (lsBlastInfosTmp != null && lsBlastInfosTmp.size() > 0) 
			{
				Collections.sort(lsBlastInfosTmp);//����ѡ����С��һ��
				BlastInfo blastInfo = lsBlastInfosTmp.get(0);
				lsBlastInfos.add(blastInfo);
			}
		}
	}
	
	/**
	 * �������ֵ�blast
	 * ��ñ�copedID blast����Ӧ���ֵ�copedID��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getCopedIDBlast(int StaxID,double evalue) {
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
	 * blast�������
	 * ����Ҫ�趨blast��Ŀ��
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ����һϵ�е�Ŀ�����ֵ�taxID�����CopedIDlist
	 * ���û�н��������һ���յ�lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
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
	 * blast�������
	 * ����Ҫ�趨blast��Ŀ��
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * @return
	 * ����blast����Ϣ������evalue�ȣ���list��getCopedIDLsBlast()�õ���list��һһ��Ӧ��
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}
	////////////////////   normal setting  /////////////////////////////////////////////////////////////////
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
		setSymbolDescrip();
		return description;
	}
	/**
	 * ��øû����symbol
	 * @return
	 */
	public String getSymbo() {
		setSymbolDescrip();
		return symbol;
	}
	/**
	 * �趨geneInfo��Ϣ
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
	 * ���������NCBIgeneID����databaseType����� accID.
	 * ���databaseType == null �� ���� �Ǿ����ѡһ��accID
	 * ���geneID��0������""
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
	 * 	 * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��accID��û���򷵻�null
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
	 * 	 * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��AgeneUniID��û���򷵻�null
	 * @param dbInfo
	 * @return
	 */
	protected abstract AgeneUniID getGenUniID(String genUniID, String dbInfo);

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
			CopedID copedIDBlast = getCopedIDBlast(StaxID, evalue);
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
//////////////////////////////////GOInfo  ////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �趨 goInfoAbs����Ϣ
	 */
	protected abstract void setGoInfo();
	/**
	 * ���ظû�������Ӧ��GOInfo��Ϣ��������Blast
	 * @return
	 */
	protected GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			setGoInfo();
		}
		return goInfoAbs;
	}
	/**
	 * ���ظ�CopedID����Ӧ��Gene2GOInfo
	 * @param GOType
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(String GOType) {
		return getGOInfo().getLsGene2Go(GOType);
	}
	/**
	 * 	blast�������
	 * �����趨blast������
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��GoInfo
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
	 * ��ø�CopeID��List-KGentry,���û�л�Ϊ�գ��򷵻�null
	 * @param blast �Ƿ�blast����Ӧ���ֲ鿴
	 * @param StaxID ���blastΪtrue����ô�趨StaxID
	 * @return ���û�оͷ���null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) {
		if (!blast) {
			return setKegGenEntryKO().getLsKGentries();
		}
		else {
			//������������ҵ�keggID�Ͳ�����blast
			if ( setKegGenEntryKO().getLsKGentries() != null) {
				return setKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getCopedIDBlast(StaxID,evalue);
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
	 * �����ص�Kegg��Ϣ
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
	 * ��ø�copedID��KegPath
	 */
	public ArrayList<KGpathway> getKegPath() {
		getKeggInfo();
		return keggInfo.getLsKegPath();
	}
	/**
	 * 	blast�������
	 * �����趨blast������
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��KegPath
	 */
	public ArrayList<KGpathway> getKegPathBlast() {
		getKeggInfo();
		ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
		ArrayList<CopedID> lsBlastCopedIDs = getCopedIDLsBlast();
		if (lsBlastCopedIDs != null) {
			for (CopedID copedID : lsBlastCopedIDs) {
				lskeggInfo.add(copedID.getKeggInfo());
			}
		}
		return keggInfo.getLsKegPath(lskeggInfo);
	}
	
//	/**
//	 * blast��������
//	 * ����blast����copedID���� getBlastCopedID(int StaxID,double evalue) �������
//	 * �÷����� setBlastInfo(double evalue, int... StaxID)
//	 * ��þ���blast��KegPath
//	 */
//	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID) {
//		getKeggInfo();
//		ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
//		lskeggInfo.add(copedID.getKeggInfo());
//		return keggInfo.getLsKegPath(lskeggInfo);
//	}
	///////////////////  static method //////////////////////////////////////////////////////////////////////////////////////////////
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
//		ServGeneAnno servGeneAnno = new ServGeneAnno();
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID); ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
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
	

	
	
	
	
}