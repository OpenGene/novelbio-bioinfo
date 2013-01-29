package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Map;

import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;

/**
 * 基于bam文件的MapReads，适合小范围的提取序列序列
 * 譬如提取tss，genebody等，
 * 不适合提取全基因组的信息
 * 不适合提取RNA-Seq的全基因信息
 * @author zong0jie
 *
 */
public class SamMapReads extends MapReadsAbs {
	Map<String, Long> mapChrIDlowcase2Length;
	
	SamFile samFile;
	
	/** 输入的samFile必须是排序并且有索引的 */
	public SamMapReads(SamFile samFile) {
		this.samFile = samFile;
		mapChrIDlowcase2Length = samFile.getChrID2LengthMap();
	}
	
	/**
	 * 设定染色体名称与长度的对照表，注意key为小写
	 * @param mapChrIDlowcase2Length
	 */
	public void setMapChrIDlowcase2Length(
			Map<String, Long> mapChrIDlowcase2Length) {
		this.mapChrIDlowcase2Length = mapChrIDlowcase2Length;
	}

	@Override
	protected long getAllReadsNum() {
		return allReadsNum;
	}

	@Override
	protected void ReadMapFileExp() throws Exception {
		//TODO 可以考虑通过这个来获得bam文件的reads数量
	}
	
	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum, int endNum, int type) {
		int[] startEndLoc = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEndLoc == null) {
			return null;
		}
		if (thisInvNum <= 0) {
			thisInvNum = 1;
		}
		double binNum = (double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		} else {
			binNumFinal = (int)binNum;
		}
		if (binNumFinal == 0) {
			binNumFinal = 1;
		}
		return getRangeInfo(chrID, startNum, endNum, binNumFinal, type);
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		double[] value = getRangeInfo(chrID, startNum, endNum);
		if (value == null) {
			return null;
		}
		double[] result = null;
		if (binNum <= 0) {
			result = value;
		} else {
			result = MathComput.mySpline(value, binNum, 0, 0, type);
		}
		
		normDouble(NormalType, result, allReadsNum);
		return result;
	}

	private double[] getRangeInfo(String chrID, int startNum, int endNum) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		double[] result = new double[startEnd[1] - startEnd[0] + 1];

		for (SamRecord samRecord : samFile.readLinesOverlap(chrID, startEnd[0], startEnd[1])) {
			try {
				addReadsInfo(samRecord, startEnd, result);
			} catch (Exception e) { }
		}
		
		return result;
	}
	/** 将samRecord的信息添加至 result上 */
	private void addReadsInfo(SamRecord samRecord, int[] startEnd, double[] result) {
		if (booUniqueMapping && samRecord.getMappingNum() > 1) {
			return;
		}
		ArrayList<Align> lsAlign = samRecord.getAlignmentBlocks();
		for (Align align : lsAlign) {
			if (isInRegion(startEnd, align) == 1) {
				continue;
			} else if (isInRegion(startEnd, align) == 2) {
				break;
			}
			int[] startEndRegion = getStartEndLoc(startEnd, align);
			for (int i = startEndRegion[0]; i <= startEndRegion[1]; i++) {
				result[i] = result[i] + (double)1/samRecord.getMappingNum();
			}
		}
	}
	
	/**
	 * 看align是否在region中
	 * @param region 区间，必须  region[0] < region[1]
	 * @param align
	 * @return 0 inside
	 * 1 align before region
	 * 2 align after region
	 */
	private static int isInRegion(int[] region, Alignment align) {
		if (align.getEndAbs() < region[0]) {
			return 1;
		} else if (align.getStartAbs() > region[1]) {
			return 2;
		} else {
			return 0;
		}
	}
	
	/**
	 * <b>align只看getStartAbs和getEndAbs</b><br>
	 * 给定一个alignment，确定其相对于 startEnd 这个范围的相对坐标
	 * 从0开始计算
	 * @param startEnd
	 * @param alignment 如果alignmen超出了starend的范围，则从头/尾开始计算。譬如小于0就设置为0
	 * @return 返回从0开始计算的相对startLoc和endLoc坐标
	 * 可以直接当作下标使用
	 */
	private static int[] getStartEndLoc(int[] startEnd, Alignment align) {
		int startLoc = align.getStartAbs() - startEnd[0];
		int endLoc = align.getEndAbs() - startEnd[0];
		if (startLoc < 0) {
			startLoc = 0;
		}
		int length = startEnd[1] - startEnd[0];
		if (endLoc > length) {
			endLoc = length;
		}
		return new int[]{startLoc, endLoc};
	}
	
	/** 
	 * <b>align只看getStartAbs和getEndAbs</b><br>
	 * 给定一个alignment，确定其相对于 startEnd 这个范围，所能使用的起点和终点
	 * 从0开始计算。
	 * 如果alignment与startEnd是overlap的，那么从alignment的第几位开始算起，到第几位结束
	 * @param startEnd
	 * @param alignment
	 * @return 返回从0开始计算的相对align的起点和终点坐标
	 * 可以直接当作下标使用
	 */
	private static int[] getStartEndAlign(int[] startEnd, Alignment align) {
		int alignStart = 0;
		int alignEnd = align.Length() - 1;
		
		if (align.getStartAbs() < startEnd[0]) {
			alignStart = startEnd[0] - align.getStartAbs();
		}
		if (align.getEndAbs() > startEnd[1]) {
			alignEnd = startEnd[1] - align.getStartAbs();
		}
		return new int[]{alignStart, alignEnd};
	}

}
