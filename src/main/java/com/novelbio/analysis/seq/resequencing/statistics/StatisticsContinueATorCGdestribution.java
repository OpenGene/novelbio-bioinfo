package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.listOperate.HistList;

/**
 * ����AT��CG�ķֲ�
 * @author zong0jie
 */
public class StatisticsContinueATorCGdestribution {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGdestribution.class);
	
	/** ����1CG��2CG��3CG....�ķֲ� */
	HistList histList;
	/** �����AT��CG������ */
	int maxContinueATorCG = 80;
	
	SeqType seqType = SeqType.AT;
	
	/** ÿ��2��cgͳ��һ�£���˼����2CG��4CG��reads���Ƕ� */
	int cgInterval = 2;
	
	/**
	 * @param statisticAT true: ͳ��AT 
	 * false: ͳ��CG
	 */
	public StatisticsContinueATorCGdestribution(boolean statisticAT) {
		if (statisticAT) {
			seqType = SeqType.AT;
			histList = HistList.creatHistList("ATdestribution", true);
		} else {
			seqType = SeqType.CG;
		}
	}
	
	/**
	 * ÿ��2��cgͳ��һ�£���˼����ͳ��2CG��4CG��reads�ֲ�
	 * @param cgInterval
	 */
	public void setCgInterval(int cgInterval) {
		this.cgInterval = cgInterval;
	}

	/** �趨�����CG����������������Ͳ�ͳ���� */
	public void setMaxContinueATorCG(int maxContinueCG) {
		this.maxContinueATorCG = maxContinueCG;
	}
	
	/**
	 * <b>�����趨</b>
	 * ���ֶ�������ÿ���������interval
	 * @param binNum
	 * @param interval ����2�Ļ�������ÿ��2��CGͳ��һ�£���˼����2CG��������4CG������
	 * @param maxCoverageNum  ���ֵ��������һλbin��û�����ֵ��������һ��bin�ͺ����ֵ�ϲ������Կ����������趨
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histList.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	
	public HistList getHistList() {
		return histList;
	}
	
	/** 
	 * ǰһ����C��ǰһ����A����¼ǰ������C��rawsƽ���������� ��ʽ����key��4C_10��value��num��������3
	 */
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		OneSeqInfo oneSeqInfoLast = oneSeqInfo.getOneSeqInfoLast();
		if (oneSeqInfoLast == null) {
			return;
		}
		if (oneSeqInfoLast.getSiteSeqType() == seqType) {
			histList.addNum(oneSeqInfoLast.getSameSiteNum());
		}
	}

}
