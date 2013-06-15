package com.novelbio.analysis.seq.genome;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * 给定一个没有ORF的gff
 * 还有一个有ORF的gff
 * 将没有ORF的gff的ATG和UAG注释出来
 * 
 * 务必是同一个物种同一个版本的Gff
 */
public class GffHashModifyORF {
	/** 待修该的Gff */
	GffHashGene gffHashGeneRaw;
	/** 参考的Gff，用Ref来矫正Raw的ATG等位点 */
	GffHashGene gffHashGeneRef;
	
	public void setGffHashGeneRaw(GffHashGene gffHashGeneRaw) {
		this.gffHashGeneRaw = gffHashGeneRaw;
	}
	public void setGffHashGeneRef(GffHashGene gffHashGeneRef) {
		this.gffHashGeneRef = gffHashGeneRef;
	}
	
	public void addATGUAG() {
		for (GffDetailGene gffDetailGeneRef : gffHashGeneRef.getGffDetailAll()) {
			int median = (gffDetailGeneRef.getStartCis() + gffDetailGeneRef.getEndCis())/2;
			GffCodGene gffCodGene = gffHashGeneRaw.searchLocation(gffDetailGeneRef.getRefID(), median);
			if (gffCodGene.isInsideLoc()) {
				continue;
			}
			GffDetailGene gffDetailGeneThis = gffCodGene.getGffDetailThis();
			modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneThis);
		}
	}
	
	private void modifyGffDetailGene(GffDetailGene gffDetailGeneRef, GffDetailGene gffDetailGeneThis) {
		if (!gffDetailGeneRef.isMRNA()) return;
		
		for (GffGeneIsoInfo gffIso : gffDetailGeneThis.getLsCodSplit()) {
			GffGeneIsoInfo gffRef = getSimilarIso(gffIso, gffDetailGeneRef);
			gffIso.setATGUAG(gffRef.getATGsite(), gffIso.getUAGsite());
		}
	}
	
	/** 返回相似的ISO，注意这两个ISO的包含atg的exon必须一致或者至少是overlap的 */
	private GffGeneIsoInfo getSimilarIso(GffGeneIsoInfo gffIso, GffDetailGene gffDetailGeneRef) {
		GffGeneIsoInfo gffRef = gffDetailGeneRef.getSimilarIso(gffIso, 0.5);
		if (gffRef.ismRNA() && isCanbeRef(gffIso, gffRef)) {
			return gffRef;
		} else {
			for (GffGeneIsoInfo gffAnoterRef : gffDetailGeneRef.getLsCodSplit()) {
				if (gffAnoterRef.ismRNA() && isCanbeRef(gffIso, gffAnoterRef)) {
					return gffAnoterRef;
				}
			}
		}
		return null;
	}
	
	/** 只有当atg和uag都落在exon中，才能被当作是可以作为参考的ref iso
	 * 才能将该ref iso的atg和uag加到该iso上。
	 * @return
	 */
	private boolean isCanbeRef(GffGeneIsoInfo gffIso, GffGeneIsoInfo gffRef) {
		if (gffIso.isCis5to3() != gffRef.isCis5to3()) return false;
		
		ListCodAbs<ExonInfo> lsCodExonAtg = gffIso.searchLocation(gffRef.getATGsite());
		ListCodAbs<ExonInfo> lsCodExonUag = gffIso.searchLocation(gffRef.getUAGsite());
		if (lsCodExonAtg.isInsideLoc() && lsCodExonUag.isInsideLoc()) {
			return true;
		}
		return false;
	}
	
}
