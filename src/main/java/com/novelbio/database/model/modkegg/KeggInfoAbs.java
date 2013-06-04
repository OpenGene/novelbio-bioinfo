package com.novelbio.database.model.modkegg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.service.servkegg.ServKIDKeg2Ko;
import com.novelbio.database.service.servkegg.ServKPathway;

public abstract class KeggInfoAbs implements KeggInfoInter{
	/**
	 * 存储KO2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, KGpathway> hashKGPath = new HashMap<String, KGpathway>();
	
	ServKIDKeg2Ko servKIDKeg2Ko = new ServKIDKeg2Ko();
	/**
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	protected String genUniAccID = "";
	protected String keggID = null;
	
	protected int taxID;
	/**
	 * 是否已经装载过lskGentries了
	 */
	boolean boolskGentries = false;
	private ArrayList<KGentry> lskGentries = null;
	
	boolean boolsKgiDkeg2Kos = false;
	
	/** kegID2KO的对照表 */
	ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = null;
	/**
	 * 如果本类是人类等注释全面物种的KG，那么这个存储本身以及blast的query物种信息，也就是mapping到query物种的entry
	 */
//	private HashSet<KGentry> hashKegEntities = new HashSet<KGentry>();
	
	public KeggInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	/**
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	public String getGenUniID() {
		return this.genUniAccID;
	}
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

	public int getTaxID() {
		return this.taxID;
	}

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
		Set<KGentry> hashKegEntities = new HashSet<KGentry>();
		for (KGentry kGentry : lskGentries) {
			if (!hashKegEntities.contains(kGentry)) {
				hashKegEntities.add(kGentry);
			}
		}
		return lskGentries;
	}

	/**
	 * 获得该keggID所对应的KO<br>
	 * 如果没有就返回null<br>
	 * accID需要将其覆盖，因为理论上accID只有希望对应component
	 */
	public ArrayList<String> getLsKo() {
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
	 * 获得该accID对应的所有不重复的keggpathway对象
	 * <b>不进行blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath() {
		 getKgGentries();
		ArrayList<KGpathway> lsKGpathways = getLsKegPath(lskGentries, taxID);
		return lsKGpathways;
	}
	
	/**
	 * 输入blast到的CopedIDs
	 * 返回该geneID所对应的KGentry
	 * @return
	 */
	public static ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		ArrayList<KGentry> lsGentries = new ArrayList<KGentry>();
		if (ls_keggInfo != null && ls_keggInfo.size() > 0) {
			for (KeggInfoInter keggInfo : ls_keggInfo) {
				lsGentries.addAll(getBlastQInfo(keggInfo.getKegID(), keggInfo.getTaxID()));
			}
		}
		return lsGentries;
	}
	
	/**
	 * 输入blast到的copedIDs，可以是多个<br>
	 * 如果Sub物种含有Query物种所不存在的pathway，则该pathway会以Sub物种的形式返回<br>
	 * @return
	 */
	public static ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo, int taxID) {
		List<KGentry> lsKGentries = new ArrayList<KGentry>();
		if (ls_keggInfo != null && ls_keggInfo.size() > 0) {
			for (KeggInfoInter keggInfo : ls_keggInfo) {
				lsKGentries.addAll(keggInfo.getKgGentries());
			}
		}
		ArrayList<KGpathway> lsKGpathways = getLsKegPath(lsKGentries, taxID);
		return lsKGpathways;
	}

	/**
	 * 如果Sub物种含有Query物种所不存在的pathway，则该pathway会以Sub物种的形式返回
	 * @param lsKGentries
	 * @return
	 */
	private static ArrayList<KGpathway> getLsKegPath(Collection<KGentry> lsKGentries, int taxID) {
		if (lsKGentries == null) {
			return null;
		}
		//用来去冗余，根据pathName进行去冗余
		HashMap<String, KGpathway> hashPath = new HashMap<String, KGpathway>();
		for (KGentry kGentry : lsKGentries) {
			KGpathway kGpathway = getHashKGpath().get(kGentry.getPathName());//DaoKPathway.queryKGpathway(kGpathwayQ);
			KGpathway kGpathwayResult = getKGpath(kGpathway.getMapNum(), taxID);
			if (kGpathwayResult == null) {
				kGpathwayResult = kGpathway;
			}
			
			hashPath.put(kGpathwayResult.getMapNum(), kGpathwayResult);
		}
		return ArrayOperate.getArrayListValue(hashPath);
	}
	
	private static List<KGentry> getBlastQInfo(String kegIDS, int taxIDS) {
		if (kegIDS == null || taxIDS == 0) {
			return new ArrayList<KGentry>();
		}
		KGentry kGentry = new KGentry(); kGentry.setEntryName(kegIDS); kGentry.setTaxID(taxIDS);
		List<KGentry> lsQKegEntities = KGentry.getLsEntity(kGentry);
		if (lsQKegEntities == null) {
			return new ArrayList<KGentry>();
		}
		return lsQKegEntities;
	}

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
		for (KGpathway kGpathway2 : lsKGpathways) {
			hashKGPath.put(kGpathway2.getPathName(), kGpathway2);
			hashKGPath.put(kGpathway2.getMapNum(), kGpathway2);	
			hashKGPath.put(kGpathway2.getMapNum() + SepSign.SEP_ID + kGpathway2.getTaxID(), kGpathway2);
		}
		return hashKGPath;
	}
	
	/**
	 * 将所有pathway信息提取出来放入hash表中，方便查找
	 * 存储pathway2Term的信息
	 * key:GoID
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * 如果已经查过了一次，自动返回
	 */
	public static KGpathway getKGpath(String MapNum, int taxID) {
		return getHashKGpath().get(MapNum + SepSign.SEP_ID + taxID);
	}
}
