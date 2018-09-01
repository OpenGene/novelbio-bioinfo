package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.bioinfo.base.binarysearch.BsearchSite;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;

/**
 * 待检查，默认走全部覆盖该基因，没有5UTR和3UTR
 * 
 * @author zong0jie
 * 
 */
public class GffCodGeneDU extends BsearchSiteDu<GffGene> {
	private static final Logger logger = Logger.getLogger(GffCodGeneDU.class);
	
	/** 是否需要查询Iso */
	private boolean flagSearchAnno = false;
	/** 是否需要查询Hash */
	private boolean flagSearchHash = false;
	/** 是否需要重新查询 */
	private boolean flagSearch = false;
	int[] tss = null;
	int[] tes = null;
	boolean geneBody = true;
	/** 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到 */
	boolean utr5 = false;
	boolean utr3 = false;
	boolean exon = false;
	boolean intron = false;
	/** 保存最后的注释信息 */
	ArrayList<String[]> lsAnno;
	
	/** 保存最后选择到的gene key: geneName + sep +chrID */
	Set<GffGene> setGffDetailGene;
	
	/** 左侧坐标保存的基因信息，Up和Down之间没有交集
	 * 里面保存的GffDetailGene都是clone的
	 */
	LinkedHashSet<GffGene> setGffDetailGenesLeft = null;
	/** 右侧坐标保存的基因信息，Up和Down之间没有交集
	 * 里面保存的GffDetailGene都是clone的
	 */
	LinkedHashSet<GffGene> setGffDetailGenesRight = null;
	
	public GffCodGeneDU(BsearchSiteDu<GffGene> bsearchSiteDu) {
		super(bsearchSiteDu.getLsAlignMid(), 
				new GffCodGene(bsearchSiteDu.getSiteLeft()), new GffCodGene(bsearchSiteDu.getSiteRight()));		
		opLeftInItem = bsearchSiteDu.getOpLeftInItem();
		opLeftInCod = bsearchSiteDu.getOpLeftInCod();
		opLeftBp = bsearchSiteDu.getOpLeftBp();
		
		opRightInItem = bsearchSiteDu.getOpRightInItem();
		opRightInCod = bsearchSiteDu.getOpRightInCod();
		opRightBp = bsearchSiteDu.getOpRightBp();
	}
	
	@Override
	public GffCodGene getSiteLeft() {
		return (GffCodGene) super.getSiteLeft();
	}
	@Override
	public GffCodGene getSiteRight() {
		return (GffCodGene) super.getSiteRight();
	}
	/** 将过滤标签清空 */
	public void cleanFilter() {
		tss = null;
		tes = null;
		geneBody = false;
		/** 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到 */
		utr5 = false;
		utr3 = false;
		exon = false;
		intron = false;
	}
	/**
	 * 设定tss的范围，默认为null
	 * @param tss
	 */
	public void setTss(int[] tss) {
		if (this.tss != null && tss != null) {
			if (this.tss[0] == tss[0] && this.tss[1] == tss[1]) {
				return;
			}
		}
		this.tss = tss;
		resetFlag();
	}

	/**
	 * 设定tes的范围，默认为null
	 * 
	 * @param tes
	 */
	public void setTes(int[] tes) {
		// 一模一样就返回
		if (this.tes != null && tes != null) {
			if (this.tes[0] == tes[0] && this.tes[1] == tes[1]) {
				return;
			}
		}
		this.tes = tes;
		resetFlag();
	}

	/**
	 * 设定genebody的范围，默认为true，也就是获得genebody的信息
	 * 
	 * @param geneBody
	 */
	public void setGeneBody(boolean geneBody) {
		if (this.geneBody == geneBody) {
			return;
		}
		this.geneBody = geneBody;
		resetFlag();
	}

	/**
	 * 设定是否抓取覆盖5UTR的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * @param utr5
	 */
	public void setUTR5(boolean utr5) {
		if (this.utr5 == utr5) {
			return;
		}
		this.utr5 = utr5;
		resetFlag();
	}

	/**
	 * 设定是否抓取覆盖3UTR的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * @param utr3
	 */
	public void setUTR3(boolean utr3) {
		if (this.utr3 == utr3) {
			return;
		}
		this.utr3 = utr3;
		resetFlag();
	}
	/**
	 * 设定覆盖exon的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * @param exon
	 */
	public void setExon(boolean exon) {
		if (this.exon == exon) {
			return;
		}
		this.exon = exon;
		resetFlag();
	}

	/**
	 * 设定覆盖intron的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * 
	 * @param intron
	 */
	public void setIntron(boolean intron) {
		if (this.intron == intron) {
			return;
		}
		this.intron = intron;
		resetFlag();
	}
	private void resetFlag() {
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}



	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * @return 0：accID <br>
	 *         1：symbol<br>
	 *         2：description<br>
	 *         3：两端是具体信息，中间是covered
	 */
	public ArrayList<String[]> getAnno() {
		if (flagSearchAnno) {
			return lsAnno;
		}
		flagSearchAnno = true;
		lsAnno = new ArrayList<String[]>();

		setStructureGene_And_Remove_IsoNotBeFiltered();
		for (GffGene gffDetailGene : setGffDetailGenesLeft) {
			String[] anno = getAnnoCod(tss, siteLeft.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(tss, siteRight.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		if (lsAlignMid != null) {
			for (GffGene gffDetailGene : lsAlignMid) {
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		for (GffGene gffDetailGene : setGffDetailGenesRight) {
			String[] anno = getAnnoCod(tss, siteLeft.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(tss, siteRight.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		return lsAnno;
	}
	/**
	 * 返回两个坐标中间夹着的的GffDetail，覆盖成相应的GffDetail类
	 * @return
	 */
	public ArrayList<GffGene> getLsGffDetailMid() {
		List<GffGene> lsGeneMid = super.lsAlignMid;
		ArrayList<GffGene> lsGeneResult = new ArrayList<>();
		for (GffGene gffDetailGene : lsGeneMid) {
			lsGeneResult.addAll(gffDetailGene.getlsGffDetailGenes());
		}
		return lsGeneResult;
	}
	public List<GffGene> getCoveredElement() {
		List<GffGene> lsCoveredGene = super.getCoveredElement();
		ArrayList<GffGene> lsGeneResult = new ArrayList<>();
		for (GffGene gffDetailGene : lsCoveredGene) {
			lsGeneResult.addAll(gffDetailGene.getlsGffDetailGenes());
		}
		return lsGeneResult;
	}
	/**
	 * 获得peak覆盖的具体内含子外显子的数量
	 * 务必先设定tss等信息
	 */
	public Map<GeneStructure, Integer> getMapStructure2Num() {
		
		return null;
	}
	
	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * 
	 * @param gffDetailGene
	 * @return 0：accID<br>
	 *         1：symbol<br>
	 *         2：description<br>
	 *         3：文字形式的定位描述
	 */
	private String[] getAnnoCod(int[] tss, int coord, GffGene gffDetailGene, String peakPointInfo) {
		List<String> lsAnno = getLsAnno(gffDetailGene);
		lsAnno.add(peakPointInfo + gffDetailGene.getLongestSplitMrna().toStringCodLocStr(tss, coord));
		return lsAnno.toArray(new String[0]);
	}

	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * 
	 * @param gffDetailGene
	 * @return 0：accID<br>
	 *         1：symbol<br>
	 *         2：description<br>
	 *         3：Covered
	 */
	private String[] getAnnoMid(GffGene gffDetailGene) {
		List<String> lsAnno = getLsAnno(gffDetailGene);
		lsAnno.add("Covered");
		return lsAnno.toArray(new String[0]);
	}
	
	private List<String> getLsAnno(GffGene gffDetailGene) {
		List<String> lsAnno = new ArrayList<>();
		String[] anno = new String[3];
		for (GffGene gffDetailGeneSub : gffDetailGene.getlsGffDetailGenes()) {
			anno[0] = anno[0] + "///" + gffDetailGeneSub.getName();
			if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
				GeneID geneID = new GeneID(gffDetailGeneSub.getName(), gffDetailGene.getTaxID());
				anno[1] = geneID.getSymbol();
				anno[2] = geneID.getDescription();
			}
		}
		anno[0] = anno[0].replaceFirst("///", "");
		anno[1] = anno[1].replaceFirst("///", "");
		anno[2] = anno[2].replaceFirst("///", "");
		lsAnno.add(anno[0]);
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			lsAnno.add(anno[1]);lsAnno.add(anno[2]);
		}
		return lsAnno;
	}
	
	
	/**
	 * 不查询数据库，直接返回gffDetailGene<br>
	 * 如果一个基因的某些iso没有和两个位点区域有交集，则去除这些iso<br>
	 * <br>
	 * 譬如转录本为<br>
	 * chr1 30366 30900 <br>
	 * chr1 30350 31200<br>
	 * 不扩展tes区域<br>
	 * <br>
	 * 而left位点为chr1 31000<br>
	 * 则返回基因时将chr1 30366 30900 这个转录本删掉<br>
	 * 
	 * @return LinkedHashSet
	 * 不需要调用 {@link GffGene#getlsGffDetailGenes()}
	 */
	public Set<GffGene> getCoveredOverlapGffGene() {
		setHashCoveredGenInfo();
		Set<GffGene> setResult = new HashSet<GffGene>();
		for (GffGene gffDetailGene : setGffDetailGene) {
			setResult.addAll(gffDetailGene.getlsGffDetailGenes());
		}
		return setResult;
	}
	
	/**
	 * 不查询数据库，直接返回gffDetailGene<br>
	 * 如果一个基因的某些iso没有和两个位点区域有交集，则去除这些iso<br>
	 * <br>
	 * 譬如转录本为<br>
	 * chr1 30366 30900 <br>
	 * chr1 30350 31200<br>
	 * 不扩展tes区域<br>
	 * <br>
	 * 而left位点为chr1 31000<br>
	 * 则返回基因时将chr1 30366 30900 这个转录本删掉<br>
	 * 
	 * @return LinkedHashSet
	 * 不需要调用 {@link GffGene#getlsGffDetailGenes()}
	 */
	public List<GffGene> getLsCoveredOverlapGffGene() {
		setHashCoveredGenInfo();
		Set<GffGene> setResult = new LinkedHashSet<GffGene>();
		for (GffGene gffDetailGene : setGffDetailGene) {
			for (GffGene gene : gffDetailGene.getlsGffDetailGenes()) {
				if (setResult.contains(gene)) {
					continue;
				}
				setResult.add(gene);
			}
		}
		return new ArrayList<>(setResult);
	}

	/** 将两个位点间覆盖到的基因提取出来，保存至hashGffDetailGene */
	private void setHashCoveredGenInfo() {
		if (flagSearchHash && setGffDetailGene != null) {
			return;
		}
		flagSearchHash = true;
		setGffDetailGene = new LinkedHashSet<GffGene>();
		//TODO: 这里修改tss和tes后，gffDetailgene要修改tss和tes，gffiso也要修改tss和tes
		setStructureGene_And_Remove_IsoNotBeFiltered();
		for (GffGene gffDetailGene : setGffDetailGenesLeft) {
			setGffDetailGene.add(gffDetailGene);
		}
		if (lsAlignMid != null) {
			for (GffGene gffDetailGene : lsAlignMid) {
				if (setGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid出现与第一个点一样的iso，建议复查");
					continue;
				}
				setGffDetailGene.add(gffDetailGene);
			}
		}
		for (GffGene gffDetailGene : setGffDetailGenesRight) {
			if (setGffDetailGene.contains(gffDetailGene))
				continue;
			setGffDetailGene.add(gffDetailGene);
		}
	}
	/**
	 * 将覆盖到指定区域的基因全部提取出来并保存至setGffDetailGenesLeft和setGffDetailGenesRight
	 * 其中不符合的iso都会被过滤掉
	 */
	private void setStructureGene_And_Remove_IsoNotBeFiltered() {
		if (flagSearch) {
			return;
		}
		flagSearch = true;
		int tssUp = 0;
		int tesDown = 0;
		if (tss != null) {
			tssUp = tss[0];
		}
		if (tes != null) {
			tesDown = tes[1];
		}
		set_SetGffDetailGenes_Clone(tssUp, tesDown);
		ArrayList<GffGene> lsRemove = new ArrayList<GffGene>();
		for (GffGene gffDetailGene : setGffDetailGenesLeft) {
			//首先判断该gffdetailgene是否满足条件，如果满足了但是里面已经没有gffiso了，也要删除
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffGene gffDetailGene : lsRemove) {
			setGffDetailGenesLeft.remove(gffDetailGene);
		}
		lsRemove.clear();
		for (GffGene gffDetailGene : setGffDetailGenesRight) {			
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffGene gffDetailGene : lsRemove) {
			setGffDetailGenesRight.remove(gffDetailGene);
		}
	}

	/**
	 * 用clone的方法获得端点所牵涉到的基因
	 * @param tssUp
	 * @param tesDown 向下扩展多少bp
	 */
	private void set_SetGffDetailGenes_Clone(int tssUp, int tesDown) {
		setGffDetailGenesLeft = new LinkedHashSet<GffGene>();
		setGffDetailGenesRight = new LinkedHashSet<GffGene>();
		// //////////////// up /////////////////////////////////
		if (this.siteLeft.isInsideUpExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(siteLeft.getAlignUp().clone());
		}
		// //////////////////// this /////////////////////////////////
		if (this.siteLeft.isInsideLoc()) {
			setGffDetailGenesLeft.add(siteLeft.getAlignThis().clone());
		}
		if (this.siteLeft.isInsideDownExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(siteLeft.getAlignDown().clone());
		}
		// //////////////////////////// cod2
		//说明上一个点与本点不在同一个基因内
		if (this.siteRight.isInsideUpExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(siteRight.getAlignUp())) {
			setGffDetailGenesRight.add(siteRight.getAlignUp().clone());
		}
		if (this.siteRight.isInsideLoc() && !setGffDetailGenesLeft.contains(siteRight.getAlignThis())) {
			setGffDetailGenesRight.add(siteRight.getAlignThis().clone());
		}
		if (this.siteRight.isInsideDownExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(siteRight.getAlignDown())) {
			setGffDetailGenesRight.add(siteRight.getAlignDown().clone());
		}
	}
	/**
	 * <b>内部会删除iso信息，所以输入的gffDetail必须是clone的</b> 使用前先判定cod是否在两个相同的gffDetailGene内
	 * 仅考虑两个点在同一个基因内部的情况时 效率稍低但是很全面，每个isoform都会判断
	 * 
	 * @param gffDetailGene
	 *            
	 * @return
	 */
	private boolean isInRegion2Cod_And_Remove_IsoNotBeFiltered(GffGene gffDetailGene) {
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		List<Set<GeneStructure>> lsIso2Structure = getLsIso2Structure(getSiteLeft().getCoord(), getSiteRight().getCoord(), gffDetailGene);

		boolean flagResult = false;
		for (int i = lsIso2Structure.size() - 1; i >= 0; i--) {
			Set<GeneStructure> setStructures = lsIso2Structure.get(i);
			if (setStructures.isEmpty()) {
				gffDetailGene.removeIso(i);
			}
			if (flagResult == true) {
				continue;
			}
			if (!setStructures.isEmpty()) {
				flagResult = true;
			}
		}
		return flagResult;
	}
	
	/** 给定坐标，判定每个iso都覆盖了哪些区域
	 * 
	 * @param coord1
	 * @param coord2
	 * @param gffGeneIsoInfo
	 * @return
	 */
	public List<Set<GeneStructure>> getLsIso2Structure(int coord1, int coord2, GffGene gffDetailGene) {
		/** 标记，0表示需要去除，1表示保留 */
		List<Set<GeneStructure>> lsIso2GeneStructure = new ArrayList<>();
		for (GffIso iso : gffDetailGene.getLsCodSplit()) {
			Set<GeneStructure> setStructures = getGeneStructure(coord1, coord2, iso);
			lsIso2GeneStructure.add(setStructures);
		}
		return lsIso2GeneStructure;
	}
	
	/** 给定坐标，判定覆盖了哪些区域
	 * 
	 * @param coord1
	 * @param coord2
	 * @param gffGeneIsoInfo
	 * @return
	 */
	public Set<GeneStructure> getGeneStructure(int coord1, int coord2, GffIso gffGeneIsoInfo) {
		// 一个是起点，一个是终点
		int coordStart = 0;
		int coordEnd = 0;

		Set<GeneStructure> setStructures = new HashSet<>();
		// 输入的是同一个GffGeneDetail。不过每一个gffGeneDetail含有一个cod，并且 cod1 绝对值< cod2
		// 绝对值
		// 那么以下需要将cod1在基因中的位置小于cod2，所以当gene反向的时候需要将cod反向
		if (gffGeneIsoInfo.isCis5to3()) {
			coordStart = Math.min(coord1, coord2);
			coordEnd = Math.max(coord1, coord2);
		} else {
			coordStart = Math.max(coord1, coord2);
			coordEnd = Math.min(coord1, coord2);
		}
		if (tss != null) {
			if (gffGeneIsoInfo.getCod2Tss(coordStart) <= tss[1]
					&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= tss[0]) {
				setStructures.add(GeneStructure.TSS);
			}
		}
		if (tes != null) {
			if (gffGeneIsoInfo.getCod2Tes(coordStart) <= tes[1]
					&& gffGeneIsoInfo.getCod2Tes(coordEnd) >= tes[0]) {
				setStructures.add(GeneStructure.TES);
			}
		}
		if (geneBody) {
			// 在基因下游肯定是在基因外了
			if (gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
					&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0) {
				setStructures.add(GeneStructure.ALLLENGTH);
			}
		}
		if (utr5) {
			if (GeneType.isMRNA_CanHaveUTR(gffGeneIsoInfo.getGeneType())
					&& gffGeneIsoInfo.getCod2ATG(coordStart) <= 0
					&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0
					&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordStart) 
					|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordStart)
							&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffIso.COD_LOC_EXON 
				                 || gffGeneIsoInfo.getCodLoc(coordEnd) == GffIso.COD_LOC_EXON))) 
			{
				setStructures.add(GeneStructure.UTR5);
			}
		}
		if (utr3) {
			if (GeneType.isMRNA_CanHaveUTR(gffGeneIsoInfo.getGeneType())
					&& gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
					&& gffGeneIsoInfo.getCod2UAG(coordEnd) >= 0
					&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
					|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
							&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffIso.COD_LOC_EXON 
							|| gffGeneIsoInfo.getCodLoc(coordEnd) == GffIso.COD_LOC_EXON))) 
			{
				setStructures.add(GeneStructure.UTR3);
			}
		}
		//位点在两端肯定是包括了
		if (exon || intron) {
			if (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) {
				setStructures.add(GeneStructure.EXON);
				if (gffGeneIsoInfo.size() > 1) {
					setStructures.add(GeneStructure.INTRON);
				}
			}
		}
		if (exon && !setStructures.contains(GeneStructure.EXON)) {
			if (			(gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) 
					|| gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
				    || (gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
							&&
							gffGeneIsoInfo.getCodLoc(coordStart) == GffIso.COD_LOC_EXON )
			)
			{
				setStructures.add(GeneStructure.EXON);
			}
		}
		if (intron && !setStructures.contains(GeneStructure.INTRON)) {
			if ( gffGeneIsoInfo.getExonNum() > 1 &&
			( (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd)
			    && gffGeneIsoInfo.getNumCodInEle(coordStart) != 0 && gffGeneIsoInfo.getNumCodInEle(coordEnd) != 0)
			|| (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0)
			||
			(gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd) 
					      && gffGeneIsoInfo.getCodLoc(coordStart) == GffIso.COD_LOC_INTRON  )
			|| (gffGeneIsoInfo.getNumCodInEle(coordStart) == 0 && (gffGeneIsoInfo.getNumCodInEle(coordEnd) >= 2 
				          || gffGeneIsoInfo.getCodLoc(coordEnd) == GffIso.COD_LOC_INTRON))
			|| (gffGeneIsoInfo.getNumCodInEle(coordEnd) == 0 
					      && (gffGeneIsoInfo.getNumCodInEle(coordStart) <= gffGeneIsoInfo.getExonNum() - 1 
					          || gffGeneIsoInfo.getCodLoc(coordStart) == GffIso.COD_LOC_INTRON))
		   )
		) {
				setStructures.add(GeneStructure.INTRON);
			}
		}
		return setStructures;
	}
	
}
