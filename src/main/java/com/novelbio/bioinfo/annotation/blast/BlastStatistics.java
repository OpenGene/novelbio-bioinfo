package com.novelbio.bioinfo.annotation.blast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.base.freqcount.HistBin;
import com.novelbio.bioinfo.base.freqcount.HistList;
import com.novelbio.bioinfo.base.freqcount.HistList.HistBinType;
import com.novelbio.database.model.geneanno.BlastInfo;

public class BlastStatistics {
	/** 进行blast的所有序列的名字 */
	Set<String> setQueryAllNames = new HashSet<String>();
	Map<String, BlastInfo> mapQueryID2Blastinfos;
	
	int evalueStep = 20;
	int identityStep = 10;
	
	/**
	 * 设定并计算，会删除重复的queryID
	 * @param lsBlastinfos
	 */
	public void setLsBlastinfos(List<BlastInfo> lsBlastinfos) {
		mapQueryID2Blastinfos = new HashMap<String, BlastInfo>();
		for (BlastInfo blastInfo : lsBlastinfos) {
			if (mapQueryID2Blastinfos.containsKey(blastInfo.getQueryID())) {
				BlastInfo blastInfoLast = mapQueryID2Blastinfos.get(blastInfo.getQueryID());
				if (blastInfoLast.compareTo(blastInfo) < 0) {
					continue;
				}
			}
			mapQueryID2Blastinfos.put(blastInfo.getQueryID(), blastInfo);
		}
	}
	
	/** 必须是simple版本的blast才能被读取 */
	public void setBlastResultFile(String blastFile) {
		List<BlastInfo> lsBlastInfo = new ArrayList<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(blastFile);
		for (String string : txtRead.readlines()) {
			BlastInfo blastInfo = new BlastInfo(string);
			lsBlastInfo.add(blastInfo);
		}
		setLsBlastinfos(lsBlastInfo);
		txtRead.close();
	}
	
	public void setQueryFastaFile(String queryFastaFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(queryFastaFile);
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith(">")) {
				setQueryAllNames.add(string.replace(">", "").split(" ")[0]);
			}
		}
		txtRead.close();
	}
	
	/** evalue统计 */
	public HistList getHistEvalue() {
		HistList hListEvalue = HistList.creatHistList("Evalue", true);
		hListEvalue.setStartBin(10, -5 +"to" + -10, 5, 10);
		int numEvalue = 10 + evalueStep;
		for ( ; numEvalue < 200; numEvalue += evalueStep) {
			hListEvalue.addHistBin(numEvalue, -(numEvalue-evalueStep) +"to" + -numEvalue, numEvalue);
		}
		hListEvalue.addHistBin(numEvalue, -(numEvalue-evalueStep) +"to" + 0, 400);
		
		HistBin histBinThis = new HistBin(0.0);
		histBinThis.setName("NoHit");
		histBinThis.setStartCis(-10);
		histBinThis.setEndCis(evalueStep);
		histBinThis.setParentName(hListEvalue.getName());
		hListEvalue.add(0, histBinThis);
		hListEvalue.setHistBinType(HistBinType.LcloseRopen);
		for (BlastInfo blastInfo : mapQueryID2Blastinfos.values()) {
			if (blastInfo.getEvalue() == 0) {
				hListEvalue.addNum(200);
			} else {
				hListEvalue.addNum((int)(-Math.log10(blastInfo.getEvalue()) + 0.99));
			}
		}
		
		for (String names : setQueryAllNames) {
			if (!mapQueryID2Blastinfos.containsKey(names)) {
				hListEvalue.addNum(-9);
			}
		}
		return hListEvalue;
	}
	
	/** identity统计 
	 * @return */
	public HistList getHistIdentity() {
		HistList hListIdentity =  HistList.creatHistList("Identity", true);
		hListIdentity.setStartBin(identityStep*3, (double)identityStep*3/100 +"to" + (double)identityStep*4/100, identityStep*3, identityStep*4);
		int numIdentity = identityStep*5;
		for (; numIdentity < 100; numIdentity += identityStep) {
			hListIdentity.addHistBin(numIdentity, (double)(numIdentity-identityStep)/100 +"to" + ((double)numIdentity/100), numIdentity);
		}
		hListIdentity.addHistBin(numIdentity, (double)(numIdentity-identityStep)/100 +"to" + 1, 400);
		
		HistBin histBinThis = new HistBin(0.0);
		histBinThis.setName("NoHit");
		histBinThis.setStartCis(-10);
		histBinThis.setEndCis(0);
		histBinThis.setParentName(hListIdentity.getName());
		hListIdentity.add(0, histBinThis);
		hListIdentity.setHistBinType(HistBinType.LcloseRopen);
		for (BlastInfo blastInfo : mapQueryID2Blastinfos.values()) {
			hListIdentity.addNum((int) blastInfo.getIdentities());
		}
		for (String names : setQueryAllNames) {
			if (!mapQueryID2Blastinfos.containsKey(names)) {
				hListIdentity.addNum(-9);
			}
		}
		return hListIdentity;
	}

}
