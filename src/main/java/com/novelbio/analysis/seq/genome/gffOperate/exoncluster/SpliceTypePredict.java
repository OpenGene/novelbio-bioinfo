package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.database.domain.geneanno.SepSign;

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
		return isType();
	}
	
	protected abstract boolean isType();
	/** 获得剪接类型 */
	public abstract String getType();
	/** 获得不同的位点，用于检测表达 */
	public abstract Align getDifSite();
	/**
	 * 根据junction reads的数量从大到小排序
	 * 并返回topjunction中reads支持最多的两个点
	 */
	protected SiteInfo sortByJunctionReads(final TophatJunction tophatJunction, Collection<Integer> colBount) {
		ArrayList<Integer> lsBount = new ArrayList<Integer>(colBount);
		Collections.sort(lsBount, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				Integer start1 = tophatJunction.getJunctionSite(exonCluster.getChrID(), o1);
				Integer start2 = tophatJunction.getJunctionSite(exonCluster.getChrID(), o2);
				return -start1.compareTo(start2);
			}
		});
		int start = lsBount.get(0);
		int end = lsBount.get(1);
		SiteInfo siteInfo = new SiteInfo(exonCluster.getChrID(), Math.min(start, end), Math.max(start, end));
		return siteInfo;
	}
	

	public static enum SplicingAlternativeType {
		cassette, cassette_multi, alt5, alt3, altend, altstart, mutually_exclusive, retain_intron, unknown, sam_exon;
		
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
			}
			return mapName2Events;
		}
	}

}
