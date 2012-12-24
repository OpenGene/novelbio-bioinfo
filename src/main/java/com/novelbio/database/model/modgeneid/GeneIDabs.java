package com.novelbio.database.model.modgeneid;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ServBlastInfo;
import com.novelbio.database.service.servgeneanno.ServGene2Go;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniGene2Go;
import com.novelbio.database.service.servgeneanno.ServUniGeneInfo;
import com.novelbio.database.service.servgeneanno.ServUniProtID;
import com.novelbio.generalConf.NovelBioConst;

public abstract class GeneIDabs implements GeneIDInt {
	private static Logger logger = Logger.getLogger(GeneIDabs.class);
	static HashMap<Integer, String> hashDBtype = new HashMap<Integer, String>();
	
	/**  ����id  */
	int taxID = 0;
	/**  idType��������IDTYPE�е�һ�� */
	String idType = GeneID.IDTYPE_ACCID;
	/** �����accID */
	String accID = null;
	String genUniID = "";
	String symbol = null;
	/** Ʃ���Ͷ�����ֽ���blast��Ȼ������Щ���ֵ���Ϣ��ȡ���� */
	ArrayList<BlastInfo> lsBlastInfos = null;
	double evalue = 10;
	AGeneInfo geneInfo = null;
	KeggInfo keggInfo;
	GOInfoAbs goInfoAbs = null;
	/** �趨�Ƿ�blast�Ľ�������˲��� */
	boolean isBlastedFlag = false;
	ArrayList<GeneID> lsBlastGeneID = new ArrayList<GeneID>();
	boolean overrideUpdateDBinfo = false;
	String geneIDDBinfo;
	// //////////////////// service ��
	ServBlastInfo servBlastInfo = new ServBlastInfo();
	ServNCBIID servNCBIID = new ServNCBIID();
	ServUniProtID servUniProtID = new ServUniProtID();
	ServGeneInfo servGeneInfo = new ServGeneInfo();
	ServUniGeneInfo servUniGeneInfo = new ServUniGeneInfo();
	ServGene2Go servGene2Go = new ServGene2Go();
	ServUniGene2Go servUniGene2Go = new ServUniGene2Go();
	// ///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����������accID����ô���ظ�accID��Ӧ�����ݿ�
	 * ���û���򷵻�null
	 * @return
	 */
	@Override
	public String getDBinfo() {
		return this.geneIDDBinfo;
	}
	/**
	 * �����ֵ�symbolӦ���������ĸ����ݿ�
	 * @return
	 */
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
	/**
	 * �趨������ֽ���blast ÿ���趨�󶼻�ˢ��
	 * @param evalue
	 * @param StaxID
	 */
	@Override
	public abstract void setBlastInfo(double evalue, int... StaxID);
	
	protected void addLsBlastInfo(BlastInfo blastInfo) {
		if (blastInfo != null)
			lsBlastInfos.add(blastInfo);
	}
	/**
	 * �������ֵ�blast ��ñ�copedID blast����Ӧ���ֵĵ�һ��copedID��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	@Override
	public GeneID getGeneIDBlast() {
		ArrayList<GeneID> lsGeneIDs = getLsBlastGeneID();
		if (lsGeneIDs.size() != 0) {
			return lsGeneIDs.get(0);
		}
		return null;
	}

	/**
	 * ����趨�ĵ�һ��blast�Ķ�������Ҫ�趨blast��Ŀ��
	 * @param blastInfo
	 * @return
	 */
	private GeneID getBlastGeneID(BlastInfo blastInfo) {
		if (blastInfo == null) return null;
		
		String idType = blastInfo.getSubjectTab();
		GeneID copedID = new GeneID(idType, blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		return copedID;
	}
	/**
	 * blast������� ����Ҫ�趨blast��Ŀ�� �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ����һϵ�е�Ŀ�����ֵ�taxID�����CopedIDlist ���û�н��������һ���յ�lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	@Override
	public ArrayList<GeneID> getLsBlastGeneID() {
		if (isBlastedFlag) {
			return lsBlastGeneID;
		}
		isBlastedFlag = true;
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return new ArrayList<GeneID>();
		}
		for (BlastInfo blastInfo : lsBlastInfos) {
			GeneID copedID = getBlastGeneID(blastInfo);
			if (copedID != null) {
				lsBlastGeneID.add(copedID);
			}
		}
		return lsBlastGeneID;
	}

	/**
	 * blast������� ����Ҫ�趨blast��Ŀ�� �÷����� setBlastInfo(double evalue, int... StaxID)
	 * @return ����blast����Ϣ������evalue�ȣ���list��getCopedIDLsBlast()�õ���list��һһ��Ӧ��
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}
	// ////////////////// normal setting
	/**
	 * idType��������IDTYPE�е�һ��
	 * �������趨��lsRefAccID�󣬿��Ը��ݾ����lsRefAccIDȥ�������ݿⲢȷ��idtype
	 */
	public String getIDtype() {
		if (lsRefAccID == null || lsRefAccID.size() == 0) {
			return this.idType;
		}
		AgeneUniID ageneUniID = getUpdateGenUniID();
		if (ageneUniID == null) {
			return GeneID.IDTYPE_ACCID;
		}
		return ageneUniID.getGeneIDtype();
	}
	/**
	 * �����accID�����û��������������ץһ������
	 */
	public String getAccID() {
		if (accID == null) {
			accID = getAccIDDBinfo(getDatabaseTyep());
		}
		return this.accID;
	}
	
	/**
	 * �����accID���������ݿ����ץһ������
	 */
	public String getAccIDDBinfo() {
		 String accID = getAccIDDBinfo(getDatabaseTyep());
		 if (accID == null) {
			 accID = getAccID();
		 }
		 return accID;
	}
	
	/**
	 * * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��accID��û���򷵻�null
	 * @param dbInfo Ϊnull��ʾ��ָ��dbinfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo) {
		AgeneUniID genuniID = getGenUniID(getGenUniID(), dbInfo);
		if (genuniID != null) {
			return genuniID.getAccID();
		}
		return null;
	}
	public String getGenUniID() {
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
		if (geneInfo == null || geneInfo.getDescrp() == null) {
			return "";
		}
		return geneInfo.getDescrp().replaceAll("\"", "");
	}
	/**
	 * ��øû����symbol
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
		setGenInfo();
		if (geneInfo == null) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		} else {
			geneInfo.setTaxID(taxID);
			symbol = geneInfo.getSymb();
		}
		if (symbol == null || symbol.equals("")) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		}
		symbol = symbol.replace("@", "");
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
	 * * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��AgeneUniID�����Ҹ�AgeneUniIDҲ����Ӧһ��geneUniID��
	 * �����dbinfoû�У�����㷵��һ����
	 * @param dbInfo
	 * @return
	 */
	protected abstract AgeneUniID getGenUniID(String genUniID, String dbInfo);
	
	/**
	 * ���趨blast����� ���blast * 0:symbol 1:description  2:subjectSpecies 3:evalue
	 * 4:symbol 5:description �����blast 0:symbol 1:description
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
			if (getLsBlastGeneID() != null && getLsBlastInfos() != null
					&& getLsBlastInfos().size() > 0) {
				for (int i = 0; i < getLsBlastInfos().size(); i++) {
					if (tmpAnno[2] == null || tmpAnno[2].trim().equals("")) {
						tmpAnno[2] = Species.getSpeciesTaxIDName().get(getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = getLsBlastInfos().get(i).getEvalue() + "";
						tmpAnno[4] = getLsBlastGeneID().get(i).getSymbol();
						tmpAnno[5] = getLsBlastGeneID().get(i).getDescription();
					} else {
						tmpAnno[2] = tmpAnno[2] + "//" + Species.getSpeciesTaxIDName().get(getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = tmpAnno[3] + "//" + getLsBlastInfos().get(i).getEvalue();
						tmpAnno[4] = tmpAnno[4] + "//" + getLsBlastGeneID().get(i).getSymbol();
						tmpAnno[5] = tmpAnno[5] + "//" + getLsBlastGeneID().get(i).getDescription();
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
	/**
	 * �趨 goInfoAbs����Ϣ
	 */
	protected abstract void setGoInfo();
	/**
	 * ����geneinfo��Ϣ
	 * @return
	 */
	public AGeneInfo getGeneInfo() {
		setGenInfo();
		return geneInfo;
	}
	/**
	 * ���ظû�������Ӧ��GOInfo��Ϣ��������Blast
	 * @return
	 */
	public GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			setGoInfo();
		}
		return goInfoAbs;
	}
	/**
	 * ���ظ�CopedID����Ӧ��Gene2GOInfo <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
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

		ArrayList<GeneID> lsBlastGeneIDs = getLsBlastGeneID();
		for (GeneID geneID : lsBlastGeneIDs) {
			lsGoInfo.add(geneID.getGOInfo());
		}
		return getGOInfo().getLsGen2Go(lsGoInfo, GOType);
	}
	ArrayList<KGentry> lsKGentries = null;
	// ////////////////KEGG //////////////////////////////////////////////
	/**
	 * ��ø�CopeID��List-KGentry,���û�л�Ϊ�գ��򷵻�null
	 * 
	 * @param blast
	 *            �Ƿ�blast����Ӧ���ֲ鿴
	 * @param StaxID
	 *            ���blastΪtrue����ô�趨StaxID
	 * @return ���û�оͷ���һ���յ�list
	 */
	public ArrayList<KGentry> getKegEntity(boolean blast) {
		getKeggInfo();
		if (!blast) {
			return keggInfo.getLsKgGentries(null);
		} else {
			ArrayList<GeneID> lsGeneIDsBlast = getLsBlastGeneID();
			ArrayList<KeggInfo> lsKeggInfos = new ArrayList<KeggInfo>();
			for (GeneID copedID : lsGeneIDsBlast) {
				lsKeggInfos.add(copedID.getKeggInfo());
			}
			return keggInfo.getLsKgGentries(lsKeggInfos);
		}
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
	 * blast������� �����趨blast������ �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��KegPath
	 * û�оͷ��ؿյ�list
	 */
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		getKeggInfo();
		if (blast) {
			ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
			ArrayList<GeneID> lsBlastCopedIDs = getLsBlastGeneID();
			for (GeneID copedID : lsBlastCopedIDs) {
				lskeggInfo.add(copedID.getKeggInfo());
			}
			return keggInfo.getLsKegPath(lskeggInfo);
		} else {
			return keggInfo.getLsKegPath();
		}
	}
	// ///////////////// update method
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	ArrayList<String> lsRefAccID = new ArrayList<String>();
	Boolean uniqID = null;
	/**
	 * �ڲ���refaccID��Ϊ���ս�������ID��ʱ���Ƿ������uniqID
	 * @param uniqID	�ø����Ĳο�ID���ҵ����ݿ��е�Ψһ����
	 * true��ֻ�е�uniqIDʱ������
	 * null��Ĭ�ϲ���--��uniqIDҲ����������ֻ������һ������
	 * false����uniqIDҲ������������������ȫ��ID���ù�����δʵ��
	 */
	@Override
	public void setUpdateRefAccIDClear(Boolean uniqID) {
		this.uniqID = uniqID;
	}
	/**
	 * ��ӿ��������������ݿ��ID 
	 * Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	@Override
	public void addUpdateRefAccID(String... refAccID) {
		for (String string : refAccID) {
			String tmpRefID = GeneID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			lsRefAccID.add(tmpRefID);
		}
	}
	/**
	 * ���ÿ��������������ݿ��ID 
	 * Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	@Override
	public void setUpdateRefAccID(String... refAccID) {
		lsRefAccID.clear();
		addUpdateRefAccID(refAccID);
	}
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	@Override
	public void setUpdateRefAccID(ArrayList<String> lsRefAccID) {
		this.lsRefAccID.clear();
		for (String string : lsRefAccID) {
			String tmpRefID = GeneID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			this.lsRefAccID.add(tmpRefID);
		}
	}


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
			this.geneIDDBinfo = DBInfo;
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
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
		this.accID = GeneID.removeDot(accID);
	}
	/**
	 * �趨��ID��accID�������������ID
	 */
	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		this.accID = accID;
	}
	
	/** ��¼������GO��Ϣ�ģ�ÿ��������Ϻ���� */
	ArrayList<Gene2Go> lsGOInfoUpdate = new ArrayList<Gene2Go>();

	/**
	 * ����������Ҫ������GO��Ϣ��������� ����ֻ���Ȼ�ȡGO����Ϣ������������method��ʱ��������
	 * �����������ϵ����
	 * @param GOID ����
	 * @param GOdatabase û�о�����Ϊ null 
	 * @param GOevidence û�о�����Ϊ null 
	 * @param GORef û�о�����Ϊ null 
	 * @param gOQualifiy û�о�����Ϊ null 
	 */
	@Override
	public void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		if (GOID == null) {
			return;
		}
		GOID = GOID.trim();
		if (GOID.equals("")) {
			return;	
		}
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
		if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
			logger.error("û�и�blast��accID��"+SubAccID);
			return;
		}
		blastInfo.setQueryID(genUniID);
		blastInfo.setQueryTax(getTaxID());
		blastInfo.setEvalue_Identity(evalue, identities);
		blastInfo.setQueryDB_SubDB(this.geneIDDBinfo, subDBInfo);
		if (lsBlastInfosUpdate == null) {
			lsBlastInfosUpdate = new ArrayList<BlastInfo>();
		}
		lsBlastInfosUpdate.add(blastInfo);
	}
	/**
	 * 
	 * ���û��QueryID, SubjectID, taxID�е��κ�һ��Ͳ����� ���evalue>50 �� evalue<0���Ͳ�����
	 * �����������ϵ����
	 * @param blastInfo
	 */
	@Override
	public void setUpdateBlastInfo(String SubGenUniID, String subIDtype, String subDBInfo, int SubTaxID, double evalue, double identities) {
		if (genUniID == null || genUniID.equals("")) {
			return;
		}
		BlastInfo blastInfo = new BlastInfo(null, 0, SubGenUniID, subIDtype, SubTaxID);
		if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
			logger.error("û�и�blast��geneUniID��"+SubGenUniID);
		}
		blastInfo.setQueryID(genUniID);
		blastInfo.setQueryTax(getTaxID());
		blastInfo.setEvalue_Identity(evalue, identities);
		blastInfo.setQueryDB_SubDB(this.geneIDDBinfo, subDBInfo);
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
		AgeneUniID geneUniID = getUpdateGenUniID();
		if (geneUniID == null) {
			return false;
		}
		if (geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_GENEID) && geneUniID.getGenUniID().equals("0")) {
			logger.error("geneIDΪ0����check");
			return false;
		}
		boolean flag1 = false;
		try {
			flag1 =updateGeneID(geneUniID, updateUniID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		boolean flag2 = updateGeneInfo();
		boolean flag3 = false;
		try {
			flag3 = updateGene2Go();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean flag4 = updateBlastInfo();
		return flag1&&flag2&&flag3&&flag4;
	}

	/** ����GO���ݿ� */
	private boolean updateGene2Go() {
		boolean flag = true;
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		for (Gene2Go gene2Go : lsGOInfoUpdate) {
			if (idType.equals(GeneID.IDTYPE_GENEID)) {
				if (!servGene2Go.updateGene2Go(genUniID, taxID, gene2Go))
					flag = false;
			} 
			else if (idType.equals(GeneID.IDTYPE_UNIID)) {
				if (!servUniGene2Go.updateUniGene2Go(genUniID, taxID, gene2Go))
					flag = false;
			}
			else {
				return false;
			}
		}
		return flag;
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
	private boolean updateGeneID(AgeneUniID geneUniID, boolean updateUniID) {
		if (genUniID == null && accID == null) {
			return false;
		}
		//˵������Ҫ����accID
		if (accID == null || accID.equals("")) {
			return true;
		}
		if (geneIDDBinfo == null || geneIDDBinfo.equals("")) {
			logger.error("����geneIDʱû�����ø�gene�����ݿ���Դ���Զ�����ΪNCBIID");
			geneIDDBinfo = NovelBioConst.DBINFO_NCBI_ACC_GENEAC;
		}
		// ֻ������һ����õ�geneID
		if (geneUniID != null && !geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_ACCID)) {
			this.idType = geneUniID.getGeneIDtype();
			//refAccID���ܻ�鵽������ֹһ��ID����ͬ��������ò�ͬ�ķ�������
			if (uniqID == null) {
				
			}
			
			if (geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_GENEID)) {
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(accID);
				ncbiid.setDBInfo(this.geneIDDBinfo);
				ncbiid.setGenUniID(geneUniID.getGenUniID());
				ncbiid.setTaxID(geneUniID.getTaxID());
				servNCBIID.updateNCBIID(ncbiid, overrideUpdateDBinfo);
			} else if (geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_UNIID)) {
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(accID);
				uniProtID.setDBInfo(this.geneIDDBinfo);
				uniProtID.setGenUniID(geneUniID.getGenUniID());
				uniProtID.setTaxID(geneUniID.getTaxID());
				servUniProtID.updateUniProtID(uniProtID, overrideUpdateDBinfo);
			}
		} else if (updateUniID) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setDBInfo(this.geneIDDBinfo);
			uniProtID.setGenUniID(genUniID);
			uniProtID.setTaxID(taxID);
			servUniProtID.updateUniProtID(uniProtID, overrideUpdateDBinfo);
			// ����geneUniID
			idType = GeneID.IDTYPE_UNIID;
			genUniID = accID;
			//��ֹ��һ�������ʱ�����
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			servUniGeneInfo.updateUniGenInfo(genUniID, taxID, geneInfo);
			updateUniGeneInfoSymbolAndSynonyms(geneInfo);
		} else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			try {
				servGeneInfo.updateGenInfo(genUniID, taxID, geneInfo);
			} catch (Exception e) {
				servGeneInfo.updateGenInfo(genUniID, taxID, geneInfo);
			}
			updateGeneInfoSymbolAndSynonyms(geneInfo);
		}
		else {
			return false;
		}
		geneInfo = null;
		return true;
	}
	
	private void updateGeneInfoSymbolAndSynonyms(AGeneInfo geneInfo2) {
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
	private void updateUniGeneInfoSymbolAndSynonyms(AGeneInfo uniGeneInfo) {
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
			if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
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
	 */
	private AgeneUniID getUpdateGenUniID() {
		// /// ����Ѿ�����IDtype����ֱ�ӷ��� ////////////////////////////////////////
		if (!idType.equals(GeneID.IDTYPE_ACCID)) {
			AgeneUniID geneUniID = null;
			if (idType.equals(GeneID.IDTYPE_GENEID)) {
				geneUniID = new NCBIID();
			} else if (idType.equals(GeneID.IDTYPE_UNIID)) {
				geneUniID = new UniProtID();
			}
			geneUniID.setTaxID(taxID);
			geneUniID.setGenUniID(genUniID);
			geneUniID.setAccID(accID);
			geneUniID.setDBInfo(geneIDDBinfo);
			return geneUniID;
		}
		
		// ��������refID--Ҳ�������ڲ������ݿ��refxDB����ϢID����������Ӧ��geneUniID
		ArrayList<ArrayList<AgeneUniID>> lsgeneID = new ArrayList<ArrayList<AgeneUniID>>();
		for (String string : lsRefAccID) {
			ArrayList<AgeneUniID> lsTmpGenUniID = getNCBIUniTax(string, taxID);
			if (lsTmpGenUniID.size() == 0 || lsTmpGenUniID.get(0).equals(GeneID.IDTYPE_ACCID)) {
				continue;
			} else if (lsTmpGenUniID.size() == 1 && lsTmpGenUniID.get(0).getDBInfo().equals(GeneID.IDTYPE_GENEID)) {
				genUniID = lsTmpGenUniID.get(0).getGenUniID();
				idType = GeneID.IDTYPE_GENEID;
				return lsTmpGenUniID.get(0);
			}
			lsgeneID.add(lsTmpGenUniID);
		}
		// ��ѡ������geneUniID��geneID
		Collections.sort(lsgeneID, new Comparator<ArrayList<AgeneUniID>>() {
			public int compare(ArrayList<AgeneUniID> o1, ArrayList<AgeneUniID> o2) {
				Integer o1Info = GeneIDabs.getHashAccIDtype2Int().get(o1.get(0).getGeneIDtype());
				Integer o2Info = GeneIDabs.getHashAccIDtype2Int().get(o2.get(0).getGeneIDtype());
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
		if (lsgeneID.size() == 0) {
			if (accID != null && !accID.equals("")) {
				if (genUniID == null || genUniID.equals("") || genUniID.equals("0")) {
					genUniID = accID;
				}
				AgeneUniID geneUniID = new UniProtID();
				geneUniID.setTaxID(taxID);
				geneUniID.setGenUniID(genUniID);
				geneUniID.setAccID(accID);
				geneUniID.setDBInfo(geneIDDBinfo);
				return geneUniID;
			}
		}
		AgeneUniID ageneUniID = lsgeneID.get(0).get(0);
		return ageneUniID;
	}

	private static HashMap<String, Integer> hashAccIDtype2Int = null;

	private static HashMap<String, Integer> getHashAccIDtype2Int() {
		if (hashAccIDtype2Int == null) {
			hashAccIDtype2Int = new HashMap<String, Integer>();
			hashAccIDtype2Int.put(GeneID.IDTYPE_ACCID, 300);
			hashAccIDtype2Int.put(GeneID.IDTYPE_UNIID, 200);
			hashAccIDtype2Int.put(GeneID.IDTYPE_GENEID, 100);
		}
		return hashAccIDtype2Int;
	}

	// ///////////////// static method
	/**
	 * ����һ��accessID���������ݿ�
	 * ����list-string[] 0: IDtype 1: geneUniID 2: taxID
	 * ���ͬһ�����ֵõ����������ϵ�accID����ô�������ݿ�ΪDBINFO_SYNONYMS����Ŀ
	 * ���û�ѵ����򷵻ؿյ�list
	 * @param accID  �����accID,û������ȥ�ո�ȥ��
	 * @param taxID ����ID�������֪��������Ϊ0��ֻҪ����symbol������Ϊ0
	 * @return list-string[] 0: IDtype 1: geneUniID 2: taxID
	 */
	protected static ArrayList<AgeneUniID> getNCBIUniTax(String accID, int taxID) {
		ArrayList<AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (accID == null || accID.equals("")) {
			return lsResult;
		}
		
		ServNCBIID servGeneAnno = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();

		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID);
		ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
		ArrayList<UniProtID> lsUniProtIDs = null;
		// �Ȳ�ncbiid
		if (lsNcbiids != null && lsNcbiids.size() > 0) {
			return getLsGeneIDinfo(lsNcbiids);
		}
		// �鲻����uniprotID
		else {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setTaxID(taxID);
			lsUniProtIDs = servUniProtID.queryLsUniProtID(uniProtID);
			return getLsGeneIDinfo(lsUniProtIDs);
		}
	}
	/** ָ��һϵ�е�NCBIID����UniID�����й��ˣ�
	 * ��Ҫ�ǹ���
	 * ���ͬһ�����ֵõ����������ϵ�accID����ô�������ݿ�ΪDBINFO_SYNONYMS����Ŀ */
	private static ArrayList<AgeneUniID> getLsGeneIDinfo(ArrayList<? extends AgeneUniID> lsNcbiids) {
		ArrayList<AgeneUniID> lsGeneIDinfo = new ArrayList<AgeneUniID>();
		for (AgeneUniID geneUniID : lsNcbiids) {
			if (geneUniID.getDBInfo().equals(NovelBioConst.DBINFO_SYNONYMS)) {
				continue;
			}
			lsGeneIDinfo.add(geneUniID);
		}
		if (lsGeneIDinfo.size() == 0 && lsNcbiids.size() > 0) {
			lsGeneIDinfo.addAll(lsNcbiids);
		}
		return lsGeneIDinfo;
	}
	// /////////////////////////// ��дequals��
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
		GeneID otherObj = (GeneID) obj;

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
