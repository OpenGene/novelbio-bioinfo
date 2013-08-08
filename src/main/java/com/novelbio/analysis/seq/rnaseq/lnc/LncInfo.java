package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

public class LncInfo {
	int taxID = 0;
	GffHashGene gffHashGene;
	/** 找出上下游该区域内的旁临基因 */
	int upDownExtend = 0;
	/**基因名称*/
	String lncName;
	Align align;
	GffGeneIsoInfo gffGeneIsoInfoLnc;
	/**重叠区域的mRna*/
	String mRna;
	/**前面最长转录本的基因*/
	String upGene;
	int upDistance;
	
	/**后面最长转录本的基因*/
	String downGene;
	int downDistance;;
	
	boolean cis5to3;
	
	public LncInfo(int taxID, GffHashGene gffHashGene, int upDownExtend) {
		this.taxID = taxID;
		this.gffHashGene = gffHashGene;
		this.upDownExtend = upDownExtend;
	}

	/**基因名称*/
	public void setLncName(String lncName) {
		if (lncName != null && !lncName.trim().equals("")) {
			this.lncName = lncName;
		}
	}
	
	public void setLncCoord(Align align) {
		this.align = align;
	}
	
	public boolean isCis5to3() {
		return cis5to3;
	}
	/**基因名称*/
	public String getLncName() {
		return lncName;
	}
	/**重叠区域的mRna*/
	public String getmRna() {
		return mRna;
	}
	
	/**前面最长转录本的基因*/
	public String getUpGene() {
		return upGene;
	}
	
	public void searchLnc() {
		if (lncName != null ) {
			searchByGeneName();
		}
		if (align != null && gffGeneIsoInfoLnc == null) {
			searchByAlign();
		}
	}
	
	private void searchByAlign() {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(align.getRefID(), align.getStartAbs(), align.getEndAbs());
		if (gffCodGeneDU == null) return;
		
		Set<GffDetailGene> setGffDetailGenes = gffCodGeneDU.getCoveredGffGene();
		if (setGffDetailGenes.size() == 0) return;
		
		Set<GffGeneIsoInfo> setLncIso = new HashSet<>();
		for (GffDetailGene gffDetailGene : setGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (!gffGeneIsoInfo.ismRNA() && gffGeneIsoInfo.getGeneType() != GeneType.miRNA) {
					setLncIso.add(gffGeneIsoInfo);
				}
			}
		}
		if (setLncIso.isEmpty()) return;
		
		gffGeneIsoInfoLnc = null;
		if (setLncIso.size() == 1) {
			gffGeneIsoInfoLnc = setLncIso.iterator().next();
		} else {
			double overlap = 0;
			for (GffGeneIsoInfo gffGeneIsoInfo : setLncIso) {
				double[] region1 = new double[]{align.getStartAbs(), align.getEndAbs()};
				double[] region2 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
				if (ArrayOperate.cmpArray(region1, region2)[1] > overlap) {
					gffGeneIsoInfoLnc = gffGeneIsoInfo;
				}
			}
		}
		searchIso(gffGeneIsoInfoLnc);
	}
	
	private void searchByGeneName() {
		GffGeneIsoInfo gffiso = gffHashGene.searchISO(lncName);
		if (gffiso == null) return;
		
		GffDetailGene detailGene = gffiso.getParentGffDetailGene();
		if (gffiso.getGeneType() == GeneType.mRNA || gffiso.getGeneType() == GeneType.miRNA) {
			gffiso = getOppLnc(detailGene);
			if (gffiso == null) return;
		}
		gffGeneIsoInfoLnc = gffiso;
		cis5to3 = gffGeneIsoInfoLnc.isCis5to3();
		searchIso(gffGeneIsoInfoLnc);
	}
	
	private void searchIso(GffGeneIsoInfo gffLncIso) {
		if (gffLncIso == null) return;
		
		if (lncName == null || lncName.trim().equals("")) {
			lncName = gffLncIso.getParentGeneName();
		}
		GffDetailGene gffDetailGene = gffLncIso.getParentGffDetailGene();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA && gffGeneIsoInfo.isCis5to3() != isCis5to3()) {
				mRna = gffGeneIsoInfo.getName();
				break;
			}
		}
		setUpDownGeneInfo();
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
	
	private void setUpDownGeneInfo() {
		setUpGffDetailGene(upDownExtend);
		setDownGffDetailGene(upDownExtend);
	}
	
	/**
	 * 获取 前一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setUpGffDetailGene(int upDownExtend) {
		int num = gffGeneIsoInfoLnc.getParentGffDetailGene().getItemNum();
		if (num != 0) {
			GffDetailGene detailGeneUp;
			try {
				detailGeneUp = gffHashGene.getMapChrID2LsGff().get(gffGeneIsoInfoLnc.getRefID()).get(num - 1);
			} catch (Exception e) {
				return;
			}
			int space = Math.abs(detailGeneUp.getEndAbs() - gffGeneIsoInfoLnc.getStartAbs());
			if (Math.abs(space) <= upDownExtend) {
				upGene = detailGeneUp.getLongestSplitMrna().getName();
				upDistance = space;
			} else {
				return;
			}
		} else {
			return;
		}
	}
	/**
	 * 获取 下一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setDownGffDetailGene(int upDownExtend) {
		int num = gffGeneIsoInfoLnc.getParentGffDetailGene().getItemNum();
		GffDetailGene detailGeneDown = null;
		try {
			detailGeneDown = gffHashGene.getMapChrID2LsGff().get(gffGeneIsoInfoLnc.getRefID()).get(num + 1);
		} catch (Exception e) {
			return;
		}
		int space = Math.abs(gffGeneIsoInfoLnc.getEndAbs() - detailGeneDown.getStartAbs());
		if (Math.abs(space) <= upDownExtend) {
			downGene = detailGeneDown.getLongestSplitMrna().getName();
			downDistance = space;
		} else {
			return;
		}
	}
	
	/**
	 * 方便写入EXCEL
	 */
	@Override
	public String toString() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(lncName);
		if (gffGeneIsoInfoLnc == null) {
			lsResult.add("");
		} else {
			GeneID geneID = gffGeneIsoInfoLnc.getGeneID();
			String geneType = gffGeneIsoInfoLnc.getGeneType().toString();
			if (geneID != null && geneID.getGeneInfo() != null && geneID.getGeneInfo().getTypeOfGene() != null) {
				String geneTypeGeneID = geneID.getGeneInfo().getTypeOfGene();
				if (!geneTypeGeneID.equals("") && !geneTypeGeneID.equalsIgnoreCase("mrna") && !geneTypeGeneID.toLowerCase().contains("protein")) {
					geneType = geneTypeGeneID;
				}
			}
			lsResult.add(geneType);
		}
	
		if (mRna != null && !mRna.equals("")) {
			lsResult.add(mRna);
			GeneID geneID = new GeneID(mRna, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
		} else {
			lsResult.add("");
			lsResult.add("");
			lsResult.add("");
		}
		
		if (upGene != null && !upGene.equals("")) {
			GeneID geneID = new GeneID(upGene, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
			
			lsResult.add(upDistance + "");
		} else {
			lsResult.add("");
			lsResult.add("");
			lsResult.add("");
		}
		
		if (downGene != null && !downGene.equals("")) {
			GeneID geneID = new GeneID(downGene, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
			
			lsResult.add(downDistance + "");
		} else {
			lsResult.add("");
			lsResult.add("");
		}
		
		return ArrayOperate.cmbString(lsResult.toArray(new String[0]), "\t");
	}
	
	public static String[] getTitle() {
		List<String> lsTitle = new ArrayList<String>();
		lsTitle.add("LncName");
		lsTitle.add("LncGeneType");
		lsTitle.add("mRNA_AccID");
		lsTitle.add("mRNA_Symbol");
		lsTitle.add("mRNA_Description");
		lsTitle.add("UpStream_GeneName");
		lsTitle.add("UpStream_GeneDescription");
		lsTitle.add("Distance_To_UpGene");
		lsTitle.add("DownStream_GeneName");
		lsTitle.add("DownStream_GeneDescription");
		lsTitle.add("Distance_To_DownGene");
		return lsTitle.toArray(new String[0]);
	}
	
}
