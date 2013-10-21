package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
	boolean isMulitCassette = false;
	
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
			skip += tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs());
		}
		for (Align align : setAlignExist) {
			exist += tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs());
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
		Set<GffGeneIsoInfo> lsIsoExist = new HashSet<>();
		Set<GffGeneIsoInfo> lsIsoSkip = new HashSet<>();
		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			if (exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() == 0) {
				lsIsoSkip.add(gffGeneIsoInfo);
			} else {
				lsIsoExist.add(gffGeneIsoInfo);
			}
		}
		if (lsIsoSkip.size() == 0) {
			return false;
		}
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforAfterExist = getIsoExistHaveBeforeAndAfterExon(initialNum, lsIsoExist, lsIsoSkip);
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforAfterSkip = getIsoSkipHaveBeforeAndAfterExon(initialNum, lsIsoSkip);
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
	private ArrayListMultimap<String, GffGeneIsoInfo> getIsoExistHaveBeforeAndAfterExon(int initialNum, 
			Set<GffGeneIsoInfo> lsIso_ExonExist, Set<GffGeneIsoInfo> lsIso_ExonSkip) {
		
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforeAfter = ArrayListMultimap.create();

		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			ExonCluster clusterBefore = exonCluster.exonClusterBefore;
			ExonCluster clusterAfter = exonCluster.exonClusterAfter;
			int[] beforeAfter = new int[]{initialNum, initialNum};//初始化为负数
			int numBefore = 0, numAfter = 0;//直接上一位的exon标记为0，再向上一位标记为-1
			boolean cancel = false;
			boolean muti = false, before = false, after = false;
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					List<ExonInfo> lsExon = clusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo);
					if (gffGeneIsoInfo.indexOf(lsExon.get(0)) != 0) {
						before = true;
					}
					for (Entry<GffGeneIsoInfo, ArrayList<ExonInfo>> entryIso2Lsexon : clusterBefore.getMapIso2LsExon().entrySet()) {
						//上一个exoncluster中，存在 没有本exon的iso
						if (entryIso2Lsexon.getValue().size() > 0 && lsIso_ExonSkip.contains(entryIso2Lsexon.getKey()) ) {
							beforeAfter[0] = numBefore;
							cancel = true;
							break;
						}
					}
					if (cancel) break;
				} else {
					//如果本exoncluster中含有 跳过当前exon的iso，并且在beforecluster中也跳过exon，那么就继续检查上一个exoncluster
					//         beforeCluster     当前Cluster
					// 1--2--------3--4--------------------------------------7--8
					//  1--2--------------------------------5--6-------------7--8
					// 1--2----------------------------------------------------7--8 //这就是 跳过当前cluster，还跳过 before cluster的 iso
					Set<GffGeneIsoInfo> setIsoSkipBeforExon = clusterBefore.getMapIso2ExonIndexSkipTheCluster().keySet();
					boolean out = true;
					for (GffGeneIsoInfo gffSkipBefore : setIsoSkipBeforExon) {
						if (lsIso_ExonSkip.contains(gffSkipBefore)) {
							out = false;
							break;
						}
					}
					if (out) {
						break;
					}
				}
				if (before) {
					muti = true;
				}
				clusterBefore = clusterBefore.exonClusterBefore;
				numBefore--;
			}
			cancel = false;
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					List<ExonInfo> lsExon = clusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
					if (gffGeneIsoInfo.indexOf(lsExon.get(lsExon.size() - 1)) != gffGeneIsoInfo.size() - 1) {
						after = true;
					}
					for (Entry<GffGeneIsoInfo, ArrayList<ExonInfo>> entryIso2Lsexon : clusterAfter.getMapIso2LsExon().entrySet()) {
						//下一个exoncluster中，存在 没有本exon的iso
						if (entryIso2Lsexon.getValue().size() > 0 && lsIso_ExonSkip.contains(entryIso2Lsexon.getKey())) {
							beforeAfter[1] = numAfter;
							cancel = true;
							break;
						}
					}
					if (cancel) break;
				} else {
					//如果本exoncluster中含有 跳过当前exon的iso，并且在aftercluster中也跳过exon，那么就继续检查下一个exoncluster
					//         当前Cluster         afterCluster
					// 1--2--------3--4--------------------------------------7--8
					//  1--2--------------------------------5--6-------------7--8
					// 1--2----------------------------------------------------7--8 //这就是 跳过当前cluster，r还跳过 before cluster的 iso
					Set<GffGeneIsoInfo> setIsoSkipAfteExon = clusterAfter.getMapIso2ExonIndexSkipTheCluster().keySet();
					boolean out = true;
					for (GffGeneIsoInfo gffSkipAfter : setIsoSkipAfteExon) {
						if (lsIso_ExonSkip.contains(gffSkipAfter)) {
							out = false;
							break;
						}
					}
					if (out) {
						break;
					}
				}
				if (after) {
					muti = true;
				}
				clusterAfter = clusterAfter.exonClusterAfter;
				numAfter++;
			}
			if (numAfter -  numBefore >= 1 && numAfter - numBefore <=3 && muti == true) {
				isMulitCassette = true;
			}
			
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.put(tmpBeforeAfter, gffGeneIsoInfo);
		}
		return setBeforeAfter;
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
	private ArrayListMultimap<String, GffGeneIsoInfo> getIsoSkipHaveBeforeAndAfterExon(int initialNum, 
			Collection<GffGeneIsoInfo> lsIso_ExonSkip) {
		
		ArrayListMultimap<String, GffGeneIsoInfo> setBeforeAfter = ArrayListMultimap.create();

		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonSkip) {
			ExonCluster clusterBefore = exonCluster.exonClusterBefore;
			ExonCluster clusterAfter = exonCluster.exonClusterAfter;
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
			if (lsExons.size() > 1 || isMulitCassette) {
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
