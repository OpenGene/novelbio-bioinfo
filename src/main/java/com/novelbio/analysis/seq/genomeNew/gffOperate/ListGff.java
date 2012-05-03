package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;

public class ListGff extends ListAbsSearch<GffDetailGene, GffCodGene, GffCodGeneDU>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1121905415019539320L;

	@Override
	protected GffCodGene creatGffCod(String chrID, int Coordinate) {
		GffCodGene gffCodGene = new GffCodGene(chrID, Coordinate);
		return gffCodGene;
	}

	@Override
	protected GffCodGeneDU creatGffCodDu(GffCodGene gffCod1, GffCodGene gffCod2) {
		GffCodGeneDU gffCodGeneDU = new GffCodGeneDU(gffCod1, gffCod2);
		return gffCodGeneDU;
	}

}
