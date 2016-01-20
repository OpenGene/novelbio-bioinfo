package com.novelbio.analysis.seq.fastq;

import java.util.Map;
import java.util.TreeSet;
//TODO 序列文件有问题该如何处理
/**
 * 需要设定mapFastQFilter
 * @author zong0jie
 */
public class FQrecordFilterQC extends FQrecordFilter {
	/** fastQ里面asc||码的指标与最大比例
	 * value 大于等于1，表示绝对数量，value小于1，表示相对数量
	 */
	Map<Integer, Double> mapQuality2CutoffProportion;
	/** 检测哪几个quality值
	 * 从小到大排列
	 */
	int[] qcQalitySmall2Big;
	
	/**
	 * 输入为null则只会判定readsMinLen
	 * @param mapFastQFilter
	 */
	public void setMapFastQFilter(Map<Integer, Double> mapFastQFilter) {
		this.mapQuality2CutoffProportion = mapFastQFilter;
		TreeSet<Integer> setQuality = new TreeSet<>(mapQuality2CutoffProportion.keySet());
		qcQalitySmall2Big = new int[setQuality.size()];
		int i = 0;
		for (Integer integer : setQuality) {
			qcQalitySmall2Big[i++] = integer;
		}
	}
	
	/** 没用 */
	@Override
	protected int trimLeft(FastQRecord fastQRecord) {
		return 0;
	}
	@Override
	protected int trimRight(FastQRecord fastQRecord) {
		if (filter(fastQRecord)) {
			return fastQRecord.seqQuality.length();
		} else {
			return 0;
		}
	}

	@Override
	public boolean isUsing() {
		if (mapQuality2CutoffProportion == null || mapQuality2CutoffProportion.isEmpty()) {
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
		int fastqLen = fastQRecord.seqFasta.toString().length();
		if (fastqLen < readsMinLen) {
			return false;
		}
		if (this.fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET && fastQRecord.seqQuality.endsWith("BBBBBBBBBB") ) {
			return false;
		}
		if (fastQRecord.seqFasta.Length() != fastQRecord.getSeqQuality().length()) {
			return false;
		}
		/** 就看Q10，Q13和Q20就行了 */
		int[][] seqQC1 = copeFastQ(fastQRecord, qcQalitySmall2Big);
		return filterFastQ(seqQC1, fastqLen);
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
	 * @return int 按照顺序，小于等于每个Qvalue的数量<br>
	 * key quality的cutoff<br>
	 * value 小于该quality的碱基数量
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
	private boolean filterFastQ(int[][] thisFastQ, int readsLen) {
		for (int[] is : thisFastQ) {
			Double proportion = mapQuality2CutoffProportion.get(is[0]);
			if (proportion == null) {
				continue;
			} else {
				double num = proportion;
				if (proportion < 1) {
					num = proportion * readsLen;
				}
				if (num < is[1]) {
					return false;
				}
			}
			
		}
		return true;
	}


}
