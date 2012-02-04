package com.novelbio.database.model.modcopeid;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.log4j.Logger;

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
	private static Logger logger = Logger.getLogger(CopedIDAbs.class);
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
	String accID = null;

	String genUniID = null;

	String symbol = null;

	// BlastInfo blastInfo = null;

	/**
	 * Ʃ���Ͷ�����ֽ���blast��Ȼ������Щ���ֵ���Ϣ��ȡ����
	 */
	ArrayList<BlastInfo> lsBlastInfos = null;

	double evalue = 10;

	KegGenEntryKO kegGenEntryKO = null;

	AGeneInfo geneInfo = null;
	// ArrayList<AGene2Go> lsGene2Gos = null;

	static HashMap<Integer, String> hashDBtype = new HashMap<Integer, String>();

	private String getDatabaseTyep() {
		if (hashDBtype.size() == 0) {
			hashDBtype.put(39947, NovelBioConst.DBINFO_RICE_TIGR);
			hashDBtype.put(10090, NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
			hashDBtype.put(3702, NovelBioConst.DBINFO_ATH_TAIR);
			hashDBtype.put(3847, NovelBioConst.DBINFO_GLYMAX_SOYBASE);
			hashDBtype.put(4102, NovelBioConst.DBINFO_PLANTGDB_ACC);
		}
		String result = hashDBtype.get(taxID);
		if (result == null) {
			return NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
		}
		return result;
	}

	KeggInfo keggInfo;

	GOInfoAbs goInfoAbs = null;

	// //////////////////// service ��
	// ////////////////////////////////////////////////////
	ServBlastInfo servBlastInfo = new ServBlastInfo();
	ServNCBIID servNCBIID = new ServNCBIID();
	ServUniProtID servUniProtID = new ServUniProtID();
	ServGeneInfo servGeneInfo = new ServGeneInfo();
	ServUniGeneInfo servUniGeneInfo = new ServUniGeneInfo();
	ServGene2Go servGene2Go = new ServGene2Go();
	ServUniGene2Go servUniGene2Go = new ServUniGene2Go();
	// ///////////////////////////////////////////////////////////////////////////////////////////

	// ///////////////// Blast setting
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �趨������ֽ���blast ÿ���趨�󶼻�ˢ��
	 * 
	 * @param evalue
	 * @param StaxID
	 */
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(genUniID,taxID, i,
					evalue);
			if (blastInfo != null) {
				lsBlastInfos.add(blastInfo);
			}
		}
	}

	/**
	 * �������ֵ�blast ��ñ�copedID blast����Ӧ���ֵĵ�һ��copedID��û�оͷ���null
	 * 
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
	 * ����趨�ĵ�һ��blast�Ķ�������Ҫ�趨blast��Ŀ��
	 * 
	 * @param blastInfo
	 * @return
	 */
	private CopedID getBlastCopedID(BlastInfo blastInfo) {
		if (blastInfo == null) {
			return null;
		}
		String idType = blastInfo.getSubjectTab();
		CopedID copedID = new CopedID(idType, blastInfo.getSubjectID(),
				blastInfo.getSubjectTax());
		return copedID;
	}

	/**
	 * blast������� ����Ҫ�趨blast��Ŀ�� �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ����һϵ�е�Ŀ�����ֵ�taxID�����CopedIDlist ���û�н��������һ���յ�lsResult
	 * 
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
	 * blast������� ����Ҫ�趨blast��Ŀ�� �÷����� setBlastInfo(double evalue, int... StaxID)
	 * 
	 * @return ����blast����Ϣ������evalue�ȣ���list��getCopedIDLsBlast()�õ���list��һһ��Ӧ��
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}

	// ////////////////// normal setting
	// /////////////////////////////////////////////////////////////////
	/**
	 * idType��������IDTYPE�е�һ��
	 * �������趨��lsRefAccID�󣬿��Ը��ݾ����lsRefAccIDȥ�������ݿⲢȷ��idtype
	 */
	public String getIDtype() {
		if (lsRefAccID == null || lsRefAccID.size() == 0) {
			return this.idType;
		}
		ArrayList<String> lsIDtype = getUpdateGenUniID();
		return lsIDtype.get(0);
	}

	/**
	 * �����accID
	 */
	public String getAccID() {
		return this.accID;
	}

	/**
	 * ���geneID
	 * 
	 * @return
	 */
	public String getGenUniID() {
		return this.genUniID;
	}

	public int getTaxID() {
		if (taxID <= 0) {
			taxID = Integer.parseInt(getNCBIUniTax(accID, 0).get(1));
		}
		return taxID;
	}

	/**
	 * ��øû����description
	 * 
	 * @return
	 */
	public String getDescription() {
		setSymbolDescrip();
		if (geneInfo == null || geneInfo.getDescrp() == null) {
			return "";
		}
		return geneInfo.getDescrp().replaceAll("\"", "");
	}

	/**
	 * ��øû����symbol
	 * 
	 * @return
	 */
	public String getSymbol() {
		setSymbolDescrip();
		return symbol;
	}

	/**
	 * �趨geneInfo��Ϣ
	 */
	protected abstract void setGenInfo();

	protected void setSymbolDescrip() {
		if (geneInfo != null || symbol != null) {
			return;
		}
		if (idType.equals(CopedID.IDTYPE_ACCID)) {
			symbol = "";
		}
		
		setGenInfo();
		if (geneInfo == null) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		} else {
			geneInfo.setTaxID(taxID);
			symbol = geneInfo.getSymb();
		}
		if (symbol.equals("")) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		}
	}

	/**
	 * ���������NCBIgeneID����databaseType����� accID. ���databaseType == null �� ����
	 * �Ǿ����ѡһ��accID ���geneID��0������""
	 * 
	 * @return
	 */
	protected String getGenName(String genUniID,String databaseType) {
		AgeneUniID ageneUniID = getGenUniID(genUniID, databaseType);
		if (ageneUniID == null) {
			return "";
		} else {
			return ageneUniID.getAccID();
		}
	}

	/**
	 * * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��accID��û���򷵻�null
	 * 
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
	 * * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��AgeneUniID��û���򷵻�null
	 * 
	 * @param dbInfo
	 * @return
	 */
	protected abstract AgeneUniID getGenUniID(String genUniID, String dbInfo);

	/**
	 * ���趨blast����� ���blast * 0:symbol 1:description 2:evalue 3:subjectSpecies
	 * 4:symbol 5:description �����blast 0:symbol 1:description
	 * 
	 * @return
	 */
	public String[] getAnno(boolean blast) {
		String[] tmpAnno = null;
		if (blast) {
			tmpAnno = new String[6];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbol();
			tmpAnno[1] = getDescription();
			if (getCopedIDLsBlast() != null && getLsBlastInfos() != null
					&& getLsBlastInfos().size() > 0) {
				for (int i = 0; i < getLsBlastInfos().size(); i++) {
					if (tmpAnno[2] == null || tmpAnno[2].trim().equals("")) {
						tmpAnno[2] = CopedID.getHashTaxIDName().get(
								getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = getLsBlastInfos().get(i).getEvalue() + "";
						tmpAnno[4] = getCopedIDLsBlast().get(i).getSymbol();
						tmpAnno[5] = getCopedIDLsBlast().get(i)
								.getDescription();
					} else {
						tmpAnno[2] = tmpAnno[2]
								+ "//"
								+ CopedID.getHashTaxIDName().get(
										getLsBlastInfos().get(i)
												.getSubjectTax());
						tmpAnno[3] = tmpAnno[3] + "//"
								+ getLsBlastInfos().get(i).getEvalue();
						tmpAnno[4] = tmpAnno[4] + "//"
								+ getCopedIDLsBlast().get(i).getSymbol();
						tmpAnno[5] = tmpAnno[5] + "//"
								+ getCopedIDLsBlast().get(i).getDescription();
					}
				}
			}
		} else {
			tmpAnno = new String[2];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbol();
			tmpAnno[1] = getDescription();
		}
		return tmpAnno;
	}

	// ////////////////////////////////GOInfo
	// ////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �趨 goInfoAbs����Ϣ
	 */
	protected abstract void setGoInfo();
	/**
	 * ����geneinfo��Ϣ
	 * @return
	 */
	public AGeneInfo getGeneInfo() {
		return geneInfo;
	}
	/**
	 * ���ظû�������Ӧ��GOInfo��Ϣ��������Blast
	 * 
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
	 * 
	 * @param GOType
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(String GOType) {
		return getGOInfo().getLsGene2Go(GOType);
	}

	/**
	 * blast������� �����趨blast������ �÷����� setBlastInfo(double evalue, int... StaxID)
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

	// ////////////////KEGG //////////////////////////////////////////////
	/**
	 * ��ø�CopeID��List-KGentry,���û�л�Ϊ�գ��򷵻�null
	 * 
	 * @param blast
	 *            �Ƿ�blast����Ӧ���ֲ鿴
	 * @param StaxID
	 *            ���blastΪtrue����ô�趨StaxID
	 * @return ���û�оͷ���null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast) {
		if (!blast) {
			return setKegGenEntryKO().getLsKGentries();
		} else {
			// ������������ҵ�keggID�Ͳ�����blast
			if (setKegGenEntryKO().getLsKGentries() != null) {
				return setKegGenEntryKO().getLsKGentries();
			}
			CopedID ScopedID = getCopedIDBlast();
			return ScopedID.getKegEntity(false);
		}
	}

	private KegGenEntryKO setKegGenEntryKO() {
		if (kegGenEntryKO == null) {
			kegGenEntryKO = new KegGenEntryKO(idType, genUniID, taxID);
		}
		return kegGenEntryKO;
	}

	/**
	 * �����ص�Kegg��Ϣ
	 * 
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
	 * blast������� �����趨blast������ �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��KegPath
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
		} else {
			return keggInfo.getLsKegPath();
		}

	}

	// ///////////////// update method
	// //////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	ArrayList<String> lsRefAccID = new ArrayList<String>();
	Boolean uniqID = null;
	/**
	 * �ڲ���refaccID��Ϊ���ս�������ID��ʱ���Ƿ������uniqID
	 * uniqID���ø����Ĳο�ID���ҵ����ݿ��е�Ψһ����
	 * true��ֻ�е�uniqIDʱ������
	 * null��Ĭ�ϲ���--��uniqIDҲ����������ֻ������һ������
	 * false����uniqIDҲ������������������ȫ��ID���ù�����δʵ��
	 * @param uniqID
	 */
	@Override
	public void setUpdateRefAccID(Boolean uniqID) {
		this.uniqID = uniqID;
	}
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	@Override
	public void setUpdateRefAccID(String... refAccID) {
		lsRefAccID.clear();
		for (String string : refAccID) {
			String tmpRefID = CopedID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			lsRefAccID.add(tmpRefID);
		}
	}
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	@Override
	public void setUpdateRefAccID(ArrayList<String> lsRefAccID) {
		this.lsRefAccID.clear();
		for (String string : lsRefAccID) {
			String tmpRefID = CopedID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			this.lsRefAccID.add(tmpRefID);
		}
	}
	boolean overrideDBinfo = false;
	String databaseType = "";

	/**
	 * ��¼��ID������ID�����ݿ���Ϣ������������ǰ�����ݿ�
	 * 
	 * @param taxID
	 * @param DBInfo
	 * @param �Ƿ��ñ�DBInfo������ǰ��DBInfo
	 * ������true����false��geneinfo���������������
	 */
	@Override
	public void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo) {
		if (!DBInfo.trim().equals("")) {
			this.databaseType = DBInfo;
		}
		this.overrideDBinfo = overlapDBinfo;
	}

	/**
	 * ������֪��geneUniID��IDtype
	 * 
	 * @param geneUniID
	 * @param idType
	 *            ������CopedID.IDTYPE_GENEID�ȣ����Բ�����
	 */
	@Override
	public void setUpdateGeneID(String geneUniID, String idType) {
		if (geneUniID != null && !genUniID.trim().equals("")
				&& !genUniID.trim().equals("0")) {
			this.genUniID = geneUniID;
		}
		if (idType == null || idType.trim().equals("")) {
			return;
		}
		this.idType = idType;
	}
	/**
	 * �趨��ID��accID
	 */
	@Override
	public void setUpdateAccID(String accID) {
		this.accID = CopedID.removeDot(accID);
	}
	
	
	/**
	 * ��¼������GO��Ϣ�ģ�ÿ��������Ϻ����
	 */
	ArrayList<Gene2Go> lsGOInfoUpdate = new ArrayList<Gene2Go>();

	/**
	 * ����������Ҫ������GO��Ϣ��������� ����ֻ���Ȼ�ȡGO����Ϣ������������method��ʱ��������
	 * �����������ϵ����
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef
	 * @param gOQualifiy
	 */
	@Override
	public void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(GOID);
		gene2Go.setTaxID(taxID);
		gene2Go.setEvidence(GOevidence);
		gene2Go.setDataBase(GOdatabase);
		gene2Go.setQualifier(gOQualifiy);
		gene2Go.setReference(GORef);
		lsGOInfoUpdate.add(gene2Go);
	}

	/**
	 * ������Ҫupdate��geneInfo��ע�ⲻ��Ҫ�趨geneUniID
	 * ������Ҫ�趨
	 * @param geneInfo
	 */
	@Override
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		this.geneInfo = geneInfo;
	}

	// ר����������
	ArrayList<BlastInfo> lsBlastInfosUpdate = null;
	
	/**
	 * 
	 * ���û��QueryID, SubjectID, taxID�е��κ�һ��Ͳ����� ���evalue>50 �� evalue<0���Ͳ�����
	 * �����������ϵ����
	 * @param blastInfo
	 */
	@Override
	public void setUpdateBlastInfo(String SubAccID, String subDBInfo, int SubTaxID, double evalue, double identities) {
		if (genUniID == null || genUniID.equals("")) {
			return;
		}
		BlastInfo blastInfo = new BlastInfo(null, 0, SubAccID, SubTaxID);
		if (blastInfo.getSubjectTab().equals(CopedID.IDTYPE_ACCID)) {
			logger.error("û�и�blast��accID��"+SubAccID);
		}
		blastInfo.setQueryID(genUniID);
		blastInfo.setQueryTax(getTaxID());
		blastInfo.setEvalue_Identity(evalue, identities);
		blastInfo.setQueryDB_SubDB(this.databaseType, subDBInfo);
		if (lsBlastInfosUpdate == null) {
			lsBlastInfosUpdate = new ArrayList<BlastInfo>();
		}
		lsBlastInfosUpdate.add(blastInfo);
	}

	/**
	 * ����µ�ID������UniID����ô��д��ָ�����ļ��� �ļ���Ҫ�ʼ��setָ��
	 * 
	 * @param updateUniID
	 */
	@Override
	public boolean update(boolean updateUniID) {
		boolean flag1 = updateGeneID(updateUniID);
		boolean flag2 = updateGeneInfo();
		boolean flag3 = updateGene2Go();
		boolean flag4 = updateBlastInfo();
		return flag1&&flag2&&flag3&&flag4;
	}

	/**
	 * ����GO���ݿ�
	 */
	private boolean updateGene2Go() {
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		for (Gene2Go gene2Go : lsGOInfoUpdate) {
			if (idType.equals(CopedID.IDTYPE_GENEID)) {
				servGene2Go.updateGene2Go(genUniID, taxID, gene2Go);
			} else if (idType.equals(CopedID.IDTYPE_UNIID)) {
				servUniGene2Go.updateUniGene2Go(genUniID, taxID, gene2Go);
			}
			else {
				return false;
			}
		}
		return true;
	}

	/**
	 * ����ʧ�ܵ�IDд�뱾��
	 */
	static String txtUpdateFailFile = "";

	/**
	 * ����geneID���ݿ⣬���ҽ�geneUniID�������ݿ�������� <b>ֻ������һ����õ�geneID</b>
	 * ���accIDû�У�������
	 * @param ��������ݿ���û���ҵ���Ӧ��ID
	 *            ���Ƿ�ID����UniID��
	 *            true,����uniID�⣬��������idtype
	 * @throws EOFException
	 */
	private boolean updateGeneID(boolean updateUniID) {
		if (genUniID == null && accID == null) {
			return false;
		}
		//˵������Ҫ����accID
		if (accID == null || accID.equals("")) {
			return true;
		}
		if (databaseType == null || databaseType.equals("")) {
			logger.error("����geneIDʱû�����ø�gene�����ݿ���Դ���Զ�����ΪNCBIID");
			databaseType = NovelBioConst.DBINFO_NCBI_ACC_GENEAC;
		}
		ArrayList<String> lsGenID = getUpdateGenUniID();
		// ֻ������һ����õ�geneID
		if (lsGenID != null && !lsGenID.get(0).equals(CopedID.IDTYPE_ACCID)) {
			this.idType = lsGenID.get(0);
			//refAccID���ܻ�鵽������ֹһ��ID����ͬ��������ò�ͬ�ķ�������
			if (uniqID == null) {
				
			}
			else if ( lsGenID.size() > 3) {
				if (uniqID) {
					logger.error("�鵽�˳���һ��ID! ����accID��" + getAccID() + "  RefID��" + lsRefAccID.get(0));
					return false;
				}
				else if (!uniqID) {
					logger.error("�ù�����δʵ��");
					return false;
				}
			}
			
			if (lsGenID.get(0).equals(CopedID.IDTYPE_GENEID)) {
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(accID);
				ncbiid.setDBInfo(this.databaseType);
				ncbiid.setGenUniID(lsGenID.get(2));
				ncbiid.setTaxID(Integer.parseInt(lsGenID.get(1)));
				servNCBIID.updateNCBIID(ncbiid, overrideDBinfo);
			} else if (lsGenID.get(0).equals(CopedID.IDTYPE_UNIID)) {
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(accID);
				uniProtID.setDBInfo(this.databaseType);
				uniProtID.setGenUniID(lsGenID.get(2));
				uniProtID.setTaxID(Integer.parseInt(lsGenID.get(1)));
				servUniProtID.updateUniProtID(uniProtID, overrideDBinfo);
			}
		} else if (updateUniID) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setDBInfo(this.databaseType);
			uniProtID.setGenUniID(genUniID);
			uniProtID.setTaxID(taxID);
			servUniProtID.updateUniProtID(uniProtID, overrideDBinfo);
			// ����geneUniID
			idType = CopedID.IDTYPE_UNIID;
			genUniID = accID;
		} else {
			return false;
		}
		return true;
	}

	/**
	 * ����geneID��idType������ص�geneInfo
	 * ע�⣬geneInfoֻ���ǵ����������Ǻϲ�����geneInfo
	 * ���û��genUniID������û����������Ӧ��genID���򷵻�false��
	 * ���û��geneInfo��Ϣ������Ϊ����Ҫ����������true
	 */
	private boolean updateGeneInfo() {
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		if (geneInfo == null) {
			return true;
		}
		geneInfo.setTaxID(taxID);
		if (idType.equals(CopedID.IDTYPE_UNIID)) {
			servUniGeneInfo.updateUniGenInfo(genUniID, taxID, geneInfo);
			updateUniGeneInfoSymbolAndSynonyms(geneInfo);
		} else if (idType.equals(CopedID.IDTYPE_GENEID)) {
			servGeneInfo.updateGenInfo(genUniID, taxID, geneInfo);
			updateGeneInfoSymbolAndSynonyms(geneInfo);
		}
		else {
			return false;
		}
		geneInfo = null;
		return true;
	}
	
	private void updateGeneInfoSymbolAndSynonyms(AGeneInfo geneInfo2)
	{
		NCBIID ncbiid = null;
		if (geneInfo2.getSep() != null && !geneInfo2.equals("")) {
			if (geneInfo2.getSymb() != null) {
				String[] ssymb = geneInfo2.getSymb().split(geneInfo2.getSep());
				for (String string : ssymb) {
					ncbiid = new NCBIID();
					ncbiid.setAccID(string.trim());
					ncbiid.setDBInfo(NovelBioConst.DBINFO_SYMBOL);
					ncbiid.setGenUniID(genUniID);
					ncbiid.setTaxID(taxID);
					servNCBIID.updateNCBIID(ncbiid, true);
				}
			}
			if (geneInfo2.getSynonym() != null) {
				String[] ssynonym = geneInfo2.getSynonym().split(geneInfo2.getSep());
				for (String string : ssynonym) {
					ncbiid = new NCBIID();
					ncbiid.setAccID(string.trim());
					ncbiid.setDBInfo(NovelBioConst.DBINFO_SYNONYMS);
					ncbiid.setGenUniID(genUniID);
					ncbiid.setTaxID(taxID);
					servNCBIID.updateNCBIID(ncbiid, true);
				}
			}
		}
		else {
			if (geneInfo2.getSymb() != null) {
				ncbiid = new NCBIID();
				ncbiid.setAccID(geneInfo2.getSymb());
				ncbiid.setDBInfo(NovelBioConst.DBINFO_SYMBOL);
				ncbiid.setGenUniID(genUniID);
				ncbiid.setTaxID(taxID);
				servNCBIID.updateNCBIID(ncbiid, false);
			}
			if (geneInfo2.getSynonym() != null) {
				ncbiid = new NCBIID();
				ncbiid.setAccID(geneInfo2.getSynonym());
				ncbiid.setDBInfo(NovelBioConst.DBINFO_SYNONYMS);
				ncbiid.setGenUniID(genUniID);
				ncbiid.setTaxID(taxID);
				servNCBIID.updateNCBIID(ncbiid, false);
			}
		}
	}
	private void updateUniGeneInfoSymbolAndSynonyms(AGeneInfo uniGeneInfo)
	{
		UniProtID uniprotID = null;
		if (uniGeneInfo.getSep() != null && !uniGeneInfo.equals("")) {
			if (uniGeneInfo.getSymb() != null) {
				String[] ssymb = uniGeneInfo.getSymb().split(uniGeneInfo.getSep());
				for (String string : ssymb) {
					uniprotID = new UniProtID();
					uniprotID.setAccID(string.trim());
					uniprotID.setDBInfo(NovelBioConst.DBINFO_SYMBOL);
					uniprotID.setGenUniID(genUniID);
					uniprotID.setTaxID(taxID);
					servUniProtID.updateUniProtID(uniprotID, true);
				}
			}
			if (uniGeneInfo.getSynonym() != null) {
				String[] ssynonym = uniGeneInfo.getSynonym().split(uniGeneInfo.getSep());
				for (String string : ssynonym) {
					uniprotID = new UniProtID();
					uniprotID.setAccID(string.trim());
					uniprotID.setDBInfo(NovelBioConst.DBINFO_SYNONYMS);
					uniprotID.setGenUniID(genUniID);
					uniprotID.setTaxID(taxID);
					servUniProtID.updateUniProtID(uniprotID, true);
				}
			}
		}
		else {
			if (uniGeneInfo.getSymb() != null) {
				uniprotID = new UniProtID();
				uniprotID.setAccID(uniGeneInfo.getSymb());
				uniprotID.setDBInfo(NovelBioConst.DBINFO_SYMBOL);
				uniprotID.setGenUniID(genUniID);
				uniprotID.setTaxID(taxID);
				servUniProtID.updateUniProtID(uniprotID, true);
			}
			if (uniGeneInfo.getSynonym() != null) {
				uniprotID = new UniProtID();
				uniprotID.setAccID(uniGeneInfo.getSynonym());
				uniprotID.setDBInfo(NovelBioConst.DBINFO_SYNONYMS);
				uniprotID.setGenUniID(genUniID);
				uniprotID.setTaxID(taxID);
				servUniProtID.updateUniProtID(uniprotID, true);
			}
		}
	}
	// /////////////////////// ���� Blast ����Ϣ
	// /////////////////////////////////////////////////////

	private boolean updateBlastInfo() {
		boolean blastCorrect = true;
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		if (lsBlastInfosUpdate == null) {
			return true;
		}
		for (BlastInfo blastInfo : lsBlastInfosUpdate) {
			if (blastInfo.getSubjectTab().equals(CopedID.IDTYPE_ACCID)) {
				blastCorrect = false;
				continue;
			}
			servBlastInfo.updateBlast(blastInfo);
		}
		return blastCorrect;
	}

	// /////////////////////// ���� uniGene ����Ϣ
	// /////////////////////////////////////////////////////
	/**
	 * ����lsRefAccID����Ϣ�趨geneUniID��IDtype �������Ӧ��geneUniID
	 * arraylist-string:0:Ϊ"geneID"��"uniID"��"accID"��1:taxID��2-֮�󣺾����geneID ��
	 * UniID��accID û�鵽�ͷ���accID-accID
	 */
	private ArrayList<String> getUpdateGenUniID() {
		ArrayList<String> lsgeneID = new ArrayList<String>();
		// /// ����Ѿ�����IDtype����ֱ�ӷ��� ////////////////////////////////////////
		if (!idType.equals(CopedID.IDTYPE_ACCID)) {
			lsgeneID.add(idType);
			lsgeneID.add(taxID + "");
			lsgeneID.add(genUniID);
			return lsgeneID;
		}
		// ///////////////////////////////////////////
		// ��������refID--Ҳ�������ڲ������ݿ��refxDB����ϢID����������Ӧ��geneUniID
		ArrayList<ArrayList<String>> lsGenUniID = new ArrayList<ArrayList<String>>();
		for (String string : lsRefAccID) {
			ArrayList<String> lsTmpGenUniID = getNCBIUniTax(string, taxID);
			lsGenUniID.add(lsTmpGenUniID);
			if (lsTmpGenUniID.get(0).equals(CopedID.IDTYPE_ACCID)) {
				continue;
			} else if (lsTmpGenUniID.get(0).equals(CopedID.IDTYPE_GENEID)
					&& lsTmpGenUniID.size() == 3) {
				genUniID = lsTmpGenUniID.get(2);
				idType = CopedID.IDTYPE_GENEID;
				return lsTmpGenUniID;
			}
		}
		// ��ѡ����̵�geneID
		Collections.sort(lsGenUniID, new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				Integer o1Info = CopedIDAbs.getHashAccIDtype2Int().get(
						o1.get(0));
				Integer o2Info = CopedIDAbs.getHashAccIDtype2Int().get(
						o2.get(0));
				int flag = o1Info.compareTo(o2Info);
				if (flag != 0) {
					return flag;
				} else {
					Integer o1Len = o1.size();
					Integer o2Len = o2.size();
					return o1Len.compareTo(o2Len);
				}
			}
		});
		if (lsGenUniID.size() == 0) {
			return null;
		}
		genUniID = lsGenUniID.get(0).get(2);
		idType = lsGenUniID.get(0).get(0);
		return lsGenUniID.get(0);
	}

	private static HashMap<String, Integer> hashAccIDtype2Int = null;

	private static HashMap<String, Integer> getHashAccIDtype2Int() {
		if (hashAccIDtype2Int == null) {
			hashAccIDtype2Int.put(CopedID.IDTYPE_ACCID, 300);
			hashAccIDtype2Int.put(CopedID.IDTYPE_UNIID, 200);
			hashAccIDtype2Int.put(CopedID.IDTYPE_GENEID, 100);
		}
		return hashAccIDtype2Int;
	}

	// ///////////////// static method
	// //////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����һ��accessID�������access��NCBIID���򷵻�NCBIID��geneID
	 * �����uniprotID���򷵻�Uniprot��UniID
	 * �������õ����������ϵ�accID����ô�������ݿ�ΪDBINFO_SYNONYMS����Ŀ
	 * 
	 * @param accID
	 *            �����accID,û������ȥ�ո�ȥ��
	 * @param taxID
	 *            ����ID�������֪��������Ϊ0��ֻҪ����symbol������Ϊ0
	 * @return 
	 *         arraylist-string:0:Ϊ"geneID"��"uniID"��"accID"��1:taxID��2-֮�󣺾����geneID
	 *         �� UniID��accID<br>
	 *         û�鵽�ͷ���accID-accID
	 */
	public static ArrayList<String> getNCBIUniTax(String accID, int taxID) {
		ServNCBIID servGeneAnno = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();
		ArrayList<String> lsResult = new ArrayList<String>();
		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID);
		ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		// �Ȳ�ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0) {
			lsResult.add(CopedID.IDTYPE_GENEID);
			lsResult.add(lsNcbiids.get(0).getTaxID() + "");
			for (NCBIID ncbiid2 : lsNcbiids) {
				if (ncbiid2.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
					continue;
				}
				lsResult.add(ncbiid2.getGeneId() + "");
			}
			if (lsResult.size() <= 2) {
				lsResult.add(lsNcbiids.get(0).getGeneId() + "");
			}
			return lsResult;
		}
		// �鲻����uniprotID
		else {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setTaxID(taxID);
			lsUniProtIDs = servUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) {
				lsResult.add(CopedID.IDTYPE_UNIID);
				lsResult.add(lsUniProtIDs.get(0).getTaxID() + "");
				for (UniProtID uniProtID2 : lsUniProtIDs) {
					if (uniProtID2.getDBInfo().equals(
							NovelBioConst.DBINFO_SYNONYMS)) {
						continue;
					}
					lsResult.add(uniProtID2.getUniID());
				}
				if (lsResult.size() <= 2) {
					lsResult.add(lsUniProtIDs.get(0).getUniID() + "");
				}
				return lsResult;
			}
		}
		lsResult.add(CopedID.IDTYPE_ACCID);
		lsResult.add(taxID + "");
		lsResult.add(accID);
		return lsResult;
	}

	// /////////////////////////// ��дequals��
	// ////////////////////////////////////
	/**
	 * ֻҪ����ncbiid��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
	 * 
	 * @Override
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		CopedID otherObj = (CopedID) obj;

		if (
		// geneID��ͬ�Ҷ���Ϊ������������Ϊ����������ͬ
		(!genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("")
				&& genUniID.trim().equals(otherObj.getGenUniID().trim())
				&& idType.equals(otherObj.getIDtype()) && taxID == otherObj
				.getTaxID())
				|| // geneID��Ϊ""����ô�������accID��ͬ�Ҳ�Ϊ""��Ҳ����Ϊ����������ͬ
				(genUniID.trim().equals("")
						&& otherObj.getGenUniID().trim().equals("")
						&& (!accID.equals("") && !otherObj.getAccID()
								.equals(""))
						&& accID.equals(otherObj.getAccID())
						&& idType.equals(otherObj.getIDtype()) && taxID == otherObj
						.getTaxID())
				|| (genUniID.trim().equals("")
						&& otherObj.getGenUniID().trim().equals("")
						&& accID.equals("") && otherObj.getAccID().equals(""))) {
			return true;
		}
		return false;
	}

	/**
	 * ��дhashcode
	 */
	public int hashCode() {
		String hash = "";
		if (!genUniID.trim().equals("")) {
			hash = genUniID.trim() + "sep_@@_genUni_" + idType.trim() + "@@"
					+ taxID;
		} else if (genUniID.trim().equals("") && !accID.trim().equals("")) {
			hash = accID.trim() + "@@accID" + idType.trim() + "@@" + taxID;
		} else if (genUniID.trim().equals("") && accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}

}
