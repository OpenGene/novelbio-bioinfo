package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class MirCountGetValueAbs {
	
	/** �������ļ���miRNA��ֵ�ϲ����� */
	public ArrayList<String[]> combValue(HashMap<String, HashMap<String, Double>> mapPrefix_2_ID2Value) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		//ǰ������������Ϣ�����濪ʼ��prefix
		ArrayList<String> lstitle = getTitlePre(mapPrefix_2_ID2Value);
		lsResult.add(lstitle.toArray(new String[1]));
		
		HashSet<String> setID = getAllName(mapPrefix_2_ID2Value);
		
		for (String id : setID) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			fillMataInfo(id, lsTmpResult);
			for (int i = getTitleIDAndInfo().length; i < lstitle.size(); i++) {
				HashMap<String, Double> mapMirna2Value = mapPrefix_2_ID2Value.get(lstitle.get(i));
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
	
	/** �����漰��������miRNA������ */
	private ArrayList<String> getTitlePre(HashMap<String, ? extends Object> mapPrefix2Info) {
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
	/** �����ı���ǰ���е�title��Ʃ��TitleFormatNBC.RfamID.toString()�� sequence�� */
	protected abstract String[] getTitleIDAndInfo();
	
	/** �������ID���ָ����һЩ��Ϣ��ע����غ�title��Ӧ */
	protected abstract void fillMataInfo(String id, ArrayList<String> lsTmpResult);
	
	/** �����漰��������miRNA������ */
	private HashSet<String> getAllName(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		LinkedHashSet<String> setMirNameAll = new LinkedHashSet<String>();
		for (HashMap<String, Double> mapMiRNA2Value : mapPrefix2_mapMiRNA2Value.values()) {
			for (String miRNAname : mapMiRNA2Value.keySet()) {
				setMirNameAll.add(miRNAname);
			}
		}
		return setMirNameAll;
	}
}
