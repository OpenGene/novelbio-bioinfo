package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TophatJunction {
	
	
	
	
	
	
	
	///////////////////// 读取 junction  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * key condition--
	 * key：junction: 
	 * value：int[2]；分别是对应的junction坐标和reads数
	 */
	HashMap<String, HashMap<String, ArrayList<int[]>>> hashJunction = new HashMap<String, HashMap<String,ArrayList<int[]>>>();
	/**
	 * condition--junction 对和具体的reads数
	 */
	HashMap<String, HashMap<String,Integer>>  hashJunctionBoth = new HashMap<String, HashMap<String,Integer>>();
	String cond = "oneJunFile";
	/**
	 * 不加条件的和不加条件的搭配
	 * 读取juction文件
	 * @param junctionFile
	 */
	public void setJunFile(String junctionFile) {
		setJunFile(junctionFile, cond);
	}
	
	
	/**
	 * 读取junction文件
	 * @param junctionFile
	 */
	public void setJunFile(String junctionFile, String condition) {
		//获得对应的hash表
		HashMap<String,  ArrayList<int[]>> tmpHashJunction = null;
		if (hashJunction.containsKey(condition))
			tmpHashJunction = hashJunction.get(condition);
		else
			tmpHashJunction = new HashMap<String, ArrayList<int[]>>();
			hashJunction.put(condition, tmpHashJunction);
		
		HashMap<String,Integer> tmpHashJunctionBoth = null;
		if (hashJunctionBoth.containsKey(condition)) 
			tmpHashJunctionBoth = hashJunctionBoth.get(condition);
		else 
			tmpHashJunctionBoth = new HashMap<String, Integer>();
			hashJunctionBoth.put(condition, tmpHashJunctionBoth);
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			//junction位点都设定在exon上
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			String strjunct1 = ss[0].toLowerCase() +"//"+junct1;
			String strjunct2 = ss[0].toLowerCase() +"//"+ junct2;
			String strJunBoth = strjunct1 + "///" + strjunct2;
			
			if (tmpHashJunctionBoth.containsKey(strJunBoth)) {
				int junNum = tmpHashJunctionBoth.get(strJunBoth);
				junNum = junNum + Integer.parseInt(ss[4]);
				tmpHashJunctionBoth.put(strJunBoth, junNum);
			}
			else {
				tmpHashJunctionBoth.put(strJunBoth, Integer.parseInt(ss[4]));
			}
			
			if (tmpHashJunction.containsKey(strjunct1)) {
				ArrayList<int[]> lsJun2 = tmpHashJunction.get(strjunct1);
				int[] info = new int[]{junct2, Integer.parseInt(ss[4])};
				lsJun2.add(info);
			}
			else {
				ArrayList<int[]> lsJun2 = new ArrayList<int[]>();
				int[] info = new int[]{junct2, Integer.parseInt(ss[4])};
				lsJun2.add(info);
				tmpHashJunction.put(strjunct1, lsJun2);
			}
			
			if (tmpHashJunction.containsKey(strjunct2)) {
				ArrayList<int[]> lsJun2 = tmpHashJunction.get(strjunct2);
				int[] info = new int[]{junct1, Integer.parseInt(ss[4])};
				lsJun2.add(info);
			}
			else {
				ArrayList<int[]> lsJun2 = new ArrayList<int[]>();
				int[] info = new int[]{junct1, Integer.parseInt(ss[4])};
				lsJun2.add(info);
				tmpHashJunction.put(strjunct2, lsJun2);
			}
		}
	}
	/**
	 * 不加条件的查找剪接位点
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite)
	{
		return getJunctionSite(chrID, locSite, cond);
	}
	
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite,String condition)
	{
		HashMap<String, ArrayList<int[]>> tmpHashJunction = hashJunction.get(condition);
		if (tmpHashJunction == null) {
			return 0;
		}
		if (tmpHashJunction.containsKey(chrID.toLowerCase()+"//"+locSite) )
		{
			ArrayList<int[]> lsJun2 = tmpHashJunction.get(chrID.toLowerCase()+"//"+locSite);
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
	
	public int getJunctionSite(String chrID, int locStartSite, int locEndSite)
	{
		return getJunctionSite(chrID, locStartSite, locEndSite, cond);
	}
	
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locStartSite, int locEndSite, String condition)
	{
		HashMap<String, Integer> tmpHashJunctionBoth = hashJunctionBoth.get(condition);
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID.toLowerCase() + "//" + locS +"///"+chrID.toLowerCase() + "//" + locE;
		if (tmpHashJunctionBoth.containsKey(key) )
		{
			return tmpHashJunctionBoth.get(key);
		}
		else {
			return 0;
		}
	}
}
