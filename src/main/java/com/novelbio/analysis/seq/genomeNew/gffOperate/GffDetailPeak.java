package com.novelbio.analysis.seq.genomeNew.gffOperate;

public class GffDetailPeak extends GffDetailAbs{

	public GffDetailPeak(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailPeak clone() {
		GffDetailPeak gffDetailPeak = new GffDetailPeak(getChrID(), locString, cis5to3);
		this.clone(gffDetailPeak);
		return gffDetailPeak;
	}

}
