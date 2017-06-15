package com.novelbio.analysis.seq.resequencing.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;

public class StatisticsGenome {
	private static Logger logger = Logger.getLogger(StatisticsGenome.class);
	
	GffChrAbs gffChrAbs;
	/** 最大多少gap就不统计了，0表示不管多少都统计 */
	int gapMaxNum = 0;
			
		/** 记录Exon位点信息的list，Start_end; 
	 * 外显子测序用到，因为外显子测序仅统计外显子区域的覆盖度。
	 * chrID 小写
	 */
	HashMap<String, Queue<? extends Alignment>> mapChrID2QueueExonStartAndEnd;
	ArrayList<StatisticsUnit> lsStatisticsUnits = new ArrayList<StatisticsUnit>();

	OneSeqInfo oneSeqInfoLast;
	
	String pileupFile = "";
	
	/** 设定pileup文件 */
	public void setPileupFile(String pileupFile) {
		this.pileupFile = pileupFile;
	}
	
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
	
	/**
	 * 设置当前物种Exon的位置信息
	 * @param lsExonStartAndEnd
	 */
	public void setMapChrID2QueueExonStartAndEnd(HashMap<String, Queue<? extends Alignment>> mapChrID2QueueExonStartAndEnd) {
		this.mapChrID2QueueExonStartAndEnd = mapChrID2QueueExonStartAndEnd;
	}
	
	/** 是否只统计exon区域，在外显子测序时用到
	 * <b>务必先设定setGffChrAbs()</b>
	 *  */
	public void setExonOnly(boolean isExonOnly) {
		if (isExonOnly == false) {
			mapChrID2QueueExonStartAndEnd = null;
			return;
		}
		
		//chrID小写
		HashMultimap<String, ExonInfo> mapChrID2SetExonInfo = getMapChrID2SetExonInfo();
		if (mapChrID2SetExonInfo == null) {
			mapChrID2QueueExonStartAndEnd = null;
			return;
		}
		for (String chrID : mapChrID2SetExonInfo.keySet()) {
			Set<ExonInfo> setExonInfo = mapChrID2SetExonInfo.get(chrID);
			ArrayList<ExonInfo> lsExonInfos = new ArrayList<ExonInfo>();
			for (ExonInfo exonInfo : setExonInfo) {
				lsExonInfos.add(exonInfo);
			}
			Collections.sort(lsExonInfos, new Comparator<ExonInfo>() {
				@Override
				public int compare(ExonInfo o1, ExonInfo o2) {
					Integer start1 = o1.getStartAbs();
					Integer start2 = o2.getStartAbs();
					return start1.compareTo(start2);
				}
			});
			mapChrID2QueueExonStartAndEnd.put(chrID.toLowerCase(), new LinkedList<ExonInfo>(lsExonInfos));
		}
	}
	
	/**
	 * 获得
	 * key：chrID
	 * value：无重复的exoninfo 的set
	 */
	private HashMultimap<String, ExonInfo> getMapChrID2SetExonInfo() {
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			return null;
		}
		//chrID小写
		HashMultimap<String, ExonInfo> mapChrID2SetExonInfo = HashMultimap.create();
		List<GffDetailGene> lsGffDetailGene = gffChrAbs.getGffHashGene().getLsGffDetailGenes();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (ExonInfo exonInfo : gffDetailGene.getLongestSplitMrna()) {
				Set<ExonInfo> setExonInfos = mapChrID2SetExonInfo.get(gffDetailGene.getRefID().toLowerCase());
				setExonInfos.add(exonInfo);
			}
		}
		return mapChrID2SetExonInfo;
	}
	
	/** 添加要统计的模块 */
	public void addStatisticUnits(StatisticsUnit statisticsUnit) {
		lsStatisticsUnits.add(statisticsUnit);
	}
	/** 清空统计模块 */
	public void clearStatisticUnits() {
		lsStatisticsUnits.clear();
	}

	
	/** 读取文件并进行统计 */
	public void readAndRecord() {
		int recorderNum = 0;
		TxtReadandWrite txtWrite = new TxtReadandWrite(pileupFile, false);
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
//			if (recorderNum == 300000) {
//				break;
//			}
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
		if (mapChrID2QueueExonStartAndEnd == null) {
			return true;
		}
		Queue<? extends Alignment> queueExonStartAndEnd = mapChrID2QueueExonStartAndEnd.get(oneSeqInfo.getRefID().toLowerCase());
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
		if (oneSeqInfo.isGapWithOneSeqLast() &&
				( gapMaxNum <= 0 || oneSeqInfo.getGapLengthWithLastSeq() <= gapMaxNum )) 
		{
			SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
					oneSeqInfoLast.getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
			if (seqFastaGap != null) {
				countOneSeqInfoGap(seqFastaGap, oneSeqInfo);
			} else {
				countOneSeqInfoGap( oneSeqInfo.getRefSnpIndelStart() - oneSeqInfoLast.getRefSnpIndelStart() - 1, oneSeqInfo);
			}
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
	private void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
		if (seqFastaGap == null) {
			countOneSeqInfo(oneSeqInfo);
			return;
		}
		String seqGap = seqFastaGap.toString();
		// 考虑中间断开的情况，从Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
		// gap前面的那个位点
		OneSeqInfo oneSeqInfoGapEdgeUp = oneSeqInfo.getOneSeqInfoLast();
		char[] chrGapSeq = seqGap.toCharArray();
		for (int i = 0; i < chrGapSeq.length; i++) {
			oneSeqInfoGapEdgeUp = get_And_Statistic_OneSeqGapNext(chrGapSeq[i], oneSeqInfoGapEdgeUp);
		}
		// 测试一下提取的序列有没有完全提取出来
		if (oneSeqInfoGapEdgeUp.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap 出错");
		}
	}
	/**
	 * 考虑中间断开的情况，Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
	 * @param seqFastaGap
	 * @param oneSeqInfoGapEdge  gap上边缘的那个site
	 * @return 返回Gap的最后一位site
	 */
	private void countOneSeqInfoGap(int gapLength, OneSeqInfo oneSeqInfo) {
		OneSeqInfo oneSeqInfoGapEdgeUp = oneSeqInfo.getOneSeqInfoLast();
		for (int i = 0; i < gapLength; i++) {
			oneSeqInfoGapEdgeUp = get_And_Statistic_OneSeqGapNext('N', oneSeqInfoGapEdgeUp);
		}
		// 测试一下提取的序列有没有完全提取出来
		if (oneSeqInfoGapEdgeUp.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap 出错");
		}
	}
	/**
	 * @param oneSeqNext 下一个碱基
	 * @param oneSeqInfoGapEdgeUp
	 * @return
	 */
	private OneSeqInfo get_And_Statistic_OneSeqGapNext(char oneSeqNext, OneSeqInfo oneSeqInfoGapEdgeUp) {
		OneSeqInfo oneSeqInfoGapEdgeNext = oneSeqInfoGapEdgeUp.getOneSeqInfoNext(oneSeqNext + "");
		oneSeqInfoGapEdgeUp.clearOneSeqInfoLast();
		
		countOneSeqInfo(oneSeqInfoGapEdgeNext);
		return oneSeqInfoGapEdgeNext;
	}
}
