package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.freehep.graphicsio.swf.End;

import com.google.common.collect.ArrayListMultimap;
import com.hg.doc.en;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.listOperate.ListCodAbs;
import com.novelbio.listOperate.ListCodAbsDu;

public class PredictCassette extends SpliceTypePredict {
	private static final Logger logger = Logger.getLogger(PredictCassette.class);
	
	/** 是否严格按照转录本来提取junction的信息 */
	boolean isGetJuncStrict = true;
	HashSet<GffGeneIsoInfo> setExistExonIso;
	HashSet<GffGeneIsoInfo> setSkipExonIso;
	boolean isMulitCassette = false;
	
	/** 在差异exon两侧的exon */
	List<ExonInfo> lsBG = new ArrayList<>();
	
	
	public PredictCassette(ExonCluster exonCluster) {
		super(exonCluster);
	}
	
	@Override
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
		if (isGetJuncStrict) {
			return getJuncCountsStrict(condition);
		} else {
			return getJuncCountsLess(condition);
		}
	}
	
	/** 不完全按照转录本信息来 */
	private ArrayListMultimap<String, Double> getJuncCountsLess(String condition) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		addMapGroup2LsValue(mapGroup2LsValue, getSkipReadsNum(condition));
		Set<Integer> setAlignExist = new HashSet<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : setExistExonIso) {
			List<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
			setAlignExist.add(lsExon.get(0).getStartCis());
			setAlignExist.add(lsExon.get(lsExon.size() - 1).getEndCis());
		}
		Map<String, Double> mapGroup2JunReads = new HashMap<>();
		for (Integer align : setAlignExist) {
			mapGroup2JunReads = addMapDouble(mapGroup2JunReads, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align));
		}
		
		// 像这种 ------exon------------，应该将junction reads的数量减半，这样可以获得更准确的值
		for (String group : mapGroup2JunReads.keySet()) {
			double value = mapGroup2JunReads.get(group);
			mapGroup2JunReads.put(group, value/2);
		}
		addMapGroup2LsValue(mapGroup2LsValue, mapGroup2JunReads);
		return mapGroup2LsValue;
	}
	
	/** 完全按照转录本信息来 */
	private ArrayListMultimap<String, Double> getJuncCountsStrict(String condition) {
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
			List<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
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
		Map<String, Double> mapSkip = null;
		Map<String, Double> mapExist = null;
		for (Align align : setAlignSkip) {
			mapSkip = addMapDouble(mapSkip, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs()));
		}
		
		for (Align align : setAlignExist) {
			mapExist = addMapDouble(mapExist, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs(), align.getEndAbs()));
		}
		
		// 像这种 ------exon------------，应该将junction reads的数量减半，这样可以获得更准确的值
//		for (String group : mapExist.keySet()) {
//			double value = mapExist.get(group);
//			mapExist.put(group, value/2);
//		}
		
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		addMapGroup2LsValue(mapGroup2LsValue, mapSkip);
		addMapGroup2LsValue(mapGroup2LsValue, mapExist);
		return mapGroup2LsValue;
	}

	@Override
	protected boolean isType() {
		setExistExonIso = new HashSet<GffGeneIsoInfo>();
		setSkipExonIso = new HashSet<GffGeneIsoInfo>();
		boolean isType = false;
		int initialNum = -1000;
		
		Set<GffGeneIsoInfo> lsIsoSkip = getSetIso2(true);

		Set<GffGeneIsoInfo> lsIsoExist = getSetIso2(false);
		if (lsIsoSkip.size() == 0) {
			return false;
		}
//		Set<GffGeneIsoInfo> lsIsoExist = new HashSet<>();
//		Set<GffGeneIsoInfo> lsIsoSkip = new HashSet<>();
//		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
//			if (exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() == 0) {
//				lsIsoSkip.add(gffGeneIsoInfo);
//			} else {
//				lsIsoExist.add(gffGeneIsoInfo);
//			}
//		}

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
	
	private Set<GffGeneIsoInfo> getSetIso(boolean isSkip) {
		Set<GffGeneIsoInfo> setIso = new HashSet<>();
		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			if (isSkip && exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() == 0) {
				setIso.add(gffGeneIsoInfo);
			} else if (!isSkip && exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
				setIso.add(gffGeneIsoInfo);
			}
		}
		return setIso;
	}
	
	/** 是否获取跳过当前的iso */
	private Set<GffGeneIsoInfo> getSetIso2(boolean isSkip) {
		//key 为该iso跳过的前一个exon的坐标和后一个exon的坐标，用这个来去冗余
		Map<String, GffGeneIsoInfo> mapKey2Iso = new HashMap<>();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			//用来去重复的一组exon */
			List<ExonInfo> lsExonToRemoveDuplicate = new ArrayList<>();
			int isoNum = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size();
			//没有跳过该exon
			if (!isSkip && isoNum > 0) {
				ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>> lsCodDu = gffGeneIsoInfo.searchLocationDu(exonCluster.getStartAbs(), exonCluster.getEndAbs());
				List<ExonInfo> lsExonInfos = lsCodDu.getCoveredElement();
				Collections.sort(lsExonInfos);
				int start = 0, end = -1;
				if (lsExonInfos.size() > 0) {
					start = gffGeneIsoInfo.indexOf(lsExonInfos.get(0));
					end = gffGeneIsoInfo.indexOf(lsExonInfos.get(lsExonInfos.size() - 1));
	            }
				if (start > 0) {
					lsExonToRemoveDuplicate.add(gffGeneIsoInfo.get(start - 1));
	            }
				lsExonToRemoveDuplicate.addAll(lsExonInfos);
				if (end < gffGeneIsoInfo.size() - 1) {
					lsExonToRemoveDuplicate.add(gffGeneIsoInfo.get(end + 1));
	            }
			} else if(isSkip &&isoNum == 0) {
				ListCodAbs<ExonInfo> lsInfo = gffGeneIsoInfo.searchLocation((exonCluster.getStartAbs()+exonCluster.getEndAbs())/2);
				if (lsInfo.getGffDetailUp() != null) {
					lsExonToRemoveDuplicate.add(lsInfo.getGffDetailUp());
                }
				if (lsInfo.getGffDetailDown() != null) {
	                		lsExonToRemoveDuplicate.add(lsInfo.getGffDetailDown());
                }
			}
			StringBuilder keybuilder = new StringBuilder();
			if (lsExonToRemoveDuplicate.isEmpty()) {
				continue;
			}
			for (ExonInfo exonInfo : lsExonToRemoveDuplicate) {
				keybuilder.append(exonInfo.toString());
            }
			mapKey2Iso.put(keybuilder.toString(), gffGeneIsoInfo);
		}
		return new HashSet<>(mapKey2Iso.values());
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
		
		lsBG.clear();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			ExonCluster clusterBefore = exonCluster.exonClusterBefore;
			ExonCluster clusterAfter = exonCluster.exonClusterAfter;
			int[] beforeAfter = new int[]{initialNum, initialNum};//初始化为负数
			int numBefore = 0, numAfter = 0;//直接上一位的exon标记为0，再向上一位标记为-1
			boolean cancel = false;
			boolean muti_maybe = false, before = false, after = false;
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					List<ExonInfo> lsExon = clusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo);
					if (!gffGeneIsoInfo.get(0).equals(lsExon.get(0))) {
						before = true;
					}
					for (GffGeneIsoInfo gffIsoSkip : lsIso_ExonSkip) {
						List<ExonInfo> lsExonBefore = clusterBefore.getMapIso2LsExon().get(gffIsoSkip);
						if (lsExonBefore != null && lsExonBefore.size() > 0) {
							beforeAfter[0] = numBefore;
							lsBG.addAll(lsExonBefore);
							cancel = true;
							break;
						}
					}
					if (cancel) break;
				} else {
					//如果本exoncluster中含有 跳过当前exon的iso，并且在beforecluster中也跳过exon，那么就继续检查上一个exoncluster
					//如果beforecluster的跳过iso中不含有该iso，说明该iso就不存在
					//         beforeCluster     当前Cluster
					// 1--2--------3--4--------------------------------------7--8
					//  1--2--------------------------------5--6-------------7--8
					// 1--2----------------------------------------------------7--8 //这就是 跳过当前cluster，还跳过 before cluster的 iso
					Set<GffGeneIsoInfo> setIsoSkipBeforExon = clusterBefore.getMapIso2ExonIndexSkipTheCluster().keySet();
					boolean iso_No_Before = true;
					for (GffGeneIsoInfo gffSkipBefore : setIsoSkipBeforExon) {
						if (lsIso_ExonSkip.contains(gffSkipBefore)) {
							iso_No_Before = false;
							break;
						}
					}
					if (iso_No_Before) {
						break;
					}
				}
				if (before) {
					muti_maybe = true;
				}
				clusterBefore = clusterBefore.exonClusterBefore;
				numBefore--;
			}
			cancel = false;
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					List<ExonInfo> lsExon = clusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
					if (!gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).equals(lsExon.get(lsExon.size() - 1))) {
						after = true;
					}
					for (GffGeneIsoInfo gffIsoSkip : lsIso_ExonSkip) {
						List<ExonInfo> lsExonAfter = clusterAfter.getMapIso2LsExon().get(gffIsoSkip);
						if (lsExonAfter != null && lsExonAfter.size() > 0) {
							beforeAfter[1] = numAfter;
							lsBG.addAll(lsExonAfter);
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
					muti_maybe = true;
				}
				clusterAfter = clusterAfter.exonClusterAfter;
				numAfter++;
			}
			if (numAfter -  numBefore >= 1 && numAfter - numBefore <=3 && muti_maybe == true) {
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
			List<ExonInfo> lsExons = exonCluster.getIsoExon(gffGeneIsoInfo);
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
		if (!lsBG.isEmpty()) {
			return lsBG;
		} else {
			GffGeneIsoInfo iso = exonCluster.getParentGene().getLongestSplitMrna();
			return iso.getLsElement();
		}
	}

}
