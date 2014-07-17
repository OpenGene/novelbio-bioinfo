package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.Alignment;

//TODO 需要返回该差异剪接位点所对应的两类Iso
public abstract class SpliceTypePredict {
	private static final Logger logger = Logger.getLogger(SpliceTypePredict.class);
	ExonCluster exonCluster;
	TophatJunction tophatJunction;
	Boolean isType = null;
	HashMultimap<String, String> mapCond2Group;
	public SpliceTypePredict(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	public void setTophatJunction(TophatJunction tophatJunction) {
		this.tophatJunction = tophatJunction;
		this.mapCond2Group = tophatJunction.getMapCondition2Group();
	}
	
	/**
	 * @param condition
	 * @return
	 * key: group<br>
	 * value: 每个位点的值
	 */
	public ArrayListMultimap<String, Double> getJunGroup2lsValue(String condition) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = getLsJuncCounts(condition);
		for (String group : mapGroup2LsValue.keys()) {
			List<Double> lsValue = mapGroup2LsValue.get(group);
			if (lsValue == null || lsValue.size() == 0) {
				mapGroup2LsValue.put(group, 0.0);
				mapGroup2LsValue.put(group, 0.0);
			}
		}
		return mapGroup2LsValue;
	}
	/** 获得用于检验的junction reads */
	protected abstract ArrayListMultimap<String, Double> getLsJuncCounts(String condition);
	/** 是否为该种剪接类型 */
	public boolean isSpliceType() {
		if (isType != null) {
			return isType;
		}
		try {
			return isType();
		} catch (Exception e) {
			//TODO 删除断点
			return isType();
		}
	}
	
	protected abstract boolean isType();
	/** 获得剪接类型 */
	public abstract SplicingAlternativeType getType();
	/** 获得差异可变剪接的位点，用于检测表达 */
	public abstract Align getDifSite();
	/**
	 * 获得比较的位点
	 * 如果是cassette则返回全基因长度
	 * 如果是retain intron和alt5 alt3，返回该exon的长度<br>
	 * <b>list中的单个Alignment不考虑方向</b><br>
	 * 可以直接返回{@link GffGeneIsoInfo}
	 */
	public abstract List<? extends Alignment> getBGSite();
	
	
	/** 分别计算exon边界所参与的reads数，只取前两个值 */
	protected ArrayListMultimap<String, Double> getlsJunInfoEdge(String condition, List<ExonInfo> lsExonInfos) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		ExonInfo exonInfo0 = lsExonInfos.get(0);
		Map<String, Double> mapGroup2Value0_1 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo0.getStartAbs());
		Map<String, Double> mapGroup2Value0_2 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo0.getEndAbs());
		Map<String, Double> map0 = addMapDouble(mapGroup2Value0_1, mapGroup2Value0_2);
		addMapGroup2LsValue(mapGroup2LsValue, map0);
		
		if (lsExonInfos.size() > 1) {
			ExonInfo exonInfo1 = lsExonInfos.get(1);
			Map<String, Double> mapGroup2Value1_1 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo1.getStartAbs());
			Map<String, Double> mapGroup2Value1_2 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo1.getEndAbs());
			Map<String, Double> map1 = addMapDouble(mapGroup2Value1_1, mapGroup2Value1_2);
			addMapGroup2LsValue(mapGroup2LsValue, map1);
		}
		return mapGroup2LsValue;
	}
	
	protected void addMapGroup2LsValue(ArrayListMultimap<String, Double> mapGroup2LsValue, Map<String, Double> mapGroup2Value) {
		for (String group : mapGroup2Value.keySet()) {
			mapGroup2LsValue.put(group, mapGroup2Value.get(group));
		}
	}
	
	/**
	 * 获得跳过该exonCluster组的readsNum
	 * @param gffDetailGene1
	 * @param exonCluster
	 * @param condition
	 * @return list-int 返回同一个位点，n个重复中的情况
	 */
	protected Map<String, Double> getJunReadsNum(String condition) {
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		Map<String, Double> mapGroupeValue = null;
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon(gffDetailGene));
		
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			Map<String, Double> mapGroup2ValueTmp = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
			mapGroupeValue = addMapDouble(mapGroupeValue, mapGroup2ValueTmp);	
		}
		return mapGroupeValue;
	}
	
	/**
	 * 将lsTmpValue的数据加到lsResult上
	 * @param lsResult
	 * @param lsTmpValue
	 * @return
	 */
	protected Map<String, Double> addMapDouble(Map<String, Double> mapResult, Map<String, Double> mapTmpValue) {
		if (mapResult == null && mapTmpValue == null) {
			return mapResult;
		} else if (mapTmpValue == null) {
			return mapResult;
		} else if (mapResult == null || mapResult.size() == 0) {
			mapResult = new HashMap<>(mapTmpValue);
			return mapResult;
		}
		for (String group : mapTmpValue.keySet()) {
			double value = mapResult.get(group);
			double valueTmp = mapTmpValue.get(group);
			mapResult.put(group, value + valueTmp);
		}
		return mapResult;
	}
	
	/**
	 * 查找含有该exon的转录本，
	 * 获得跨过该外显子的坐标
	 */
	private HashSet<String> getSkipExonLoc_From_IsoHaveExon() {
		HashSet<String> setLocation = new HashSet<String>();
		for (List<ExonInfo> lsExonInfos : exonCluster.getLsIsoExon()) {
			if (lsExonInfos.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfos.get(0).getParent();
			int exonNumBefore = lsExonInfos.get(0).getItemNum() - 1;
			int exonNumAfter = lsExonInfos.get(lsExonInfos.size() - 1).getItemNum() + 1;
			if (exonNumBefore < 0 || exonNumAfter >= gffGeneIsoInfo.size()) {
				continue;
			}
			int start = gffGeneIsoInfo.get(exonNumBefore).getEndCis();
			int end = gffGeneIsoInfo.get(exonNumAfter).getStartCis();
			setLocation.add(start + SepSign.SEP_ID + end);
		}
		return setLocation;
	}
	
	/**查找不含该exon的转录本， 
	 * 获得跨过该外显子的坐标 */
	private HashSet<String> getSkipExonLoc_From_IsoWithoutExon(GffDetailGene gffDetailGene) {
		HashSet<String> setLocation = new HashSet<String>();
		
		Map<GffGeneIsoInfo, Integer> hashTmp = exonCluster.getMapIso2ExonIndexSkipTheCluster();
		for (Entry<GffGeneIsoInfo, Integer> entry : hashTmp.entrySet()) {
			GffGeneIsoInfo gffGeneIsoInfo = entry.getKey();
			int exonNum = entry.getValue();
			if (exonNum >= gffGeneIsoInfo.size()-1) {
				continue;
			}
			String location = gffGeneIsoInfo.get(exonNum).getEndCis() + SepSign.SEP_ID + gffGeneIsoInfo.get(exonNum+1).getStartCis();
			setLocation.add(location);
		}
		return setLocation;
	}
	
	/** 按照junction reads数从大到小排序 */
	protected List<ExonInfo2Value> getLsExon2Value(List<ExonInfo> lsExonInfos) {
		List<ExonInfo2Value> lsExon2Value = new ArrayList<>();
		for (ExonInfo exonInfo : lsExonInfos) {
			double number = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo.getStartAbs())
					+ tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), exonInfo.getEndAbs());
			ExonInfo2Value exonInfo2Value = new ExonInfo2Value(exonInfo, number);
			lsExon2Value.add(exonInfo2Value);
		}
		Collections.sort(lsExon2Value);
		return lsExon2Value;
	}
	
	
	public static List<SpliceTypePredict> getSplicingTypeLs(ExonCluster exonCluster) {
		ArrayList<SpliceTypePredict> lsSpliceTypePredictsTmp = new ArrayList<SpliceTypePredict>();
		if (exonCluster.isSameExonInExistIso() || exonCluster.isNotSameTss_But_SameEnd() || exonCluster.isAtEdge()) {
			return lsSpliceTypePredictsTmp;
		}
		
		SpliceTypePredict spliceTypeME = new PredictME(exonCluster);
		SpliceTypePredict spliceTypeAS = new PredictAltStart(exonCluster);
		SpliceTypePredict spliceTypeAE = new PredictAltEnd(exonCluster);
		SpliceTypePredict spliceTypeRI = new PredictRetainIntron(exonCluster);
		SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
		SpliceTypePredict spliceTypeA5 = new PredictAlt5(exonCluster);
		SpliceTypePredict spliceTypeA3 = new PredictAlt3(exonCluster);
		lsSpliceTypePredictsTmp.add(spliceTypeME);
		lsSpliceTypePredictsTmp.add(spliceTypeAS);
		lsSpliceTypePredictsTmp.add(spliceTypeAE);
		lsSpliceTypePredictsTmp.add(spliceTypeRI);
		lsSpliceTypePredictsTmp.add(spliceTypeCS);
		lsSpliceTypePredictsTmp.add(spliceTypeA5);
		lsSpliceTypePredictsTmp.add(spliceTypeA3);
		
		ArrayList<SpliceTypePredict> lsResult = new ArrayList<SpliceTypePredict>();
		
		for (SpliceTypePredict spliceTypePredict : lsSpliceTypePredictsTmp) {
			if (spliceTypePredict.isSpliceType()) {
				lsResult.add(spliceTypePredict);
			}
		}
		if (lsResult.size() == 0) {
			lsResult.add(new PredictUnKnown(exonCluster));
		}
		return lsResult;
	}

	public static enum SplicingAlternativeType {
		cassette("cassette"), cassette_multi("cassette_multi"), alt5("A5SS"), alt3("A3SS"), altend("AltEnd"), altstart("AltStart"),
		mutually_exclusive("MX"), retain_intron("IR"), unknown("Undefined"), sam_exon("same_exon"),
		startDif("StartDiff"), endDif("EndDif");
		static HashMap<String, SplicingAlternativeType> mapName2Events = new LinkedHashMap<String, SplicingAlternativeType>();
		String info;
		SplicingAlternativeType(String name) {
			this.info = name;
		}
		
		public static HashMap<String, SplicingAlternativeType> getMapName2SplicingEvents() {
			if (mapName2Events.size() == 0) {
				mapName2Events.put("cassette", cassette);
				mapName2Events.put("cassette_multi", cassette_multi);
				mapName2Events.put("A5SS", alt5);
				mapName2Events.put("A3SS", alt3);
				mapName2Events.put("AltEnd", altend);
				mapName2Events.put("AltStart", altstart);
				mapName2Events.put("MX", mutually_exclusive);
				mapName2Events.put("IR", retain_intron);
				mapName2Events.put("Undefined", unknown);
//				mapName2Events.put("sam_exon", sam_exon);
//				mapName2Events.put("startDif", startDif);
//				mapName2Events.put("endDif", endDif);
			}
			return mapName2Events;
		}
	}
	
	/**
	 * 是否为所识别的位点，一般情况下都为true。
	 * 例外：alt5和alt3，如果差异的那一小段的太短，譬如长度小于10bp，就会返回false
	 * @return
	 */
	public  boolean isFiltered() {
		return true;
	}

}

/** 用来排序的类 */
class ExonInfo2Value implements Comparable<ExonInfo2Value> {
	ExonInfo exonInfo;
	Double value;
	public ExonInfo2Value(ExonInfo exonInfo, double value) {
		this.exonInfo = exonInfo;
		this.value = value;
	}
	@Override
	public int compareTo(ExonInfo2Value o) {
		return -value.compareTo(o.value);
	}
	
}
