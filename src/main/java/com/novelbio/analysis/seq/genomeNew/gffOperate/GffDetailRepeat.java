package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class GffDetailRepeat extends ListDetailAbs
{
	public GffDetailRepeat(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	/**
	 * �ظ����е�����
	 */
	protected String repeatName="";

	/**
	 * �ظ����е�����
	 */
	protected String repeatClass="";
	
	/**
	 * �ظ����еļ���
	 */
	protected String repeatFamily="";
	/**
	 * �ظ����е�����
	 */
	public String getRepName()
	{
		return repeatName;
	}
	/**
	 * �ظ����е�����
	 */
	public String getRepClass()
	{
		return repeatClass;
	}
	/**
	 * �ظ����еļ���
	 */
	public String getRepFamily()
	{
		return repeatFamily;
	}
	@Override
	public GffDetailRepeat clone() {
		GffDetailRepeat gffDetailRepeat = new GffDetailRepeat(getParentName(), getName(), cis5to3);
		this.clone(gffDetailRepeat);
		gffDetailRepeat.repeatName = repeatName;
		gffDetailRepeat.repeatClass = repeatClass;
		gffDetailRepeat.repeatFamily = repeatFamily;
		return gffDetailRepeat;
	}
}
