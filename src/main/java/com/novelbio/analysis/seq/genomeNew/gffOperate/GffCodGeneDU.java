package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.test.mytest;
/**
 * 待检查
 * @author zong0jie
 *
 */
public class GffCodGeneDU extends ListCodAbsDu<GffDetailGene, GffCodGene>{
	private static Logger logger = Logger.getLogger(GffCodGeneDU.class); 
	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	public GffCodGeneDU(GffCodGene gffCod1, GffCodGene gffCod2) {
		super(gffCod1, gffCod2);
	}
	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * @param Tss tss区域 tss前为负数，后为正数
	 * @param Tes tes区域 tes前为负数，后为正数
	 * @param geneBody true or false
	 * @param UTR5 true or false 会判断是否是mRNA
	 * @param UTR3 true or false 会判断是否是mRNA
	 * @param Exon true or false
	 * @param Intron true or false
	 * @return
	 * 0：accID <br>
	 * 1：symbol<br>
	 * 2：description<br>
	 * 3：两端是具体信息，中间是covered
	 */
	public ArrayList<String[]> getAnno(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		HashSet<GffDetailGene> hashGffDetailGeneAnno = new HashSet<GffDetailGene>();
		ArrayList<String[]> lsAnno = new ArrayList<String[]>();
		fsef
		//TODO: 这里修改tss和tes后，gffDetailgene要修改tss和tes，gffiso也要修改tss和tes
		Set<GffDetailGene> gffUpGene = getStructureUpGene(Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		for (GffDetailGene gffDetailGene : gffUpGene) {
			if (hashGffDetailGeneAnno.contains(gffDetailGene))
				continue;
			hashGffDetailGeneAnno.add(gffDetailGene);
			String[] anno = null;
			try {
				anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_left_point:");
			} catch (Exception e) {
				System.out.println("stop");
			}
			lsAnno.add(anno);
		}
		
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGeneAnno.contains(gffDetailGene))
					continue;
				hashGffDetailGeneAnno.add(gffDetailGene);
				
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		
		Set<GffDetailGene> gffDownGene = getStructureDownGene(Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		for (GffDetailGene gffDetailGene : gffDownGene) {
			if (hashGffDetailGeneAnno.contains(gffDetailGene))
				continue;
			hashGffDetailGeneAnno.add(gffDetailGene);
			
			String[] anno = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_right_point:");
			lsAnno.add(anno);
		}
		return lsAnno;
	}
	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * @param gffDetailGene
	 * @return
	 * 0：accID<br>
	 * 1：symbol<br>
	 * 2：description<br>
	 * 3：文字形式的定位描述
	 */
	private String[] getAnnoCod(int coord, GffDetailGene gffDetailGene, String peakPointInfo)
	{
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", ""); anno[1] = anno[1].replaceFirst("///", ""); anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = peakPointInfo+gffDetailGene.getLongestSplit().getCodLocStr(coord);
		return anno;
	}
	/**
	 * 获得gffDetailGene的具体信息，如果该gffDetailGene包含多个copedID，则用“///”分割
	 * @param gffDetailGene
	 * @return
	 * 0：accID<br>
	 * 1：symbol<br>
	 * 2：description<br>
	 * 3：Covered
	 */
	private String[] getAnnoMid(GffDetailGene gffDetailGene)
	{
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", ""); anno[1] = anno[1].replaceFirst("///", ""); anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = "Covered";
		return anno;
	}
	
	/**
	 * 左端端点覆盖
	 * 将覆盖到指定区域的基因全部提取出来并返回
	 * @param Tss Tss上下游多少bp，上游为负数下游为正数， 两个都为正数表示只选取Tss下游，两个都为负数表示只选取Tss上游
	 * @param Tes 同Tss
	 * @param geneBody 是否在genebody
	 * @param Exon 当genebody为false时，是否覆盖exon
	 * @param Intron 当genebody为false时，是否覆盖exon
	 * @return
	 * 没有则返回一个size为0的set
	 */
	private Set<GffDetailGene> getStructureUpGene(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		int tssUp = 0; int tesDown = 0;
		if (Tss != null) {
			tssUp = Tss[0];
		}
		if (Tes != null) {
			tesDown = Tes[1];
		}
		
		LinkedHashSet<GffDetailGene> hashGene = new LinkedHashSet<GffDetailGene>();
		ArrayList<GffDetailGene[]> lsGffDetailGenes = getSameGeneDetail(tssUp,tesDown);
		for (GffDetailGene[] gffDetailGenes : lsGffDetailGenes) {
			if (gffDetailGenes[0] == null && gffDetailGenes[1] == null) {
				continue;
			}
			else if (gffDetailGenes[0] != null && gffDetailGenes[1] == null) {
				if (isInRegion1CodUp(gffDetailGenes[0], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					hashGene.add(gffDetailGenes[0]);
				}
			}
			else if (gffDetailGenes[0] != null && gffDetailGenes[1] != null) {
				if (isInRegion2Cod(gffDetailGenes[0], gffDetailGenes[1], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					//只添加第一个的信息
					hashGene.add(gffDetailGenes[0]);
				}
			}
		}
		return hashGene;
	}
	/**
	 * 右端端点覆盖
	 * 将覆盖到指定区域的基因全部提取出来并返回
	 * @param Tss Tss上下游多少bp，上游为负数下游为正数， 两个都为正数表示只选取Tss下游，两个都为负数表示只选取Tss上游
	 * @param Tes 同Tss
	 * @param geneBody 是否在genebody
	 * @param Exon 当genebody为false时，是否覆盖exon
	 * @param Intron 当genebody为false时，是否覆盖exon
	 * @return
	 * 没有则返回一个size为0的set
	 */
	private Set<GffDetailGene> getStructureDownGene(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		int tssUp = 0; int tesDown = 0;
		if (Tss != null) {
			tssUp = Tss[0];
		}
		if (Tes != null) {
			tesDown = Tes[1];
		}
		
		LinkedHashSet<GffDetailGene> hashGene = new LinkedHashSet<GffDetailGene>();
		ArrayList<GffDetailGene[]> lsGffDetailGenes = getSameGeneDetail(tssUp, tesDown);
		for (GffDetailGene[] gffDetailGenes : lsGffDetailGenes) {
			if (gffDetailGenes[0] == null && gffDetailGenes[1] == null) {
				continue;
			}
			else if (gffDetailGenes[0] == null && gffDetailGenes[1] != null) {
				if (isInRegion1CodDown(gffDetailGenes[1], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					hashGene.add(gffDetailGenes[1]);
				}
			}
		}
		return hashGene;
	}
	
	ArrayList<GffDetailGene[]> lsGffDetailGenes = null;
	 这个方法有问题，连带了调用它的方法也有问题
	 主要就是cod是否在同一个基因内 的方法的调用
	/**
	 * 
	 * 看两个坐标点是否处在同一个geneDetail中
	 * @return
	 * lsGffDetailGenes - gffDetailGene[2] <br>
	 * 0: gffDetailGene 1: null 位点在前面的gff中 <br>
	 * 0: null 1: gffDetailGene 位点在后面的gff中 <br>
	 *  0: gffDetailGene 1: gffDetailGene 位点同时在两个gff中
	 */
	private ArrayList<GffDetailGene[]> getSameGeneDetail(int tssUp, int tesDown)
	{
		if (lsGffDetailGenes != null) {
			return lsGffDetailGenes;
		}
		lsGffDetailGenes = new ArrayList<GffDetailGene[]>();
		
		/**
		 * 0，1，2 第一个坐标点，在up this 和down
		 * 3，4，5 第二个坐标点，在up this 和down
		 * 1：坐标点在内部
		 * 2：两个坐标点都在本基因内部
		 * -1：前面一个点不在基因内
		 */
		int[] flag = new int[6];
		//////////////////  up   /////////////////////////////////
		if (this.gffCod1.isInsideUpExtend(tssUp, tesDown)) {
			GffDetailGene[] gffDetailGenesUp = new GffDetailGene[2];
			gffDetailGenesUp[0] = gffCod1.getGffDetailUp();
			flag[0] = 1;
			if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && this.gffCod1.getGffDetailUp().equals(this.gffCod2.getGffDetailUp())) {
				flag[0] = 2; flag[3] = -1;
				gffDetailGenesUp[1] = gffCod2.getGffDetailUp();
			}
			lsGffDetailGenes.add(gffDetailGenesUp);
		}
		//////////////////////   this    /////////////////////////////////
		if (this.gffCod1.isInsideLoc()) {
			flag[1] = 1;
			GffDetailGene[] gffDetailGenes = new GffDetailGene[2];
			gffDetailGenes[0] = gffCod1.getGffDetailThis();
			//cod2 up
			if (this.gffCod2.isInsideUp() && this.gffCod1.getGffDetailThis().equals(this.gffCod2.getGffDetailUp())) {
				flag[1] = 2; flag[3] = -1;
				gffDetailGenes[1] = gffCod2.getGffDetailUp();
			}
			//cod2 this
			else if ( this.gffCod2.isInsideLoc() && this.gffCod1.getGffDetailThis().equals(this.gffCod2.getGffDetailThis())) {
				flag[1] = 2; flag[4] = -1;
				gffDetailGenes[1] = gffCod2.getGffDetailThis();
			}
			lsGffDetailGenes.add(gffDetailGenes);
		}
	
		if (this.gffCod1.isInsideDownExtend(tssUp, tesDown)) {
			GffDetailGene[] gffDetailGenesDown = new GffDetailGene[2];
			gffDetailGenesDown[0] = gffCod1.getGffDetailDown();
			flag[2] = 1;
			if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailUp())) {
				flag[2] = 2; flag[3] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailUp();
			}
			if ( this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailThis())) {
				flag[2] = 2; flag[4] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailThis();
			}
			if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailDown())) {
				flag[2] = 2; flag[5] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailDown();
			}
			lsGffDetailGenes.add(gffDetailGenesDown);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////  cod2  /////////////////////////////////////////////////////////////////////////////////////////////////
		if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && flag[3] != -1) {
			flag[3] = 1;
			GffDetailGene[] gffDetailGenesUp2 = new GffDetailGene[2];
			gffDetailGenesUp2[1] = gffCod2.getGffDetailUp();
			lsGffDetailGenes.add(gffDetailGenesUp2);
		}
		
		if (flag[4] != -1) {
			flag[4] = 1;
			GffDetailGene[] gffDetailGenes2 = new GffDetailGene[2];
			gffDetailGenes2[1] = gffCod2.getGffDetailThis();
			lsGffDetailGenes.add(gffDetailGenes2);
		}
		if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && flag[5] != -1) {
			flag[5] = 1;
			GffDetailGene[] gffDetailGenes2 = new GffDetailGene[2];
			gffDetailGenes2[1] = gffCod2.getGffDetailDown();
			lsGffDetailGenes.add(gffDetailGenes2);
		}
		return lsGffDetailGenes;
	}
	
	
	/**
	 * 需要检查
	 * 仅考虑两个点不在同一个基因内部的情况时，第一个点的情况
	 * 效率稍低但是很全面，每个isoform都会判断
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion1CodUp(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = null;
		//正向
		flag = isInRegion1CodCisRegion_Cis5to3(gffDetailGene, true,Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
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
	 * 需要检查
	 * 仅考虑两个点不在同一个基因内部的情况时，第二个点的情况
	 * 效率稍低但是很全面，每个isoform都会判断
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion1CodDown(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = null;
		flag = isInRegion1CodCisRegion_Cis5to3(gffDetailGene, false, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
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
	 * 使用前先判定cod是否在两个相同的gffDetailGene内
	 * 仅考虑两个点不在同一个基因内部的情况时，第一个点的情况
	 * 效率稍低但是很全面，每个isoform都会判断
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion2Cod(GffDetailGene gffDetailGene1, GffDetailGene gffDetailGene2, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene1 == null || gffDetailGene2 == null) {
			return false;
		}
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = null;
		flag = getInRegion2Cod(getGffCod1().getCoord(), getGffCod2().getCoord(),gffDetailGene1, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
	
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
			if (flag[i] == 0) {
				gffDetailGene1.removeIso(i);
				gffDetailGene2.removeIso(i);
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
	 * 仅考虑两个点不在同一个基因内部的情况时，顺式第一个点或反式第二个点的情况//或// 顺式后一个位点或反式前一个位点
	 * 当cod覆盖方向和转录本方向一致的时候采用
	 * 效率稍低但是很全面，每个isoform都会判断
	 * @param gffDetailGene
	 * @param startCod 是否为第一个位点
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 如果位点在Tss前，那么即使本基因没有5UTR，也会被选择到
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 * int[iso.size] 标记其中每个转录本是否合格，0不合格1合格
	 * 
	 */
	private int[] isInRegion1CodCisRegion_Cis5to3(int coord, GffDetailGene gffDetailGene, boolean startCod,int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
 {
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		// 正向
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			//顺式前一个位点或反式后一个位点
			if (gffGeneIsoInfo.isCis5to3() && startCod
			||
			!gffGeneIsoInfo.isCis5to3() && !startCod
			) {
				if (Tss != null) {
					if (gffGeneIsoInfo.getCod2Tss(coord) <= Tss[1]) {
						flag[i] = 1;
					}
				}
				if (Tes != null) {
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coord) <= Tes[1]) {
						flag[i] = 1;
					}
				}
				if (geneBody) {
					// 在基因下游肯定是在基因外了
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coord) <= 0) {
						flag[i] = 1;
					}
				}
				if (UTR5) {
					if (flag[i] == 0 && geneBody == false
							&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
							&& gffGeneIsoInfo.getCod2ATG(coord) <= 0) {
						flag[i] = 1;
					}
				}
				if (UTR3) {
					if (flag[i] == 0 && geneBody == false
							&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
							&& gffGeneIsoInfo.getCod2Tes(coord) <= 0) {
						flag[i] = 1;
					}
				}
				if (Exon) {
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coord) <= 0) {
						flag[i] = 1;
					}
				}
				if (Intron) {
					if (flag[i] == 0
							&& gffGeneIsoInfo.getCod2Tes(coord) <= 0
							&& gffGeneIsoInfo.getLocInEleNum(coord) < gffGeneIsoInfo.size()) {
						flag[i] = 1;
					}
				}
			}
			//顺式后一个位点或反式前一个位点
			else if (gffGeneIsoInfo.isCis5to3() && !startCod
					||
					!gffGeneIsoInfo.isCis5to3() && startCod
					) {
				if (Tss != null) {
					if (gffGeneIsoInfo.getCod2Tss(coord) >= Tss[0]) {
						flag[i] = 1;
					}
				}
				if (Tes != null) {
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coord) >= Tes[0]) {
						flag[i] = 1;
					}
				}
				if (geneBody) {
					// 在基因下游肯定是在基因外了
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tss(coord) >= 0) {
						flag[i] = 1;
					}
				}
				if (UTR5) {
					if (flag[i] == 0 && geneBody == false
							&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
							&& gffGeneIsoInfo.getCod2Tss(coord) >= 0) {
						flag[i] = 1;
					}
				}
				if (UTR3) {
					if (flag[i] == 0
							&& geneBody == false
							&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
							&& (gffGeneIsoInfo.getCod2UAG(coord) >= 0)) {
						flag[i] = 1;
					}
				}
				if (Exon) {
					if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tss(coord) >= 0) {
						flag[i] = 1;
					}
				}
				if (Intron) {
					if (flag[i] == 0
							&& gffGeneIsoInfo.getCod2Tss(coord) >= 0
							&& (gffGeneIsoInfo.getLocInEleNum(coord) < 0
									|| gffGeneIsoInfo.getLocInEleNum(coord) > 1
									|| (gffGeneIsoInfo.getLocInEleNum(coord) == 1 && gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_INTRON))) 
					{
						flag[i] = 1;
					}
				}
			}
			//不可能出现的情况
			else {
				logger.error("unknown events");
			}
		}
		return flag;
	}
	
	/**
	 * 两个coord都在同一个gffDetailGene中时进行判定
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
	private int[] getInRegion2Cod(int coord1, int coord2, GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		//一个是起点，一个是终点
		int coordStart = 0; int coordEnd = 0;
		/**
		 * 标记，0表示需要去除，1表示保留
		 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			//输入的是同一个GffGeneDetail。不过每一个gffGeneDetail含有一个cod，并且 cod1 绝对值< cod2 绝对值
			//那么以下需要将cod1在基因中的位置小于cod2，所以当gene反向的时候需要将cod反向
			if (gffDetailGene.getLsCodSplit().get(i).isCis5to3()) {
				coordStart = Math.min(coord1, coord2); coordEnd = Math.max(coord1, coord2);
			}
			else {
				coordStart = Math.max(coord1, coord2); coordEnd = Math.min(coord1, coord2);
			}
			if (Tss != null) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= Tss[1] && gffGeneIsoInfo.getCod2Tss(coordEnd) >= Tss[0]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coordStart) <= Tes[1] && gffGeneIsoInfo.getCod2Tes(coordEnd) >= Tes[0]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// 在基因下游肯定是在基因外了
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0) {
					flag[i] = 1;
				}
			}
			if (UTR5) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& 
						gffGeneIsoInfo.getCod2ATG(coordStart) <=0 && gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0
						&&
						(gffGeneIsoInfo.getLocInEleNum(coordStart) != gffGeneIsoInfo.getLocInEleNum(coordStart)  
								||
						gffGeneIsoInfo.getLocInEleNum(coordStart) == gffGeneIsoInfo.getLocInEleNum(coordStart) && (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON)		
						)
						) {
					flag[i] = 1;
				}
			}
			if (UTR3) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& 
						gffGeneIsoInfo.getCod2Tes(coordStart) <= 0 && gffGeneIsoInfo.getCod2UAG(coordEnd) >= 0
						&&
						(gffGeneIsoInfo.getLocInEleNum(coordStart) != gffGeneIsoInfo.getLocInEleNum(coordEnd)  
								||
						gffGeneIsoInfo.getLocInEleNum(coordStart) == gffGeneIsoInfo.getLocInEleNum(coordEnd) && (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON)		
						)
								) {
					flag[i] = 1;
				}
			}
			if (Exon) {
				if (flag[i] == 0 &&
						(gffGeneIsoInfo.getLocInEleNum(coordStart) != gffGeneIsoInfo.getLocInEleNum(coordEnd)  
								||
						gffGeneIsoInfo.getLocInEleNum(coordStart) == gffGeneIsoInfo.getLocInEleNum(coordEnd) && (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON)		
						)
					) {
					flag[i] = 1;
				}
			}
			if (Intron) {
				if (flag[i] == 0 &&
						(gffGeneIsoInfo.getLocInEleNum(coordStart) != gffGeneIsoInfo.getLocInEleNum(coordEnd) && gffGeneIsoInfo.getLocInEleNum(coordStart) != 0 && gffGeneIsoInfo.getLocInEleNum(coordEnd) != 0)
						||
						(gffGeneIsoInfo.getLocInEleNum(coordStart) == gffGeneIsoInfo.getLocInEleNum(coordEnd) && (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_INTRON) )
						||
						(gffGeneIsoInfo.getLocInEleNum(coordStart) == 0 && (gffGeneIsoInfo.getLocInEleNum(coordEnd) >= 2 || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_INTRON))
						||
						(gffGeneIsoInfo.getLocInEleNum(coordEnd) == 0 && (gffGeneIsoInfo.getLocInEleNum(coordStart) <= gffGeneIsoInfo.getExonNum() - 1 || gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON))
				) 
				{
					flag[i] = 1;
				}
			}
		}
		return flag;
	}
	
	/**
	 * 将这两个端点中，涉及到Tes的基因全部提取出来
	 * 不遍历iso
	 * @return
	 */
	public Set<GffDetailGene> getTESGene(int[] tes) {
		/**
		 * 待返回的基因
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//在前面基因的最长转录本范围内
		if (gffCod1 != null)
		{
			//上一个基因有关系
			if (isUpTes(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//本基因关系
			if (isUpTes(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}		
		if (gffCod2 != null)
		{
			//上一个基因有关系
			if (isDownTes(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//本基因关系
			if (isDownTes(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	/**
	 * 待检查
	 * 将这两个端点中，涉及到Tes的基因全部提取出来
	 * 不遍历iso
	 * @return
	 */
	private boolean isUpTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		gffDetailGene.setTesRegion(tes[0], tes[1]);
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd(coord) )
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		gffDetailGene.setTesRegion(tes[0], tes[1]);
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd(coord))
		)
		{
			return true;
		}
		return false;
	}

	
	
	/**
	 * 待检查
	 * 将这两个端点中，涉及到Tss的基因全部提取出来
	 * @return
	 */
	public Set<GffDetailGene> getTSSGene(int[] tss) {
		/**
		 * 待返回的基因
		 */
		Set<GffDetailGene> setGffDetailGenes = new LinkedHashSet<GffDetailGene>();
		//在前面基因的最长转录本范围内
		if (gffCod1 != null)
		{
			//上一个基因有关系
			if (isUpTss(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//本基因关系
			if (isUpTss(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}
	
		if (gffCod2 != null)
		{
			//上一个基因有关系
			if (isDownTss(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tss))
			{
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//本基因关系
			if (isDownTss(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		gffDetailGene.setTssRegion(tss[0], tss[1]);
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend(coord))
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		gffDetailGene.setTssRegion(tss[0], tss[1]);
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend(coord))
		)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * 返回所有覆盖到的基因的copedID
	 * @return
	 */
	public ArrayList<CopedID> getAllCoveredGenes() {
		//用来去冗余的
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailUp().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), getGffCodLeft().getGffDetailUp().getTaxID(), false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), getGffCodLeft().getGffDetailThis().getTaxID(), false);
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
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					// 看是否真正的落在该基因内部
					CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}
		
		
		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), getGffCodRight().getGffDetailThis().getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
			if (getGffCodRight().isInsideDown())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailDown().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), getGffCodRight().getGffDetailDown().getTaxID(), false);
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
