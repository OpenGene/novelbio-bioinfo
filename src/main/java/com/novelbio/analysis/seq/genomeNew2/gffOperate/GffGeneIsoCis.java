package com.novelbio.analysis.seq.genomeNew2.gffOperate;


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
public class GffGeneIsoCis extends GffGeneIsoInfo {
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene) {
		super(IsoName, gffDetailGene);
	}

	
//	/**
//	 * 第一个计算的，计算坐标与本 外显子/内含子 的 起点/终点 的距离
//	 */
//	@Override
//	protected void setCod2ExInStartEnd() {}
	



	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	@Override
	protected int getLocExInNum(int location) {
		if (hashLocExInNum == null) {
			hashLocExInNum = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInNum.containsKey(location)) {
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
	 * 坐标到外显子/内含子 起点距离
	 * @param location 坐标
	 */
	@Override
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
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
	 * 坐标到外显子/内含子 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	@Override
	protected int getLoc2ExInEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
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
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	@Override
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



	
	
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾 添加的时候必须按照基因方向添加， 正向从小到大添加 且 int0<int1 反向从大到小添加 且
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
		lsIsoform.add(tmpexon);
	}
	/**
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected void addExonGFF(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾
		 * 添加的时候必须按照基因方向添加，
		 * 正向从小到大添加 且 int0<int1
		 * 反向从大到小添加 且 int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);

		lsIsoform.add(tmpexon);
	}
	
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		 //0    1     2     3     4     5   每个外显子中 1 > 0      0    atg   1
			for (int i = 0; i <exonNum; i++) 
			{
				if(lsIsoform.get(i)[1] < getATGSsite())    // 0       1   atg    
					FUTR = FUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
				else if (lsIsoform.get(i)[0] < getATGSsite() && lsIsoform.get(i)[1] >= getATGSsite())  //     0    atg    1 
					FUTR = FUTR + getATGSsite() - lsIsoform.get(i)[0];
				else if (lsIsoform.get(i)[0] >= getATGSsite())  //     atg   0       1   
					break;
			}
		return FUTR;
	}
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public int getLenUTR3()
	{
		int TUTR=0;
		int exonNum = lsIsoform.size();
		 //0    1     2     3     4     5   每个外显子中 0 < 1      0    uag   1
		for (int i = exonNum - 1; i >=0 ; i--) 
		{
			if(lsIsoform.get(i)[0] > getUAGsite())  //      uag     0      1
				TUTR = TUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
			else if (lsIsoform.get(i)[1] > getUAGsite() && lsIsoform.get(i)[0] <= getUAGsite())  //     0     uag    1
				TUTR = TUTR + lsIsoform.get(i)[1] - getUAGsite();
			else if (lsIsoform.get(i)[1] <= getUAGsite())   //   0      1     uag   
				break;
		}
		return TUTR;
	}

	@Override
	public boolean isCis5to3() {
		return true;
	}


	@Override
	protected void addExonGFFCDSUTR(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾 添加的时候必须按照基因方向添加， 正向从小到大添加 且 int0<int1 反向从大到小添加 且
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
		if (lsIsoform.size() > 0) {
			int[] exon = lsIsoform.get(lsIsoform.size() - 1);
			if (Math.abs(exon[1] - tmpexon[0]) == 1) {
				exon[1] = tmpexon[1];
				return;
			}
		}
		lsIsoform.add(tmpexon);

		
	}


	@Override
	public GffGeneIsoInfoCod setCod(int coord) {
		return new GffGeneIsoCisCod(this, coord);
	}
}
