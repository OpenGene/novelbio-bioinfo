package com.novelbio.analysis.seq.fasta;

import java.util.LinkedHashMap;
import java.util.Map;

import com.novelbio.listOperate.HistList;

/**
 * 读取一个列表，按照指定的window输出染色体上的密度信息
 * 如画出snp在染色体上的分布密度图，哪个区域snp多，哪个区域snp少之类的
 * @author zong0jie
 *
 */
public class ChrDensity {
	/** key 为小写 */
	Map<String, HistList> mapChr2His = new LinkedHashMap<>();
	
	/**
	 * @param mapChr2Len
	 * key 染色体名字<br>
	 * value 染色体长度
	 * @param 区段长度
	 */
	public ChrDensity(Map<String, Long> mapChr2Len, int binLen) {
		for (String chrId : mapChr2Len.keySet()) {
			long chrLen = mapChr2Len.get(chrId);
			HistList histList = HistList.creatHistList(chrId, true);
			histList.setBinAndInterval((int)(Math.ceil((double)chrLen/binLen)), binLen);
			mapChr2His.put(chrId.toLowerCase(), histList);
		}
	}
	
	public void addSite(String chrId, int start) {
		HistList histList = mapChr2His.get(chrId);
		histList.addNum(start);
	}
	
	public Map<String, HistList> getMapChr2His() {
		return mapChr2His;
	}
}
