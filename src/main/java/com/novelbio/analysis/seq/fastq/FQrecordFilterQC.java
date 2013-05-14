package com.novelbio.analysis.seq.fastq;

import java.util.Map;

/**
 * 需要设定mapFastQFilter
 * @author zong0jie
 */
public class FQrecordFilterQC extends FQrecordFilter {
	/** fastQ里面asc||码的指标与个数 */
	Map<Integer, Integer> mapFastQFilter;
	
	/**
	 * 输入为null则只会判定readsMinLen
	 * @param mapFastQFilter
	 */
	public void setMapFastQFilter(Map<Integer, Integer> mapFastQFilter) {
		this.mapFastQFilter = mapFastQFilter;
	}
	
	/** 没用 */
	@Override
	protected int trimLeft(FastQRecord fastQRecord) {
		return 0;
	}
	/** 没用 */
	@Override
	protected int trimRight(FastQRecord fastQRecord) {
		return 0;
	}

	@Override
	public boolean isUsing() {
		if (mapFastQFilter == null || mapFastQFilter.size() == 0) {
			return false;
		}
		return true;
	}
	/**
	 * 直接返回是否通过质检
	 * 看本序列的质量是否符合要求 首先会判定质量是否以BBBBB结尾，是的话直接跳过 
	 * @return
	 */
	public boolean filter(FastQRecord fastQRecord) {
		makeSureSeqLenEqualQualityLen(fastQRecord);
		if (fastQRecord.seqFasta.toString().length() < readsMinLen) {
			return false;
		}
		if (this.fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET && fastQRecord.seqQuality.endsWith("BBBBBBBBBB") ) {
			return false;
		}
		if (fastQRecord.seqFasta.Length() != fastQRecord.getSeqQuality().length()) {
			return false;
		}
		/** 就看Q10，Q13和Q20就行了 */
		int[][] seqQC1 = copeFastQ(fastQRecord, 2, 10, 13, 20);
		return filterFastQ(seqQC1);
	}
	
	/** 保证序列长度和质量长度一致 */
	private void makeSureSeqLenEqualQualityLen(FastQRecord fastQRecord) {
		int seqLen = fastQRecord.seqFasta.Length();
		int qualityLen = fastQRecord.getSeqQuality().length();
		if (seqLen == qualityLen) {
			return;
		} else if (seqLen < qualityLen) {
			fastQRecord.seqQuality = fastQRecord.seqQuality.substring(0, seqLen);
		} else if (seqLen > qualityLen) {
			char[] qualityAppend = new char[seqLen - qualityLen];
			if (fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET) {
				for (int i = 0; i < qualityAppend.length; i++) {
					qualityAppend[i] = 'd';
				}
			} else {
				for (int i = 0; i < qualityAppend.length; i++) {
					qualityAppend[i] = 'F';
				}
			}
			fastQRecord.seqQuality = fastQRecord.seqQuality + String.copyValueOf(qualityAppend);
		}
	}
	
	/**
	 * 给定一行fastQ的ascII码，同时指定一系列的Q值，返回asc||小于该Q值的char有多少
	 * 按照Qvalue输入的顺序，输出就是相应的int[]
	 * @param Qvalue Qvalue的阈值，可以指定多个<b>必须从小到大排列</b>，一般为Q13，有时为Q10，具体见维基百科的FASTQ format
	 * @return int 按照顺序，小于等于每个Qvalue的数量
	 */
	private int[][] copeFastQ(FastQRecord fastQRecord, int... Qvalue) {
		if (fastqOffset == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = fastQRecord.seqQuality.toCharArray();
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - fastqOffset;
			/////////////////////////序列质量，每个碱基的质量分布统计/////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if (qualityScore <= Qvalue[i]) {//注意是小于等于
					qNum[i][1]++;
					continue;
				} else {
					break;
				}
			}
		}
		return qNum;
	}
	/**
	 * 将mismatich比对指标文件，看是否符合
	 * @param thisFastQ
	 * @return
	 */
	private boolean filterFastQ(int[][] thisFastQ) {
		for (int[] is : thisFastQ) {
			Integer Num = mapFastQFilter.get(is[0]);
			if (Num == null) {
				continue;
			} else if (Num < is[1]) {
				return false;
			}
		}
		return true;
	}


}
