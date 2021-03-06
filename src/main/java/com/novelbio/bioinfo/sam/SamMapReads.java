package com.novelbio.bioinfo.sam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.mappedreads.MapReadsAbs;

/**
 * 基于bam文件的MapReads，适合小范围的提取序列序列
 * 譬如提取tss，genebody等，
 * 不适合提取全基因组的信息
 * 不适合提取RNA-Seq的全基因信息
 * @author zong0jie
 */
//TODO 还没考虑链特异性
public class SamMapReads extends MapReadsAbs {
	int cacheNum = 500000;
	Map<String, Long> mapChrIDlowcase2Length;
	
	SamFile samFile;
	/** 缓存 */
	double[] cacheValue;
	String chrIDCach= ""; int start = 0, end = 0;
	/** 输入的samFile必须是排序并且有索引的 */
	public SamMapReads(SamFile samFile, StrandSpecific strandSpecific) {
		if (strandSpecific == null) {
			throw new ExceptionNullParam("No Param StrandSpecific");
		}
		this.samFile = samFile;
		mapChrIDlowcase2Length = samFile.getMapChrIDLowcase2Length();
		cacheValue = new double[cacheNum];
	}

	@Override
	public void setReadsInfoFile(String mapFile) {
		this.samFile = new SamFile(mapFile);
		mapChrIDlowcase2Length = samFile.getMapChrIDLowcase2Length();
		cacheValue = new double[cacheNum];
	}
	/** catchNum不能大于5000000 */
	public void setCacheNum(int cacheNum) {
		if (cacheNum > 5000000) return;
		
		this.cacheNum = cacheNum;
		cacheValue = new double[cacheNum];
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
		return (long)allReadsNum;
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

	
	/**
	 * 使用之前务必先设定 {@link #setAllReadsNum(long)}
	 */
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
		normDouble(NormalType, result, (long)allReadsNum);
		return result;
	}

	private double[] getRangeInfo(String chrID, int startNum, int endNum) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		double[] result = new double[startEnd[1] - startEnd[0] + 1];
		if (!chrID.toLowerCase().equals(chrIDCach) || startEnd[1] > end || startEnd[0] < start) {
			cacheValue = new double[cacheNum];
			if (startEnd[1] - startEnd[0] < cacheNum - 100) {
				int media = (startEnd[1] + startEnd[0])/2;
				int range = cacheNum/2;
				int[] startEndFinal = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID,  media - range, media + range);
				chrIDCach = chrID.toLowerCase();
				start = startEndFinal[0]; end = startEndFinal[1];
				cacheValue = getRangeValueFromSam(chrID, start, end);
			} else {
				return getRangeValueFromSam(chrID, startEnd[0], startEnd[1]);
			}
		}
		int startReal = startEnd[0] - start;
		int endReal = startEnd[1] - start;
		int m = 0;
		for (int i = startReal; i <= endReal; i++) {
			result[m] = cacheValue[i];
			m++;
		}
		return result;
	}
	
	/** 从文件中读取，而不是缓存 */
	private double[] getRangeValueFromSam(String chrID, int startNum, int endNum) {
		double[] result = new double[endNum - startNum + 1];
		int[] startEnd = new int[]{startNum, endNum};
		for (SamRecord samRecord : samFile.readLinesOverlap(chrID, startNum, endNum)) {
			try {
				addReadsInfo(samRecord, startEnd, result);
			} catch (Exception e) { }
		}
		return result;
	}
	
	
	/** 将samRecord的信息添加至 result上 */
	private void addReadsInfo(SamRecord samRecord, int[] startEnd, double[] result) {
		if (booUniqueMapping && samRecord.getMappedReadsWeight() > 1) {
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
				result[i] = result[i] + (double)1/samRecord.getMappedReadsWeight();
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
		int alignEnd = align.getLength() - 1;
		
		if (align.getStartAbs() < startEnd[0]) {
			alignStart = startEnd[0] - align.getStartAbs();
		}
		if (align.getEndAbs() > startEnd[1]) {
			alignEnd = startEnd[1] - align.getStartAbs();
		}
		return new int[]{alignStart, alignEnd};
	}

	/**
	 *  用于mRNA的计算，经过标准化，和equations修正
	 * 输入坐标区间，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * @param chrID
	 * @param lsLoc 直接输入gffIso即可，<b>输入的Alignment不考虑方向</b>
	 * @param type  0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	protected List<double[]> getRangeInfoLs(String chrID, List<? extends Alignment> lsLoc, int type) {
		List<double[]> lsResult = new ArrayList<double[]>();
		if (lsLoc.size() > 1 && !lsLoc.get(0).isCis5to3()) {
			lsLoc = sortLsLoc(lsLoc);
		}
		
		int start = lsLoc.get(0).getStartAbs();
		int end = lsLoc.get(lsLoc.size() - 1).getEndAbs();
		double[] info = getRangeInfo(0, chrID, start, end, type);
		for (Alignment alignment : lsLoc) {
			double[] tmpInfo = new double[alignment.getLength()];
			int startBias = alignment.getStartAbs() - start;
			for (int i = 0; i < alignment.getLength(); i++) {
				tmpInfo[i] = info[startBias + i];
			}
			lsResult.add(tmpInfo);
		}
		return lsResult;
	}
	
	/**
	 *  用于mRNA的计算，经过标准化，和equations修正
	 * 输入坐标区间，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * @param chrID
	 * @param lsLoc 直接输入gffIso即可，<b>输入的Alignment不考虑方向</b>
	 * @param type  0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public List<double[]> getRangeInfoLsOld(String chrID, List<? extends Alignment> lsLoc) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		if (lsLoc.size() > 1 && !lsLoc.get(0).isCis5to3()) {
			lsLoc = sortLsLoc(lsLoc);
		}
	
		for (Alignment is : lsLoc) {
			double[] info = getRangeInfo(0, chrID, is.getStartAbs(), is.getEndAbs(), 0);
			if (info == null) {
				return null;
			}
			lstmp.add(info);
		}
		return lstmp;
	}

	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc,
			int binNum) {
		throw new RuntimeException("cannot do this work with SamFile");
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

}
