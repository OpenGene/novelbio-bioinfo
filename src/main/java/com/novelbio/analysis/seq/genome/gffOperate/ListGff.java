package com.novelbio.analysis.seq.genome.gffOperate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listOperate.ListAbsSearch;

public class ListGff extends ListAbsSearch<GffDetailGene, GffCodGene, GffCodGeneDU> {
	private static Logger logger = LoggerFactory.getLogger(ListGff.class);
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
		listGffNew.setCis5to3(this.isCis5to3());
		listGffNew.setName(this.getName());
		GffDetailGene gffDetailGeneLast = null;
		//合并两个重叠的基因
		for (GffDetailGene gffDetailGene : this) {
			gffDetailGene.resetStartEnd();
			if (gffDetailGeneLast != null && gffDetailGene.getRefID().equals(gffDetailGeneLast.getRefID())) {
				double[] regionLast = new double[]{gffDetailGeneLast.getStartAbs(), gffDetailGeneLast.getEndAbs()};
				double[] regionThis = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs() };
				double[]  overlapInfo = ArrayOperate.cmpArray(regionLast, regionThis);
				if ((overlapInfo[2] > 0.5 || overlapInfo[3] > GffHashGeneNCBI.overlapFactor)) {
					gffDetailGeneLast.addIsoSimple(gffDetailGene);
					gffDetailGeneLast.resetStartEnd();
					continue;
				}
			}
			listGffNew.add(gffDetailGene);
			gffDetailGeneLast = gffDetailGene;
		}
		return listGffNew;
	}
	
	public void removeGene(GffDetailGene gene) {
		lsElement.indexOf(gene);
	}
}
