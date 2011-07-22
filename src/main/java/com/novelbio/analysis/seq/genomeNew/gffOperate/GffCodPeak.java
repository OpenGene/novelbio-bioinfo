package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodPeak  extends GffCodAbs{

	protected GffCodPeak(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailPeak getGffDetailUp() {
		return (GffDetailPeak)gffDetailUp;
	}

	@Override
	public GffDetailPeak getGffDetailThis() {
		return (GffDetailPeak)gffDetailUp;
	}

	@Override
	public GffDetailPeak getGffDetailDown() {
		return (GffDetailPeak)gffDetailUp;
	}
}
