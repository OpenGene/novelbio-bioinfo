package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;

public class StatisticsCoverage {
	private static Logger logger = Logger.getLogger(StatisticsCoverage.class);
		
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	/** ���������䣬���ڸþ���Ͳ�����ͳ�ƣ�������Ȼͳ�� */
	private int gapMaxNum;
	/** reads ���ǵİٷֱ�ͳ�� */
	HistList histListCoverPercentage;
	/** reads ���ǵ�����ͳ�� */
	HistList histListCoverNum;
	
	int maxCoverageNum = 1000;
	/** ÿ��2��coverageͳ��һ�£���˼����2reads���ǣ�4reads���ǵ����� */
	int coverageInterval = 2;
	int binNum = 100;
	/**
	 * @param statisticAT true: ͳ��AT 
	 * false: ͳ��CG
	 */
	public StatisticsCoverage(String histName) {
		histListCoverPercentage = HistList.creatHistList(histName + "CoverPercentage", true);
	}
	/**
	 * ���ֶ�������ÿ���������interval
	 * @param binNum
	 * @param interval ����2�Ļ�������ÿ��2��coverageͳ��һ�£���˼����2reads���ǣ�4reads���ǵ�����
	 * @param maxCoverageNum
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histListCoverNum.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	/**
	 * ���ڸþ���Ͳ�����ͳ��
	 * @param gapMaxNum
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * ��ȡCGBoxPlotList ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
	 * @return
	 */
	public void setHistList() {
		
	}
	

	/**
	 * ��¼AT����CG������
	 * @param oneSeqInfo
	 */
	public void countOneCGAndATCover(OneSeqInfo oneSeqInfo) {
		if (!oneSeqInfo.isSameSiteType_And_Not_N()) {
			recordCG_rawsLength2Num(oneSeqInfo);
			return;
		}
		//��gap
		if (oneSeqInfo.isGapWithOneSeqLast()) {
			if (oneSeqInfo.getGapLengthWithLastSeq() > gapMaxNum) {
				recordCG_rawsLength2Num(oneSeqInfo);
				return;
			}
			getNextSeqInfoInGap_And_Statistics(oneSeqInfo);
		}
	}
	
	/**
	 * �����м�Ͽ��������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ�������� ��󷵻�Gapĩβ���Ǹ�OneSeqInfo
	 * @param oneSeqInfoGapEdge  gap�ϱ�Ե���Ǹ�site
	 * @param seqGap gap����
	 * @return ����Gap�����һλsite
	 */
	private void getNextSeqInfoInGap_And_Statistics(OneSeqInfo oneSeqInfo) {
		SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
				oneSeqInfo.getOneSeqInfoLast().getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
		if (seqFastaGap == null) {
			recordCG_rawsLength2Num(oneSeqInfo);
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
			
			recordCG_rawsLength2Num(oneSeqInfoGapEdgeNext);
			oneSeqInfoGapEdgeUp = oneSeqInfoGapEdgeNext;
		}
		// ����һ����ȡ��������û����ȫ��ȡ����
		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap ����");
		}
	}

	/** 
	 * ǰһ����C��ǰһ����A����¼ǰ������C��rawsƽ���������� ��ʽ����key��4C_10��value��num��������3
	 */
	private void recordCG_rawsLength2Num(OneSeqInfo oneSeqInfo) {
		OneSeqInfo oneSeqInfoLast = oneSeqInfo.getOneSeqInfoLast();
		if (oneSeqInfoLast == null) {
			return;
		}
		Integer cgNum = oneSeqInfoLast.getSameSiteNum();
		HistList histList = boxPlotList.getHistList(getKeyCGnum(cgNum));
		
		double coverageAvg = oneSeqInfoLast.getSameSiteNumAvg();
		if (oneSeqInfoLast.getSiteSeqType() == seqType) {
			histList.addNum(norm4To5(coverageAvg));
		}
	}
	
	/**
	 * �������������CG�������������boxplot�в��ҵ�key
	 * @return
	 */
	private String getKeyCGnum(int cgNum) {
		if (cgNum > maxContinueATorCG) {
			cgNum = maxContinueATorCG;
		}
		double intervalNum = (double)cgNum/cgInterval;
		int num = (int)Math.ceil(intervalNum);		
		return num + "";
	}
	/**
	 * ��������
	 * @param num
	 * @return
	 */
	private static int norm4To5(Double num) {
		num = num + 0.5;
		return num.intValue();
	}


}
