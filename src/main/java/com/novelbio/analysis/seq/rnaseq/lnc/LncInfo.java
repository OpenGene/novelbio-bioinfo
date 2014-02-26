package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
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
	/**基因具体的转录本名称，方便提取序列 */
	String lncIsoName;
	/**基因名称*/
	String lncName;
	Align align;
	List<GffGeneIsoInfo> lsLncIso;
	/** 本组中最后展示的lnc */
	GffGeneIsoInfo gffLncIso;
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
		if (align != null && lsLncIso == null) {
			searchByAlign();
		}
	}
	
	/** 是否找到了lnc */
	public boolean isFindLnc() {
		return !lsLncIso.isEmpty();
	}
	
	private void searchByGeneName() {
		GffGeneIsoInfo gffiso = gffHashGene.searchISO(lncName);
		if (gffiso == null) return;
		
		GffDetailGene detailGene = gffiso.getParentGffDetailGene();
		if (gffiso.getGeneType() == GeneType.mRNA || gffiso.getGeneType() == GeneType.miRNA) {
			lsLncIso = getLncIso(detailGene);
			if (lsLncIso.isEmpty()) return;
		}
		gffLncIso = getLncIsoOne();
		cis5to3 = gffLncIso.isCis5to3();
		setNameAndUpDown(gffLncIso);
	}
	
	private void searchByAlign() {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(align.getRefID(), align.getStartAbs(), align.getEndAbs());
		if (gffCodGeneDU == null) return;
		
		Set<GffDetailGene> setGffDetailGenes = gffCodGeneDU.getCoveredGffGene();
		if (setGffDetailGenes.size() == 0) return;
		
		lsLncIso = new ArrayList<>();
		for (GffDetailGene gffDetailGene : setGffDetailGenes) {
			lsLncIso.addAll(getLncIso(gffDetailGene));
		}
		gffLncIso = getLncIsoOne();
		setNameAndUpDown(gffLncIso);
	}
	
	/** 选择一个iso */
	private GffGeneIsoInfo getLncIsoOne() {
		if (lsLncIso.isEmpty()) return null;
		
		GffGeneIsoInfo gffGeneIsoInfoLnc = null;
		if (lsLncIso.size() == 1) {
			gffGeneIsoInfoLnc = lsLncIso.iterator().next();
		} else if (align != null) {
			double overlap = 0;
			for (GffGeneIsoInfo gffGeneIsoInfo : lsLncIso) {
				double[] region1 = new double[]{align.getStartAbs(), align.getEndAbs()};
				double[] region2 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
				
				//获得与输入区域覆盖度最大的iso
				double overlapNew = ArrayOperate.cmpArray(region1, region2)[1];
				if (overlapNew > overlap) {
					gffGeneIsoInfoLnc = gffGeneIsoInfo;
					overlap = overlapNew;
				}
			}
		} else {
			//按照名字查询lnc，直接获取第一个就行了
			gffGeneIsoInfoLnc = lsLncIso.iterator().next();
		}
		return gffGeneIsoInfoLnc;
	}

	private void setNameAndUpDown(GffGeneIsoInfo gffLncIso) {
		if (gffLncIso == null) return;
		
		if (lncName == null || lncName.trim().equals("")) {
			lncName = gffLncIso.getParentGeneName();
		}
		lncIsoName = gffLncIso.getName();
		GffDetailGene gffDetailGene = gffLncIso.getParentGffDetailGene();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA && gffGeneIsoInfo.isCis5to3() != isCis5to3()) {
				mRna = gffGeneIsoInfo.getName();
				break;
			}
		}
		setUpDownGeneInfo(gffLncIso);
	}
	
	/**
	 * 获得与mRNA反向的lnc的Set，如果没有mRNA，则返回全体lnc
	 * 如果没有与mRNA反向的lnc，则返回null
	 * @param gffDetailGene
	 * @return
	 */
	protected static List<GffGeneIsoInfo> getLncIso(GffDetailGene gffDetailGene) {
		List<GffGeneIsoInfo> lsLnc = new ArrayList<>();
		List<GffGeneIsoInfo> lsMRNAcis = new ArrayList<>();
		List<GffGeneIsoInfo> lsMRNAtrans = new ArrayList<>();

		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA) {
				if (gffGeneIsoInfo.isCis5to3()) {
					lsMRNAcis.add(gffGeneIsoInfo);
				} else {
					lsMRNAtrans.add(gffGeneIsoInfo);	
				}				
			} else if (gffGeneIsoInfo.getGeneType() != GeneType.miRNA) {
				lsLnc.add(gffGeneIsoInfo);
			}
		}
		
		if (lsMRNAcis.isEmpty() && lsMRNAtrans.isEmpty()) {
			return lsLnc;
		}
		//筛选出同方向的mRNA和lnc必须有很少相同边界，或者方向相反
		Set<Integer> setEdgeMrnaCis = new HashSet<>();
		Set<Integer> setEdgeMrnaTrans = new HashSet<>();
		for (GffGeneIsoInfo gffMrna : lsMRNAcis) {
			for (ExonInfo exonInfo : gffMrna) {
				setEdgeMrnaCis.add(exonInfo.getStartAbs());
				setEdgeMrnaCis.add(exonInfo.getEndAbs());
			}
		}
		for (GffGeneIsoInfo gffMrna : lsMRNAtrans) {
			for (ExonInfo exonInfo : gffMrna) {
				setEdgeMrnaTrans.add(exonInfo.getStartAbs());
				setEdgeMrnaTrans.add(exonInfo.getEndAbs());
			}
		}
		List<GffGeneIsoInfo> lsLncFinal = new ArrayList<>();
		for (GffGeneIsoInfo gffLnc : lsLnc) {
			if(isHaveLessEdge(gffLnc, setEdgeMrnaCis, setEdgeMrnaTrans, 0.3)) {
				lsLncFinal.add(gffLnc);
			}
		}
		
		return lsLncFinal;
	}
	
	/**
	 * protected修饰仅用于测试
	 * @param gffLnc
	 * @param setEdgeCis
	 * @param setEdgeTrans
	 * @param property 与mRNA的相同边界数小于该值，就可认为是一个lnc，否则认为是mRNA
	 * @return
	 */
	private static boolean isHaveLessEdge(GffGeneIsoInfo gffLnc, Set<Integer> setEdgeCis, Set<Integer> setEdgeTrans, double property) {
		Set<Integer> setEdge = setEdgeCis;
		if (!gffLnc.isCis5to3()) {
			setEdge = setEdgeTrans;
		}
		
		if (setEdge.isEmpty()) {
			return true;
		}
		int sameEdge = 0;
		for (ExonInfo exonInfo : gffLnc) {
			if (setEdge.contains(exonInfo.getStartAbs())) {
				sameEdge++;
			}
			if (setEdge.contains(exonInfo.getEndAbs())) {
				sameEdge++;
			}
		}
		double sameEdgeProp = (double)sameEdge/(gffLnc.size()*2);
		if (sameEdgeProp <= property || (sameEdge <= 4 && gffLnc.size() <= 4 && sameEdgeProp <= 0.5)) {
			return true;
		}
		return false;
	}
	
	private void setUpDownGeneInfo(GffGeneIsoInfo gffLncIso) {
		setUpGffDetailGene(upDownExtend, gffLncIso);
		setDownGffDetailGene(upDownExtend, gffLncIso);
	}
	
	/**
	 * 获取 前一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setUpGffDetailGene(int upDownExtend, GffGeneIsoInfo gffLncIso) {
		int num = gffLncIso.getParentGffDetailGene().getItemNum();
		if (num != 0) {
			GffDetailGene detailGeneUp;
			try {
				detailGeneUp = gffHashGene.getMapChrID2LsGff().get(gffLncIso.getRefIDlowcase()).get(num - 1);
			} catch (Exception e) {
				return;
			}
			int space = Math.abs(detailGeneUp.getEndAbs() - gffLncIso.getStartAbs());
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
	private void setDownGffDetailGene(int upDownExtend, GffGeneIsoInfo gffLncIso) {
		int num = gffLncIso.getParentGffDetailGene().getItemNum();
		GffDetailGene detailGeneDown = null;
		try {
			detailGeneDown = gffHashGene.getMapChrID2LsGff().get(gffLncIso.getRefIDlowcase()).get(num + 1);
		} catch (Exception e) {
			return;
		}
		int space = Math.abs(gffLncIso.getEndAbs() - detailGeneDown.getStartAbs());
		if (Math.abs(space) <= upDownExtend) {
			downGene = detailGeneDown.getLongestSplitMrna().getName();
			downDistance = space;
		} else {
			return;
		}
	}
	
	/**
	 * 方便写入EXCEL
	 * 最后返回的是Lnc的Iso name而不是symbol，这样方便后面提取序列
	 */
	@Override
	public String toString() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(lncIsoName);
		lsResult.add(lncName);
		if (lsLncIso.isEmpty()) {
			lsResult.add("");
			lsResult.add("");
		} else {
			GeneID geneID = gffLncIso.getGeneID();
			String geneType = gffLncIso.getGeneType().toString();
			if (geneID != null && geneID.getGeneInfo() != null && geneID.getGeneInfo().getTypeOfGene() != null) {
				String geneTypeGeneID = geneID.getGeneInfo().getTypeOfGene();
				if (!geneTypeGeneID.equals("") && !geneTypeGeneID.equalsIgnoreCase("mrna") && !geneTypeGeneID.toLowerCase().contains("protein")) {
					geneType = geneTypeGeneID;
				}
			}
			lsResult.add(gffLncIso.getRefIDlowcase() + ":" + gffLncIso.getStartAbs() + "-" + gffLncIso.getEndAbs());
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
		lsTitle.add("LncAccID");
		lsTitle.add("LncName");
		lsTitle.add("Location");
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
