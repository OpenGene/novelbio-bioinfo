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
 * 因为snp一般不会发生在相同的位点，而更可能发生在相同的基因，所以用这个做一个过滤器
 * @author zong0jie
 */
public class GeneFilter {
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2SnpIndel = new HashMap<String, MapInfoSnpIndel>();
	ArrayListMultimap<GeneID, MapInfoSnpIndel> mapGeneID2LsMapInfoSnpIndel = ArrayListMultimap.create();
	
	Set<String> setTreat = new HashSet<String>();
	Set<String> setCol = new HashSet<String>();
	
	/** 实验组通过过滤的数目 */
	int treatFilteredMinNum = 0;
	
	GffChrAbs gffChrAbs;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * 添加要过滤的mapInfoSnpIndel，按照样本过滤
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
	
	/** 超过几个treat含有该gene就认为通过了 */
	public void setTreatFilteredNum(int treatFilteredNum) {
		this.treatFilteredMinNum = treatFilteredNum;
	}
	
	/** 把输入的mapInfoSnpIndel按照基因名字整理起来 */
	private void setMapGeneID2LsMapInfoSnpIndel() {
		for (MapInfoSnpIndel mapInfoSnpIndel : mapSiteInfo2SnpIndel.values()) {
			GeneID geneID = mapInfoSnpIndel.getGffIso().getGeneID();
			mapGeneID2LsMapInfoSnpIndel.put(geneID, mapInfoSnpIndel);
		}
	}
	
	/** 按照treat的样本数量排序，就是越多样本含有该基因就把他跳出来，然后保存进入treemap */
	private void sortByTreatSampleNum() {
		//倒序排列的treemap
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
	
	/** 给定MapInfoSnpIndel，返回其坐标所对应的string，用于做hashmap的key */
	private static String getMapInfoSnpIndelStr(MapInfoSnpIndel mapInfoSnpIndel) {
		return mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
	}
	
	
	
}
