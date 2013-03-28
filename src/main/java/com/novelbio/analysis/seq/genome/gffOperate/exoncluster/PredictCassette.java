package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.Alignment;

public class PredictCassette extends SpliceTypePredict {
	HashSet<GffGeneIsoInfo> setExistExonIso;
	HashSet<GffGeneIsoInfo> setSkipExonIso;
	
	public PredictCassette(ExonCluster exonCluster) {
		super(exonCluster);
	}
	
	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		return getJuncCountsLoose(condition);
	}
	/** 不完全按照转录本信息来 */
	private ArrayList<Double> getJuncCountsLoose(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		lsCounts.add((double) getJunReadsNum(condition));
		Set<Integer> setAlignExist = new HashSet<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : setExistExonIso) {
			ArrayList<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
			setAlignExist.add(lsExon.get(0).getStartCis());
			setAlignExist.add(lsExon.get(lsExon.size() - 1).getEndCis());
		}
		int exist = 0;
		for (Integer align : setAlignExist) {
			exist = exist + tophatJunction.getJunctionSite(condition, exonCluster.getRefID(), align);
		}
		lsCounts.add((double) exist);
		return lsCounts;
	}
	
	/** 完全按照转录本信息来 */
	private ArrayList<Double> getJuncCountsStrict(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		HashSet<Align> setAlignSkip = new HashSet<Align>();
		HashSet<Align> setAlignExist = new HashSet<Align>();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : setSkipExonIso) {
			int beforeIndex = exonCluster.getMapIso2ExonIndexSkipTheCluster().get(gffGeneIsoInfo);
			int afterIndex = beforeIndex + 1;
			ExonInfo exonBefore = gffGeneIsoInfo.get(beforeIndex);
			ExonInfo exonAfter = gffGeneIsoInfo.get(afterIndex);
			Align align = new Align(exonCluster.getRefID(), exonBefore.getEndCis(), exonAfter.getStartCis());
			setAlignSkip.add(align);
		}
		
		for (GffGeneIsoInfo gffGeneIsoInfo : setExistExonIso) {
			ArrayList<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
			if (lsExon.size() == 0) {
				continue;
			}
			int beforeIndex = lsExon.get(0).getItemNum() - 1;
			int afterIndex = lsExon.get(lsExon.size() - 1).getItemNum() + 1;
			Align alignBefore = new Align(exonCluster.getRefID(), gffGeneIsoInfo.get(beforeIndex).getEndCis(), lsExon.get(0).getStartCis());
			Align alignAfter = new Align(exonCluster.getRefID(), lsExon.get(lsExon.size() - 1).getEndCis(), 
					gffGeneIsoInfo.get(afterIndex).getStartCis());
			
			setAlignExist.add(alignBefore);
			setAlignExist.add(alignAfter);
		}
		//获得junction reads
		int skip = 0;
		int exist = 0;
		for (Align align : setAlignSkip) {
			skip += tophatJunction.getJunctionSite(condition, exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs());
		}
		for (Align align : setAlignExist) {
			exist += tophatJunction.getJunctionSite(condition, exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs());
		}
		lsCounts.add((double) skip);
		lsCounts.add((double) exist);
		return lsCounts;
	}

	@Override
	protected boolean isType() {
		setExistExonIso = new HashSet<GffGeneIsoInfo>();
		setSkipExonIso = new HashSet<GffGeneIsoInfo>();
		boolean isType = false;
		int initialNum = -1000;
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosExist = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			if (exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() == 0) {
				continue;
			}
			lsGeneIsoInfosExist.add(gffGeneIsoInfo);
		}
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforAfterExist = getIsoHaveBeforeAndAfterExon(initialNum, lsGeneIsoInfosExist);
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforAfterSkip = getIsoHaveBeforeAndAfterExon(initialNum, exonCluster.getMapIso2ExonIndexSkipTheCluster().keySet());
		//判定是否前后的exon相同
		for (String string : setBeforAfterExist.keySet()) {
			if (string.contains(initialNum + "")) {
				continue;
			}
			if (setBeforAfterSkip.containsKey(string)) {
				setExistExonIso.addAll(setBeforAfterExist.get(string));
				setSkipExonIso.addAll(setBeforAfterSkip.get(string));
				isType = true;
			}
		}
		return isType;
	}
	/** 
	 * 获得某个iso的前后的 exon的相对位置
	 * 譬如某个iso在前面有一个exon，后面有一个exon
	 * 则统计为0sepsign0
	 * 如果前面的前面有一个exon，后面的后面的后面有一个exon
	 * 则统计为
	 * -1sepSign2
	 * @param initialNum 初始化数字，设定为一个比较大的负数就好，随便设定，譬如-1000
	 * @param lsIso_ExonExist
	 * @return
	 */
	private ArrayListMultimap<String, GffGeneIsoInfo> getIsoHaveBeforeAndAfterExon(int initialNum, Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforeAfter = ArrayListMultimap.create();
		ExonCluster clusterBefore = exonCluster.exonClusterBefore;
		ExonCluster clusterAfter = exonCluster.exonClusterAfter;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			int[] beforeAfter = new int[]{initialNum, initialNum};//初始化为负数
			int numBefore = 0, numAfter = 0;//直接上一位的exon标记为0，再向上一位标记为-1
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[0] = numBefore;
					break;
				}
				clusterBefore = clusterBefore.exonClusterBefore;
				numBefore--;
			}
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[1] = numAfter;
					break;
				}
				clusterAfter = clusterAfter.exonClusterAfter;
				numAfter++;
			}
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.put(tmpBeforeAfter, gffGeneIsoInfo);
		}
		return setBeforeAfter;
	}
	
	@Override
	public SplicingAlternativeType getType() {
		for (GffGeneIsoInfo gffGeneIsoInfo : setExistExonIso) {
			ArrayList<ExonInfo> lsExons = exonCluster.getIsoExon(gffGeneIsoInfo);
			if (lsExons.size() > 1) {
				return SplicingAlternativeType.cassette_multi;
			}
		}
		return SplicingAlternativeType.cassette;
	}

	@Override
	public Align getDifSite() {
		return new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
	}

	@Override
	public List<? extends Alignment> getBGSite() {
		return exonCluster.getParentGene().getLongestSplitMrna();
	}

}
