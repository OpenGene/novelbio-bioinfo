package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.List;
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
		List<int[]> lsQuality2Num = copeFastQ(fastQRecord, qcQalitySmall2Big);
		return filterFastQ(lsQuality2Num, fastqLen);
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
	 * @param fastQRecord
	 * @param Qvalue Qvalue的阈值，可以指定多个<b>必须从小到大排列</b>，譬如 10，13，20这样多个<br>
	 * @return list-int[]，其中list的长度和输入的Qvalue一致<br>
	 * 对于每个int，int[0]--quality值<br>
	 * int[1]--<b>小于等于</b>该quality的碱基数量<br>
	 */
	private List<int[]> copeFastQ(FastQRecord fastQRecord, int... Qvalue) {
		if (fastqOffset == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
		List<int[]> lsQuality2Num = new ArrayList<>();
		for (int i = 0; i < Qvalue.length; i++) {
			int[] quality2Num = new int[2];
			quality2Num[0] = Qvalue[i];
			lsQuality2Num.add(quality2Num);
		}
		char[] fastq = fastQRecord.seqQuality.toCharArray();
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - fastqOffset;
			/////////////////////////序列质量，每个碱基的质量分布统计/////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if (qualityScore <= Qvalue[i]) {//注意是小于等于
					lsQuality2Num.get(i)[1]++;
					continue;
				} else {
					break;
				}
			}
		}
		return lsQuality2Num;
	}
	/**
	 * 将mismatich比对指标文件，看是否符合
	 * @param thisFastQ
	 * @return
	 */
	private boolean filterFastQ(List<int[]> lsQuality2Num, int readsLen) {
		for (int[] is : lsQuality2Num) {
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
