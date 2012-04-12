package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class GffDetailPeak extends ListDetailAbs{
	
	
	
	public GffDetailPeak(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailPeak clone() {
		GffDetailPeak gffDetailPeak = new GffDetailPeak(getParentName(), getName(), cis5to3);
		this.clone(gffDetailPeak);
		return gffDetailPeak;
	}
}
