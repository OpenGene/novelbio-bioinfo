package com.novelbio.analysis.seq.genomeNew2.gffOperate;

public class GffDetailRepeat extends GffDetailAbs
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
	public GffDetailRepeatCod setCood(int coord) {
		return new GffDetailRepeatCod(this, coord);
	}
}
