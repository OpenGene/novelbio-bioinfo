package com.novelbio.software.rnaaltersplice.splicetype;

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

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.base.binarysearch.BsearchSite;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.base.binarysearch.ListCodAbs;
import com.novelbio.bioinfo.base.binarysearch.ListCodAbsDu;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;

public class PredictCassette extends SpliceTypePredict {
	private static final Logger logger = Logger.getLogger(PredictCassette.class);
	
	/** 是否严格按照转录本来提取junction的信息 */
	boolean isGetJuncStrict = true;
	Set<GffIso> setExistExonIso;
	Set<GffIso> setSkipExonIso;
	boolean isMulitCassette = false;
	
	/** 在差异exon两侧的exon */
	List<ExonInfo> lsBG = new ArrayList<>();
	
	Align alignDisplay;
	
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
		
		Set<Integer> setAlignExist = new HashSet<Integer>();
		for (GffIso gffGeneIsoInfo : setExistExonIso) {
			List<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
			setAlignExist.add(lsExon.get(0).getStartCis());
			setAlignExist.add(lsExon.get(lsExon.size() - 1).getEndCis());
		}
		Map<String, Double> mapGroup2JunReads = new HashMap<>();
		for (Integer align : setAlignExist) {
			mapGroup2JunReads = addMapDouble(mapGroup2JunReads, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getChrId(), align));
		}
		
		// 像这种 ------exon------------，应该将junction reads的数量减半，这样可以获得更准确的值
		for (String group : mapGroup2JunReads.keySet()) {
			double value = mapGroup2JunReads.get(group);
			mapGroup2JunReads.put(group, value/2);
		}
		addMapGroup2LsValue(mapGroup2LsValue, mapGroup2JunReads);
		addMapGroup2LsValue(mapGroup2LsValue, getSkipReadsNum(condition));
		return mapGroup2LsValue;
	}
	
	/** 完全按照转录本信息来 */
	private ArrayListMultimap<String, Double> getJuncCountsStrict(String condition) {
		HashSet<Align> setAlignSkip = new HashSet<Align>();
		HashSet<Align> setAlignExist = new HashSet<Align>();
		
		for (GffIso gffGeneIsoInfo : setSkipExonIso) {
			int beforeIndex = exonCluster.getMapIso2ExonIndexSkipTheCluster().get(gffGeneIsoInfo);
			int afterIndex = beforeIndex + 1;
			ExonInfo exonBefore = gffGeneIsoInfo.get(beforeIndex);
			ExonInfo exonAfter = gffGeneIsoInfo.get(afterIndex);
			Align align = new Align(exonCluster.getChrId(), exonBefore.getEndCis(), exonAfter.getStartCis());
			setAlignSkip.add(align);
		}
		
		for (GffIso gffGeneIsoInfo : setExistExonIso) {
			List<ExonInfo> lsExon = exonCluster.getIsoExon(gffGeneIsoInfo);
			if (lsExon.size() == 0) {
				continue;
			}
			int beforeIndex = lsExon.get(0).getItemNum() - 1;
			int afterIndex = lsExon.get(lsExon.size() - 1).getItemNum() + 1;
			Align alignBefore = new Align(exonCluster.getChrId(), gffGeneIsoInfo.get(beforeIndex).getEndCis(), lsExon.get(0).getStartCis());
			Align alignAfter = new Align(exonCluster.getChrId(), lsExon.get(lsExon.size() - 1).getEndCis(), 
					gffGeneIsoInfo.get(afterIndex).getStartCis());
			
			setAlignExist.add(alignBefore);
			setAlignExist.add(alignAfter);
		}
		//获得junction reads
		Map<String, Double> mapSkip = null;
		Map<String, Double> mapExist = null;
		for (Align align : setAlignSkip) {
			mapSkip = addMapDouble(mapSkip, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getChrId(), align.getStartAbs(), align.getEndAbs()));
		}
		
		for (Align align : setAlignExist) {
			mapExist = addMapDouble(mapExist, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getChrId(), align.getStartAbs(), align.getEndAbs()));
		}
		
		// 像这种 ------exon------------，应该将junction reads的数量减半，这样可以获得更准确的值
//		for (String group : mapExist.keySet()) {
//			double value = mapExist.get(group);
//			mapExist.put(group, value/2);
//		}
		
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		addMapGroup2LsValue(mapGroup2LsValue, mapExist);
		addMapGroup2LsValue(mapGroup2LsValue, mapSkip);
		return mapGroup2LsValue;
	}
	
	@Override
	protected boolean isType() {
		setExistExonIso = new HashSet<GffIso>();
		setSkipExonIso = new HashSet<GffIso>();
		boolean isType = false;
		int initialNum = -1000;
		
		Set<GffIso> lsIsoSkip = getSetIso2(true);

		Set<GffIso> lsIsoExist = getSetIso2(false);
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

		ArrayListMultimap<String, GffIso> setBeforAfterExist = getIsoExistHaveBeforeAndAfterExon(initialNum, lsIsoExist, lsIsoSkip);
		ArrayListMultimap<String, GffIso> setBeforAfterSkip = getIsoSkipHaveBeforeAndAfterExon(initialNum, lsIsoSkip);
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
	
	private Set<GffIso> getSetIso(boolean isSkip) {
		Set<GffIso> setIso = new HashSet<>();
		for (GffIso gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			if (isSkip && exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() == 0) {
				setIso.add(gffGeneIsoInfo);
			} else if (!isSkip && exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
				setIso.add(gffGeneIsoInfo);
			}
		}
		return setIso;
	}
	
	/** 是否获取跳过当前的iso */
	private Set<GffIso> getSetIso2(boolean isSkip) {
		//key 为该iso跳过的前一个exon的坐标和后一个exon的坐标，用这个来去冗余
		Map<String, GffIso> mapKey2Iso = new HashMap<>();
		
		for (GffIso gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
			//用来去重复的一组exon */
			List<ExonInfo> lsExonToRemoveDuplicate = new ArrayList<>();
			int isoNum = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo).size();
			//没有跳过该exon
			if (!isSkip && isoNum > 0) {
				BsearchSiteDu<ExonInfo> lsCodDu = gffGeneIsoInfo.searchLocationDu(exonCluster.getStartAbs(), exonCluster.getEndAbs());
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
				BsearchSite<ExonInfo> lsInfo = gffGeneIsoInfo.searchLocation((exonCluster.getStartAbs()+exonCluster.getEndAbs())/2);
				if (lsInfo.getAlignUp() != null) {
					lsExonToRemoveDuplicate.add(lsInfo.getAlignUp());
                }
				if (lsInfo.getAlignDown() != null) {
	                		lsExonToRemoveDuplicate.add(lsInfo.getAlignDown());
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
	private ArrayListMultimap<String, GffIso> getIsoExistHaveBeforeAndAfterExon(int initialNum, 
			Set<GffIso> lsIso_ExonExist, Set<GffIso> lsIso_ExonSkip) {
		
		ArrayListMultimap<String, GffIso> setBeforeAfter = ArrayListMultimap.create();
		
		lsBG.clear();
		
		for (GffIso gffGeneIsoInfo : lsIso_ExonExist) {
			ExonCluster clusterBefore = exonCluster.getExonClusterBefore();
			ExonCluster clusterAfter = exonCluster.getExonClusterAfter();
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
					for (GffIso gffIsoSkip : lsIso_ExonSkip) {
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
					Set<GffIso> setIsoSkipBeforExon = clusterBefore.getMapIso2ExonIndexSkipTheCluster().keySet();
					boolean iso_No_Before = true;
					for (GffIso gffSkipBefore : setIsoSkipBeforExon) {
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
				clusterBefore = clusterBefore.getExonClusterBefore();
				numBefore--;
			}
			cancel = false;
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					List<ExonInfo> lsExon = clusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
					if (!gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).equals(lsExon.get(lsExon.size() - 1))) {
						after = true;
					}
					for (GffIso gffIsoSkip : lsIso_ExonSkip) {
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
					Set<GffIso> setIsoSkipAfteExon = clusterAfter.getMapIso2ExonIndexSkipTheCluster().keySet();
					boolean out = true;
					for (GffIso gffSkipAfter : setIsoSkipAfteExon) {
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
				clusterAfter = clusterAfter.getExonClusterAfter();
				numAfter++;
			}
			if (numAfter -  numBefore >= 1 && numAfter - numBefore <=3 && muti_maybe == true) {
				isMulitCassette = true;
			}
			
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.put(tmpBeforeAfter, gffGeneIsoInfo);
		}
		removeRedundantBG();
		return setBeforeAfter;
	}
	
	/** 去除重复位点 */
	private void removeRedundantBG() {
		if (lsBG.isEmpty()) {
			return;
		}
		List<ExonInfo> lsBGnew = new ArrayList<>();//去除冗余的BG
		Set<String> setSite = new HashSet<>();
		for (ExonInfo exonInfo : lsBG) {
			if (setSite.contains(exonInfo.getStartAbs() + SepSign.SEP_ID + exonInfo.getEndAbs())) {
				continue;
			}
			setSite.add(exonInfo.getStartAbs() + SepSign.SEP_ID + exonInfo.getEndAbs());
			lsBGnew.add(exonInfo);
		}		
		lsBG = lsBGnew;
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
	private ArrayListMultimap<String, GffIso> getIsoSkipHaveBeforeAndAfterExon(int initialNum, 
			Collection<GffIso> lsIso_ExonSkip) {
		
		ArrayListMultimap<String, GffIso> setBeforeAfter = ArrayListMultimap.create();

		for (GffIso gffGeneIsoInfo : lsIso_ExonSkip) {
			ExonCluster clusterBefore = exonCluster.getExonClusterBefore();
			ExonCluster clusterAfter = exonCluster.getExonClusterAfter();
			int[] beforeAfter = new int[]{initialNum, initialNum};//初始化为负数
			int numBefore = 0, numAfter = 0;//直接上一位的exon标记为0，再向上一位标记为-1
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[0] = numBefore;
					break;
				}
				clusterBefore = clusterBefore.getExonClusterBefore();
				numBefore--;
			}
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[1] = numAfter;
					break;
				}
				clusterAfter = clusterAfter.getExonClusterAfter();
				numAfter++;
			}
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.put(tmpBeforeAfter, gffGeneIsoInfo);
		}
		return setBeforeAfter;
	}
	
	@Override
	public SplicingAlternativeType getType() {
		for (GffIso gffGeneIsoInfo : setExistExonIso) {
			List<ExonInfo> lsExons = exonCluster.getIsoExon(gffGeneIsoInfo);
			if (lsExons.size() > 1 || isMulitCassette) {
				return SplicingAlternativeType.cassette_multi;
			}
		}
		return SplicingAlternativeType.cassette;
	}

	@Override
	public List<Align> getDifSite() {
		List<Align> lsAligns = new ArrayList<>();

		Map<String, Align> mapKey2Align = new HashMap<>();
		for (GffIso gffGeneIsoInfo : setExistExonIso) {
			List<ExonInfo> lsExons = exonCluster.getIsoExon(gffGeneIsoInfo);
			if (lsExons.size() > 1) {
				for (ExonInfo exonInfo : lsExons) {
					Align align = new Align(exonInfo);
					align.setChrId(exonCluster.getChrId());
					mapKey2Align.put(align.getStartAbs() + SepSign.SEP_ID + align.getEndAbs(), align);
				}
			}
		}
				
		if (mapKey2Align.isEmpty()) {
			Align align = new Align(exonCluster.getChrId(), exonCluster.getStartCis(), exonCluster.getEndCis());
			lsAligns.add(align);
			return lsAligns;
		}

		List<Align> lsAlignTmp = new ArrayList<>(mapKey2Align.values());
		Collections.sort(lsAlignTmp, new Comparator<Align>() {
			public int compare(Align o1, Align o2) {
				Integer start1 = o1.getStartAbs();
				Integer end1 = o1.getEndAbs();
				Integer start2 = o2.getStartAbs();
				Integer end2 = o2.getEndAbs();
				if (start1 != start2) {
					return start1.compareTo(start2);
				} else {
					return end1.compareTo(end2);
				}
			}
		});
		
		Align alignOld = lsAlignTmp.get(0);
		lsAligns.add(alignOld);
		for (int i = 1; i < lsAlignTmp.size(); i++) {
			Align align = lsAlignTmp.get(i);
			if (Alignment.isOverlap(align, alignOld)) {
				continue;
			} else {
				lsAligns.add(align);
				alignOld = align;
			}
		}
		return lsAligns;
	}

	@Override
	public List<? extends Alignment> getBGSiteSplice() {
		if (!lsBG.isEmpty()) {
			return lsBG;
		} else {
			GffIso iso = exonCluster.getParentGene().getLongestSplitMrna();
			return iso.getLsElement();
		}
	}
	public Align getResultSite() {
		List<Align> lsDifSite = getDifSite();
		List<Alignment> lsResult = new ArrayList<>();
		lsResult.addAll(lsBG);
		lsResult.addAll(lsDifSite);
		return Align.getAlignFromList(lsResult);
	}
}
