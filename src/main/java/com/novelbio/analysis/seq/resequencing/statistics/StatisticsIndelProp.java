package com.novelbio.analysis.seq.resequencing.statistics;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpFilter;
import com.novelbio.base.dataStructure.listOperate.HistList;

/**
 * indel ���ֺ�ı����ֲ���Ʃ�� 0.1��ռ���٣�0.2��ռ��������
 * @author zong0jie
 */
public class StatisticsIndelProp implements StatisticsUnit {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGdestribution.class);
	
	/** 1�Ķ��ٱ�����ΪhistList����ֻ�ܷ���int������Ҫ��1 * fold��ͳ�� */
	int fold = 1000;
	/** ����0.1��indel��0.2��indel��0.3��indel....�ķֲ� */
	HistList histList = HistList.creatHistList("IndelProp", true);
	SnpFilter snpFilter = new SnpFilter();
	
	/**
	 * <b>�����趨</b>
	 * ���ֶ�������ÿ���������interval
	 * @param binNum ��������Ϊ100
	 */
	public void setBinNum(int binNum) {
		double interval = (double)fold/binNum;
		histList.setBinAndInterval(binNum, (int)interval, fold);
	}
	
	public HistList getHistList() {
		return histList;
	}
	
	/**
	 * �趨��������Ҫ���˵�snpLevel
	 * @param snpLevel �����ҵ�snp���� SnpGroupFilterInfo.HetoLess ��
	 */
	public void setSnpSampleInfo(int snpLevel) {
		snpFilter.setSampleFilterInfoSingle(snpLevel);
	}
	
	/**  ǰһ����C��ǰһ����A����¼ǰ������C��rawsƽ���������� ��ʽ����key��4C_10��value��num��������3 */
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		OneSeqInfo oneSeqInfoLast = oneSeqInfo.getOneSeqInfoLast();
		if (oneSeqInfoLast == null) {
			return;
		}
		ArrayList<SiteSnpIndelInfo> lsSnpSites = snpFilter.getFilterdSnp(oneSeqInfoLast);
		if (lsSnpSites.size() > 0) {
			histList.addNum(lsSnpSites.get(0).getReadsNum() * fold/oneSeqInfoLast.getReadsNumAll());
		}
	}

}
