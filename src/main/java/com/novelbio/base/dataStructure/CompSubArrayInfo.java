package com.novelbio.base.dataStructure;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.base.cmd.cmdOperate2;

public class CompSubArrayInfo
{
	public static String FLAGTHIS = "flagthis";
	public static String FLAGCOMP = "flagcomp";
	
	boolean cis = true;
	public CompSubArray cmp;
	public String flag;
	/**
	 * 
	 * @param cmp
	 * @param flag
	 * @param cis true: cell[1] > cell[0] <br>
	 * false: cell[1] < cell[0];
	 */
	public CompSubArrayInfo(CompSubArray cmp, String flag, boolean cis) {
		this.cmp = cmp;
		this.flag = flag;
		this.cis = cis;
	}
	public boolean isCis() {
		return cis;
	}
	/**
	 * 获得单元格子，正向永远小于反向
	 * @return
	 */
	public double[] getCell() {
		return cmp.getCell();
	}
	public String getFlag() {
		return flag;
	}
	public double getLen() {
		return Math.abs(cmp.getCell()[1] - cmp.getCell()[0]) + 1;
	}
	/**
	 * 按照方向返回小的那个，如果反向就返回大的
	 * @return
	 */
	public double getStart()
	{
		return cmp.getStartCis();
	}
	/**
	 * 按照方向返回大的那个，如果反向就返回小的
	 * @return
	 */
	public double getEnd()
	{
		return cmp.getEndCis();
	}

	public CompSubArray getCmp() {
		return cmp;
	}
	/**
	 * 根据方向设定起点
	 * @param start
	 */
	public void setStart(double start)
	{
		cmp.setStartCis(start);
	}
	/**
	 * 根据方向设定终点
	 * @param start
	 */
	public void setEnd(double end)
	{
		cmp.setEndCis(end);
	}
}