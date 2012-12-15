package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * ��Ϊsnpһ�㲻�ᷢ������ͬ��λ�㣬�������ܷ�������ͬ�Ļ��������������һ��������
 * @author zong0jie
 */
public class GeneFilter {
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2SnpIndel = new HashMap<String, MapInfoSnpIndel>();
	ArrayListMultimap<GeneID, MapInfoSnpIndel> mapGeneID2LsMapInfoSnpIndel = ArrayListMultimap.create();
	
	Set<String> setTreat = new HashSet<String>();
	Set<String> setCol = new HashSet<String>();
	
	/** ʵ����ͨ�����˵���Ŀ */
	int treatFilteredMinNum = 0;
	
	GffChrAbs gffChrAbs;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * ���Ҫ���˵�mapInfoSnpIndel��������������
	 * @param mapInfoSnpIndel
	 */
	public void addMapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
		mapInfoSnpIndel.setGffChrAbs(gffChrAbs);
		mapSiteInfo2SnpIndel.put(getMapInfoSnpIndelStr(mapInfoSnpIndel), mapInfoSnpIndel);
	}
	
	public void addTreatName(String treatName) {
		setTreat.add(treatName);
	}
	public void addTreatName(Collection<String> colTreatName) {
		setTreat.addAll(colTreatName);
	}
	public void addColName(String colName) {
		setCol.add(colName);
	}
	public void addColTreatName(Collection<String> colColName) {
		setCol.addAll(colColName);
	}
	
	/** ��������treat���и�gene����Ϊͨ���� */
	public void setTreatFilteredNum(int treatFilteredNum) {
		this.treatFilteredMinNum = treatFilteredNum;
	}
	
	/** �������mapInfoSnpIndel���ջ��������������� */
	private void setMapGeneID2LsMapInfoSnpIndel() {
		for (MapInfoSnpIndel mapInfoSnpIndel : mapSiteInfo2SnpIndel.values()) {
			GeneID geneID = mapInfoSnpIndel.getGffIso().getGeneID();
			mapGeneID2LsMapInfoSnpIndel.put(geneID, mapInfoSnpIndel);
		}
	}
	
	/** ����treat�������������򣬾���Խ���������иû���Ͱ�����������Ȼ�󱣴����treemap */
	private void sortByTreatSampleNum() {
		//�������е�treemap
		TreeMap<Integer, ArrayList<MapInfoSnpIndel>> mapNum2LsMapSnpIndelInfo =
				new TreeMap<Integer, ArrayList<MapInfoSnpIndel>>(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return -o1.compareTo(o2);
					}
		});
		for (GeneID geneID : mapGeneID2LsMapInfoSnpIndel.keySet()) {
			List<MapInfoSnpIndel> lsSnpIndels = mapGeneID2LsMapInfoSnpIndel.get(geneID);
		}
	
	}
	
	private int getTreatNum(List<MapInfoSnpIndel> lsSnpIndels) {
		HashSet<String> setTreatName = new HashSet<String>();
		for (MapInfoSnpIndel mapInfoSnpIndel : lsSnpIndels) {
			
		}
	}
	
	/** ����MapInfoSnpIndel����������������Ӧ��string��������hashmap��key */
	private static String getMapInfoSnpIndelStr(MapInfoSnpIndel mapInfoSnpIndel) {
		return mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
	}
	
	
	
}
