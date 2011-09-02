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
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	protected String genUniAccID = "";
	
	static HashMap<Integer, String[]> hashTaxID = CopedID.getSpecies();
	/**
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	public String getGenUniID() {
		return this.genUniAccID;
	}
	
	protected String keggID = null;
	/**
	 * 获得该ID所对应的keggID，如果没有，则返回null
	 */
	public String getKegID() {
		setKeggID();
		return this.keggID;
	}
	/**
	 * 设定该类的KeggID等，需要根据accID，geneID和UniID等选择不同的对象<br>
	 * 譬如accID就是KGNIdKeg
	 */
	protected abstract void setKeggID();
	
	protected int taxID;
	public int getTaxID() {
		return this.taxID;
	}

	boolean boolskGentries = false;
	private ArrayList<KGentry> lskGentries = null;
	/**
	 * 返回该geneID所对应的KGentry
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
	 * kegID2KO的对照表
	 */
	ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = null;
	
	/**
	 * 获得该keggID所对应的KO<br>
	 * 如果没有就返回null<br>
	 * accID需要将其覆盖，因为理论上accID只有希望对应component
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
	 * 如果本类是人类等注释全面物种的KG，那么这个存储blast的query物种信息，也就是mapping到query物种的entry
	 */
	private ArrayList<KGentry> lsQKegEntities = null;
	
	private HashSet<KGentry> hashKegEntities = new HashSet<KGentry>();
	/**
	 * 将通过blast获得的KO list放入，获得本物种相应的KGentry list
	 * 如果没有就返回null
	 * accID需要将其覆盖，因为理论上accID只有希望对应component
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
			////////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，并用KeggID直接mapping回本基因////////////////////////////////////////////////////////////////
			if (lsKgiDkeg2Kos != null && lsKgiDkeg2Kos.size()>0) 
			{
				//虽然一个ko对应多个keggID，但是对于pathway来说，一个ko就对应到一个pathway上，所以一个ko就够了
				String keggID = lsKgiDkeg2Kos.get(0).getKeggID();//这就是本物中的KeggID，用这个KeggID直接可以搜索相应的pathway
				KGentry kGentry = new KGentry(); kGentry.setEntryName(keggID); kGentry.setTaxID(taxID);
				//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
				lskGentriesTmp=KGentry.getLsEntity(kGentry);
			}
			/////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，如果没有KeggID，则用KOmapping回本基因//////////////////////////////////////////////////////////////////
			else
			{
				KGentry kGentry = new KGentry(); kGentry.setEntryName(ko); kGentry.setTaxID(taxID);
				//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
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
	 * 获得该accID对应的所有不重复的keggpathway对象
	 * <b>如果要用blast的结果，需要先执行getBlastQInfo方法</b>，也就是用另一个copedid的方法获得对应的lsQKegEntities信息<br>
	 * <b>但是如果本基因已经有了pathway信息，那么就不进行blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath() {如何将blast集成进来还需商榷
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
		//用来去冗余，根据pathName进行去冗余
		HashMap<String, KGpathway> hashPath = new HashMap<String, KGpathway>();
		for (KGentry kGentry : lsKGentries) {
			KGpathway kGpathwayQ = new KGpathway(); kGpathwayQ.setPathName(kGentry.getPathName());
			KGpathway kGpathway = DaoKPathway.queryKGpathway(kGpathwayQ);
			hashPath.put(kGpathway.getPathName(), kGpathway);
		}
		return ArrayOperate.getArrayListValue(hashPath);
	}
	
	/**
	 * 还有做Relation方面的工作
	 */
//	KGrelation
	
	
	
	
}
