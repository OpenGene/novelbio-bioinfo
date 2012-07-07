package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.test.mytest;

/**
 * 待检查，默认走全部覆盖该基因，没有5UTR和3UTR
 * 
 * @author zong0jie
 * 
 */
public class GffCodGeneDU extends ListCodAbsDu<GffDetailGene, GffCodGene> {
	private static Logger logger = Logger.getLogger(GffCodGeneDU.class);
	/** 是否需要查询Iso */
	private boolean flagSearchAnno = false;
	/** 是否需要查询Hash */
	private boolean flagSearchHash = false;
	/** 是否需要重新查询 */
	private boolean flagSearch = false;
	int[] tss = null;
	int[] tes = null;
	boolean geneBody = true;
	boolean utr5 = false;
	boolean utr3 = false;
	boolean exon = false;
	boolean intron = false;
	/** 保存最后的注释信息 */
	ArrayList<String[]> lsAnno;
	/**
	 * 保存最后选择到的gene key: geneName + sep +chrID
	 * */
	HashSet<GffDetailGene> hashGffDetailGene;
	/**
	 * 设定tss的范围，默认为null
	 * 
	 * @param tss
	 */
	public void setTss(int[] tss) {
		// 一模一样就返回
		if (this.tss != null && tss != null) {
			if (this.tss[0] == tss[0] && this.tss[1] == tss[1]) {
				return;
			}
		}
		this.tss = tss;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
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
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
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
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * 设定是否抓取覆盖5UTR的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * 
	 * @param utr5
	 */
	public void setUTR5(boolean utr5) {
		if (this.utr5 == utr5) {
			return;
		}
		this.utr5 = utr5;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * 设定是否抓取覆盖3UTR的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * 
	 * @param utr3
	 */
	public void setUTR3(boolean utr3) {
		if (this.utr3 == utr3) {
			return;
		}
		this.utr3 = utr3;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * 设定覆盖exon的gene，默认为false，因为genebody已经是true，只有genebody为false时才会起作用
	 * 
	 * @param exon
	 */
	public void setExon(boolean exon) {
		if (this.exon == exon) {
			return;
		}
		this.exon = exon;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
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
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
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
		if (flagSearchAnno && lsAnno != null) {
			return lsAnno;
		}
		flagSearchAnno = true;
		flagSearchHash = true;
		lsAnno = new ArrayList<String[]>();
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		
		hashGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: 这里修改tss和tes后，gffDetailgene要修改tss和tes，gffiso也要修改tss和tes
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		for (GffDetailGene gffDetailGene : lsGffDetailGenesUp) {
			hashGffDetailGene.add(gffDetailGene);
			if (gffCod1.getCoord() == 80391798) {
				System.out.println("stop");
			}
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid出现与第一个点一样的iso，建议复查");
					continue;
				}
				hashGffDetailGene.add(gffDetailGene);
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		for (GffDetailGene gffDetailGene : lsGffDetailGenesDown) {
			if (hashGffDetailGene.contains(gffDetailGene))
				continue;
			hashGffDetailGene.add(gffDetailGene);
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		return lsAnno;
	}
	
	private void setHashCoveredGenInfo() {
		if (flagSearchHash && hashGffDetailGene != null) {
			return;
		}
		flagSearchHash = true;
		hashGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: 这里修改tss和tes后，gffDetailgene要修改tss和tes，gffiso也要修改tss和tes
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		for (GffDetailGene gffDetailGene : lsGffDetailGenesUp) {
			hashGffDetailGene.add(gffDetailGene);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid出现与第一个点一样的iso，建议复查");
					continue;
				}
				hashGffDetailGene.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsGffDetailGenesDown) {
			if (hashGffDetailGene.contains(gffDetailGene))
				continue;
			hashGffDetailGene.add(gffDetailGene);
		}
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
	private String[] getAnnoCod(int coord, GffDetailGene gffDetailGene, String peakPointInfo) {
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";

		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GeneID copedID = new GeneID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", "");
		anno[1] = anno[1].replaceFirst("///", "");
		anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = peakPointInfo + gffDetailGene.getLongestSplit().getCodLocStr(coord);
		return anno;
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
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";

		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
					gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", "");
		anno[1] = anno[1].replaceFirst("///", "");
		anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = "Covered";
		return anno;
	}

	/**
	 * 将覆盖到指定区域的基因全部提取出来并返回
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
	private void getStructureGene(int[] Tss, int[] Tes, boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		if (flagSearch) {
			return;
		}
		flagSearch = true;
		int tssUp = 0;
		int tesDown = 0;
		if (Tss != null) {
			tssUp = Tss[0];
		}
		if (Tes != null) {
			tesDown = Tes[1];
		}
		setSameGeneDetail(tssUp, tesDown);
		ArrayList<GffDetailGene> lsRemove = new ArrayList<GffDetailGene>();
		for (GffDetailGene gffDetailGenes : lsGffDetailGenesUp) {
			GffDetailGene gffDetailGene = gffDetailGenes.clone();
			if (!isInRegion2Cod(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
//				logger.error("看下这边是不是正确：出现了需要删除的iso");
				lsRemove.add(gffDetailGene);
//				lsGffDetailGenesUp.remove(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			lsGffDetailGenesUp.remove(gffDetailGene);
		}
		lsRemove.clear();
		for (GffDetailGene gffDetailGenes : lsGffDetailGenesDown) {
			GffDetailGene gffDetailGene = gffDetailGenes.clone();
			if (!isInRegion2Cod(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
//				logger.error("看下这边是不是正确：出现了需要删除的iso");
				lsRemove.add(gffDetailGene);
//				lsGffDetailGenesDown.remove(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			lsGffDetailGenesDown.remove(gffDetailGene);
		}
	}
	/** Up和Down之间没有交集 */
	LinkedHashSet<GffDetailGene> lsGffDetailGenesUp = null;
	/** Up和Down之间没有交集 */
	LinkedHashSet<GffDetailGene> lsGffDetailGenesDown = null;
	/**
	 * 获得端点所牵涉到的基因
	 * @return lsGffDetailGenes - gffDetailGene 与之相关的坐标
	 */
	private void setSameGeneDetail(int tssUp, int tesDown) {
		lsGffDetailGenesUp = new LinkedHashSet<GffDetailGene>();
		lsGffDetailGenesDown = new LinkedHashSet<GffDetailGene>();
		// //////////////// up /////////////////////////////////
		if (this.gffCod1.isInsideUpExtend(tssUp, tesDown)) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailUp());
		}
		// //////////////////// this /////////////////////////////////
		if (this.gffCod1.isInsideLoc()) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailThis());
		}
		if (this.gffCod1.isInsideDownExtend(tssUp, tesDown)) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailDown());
		}
		// //////////////////////////// cod2
		//说明上一个点与本点不在同一个基因内
		if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailUp())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailUp());
		}
		if (this.gffCod2.isInsideLoc() && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailThis())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailThis());
		}
		if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailDown())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailDown());
		}
	}
	/**
	 * <b>内部会删除iso信息，所以输入的gffDetail必须是clone的</b> 使用前先判定cod是否在两个相同的gffDetailGene内
	 * 仅考虑两个点在同一个基因内部的情况时 效率稍低但是很全面，每个isoform都会判断
	 * 
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5
	 *            如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion2Cod(GffDetailGene gffDetailGene, int[] Tss,int[] Tes, boolean geneBody, Boolean UTR5, boolean UTR3,
			boolean Exon, boolean Intron) {
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = null;
		flag = getInRegion2Cod(getGffCod1().getCoord(), getGffCod2().getCoord(), gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);

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
	private int[] getInRegion2Cod(int coord1, int coord2, GffDetailGene gffDetailGene, int[] Tss, int[] Tes, 
			boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		// 一个是起点，一个是终点
		int coordStart = 0;
		int coordEnd = 0;
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
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
			if (Tss != null) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= Tss[1]
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= Tss[0]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= Tes[1]
						&& gffGeneIsoInfo.getCod2Tes(coordEnd) >= Tes[0]) {
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
			if (UTR5) {
				if (flag[i] == 0
						&& geneBody == false
						&& GffGeneIsoInfo.hashMRNA.contains(gffGeneIsoInfo.getGeneType())
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
			if (UTR3) {
				if (flag[i] == 0
						&& geneBody == false
						&& GffGeneIsoInfo.hashMRNA.contains(gffGeneIsoInfo.getGeneType())
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
			if (flag[i] == 0 && (Exon || Intron)) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) {
					flag[i] = 1;
				}
			}
			if (Exon) {
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
			if (Intron) {
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
	
	public HashSet<GffDetailGene> getCoveredGffGene() {
		setHashCoveredGenInfo();
		return hashGffDetailGene;
	}
	/**
	 * 由前面的设定，将所有符合要求的gene的全部提取出来
	 * @return
	 */
	public ArrayList<GeneID> getCoveredGene() {
		setHashCoveredGenInfo();
		// 用来去冗余的
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
	
		for (GffDetailGene gffDetailGene : hashGffDetailGene) {
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

	/**
	 * 将这两个端点中，涉及到Tes的基因全部提取出来 不遍历iso
	 * 
	 * @return
	 */
	@Deprecated
	public Set<GffDetailGene> getTESGene(int[] tes) {
		/**
		 * 待返回的基因
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		// 在前面基因的最长转录本范围内
		if (gffCod1 != null) {
			// 上一个基因有关系
			if (isUpTes(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			// 本基因关系
			if (isUpTes(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}
		if (gffCod2 != null) {
			// 上一个基因有关系
			if (isDownTes(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			// 本基因关系
			if (isDownTes(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}

	/**
	 * 待检查 将这两个端点中，涉及到Tes的基因全部提取出来 不遍历iso
	 * 
	 * @return
	 */
	@Deprecated
	private boolean isUpTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTesRegion(tes);
		if (gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
				|| (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoGenEnd(coord))) {
			return true;
		}
		return false;
	}
	@Deprecated
	private boolean isDownTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTesRegion(tes);
		if (!gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
				|| (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoGenEnd(coord))) {
			return true;
		}
		return false;
	}

	/**
	 * 待检查 将这两个端点中，涉及到Tss的基因全部提取出来
	 * 
	 * @return
	 */
	@Deprecated
	public HashSet<GffDetailGene> getTSSGene(int[] tss) {
		/**
		 * 待返回的基因
		 */
		HashSet<GffDetailGene> setGffDetailGenes = new LinkedHashSet<GffDetailGene>();
		// 在前面基因的最长转录本范围内
		if (gffCod1 != null) {
			// 上一个基因有关系
			if (isUpTss(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			// 本基因关系
			if (isUpTss(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}

		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}

		if (gffCod2 != null) {
			// 上一个基因有关系
			if (isDownTss(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			// 本基因关系
			if (isDownTss(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	@Deprecated
	private boolean isUpTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTssRegion(tss);
		if (gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
				|| (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoExtend(coord))) {
			return true;
		}
		return false;
	}
	@Deprecated
	private boolean isDownTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTssRegion(tss);
		if (!gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
				|| (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoExtend(coord))) {
			return true;
		}
		return false;
	}

	/**
	 * 返回所有覆盖到的基因的copedID
	 * 
	 * @return
	 */
	@Deprecated
	public ArrayList<GeneID> getAllCoveredGenes() {
		// 用来去冗余的
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailUp().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
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
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft()
					.getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							getGffCodLeft().getGffDetailThis().getTaxID(),
							false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}

		if (getLsGffDetailMid() != null) {
			for (GffDetailGene gffDetailGene : getLsGffDetailMid()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene
						.getLsCodSplit()) {
					// 看是否真正的落在该基因内部
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}

		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight()
					.getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							getGffCodRight().getGffDetailThis().getTaxID(),
							false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
			if (getGffCodRight().isInsideDown()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight()
						.getGffDetailDown().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
						GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
								getGffCodRight().getGffDetailDown().getTaxID(),
								false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
		}
		return lsCopedIDs;
	}
}
