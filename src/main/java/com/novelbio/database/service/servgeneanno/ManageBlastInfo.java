package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mongorepo.geneanno.RepoBlastInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageBlastInfo {
	static double[] lock = new double[0];
	/** 缓存, key都为小写*/
	static Map<String, Map<String, BlastInfo>> mapAccIDTaxID_2_mapAccID;
	/** key为小写 */
	static Map<String, Integer> mapAccID2TaxID;

	@Autowired
	RepoBlastInfo repoBlastInfo;

	public ManageBlastInfo() {
		repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
	}
	
	public static void clearBlastCach() {
		if (mapAccIDTaxID_2_mapAccID != null) {
			mapAccIDTaxID_2_mapAccID.clear();
		}
	}
	
	/** 一行一行的添加信息*/
	public static void addBlastLine(int taxIDQ, int taxIDS, String blastLine, boolean isBlastFormat) {
		BlastInfo blastInfo = new BlastInfo(taxIDQ, taxIDS, blastLine, isBlastFormat);
		addBlastInfoToCache(blastInfo);
	}
	
	/**
	 * @param taxIDQ
	 * @param taxIDS
	 * @param blastFile
	 * @param isBlastFormat 如果subjecdt是accID，具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public static void readBlastFile(int taxIDQ, int taxIDS, String blastFile, boolean isBlastFormat) {
		TxtReadandWrite txtRead = new TxtReadandWrite(blastFile);
		for (String content : txtRead.readlines()) {
			BlastInfo blastInfo = new BlastInfo(taxIDQ, taxIDS, content, isBlastFormat);
			addBlastInfoToCache(blastInfo);
		}
		txtRead.close();
	}
	
	/** 添加单个BlastInfo进入缓存 */
	public static void addBlastInfoToCache(BlastInfo blastInfo) {
		if (mapAccID2TaxID == null) {
			mapAccID2TaxID = new HashMap<String, Integer>();
		}
		if (mapAccIDTaxID_2_mapAccID == null) {
			mapAccIDTaxID_2_mapAccID = new HashMap<String, Map<String,BlastInfo>>();
		}
		
		String key1 = getKey1(blastInfo);
		Map<String, BlastInfo> mapAccIDTaxIDSaccIDStaxID_2_BlastInfo = mapAccIDTaxID_2_mapAccID.get(key1);
		mapAccID2TaxID.put(blastInfo.getQueryID().toLowerCase(), blastInfo.getQueryTax());
		if (mapAccIDTaxIDSaccIDStaxID_2_BlastInfo == null) {
			mapAccIDTaxIDSaccIDStaxID_2_BlastInfo = new HashMap<String, BlastInfo>();
			mapAccIDTaxIDSaccIDStaxID_2_BlastInfo.put(getKey2(blastInfo), blastInfo);
			mapAccIDTaxID_2_mapAccID.put(getKey1(blastInfo), mapAccIDTaxIDSaccIDStaxID_2_BlastInfo);
		} else {
			BlastInfo blastInfoS = mapAccIDTaxIDSaccIDStaxID_2_BlastInfo.get(getKey2(blastInfo));
			if (blastInfoS == null || blastInfoS.compareTo(blastInfo) == 1) {
				mapAccIDTaxIDSaccIDStaxID_2_BlastInfo.put(getKey2(blastInfo), blastInfo);
			}
		}
	}
	
	private static String getKey1(BlastInfo blastInfo) {
		return (blastInfo.getQueryID() + SepSign.SEP_ID + blastInfo.getQueryTax()).toLowerCase();
	}
	
	private static String getKey2(BlastInfo blastInfo) {
		return (blastInfo.getQueryID() + SepSign.SEP_ID + blastInfo.getQueryTax() 
				+ SepSign.SEP_ID + blastInfo.getSubjectID()
				+ SepSign.SEP_ID + blastInfo.getSubjectTax()).toLowerCase();
	}
	
//	/**
//	 * 查找指定物种的，全部BlastInfo
//	 * @param queryID 待查找ID，一般是genUniID
//	 * @param taxIDQ query物种ID
//	 * @param taxIDS blast到的物种ID
//	 * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
//	 * @return
//	 */
//	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ, int taxIDS) {
//		return repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
//	}
//	
//	/**
//	 * 查找指定物种的，符合条件的第一个BlastInfo
//	 * @param queryID 待查找ID，一般是genUniID
//	 * @param taxIDQ query物种ID
//	 * @param taxIDS blast到的物种ID
//d * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
//	 * @return
//	 */
//	public BlastInfo queryBlastInfo(String queryID, int taxIDQ, int taxIDS, double evalue) {
//		List<BlastInfo> lsBlastInfos = repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
//		if (lsBlastInfos != null && lsBlastInfos.size() > 0) {
//			Collections.sort(lsBlastInfos);//排序选择最小的一项
//			BlastInfo blastInfo = lsBlastInfos.get(0);
//			if (evalue < 5 && evalue > -1 && blastInfo.getEvalue() <= evalue) {
//				return blastInfo;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 查找符合条件的List BlastInfo，已经去重复了
	 * @param queryID 待查找ID，一般是genUniID
	 * @param taxID 物种ID
	 * @return
	 */
	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ) {
		List<BlastInfo> lsBlastInfos = repoBlastInfo.findByQueryID(queryID, taxIDQ);
		if (taxIDQ == 0) {
			Integer taxIDQ1 = mapAccID2TaxID.get(queryID.toLowerCase());
			if (taxIDQ1 != null) {
				taxIDQ = taxIDQ1;
			}
		}
		String key1 = (queryID + SepSign.SEP_ID + taxIDQ).toLowerCase();
		Map<String, BlastInfo> mapValue = mapAccIDTaxID_2_mapAccID.get(key1);
		if (mapValue != null && mapValue.size() > 0) {
			lsBlastInfos.addAll(mapValue.values());
		}
		if (mapValue != null) {
			Map<String, BlastInfo> mapRemoveDuplicate = new HashMap<String, BlastInfo>();
			for (BlastInfo blastInfo : lsBlastInfos) {
				String key = getKey2(blastInfo);
				if (!mapRemoveDuplicate.containsKey(key) 
						|| (mapRemoveDuplicate.containsKey(key) && mapRemoveDuplicate.get(key).compareTo(blastInfo) == 1)) {
					mapRemoveDuplicate.put(key, blastInfo);
				}
			}
			lsBlastInfos = new ArrayList<BlastInfo>(mapRemoveDuplicate.values());
		}
		return lsBlastInfos;
	}
	
	public void save(BlastInfo blastInfo) {		
		if (blastInfo != null) {
		repoBlastInfo.save(blastInfo);
		}
	}
	
	/**
	 * 给定blastInfo的信息，如果数据库中的本物种已经有了该结果，则比较evalue，用低evalue的覆盖高evalue的
	 * 如果没有，则插入
	 * @param blastInfo
	 */
	public void updateBlast(BlastInfo blastInfo) {
		BlastInfo blastInfoQ = repoBlastInfo.findByQueryIDAndSubID(blastInfo.getQueryID(), blastInfo.getQueryTax(), 
				blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		if (blastInfoQ == null) {
			repoBlastInfo.save(blastInfo);
		} else {
			if (blastInfo.compareTo(blastInfoQ) == -1) {
				blastInfo.setId(blastInfoQ.getId());
				repoBlastInfo.save(blastInfo);
			}
		}
	}
}
