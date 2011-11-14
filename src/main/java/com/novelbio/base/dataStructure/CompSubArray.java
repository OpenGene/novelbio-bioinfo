package com.novelbio.base.dataStructure;
/**
 * 提取一个list中一对一对的元素的接口
 * 用于在ArrayOperate中比较两个list之间的差异
 * 实际用于两个转录本之间的差异和两个转录组之间的差异
 * 用于中科院，冯英组项目
 * @author zong0jie
 *
 */
public interface CompSubArray {
	/**
	 * 获得单元格子，正向永远小于反向
	 * @return
	 */
	double[] getCell();
	Boolean isCis5to3();
	/**
	 * 根据cis方向获得起点
	 * @return
	 */
	double getStartCis();
	/**
	 * 根据cis方向获得终点
	 * @return
	 */
	double getEndCis();
}
