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
	 * ��õ�Ԫ���ӣ�������ԶС�ڷ���
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
	 * ���շ��򷵻�С���Ǹ����������ͷ��ش��
	 * @return
	 */
	public double getStart()
	{
		return cmp.getStartCis();
	}
	/**
	 * ���շ��򷵻ش���Ǹ����������ͷ���С��
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
	 * ���ݷ����趨���
	 * @param start
	 */
	public void setStart(double start)
	{
		cmp.setStartCis(start);
	}
	/**
	 * ���ݷ����趨�յ�
	 * @param start
	 */
	public void setEnd(double end)
	{
		cmp.setEndCis(end);
	}
}