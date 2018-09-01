package com.novelbio.bioinfo.gff;

import com.novelbio.bioinfo.base.binarysearch.ListCodAbs;

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
