package com.novelbio.analysis.seq.genome.gffoperate;

import com.novelbio.listoperate.ListDetailAbs;

public class GffDetailRepeat extends ListDetailAbs
{
	public GffDetailRepeat(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 重复序列的名字
	 */
	protected String repeatName="";

	/**
	 * 重复序列的类型
	 */
	protected String repeatClass="";
	
	/**
	 * 重复序列的家族
	 */
	protected String repeatFamily="";
	/**
	 * 重复序列的名字
	 */
	public String getRepName()
	{
		return repeatName;
	}
	/**
	 * 重复序列的类型
	 */
	public String getRepClass()
	{
		return repeatClass;
	}
	/**
	 * 重复序列的家族
	 */
	public String getRepFamily()
	{
		return repeatFamily;
	}
	@Override
	public GffDetailRepeat clone() {
		GffDetailRepeat gffDetailRepeat = (GffDetailRepeat) super.clone();
		gffDetailRepeat.repeatName = repeatName;
		gffDetailRepeat.repeatClass = repeatClass;
		gffDetailRepeat.repeatFamily = repeatFamily;
		return gffDetailRepeat;
	}
}
