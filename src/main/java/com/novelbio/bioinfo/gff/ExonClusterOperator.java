package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novelbio.bioinfo.base.AlignExtend;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.base.binarysearch.ListEle;

public class ExonClusterOperator {

	/**
	 * <b>目前仅用于差异可变剪接查找具体哪个exon发生了剪接事件</b><br>
	 * 给定一系列ListElement，以及一个方向。
	 * 将相同方向的ListElement提取出来，然后合并，然后找出这些element的共同边界
	 * @param cis5to3 null,不考虑方向
	 * @param lsIso
	 * 	 * ---a--a---------b----b-------------<br>
	 *    ---m----------------n----<br>
	 *    得到：---m--a-------------b----b----------
	 * @return
	 * 返回一个list，按照cis5to3排序，如果cis5to3为true，从小到大排列
	 * 如果cis5to3为false，从大到小排列
	 * 内部的int[] 0: startAbs 1: endAbs
	 */
	public static List<int[]> getSep(Boolean cis5to3, List<GffIso> lsIso) {
		return getCombSep(cis5to3, lsIso, true);
	}

	/**
	 * 仅用与差异可变剪接
	 * 将可能存在multi-cassette这种，多个exon的也放到一个 ExonCluster 中
	 * 并和之前的cluster合并
	 * @param cis5To3
	 * @param lsGffGeneIsoInfos
	 * @return
	 */
	public static ArrayList<ExonCluster> getExonCluster(Boolean cis5To3, List<GffIso> lsGffGeneIsoInfos) {
		String chrID = lsGffGeneIsoInfos.get(0).getRefIDlowcase();

		ArrayList<ExonCluster> lsExonClusters = getExonClusterSingle(cis5To3, lsGffGeneIsoInfos, false);
		List<ExonCluster> lsExonClustersNew = new ArrayList<>();
		ExonCluster exonClusterBefore = null;
		ExonCluster exonClusterBeforeReal = null;
		int[] exonMultiSE = null;
		
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			if (exonClusterBefore == null) {
				exonClusterBefore = exonCluster;
				exonClusterBeforeReal = exonClusterBefore;
				continue;
			}
			//判断是否存在类型
			// 10=20----30=40----50=60-------70=80---
			// 10=20-------------------------70=80---
			if (!exonClusterBefore.getMapIso2ExonIndexSkipTheCluster().isEmpty() 
					&& exonClusterBefore.getMapIso2ExonIndexSkipTheCluster().equals(exonCluster.getMapIso2ExonIndexSkipTheCluster())) {
				if (exonMultiSE == null) {
					int start = Math.min(exonClusterBefore.getStartAbs(), exonCluster.getStartAbs());
					int end = Math.max(exonClusterBefore.getEndAbs(), exonCluster.getEndAbs());
					exonMultiSE = new int[]{start, end};
				} else {
					exonMultiSE[0] = Math.min(exonMultiSE[0], exonCluster.getStartAbs());
					exonMultiSE[1] = Math.max(exonMultiSE[1], exonCluster.getEndAbs());
				}
			} else {
				if (exonMultiSE != null) {
					ExonCluster exonClusterNew = new ExonCluster(chrID, exonMultiSE[0], exonMultiSE[1], lsGffGeneIsoInfos, cis5To3);
					exonClusterNew.setExonClusterBefore(exonClusterBeforeReal);
					exonClusterNew.setExonClusterAfter(lsExonClusters.get(i));
					exonClusterNew.initail();
					lsExonClustersNew.add(exonClusterNew);
					exonMultiSE = null;
				}
				if ( i >= 1) {
					exonClusterBeforeReal = lsExonClusters.get(i - 1);
				}
			}
			exonClusterBefore = exonCluster;
		}
		
		//不是multise不需要统计这个
//		if (exonMultiSE != null) {
//			ExonCluster exonClusterNew = new ExonCluster(chrID, exonMultiSE[0], exonMultiSE[1], lsGffGeneIsoInfos, cis5To3);
//			exonClusterNew.setExonClusterBefore(exonClusterBeforeReal);
//			exonClusterNew.initail();
//			lsExonClustersNew.add(exonClusterNew);
//			exonMultiSE = null;
//		}
		
		lsExonClusters.addAll(lsExonClustersNew);
		
		return lsExonClusters;
	}
	/**
	 * 按照分组好的边界exon，将每个转录本进行划分，
	 * 划分好的ExonCluster里面每组的lsExon都是考虑
	 * 了方向然后按照方向顺序装进去的 
	 */
	public static ArrayList<ExonCluster> getExonClusterSingle(Boolean cis5To3, List<GffIso> lsGffGeneIsoInfos) {
		return getExonClusterSingle(cis5To3, lsGffGeneIsoInfos, true);
	}
	
	/**
	 * 按照分组好的边界exon，将每个转录本进行划分，
	 * 划分好的ExonCluster里面每组的lsExon都是考虑
	 * 了方向然后按照方向顺序装进去的 
	 */
	public static ArrayList<ExonCluster> getExonClusterSingle(Boolean cis5To3, List<GffIso> lsGffGeneIsoInfos, boolean sepSingle) {
		String chrID = lsGffGeneIsoInfos.get(0).getRefIDlowcase();
		ArrayList<ExonCluster> lsResult = new ArrayList<ExonCluster>();
		List<int[]> lsExonBound = getCombSep(cis5To3, lsGffGeneIsoInfos, sepSingle);
		ExonCluster exonClusterBefore = null;
		for (int[] exonBound : lsExonBound) {
			ExonCluster exonCluster = new ExonCluster(chrID, exonBound[0], exonBound[1], lsGffGeneIsoInfos, cis5To3);
			
			exonCluster.setExonClusterBefore(exonClusterBefore);
			if (exonClusterBefore != null) {
				exonClusterBefore.setExonClusterAfter(exonCluster);
			}
			
			exonCluster.initail();
			lsResult.add(exonCluster);
			exonClusterBefore = exonCluster;
		}
		return lsResult;
	}
	/**
	 * 给定一系列ListElement，以及一个方向。
	 * 将相同方向的ListElement提取出来，然后合并，然后找出这些element的共同边界
	 * @param cis5to3 null,不考虑方向
	 * @param lsIso
	 * @param sepSingle 遇到这种情况怎么分割：<br>
	 * 	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 *    ---m-m---------------------------------------------n-n----<br>
	 *    true aa 和 bb 分开
	 *    false aa 和 bb合在一起
	 * @return
	 * 返回一个list，按照cis5to3排序，如果cis5to3为true，从小到大排列
	 * 如果cis5to3为false，从大到小排列
	 * 内部的int[] 0: startAbs 1: endAbs
	 */
	public static List<int[]> getCombSep(Boolean cis5to3, List<? extends ListEle<? extends AlignExtend>> lsIso, boolean sepSingle) {
		ArrayList<AlignWithParent> lsAllelement = combListAbs(cis5to3, lsIso);
		ArrayList<int[]> lsSep = null;
		if (sepSingle) {
			lsSep = getLsElementSepSingle(cis5to3, lsAllelement);
		} else {
			lsSep = getLsElementSepComb(cis5to3, lsAllelement);
		}
		return lsSep;
	}
	/**
	 * 
	 * 将一个List中的Iso全部合并起来。
	 * @param cis5to3 是否只合并指定方向的iso， null,不考虑方向
	 * @param lsIso
	 * @return
	 */
	private static ArrayList<AlignWithParent> combListAbs(Boolean cis5to3, List<? extends ListEle<? extends AlignExtend>> lsIso) {
		ArrayList<AlignWithParent> lsAll = new ArrayList<>();
		//将全部的exon放在一个list里面并且排序
		for (int i = 0; i < lsIso.size(); i++) {
			ListEle<? extends AlignExtend> gffIso = lsIso.get(i);
			if (cis5to3 != null && gffIso.isCis5to3() != cis5to3) {
				continue;
			}
			for (AlignExtend exonInfo : gffIso) {
				lsAll.add(new AlignWithParent(exonInfo, i));
			}
		}
		if (cis5to3 == null) {
			Collections.sort(lsAll, new ListDetailAbsCompareNoStrand());
		} else {
			Collections.sort(lsAll, new ListDetailAbsCompareStrand());
		}
		return lsAll;
	}
	/**
	 * 当exoncluster中的exon不一样时，查看具体有几条边是相同的。
	 * 因为一致的exon也仅有2条相同边，所以返回的值为0，1，2
	 * @param exonCluster
	 * @return
	 */
	public static int getSameBoundsNum(ExonCluster exonCluster) {
		if (exonCluster.isSameExon()) {
			return 2;
		}

		List<List<ExonInfo>> lsExon = exonCluster.getLsIsoExon();
		if (lsExon.size() < 2) {
			return 0;
		}
		List<ExonInfo> lsExon1 = lsExon.get(0);
		List<ExonInfo> lsExon2 = lsExon.get(1);
		if (lsExon1.size() == 0 || lsExon2.size() == 0) {
			return 0;
		}
		if (lsExon1.get(0).getStartAbs() == lsExon2.get(0).getStartAbs()
			|| lsExon1.get(0).getEndAbs() == lsExon2.get(0).getEndAbs() ) {
			return 1;
		}
		
		if (lsExon1.get(lsExon1.size() - 1).getStartAbs() == lsExon2.get(lsExon2.size() - 1).getStartAbs()
				|| lsExon1.get(lsExon1.size() - 1).getEndAbs() == lsExon2.get(lsExon2.size() - 1).getEndAbs()) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * 将经过排序的exonlist合并，获得几个连续的exon，用于分段
	 * 返回的int[] 0: startAbs    1: endAbs
	 *  
	 */
	public static ArrayList<int[]> getLsElementSepSingle(Boolean cis5to3, List<? extends Alignment> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);
		for (int i = 1; i < lsAll.size(); i++) {
			int[] exon = new int[]{lsAll.get(i).getStartAbs(), lsAll.get(i).getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
		}
		return lsExonBounder;
	}
	
	/** 将经过排序的exonlist合并，获得几个连续的exon，用于分段<br>
	 * 如果有两个exon连续并且单独出现，类似<br>
	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 * ---m-m---------------------------------------------n-n----<br>
	 * <br>
	 * 那么a-a和b-b放在一起<br>
	 */
	private static ArrayList<int[]> getLsElementSepComb(Boolean cis5to3, List<AlignWithParent> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);		
		
		//一堆flag标签
		
		// 上一个exon的父类，判断是否为同一个父类基因
		int lastExonParent = lsAll.get(0).getParent(); 
		
		//上一个exon是否来自于单一父类，就是说没有跟来自另一个父类的exon混合，以下mm和kk是混合的，aa是单独的
		//* -------m-----------m-------------a--a---------b--b------------n-n----<br>
		 //* ---k---------k--------------------------------------n-n----<br>
		boolean lastParentIsSingle = true; 
		
		for (int i = 1; i < lsAll.size(); i++) {
			AlignWithParent exonInfo = lsAll.get(i);
			AlignWithParent listDetailAbsNext = null;
			if (i < lsAll.size() - 1) {
				listDetailAbsNext = lsAll.get(i+1);
			}
			
			int[] exon = new int[]{exonInfo.getStartAbs(), exonInfo.getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					lastParentIsSingle = false;
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					//如果是这种情况：
					//* ---m-m-------------a--a---------b--b------------n-n----<br>
					//* ---m-m---------------------------------------------n-n----<br>
					if (lastParentIsSingle == true && lastExonParent == exonInfo.getParent() 
							&&
							(i == lsAll.size() - 1 || listDetailAbsNext.getStartAbs() >= exonInfo.getEndAbs())
					) {
						exonOld[1] = exon[1];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = exonInfo.getParent();
					}
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					lastParentIsSingle = false;
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					if (lastParentIsSingle == true && lastExonParent == exonInfo.getParent() 
							&&
							(i == lsAll.size() - 1 || listDetailAbsNext.getStartCis() <= exonInfo.getEndCis())
					) {
						exonOld[0] = exon[0];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = lsAll.get(i).getParent();
					}
				}
			}
		}
		return lsExonBounder;
	}
	
	private static class AlignWithParent implements Alignment {
		Alignment alignment;
		int parent;
		
		public AlignWithParent(Alignment alignment, int parent) {
			this.alignment = alignment;
			this.parent = parent;
		}
		
		public int getStartAbs() {
			return alignment.getStartAbs();
		}
		public int getEndAbs() {
			return alignment.getEndAbs();
		}
		public int getStartCis() {
			return alignment.getStartCis();
		}
		public int getEndCis() {
			return alignment.getEndCis();
		}
		public void setAlignment(Alignment alignment) {
			this.alignment = alignment;
		}
		public Alignment getAlignment() {
			return alignment;
		}
		public void setParent(int parent) {
			this.parent = parent;
		}
		public int getParent() {
			return parent;
		}

		@Override
		public Boolean isCis5to3() {
			return alignment.isCis5to3();
		}

		@Override
		public int getLength() {
			return alignment.getLength();
		}

		@Override
		public String getChrId() {
			return alignment.getChrId();
		}
		
	}
}


/** 有方向的排序 */
class ListDetailAbsCompareStrand implements Comparator<Alignment> {

	@Override
    public int compare(Alignment o1, Alignment o2) {
		Integer o1startCis = o1.getStartCis(); Integer o1endCis = o1.getEndCis();
		Integer o2startCis = o2.getStartCis(); Integer o2endCis = o2.getEndCis();
		
		if (o1.isCis5to3() == null || o1.isCis5to3()) {
			int result = o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return o1endCis.compareTo(o2endCis);
			}
			return result;
		} else {
			int result = - o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return - o1endCis.compareTo(o2endCis);
			}
			return result;
		}
    }
	
}
/** 没有方向的排序 */
class ListDetailAbsCompareNoStrand implements Comparator<Alignment> {
	@Override
    public int compare(Alignment o1, Alignment o2) {
		Integer o1startAbs = o1.getStartAbs(); Integer o1endAbs = o1.getEndAbs();
		Integer o2startAbs = o2.getStartAbs(); Integer o2endAbs = o2.getEndAbs();
		int result = o1startAbs.compareTo(o2startAbs);
		if (result == 0) {
			return o1endAbs.compareTo(o2endAbs);
		}
		return result;

    }
}