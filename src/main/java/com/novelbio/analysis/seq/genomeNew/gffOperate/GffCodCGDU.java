package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodCGDU extends GffCodAbsDu{
	/**
	 * 不管Cod1和Cod2哪个在前，总之小的给gffCodAbs1，大的给gffCodAbs2
	 * @param chrID
	 * @param Cod1
	 * @param Cod2
	 */
	public GffCodCGDU(String chrID, int Cod1, int Cod2) {
		if (Cod1 < Cod2) {
			gffCodAbs1 = new GffCodCG(chrID, Cod1);
			gffCodAbs2 = new GffCodCG(chrID, Cod2);
		}
		else {
			gffCodAbs1 = new GffCodCG(chrID, Cod2);
			gffCodAbs2 = new GffCodCG(chrID, Cod1);
		}
	}

	@Override
	public GffCodAbs getGffCodLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GffCodAbs getGffCodRight() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<GffDetailAbs> getLsGffDetailMid() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
