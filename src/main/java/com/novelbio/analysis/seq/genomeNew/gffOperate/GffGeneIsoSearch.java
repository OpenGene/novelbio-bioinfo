package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class GffGeneIsoSearch extends GffGeneIsoInfo {

	public GffGeneIsoSearch(GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		super(gffGeneIsoInfo);
		this.coord = coord;
		searchCoord();
	}
	GffDetailGene gffDetailGene;
	private static final Logger logger = Logger.getLogger(GffGeneIsoSearch.class);
	/**
	 * 标记codInExon处在外显子中
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * 标记codInExon处在内含子中
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * 标记codInExon不在转录本中
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * 标记codInExon处在5UTR中
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * 标记codInExon处在3UTR中
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * 标记codInExon不在UTR中
	 */
	public static final int COD_LOCUTR_OUT = 0;	
	
	/**
	 * 坐标
	 */
	protected int coord = -100;
	
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在起点上游为负数，下游为正数
	 */
	protected int cod2TSS = -1000000000;
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 */
	protected int cod2TES = -1000000000;
	
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子
	 * 坐标到该转录本起点的距离，只看mRNA水平，考虑正反向
	 * 只有当坐标处在外显子中才有距离，不包含内含子\
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	protected int cod2TSSmRNA = -1000000000;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子
	 * 坐标到该转录本终点的距离，只看mRNA水平，考虑正反向
	 * 不去除内含子的直接用getCod2UAG
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	protected int cod2TESmRNA = -1000000000;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子<br>
	 * 坐标到该转录本atg的距离，只看mRNA水平，考虑正反向<br>
	 * 坐标在起点上游为负数，下游为正数<br>
	 */
	protected int cod2ATGmRNA= -1000000000;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子<br>
	 * 坐标到该转录本uag的距离，只看mRNA水平，考虑正反向<br>
	 * 坐标在终点上游为负数，下游为正数<br>
	 * Cnn nn  nuaG 距离为8
	 */
	protected int cod2UAGmRNA = -1000000000;
	
	
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子起点的距离
	 * 都为正数
	 */
	protected int cod2ExInStart = -1000000000;
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子终点的距离
	 * 都为正数
	 */
	protected int cod2ExInEnd = -1000000000;
	/**
	 * 包含内含子
	 * 坐标到ATG的距离，考虑正反向.
	 * 在ATG上游为负数，下游为正数
	 * @return
	 */
	protected int cod2ATG = -1000000000;
	/**
	 * 包含内含子
	 * 坐标到UAG的距离，考虑正反向.
	 * 在UAG上游为负数，下游为正数
	 * @return
	 */
	protected int cod2UAG = -1000000000;
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
	 * 实际数目，从1开始记数
	 */
	protected int numExIntron = -1;
	
	/**
	 * 坐标在5UTR、3UTR还是不在
	 */
	protected int codLocUTR = 0;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2start/cod2cdsEnd
	 */
	protected int cod2UTRstartmRNA = -100000000;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2atg/cod2End
	 */
	protected int cod2UTRendmRNA = -100000000;

	/**
	 * 坐标在外显子、内含子还是在该转录本外
	 * 与codLocExon和codLocIntron比较即可
	 */
	protected int codLoc = 0;
	
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLocUTR() {
		return codLocUTR;
	}
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tss() {
		return cod2TSS;
	}
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tes() {
		return cod2TES;
	}
	public int getCoord() {
		return coord;
	}
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
	 * 实际数目，从1开始记数
	 * @return
	 */
	public int getExInNum() {
		return numExIntron;
	}
	/**
	 * 坐标到该外显子/内含子起点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * 坐标到该外显子/内含子终点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * 坐标到ATG的距离，考虑正反向.
	 * 在ATG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * 坐标到UAG的最后一个碱基的距离，考虑正反向.
	 * 在UAG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2UAG() {
		return cod2UAG;
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子 <br>
	 */
	public int getCod2UTRstartmRNA() {
		return cod2UTRstartmRNA;
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子<br>
	 */
	public int getCod2UTRendmRNA() {
		return cod2UTRendmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到该转录本atg的距离
	 * 不去除内含子的直接用cod2atg/cod2End
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2ATGmRNA() {
		return cod2ATGmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到UAG的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 坐标在终点上游为负数，下游为正数<br>
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2UAGmRNA() {
		return cod2UAGmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到TSS的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 只有当坐标处在外显子中才有距离，不包含内含子\
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	public int getCod2TSSmRNA() {
		return cod2TSSmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到TES的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	public int getCod2TESmRNA() {
		return cod2TESmRNA;
	}
	
	/**
	 */
	private void searchCoord()
	{
		codSearchNum();
		if (codLocUTR == COD_LOCUTR_5UTR) {
			setCod2UTR5();
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			setCod2UTR3();
		}
		if (codLoc == COD_LOC_EXON) {
			setCod2StartEndmRNA();
			setCod2StartEndCDS();
		}
	}

	/**
	 * 第一个计算的，计算坐标与本 外显子/内含子 的 起点/终点 的距离
	 */
	protected abstract void setCod2ExInStartEnd();
	/**
	 * 查找坐标在第几个外显子或内含子中
	 * 并且指出在是在外显子还是内含子
	 * 是否在UTR中
	 * 同时填充		
	 * cod2ATG
		cod2cdsEnd 
		cod2start 
		cod2end 
		等
	 */
	protected void codSearchNum()
	{
		int ExIntronnum = getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if((coord < ATGsite && cis5to3) || (coord > ATGsite && !cis5to3)){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && cis5to3) || (coord < UAGsite && !cis5to3)){       //大于cds起始区，在3‘UTR中
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		setCod2SiteAbs();
		cod2ExInStart = getLoc2ExInStart(coord);
		cod2ExInEnd = getLoc2ExInEnd(coord);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		numExIntron = Math.abs(ExIntronnum);
	}
	protected abstract void setCod2SiteAbs();
	
	/**
	 * 当坐标在5UTR的外显子中时使用
	 * 方向为正向
	 */
	protected abstract void setCod2UTR5();
	
	/**
	 * 当坐标在3UTR的外显子中时使用
	 * 方向为正向
	 */
	protected abstract void setCod2UTR3();
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的起点和终点的距离
	 * 不包括内含子
	 */
	protected abstract void setCod2StartEndmRNA();
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的atg和uag的距离
	 * 不包括内含子
	 */
	protected abstract void setCod2StartEndCDS();

	/**
	 * 保存某个坐标和所在的内含子外显子数目
	 */
	HashMap<Integer, Integer> hashLocExInNum;
	/**
	 * @param location 该点坐标在第几个外显子或内含子中
	 * @return
	 */
	protected abstract int getLocExInNum(int location);
	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Integer, Integer> hashLocExInStart;
	/**
	 * 坐标到外显子/内含子 起点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected abstract int getLoc2ExInStart(int location);
	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	/**
	 * 坐标到外显子/内含子 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected abstract int getLoc2ExInEnd(int location);
	/**
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	public abstract int getLocdistanceSite(int location, int mRNAnum);
	/**
	 * 两个坐标之间的距离，mRNA层面，当loc1在loc2上游时，返回负数，当loc1在loc2下游时，返回正数
	 * 要求这两个坐标都在exon上.如果不符合，则返回-1000000000
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public int getLocDistance(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (cis5to3) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getLocExInNum(locSmall); int locBigExInNum = getLocExInNum(locBig);
		
		int distance = -1000000000;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getLoc2ExInEnd(locSmall) + getLoc2ExInStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + Math.abs(lsIsoform.get(i)[0] -lsIsoform.get(i)[1]) + 1;
			}
		}
		
		if ((cis5to3 && loc1 < loc2) || (!cis5to3 && loc1 > loc2)) {
			return -Math.abs(distance);
		}
		return Math.abs(distance);
	}
	
	/**
	 * 指定一个起点和一个终点坐标，将这两个坐标间的外显子区域提取出来并返回
	 * 按照基因的方向排序
	 * 大小无所谓，最后返回不依赖 startLoc和EndLoc的大小关系
	 * 如果这两个坐标不在外显子中，则返回null
	 * @return
	 */
	public ArrayList<int[]> getRangeIso(int startLoc, int EndLoc)
	{

		ArrayList<int[]> lsresult = new ArrayList<int[]>();
		int start = 0;
		int end = 0;
		if (cis5to3) {
			start = Math.min(startLoc, EndLoc);
			end = Math.max(startLoc, EndLoc);
		}
		else {
			start = Math.max(startLoc, EndLoc);
			end = Math.min(startLoc, EndLoc);
		}
		
		int exonNumStart = getLocExInNum(start) - 1;
		int exonNumEnd =getLocExInNum(end) - 1;
		
		if (exonNumStart < 0 || exonNumEnd < 0) {
			return null;
			
		}
		
		if (exonNumStart == exonNumEnd) {
			int[] exonSub = new int[2];
			exonSub[0] = start; exonSub[1] = end;
			lsresult.add(exonSub);
			return lsresult;
		}
		
		int[] exonSub = new int[2];
		exonSub[0] = start; exonSub[1] = lsIsoform.get(exonNumStart)[1];
		lsresult.add(exonSub);
		for (int i = exonNumStart+1; i < exonNumEnd; i++) {
			lsresult.add(lsIsoform.get(i));
		}
		exonSub = new int[2];
		exonSub[0] = lsIsoform.get(exonNumEnd)[0]; exonSub[1] = end; 
		lsresult.add(exonSub);
		
		return lsresult;
	
	}
	
	
	
	
}
