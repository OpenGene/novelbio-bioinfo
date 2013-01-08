package com.novelbio.analysis.seq.genome.gffOperate;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.database.model.modgeneid.GeneID;

public class ListGff extends ListAbsSearch<GffDetailGene, GffCodGene, GffCodGeneDU> {
	private static Logger logger = Logger.getLogger(ListGff.class);
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
	
	/**
	 * 合并重复的GffDetailGene
	 * @return
	 */
	public ListGff combineOverlapGene() {
		ListGff listGffNew = new ListGff();
		GffDetailGene gffDetailGeneLast = null;
		//合并两个重叠的基因
		for (GffDetailGene gffDetailGene : this) {
			if (gffDetailGeneLast != null && gffDetailGene.getRefID().equals(gffDetailGeneLast.getRefID())) {
				double[] regionLast = new double[]{gffDetailGeneLast.getStartAbs(), gffDetailGeneLast.getEndAbs()};
				double[] regionThis = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs() };
				double[]  overlapInfo = ArrayOperate.cmpArray(regionLast, regionThis);
				if ((overlapInfo[2] > 0.5 || overlapInfo[3] > 0.5)) {
					gffDetailGeneLast.addIsoSimple(gffDetailGene);
					continue;
				}
			}
			listGffNew.add(gffDetailGene);
			gffDetailGeneLast = gffDetailGene;
		}
		return listGffNew;
	}
}
