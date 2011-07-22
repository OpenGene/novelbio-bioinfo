package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
/**
 * 计算距离时，SnnnC<br>
 * S距离C为5，S和C重合时距离为0<br>
 * CnnnATG<br>
 * C到UTRend的距离: ATGsite - coord - 1;//CnnnATG<br>
 * C到ATG的距离: coord - ATGsite//CnnnATG<br>
 * 距离本外显子起始 nnnCnn为3个碱基，距离终点为2个碱基<br>
 * 距离本外显子起始 Cnn为0个碱基<br>
 * @author zong0jie
 *
 */
public class GffGeneIsoSearchCis extends GffGeneIsoSearch {
	private static final Logger logger = Logger.getLogger(GffGeneIsoSearchCis.class);

	public GffGeneIsoSearchCis(GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		super(gffGeneIsoInfo,coord);
	}
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
	@Override
	protected void codSearchNum() {
		int ExIntronnum = getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if(coord < ATGsite){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if(coord > UAGsite){       //大于cds起始区，在3‘UTR中
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		cod2ATG = coord - ATGsite; //CnnnATG    AtgnC
		cod2UAG = coord - UAGsite; //CnuaG    UAGnnnC
		cod2TSS = coord - lsIsoform.get(0)[0];
		cod2TES = coord - lsIsoform.get(lsIsoform.size() - 1)[1];
		cod2ExInStart = getLoc2ExInStart(coord);
		cod2ExInEnd = getLoc2ExInEnd(coord);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		numExIntron = Math.abs(ExIntronnum);
	}
	
	/**
	 * 第一个计算的，计算坐标与本 外显子/内含子 的 起点/终点 的距离
	 */
	@Override
	protected void setCod2ExInStartEnd() {}
	
	/**
	 * 当坐标在5UTR的外显子中时使用
	 * 方向为正向
	 */
	@Override
	protected void setCod2UTR5() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		//             tss             0-0   0-1        1-0 cood 1-1           2-0  2-1               3-0  atg  3-1               4               5
		for (int i = 0; i < NumExon; i++) {
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
		//  tss             0        1-0    cood  atg  1-1      2               3               4               5
		if (ATGsite <= lsIsoform.get(NumExon)[1]) //一定要小于等于
		{
			cod2UTRendmRNA = ATGsite - coord - 1;//CnnnnnnATG
		}
		// tss             0        1-0    cood   1-1      2               3-0   atg   3-1                 4               5
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///Cnnnnnn
			int m = NumExon+1;
			while ( lsIsoform.get(m)[1] < ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + ATGsite - lsIsoform.get(m)[0];//nnnnnnnATG
			
			if (ATGsite < lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			}
		}
	}
	
	/**
	 * 当坐标在3UTR的外显子中时使用
	 * 方向为正向
	 */
	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag  cood  2-1               3-0      3-1                 4               5
		if ( UAGsite >= lsIsoform.get(NumExon)[0])//一定要大于等于 
		{
			cod2UTRstartmRNA = coord - UAGsite - 1;  //UAGnnnnnnnC
		}
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag    2-1               3-0      3-1                 4-0    cood    4-1               5
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //nnnnnnnC
			int m = NumExon-1;
			while (m >= 0 && lsIsoform.get(m)[0] > UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - UAGsite; //UAGnnnnn
		}
		/////////////////////utrend//////////////////
		// tss             0        1-0    atg   1-1      2-0    uag    2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2UTRendmRNA = cod2UTRendmRNA + cod2ExInEnd;
	}
	
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的起点和终点的距离
	 * 不包括内含子
	 */
	protected void setCod2StartEndmRNA() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2TSSmRNA = 0; cod2TESmRNA = 0;
		// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = 0; i < NumExon; i--) {
			cod2TSSmRNA = cod2TSSmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TESmRNA = cod2TESmRNA + cod2ExInEnd;
	}
	
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的atg和uag的距离
	 * 先计算setCod2StartEndmRNA
	 * 不包括内含子
	 */
	protected void setCod2StartEndCDS() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2ATGmRNA = 0; cod2UAGmRNA = 0;
		// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		if (codLocUTR == COD_LOCUTR_5UTR) {
			cod2ATGmRNA = -(cod2UTRendmRNA + 1);
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			cod2UAGmRNA = cod2UTRstartmRNA + 1;
		}
		//当coord在ATG下游时,为正数
		if (codLocUTR != COD_LOCUTR_5UTR) {
			// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 uag 5-1
			for (int i = 0; i < NumExon; i++) {
				if (lsIsoform.get(i)[1] < ATGsite) {
					continue;
				}
				if (lsIsoform.get(i)[0] <= ATGsite && lsIsoform.get(i)[1] >= ATGsite) {
					cod2ATGmRNA = lsIsoform.get(i)[1] - ATGsite + 1; // Atgnn   nnnnC
					continue;
				}
				cod2ATGmRNA = cod2ATGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
			}
			cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
		}
		//当coord在UAG上游时,为负数
		if (codLocUTR != COD_LOCUTR_3UTR) {
			// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1       4-0 4-1       5-0 uag 5-1       6-0 6-1
			for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
				if (lsIsoform.get(i)[0] > UAGsite) {
					break;
				}
				if (lsIsoform.get(i)[0] <= UAGsite && lsIsoform.get(i)[1] >= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + UAGsite - lsIsoform.get(i)[0] + 1; // nCnnn nnn  nnuaG
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	
	/**
	 * 保存某个坐标和所在的内含子外显子数目
	 */
	HashMap<Integer, Integer> hashLocExInNum = new HashMap<Integer, Integer>();
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	protected int getLocExInNum(int location) {
		if (hashLocExInNum.containsKey(location)) {
			return hashLocExInNum.get(location);
		}

		if (    location < lsIsoform.get(0)[0] || 
				location > lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
//			hashLocExInNum.put(location, 0);  //不在转录本内的坐标不用理会
			return 0;
		}
		for(int i = 0; i < lsIsoform.size(); i++)  //一个一个Exon的检查
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(location <= lsIsoform.get(i)[1] && location >= lsIsoform.get(i)[0]) {
				hashLocExInNum.put(location, i + 1);
				return i + 1;
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsIsoform.size() - 2 && location > lsIsoform.get(i)[1] && location < lsIsoform.get(i+1)[0]) {
				hashLocExInNum.put(location, -(i + 1));
				return -(i + 1);
			}
		}
		hashLocExInNum.put(location, 0);
		return 0;
	}
	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Integer, Integer> hashLocExInStart = new HashMap<Integer, Integer>();
	/**
	 * 坐标到外显子/内含子 起点距离
	 * @param location 坐标
	 */
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			loc2ExInStart = location - lsIsoform.get(NumExon)[0];//距离本外显子起始 nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = location - lsIsoform.get(NumExon)[1] -1;// 距前一个外显子 NnnnCnnnn
			hashLocExInStart.put(location, loc2ExInStart);
		}
		return loc2ExInStart;
	}
	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Integer, Integer> hashLocExInEnd = new HashMap<Integer, Integer>();
	/**
	 * 坐标到外显子/内含子 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected int getLoc2ExInEnd(int location) {
		if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			 loc2ExInEnd = lsIsoform.get(NumExon)[1] - location;//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			 loc2ExInEnd = lsIsoform.get(NumExon)[0] - location - 1;// 距后一个外显子 nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	/**
	 * 两个坐标之间的距离，mRNA层面，当loc1在loc2上游时，返回负数，当loc1在loc2下游时，返回正数
	 * 要求这两个坐标都在exon上.如果不符合，则返回-1000000000
	 * @param loc1 第一个坐标目
	 * @param loc2 第二个坐标
	 */
	protected int getLocDistance(int loc1, int loc2) {
		int locSmall = Math.min(loc1, loc2);
		int locBig = Math.max(loc1, loc2);
		int loc1ExInNum = getLocExInNum(locSmall);
		int loc2ExInNum = getLocExInNum(locBig);
		
		int distance = -1000000000;
		
		if (loc1ExInNum <= 0 || loc2ExInNum <= 0) {
			return distance;
		}
		
		loc1ExInNum--; loc2ExInNum--;
		if (loc1ExInNum == loc2ExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getLoc2ExInEnd(locSmall) + getLoc2ExInStart(locBig);
			for (int i = loc1ExInNum+1; i <= loc2ExInNum - 1; i++) {
				distance = distance + lsIsoform.get(i)[1] -lsIsoform.get(i)[0] + 1;
			}
		}
		if (loc1 < loc2) {
			return -distance;
		}
		return distance;
	}
	
	/**
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	public int getLocdistanceSite(int location, int mRNAnum) {
		if (getLocExInNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getLoc2ExInStart(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getLoc2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					int[] tmpExon = lsIsoform.get(i);
					// 一个一个外显子的向前遍历
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[1] - remain + 1;
					}
				}
				return -1;
			}
		} 
		else {
			if (mRNAnum <= getLoc2ExInEnd(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = mRNAnum - getLoc2ExInEnd(location);
				for (int i = exonNum + 1; i < lsIsoform.size(); i++) {
					int[] tmpExon = lsIsoform.get(i);
					// 一个一个外显子的向前遍历
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[0] + remain - 1;
					}
				}
				return -1;
			}
		}
	}
	
}
