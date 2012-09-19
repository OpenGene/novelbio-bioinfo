package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.SepSign;

public class TophatJunction {
	///////////////////// ��ȡ junction  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * key condition--
	 * key��junction: 
	 * value��int[2]���ֱ��Ƕ�Ӧ��junction�����reads��
	 * һ��junction1 ��Ӧ���junction2��Ҳ����junction�Ǻ������exon�ģ���ô���Ϊjunction1���Ҷ�Ϊjunction2.
	 * ���ڿɱ���Ӵ��ڣ�����һ��jun1���ܶ�Ӧ���jun2��Ҳ���ǲ�ͬ��exon������һ��
	 */
	HashMap<String, HashMapLsValue<String, int[]>> mapCond_To_Jun1toLsJun2LocAndReadsNum = new HashMap<String, HashMapLsValue<String,int[]>>();
	/**
	 * condition--junction �Ժ;����reads��
	 */
	HashMap<String, HashMap<String,Integer>>  mapCond_To_JunLoc2ReadsNum = new HashMap<String, HashMap<String,Integer>>();
	String cond = "oneJunFile";
	/**
	 * ���������ĺͲ��������Ĵ���
	 * ��ȡjuction�ļ�
	 * @param junctionFile
	 */
	public void setJunFile(String junctionFile) {
		setJunFile(junctionFile, cond);
	}
	
	public int getJunNum(String condition) {
		return mapCond_To_JunLoc2ReadsNum.get(condition).size();
	}
	/**
	 * ��ȡjunction�ļ����ļ���ÿ������λ��ֻ�ܳ���һ��\
	 * @param condition
	 * @param junctionFile
	 */
	public void setJunFile(String condition, String junctionFile) {
		HashMapLsValue<String,  int[]> tmpMapJun1toLsJun2AndReadsNum = getTmpMapJun1toLsJun2AndReadsNum(condition);
		HashMap<String,Integer> tmpHashJunctionBoth = getTmpMapJun1Jun2_To_Num(condition);
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			//junctionλ�㶼�趨��exon��
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
	/** ��ü���λ��1����Ӧ�ļ���λ��2������
	 * �Լ��������λ���junction reads���� */
	private HashMapLsValue<String,  int[]> getTmpMapJun1toLsJun2AndReadsNum(String condition) {
		//��ö�Ӧ��hash��
		HashMapLsValue<String,  int[]> tmpMapJun1toLsJun2AndReadsNum = null;
		if (mapCond_To_Jun1toLsJun2LocAndReadsNum.containsKey(condition)) {
			tmpMapJun1toLsJun2AndReadsNum = mapCond_To_Jun1toLsJun2LocAndReadsNum.get(condition);
		} else {
			tmpMapJun1toLsJun2AndReadsNum = new HashMapLsValue<String, int[]>();
			mapCond_To_Jun1toLsJun2LocAndReadsNum.put(condition, tmpMapJun1toLsJun2AndReadsNum);
		}
		return tmpMapJun1toLsJun2AndReadsNum;

	}
	/** ��ü���λ��1 �ͼ���λ��2 ����Ӧ��junction reads */
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
	 * ���������Ĳ��Ҽ���λ��
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite) {
		return getJunctionSite(chrID, locSite, cond);
	}
	/**
	 * ���������λ�㣬�ҳ�locsite,�Լ��ܹ��ж���reads֧��
	 * 0��ʾû��junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite,String condition) {
		HashMapLsValue<String, int[]> tmpHashLsJunction = mapCond_To_Jun1toLsJun2LocAndReadsNum.get(condition);
		if (tmpHashLsJunction == null) {
			return 0;
		}
		if (tmpHashLsJunction.containsKey(chrID.toLowerCase()+SepSign.SEP_INFO_SAMEDB+locSite) ) {
			ArrayList<int[]> lsJun2 = tmpHashLsJunction.get(chrID.toLowerCase()+SepSign.SEP_INFO_SAMEDB+locSite);
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
	 * ���������λ�㣬�ҳ�locsite
	 * @param chrID
	 * @param locStartSite ����νǰ���ڲ��Զ��ж�
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
