package com.novelbio.bioinfo.rnaseq.lnc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modgeneid.GeneType;

public class LncInfo {
	int taxID = 0;
	GffHashGene gffHashGene;
	/** 找出上下游该区域内的旁临基因 */
	int upDownExtend = 0;
	/**基因具体的转录本名称，方便提取序列 */
	String lncIsoName = "";
	/**基因名称*/
	String lncName = "";
	Align align;
	Map<String, GffIso> mapName2LncIso = new LinkedHashMap<>();
	/** 本组中最后展示的lnc */
	GffIso gffLncIso;
	/**重叠区域的mRna*/
	String mRna = "";
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
		if (align != null && mapName2LncIso.isEmpty()) {
			searchByAlign();
		}
	}
	
	/** 是否找到了lnc */
	public boolean isFindLnc() {
		return !mapName2LncIso.isEmpty();
	}
	
	private void searchByGeneName() {
		GffIso gffiso = gffHashGene.searchISO(lncName);
		if (gffiso == null) return;
		
		GffGene detailGene = gffiso.getParentGffDetailGene();
		mapName2LncIso = getLncIso(detailGene);
		if (mapName2LncIso.isEmpty()) return;
		
		gffLncIso = getLncIsoOne(gffiso);
		if (gffLncIso == null) {
			return;
		}
		cis5to3 = gffLncIso.isCis5to3();
		setNameAndUpDown(gffLncIso);
	}
	
	private void searchByAlign() {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(align.getChrId(), align.getStartAbs(), align.getEndAbs());
		if (gffCodGeneDU == null) return;
		
		Set<GffGene> setGffDetailGenes = gffCodGeneDU.getCoveredOverlapGffGene();
		if (setGffDetailGenes.size() == 0) {
			GffGene gffDetailGeneUp = gffCodGeneDU.getSiteLeft().getAlignUp();
			if (gffDetailGeneUp != null) {
				int spaceUp = Math.abs(gffDetailGeneUp.getEndAbs() - align.getStartAbs());

				if (Math.abs(spaceUp) <= upDownExtend) {
					upGene = gffDetailGeneUp.getLongestSplitMrna().getParentGffDetailGene().getName();
					upDistance = spaceUp;
				}
				
			}
			
			GffGene gffDetailGeneDown = gffCodGeneDU.getSiteRight().getAlignDown();
			if (gffDetailGeneDown != null) {
				int spaceDown = Math.abs(align.getEndAbs() - gffDetailGeneDown.getStartAbs());
				
				if (Math.abs(spaceDown) <= upDownExtend) {
					downGene = gffDetailGeneDown.getLongestSplitMrna().getParentGffDetailGene().getName();
					downDistance = spaceDown;
				}
			}

			return;
		}
		
		mapName2LncIso = new LinkedHashMap<>();
		for (GffGene gffDetailGene : setGffDetailGenes) {
			mapName2LncIso.putAll(getLncIso(gffDetailGene));
		}
		gffLncIso = getLncIsoOne(null);
		setNameAndUpDown(gffLncIso);
	}
	
	/** 选择一个iso */
	private GffIso getLncIsoOne(GffIso isoInput) {
		if (mapName2LncIso.isEmpty()) return null;
		
		GffIso gffGeneIsoInfoLnc = null;
		if (mapName2LncIso.size() == 1) {//只有一个lnc，就选这个了
			gffGeneIsoInfoLnc = mapName2LncIso.values().iterator().next();
		} else if (isoInput != null && mapName2LncIso.containsKey(isoInput.getName())) {//得到的lnc中有我们的选项，就选这个
			gffGeneIsoInfoLnc = mapName2LncIso.get(isoInput.getName());
		} else if (align != null || isoInput != null) {//比较lnc与输入基因的overlap，选择overlap最大的那个
			Align alignThis = align;
			if (alignThis == null && isoInput != null) {
				alignThis = new Align(isoInput.getRefID(), isoInput.getStartAbs(), isoInput.getEndAbs());
			}
			double overlap = 0;
			for (GffIso iso : mapName2LncIso.values()) {
				double[] region1 = new double[]{alignThis.getStartAbs(), alignThis.getEndAbs()};
				double[] region2 = new double[]{iso.getStartAbs(), iso.getEndAbs()};
				
				//获得与输入区域覆盖度最大的iso
				double overlapNew = ArrayOperate.cmpArray(region1, region2)[1];
				if (overlapNew > overlap) {
					gffGeneIsoInfoLnc = iso;
					overlap = overlapNew;
				}
			}
		} else {//啥也没找到，选第一个
			gffGeneIsoInfoLnc = mapName2LncIso.values().iterator().next();
		}
		return gffGeneIsoInfoLnc;
	}

	private void setNameAndUpDown(GffIso gffLncIso) {
		if (gffLncIso == null) return;
		
		if (lncName == null || lncName.trim().equals("")) {
			lncName = gffLncIso.getParentGeneName();
		}
		lncIsoName = gffLncIso.getName();
		GffGene gffDetailGene = gffLncIso.getParentGffDetailGene();
		for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
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
	protected static Map<String, GffIso> getLncIso(GffGene gffDetailGene) {
		Map<String, GffIso> lsLnc = new LinkedHashMap<>();
		List<GffIso> lsMRNAcis = new ArrayList<>();
		List<GffIso> lsMRNAtrans = new ArrayList<>();
		for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.mRNA) {
				if (gffGeneIsoInfo.isCis5to3()) {
					lsMRNAcis.add(gffGeneIsoInfo);
				} else {
					lsMRNAtrans.add(gffGeneIsoInfo);	
				}				
			} else if (gffGeneIsoInfo.getGeneType() != GeneType.miRNA) {
				lsLnc.put(gffGeneIsoInfo.getName(), gffGeneIsoInfo);
			}
		}
		
		if (lsMRNAcis.isEmpty() && lsMRNAtrans.isEmpty()) {
			return lsLnc;
		}
		//筛选出同方向的mRNA和lnc必须有很少相同边界，或者方向相反
		Set<Integer> setEdgeMrnaCis = new HashSet<>();
		Set<Integer> setEdgeMrnaTrans = new HashSet<>();
		for (GffIso gffMrna : lsMRNAcis) {
			for (ExonInfo exonInfo : gffMrna) {
				setEdgeMrnaCis.add(exonInfo.getStartAbs());
				setEdgeMrnaCis.add(exonInfo.getEndAbs());
			}
		}
		for (GffIso gffMrna : lsMRNAtrans) {
			for (ExonInfo exonInfo : gffMrna) {
				setEdgeMrnaTrans.add(exonInfo.getStartAbs());
				setEdgeMrnaTrans.add(exonInfo.getEndAbs());
			}
		}
		Map<String, GffIso> lsLncFinal = new LinkedHashMap<>();
		for (GffIso gffLnc : lsLnc.values()) {
			if(isHaveLessEdge(gffLnc, setEdgeMrnaCis, setEdgeMrnaTrans, 0.3)) {
				lsLncFinal.put(gffLnc.getName(), gffLnc);
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
	private static boolean isHaveLessEdge(GffIso gffLnc, Set<Integer> setEdgeCis, Set<Integer> setEdgeTrans, double property) {
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
	
	private void setUpDownGeneInfo(GffIso gffLncIso) {
		setUpGffDetailGene(upDownExtend, gffLncIso);
		setDownGffDetailGene(upDownExtend, gffLncIso);
	}
	
	/**
	 * 获取 前一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setUpGffDetailGene(int upDownExtend, GffIso gffLncIso) {
		int num = gffLncIso.getParentGffDetailGene().getItemNum();
		if (num != 0) {
			GffGene detailGeneUp;
			try {
				detailGeneUp = gffHashGene.getMapChrID2LsGff().get(gffLncIso.getRefIDlowcase()).get(num - 1);
			} catch (Exception e) {
				return;
			}
			int space = Math.abs(detailGeneUp.getEndAbs() - gffLncIso.getStartAbs());
			if (Math.abs(space) <= upDownExtend) {
				upGene = detailGeneUp.getLongestSplitMrna().getParentGffDetailGene().getName();
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
	private void setDownGffDetailGene(int upDownExtend, GffIso gffLncIso) {
		int num = gffLncIso.getParentGffDetailGene().getItemNum();
		GffGene detailGeneDown = null;
		try {
			detailGeneDown = gffHashGene.getMapChrID2LsGff().get(gffLncIso.getRefIDlowcase()).get(num + 1);
		} catch (Exception e) {
			return;
		}
		int space = Math.abs(gffLncIso.getEndAbs() - detailGeneDown.getStartAbs());
		if (Math.abs(space) <= upDownExtend) {
			downGene = detailGeneDown.getLongestSplitMrna().getParentGffDetailGene().getName();
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
		if (mapName2LncIso.isEmpty() || gffLncIso == null) {
			lsResult.add("");
			lsResult.add("");
		} else {
			GeneID geneID = new GeneID(gffLncIso.getName(), taxID);
			
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
