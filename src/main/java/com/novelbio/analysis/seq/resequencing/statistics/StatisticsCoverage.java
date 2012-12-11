package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;

public class StatisticsCoverage {
	private static Logger logger = Logger.getLogger(StatisticsCoverage.class);

	/** reads ���ǵ�����ͳ�� */
	HistList histListCoverNum;
	
	/**
	 * @param statisticAT true: ͳ��AT 
	 * false: ͳ��CG
	 */
	public StatisticsCoverage(String histName) {
		histListCoverNum = HistList.creatHistList(histName + "Coverage", true);
	}
	/**
	 * <b>�����趨</b>
	 * ���ֶ�������ÿ���������interval
	 * @param binNum
	 * @param interval ����2�Ļ�������ÿ��2��coverageͳ��һ�£���˼����2reads���ǣ�4reads���ǵ�����
	 * @param maxCoverageNum  ���ֵ��������һλbin��û�����ֵ��������һ��bin�ͺ����ֵ�ϲ������Կ����������趨
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histListCoverNum.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	
	/** ��¼���Ƕȣ���gap  */
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		histListCoverNum.addNum(oneSeqInfo.getReadsNumAll());
	}
	
	public HistList getResultHistList() {
		return histListCoverNum;
	}
}
