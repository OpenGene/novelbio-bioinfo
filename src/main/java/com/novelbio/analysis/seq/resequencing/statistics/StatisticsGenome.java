package com.novelbio.analysis.seq.resequencing.statistics;

import java.awt.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class StatisticsGenome {
	private static Logger logger = Logger.getLogger(StatisticsGenome.class);
	
	GffChrAbs gffChrAbs;

	/** 记录总数，为下面百分比做准备 */
	int recorderNum = 0;
	/** 最大多少gap就不统计了，0表示不管多少都统计 */
	int gapMaxNum = 0;
			
		/** 记录Exon位点信息的list，Start_end; 
	 * 外显子测序用到，因为外显子测序仅统计外显子区域的覆盖度。
	 */
	Queue<? extends Alignment> queueExonStartAndEnd;
	
	ArrayList<StatisticsUnit> lsStatisticsUnits = new ArrayList<StatisticsUnit>();

	OneSeqInfo oneSeqInfoLast;
	/**
	 * 大于该距离就不进行统计
	 * @param gapMaxNum 0 表示不管多少都统计
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 添加要统计的模块 */
	public void addStatisticUnits(StatisticsUnit statisticsUnit) {
		lsStatisticsUnits.add(statisticsUnit);
	}
	/** 清空统计模块 */
	public void clearStatisticUnits() {
		lsStatisticsUnits.clear();
	}
	
	/**
	 * 设置当前物种Exon的位置信息
	 * @param lsExonStartAndEnd
	 */
	public void setQueueExonStartAndEnd(Queue<? extends Alignment> queueExonStartAndEnd) {
		this.queueExonStartAndEnd = queueExonStartAndEnd;
	}
	
	/** 读取文件并进行统计 */
	public void readAndRecord(String loadingFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(loadingFile, false);
		for (String tmpline : txtWrite.readlines()) {
			OneSeqInfo oneSeqInfo = new OneSeqInfo(tmpline, oneSeqInfoLast);
			if (!isExon(oneSeqInfo)) {
				countOneSeq(oneSeqInfo);
				oneSeqInfoLast = null;
				continue;
			}
			countOneSeq(oneSeqInfo);
			oneSeqInfoLast = oneSeqInfo;
			
			if (recorderNum % 100000 == 0) {
				logger.debug(recorderNum);
			}
			recorderNum++;
		}
	}
	// TODO 检查一下
	/**
	 * 判定位点是否落在queueExonStartAndEnd的区域内
	 * @param oneSeqInfo
	 * @return
	 */
	private boolean isExon(OneSeqInfo oneSeqInfo) {
		if (queueExonStartAndEnd == null) {
			return true;
		}
		Alignment alignment = queueExonStartAndEnd.poll();
		while (oneSeqInfo.getRefSnpIndelStart() > alignment.getEndAbs() && !queueExonStartAndEnd.isEmpty()) {
			alignment = queueExonStartAndEnd.poll();
		}
		if (oneSeqInfo.getRefSnpIndelStart() < alignment.getStartAbs()) {
			return false;
		} else {
			return true;
		}
	}
	
	/** 统计该位点的情况 */
	private void countOneSeq(OneSeqInfo oneSeqInfo) {
		if (oneSeqInfo.isGapWithOneSeqLast() && oneSeqInfo.getGapLengthWithLastSeq() <= gapMaxNum) {
			SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
					oneSeqInfoLast.getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
			countOneSeqInfoGap(seqFastaGap, oneSeqInfo);
		} else {
			countOneSeqInfo(oneSeqInfo);
		}
	}
	
	private void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		for (StatisticsUnit statisticsUnit : lsStatisticsUnits) {
			statisticsUnit.countOneSeqInfo(oneSeqInfo);
		}
	}

	/**
	 * 考虑中间断开的情况，Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
	 * @param seqFastaGap
	 * @param oneSeqInfoGapEdge  gap上边缘的那个site
	 * @return 返回Gap的最后一位site
	 */
	public void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
		if (seqFastaGap == null) {
			countOneSeqInfo(oneSeqInfo);
			return;
		}
		String seqGap = seqFastaGap.toString();
		// 考虑中间断开的情况，从Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
		// gap前面的那个位点
		OneSeqInfo oneSeqInfoGapEdgeUp = oneSeqInfo.getOneSeqInfoLast();
		OneSeqInfo oneSeqInfoGapEdgeNext = null;
		char[] chrGapSeq = seqGap.toCharArray();
		for (int i = 0; i < chrGapSeq.length; i++) {
			String oneSeq = chrGapSeq[i] + "";
			oneSeqInfoGapEdgeNext = oneSeqInfoGapEdgeUp.getOneSeqInfoNext(oneSeq);
			oneSeqInfoGapEdgeUp.clearOneSeqInfoLast();
			
			countOneSeqInfo(oneSeqInfoGapEdgeNext);
			oneSeqInfoGapEdgeUp = oneSeqInfoGapEdgeNext;
		}
		// 测试一下提取的序列有没有完全提取出来
		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap 出错");
		}
	}

}
