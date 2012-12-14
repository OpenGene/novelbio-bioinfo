package com.novelbio.analysis.seq.resequencing.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class StatisticsGenome {
	private static Logger logger = Logger.getLogger(StatisticsGenome.class);
	
	GffChrAbs gffChrAbs;
	/** ������gap�Ͳ�ͳ���ˣ�0��ʾ���ܶ��ٶ�ͳ�� */
	int gapMaxNum = 0;
			
		/** ��¼Exonλ����Ϣ��list��Start_end; 
	 * �����Ӳ����õ�����Ϊ�����Ӳ����ͳ������������ĸ��Ƕȡ�
	 * chrID Сд
	 */
	HashMap<String, Queue<? extends Alignment>> mapChrID2QueueExonStartAndEnd;
	ArrayList<StatisticsUnit> lsStatisticsUnits = new ArrayList<StatisticsUnit>();

	OneSeqInfo oneSeqInfoLast;
	
	String pileupFile = "";
	
	/** �趨pileup�ļ� */
	public void setPileupFile(String pileupFile) {
		this.pileupFile = pileupFile;
	}
	
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
	
	/**
	 * ���õ�ǰ����Exon��λ����Ϣ
	 * @param lsExonStartAndEnd
	 */
	public void setMapChrID2QueueExonStartAndEnd(HashMap<String, Queue<? extends Alignment>> mapChrID2QueueExonStartAndEnd) {
		this.mapChrID2QueueExonStartAndEnd = mapChrID2QueueExonStartAndEnd;
	}
	
	/** �Ƿ�ֻͳ��exon�����������Ӳ���ʱ�õ�
	 * <b>������趨setGffChrAbs()</b>
	 *  */
	public void setExonOnly(boolean isExonOnly) {
		if (isExonOnly == false) {
			mapChrID2QueueExonStartAndEnd = null;
			return;
		}
		
		//chrIDСд
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
	 * ���
	 * key��chrID
	 * value�����ظ���exoninfo ��set
	 */
	private HashMultimap<String, ExonInfo> getMapChrID2SetExonInfo() {
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			return null;
		}
		//chrIDСд
		HashMultimap<String, ExonInfo> mapChrID2SetExonInfo = HashMultimap.create();
		ArrayList<GffDetailGene> lsGffDetailGene = gffChrAbs.getGffHashGene().getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (ExonInfo exonInfo : gffDetailGene.getLongestSplitMrna()) {
				Set<ExonInfo> setExonInfos = mapChrID2SetExonInfo.get(gffDetailGene.getRefID().toLowerCase());
				setExonInfos.add(exonInfo);
			}
		}
		return mapChrID2SetExonInfo;
	}
	
	/** ���Ҫͳ�Ƶ�ģ�� */
	public void addStatisticUnits(StatisticsUnit statisticsUnit) {
		lsStatisticsUnits.add(statisticsUnit);
	}
	/** ���ͳ��ģ�� */
	public void clearStatisticUnits() {
		lsStatisticsUnits.clear();
	}

	
	/** ��ȡ�ļ�������ͳ�� */
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
			if (recorderNum == 300000) {
				break;
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
	private void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
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
