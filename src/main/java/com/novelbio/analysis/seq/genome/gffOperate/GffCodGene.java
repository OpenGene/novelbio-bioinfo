package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodGene extends ListCodAbs<GffDetailGene> {
	/**
	 * 从原始的ListCodAbs生成本类
	 * @param lsSuper
	 */
	public GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
	}
	/**
	 * 获得cod在exon里面的iso信息，没有则返回null，首先查找最长转录本的信息
	 * 返回第一个找到的iso信息
	 */
	public GffGeneIsoInfo getCodInCDSIso() {
		GffGeneIsoInfo gffGeneIsoInfoTmp = null;
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailThis(), super.getCoord());
			if (  gffGeneIsoInfo != null ) {
				if (gffGeneIsoInfo.ismRNA() && 
					gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS
					) {
					return gffGeneIsoInfo;
				}
				else {
					gffGeneIsoInfoTmp = gffGeneIsoInfo;
				}
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailUp(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA() 
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailDown(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
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
	private GffGeneIsoInfo getCodInCDS(GffDetailGene gffDetailGene, int coord) {
		//先找最长转录本，看snp是否在该转录本的exon中，不在的话，找其他所有转录本,看是否在基因的表达区中
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		//如果最长转录本是在CDS区，直接返回
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
			return gffGeneIsoInfo;
		}
		//如果最长转录本在exon中，但不在CDS区，遍历所有转录本，找到在CDS中间的
		else if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//如果最长转录本不在exon中，遍历所有转录本，如果在CDS中就直接返回，如果不在CDS中，就返回在Exon中的
		else {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				} else if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
					gffGeneIsoInfo = gffGeneIsoInfo2;
				}
			}
		}
		//找到了
		if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			return gffGeneIsoInfo;
		}
		return null;
	}
}
