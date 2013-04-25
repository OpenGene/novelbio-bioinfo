package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

/** 处理blast信息的类 */
public class BlastList {
	int idType;
	String genUniID;
	int taxID;
	double evalueCutoff = 1e-10;
	/** 是否一个物种仅选取一条序列 */
	boolean isGetOneSeqPerTaxID = true;
	
	Set<Integer> lsSTaxID = new HashSet<Integer>();
	
	ManageBlastInfo manageBlastInfo = new ManageBlastInfo();

	Map<Integer, Map<String, BlastInfo>> mapSubTaxID2_Key2BlastInfo = new HashMap<Integer, Map<String,BlastInfo>>();
	/** 待升级的BlastInfo */
	Map<String, BlastInfo> mapUpdate = new HashMap<String, BlastInfo>();
	/** 比对到的geneID */
	List<GeneID> lsBlastGeneID;
	
	/**
	 * 一旦初始化就会进行查找，所以在外面最好进行延迟初始
	 * @param genUniID
	 * @param taxID 物种ID不能为0
	 */
	public BlastList(int idType, String genUniID, int taxID) {
		this.idType = idType;
		this.genUniID = genUniID;
		this.taxID = taxID;
		searchBlastInfo();
	}
	
	public String getGenUniID() {
		return genUniID;
	}
	protected void setGeneInfo(int idType, String genUniID, int taxID) {
		this.idType = idType;
		this.genUniID = genUniID;
		this.taxID = taxID;
		searchBlastInfo();
	}
	public int getIdType() {
		return idType;
	}
	
	/**
	 * 设定需要比对到的物种，null或者不输入表示选择全体
	 * @param taxIDfile
	 */
	public void setTaxIDBlastTo(List<Integer> lsStaxID) {
		this.lsSTaxID.clear();
		if (lsStaxID == null || lsStaxID.size() == 0) {
			return;
		}
		this.lsSTaxID = new HashSet<Integer>(lsStaxID);
		lsBlastGeneID = null;
	}
	/**
	 * 设定需要比对到的物种，null或者不输入表示选择全体
	 * @param taxID
	 */
	public void setTaxIDBlastTo(int... taxID) {
		this.lsSTaxID.clear();
		if (taxID == null || taxID.length == 0) {
			return;
		}
		for (int i : taxID) {
			lsSTaxID.add(i);
		}
		lsBlastGeneID = null;
	}
	
	/**
	 * 设定参数
	 * @param evalueCutoff 小于0就走默认，默认1e-10 
	 * @param isGetOneSeqPerTaxID  设定一个物种选择一个blast到的gene还是多个blast到的gene
	 */
	public void setEvalue_And_GetOneSeqPerTaxID(double evalueCutoff, boolean isGetOneSeqPerTaxID) {
		this.isGetOneSeqPerTaxID = isGetOneSeqPerTaxID;
		if (evalueCutoff < 0) {
			return;
		}
		this.evalueCutoff = evalueCutoff;
		lsBlastGeneID = null;
	}
	
	/**
	 * 没有则返回空的list
	 * @return
	 */
	public List<GeneID> getLsBlastGeneID() {
		if (lsBlastGeneID != null) {
			return lsBlastGeneID;
		}
		lsBlastGeneID = new ArrayList<GeneID>();
		for (BlastInfo blastInfo : getBlastInfo()) {
			GeneID geneID = getBlastGeneID(blastInfo);
			if (geneID != null) {
				lsBlastGeneID.add(geneID);
			}
		}
		return lsBlastGeneID;
	}

	/**
	 * 获得设定的第一个blast的对象，首先要设定blast的目标
	 * @param blastInfo
	 * @return
	 */
	private GeneID getBlastGeneID(BlastInfo blastInfo) {
		if (blastInfo == null) return null;
		
		GeneID geneID = blastInfo.getGeneIDS();
		return geneID;
	}
	
	protected List<BlastInfo> getBlastInfoAll() {
		List<BlastInfo> lsResult = new ArrayList<BlastInfo>();
		for (Map<String, BlastInfo> mapKey2BlastInfo : mapSubTaxID2_Key2BlastInfo.values()) {
			lsResult.addAll(mapKey2BlastInfo.values());
		}
 		return lsResult;
	}
	
	public List<BlastInfo> getBlastInfo() {
		List<BlastInfo> lsResult = new ArrayList<BlastInfo>();
		if (lsSTaxID.size() == 0) {
			for (Map<String, BlastInfo> mapKey2BlastInfo : mapSubTaxID2_Key2BlastInfo.values()) {
				addList(lsResult, mapKey2BlastInfo);
			}
		} else {
			for (int taxid : lsSTaxID) {
				Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(taxid);
				addList(lsResult, mapKey2BlastInfo);
			}
		}
 		return lsResult;
	}
	
	/** 每次只添加一个物种 */
	private void addList(List<BlastInfo> lsOut, Map<String, BlastInfo> mapKey2BlastInfo) {
		if (mapKey2BlastInfo == null || mapKey2BlastInfo.size() == 0) {
			return;
		}
		List<BlastInfo> lsBlastInfos = new ArrayList<BlastInfo>();
		for (BlastInfo blastInfo : mapKey2BlastInfo.values()) {
			if (blastInfo.getEvalue() <= evalueCutoff) {
				lsBlastInfos.add(blastInfo);
			}
		}
		if (lsBlastInfos.size() <= 0) {
			return;
		}
		//对于单个物种，是选择最相似那个ID，还是选择全部
		if (isGetOneSeqPerTaxID) {
			Collections.sort(lsBlastInfos);
			lsOut.add(lsBlastInfos.get(0));
		} else {
			lsOut.addAll(lsBlastInfos);
		}
	}
	
	protected void searchBlastInfo() {
		mapSubTaxID2_Key2BlastInfo.clear();
		List<BlastInfo> lsBlastInfos = manageBlastInfo.queryBlastInfoLs(genUniID, taxID);
		
		for (BlastInfo blastInfo : lsBlastInfos) {
			int taxID = blastInfo.getSubjectTax();
			Map<String, BlastInfo> mapKey2BlastInfo = getMapKey2BlastInfo(taxID);
			mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
		}
		//重新设定需要升级的BlastInfo
		List<BlastInfo> lsUpdate = new ArrayList<BlastInfo>(mapUpdate.values());
		mapUpdate.clear();
		for (BlastInfo blastInfo : lsUpdate) {
			addBlastInfoNew(blastInfo);
		}
	}
	
	/**
	 * 如果存在则返回该map
	 * 如果不存在则新建一个然后返回
	 * @param taxid
	 * @return
	 */
	private Map<String, BlastInfo> getMapKey2BlastInfo(int taxid) {
		Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(taxid);
		if (mapKey2BlastInfo == null) {
			mapKey2BlastInfo = new HashMap<String, BlastInfo>();
			mapSubTaxID2_Key2BlastInfo.put(taxid, mapKey2BlastInfo);
		}
		return mapKey2BlastInfo;
	}
	
	/**
	 * 添加新的，需要升级的BlastInfo
	 * @param blastInfo
	 */
	public void addBlastInfoNew(BlastInfo blastInfo) {
		blastInfo.setGeneIDQ(genUniID, taxID, idType);
		Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(blastInfo.getSubjectTax());
		if (mapKey2BlastInfo != null && mapKey2BlastInfo.containsKey(getKey(blastInfo))) {
			BlastInfo blastInfoSub = mapKey2BlastInfo.get(getKey(blastInfo));
			if (blastInfo.compareTo(blastInfoSub) == -1) {
				blastInfo.setId(blastInfoSub.getId());
				mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
				mapUpdate.put(getKey(blastInfo), blastInfo);
			}
		} else {
			mapKey2BlastInfo = getMapKey2BlastInfo(blastInfo.getSubjectTax());
			mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
			mapUpdate.put(getKey(blastInfo), blastInfo);
		}
	}
	
	public void update() {
		for (BlastInfo blastInfo : mapUpdate.values()) {
			manageBlastInfo.save(blastInfo);
		}
		mapUpdate.clear();
	}
	
	private static String getKey(BlastInfo blastInfo) {
		String key = blastInfo.getSubjectID() + SepSign.SEP_INFO + blastInfo.getSubjectTax();
		return key;
	}
	
}
