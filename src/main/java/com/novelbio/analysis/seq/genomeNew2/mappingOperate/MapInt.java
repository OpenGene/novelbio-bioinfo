package com.novelbio.analysis.seq.genomeNew2.mappingOperate;

import com.novelbio.analysis.seq.genomeNew2.gffOperate.GffCodAbs;

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
		if (flag == map.weight) {
			return 0;
		}
		if (min2max) {
			return flag < map.weight ? -1:1;
		}
		else {
			return flag > map.weight ? -1:1;
		}
	}
	
	

}
