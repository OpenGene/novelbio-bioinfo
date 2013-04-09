package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.SepSign;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

/** 处理blast信息的类 */
public class BlastList {
	String genUniID;
	int taxID;
	double evalueCutoff = 1e-10;
	/** 是否一个物种仅选取一条序列 */
	boolean isGetOneSeqPerTaxID = true;
	
	List<Integer> lsSTaxID = new ArrayList<Integer>();
	
	ManageBlastInfo manageBlastInfo = new ManageBlastInfo();

	Map<Integer, Map<String, BlastInfo>> mapSubTaxID2_Key2BlastInfo = new HashMap<Integer, Map<String,BlastInfo>>();
	List<BlastInfo> lsUpdate = new ArrayList<BlastInfo>();
	
	/**
	 * 一旦初始化就会进行查找，所以在外面最好进行延迟初始
	 * @param genUniID
	 * @param taxID 物种ID不能为0
	 */
	public BlastList(String genUniID, int taxID) {
		this.genUniID = genUniID;
		this.taxID = taxID;
		searchBlastInfo();
	}
	/**
	 * 设定需要比对到的物种，null或者不输入表示选择全体
	 * @param taxID
	 */
	public void setTaxID(List<Integer> lsStaxID) {
		this.lsSTaxID.clear();
		if (lsStaxID == null || lsStaxID.size() == 0) {
			return;
		}
		this.lsSTaxID = lsStaxID;
	}
	/**
	 * 设定需要比对到的物种，null或者不输入表示选择全体
	 * @param taxID
	 */
	public void setTaxID(int... taxID) {
		this.lsSTaxID.clear();
		if (taxID == null || taxID.length == 0) {
			return;
		}
		for (int i : taxID) {
			lsSTaxID.add(i);
		}
	}
	
	/** 设定一个物种选择一个blast到的gene还是多个blast到的gene */
	public void setGetOneSeqPerTaxID(boolean isGetOneSeqPerTaxID) {
		this.isGetOneSeqPerTaxID = isGetOneSeqPerTaxID;
	}
	
	/** 小于0就走默认，默认1e-10 */
	public void setEvalue(double evalueCutoff) {
		if (evalueCutoff < 0) {
			return;
		}
		this.evalueCutoff = evalueCutoff;
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
		List<BlastInfo> lsBlastInfos = manageBlastInfo.queryBlastInfoLs(genUniID, taxID);
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return;
		}
		
		for (BlastInfo blastInfo : lsBlastInfos) {
			int taxID = blastInfo.getSubjectTax();
			Map<String, BlastInfo> mapKey2BlastInfo = getMapKey2BlastInfo(taxID);
			mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
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
	public void addBlastInfo(BlastInfo blastInfo) {
		Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(blastInfo.getQueryTax());
		if (mapKey2BlastInfo != null && mapKey2BlastInfo.containsKey(getKey(blastInfo))) {
			BlastInfo blastInfoSub = mapKey2BlastInfo.get(getKey(blastInfo));
			if (blastInfoSub.compareTo(blastInfo) == -1) {
				blastInfo.setId(blastInfoSub.getId());
				mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
				lsUpdate.add(blastInfo);
			}
		} else {
			mapKey2BlastInfo = getMapKey2BlastInfo(blastInfo.getQueryTax());
			mapKey2BlastInfo.put(getKey(blastInfo), blastInfo);
			lsUpdate.add(blastInfo);
		}
	}
	
	public void update() {
		for (BlastInfo blastInfo : lsUpdate) {
			manageBlastInfo.save(blastInfo);
		}
		lsUpdate.clear();
	}
	
	private static String getKey(BlastInfo blastInfo) {
		String key = blastInfo.getQueryID() + SepSign.SEP_INFO + blastInfo.getQueryTax() + 
				SepSign.SEP_ID + blastInfo.getSubjectID() + SepSign.SEP_INFO + blastInfo.getSubjectTax();
		return key;
	}
	
}
