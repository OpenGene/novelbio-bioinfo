package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.SepSign;

public class TophatJunction {
	///////////////////// 读取 junction  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * key condition--
	 * key：junction: 
	 * value：int[2]；分别是对应的junction坐标和reads数
	 * 一个junction1 对应多个junction2，也就是junction是横跨两个exon的，那么左端为junction1，右端为junction2.
	 * 由于可变剪接存在，所以一个jun1可能对应多个jun2，也就是不同的exon连接在一起。
	 */
	HashMap<String, ArrayListMultimap<String, int[]>> mapCond_To_Jun1toLsJun2LocAndReadsNum = new HashMap<String, ArrayListMultimap<String,int[]>>();
	/**
	 * condition--junction 对和具体的reads数
	 */
	HashMap<String, HashMap<String,Integer>>  mapCond_To_JunLoc2ReadsNum = new HashMap<String, HashMap<String,Integer>>();
	String cond = "oneJunFile";
	/**
	 * 不加条件的和不加条件的搭配
	 * 读取juction文件
	 * @param junctionFile
	 */
	public void setJunFile(String junctionFile) {
		setJunFile(junctionFile, cond);
	}
	
	public int getJunNum(String condition) {
		return mapCond_To_JunLoc2ReadsNum.get(condition).size();
	}
	/**
	 * 读取junction文件，文件中每个剪接位点只能出现一次\
	 * @param condition
	 * @param junctionFile
	 */
	public void setJunFile(String condition, String junctionFile) {
		ArrayListMultimap<String,  int[]> tmpMapJun1toLsJun2AndReadsNum = getTmpMapJun1toLsJun2AndReadsNum(condition);
		HashMap<String,Integer> tmpHashJunctionBoth = getTmpMapJun1Jun2_To_Num(condition);
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			//junction位点都设定在exon上
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			String strjunct1 = ss[0].toLowerCase()  + SepSign.SEP_INFO_SAMEDB + junct1;
			String strjunct2 = ss[0].toLowerCase() + SepSign.SEP_INFO_SAMEDB + junct2;
			String strJunBoth = strjunct1 + SepSign.SEP_INFO + strjunct2;
			
			if (tmpHashJunctionBoth.containsKey(strJunBoth)) {
				int junNum = tmpHashJunctionBoth.get(strJunBoth);
				junNum = junNum + Integer.parseInt(ss[4]);
				tmpHashJunctionBoth.put(strJunBoth, junNum);
			}
			else {
				tmpHashJunctionBoth.put(strJunBoth, Integer.parseInt(ss[4]));
			}
			tmpMapJun1toLsJun2AndReadsNum.put(strjunct1, new int[]{junct2, Integer.parseInt(ss[4])});
			tmpMapJun1toLsJun2AndReadsNum.put(strjunct2, new int[]{junct1, Integer.parseInt(ss[4])});
		}
	}
	/** 获得剪接位点1所对应的剪接位点2的坐标
	 * 以及该组剪接位点的junction reads数量 */
	private ArrayListMultimap<String,  int[]> getTmpMapJun1toLsJun2AndReadsNum(String condition) {
		//获得对应的hash表
		ArrayListMultimap<String,  int[]> tmpMapJun1toLsJun2AndReadsNum = null;
		if (mapCond_To_Jun1toLsJun2LocAndReadsNum.containsKey(condition)) {
			tmpMapJun1toLsJun2AndReadsNum = mapCond_To_Jun1toLsJun2LocAndReadsNum.get(condition);
		} else {
			tmpMapJun1toLsJun2AndReadsNum = ArrayListMultimap.create();
			mapCond_To_Jun1toLsJun2LocAndReadsNum.put(condition, tmpMapJun1toLsJun2AndReadsNum);
		}
		return tmpMapJun1toLsJun2AndReadsNum;

	}
	/** 获得剪接位点1 和剪接位点2 所对应的junction reads */
	private HashMap<String,Integer> getTmpMapJun1Jun2_To_Num(String condition) {
		HashMap<String,Integer> tmpHashJunctionBoth = null;
		if (mapCond_To_JunLoc2ReadsNum.containsKey(condition)) {
			tmpHashJunctionBoth = mapCond_To_JunLoc2ReadsNum.get(condition);
		} else {
			tmpHashJunctionBoth = new HashMap<String, Integer>();
			mapCond_To_JunLoc2ReadsNum.put(condition, tmpHashJunctionBoth);
		}
		return tmpHashJunctionBoth;
	}
	/**
	 * 不加条件的查找剪接位点
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite) {
		return getJunctionSite(chrID, locSite, cond);
	}
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite,String condition) {
		ArrayListMultimap<String, int[]> tmpHashLsJunction = mapCond_To_Jun1toLsJun2LocAndReadsNum.get(condition);
		if (tmpHashLsJunction == null) {
			return 0;
		}
		if (tmpHashLsJunction.containsKey(chrID.toLowerCase()+SepSign.SEP_INFO_SAMEDB+locSite) ) {
			List<int[]> lsJun2 = tmpHashLsJunction.get(chrID.toLowerCase()+SepSign.SEP_INFO_SAMEDB+locSite);
			int junAll = 0;
			for (int[] is : lsJun2) {
				junAll = junAll + is[1];
			}
			return junAll;
		}
		else {
			return 0;
		}
	}
	
	public int getJunctionSite(String chrID, int locStartSite, int locEndSite) {
		return getJunctionSite(chrID, locStartSite, locEndSite, cond);
	}
	
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locStartSite, int locEndSite, String condition) {
		HashMap<String, Integer> tmpHashJunctionBoth = mapCond_To_JunLoc2ReadsNum.get(condition);
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locS + SepSign.SEP_INFO +chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locE;
		if (tmpHashJunctionBoth.containsKey(key) ) {
			return tmpHashJunctionBoth.get(key);
		}
		else {
			return 0;
		}
	}
}
