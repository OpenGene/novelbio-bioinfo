package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

public interface MapInt {
	/**
	 * 获得起点坐标
	 * @return
	 */
	public int getStart()
	{
		return startLoc;
	}
	/**
	 * 获得终点坐标
	 * @return
	 */
	public int getEnd()
	{
		return endLoc;
	}
	/**
	 * 获得该基因的名称
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * 用于比较的，从小到大比
	 */
	@Override
	public int compareTo(MapInfo map) {
		if (flag == map.score) {
			return 0;
		}
		if (min2max) {
			return flag < map.score ? -1:1;
		}
		else {
			return flag > map.score ? -1:1;
		}
	}
	
	

}
