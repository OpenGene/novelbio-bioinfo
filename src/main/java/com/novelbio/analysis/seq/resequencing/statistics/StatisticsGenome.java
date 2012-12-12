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

	/** ��¼������Ϊ����ٷֱ���׼�� */
	int recorderNum = 0;
	/** ������gap�Ͳ�ͳ���ˣ�0��ʾ���ܶ��ٶ�ͳ�� */
	int gapMaxNum = 0;
			
		/** ��¼Exonλ����Ϣ��list��Start_end; 
	 * �����Ӳ����õ�����Ϊ�����Ӳ����ͳ������������ĸ��Ƕȡ�
	 */
	Queue<? extends Alignment> queueExonStartAndEnd;
	
	ArrayList<StatisticsUnit> lsStatisticsUnits = new ArrayList<StatisticsUnit>();

	OneSeqInfo oneSeqInfoLast;
	/**
	 * ���ڸþ���Ͳ�����ͳ��
	 * @param gapMaxNum 0 ��ʾ���ܶ��ٶ�ͳ��
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** ���Ҫͳ�Ƶ�ģ�� */
	public void addStatisticUnits(StatisticsUnit statisticsUnit) {
		lsStatisticsUnits.add(statisticsUnit);
	}
	/** ���ͳ��ģ�� */
	public void clearStatisticUnits() {
		lsStatisticsUnits.clear();
	}
	
	/**
	 * ���õ�ǰ����Exon��λ����Ϣ
	 * @param lsExonStartAndEnd
	 */
	public void setQueueExonStartAndEnd(Queue<? extends Alignment> queueExonStartAndEnd) {
		this.queueExonStartAndEnd = queueExonStartAndEnd;
	}
	
	/** ��ȡ�ļ�������ͳ�� */
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
	// TODO ���һ��
	/**
	 * �ж�λ���Ƿ�����queueExonStartAndEnd��������
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
	
	/** ͳ�Ƹ�λ������ */
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
	 * �����м�Ͽ��������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ��������
	 * @param seqFastaGap
	 * @param oneSeqInfoGapEdge  gap�ϱ�Ե���Ǹ�site
	 * @return ����Gap�����һλsite
	 */
	public void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
		if (seqFastaGap == null) {
			countOneSeqInfo(oneSeqInfo);
			return;
		}
		String seqGap = seqFastaGap.toString();
		// �����м�Ͽ����������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ��������
		// gapǰ����Ǹ�λ��
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
		// ����һ����ȡ��������û����ȫ��ȡ����
		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap ����");
		}
	}

}
