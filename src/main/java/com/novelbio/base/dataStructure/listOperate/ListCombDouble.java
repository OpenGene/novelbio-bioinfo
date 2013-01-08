package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * 多条listabs合并在一起
 * 其内部的元素即为
 * @author zong0jie
 *
 */
public class ListCombDouble<T extends ElementAbsDouble> extends ListAbsDouble<ElementCombDouble<T>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4068714755622947354L;
	private static Logger logger = Logger.getLogger(ListCombDouble.class);
	/**
	 * 全体待比较的listabs信息
	 */
	ArrayList<ListAbsDouble<T>> lsAllListAbsDouble = new ArrayList<ListAbsDouble<T>>();
	ListAbsDouble<T> lsAllIdouble = new ListAbsDouble<T>();
	/**
	 * 保存所有exon合并后的边界
	 */
	ArrayList<double[]> lsExonBounder = new ArrayList<double[]>();
	public void addListAbs(ListAbsDouble<T> lsListAbs) {
		if (isCis5to3() == null) {
			setCis5to3(lsListAbs.isCis5to3());
		}
		else if (lsListAbs.isCis5to3() != null && isCis5to3() != lsListAbs.isCis5to3()) {
			logger.error("两个方向不同的list不能进行比较");
		}
		lsAllListAbsDouble.add(lsListAbs);
	}
	
	
	boolean copelist = false;
	
	/**
	 * 将输入的多条ListAbs整理成想要的格式，并且按照element进行分段
	 */
	private void copeList()
	{
		if (copelist) {
			return;
		}
		for (ListAbsDouble<T> lsAbs : lsAllListAbsDouble) {
			for (T ele : lsAbs) {
				lsAllIdouble.add(ele);
			}
		}
		lsAllIdouble.sort();
		combExon();
		setExonCluster();
		copelist = true;
	}
	
	/**
	 * 将经过排序的exonlist合并，获得几个连续的exon，用于分段
	 */
	private void combExon()
	{
		lsExonBounder.clear();
		T exonOld =  lsAllIdouble.get(0);
		double[] exonBoundOld = new double[]{exonOld.getStartCis(), exonOld.getEndCis()};
		lsExonBounder.add(exonBoundOld);
		for (int i = 1; i < lsAllIdouble.size(); i++) {
			T ele = lsAllIdouble.get(i);
			double[] exonBound = new double[]{ele.getStartCis(), ele.getEndCis()};
			if (cis5to3 )
			{
				if (exonBound[0] <= exonBoundOld[1]) {
					if (exonBound[1] > exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
			else {
				if (exonBound[0] >= exonBoundOld[1]) {
					if (exonBound[1] < exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
		}
	}
	
	/**
	 * 这里可以优化
	 * 待检查
	 * 按照分组好的边界exon，将每个转录本进行划分
	 */
	private void setExonCluster()
	{
		for (double[] exonBound : lsExonBounder) {
			ElementCombDouble<T> elementComb = new ElementCombDouble<T>(); //ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (int m = 0; m < lsAllListAbsDouble.size(); m ++) {
				ListAbsDouble<T> lsAbs = lsAllListAbsDouble.get(m);
				if (lsAbs.isCis5to3() != isCis5to3()) {
					logger.error("方向不一致，不能比较");
				}
				ArrayList<T> lsExonClusterTmp = new ArrayList<T>();
				//从1开始计数
				int beforeExonNum = 0;//如果本isoform正好没有落在bounder组中的exon，那么就要记录该isoform的前后两个exon的位置，用于查找跨过和没有跨过的exon
				boolean junc = false;//如果本isoform正好没有落在bounder组中的exon，那么就需要记录跳过的exon的位置，就将这个flag设置为true
				for (int i = 0; i < lsAbs.size(); i++) {
					T ele = lsAbs.get(i);
					if (isCis5to3()) {
						if (ele.getEndCis() < exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() >= exonBound[0] && ele.getEndCis() <= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() > exonBound[1]) {
							//如果起点大于本边界，说明过去了，那么看lsExonClusterTmp有东西没，没东西表示跳过，有东西表示没有跳过
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
					else {
						if (ele.getEndCis() > exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() <= exonBound[0] && ele.getEndCis() >= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() < exonBound[1]) {
							//如果起点大于本边界，说明过去了，那么看lsExonClusterTmp有东西没，没东西表示跳过，有东西表示没有跳过
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
				}
				if (lsExonClusterTmp.size() > 0) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum+1, beforeExonNum + lsExonClusterTmp.size() );
				}
				else if (junc && beforeExonNum < lsAbs.size()) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum, -beforeExonNum );
				}
			}
			add(elementComb);
		}
	}
	
	/**
	 * 返回有差异的exon系列
	 * @return
	 */
	public ArrayList<ElementCombDouble<T>> getDifExonCluster() {
		copeList();
		ArrayList<ElementCombDouble<T>> lsDifExon = new ArrayList<ElementCombDouble<T>>();
		for (ElementCombDouble<T> elementComb : this) {
			if (!elementComb.isSameEle()) {
				lsDifExon.add(elementComb);
			}
		}
		return lsDifExon;
	}
}
