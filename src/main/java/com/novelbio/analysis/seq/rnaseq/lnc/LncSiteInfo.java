package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;

public class LncSiteInfo {
	/** 输入的一列Lnc */
	List<String> lsLncs;
	/** 向前后扩展 ,默认2000*/
	int upDownExtend = 2000;
	/** 物种对应的Gene信息 */
	GffChrAbs gffChrAbs = new GffChrAbs();

	/**
	 * 输入的一个Excel表格 colNum 第一列colNum = 0
	 */
	public void setLslsExcel(List<List<String>> lslsExcel, int colNum) {
		lsLncs = new ArrayList<String>();
		for (List<String> list : lslsExcel) {
			lsLncs.add(list.get(colNum));
		}
	}

	/** 输入的一列Lnc */
	public void setLsLncs(List<String> lsLncs) {
		this.lsLncs = lsLncs;
	}

	/** 输入的一列Lnc */
	public List<String> getLsLncs() {
		return lsLncs;
	}

	/** 物种 */
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}

	/** 向前后扩展，意思前后范围内的mRNA都会被抓出来，默认2000bp */
	public void setUpDownExtend(int upDownExtend) {
		this.upDownExtend = upDownExtend;
	}

	/**
	 * 主方法，
	 * 根据一个lnc查找LncInfo
	 * @param lncName
	 * @return
	 */
	private LncInfo findLncSite(String lncName) {
		LncInfo lncInfo = new LncInfo(gffChrAbs.getTaxID());
		lncInfo.setLncName(lncName);
		GffGeneIsoInfo gffiso = gffChrAbs.getGffHashGene().searchISO(lncName);
		if (gffiso == null) return lncInfo;
		
		GffDetailGene detailGene = gffiso.getParentGffDetailGene();

		if (gffiso.getGeneType() == GeneType.mRNA || gffiso.getGeneType() == GeneType.miRNA) {
			gffiso = getOppLnc(detailGene);
			if (gffiso == null) return lncInfo;
		}
		
		lncInfo.setLncIso(gffiso);
		lncInfo.setCis5to3(gffiso.isCis5to3());
		for (GffGeneIsoInfo gffGeneIsoInfo : detailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA && gffGeneIsoInfo.isCis5to3() != lncInfo.isCis5to3()) {
				lncInfo.setmRna(gffGeneIsoInfo.getName());
				break;
			}
		}
		
		lncInfo.setUpDownGeneInfo(upDownExtend, gffChrAbs);
		return lncInfo;
	}
	
	/**
	 * 获得与mRNA反向的lnc，如果没有mRNA，则随便返回一个lnc
	 * 如果没有与mRNA反向的lnc，则返回null
	 * @param gffDetailGene
	 * @return
	 */
	private GffGeneIsoInfo getOppLnc(GffDetailGene gffDetailGene) {
		Boolean mRNAstrand = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA) {
				mRNAstrand = gffGeneIsoInfo.isCis5to3();
				break;
			}
		}
		if (mRNAstrand == null) {
			return gffDetailGene.getLsCodSplit().get(0);
		}
		else {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo.getGeneType() != GeneType.mRNA && gffGeneIsoInfo.getGeneType() != GeneType.miRNA && gffGeneIsoInfo.isCis5to3() != mRNAstrand) {
					return gffGeneIsoInfo;
				}
			}
		}
		return null;
	}

	/**
	 * 根据一列lnc查询lnc信息
	 * @return
	 */
	public List<LncInfo> findLncinfo() {
		List<LncInfo> lsLncInfos = new ArrayList<LncInfo>();
		for (String lncName : lsLncs) {
			LncInfo lncInfo =  findLncSite(lncName);
			lsLncInfos.add(lncInfo);
		}
		return lsLncInfos;
	}

}
