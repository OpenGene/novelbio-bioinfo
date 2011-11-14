package com.novelbio.base.dataStructure;

import java.util.ArrayList;

/**
 * 合并两个转录本的东西，专门用于冯英的项目
 * @author zong0jie
 */
public class CompSubArrayCluster
{
	boolean cis;
	static double ratio = 0.6;
	public static void setRatio(double ratio) {
		CompSubArrayCluster.ratio = ratio;
	}
	ArrayList<CompSubArrayInfo> lsCompSubArrayInfosThis = new ArrayList<CompSubArrayInfo>();
	ArrayList<CompSubArrayInfo> lsCompSubArrayInfosComp = new ArrayList<CompSubArrayInfo>();
	
	public ArrayList<CompSubArrayInfo> getLsCompSubArrayInfosComp() {
		return lsCompSubArrayInfosComp;
	}
	public ArrayList<CompSubArrayInfo> getLsCompSubArrayInfosThis() {
		return lsCompSubArrayInfosThis;
	}
	//这簇坐标的最前端，相对于本转录本来说的，也就是说如果为trans的转录本，那么startSite实际为大
	double startSite;
	//这簇坐标的最后端
	double endSite;
	public double getLength() {
		return Math.abs(startSite - endSite) + 1;
	}
	/**
	 * 这簇坐标的最前端，相对于本转录本来说的，也就是说如果为trans的转录本，那么startSite实际为大
	 * @return
	 */
	public double getStartSite() {
		return startSite;
	}
	/**
	 * 这簇坐标的最后端，相对于本转录本来说的，也就是说如果为trans的转录本，那么endSite实际为小
	 * @return
	 */
	public double getEndSite() {
		return endSite;
	}
	/**
	 * 内部没有排序，需要从小到大的加入，否则可能会出错
	 * @param compSubArrayInfo
	 * @param ratio 比例阈值，只有超过该阈值才会将element装入list中
	 * @return
	 * true：成功加入 false：没有加入
	 */
	public boolean addCompElement(CompSubArrayInfo compSubArrayInfo)
	{
		if (lsCompSubArrayInfosComp.size() == 0 && lsCompSubArrayInfosThis.size() == 0) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			cis = compSubArrayInfo.isCis();
			startSite = compSubArrayInfo.getStart();
			endSite = compSubArrayInfo.getEnd();
			return true;
		}

		//留在外面的部分长度
		double[] region1 = compSubArrayInfo.getCell();
		double[] region2 = new double[]{Math.min(startSite, endSite), Math.max(startSite, endSite)};
		double tmpLen = ArrayOperate.cmpArray(region1, region2)[1];
		//overlap > ratio 并且方向一致
		double thisLen = getLength();
		double compLen = compSubArrayInfo.getLen();
		if ((tmpLen/compSubArrayInfo.getLen() > ratio || tmpLen/getLength() > ratio) && compSubArrayInfo.cis == cis) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			
			//设定起点
			//顺式的话，0--1 0--1 
			if (compSubArrayInfo.isCis() && compSubArrayInfo.getStart() < startSite) {
				startSite = compSubArrayInfo.getStart();
			}
			if (!compSubArrayInfo.isCis() && compSubArrayInfo.getStart() > startSite) {
				startSite = compSubArrayInfo.getStart();
			}
			//设定终点
			//顺式的话，0--1 0--1 
			if (compSubArrayInfo.isCis() && compSubArrayInfo.getEnd()> endSite) {
				endSite = compSubArrayInfo.getEnd();
			}
			
			//反式，  <--  1--0   1---0
			else if ( !compSubArrayInfo.isCis() && compSubArrayInfo.getEnd() < endSite) {
				endSite = compSubArrayInfo.getEnd();
			}
			return true;
		}
		return false;
	}
	
	public double getCompScore()
	{
		if (lsCompSubArrayInfosComp.size() == 1 && lsCompSubArrayInfosThis.size() == 1) {
			double[] cell1 = lsCompSubArrayInfosComp.get(0).getCell();
			double[] cell2 = lsCompSubArrayInfosThis.get(0).getCell();
			if (cell1[0] == cell2[0] && cell1[1] == cell2[1]) {
				return 1;
			}
			else {
				return -0.8;
			}
		}
		if (lsCompSubArrayInfosComp.size() == 0 || lsCompSubArrayInfosThis.size() == 0) {
			return -1;
		}
		return -1.2;
	}
	/**
	 * 获得该list的分数
	 * @param ls
	 * @return
	 */
	public static double getCompScore(ArrayList<CompSubArrayCluster> ls)
	{
		double score = 0;
		for (CompSubArrayCluster compSubArrayCluster : ls) {
			score = score + compSubArrayCluster.getCompScore();
		}
		return score;
	}
}