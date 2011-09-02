package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.DAO.KEGGDAO.DaoKPathway;
import com.novelbio.database.entity.friceDB.TaxInfo;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.entity.kegg.KGrelation;
import com.novelbio.database.updatedb.idconvert.TaxIDInfo;

public abstract class KeggInfoAbs implements KeggInfoInter{
	
	public KeggInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	
	
	/**
	 * geneID��UniID��AccID
	 * �����AccID����ôһ����û��GeneID��UniID��
	 */
	protected String genUniAccID = "";
	
	static HashMap<Integer, String[]> hashTaxID = CopedID.getSpecies();
	/**
	 * geneID��UniID��AccID
	 * �����AccID����ôһ����û��GeneID��UniID��
	 */
	public String getGenUniID() {
		return this.genUniAccID;
	}
	
	protected String keggID = null;
	/**
	 * ��ø�ID����Ӧ��keggID�����û�У��򷵻�null
	 */
	public String getKegID() {
		setKeggID();
		return this.keggID;
	}
	/**
	 * �趨�����KeggID�ȣ���Ҫ����accID��geneID��UniID��ѡ��ͬ�Ķ���<br>
	 * Ʃ��accID����KGNIdKeg
	 */
	protected abstract void setKeggID();
	
	protected int taxID;
	public int getTaxID() {
		return this.taxID;
	}

	boolean boolskGentries = false;
	private ArrayList<KGentry> lskGentries = null;
	/**
	 * ���ظ�geneID����Ӧ��KGentry
	 * @return
	 */
	public ArrayList<KGentry> getKgGentries() {
		if (!boolskGentries) {
			lskGentries = KGentry.getLsEntity(getKegID());
			boolskGentries = true;
		}
		for (KGentry kGentry : lskGentries) {
			if (!hashKegEntities.contains(kGentry)) {
				hashKegEntities.add(kGentry);
			}
		}
		return lskGentries;
	}
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	boolean boolsKgiDkeg2Kos = false;
	/**
	 * kegID2KO�Ķ��ձ�
	 */
	ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = null;
	
	/**
	 * ��ø�keggID����Ӧ��KO<br>
	 * ���û�оͷ���null<br>
	 * accID��Ҫ���串�ǣ���Ϊ������accIDֻ��ϣ����Ӧcomponent
	 */
	public ArrayList<String> getLsKo()
	{
		if (!boolsKgiDkeg2Kos) {
			boolsKgiDkeg2Kos = true;
			
			if (getKegID() == null) {
				return null;
			}
			
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
			kgiDkeg2Ko.setKeggID(getKegID()); kgiDkeg2Ko.setTaxID(taxID);
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
	 * ��������������ע��ȫ�����ֵ�KG����ô����洢blast��query������Ϣ��Ҳ����mapping��query���ֵ�entry
	 */
	private ArrayList<KGentry> lsQKegEntities = null;
	
	private HashSet<KGentry> hashKegEntities = new HashSet<KGentry>();
	/**
	 * ��ͨ��blast��õ�KO list���룬��ñ�������Ӧ��KGentry list
	 * ���û�оͷ���null
	 * accID��Ҫ���串�ǣ���Ϊ������accIDֻ��ϣ����Ӧcomponent
	 */
	public ArrayList<KGentry> getBlastQInfo(List<String> lsKO)
	{
		if (lsKO == null) {
			return null;
		}
		lsQKegEntities = new ArrayList<KGentry>();
		for (String ko : lsKO) {
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();  kgiDkeg2Ko.setKo(ko); kgiDkeg2Ko.setTaxID(taxID);
			ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
			ArrayList<KGentry> lskGentriesTmp = new ArrayList<KGentry>();
			////////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID������KeggIDֱ��mapping�ر�����////////////////////////////////////////////////////////////////
			if (lsKgiDkeg2Kos != null && lsKgiDkeg2Kos.size()>0) 
			{
				//��Ȼһ��ko��Ӧ���keggID�����Ƕ���pathway��˵��һ��ko�Ͷ�Ӧ��һ��pathway�ϣ�����һ��ko�͹���
				String keggID = lsKgiDkeg2Kos.get(0).getKeggID();//����Ǳ����е�KeggID�������KeggIDֱ�ӿ���������Ӧ��pathway
				KGentry kGentry = new KGentry(); kGentry.setEntryName(keggID); kGentry.setTaxID(taxID);
				//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
				lskGentriesTmp=KGentry.getLsEntity(kGentry);
			}
			/////////////���geneBlast�������࣬���ҵõ�����Ӧ��KO����ô��ø�KO����Ӧ�����ֵ�KeggID�����û��KeggID������KOmapping�ر�����//////////////////////////////////////////////////////////////////
			else
			{
				KGentry kGentry = new KGentry(); kGentry.setEntryName(ko); kGentry.setTaxID(taxID);
				//�ڸ���ko��taxID������£�һ��ko���Բ�����pathway����һ��pathway��Ķ��entry
				lskGentriesTmp=KGentry.getLsEntity(kGentry);
			}
			if (lskGentriesTmp == null || lskGentriesTmp.size() < 1 ) {
				continue;
			}
			lsQKegEntities.addAll(lskGentriesTmp);
		}
		if (lsQKegEntities.size() < 1 ) {
			return null;
		}
		for (KGentry kGentry : lsQKegEntities) {
			if (!hashKegEntities.contains(kGentry)) {
				hashKegEntities.add(kGentry);
			}
		}
		return lsQKegEntities;
	}
	/**
	 * ��ø�accID��Ӧ�����в��ظ���keggpathway����
	 * <b>���Ҫ��blast�Ľ������Ҫ��ִ��getBlastQInfo����</b>��Ҳ��������һ��copedid�ķ�����ö�Ӧ��lsQKegEntities��Ϣ<br>
	 * <b>��������������Ѿ�����pathway��Ϣ����ô�Ͳ�����blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath() {��ν�blast���ɽ���������ȶ
		 getKgGentries();
		ArrayList<KGpathway> lsKGpathways = new ArrayList<KGpathway>();
		if (hashKegEntities != null) {
			lsKGpathways = getLsKegPath(hashKegEntities);
		}
		return lsKGpathways;
	}
	
	private ArrayList<KGpathway> getLsKegPath(Collection<KGentry> lsKGentries) {
		if (lsKGentries == null) {
			return null;
		}
		//����ȥ���࣬����pathName����ȥ����
		HashMap<String, KGpathway> hashPath = new HashMap<String, KGpathway>();
		for (KGentry kGentry : lsKGentries) {
			KGpathway kGpathwayQ = new KGpathway(); kGpathwayQ.setPathName(kGentry.getPathName());
			KGpathway kGpathway = DaoKPathway.queryKGpathway(kGpathwayQ);
			hashPath.put(kGpathway.getPathName(), kGpathway);
		}
		return ArrayOperate.getArrayListValue(hashPath);
	}
	
	/**
	 * ������Relation����Ĺ���
	 */
//	KGrelation
	
	
	
	
}
