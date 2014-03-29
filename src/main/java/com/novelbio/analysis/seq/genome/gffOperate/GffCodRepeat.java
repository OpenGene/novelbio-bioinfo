package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.listOperate.ListCodAbs;

public class GffCodRepeat extends ListCodAbs<GffDetailRepeat>{

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
