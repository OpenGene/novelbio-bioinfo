package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.SepSign;

public class TophatJunction implements AlignmentRecorder {
	/**
	 * key condition
	 * value: 某个junction与别的junction之间的对应关系
	 */
	HashMap<String, HashMultimap<String, String>> mapCond_To_mapJun1ToSetJun2 = new LinkedHashMap<String, HashMultimap<String,String>>();
	HashMultimap<String, String> mapJun1ToSetJun2;
	/**
	 * key condition--
	 * key：junction
	 * value：该junction所对应的总reads数
	 */
	HashMap<String, LinkedHashMap<String, Integer>> mapCond_To_JuncOne2AllNum = new LinkedHashMap<String, LinkedHashMap<String,Integer>>();
	LinkedHashMap<String, Integer> mapJuncOne2AllNum;
	/**
	 * key condition
	 * value: 某一对junction与其对应的reads总数
	 */
	HashMap<String, LinkedHashMap<String,Integer>>  mapCond_To_JuncPair2ReadsNum = new LinkedHashMap<String, LinkedHashMap<String,Integer>>();
	LinkedHashMap<String, Integer> mapJuncPair2ReadsNum;	
	
	/** 设定当前时期 */
	public void setCondition(String condition) {
		mapJun1ToSetJun2 = getMapJunc1ToJunc2(condition);
		mapJuncOne2AllNum = getMapJuncOneToAllNum(condition);
		mapJuncPair2ReadsNum = getMapJuncPair2ReadsNum(condition);
	}
	
	/**
	 * 获得剪接位点1对应的所有剪接位点2的map
	 * @param condition
	 * @return
	 */
	private HashMultimap<String, String> getMapJunc1ToJunc2(String condition) {
		//获得对应的hash表
		HashMultimap<String, String> tmpMapJunc1ToJunc2 = null;
		if (mapCond_To_mapJun1ToSetJun2.containsKey(condition)) {
			tmpMapJunc1ToJunc2 = mapCond_To_mapJun1ToSetJun2.get(condition);
		} else {
			tmpMapJunc1ToJunc2 = HashMultimap.create();
			mapCond_To_mapJun1ToSetJun2.put(condition, tmpMapJunc1ToJunc2);
		}
		return tmpMapJunc1ToJunc2;
	}
	
	/**	 * @param locEndSite

	 * 获得剪接位点1对应的全部readsnum
	 * @param condition
	 * @return
	 */
	private LinkedHashMap<String, Integer> getMapJuncOneToAllNum(String condition) {
		//获得对应的hash表
		LinkedHashMap<String, Integer> tmpMapJuncOne2AllNum = null;
		if (mapCond_To_JuncOne2AllNum.containsKey(condition)) {
			tmpMapJuncOne2AllNum = mapCond_To_JuncOne2AllNum.get(condition);
		} else {
			tmpMapJuncOne2AllNum = new LinkedHashMap<String, Integer>();
			mapCond_To_JuncOne2AllNum.put(condition, tmpMapJuncOne2AllNum);
		}
		return tmpMapJuncOne2AllNum;
	}
	
	/**
	 * 获得一对剪接位点所对应的全部readsnum
	 * @param condition
	 * @return
	 */
	private LinkedHashMap<String, Integer> getMapJuncPair2ReadsNum(String condition) {
		//获得对应的hash表
		LinkedHashMap<String, Integer> tmpMapJuncPair2ReadsNum = null;
		if (mapCond_To_JuncPair2ReadsNum.containsKey(condition)) {
			tmpMapJuncPair2ReadsNum = mapCond_To_JuncPair2ReadsNum.get(condition);
		} else {
			tmpMapJuncPair2ReadsNum = new LinkedHashMap<String, Integer>();
			mapCond_To_JuncPair2ReadsNum.put(condition, tmpMapJuncPair2ReadsNum);
		}
		return tmpMapJuncPair2ReadsNum;
	}
	
	/**添加samBam的文件用来获得信息 */
	public void addAlignRecord(AlignRecord alignRecord) {
		ArrayList<Align> lsAlign = alignRecord.getAlignmentBlocks();
		if (lsAlign.size() <= 1) {
			return;
		}
		int size = lsAlign.size();
		String chrID = alignRecord.getRefID();
		for (int i = 0; i < size - 1; i++) {
			Align alignThis = lsAlign.get(i);
			Align alignNext = lsAlign.get(i + 1);
			int junStart = alignThis.getEndAbs();
			int junEnd = alignNext.getStartAbs();
			addJunctionInfo(chrID, junStart, junEnd, 1);
		}
	}

	/**
	 * 读取junction文件，文件中每个剪接位点只能出现一次\
	 * @param condition
	 * @param junctionFile
	 */
	public void setJunFile(String condition, String junctionFile) {
		setCondition(condition);
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			String chrID = ss[0];
			
			//junction位点都设定在exon上
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			int junctionNum = Integer.parseInt(ss[4]);
			addJunctionInfo(chrID, junct1, junct2, junctionNum);
		}
	}
	/** 
	 * 添加单个剪接位点reads
	 * @param chrID 染色体
	 * @param junctionStart 剪接起点
	 * @param junctionEnd 剪接终点
	 * @param junctionNum 剪接reads的数量
	 */
	private void addJunctionInfo(String chrID, int junctionStart, int junctionEnd, int junctionNum) {
		chrID = chrID.toLowerCase();
		int junctionStartmin = Math.min(junctionStart, junctionEnd);
		int junctionEndmax = Math.max(junctionStart, junctionEnd);
		String strjunct1 = chrID + SepSign.SEP_INFO_SAMEDB + junctionStartmin;
		String strjunct2 = chrID + SepSign.SEP_INFO_SAMEDB + junctionEndmax;
		String strJunBoth = strjunct1 + SepSign.SEP_INFO + strjunct2;
		
		mapJun1ToSetJun2.put(strjunct1, strjunct2);
		addJunctionNum(mapJuncOne2AllNum, strjunct1, junctionNum);
		addJunctionNum(mapJuncOne2AllNum, strjunct2, junctionNum);
		addJunctionNum(mapJuncPair2ReadsNum, strJunBoth, junctionNum);
	}
	
	/** 向指定的map中添加junction reads */
	private void addJunctionNum(HashMap<String, Integer> mapJunc2Num, String junc, int junctionNum) {
		if (mapJunc2Num.containsKey(junc)) {
			int juncAllNum = mapJunc2Num.get(junc);
			juncAllNum = juncAllNum + junctionNum;
			mapJunc2Num.put(junc, juncAllNum);
		} else {
			mapJunc2Num.put(junc, junctionNum);
		}
	}
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String condition, String chrID, int locSite) {
		setCondition(condition);
		String junc = chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locSite;
		Integer num = mapJuncOne2AllNum.get(junc);
		if (num == null) {
			num = 0;
		}
		return num;
	}
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public int getJunctionSite(String condition, String chrID, int locStartSite, int locEndSite) {
		setCondition(condition);
		chrID = chrID.toLowerCase();
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID + SepSign.SEP_INFO_SAMEDB + locS + SepSign.SEP_INFO +chrID + SepSign.SEP_INFO_SAMEDB + locE;
		int resultNum = 0;
		if (mapJuncPair2ReadsNum.containsKey(key)) {
			resultNum = mapJuncPair2ReadsNum.get(key);
		}
		return resultNum;
	}
	
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locStartSite, int locEndSite, String condition) {
		setCondition(condition);
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locS + SepSign.SEP_INFO +chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locE;
		if (mapJuncPair2ReadsNum.containsKey(key) ) {
			return mapJuncPair2ReadsNum.get(key);
		}
		else {
			return 0;
		}
	}
	
	public void writeTo(String condition, String outPathPrefix) {
		setCondition(condition);
		TxtReadandWrite txtJuncPair = new TxtReadandWrite(outPathPrefix+"junctionPair.txt", true);
		TxtReadandWrite txtJunc2Num = new TxtReadandWrite(outPathPrefix + "Junc2Num", true);
		TxtReadandWrite txtJuncPair2Num = new TxtReadandWrite(outPathPrefix + "JuncPair2Num", true);

		
		for (String string : mapJun1ToSetJun2.keySet()) {
			Set<String> setJunc2 = mapJun1ToSetJun2.get(string);
			for (String string2 : setJunc2) {
				txtJuncPair.writefile(string + "\t" + string2);
			}
		}
		
		for (String junc : mapJuncOne2AllNum.keySet()) {
			txtJunc2Num.writefileln(junc + "\t" + mapJuncOne2AllNum.get(junc));
		}
		
		for (String juncPair : mapJuncPair2ReadsNum.keySet()) {
			txtJuncPair2Num.writefileln(juncPair + "\t" + mapJuncPair2ReadsNum.get(juncPair));
		}
		
		txtJunc2Num.close();
		txtJuncPair.close();
		txtJuncPair2Num.close();
	}

	@Override
	public void summary() {
		//NOTHING TO DO
	}
}
