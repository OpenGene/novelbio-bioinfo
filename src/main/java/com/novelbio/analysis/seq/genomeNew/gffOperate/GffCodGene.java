package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodGene extends ListCodAbs<GffDetailGene>
{
	/**
	 * 从原始的ListCodAbs生成本类
	 * @param lsSuper
	 */
	public GffCodGene(ListCodAbs<GffDetailGene> lsSuper) {
		super(lsSuper);
	}
	
	/**
	 * 获得cod在exon里面的iso信息，没有则返回null，首先查找最长转录本的信息
	 * 返回第一个找到的iso信息
	 */
	public GffGeneIsoInfo getCodInExonIso() {
		GffGeneIsoInfo gffGeneIsoInfoTmp = null;
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailThis(), super.getCoord());
			if (  gffGeneIsoInfo != null ) {
				if (gffGeneIsoInfo.ismRNA()) {
					return gffGeneIsoInfo;
				}
				else {
					gffGeneIsoInfoTmp = gffGeneIsoInfo;
				}
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailUp(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailDown(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()) {
				return gffGeneIsoInfo;
			}
		}
		
		
		if (gffGeneIsoInfoTmp != null) {
			return gffGeneIsoInfoTmp;
		}
		return null;
	}
	
	/**
	 * 返回cod在外显子中的转录本
	 * 注意不一定在exon中，所以外面需要进行判定
	 * @param gffDetailGene
	 * @return
	 */
	private GffGeneIsoInfo getCodInExon(GffDetailGene gffDetailGene, int coord)
	{
		//先找最长转录本，看snp是否在该转录本的exon中，不在的话，找其他所有转录本,看是否在基因的表达区中
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		if (!gffGeneIsoInfo.ismRNA() || gffGeneIsoInfo.getCodLoc(coord) != GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getCod2ATGmRNA(coord) < 0 
				|| gffGeneIsoInfo.getCod2UAG(coord) > 0 ) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON 
						&& gffGeneIsoInfo2.getCod2ATGmRNA(coord) >= 0 
						&& gffGeneIsoInfo2.getCod2UAG(coord) <= 0)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//找到了
		if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			return gffGeneIsoInfo;
		}
		return null;
	}

	public static GffCodGene convert2Class(ListCodAbs<GffDetailGene> lsSuper)
	{
		return new GffCodGene(lsSuper);
	}
	
}
