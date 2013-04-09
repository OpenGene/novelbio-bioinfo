package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
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
	
	ManageBlastInfo manageBlastInfo = new ManageBlastInfo();

	Map<Integer, Map<String, BlastInfo>> mapSubTaxID2_Key2BlastInfo = new HashMap<Integer, Map<String,BlastInfo>>();
	List<BlastInfo> lsUpdate = new ArrayList<BlastInfo>();
	
	/**
	 * @param genUniID
	 * @param taxID 物种ID不能为0
	 */
	public BlastList(String genUniID, int taxID) {
		this.genUniID = genUniID;
		this.taxID = taxID;
	}
	
	public List<BlastInfo> getBlastInfo(int... taxID) {
		List<BlastInfo> lsResult = new ArrayList<BlastInfo>();
 		for (int i : taxID) {
			Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(taxID);
			if (mapKey2BlastInfo == null) {
				continue;
			}
			lsResult.addAll(mapKey2BlastInfo.values());
		}
 		return lsResult;
	}
	
	public List<BlastInfo> getBlastInfo(List<Integer> lsTaxID) {
		List<BlastInfo> lsResult = new ArrayList<BlastInfo>();
 		for (int i : lsTaxID) {
			Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(taxID);
			if (mapKey2BlastInfo == null) {
				continue;
			}
			lsResult.addAll(mapKey2BlastInfo.values());
		}
 		return lsResult;
	}
	
	protected void searchBlastInfo() {
		List<BlastInfo> lsBlastInfos = manageBlastInfo.queryBlastInfoLs(genUniID, taxID);
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return;
		}
		
		for (BlastInfo blastInfo : lsBlastInfos) {
			int taxID = blastInfo.getSubjectTax();
			Map<String, BlastInfo> mapKey2BlastInfo = getMapKey2BlastInfo(taxID);
			String key = blastInfo.getQueryID() + SepSign.SEP_INFO + blastInfo.getQueryTax() + 
					SepSign.SEP_ID + blastInfo.getSubjectID() + SepSign.SEP_INFO + blastInfo.getSubjectTax();
			mapKey2BlastInfo.put(key, blastInfo);
		}
	}
	
	private Map<String, BlastInfo> getMapKey2BlastInfo(int taxid) {
		Map<String, BlastInfo> mapKey2BlastInfo = mapSubTaxID2_Key2BlastInfo.get(taxid);
		if (mapKey2BlastInfo == null) {
			mapKey2BlastInfo = new HashMap<String, BlastInfo>();
			mapSubTaxID2_Key2BlastInfo.put(taxid, mapKey2BlastInfo);
		}
		return mapKey2BlastInfo;
	}
	
	public 
	
	
}
