package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.binarysearch.ListEle;

public class ListGff extends ListEle<GffGene> {
	private static Logger logger = LoggerFactory.getLogger(ListGff.class);
	private static final long serialVersionUID = -1121905415019539320L;

	/**
	 * 会将gffDetailGene进行拆分，把独立的gene文件拿出来
	 * 注意本步骤效率低
	 */
	public List<GffGene> getLsElement() {
		List<GffGene> lsGenes = new ArrayList<>();
		lsElement.forEach((gene)->{
			lsGenes.addAll(gene.getlsGffDetailGenes());});
		return lsGenes;
	}
	
	/** 会将gffDetailGene进行拆分，把独立的gene文件拿出来 */
	public List<GffGene> getLsElementRaw() {
		return super.getLsElement();
	}
	/**
	 * 合并重复的GffDetailGene
	 * @return
	 */
	public ListGff combineOverlapGene() {
		ListGff listGffNew = new ListGff();
		listGffNew.setCis5to3(this.isCis5to3());
		listGffNew.setName(this.getName());
		GffGene gffDetailGeneLast = null;
		//合并两个重叠的基因
		for (GffGene gffDetailGene : this) {
			gffDetailGene.resetStartEnd();
			if (gffDetailGeneLast != null && gffDetailGene.getChrId().equals(gffDetailGeneLast.getChrId())) {
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
	
	public void removeGene(GffGene gene) {
		lsElement.indexOf(gene);
	}
}
