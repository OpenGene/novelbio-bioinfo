package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.KEGGDAO.DaoKCdetail;
import com.novelbio.database.DAO.KEGGDAO.DaoKEntry;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGCgen2Entry;
import com.novelbio.database.entity.kegg.KGCgen2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;

/**
 * CopedID��ص�Kegg��Ϣ
 * @author zong0jie
 *
 */
public class KegGenEntryKO {
	
	private String genUniID;
	public String getGenUniID() {
		return this.genUniID;
	}
	
	private String keggID;
	public String getKegID() {
		return this.keggID;
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	
	private ArrayList<KegEntity> lsKegEntities = null;
	
	
	/**
	 * ��������������ע��ȫ�����ֵ�KG����ô����洢blast��query������Ϣ��Ҳ����mapping��query���ֵ�entry
	 */
	private ArrayList<KegEntity> lsQKegEntities = null;
	/**
	 * ר�ű��lsQKGentries������ID�ģ����QtaxID == 0 ˵��lsQKGentriesû�б���ʼ��
	 */
	private int QtaxID = 0;
	/**
	 * û�оͷ���null
	 * @return
	 */
	public ArrayList<KegEntity> getLsKGentries() {
		return this.lsKegEntities;
	}
	
	protected void setParam(KGCgen2Entry kgCgen2Entry) {
		this.genUniID = kgCgen2Entry.getGenID()+"";
		this.keggID = kgCgen2Entry.getKegID();
		ArrayList<KGentry> lsKGentries = kgCgen2Entry.getLsKGentries();
		if (lsKGentries != null && lsKGentries.size() > 0) {
			for (KGentry kGentry : lsKGentries) {
				KegEntity kegEntity = new KegEntity(kGentry);
				this.lsKegEntities.add(kegEntity);
			}
		}
		this.taxID = kgCgen2Entry.getTaxID();
	}
	
	/**
	 * <b>��ʼ�������ʱ��ʹ��</b>
	 * 
	 * ��ncbiidȥ�������ݿ⣬����øû����entry����Ҫ������pathway��enrichment
	 * <b>���ncbiid��geneID == 0</b>��˵����NCBIIDû��ֵ����ô����null
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * @param idType
	 * @param geneID
	 * @param taxID
	 * @return
	 */
	public KegGenEntryKO(String idType, String geneID,int taxID) 
	{
		if (idType.equals(CopedID.IDTYPE_GENEID)) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setGeneId(Integer.parseInt(geneID));
			ncbiid.setTaxID(taxID);
			KGCgen2Entry kgCgen2Entry = null;
			kgCgen2Entry = DaoKCdetail.queryGen2entry(ncbiid);
			if (kgCgen2Entry != null) {
				setParam(kgCgen2Entry);
			}
		}
	}

	ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = null;
	/**
	 * ��ø�keggID����Ӧ��KO
	 * ���û�оͷ���null
	 */
	public ArrayList<String> getLsKo()
	{
		if (lsKgiDkeg2Kos == null) {
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
			kgiDkeg2Ko.setKeggID(keggID); kgiDkeg2Ko.setTaxID(taxID);
			lsKgiDkeg2Kos = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
			if (lsKgiDkeg2Kos == null) {
				lsKgiDkeg2Kos = new ArrayList<KGIDkeg2Ko>();
			}
		}
		if (lsKgiDkeg2Kos.size() == 0) {
			return null;
		}
		ArrayList<String> lsKO = new ArrayList<String>();
		for (KGIDkeg2Ko kgiDkeg2Ko : lsKgiDkeg2Kos) {
			lsKO.add(kgiDkeg2Ko.getKo());
		}
		return lsKO;
	}
	
	/**
	 * �����������ͨ��blast��õģ���ô�������������е�pathway��entity mapping��Query ����
	 * ���û�оͷ���null
	 */
	public ArrayList<KegEntity> getBlastQInfo(int QtaxID)
	{
		///////////�����ǰ�Ѿ����ҹ���һ��///////////
		if (this.QtaxID == QtaxID) {
			if (lsQKegEntities == null || lsQKegEntities.size() <1) {
				return null;
			}
			return lsQKegEntities;
		}
		///////////////////////////
		lsQKegEntities = new ArrayList<KegEntity>();
		this.QtaxID = QtaxID;
		
		ArrayList<String> lsKO = getLsKo();
		if (lsKO == null) {
			return null;
		}
		for (String ko : lsKO) {
			KGentry kGentry = new KGentry();
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();  kgiDkeg2Ko.setKo(ko); kgiDkeg2Ko.setTaxID(QtaxID);
			ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
			ArrayList<KegEntity> lskGentries = new ArrayList<KegEntity>();
			////////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID������KeggIDֱ��mapping�ر�����////////////////////////////////////////////////////////////////
			if (lsKgiDkeg2Kos != null && lsKgiDkeg2Kos.size()>0) 
			{
				//��Ȼһ��ko��Ӧ���keggID�����Ƕ���pathway��˵��һ��ko�Ͷ�Ӧ��һ��pathway�ϣ�����һ��ko�͹���
				String keggID = lsKgiDkeg2Kos.get(0).getKeggID();//����Ǳ����е�KeggID�������KeggIDֱ�ӿ���������Ӧ��pathway
				kGentry.setEntryName(keggID);
				kGentry.setTaxID(QtaxID);
				//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
				lskGentries=KegEntity.getLsEntity(kGentry);
			}
			/////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID�����û��KeggID������KOmapping�ر�����//////////////////////////////////////////////////////////////////
			else
			{
				kGentry.setEntryName(ko);
				kGentry.setTaxID(QtaxID);
				//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
				lskGentries=KegEntity.getLsEntity(kGentry);
			}
			if (lskGentries == null || lskGentries.size() < 1 ) {
				continue;
			}
			lsQKegEntities.addAll(lskGentries);
		}
		if (lsQKegEntities.size() < 1 ) {
			return null;
		}
		return lsQKegEntities;
	}
	
}
