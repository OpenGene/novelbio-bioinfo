package com.novelbio.analysis.seq.genomeNew.gffOperate;

public class GffCodRepeat extends GffCodAbs{

	protected GffCodRepeat(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailRepeat getGffDetailUp() {
		return (GffDetailRepeat)gffDetailUp;
	}

	@Override
	public GffDetailRepeat getGffDetailThis() {
		return (GffDetailRepeat)gffDetailThis;
	}

	@Override
	public GffDetailRepeat getGffDetailDown() {
		return (GffDetailRepeat)gffDetailDown;
	}

}
