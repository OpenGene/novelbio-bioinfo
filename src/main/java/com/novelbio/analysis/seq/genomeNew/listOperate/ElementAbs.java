package com.novelbio.analysis.seq.genomeNew.listOperate;

public interface ElementAbs {
	public Boolean isCis5to3();
	/**
	 * 根据方向提取
	 * @return
	 */
	public int getStartCis();
	/**
	 * 获得最小的值
	 * @return
	 */
	public int getStartAbs();
	
	public int getEndCis();
	/**
	 * 获得最大的值
	 * @return
	 */
	public int getEndAbs();
	
	public int getLen();
	
	/**
	 * 返回本element的名字
	 * @return
	 */
	public String getName();
	/**
	 * 返回listabs的name
	 * @return
	 */
	public String getParentName();
	
	public boolean equals(Object obj);
	
}

