package com.novelbio.bioinfo.gff;

import java.util.HashSet;
import java.util.Set;

import com.novelbio.bioinfo.base.binarysearch.BsearchSite;

/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodGene extends BsearchSite<GffGene> {
	/**
	 * 从原始的ListCodAbs生成本类
	 * @param lsSuper
	 */
	public GffCodGene(BsearchSite<GffGene> bsearchSite) {
		super(bsearchSite.getCoord());
		setInsideLOC(bsearchSite.isInsideLoc());

		setAlignThis(bsearchSite.getAlignThis());
		setIndexAlignThis(bsearchSite.getIndexAlignThis());
		
		setAlignUp(bsearchSite.getAlignUp());
		setIndexAlignUp(bsearchSite.getIndexAlignUp());

		setAlignDown(bsearchSite.getAlignDown());
		setIndexAlignDown(bsearchSite.getIndexAlignDown());

	}
	
	/** 返回距离该位点最近的基因
	 * 如果位点处于gffDetailGene的上游，则距离/3，意思优先选择靠tss的基因
	 * @param range 如果上下游的基因距离 coordinate 在该range之外，则返回null
	 * @return
	 */
	public GffGene getNearestGffGene(int range) {
		if (isInsideLoc()) {
			return getAlignThis();
		} else if (getAlignUp() == null) {
			return getAlignDown();
		} else if (getAlignDown() == null) {
			return getAlignUp();
		} else {
			int upDistance = Math.abs(coord - getAlignUp().getEndAbs());
			if (getAlignUp().isCis5to3() != null && getAlignUp().isCis5to3() == true) {
				upDistance = upDistance*3;
			}
			int downDistance = Math.abs(coord - getAlignDown().getStartAbs());
			if (getAlignDown().isCis5to3() != null && getAlignDown().isCis5to3() == false) {
				downDistance = downDistance*3;
			}
			
			if (upDistance <= downDistance && upDistance <= range) {
				return getAlignUp();
			} else if (downDistance <= upDistance && downDistance <= range) {
				return getAlignDown();
			} else {
				return null;
			}
		}
	}
	
	/** 返回该cod所在的gene，从前到后依次装入 */
	public Set<GffGene> getSetGeneCodIn() {
		Set<GffGene> setGene = new HashSet<>();
		if (isInsideUp()) {
			setGene.add(getAlignUp());
		}
		if (isInsideLoc()) {
			setGene.add(getAlignThis());
		}
		if (isInsideDown()) {
			setGene.add(getAlignDown());
		}
		return setGene;
	}
	
	/**
	 * 获得cod在exon里面的iso信息，没有则返回null，首先查找最长转录本的信息
	 * 返回第一个找到的iso信息
	 */
	public GffIso getCodInCDSIso() {
		GffIso gffGeneIsoInfoTmp = null;
		GffIso gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInCDS(getAlignThis(), super.getCoord());
			if (  gffGeneIsoInfo != null ) {
				if (gffGeneIsoInfo.ismRNA() && 
					gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffIso.COD_LOCUTR_CDS
					) {
					return gffGeneIsoInfo;
				}
				else {
					gffGeneIsoInfoTmp = gffGeneIsoInfo;
				}
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInCDS(getAlignUp(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA() 
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffIso.COD_LOCUTR_CDS) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInCDS(getAlignDown(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffIso.COD_LOCUTR_CDS) {
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
	private GffIso getCodInCDS(GffGene gffDetailGene, int coord) {
		//先找最长转录本，看snp是否在该转录本的exon中，不在的话，找其他所有转录本,看是否在基因的表达区中
		GffIso gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		//如果最长转录本是在CDS区，直接返回
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffIso.COD_LOCUTR_CDS) {
			return gffGeneIsoInfo;
		}
		//如果最长转录本在exon中，但不在CDS区，遍历所有转录本，找到在CDS中间的
		else if (gffGeneIsoInfo.getCodLoc(coord) == GffIso.COD_LOC_EXON) {
			for (GffIso gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffIso.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//如果最长转录本不在exon中，遍历所有转录本，如果在CDS中就直接返回，如果不在CDS中，就返回在Exon中的
		else {
			for (GffIso gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffIso.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				} else if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLoc(coord) == GffIso.COD_LOC_EXON) {
					gffGeneIsoInfo = gffGeneIsoInfo2;
				}
			}
		}
		//找到了
		if (gffGeneIsoInfo.getCodLoc(coord) == GffIso.COD_LOC_EXON) {
			return gffGeneIsoInfo;
		}
		return null;
	}
}
