package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

/** 专门用来提取exoncluster的类 */
public class ExonClusterExtract {
	GffDetailGene gene;
	int minDifLen = 6;
	
	/**
	 * @param gene
	 * @param minDifLen 小于6bp的altstart和altend都有必要删除，很可能是假的
	 */
	public ExonClusterExtract(GffDetailGene gene, int minDifLen) {
		this.gene = gene;
		this.minDifLen = minDifLen;
	}
	
	/**
	 * 仅用于测试<br>
	 * 返回有差异的exon系列，用来分析差异可变剪接
	 * 因此只返回该位点存在转录本并且有差异的位点
	 * 譬如<br>
	 *  1--2------------3--4----------5--6<br>
	 *  1--2------------3--4----------5--6<br>
	 *  -----------------------------------5‘-6’<br>
	 *  1-2和3-4不返回<br>
	 *  返回5-6<br>
	 * @return
	 */
	protected List<ExonCluster> getLsDifExon() {
		List<ExonClusterSite> lsClusterSites = getLsDifExonSite();
		List<ExonCluster> lsExonClusters = new ArrayList<>();
		for (ExonClusterSite exonClusterSite : lsClusterSites) {
			lsExonClusters.addAll(exonClusterSite.getLsExonCluster());
		}
		return lsExonClusters;
	}
	
	/**
	 * 返回有差异的exon系列，用来分析差异可变剪接
	 * 因此只返回该位点存在转录本并且有差异的位点
	 * 譬如<br>
	 *  1--2------------3--4----------5--6<br>
	 *  1--2------------3--4----------5--6<br>
	 *  -----------------------------------5‘-6’<br>
	 *  1-2和3-4不返回<br>
	 *  返回5-6<br>
	 * @return
	 */
	public List<ExonClusterSite> getLsDifExonSite() {
		List<GffGeneIsoInfo> lsSameGroupIso = getLsGffGeneIsoSameGroup();
		/**
		 * 一个基因如果有不止一个的转录本，那么这些转录本的同一区域的exon就可以提取出来，并放入该list
		 * 也就是每个exoncluster就是一个exon类，表示 
		 */
		return addExonCluster(lsSameGroupIso);

	}
	
	/**
	 * 将lsSameGroupIso切分成一个一个的exoncluster，然后装入mapLoc2DifExonCluster中
	 * @param alignRetainIntron 仅将被该位点覆盖的exoncluster装入mapLoc2DifExonCluster，如果alignRetainIntron为null，则将全体exoncluster装入mapLoc2DifExonCluster
	 * @param mapLoc2DifExonCluster
	 * @param lsSameGroupIso
	 */
	private List<ExonClusterSite> addExonCluster(List<GffGeneIsoInfo> lsSameGroupIso ) {
		if (lsSameGroupIso.size() <= 1) {
			return new ArrayList<>();
		}
		boolean cis5to3 = lsSameGroupIso.get(0).isCis5to3();
		List<ExonClusterSite> lsResult = ExonClusterSite.generateLsExonCluster(getLsExonClusterInRegion(null, cis5to3, lsSameGroupIso));
		
		if (lsSameGroupIso.size() <= 2) {
			return lsResult;
		}
		//取出包含 retain_intron形式的exonCluster
		List<ExonCluster> lsExonClusterLong = new ArrayList<>();
		for (ExonClusterSite exonClusterSite : lsResult) {
			ExonCluster exonCluster = exonClusterSite.getCurrentExonCluster();
			//含有特别长exon的iso，要把他们除去再做分析
			Set<SplicingAlternativeType> setSpliceType = exonCluster.getSplicingTypeSet();
			if (setSpliceType.contains(SplicingAlternativeType.unknown) || setSpliceType.contains(SplicingAlternativeType.retain_intron) || 
					
					((setSpliceType.contains(SplicingAlternativeType.cassette) 
							|| setSpliceType.contains(SplicingAlternativeType.cassette_multi)) 
					&& 
					(setSpliceType.contains(SplicingAlternativeType.alt5) || setSpliceType.contains(SplicingAlternativeType.alt3) 
							|| setSpliceType.contains(SplicingAlternativeType.altstart) || setSpliceType.contains(SplicingAlternativeType.altend)))
					) {
				lsExonClusterLong.add(exonCluster);
			}
		}
		
		getLsExonInRegionRecur(null, cis5to3, lsExonClusterLong, lsSameGroupIso);
		return lsResult;
	}
	
	private void getLsExonInRegionRecur(ExonClusterSite exonClusterSite, Boolean cis5to3, 
			List<ExonCluster> lsExonClusterLong, List<GffGeneIsoInfo> lsIso) {
		boolean isTopLevel = exonClusterSite == null ? true : false;

		for (ExonCluster exonCluster : lsExonClusterLong) {
			boolean isRetainIntron = exonCluster.getSplicingTypeSet().contains(SplicingAlternativeType.retain_intron);
			List<GffGeneIsoInfo> lsIsoRemoveLong = removeLongExon(isRetainIntron, exonCluster, lsIso);
			if (lsIsoRemoveLong.size() < 2) continue;
			
			List<ExonCluster> lsExonClustersSub = getLsExonClusterInRegion(exonCluster, cis5to3, lsIsoRemoveLong);
			if (lsExonClustersSub.isEmpty()) continue;
			
			if (isTopLevel) {
				exonClusterSite = exonCluster.getExonClusterSite();
			}
			exonClusterSite.addAll(lsExonClustersSub);
			
			getLsExonInRegionRecur(exonClusterSite, cis5to3, lsExonClustersSub, lsIsoRemoveLong);
		}
	}
	
	/**
	 * 把新的这一系列iso分组然后装入Map表
	 * @param align 仅将与align有overlap的exon cluster写入Map。如果align为null，则全部都写入map表
	 * @param cis5to3
	 * @param lsSameGroupIso
	 * @param mapChrID2ExonClusters 待写入的map表
	 * @return 返回本次添加到map中的全体exonClusters
	 */
	private List<ExonCluster> getLsExonClusterInRegion(Alignment align, Boolean cis5to3, List<GffGeneIsoInfo> lsSameGroupIso) {
		List<ExonCluster> lsResult = new ArrayList<>();
		if (lsSameGroupIso.size() <= 1) {
			return lsResult;
		}
		List<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(cis5to3, lsSameGroupIso);
		for (ExonCluster exonClusters : lsExonClusters) {
			if (align != null && (exonClusters.getStartAbs() > align.getEndAbs() || exonClusters.getEndAbs() < align.getStartAbs() )) {
				continue;
			}
			
			if (exonClusters.isSameExonInExistIso() || exonClusters.getSplicingTypeSet(minDifLen).isEmpty()) {
				continue;
			}
			lsResult.add(exonClusters);
		}
		return lsResult;
	}
	
	
	private List<GffGeneIsoInfo> removeLongExon(boolean isRetainIntron, ExonCluster exonCluster, List<GffGeneIsoInfo> lsIsoRaw) {
		if (isRetainIntron) {
			return removeLongExonRetainIntron(exonCluster, lsIsoRaw);
		} else {
			return removeLongExonOther(exonCluster, lsIsoRaw);
		}
	}
	
	/**
	 * 	里面的连续两个exon中间的intron
		如果发现有转录本覆盖了该intron，那么就是造成retain intron的那个转录本，把它去除就好
	 * 去除含有长exon后的转录本集合
	 * @return
	 */
	private ArrayList<GffGeneIsoInfo> removeLongExonRetainIntron(ExonCluster exonCluster, List<GffGeneIsoInfo> lsIsoRaw) {
		//如果存在多个长的exon，则删除覆盖最长intron的序列
		ArrayListMultimap<Integer, Align> mapExonLen2IntronLen = ArrayListMultimap.create();
		//按照exon长度进行排序
		//1----------2+++3----4++5--------
		//2--------1+++++3----4++++6
		//3----------2+++++++++++5---------
		//这里会获得 1 和 2,那么2就排在1之前
		TreeSet<Integer> treeExonLen = new TreeSet<>(new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
	            return -o1.compareTo(o2);
            }
		});
		
		for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
			if (lsexoninfo.size() > 1) {
				int exonLen = lsExonLenAll(lsexoninfo);
				treeExonLen.add(exonLen);
				Align alignIntron = new Align(exonCluster.getRefID(), lsexoninfo.get(0).getEndCis(), lsexoninfo.get(1).getStartCis());
				mapExonLen2IntronLen.put(exonLen, alignIntron);
			}
		}
		if (treeExonLen.isEmpty()) {
	        	return new ArrayList<>();
        }
		//获得这种长的iso
		List<ExonInfo> lsExons = null;
		HashSet<GffGeneIsoInfo> setGeneIsoWithLongExon = new HashSet<GffGeneIsoInfo>();
		for (int exonLen : treeExonLen) {
			//某一对exon中间的intron，如
			//1----------2+++3----4++5--------
			//2--------1+++++3----4++++6
			//3----------2+++++++++++5---------
			//这里获得 3---4
			//因为可能有多个复杂的exon对，如上面的转录本 1 和 2 ，那么就优先选择exon长度比较长的转录本的intron
			List<Align> lsIntron = mapExonLen2IntronLen.get(exonLen);
			for (Align alignIntron : lsIntron) {
				for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
					if (lsexoninfo.size() > 0
							&& lsexoninfo.get(0).getStartAbs() < alignIntron.getStartAbs()
							&& lsexoninfo.get(0).getEndAbs() > alignIntron.getEndAbs())
					{
						if (lsexoninfo.size() == 1) {
							setGeneIsoWithLongExon.add(lsexoninfo.get(0).getParent());
		                }
						if (lsExons == null || lsExonLenAll(lsexoninfo) > lsExonLenAll(lsExons)) {
							lsExons = lsexoninfo;
		                }
					}
				}
				if (!setGeneIsoWithLongExon.isEmpty()) {
					break;
                }
            }
			if (!setGeneIsoWithLongExon.isEmpty()) {
				break;
            }
        }
		

		
		if (setGeneIsoWithLongExon.isEmpty()) {
			if (lsExons != null && !lsExons.isEmpty()) {
				setGeneIsoWithLongExon.add(lsExons.get(0).getParent());
			} else {
				//可能存在这种情况
				//-----------1+++5----10+++15------
				//-------------2++++7-------------
				//-----------------6+++++12--------
				//这时候就没有retain intron的存在，setGeneIsoWithLongExon为空
				//这里就随便删掉一个最长的，譬如把6++++12删掉
				GffGeneIsoInfo isoLongestExon = null;
				int exonLen = 0;
				for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
					if (lsexoninfo.size() == 1 && (isoLongestExon == null || exonLen < lsLenAll(lsexoninfo))) {
						isoLongestExon = lsexoninfo.get(0).getParent();
					}
				}
				setGeneIsoWithLongExon.add(isoLongestExon);
			}
        }
		ArrayList<GffGeneIsoInfo> lsSameGroupIsoNew = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsoRaw) {
			if (setGeneIsoWithLongExon.contains(gffGeneIsoInfo)) {
				continue;
			}
			lsSameGroupIsoNew.add(gffGeneIsoInfo);
		}
		return lsSameGroupIsoNew;
	}
	/**
	 * 	里面的连续两个exon中间的intron
		如果发现有转录本覆盖了该intron，那么就是造成retain intron的那个转录本，把它去除就好
	 * 去除含有长exon后的转录本集合
	 * @return
	 */
	private ArrayList<GffGeneIsoInfo> removeLongExonOther(ExonCluster exonCluster, List<GffGeneIsoInfo> lsIsoRaw) {
		int exonLenMaxAll = 0, exonLenMax = 0, exonLenMinAll = 0, exonLenMin = 0;
		ExonInfo exonMax = null;
		int isoHaveExonNum = 0;//在该位点含有exon的iso有几个
		for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
			if (lsexoninfo.isEmpty()) continue;
			
			isoHaveExonNum++;
			ExonInfo exonMaxNow = lsExonLenMaxAlign(lsexoninfo);
			int exonLenNowAll = lsLenAll(lsexoninfo);
			if (exonLenMax == 0 ) {
				exonLenMin = exonMaxNow.getLength();
				exonLenMax = exonMaxNow.getLength();
				exonMax = exonMaxNow;
			} else if (exonMaxNow.getLength() < exonLenMin) {
				exonLenMin = exonMaxNow.getLength();
			} else if (exonMaxNow.getLength()  > exonLenMax) {
				exonLenMax = exonMaxNow.getLength();
				exonMax = exonMaxNow;
			}
			
			if (exonLenMaxAll == 0 ) {
				exonLenMinAll = exonLenNowAll;
				exonLenMaxAll = exonLenNowAll;
			} else if (exonLenNowAll < exonLenMinAll) {
				exonLenMinAll = exonLenNowAll;
			} else if (exonLenNowAll > exonLenMaxAll) {
				exonLenMaxAll = exonLenNowAll;
			}
		}
		if (isoHaveExonNum == 0 || Math.abs(exonLenMax - exonLenMin) < 50 || (double)exonLenMax/exonLenMin < 2) {
			return new ArrayList<>();
		}
		
		//在删除长的转录本的时候，如果正好该转录本的边缘跟某个转录本第一个exon的边缘一致，就把该转录本也删了，否则会误判为altstart或altend
		//如存在 -----------------------15==20-------30======60----------90==100
		//----------------------------------15==20-------30===40
		//这时候就把第二个转录本也删了
		Set<GffGeneIsoInfo> setIsoNeedToRemove = new HashSet<>();
		//左端边缘与该exon的长度，也就是 key：30  value：(60-30) 也是30
		Map<Integer, Integer> mapStart2Len = new HashMap<>();
		//右端边缘与该exon的长度
		Map<Integer, Integer> mapEnd2Len = new HashMap<>();
		
		for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
			if (lsexoninfo.isEmpty()) continue;
			
			int exonLenNow = lsExonLenMax(lsexoninfo);
			ExonInfo exonInfoNow = lsExonLenMaxAlign(lsexoninfo);
			if ((double)exonLenNow / exonLenMax > 0.8 && overlap(exonInfoNow, exonMax) > 0.9) {
				setIsoNeedToRemove.add(lsexoninfo.get(0).getParent());
				int start = lsexoninfo.get(0).getStartCis();
				int end = lsexoninfo.get(lsexoninfo.size() - 1).getEndCis();
				
				if (!mapStart2Len.containsKey(start) || mapStart2Len.get(start) < exonLenNow) {
					mapStart2Len.put(start, exonLenNow);
				}
				if (!mapEnd2Len.containsKey(end) || mapEnd2Len.get(end) < exonLenNow) {
					mapEnd2Len.put(end, exonLenNow);
				}
			}
		}
		
		for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
			if (lsexoninfo.isEmpty() || lsexoninfo.get(0).getItemNum() != 0 || lsexoninfo.size() > 1) continue;
			
			int end = lsexoninfo.get(0).getEndCis();
			int len = lsLenAll(lsexoninfo);
			if (!mapEnd2Len.containsKey(end)) {
				continue;
			}
			
			int lenOther = mapEnd2Len.get(end);
			if (lenOther >= len) {
				setIsoNeedToRemove.add(lsexoninfo.get(0).getParent());
			}
		}
		
		for (List<ExonInfo> lsexoninfo : exonCluster.getLsIsoExon()) {
			if (lsexoninfo.isEmpty() || lsexoninfo.get(0).getItemNum() != (lsexoninfo.get(0).getParent().size() - 1) || lsexoninfo.size() > 1) continue;
			
			int start = lsexoninfo.get(0).getStartCis();
			int len = lsLenAll(lsexoninfo);
			if (!mapStart2Len.containsKey(start)) {
				continue;
			}
			
			int lenOther = mapStart2Len.get(start);
			if (lenOther >= len) {
				setIsoNeedToRemove.add(lsexoninfo.get(0).getParent());
			}
		}
	
		
		ArrayList<GffGeneIsoInfo> lsSameGroupIsoNew = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsoRaw) {
			if (setIsoNeedToRemove.contains(gffGeneIsoInfo)) {
				continue;
			}
			lsSameGroupIsoNew.add(gffGeneIsoInfo);
		}
		return lsSameGroupIsoNew;
	}
	
	/** 两个区段的overlap，返回小数，是与短一点的 exon的比值 */
	private double overlap(Alignment alg1, Alignment alg2) {
		int start = Math.min(alg1.getStartAbs(), alg2.getStartAbs());
		int end = Math.max(alg1.getEndAbs(), alg2.getEndAbs());
		int len = end - start;
		if (len <= 0) {
			return 0;
		}
		int minLen = Math.min(alg1.getLength(), alg2.getLength());
		double overlapThis = (double)len/minLen;
		return overlapThis*100;
	}
	
	private int lsExonLenAll(List<ExonInfo> lsExonInfos) {
		int len = 0;
		for (ExonInfo exonInfo : lsExonInfos) {
			len+=exonInfo.getLength();
        }
		return len;
	}
	private int lsLenAll(List<ExonInfo> lsExonInfos) {
		int start = Math.min(lsExonInfos.get(0).getStartAbs(), lsExonInfos.get(lsExonInfos.size()-1).getStartAbs());
		int end = Math.max(lsExonInfos.get(0).getEndAbs(), lsExonInfos.get(lsExonInfos.size()-1).getEndAbs());

		return end - start;
	}
	private int lsExonLenMax(List<ExonInfo> lsExonInfos) {
		int len = 0;
		for (ExonInfo exonInfo : lsExonInfos) {
			if (len < exonInfo.getLength()) {
				len = exonInfo.getLength();
			}
        }
		return len;
	}
	private ExonInfo lsExonLenMaxAlign(List<ExonInfo> lsExonInfos) {
		int len = 0;
		ExonInfo exonMax = null;
		for (ExonInfo exonInfo : lsExonInfos) {
			if (len < exonInfo.getLength()) {
				len = exonInfo.getLength();
				exonMax = exonInfo;
			}
        }
		return exonMax;
	}
	/** 返回iso基本接近的一组做可变剪接分析
	 * 只有当几个iso中只有少数几个exon的差距，才能做可变剪接的分析
	 *  */
	private List<GffGeneIsoInfo> getLsGffGeneIsoSameGroup() {
		//存放lsiso组，每次输入的iso在组内查找最接近的组，然后放进去
		List<IsoGroup> lsIsoGroup = new ArrayList<IsoGroup>();
		boolean flagGetNexIso = false;
		double prop = getSimilarProp();
		if (gene.getLsCodSplit().size() <= 3) {
			ArrayList<GffGeneIsoInfo> lsResult = new ArrayList<GffGeneIsoInfo>();
			for (GffGeneIsoInfo gffGeneIsoInfo : gene.getLsCodSplit()) {
				lsResult.add(gffGeneIsoInfo);
			}
			return lsResult;
		}
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gene.getLsCodSplit()) {
			flagGetNexIso = false;
			for (IsoGroup isoGroup : lsIsoGroup) {
				if (isoGroup.getSameEdgeProp(gffGeneIsoInfo) >= prop) {
					isoGroup.addIso(gffGeneIsoInfo);
					flagGetNexIso = true;
					break;
				}
			}
			//没找到最接近的iso，就新建一个list把这个iso加进去
			if (!flagGetNexIso) {
				IsoGroup isoGroup = new IsoGroup();
				isoGroup.addIso(gffGeneIsoInfo);
				lsIsoGroup.add(isoGroup);
			}
		}
		//找出含有iso最多的组
		IsoGroup isoGroupMax = new IsoGroup();
		for (IsoGroup isoGroup : lsIsoGroup) {
			if (isoGroupMax.getIsoNum() < isoGroup.getIsoNum()) {
				isoGroupMax = isoGroup;
			}
		}
		return isoGroupMax.lsGffGeneIsoInfos;
	}
	
	//TODO
	//两种策略，1：用稍微低一点prop，挑选出一组iso然后分析
	//2.用稍微高一点的prop，挑出几组iso，然后每组都做分析
	private double getSimilarProp() {
		if (gene.getLsCodSplit().size() <= 3) {
			return 0.6;
		}
		//倒序排序
		ArrayList<Integer> lsExonNum = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gene.getLsCodSplit()) {
			lsExonNum.add(gffGeneIsoInfo.size());
		}
		Collections.sort(lsExonNum, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		int exonNum1 = 0;//最多转录本的exon数
		int exonNum2 = 0;//第二多转录本的exon数
		exonNum1 = lsExonNum.get(0);
		exonNum2 = lsExonNum.get(1);
		
		if (exonNum1 <= 3) {
			return 0.3;
		} else if (exonNum2 > 3) {
			return 0.5;
		} else {
			return 0.5;
		}
	}
}


class IsoGroup {
	List<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<>();
	Set<Integer> setEdge = new HashSet<>();
	
	public int getSameEdge(GffGeneIsoInfo gffGeneIsoInfo) {
		int i = 0;
		for (ExonInfo exonInfo : gffGeneIsoInfo) {
			if (setEdge.contains(exonInfo.getStartAbs())) {
				i++;
			}
			if (setEdge.contains(exonInfo.getEndAbs())) {
				i++;
			}
		}
		return i;
	}
	
	public double getSameEdgeProp(GffGeneIsoInfo gffGeneIsoInfo) {
		int samEdge = getSameEdge(gffGeneIsoInfo);
		double edgePropThis = samEdge/gffGeneIsoInfo.size();
		double edgeSet = samEdge/setEdge.size();
		return Math.max(edgeSet, edgePropThis);
	}
	
	public boolean isEmpty() {
		return setEdge.size() == 0;
	}
	public int getIsoNum() {
		return lsGffGeneIsoInfos.size();
	}
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo) {
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		for (ExonInfo exonInfo : gffGeneIsoInfo) {
			setEdge.add(exonInfo.getStartAbs());
			setEdge.add(exonInfo.getEndAbs());
		}
	}
}
