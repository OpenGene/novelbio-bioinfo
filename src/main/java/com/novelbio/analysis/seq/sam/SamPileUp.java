package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;

public class SamPileUp {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("C:\\Users\\jie\\Desktop\\paper\\KOod.bam");
//		samFile.indexMake();chr3:81,944,867-c
		for (SamRecord samRecord : samFile.readLinesContained("chr3", 81944867, 81955050)) {
			System.out.println(samRecord.toString());
		}
	}
	/** 最多可能插入的碱基 */
	int maxBaseNum = 100;
	
	private Queue<SamRecord> queueSamRecord = new ConcurrentLinkedQueue<SamRecord>();
	/** 不能并发 */
	private Queue<Character> queueBase = new ArrayBlockingQueue<Character>(maxBaseNum);
	/** 对于某个位点来说，是否已经准备好相应的序列集 */
	boolean prepareForBase = false;
	
	Map<String, Long> mapChrIDlowcase2Length;
	
	/**
	 * 设定染色体名称与长度的对照表，注意key为小写
	 * @param mapChrIDlowcase2Length
	 */
	public void setMapChrIDlowcase2Length(
			Map<String, Long> mapChrIDlowcase2Length) {
		this.mapChrIDlowcase2Length = mapChrIDlowcase2Length;
	}
	
	private double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		
		for (SamRecord samRecord : queueSamRecord) {
			ArrayList<Align> lsAlign = samRecord.getAlignmentBlocks();
		}
		
		
		return null;
	}
	
}

class BaseInfo {
	long baseNum;
	/** + 开头表示插入
	 * - 开头表示删除
	 */
	String baseDetail;
}
