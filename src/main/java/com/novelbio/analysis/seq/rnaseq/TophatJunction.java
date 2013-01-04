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
	 * value: ĳ��junction����junction֮��Ķ�Ӧ��ϵ
	 */
	HashMap<String, HashMultimap<String, String>> mapCond_To_mapJun1ToSetJun2 = new LinkedHashMap<String, HashMultimap<String,String>>();
	HashMultimap<String, String> mapJun1ToSetJun2;
	/**
	 * key condition--
	 * key��junction
	 * value����junction����Ӧ����reads��
	 */
	HashMap<String, LinkedHashMap<String, Integer>> mapCond_To_JuncOne2AllNum = new LinkedHashMap<String, LinkedHashMap<String,Integer>>();
	LinkedHashMap<String, Integer> mapJuncOne2AllNum;
	/**
	 * key condition
	 * value: ĳһ��junction�����Ӧ��reads����
	 */
	HashMap<String, LinkedHashMap<String,Integer>>  mapCond_To_JuncPair2ReadsNum = new LinkedHashMap<String, LinkedHashMap<String,Integer>>();
	LinkedHashMap<String, Integer> mapJuncPair2ReadsNum;	
	
	/** �趨��ǰʱ�� */
	public void setCondition(String condition) {
		mapJun1ToSetJun2 = getMapJunc1ToJunc2(condition);
		mapJuncOne2AllNum = getMapJuncOneToAllNum(condition);
		mapJuncPair2ReadsNum = getMapJuncPair2ReadsNum(condition);
	}
	
	/**
	 * ��ü���λ��1��Ӧ�����м���λ��2��map
	 * @param condition
	 * @return
	 */
	private HashMultimap<String, String> getMapJunc1ToJunc2(String condition) {
		//��ö�Ӧ��hash��
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

	 * ��ü���λ��1��Ӧ��ȫ��readsnum
	 * @param condition
	 * @return
	 */
	private LinkedHashMap<String, Integer> getMapJuncOneToAllNum(String condition) {
		//��ö�Ӧ��hash��
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
	 * ���һ�Լ���λ������Ӧ��ȫ��readsnum
	 * @param condition
	 * @return
	 */
	private LinkedHashMap<String, Integer> getMapJuncPair2ReadsNum(String condition) {
		//��ö�Ӧ��hash��
		LinkedHashMap<String, Integer> tmpMapJuncPair2ReadsNum = null;
		if (mapCond_To_JuncPair2ReadsNum.containsKey(condition)) {
			tmpMapJuncPair2ReadsNum = mapCond_To_JuncPair2ReadsNum.get(condition);
		} else {
			tmpMapJuncPair2ReadsNum = new LinkedHashMap<String, Integer>();
			mapCond_To_JuncPair2ReadsNum.put(condition, tmpMapJuncPair2ReadsNum);
		}
		return tmpMapJuncPair2ReadsNum;
	}
	
	/**���samBam���ļ����������Ϣ */
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
	 * ��ȡjunction�ļ����ļ���ÿ������λ��ֻ�ܳ���һ��\
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
			
			//junctionλ�㶼�趨��exon��
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			int junctionNum = Integer.parseInt(ss[4]);
			addJunctionInfo(chrID, junct1, junct2, junctionNum);
		}
	}
	/** 
	 * ��ӵ�������λ��reads
	 * @param chrID Ⱦɫ��
	 * @param junctionStart �������
	 * @param junctionEnd �����յ�
	 * @param junctionNum ����reads������
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
	
	/** ��ָ����map�����junction reads */
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
	 * ���������λ�㣬�ҳ�locsite,�Լ��ܹ��ж���reads֧��
	 * 0��ʾû��junction
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
	 * ���������λ�㣬�ҳ�locsite
	 * @param chrID
	 * @param locStartSite ����νǰ���ڲ��Զ��ж�
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
	 * ���������λ�㣬�ҳ�locsite
	 * @param chrID
	 * @param locStartSite ����νǰ���ڲ��Զ��ж�
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
