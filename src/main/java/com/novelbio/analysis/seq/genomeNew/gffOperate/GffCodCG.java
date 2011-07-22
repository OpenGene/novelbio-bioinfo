package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodCG extends GffCodAbs{

	protected GffCodCG(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailCG getGffDetailUp() {
		return (GffDetailCG)gffDetailUp;
	}

	@Override
	public GffDetailCG getGffDetailThis() {
		return (GffDetailCG)gffDetailThis;
	}

	@Override
	public GffDetailCG getGffDetailDown() {
		return (GffDetailCG)gffDetailDown;
	}

}
