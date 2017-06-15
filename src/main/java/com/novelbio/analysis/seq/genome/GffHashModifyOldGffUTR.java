package com.novelbio.analysis.seq.genome;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;

/**
 * 给定一个没有ORF的gff
 * 还有一个有ORF的gff
 * 将有ORF的gff的UTR注释出来
 * 
 * 务必是同一个物种同一个版本的Gff
 */
public class GffHashModifyOldGffUTR {
	private static final Logger logger = Logger.getLogger(GffHashModifyOldGffUTR.class);
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
	
	public void modifyGff() {
		Set<GffDetailGene> setGffGeneName = new HashSet<>();//用来去重复的
		for (GffDetailGene gffDetailGeneRef : gffHashGeneRef.getLsGffDetailGenes()) {
			//因为gff文件可能有错，gffgene的长度可能会大于mRNA的总长度，这时候就要遍历每个iso
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGeneRef.getLsCodSplit()) {
				int median = (gffGeneIsoInfo.getStart() + gffGeneIsoInfo.getEnd())/2;
				GffCodGene gffCodGene = gffHashGeneRaw.searchLocation(gffDetailGeneRef.getRefID(), median);
				if (gffCodGene == null || !gffCodGene.isInsideLoc()) {
					logger.warn("cannot find gene on:" + gffDetailGeneRef.getRefID() + " " + median );
					continue;
				}
				GffDetailGene gffDetailGeneThis = gffCodGene.getGffDetailThis();
				if (gffDetailGeneThis == null) {
					logger.error("stop");
				}
				if (setGffGeneName.contains(gffDetailGeneThis)) {
					continue;
				}
				setGffGeneName.add(gffDetailGeneThis);
				modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneThis);
			}
		}
	}
	
	private void modifyGffDetailGene(GffDetailGene gffDetailGeneRef, GffDetailGene gffDetailGeneThis) {
		for (GffGeneIsoInfo gffIsoRef : gffDetailGeneRef.getLsCodSplit()) {
			GffGeneIsoInfo gffIsoThis = getSimilarIso(gffIsoRef, gffDetailGeneThis);
			if (gffIsoThis != null) {
				gffIsoRef.extendUtr(gffIsoThis);
			}
		}
	}
	
	/** 返回相似的ISO，注意这两个ISO的包含atg的exon必须一致或者至少是overlap的 */
	private GffGeneIsoInfo getSimilarIso(GffGeneIsoInfo gffIsoRef, GffDetailGene gffDetailGeneThis) {
		GffGeneIsoInfo gffIsoSimilar = gffDetailGeneThis.getSimilarIso(gffIsoRef, 0.5);
		if (gffIsoSimilar != null && isBeCoveredByIso(gffIsoRef, gffIsoSimilar) && isCanbeRef(gffIsoSimilar, gffIsoRef)) {
			return gffIsoSimilar;
		} else {
			for (GffGeneIsoInfo gffAnoterThis : gffDetailGeneThis.getLsCodSplit()) {
				if (isBeCoveredByIso(gffIsoRef, gffAnoterThis) && isCanbeRef(gffAnoterThis, gffIsoRef)) {
					return gffAnoterThis;
				}
			}
		}
		return null;
	}
	
	/**
	 * refIso 是否被 thisIso 覆盖，因为要用thisIso来注释refIso的UTR区域
	 * 如果refIso和thisIso起点和终点坐标相同，也返回false
	 * @param gffIsoRef
	 * @param gffIsoThis
	 * @return
	 */
	private boolean isBeCoveredByIso(GffGeneIsoInfo gffIsoRef, GffGeneIsoInfo gffIsoThis) {
		if (gffIsoRef.getStartAbs() == gffIsoThis.getStartAbs() && gffIsoRef.getEndAbs() == gffIsoThis.getEndAbs()) {
			return false;
		}
		if (gffIsoRef.getStartAbs() >= gffIsoThis.getStartAbs() && gffIsoRef.getEndAbs() <= gffIsoThis.getEndAbs()) {
			return true;
		}
		return false;
	}
	
	/**
	 *  只有当atg和uag都落在exon中，才能被当作是可以作为参考的ref iso
	 * 才能将该ref iso的atg和uag加到该iso上。
	 * @param gffIso
	 * @param gffOrf 含有orf的gffIso
	 * @return
	 */
	private boolean isCanbeRef(GffGeneIsoInfo gffIso, GffGeneIsoInfo gffOrf) {
		if (gffIso.isCis5to3() != gffOrf.isCis5to3()) return false;
		GffCodGeneDU gffCodGeneDU = gffHashGeneRef.searchLocation(gffIso.getRefID(), gffIso.getStartAbs(), gffIso.getEndAbs());
		if (gffCodGeneDU.getCoveredOverlapGffGene().size() > 1) {
			return false;
		}
		
		if (gffIso.getCodLoc(gffOrf.getATGsite()) == GffGeneIsoInfo.COD_LOC_EXON 
				&& gffIso.getCodLoc(gffOrf.getUAGsite()) ==  GffGeneIsoInfo.COD_LOC_EXON
			) {
			return true;
		}
		return false;
	}
	
}
