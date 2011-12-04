package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
 



/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodGene extends GffCodAbs<GffDetailGene>
{
	protected GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 获得cod在exon里面的iso信息，没有则返回null，首先查找最长转录本的信息
	 * 返回第一个找到的iso信息
	 */
	public GffGeneIsoInfo getCodInExonIso() {
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailThis());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailUp());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailDown());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		return null;
	}
	
	
	private GffGeneIsoInfo getCodInExon(GffDetailGene gffDetailGene)
	{
		//先找最长转录本，看snp是否在该转录本的exon中，不在的话，找其他所有转录本,看是否在基因的表达区中
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getCod2ATGmRNA() < 0 
				|| gffGeneIsoInfo.getCod2UAG() > 0 ) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON 
						&& gffGeneIsoInfo2.getCod2ATGmRNA() >= 0 
						&& gffGeneIsoInfo2.getCod2UAG() <= 0)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//找到了
		if (gffGeneIsoInfo.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON) {
			int startLen = gffGeneIsoInfo.getCod2ATGmRNA();
			int endLen = gffGeneIsoInfo.getCod2UAG();
			// 确定在外显子中
			if (startLen >= 0 && endLen <= 0) {
				return gffGeneIsoInfo;
			}
		}
		return null;
	}
	/**
	 * 返回该gffDetailGene的具体信息
	 * string[3]
	 * @param gffDetailGene
	 */
	private static void copeGffDetailGene(GffDetailGene gffDetailGene) {
		if (gffDetailGene.isCodInGenExtend()) {
			ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfo = gffDetailGene.getLsCodSplit();
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
				if (gffGeneIsoInfo.isCodInIsoExtend()) {
					gffGeneIsoInfo.getCodLocUTR();
				}
			}
		}
	}
	
	
}
