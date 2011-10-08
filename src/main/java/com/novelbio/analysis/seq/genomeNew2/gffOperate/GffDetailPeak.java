package com.novelbio.analysis.seq.genomeNew2.gffOperate;

public class GffDetailPeak extends GffDetailAbs{

	public GffDetailPeak(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailPeakCod setCood(int coord) {
		return new GffDetailPeakCod(this, coord);
	}

}
