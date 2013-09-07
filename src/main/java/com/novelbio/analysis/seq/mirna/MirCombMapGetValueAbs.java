package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class MirCombMapGetValueAbs {
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combValue(Map<String, Map<String, Double>> mapPrefix_2_ID2Value) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		//前几行是序列信息，后面开始是prefix
		ArrayList<String> lstitle = getTitlePre(mapPrefix_2_ID2Value);
		lsResult.add(lstitle.toArray(new String[1]));
		
		HashSet<String> setID = getAllName(mapPrefix_2_ID2Value);
		
		for (String id : setID) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			fillMataInfo(id, lsTmpResult);
			for (int i = getTitleIDAndInfo().length; i < lstitle.size(); i++) {
				String prefix = lstitle.get(i);
				Map<String, Double> mapMirna2Value = mapPrefix_2_ID2Value.get(prefix);
				Double value = mapMirna2Value.get(id);
				if (value == null) {
					lsTmpResult.add(0 + "");
				} else {
					lsTmpResult.add(value.intValue() + "");
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[1]));
		}
		return lsResult;
	}
	
	/** 返回涉及到的所有miRNA的名字 */
	private ArrayList<String> getTitlePre(Map<String, ? extends Object> mapPrefix2Info) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		for (String string : getTitleIDAndInfo()) {
			lsTitle.add(string);
		}
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			lsTitle.add(prefix);
		}
		return lsTitle;
	}
	/** 返回文本的前几列的title，譬如TitleFormatNBC.RfamID.toString()， sequence等 */
	protected abstract String[] getTitleIDAndInfo();
	
	/** 给输入的ID添加指定的一些信息，注意务必和title对应 */
	protected abstract void fillMataInfo(String id, ArrayList<String> lsTmpResult);
	
	/** 返回涉及到的所有miRNA的名字 */
	private HashSet<String> getAllName(Map<String, Map<String, Double>> mapPrefix2_mapMiRNA2Value) {
		LinkedHashSet<String> setMirNameAll = new LinkedHashSet<String>();
		for (Map<String, Double> mapMiRNA2Value : mapPrefix2_mapMiRNA2Value.values()) {
			for (String miRNAname : mapMiRNA2Value.keySet()) {
				setMirNameAll.add(miRNAname);
			}
		}
		return setMirNameAll;
	}
}
