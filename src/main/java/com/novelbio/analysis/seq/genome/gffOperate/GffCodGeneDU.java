package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;
import com.novelbio.listOperate.ListCodAbsDu;

/**
 * 待检查，默认走全部覆盖该基因，没有5UTR和3UTR
 * 
 * @author zong0jie
 * 
 */
public class GffCodGeneDU extends ListCodAbsDu<GffDetailGene, GffCodGene> {
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
	Set<GffDetailGene> setGffDetailGene;
	
	/** 左侧坐标保存的基因信息，Up和Down之间没有交集
	 * 里面保存的GffDetailGene都是clone的
	 */
	LinkedHashSet<GffDetailGene> setGffDetailGenesLeft = null;
	/** 右侧坐标保存的基因信息，Up和Down之间没有交集
	 * 里面保存的GffDetailGene都是clone的
	 */
	LinkedHashSet<GffDetailGene> setGffDetailGenesRight = null;
	
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
	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail, GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}

	public GffCodGeneDU(GffCodGene gffCod1, GffCodGene gffCod2) {
		super(gffCod1, gffCod2);
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
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
			String[] anno = getAnnoCod(tss, gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(tss, gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {
			String[] anno = getAnnoCod(tss, gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(tss, gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		return lsAnno;
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
	private String[] getAnnoCod(int[] tss, int coord, GffDetailGene gffDetailGene, String peakPointInfo) {
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
	private String[] getAnnoMid(GffDetailGene gffDetailGene) {
		List<String> lsAnno = getLsAnno(gffDetailGene);
		lsAnno.add("Covered");
		return lsAnno.toArray(new String[0]);
	}
	
	private List<String> getLsAnno(GffDetailGene gffDetailGene) {
		List<String> lsAnno = new ArrayList<>();
		String[] anno = new String[3];
		for (GffDetailGene gffDetailGeneSub : gffDetailGene.getlsGffDetailGenes()) {
			anno[0] = anno[0] + "///" + gffDetailGeneSub.getNameSingle();
			if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
				GeneID geneID = new GeneID(gffDetailGeneSub.getNameSingle(), gffDetailGene.getTaxID());
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
	 * 不查询数据库，直接返回gffDetailGene
	 * 如果一个基因的某些iso没有和两个位点区域有交集，则去除这些iso
	 * 譬如转录本为
	 * chr1 30366 30900 
	 * chr1 30350 31200
	 * 不扩展tes区域
	 * 
	 * 而left位点为chr1 31000
	 * 则返回基因时将chr1 30366 30900 这个转录本删掉
	 * 
	 * @return LinkedHashSet
	 * 不需要调用 {@link GffDetailGene#getlsGffDetailGenes()}
	 */
	public Set<GffDetailGene> getCoveredOverlapGffGene() {
		setHashCoveredGenInfo();
		Set<GffDetailGene> setResult = new HashSet<GffDetailGene>();
		for (GffDetailGene gffDetailGene : setGffDetailGene) {
			setResult.addAll(gffDetailGene.getlsGffDetailGenes());
		}
		return setResult;
	}
	/**
	 * 不查询数据库，直接返回gffDetailGene
	 * 如果一个基因的某些iso没有和两个位点区域有交集，则去除这些iso
	 * 
	 * 譬如转录本为
	 * chr1 30366 30900 
	 * chr1 30350 31200
	 * 注意不扩展tes区域
	 * 
	 * 而left位点为chr1 31000
	 * 则返回基因时将chr1 30366 30900 这个转录本删掉
	 * 查询数据库，并返回GeneID
	 * @return
	 */
	public ArrayList<GeneID> getCoveredGene() {
		setHashCoveredGenInfo();
		// 用来去冗余的
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
	
		for (GffDetailGene gffDetailGene : setGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
						getGffCodLeft().getGffDetailUp().getTaxID(),
						false);
				if (hashCopedID.contains(copedID)) {
					continue;
				}
				hashCopedID.add(copedID);
				lsCopedIDs.add(copedID);
			}
		}
		return lsCopedIDs;
	}
	/** 将两个位点间覆盖到的基因提取出来，保存至hashGffDetailGene */
	private void setHashCoveredGenInfo() {
		if (flagSearchHash && setGffDetailGene != null) {
			return;
		}
		flagSearchHash = true;
		setGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: 这里修改tss和tes后，gffDetailgene要修改tss和tes，gffiso也要修改tss和tes
		setStructureGene_And_Remove_IsoNotBeFiltered();
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
			setGffDetailGene.add(gffDetailGene);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (setGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid出现与第一个点一样的iso，建议复查");
					continue;
				}
				setGffDetailGene.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {
			if (setGffDetailGene.contains(gffDetailGene))
				continue;
			setGffDetailGene.add(gffDetailGene);
		}
	}
	/**
	 * 将覆盖到指定区域的基因全部提取出来并保存至setGffDetailGenesLeft和setGffDetailGenesRight
	 * 
	 * @param Tss
	 *            Tss上下游多少bp，上游为负数下游为正数， 两个都为正数表示只选取Tss下游，两个都为负数表示只选取Tss上游
	 * @param Tes
	 *            同Tss
	 * @param geneBody
	 *            是否在genebody
	 * @param Exon
	 *            当genebody为false时，是否覆盖exon
	 * @param Intron
	 *            当genebody为false时，是否覆盖exon
	 * @return 没有则返回一个size为0的set
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
		ArrayList<GffDetailGene> lsRemove = new ArrayList<GffDetailGene>();
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
			//首先判断该gffdetailgene是否满足条件，如果满足了但是里面已经没有gffiso了，也要删除
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			setGffDetailGenesLeft.remove(gffDetailGene);
		}
		lsRemove.clear();
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {			
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			setGffDetailGenesRight.remove(gffDetailGene);
		}
	}

	/**
	 * 用clone的方法获得端点所牵涉到的基因
	 * @param tssUp
	 * @param tesDown 向下扩展多少bp
	 */
	private void set_SetGffDetailGenes_Clone(int tssUp, int tesDown) {
		setGffDetailGenesLeft = new LinkedHashSet<GffDetailGene>();
		setGffDetailGenesRight = new LinkedHashSet<GffDetailGene>();
		// //////////////// up /////////////////////////////////
		if (this.gffCod1.isInsideUpExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailUp().clone());
		}
		// //////////////////// this /////////////////////////////////
		if (this.gffCod1.isInsideLoc()) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailThis().clone());
		}
		if (this.gffCod1.isInsideDownExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailDown().clone());
		}
		// //////////////////////////// cod2
		//说明上一个点与本点不在同一个基因内
		if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailUp())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailUp().clone());
		}
		if (this.gffCod2.isInsideLoc() && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailThis())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailThis().clone());
		}
		if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailDown())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailDown().clone());
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
	private boolean isInRegion2Cod_And_Remove_IsoNotBeFiltered(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = null;
		flag = getInRegion2Cod(getGffCod1().getCoord(), getGffCod2().getCoord(), gffDetailGene);

		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0; i--) {
			if (flag[i] == 0) {
				gffDetailGene.removeIso(i);
			}
			if (flagResult == true) {
				continue;
			}
			if (flag[i] == 1) {
				flagResult = true;
			}
		}
		return flagResult;
	}
	/**
	 * 两个coord都在同一个gffDetailGene中时进行判定
	 * 
	 * @param gffDetailGene1
	 * @param gffDetailGene2
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private int[] getInRegion2Cod(int coord1, int coord2, GffDetailGene gffDetailGene) {
		// 一个是起点，一个是终点
		int coordStart = 0;
		int coordEnd = 0;
		/** 标记，0表示需要去除，1表示保留 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			// 输入的是同一个GffGeneDetail。不过每一个gffGeneDetail含有一个cod，并且 cod1 绝对值< cod2
			// 绝对值
			// 那么以下需要将cod1在基因中的位置小于cod2，所以当gene反向的时候需要将cod反向
			if (gffDetailGene.getLsCodSplit().get(i).isCis5to3()) {
				coordStart = Math.min(coord1, coord2);
				coordEnd = Math.max(coord1, coord2);
			} else {
				coordStart = Math.max(coord1, coord2);
				coordEnd = Math.min(coord1, coord2);
			}
			if (tss != null) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= tss[1]
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= tss[0]) {
					flag[i] = 1;
				}
			}
			if (tes != null) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= tes[1]
						&& gffGeneIsoInfo.getCod2Tes(coordEnd) >= tes[0]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// 在基因下游肯定是在基因外了
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0) {
					flag[i] = 1;
				}
			}
			if (utr5) {
				if (flag[i] == 0
						&& geneBody == false
						&& GeneType.isMRNA_CanHaveUTR(gffGeneIsoInfo.getGeneType())
						&& gffGeneIsoInfo.getCod2ATG(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0
						&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordStart) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordStart)
								&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON 
					                 || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON))) 
				{
					flag[i] = 1;
				}
			}
			if (utr3) {
				if (flag[i] == 0
						&& geneBody == false
						&& GeneType.isMRNA_CanHaveUTR(gffGeneIsoInfo.getGeneType())
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2UAG(coordEnd) >= 0
						&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
								&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON 
								|| gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON))) 
				{
					flag[i] = 1;
				}
			}
			//位点在两端肯定是包括了
			if (flag[i] == 0 && (exon || intron)) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) {
					flag[i] = 1;
				}
			}
			if (exon) {
				if (flag[i] == 0
						&& 
				      (
						(gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
					    || (gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
								&&
								gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON )
					 )
				)
				{
					flag[i] = 1;
				}
			}
			if (intron) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getExonNum() > 1 &&
				( (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd)
				    && gffGeneIsoInfo.getNumCodInEle(coordStart) != 0 && gffGeneIsoInfo.getNumCodInEle(coordEnd) != 0)
				|| (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0)
				||
				(gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd) 
						      && gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON  )
				|| (gffGeneIsoInfo.getNumCodInEle(coordStart) == 0 && (gffGeneIsoInfo.getNumCodInEle(coordEnd) >= 2 
					          || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_INTRON))
				|| (gffGeneIsoInfo.getNumCodInEle(coordEnd) == 0 
						      && (gffGeneIsoInfo.getNumCodInEle(coordStart) <= gffGeneIsoInfo.getExonNum() - 1 
						          || gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON))
			   )
			) {
					flag[i] = 1;
				}
			}
		}
		return flag;
	}

}
