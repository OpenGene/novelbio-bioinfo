package com.novelbio.analysis.seq.resequencing.statistics;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpFilter;
import com.novelbio.base.dataStructure.listOperate.HistList;

/**
 * indel 出现后的比例分布，譬如 0.1的占多少，0.2的占多少这种
 * @author zong0jie
 */
public class StatisticsIndelProp implements StatisticsUnit {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGdestribution.class);
	
	/** 1的多少倍，因为histList里面只能放入int，所以要把1 * fold再统计 */
	int fold = 1000;
	/** 绘制0.1个indel，0.2个indel，0.3个indel....的分布 */
	HistList histList = HistList.creatHistList("IndelProp", true);
	SnpFilter snpFilter = new SnpFilter();
	
	/**
	 * <b>必须设定</b>
	 * 划分多少区域，每个区域多少interval
	 * @param binNum 建议设置为100
	 */
	public void setBinNum(int binNum) {
		double interval = (double)fold/binNum;
		histList.setBinAndInterval(binNum, (int)interval, fold);
	}
	
	public HistList getHistList() {
		return histList;
	}
	
	/**
	 * 设定样本名和要过滤的snpLevel
	 * @param snpLevel 待查找的snp级别 SnpGroupFilterInfo.HetoLess 等
	 */
	public void setSnpSampleInfo(int snpLevel) {
		snpFilter.setSampleFilterInfoSingle(snpLevel);
	}
	
	/**  前一个是C当前一个是A，记录前面连续C的raws平均数的数量 形式例如key是4C_10，value是num数量例如3 */
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
