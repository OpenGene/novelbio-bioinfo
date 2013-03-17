package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.database.domain.geneanno.SepSign;

//TODO 需要返回该差异剪接位点所对应的两类Iso
public abstract class SpliceTypePredict {
	ExonCluster exonCluster;
	TophatJunction tophatJunction;
	Boolean isType = null;
	
	public SpliceTypePredict(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	public void setTophatJunction(TophatJunction tophatJunction) {
		this.tophatJunction = tophatJunction;
	}
	/** 获得用于检验的junction reads */
	public abstract ArrayList<Double> getJuncCounts(String condition);
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
	/**
	 * 获得跳过该exonCluster组的readsNum
	 * @param gffDetailGene
	 * @param exonCluster
	 * @param condition
	 * @return
	 */
	protected int getJunReadsNum(String condition) {
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		int result = 0;
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon(gffDetailGene));
		
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			result = result + tophatJunction.getJunctionSite(condition, gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		}
		
		return result;
	}
	
	/**
	 * 查找含有该exon的转录本，
	 * 获得跨过该外显子的坐标
	 */
	private HashSet<String> getSkipExonLoc_From_IsoHaveExon() {
		HashSet<String> setLocation = new HashSet<String>();
		for (ArrayList<ExonInfo> lsExonInfos : exonCluster.getLsIsoExon()) {
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
		cassette, cassette_multi, alt5, alt3, altend, altstart, mutually_exclusive, retain_intron, unknown, sam_exon,
		startDif, endDif;
		static HashMap<String, SplicingAlternativeType> mapName2Events = new LinkedHashMap<String, SplicingAlternativeType>();
		public static HashMap<String, SplicingAlternativeType> getMapName2SplicingEvents() {
			if (mapName2Events.size() == 0) {
				mapName2Events.put("cassette", cassette);
				mapName2Events.put("cassette_multi", cassette_multi);
				mapName2Events.put("alt5", alt5);
				mapName2Events.put("alt3", alt3);
				mapName2Events.put("altend", altend);
				mapName2Events.put("altstart", altstart);
				mapName2Events.put("mutually_exon", mutually_exclusive);
				mapName2Events.put("retain_intron", retain_intron);
				mapName2Events.put("unknown", unknown);
				mapName2Events.put("sam_exon", sam_exon);
				mapName2Events.put("startDif", startDif);
				mapName2Events.put("endDif", endDif);
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
