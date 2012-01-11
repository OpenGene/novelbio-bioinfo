package com.novelbio.analysis.seq.genomeNew.listOperate;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.objective;

public interface ElementAbs {
	public boolean isCis5to3();
	/**
	 * 根据方向提取
	 * @return
	 */
	public int getStartCis();
	
	public int getStartAbs();
	
	public int getEndCis();
	
	public int getEndAbs();
	
	public void setStartCis(int startLoc);
	
	public void setEndCis(int endLoc);
	
	public int getLen();
	
	/**
	 * 返回本element的名字
	 * @return
	 */
	public String getLocString();

	public boolean equals(Object obj);
}

