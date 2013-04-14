package com.novelbio.database.model.modkegg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;
import com.novelbio.database.service.servkegg.ServKPathRelation;
import com.novelbio.database.service.servkegg.ServKPathway;

public abstract class KeggInfoAbs implements KeggInfoInter{
	ServKIDKeg2Ko servKIDKeg2Ko = new ServKIDKeg2Ko();

	public KeggInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	
	
	/**
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	protected String genUniAccID = "";
	
//	static HashMap<Integer, String[]> hashTaxID = CopedID.getSpecies();
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
	/**
	 * 是否已经装载过lskGentries了
	 */
	boolean boolskGentries = false;
	private ArrayList<KGentry> lskGentries = null;
	/**
	 * 返回该geneID所对应的KGentry
	 * 此时会清空blast的结果
	 * @return
	 */
	public ArrayList<KGentry> getKgGentries() {
		if (!boolskGentries) {
			lskGentries = KGentry.getLsEntity(getKegID());
			boolskGentries = true;
		}
		hashKegEntities = new HashSet<KGentry>();
		for (KGentry kGentry : lskGentries) {
			if (!hashKegEntities.contains(kGentry)) {
				hashKegEntities.add(kGentry);
			}
		}
		return lskGentries;
	}
	
	/**
	 * 输入blast到的CopedIDs
	 * 返回该geneID所对应的KGentry
	 * @return
	 */
	public ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		getKgGentries();
		if (ls_keggInfo != null && ls_keggInfo.size() > 0) {
			for (KeggInfoInter keggInfo : ls_keggInfo) {
				getBlastQInfo(keggInfo.getKegID(), keggInfo.getTaxID());
			}
		}
		return ArrayOperate.getArrayListValue(hashKegEntities);
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
		if (!boolsKgiDkeg2Kos || lsKgiDkeg2Kos == null) {
			boolsKgiDkeg2Kos = true;
			
			if (getKegID() == null) {
				return null;
			}
			
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
			kgiDkeg2Ko.setKeggID(getKegID()); kgiDkeg2Ko.setTaxID(taxID);
			lsKgiDkeg2Kos = servKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
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
	 * 如果本类是人类等注释全面物种的KG，那么这个存储本身以及blast的query物种信息，也就是mapping到query物种的entry
	 */
	private HashSet<KGentry> hashKegEntities = new HashSet<KGentry>();
	/**
	 * 输入blast到的copedIDs，可以是多个
	 * 返回最后的KGentry结果，包括没有blast的结果
	 * @param lscopedIDs 如果没有blast，就不输入该项，设置为null即可
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		getKgGentries();
		if (ls_keggInfo != null && ls_keggInfo.size() > 0) {
			for (KeggInfoInter keggInfo : ls_keggInfo) {
				getBlastQInfo(keggInfo.getKegID(), keggInfo.getTaxID());
			}
		}
		ArrayList<KGpathway> lsKGpathways = new ArrayList<KGpathway>();
		if (hashKegEntities != null) {
			lsKGpathways = getLsKegPath(hashKegEntities);
		}
		return lsKGpathways;
	}
	
	/**
	 * 获得该accID对应的所有不重复的keggpathway对象
	 * <b>不进行blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath() {
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
			KGpathway kGpathway = getHashKGpath().get(kGentry.getPathName());//DaoKPathway.queryKGpathway(kGpathwayQ);
			hashPath.put(kGpathway.getMapNum(), kGpathway);
		}
		return ArrayOperate.getArrayListValue(hashPath);
	}
	
	/**
	 * 将通过blast获得的KO list放入，获得本物种相应的KGentry list
	 * 实际就是用另一个copedID的KeggInfo的lsKO放入其中
	 * 如果没有就返回null
	 * accID需要将其覆盖，因为理论上accID只有希望对应component
	 */
	private void getBlastQInfo(String kegIDS, int taxIDS) {
		if (kegIDS == null || taxIDS == 0) {
			return;
		}
		ArrayList<KGentry> lsQKegEntities = new ArrayList<KGentry>();
		KGentry kGentry = new KGentry(); kGentry.setEntryName(keggID); kGentry.setTaxID(taxID);
		ArrayList<KGentry> lskGentriesTmp = KGentry.getLsEntity(kGentry);
		if (lskGentriesTmp == null || lskGentriesTmp.size() < 1 ) {
			return;
		}
		lsQKegEntities.addAll(lskGentriesTmp);


		if (lsQKegEntities.size() < 1 ) {
			return;
		}
		for (KGentry kGentry2 : lsQKegEntities) {
			if (!hashKegEntities.contains(kGentry2)) {
				hashKegEntities.add(kGentry2);
			}
		}
	}

	/**
	 * 还有做Relation方面的工作
	 */
//	KGrelation
	
	
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, KGpathway> hashKGPath = new HashMap<String, KGpathway>();
	
	/**
	 * 将所有pathway信息提取出来放入hash表中，方便查找
	 * 存储pathway2Term的信息
	 * key:GoID
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * 如果已经查过了一次，自动返回
	 */
	public static HashMap<String, KGpathway> getHashKGpath() {
		ServKPathway servKPathway = new ServKPathway();
		if (hashKGPath != null && hashKGPath.size() > 0) {
			return hashKGPath;
		}
		KGpathway kGpathway = new KGpathway();
		ArrayList<KGpathway> lsKGpathways = servKPathway.queryLsKGpathways(kGpathway);
		for (KGpathway kGpathway2 : lsKGpathways) 
		{
			hashKGPath.put(kGpathway2.getPathName(), kGpathway2);
			hashKGPath.put("PATH:" + kGpathway2.getMapNum(), kGpathway2);	
		}
		return hashKGPath;
	}
	
}
