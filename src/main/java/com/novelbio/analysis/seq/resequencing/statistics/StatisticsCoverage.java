package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;

public class StatisticsCoverage implements StatisticsUnit {
	private static Logger logger = Logger.getLogger(StatisticsCoverage.class);

	/** reads 覆盖的数量统计 */
	HistList histListCoverNum = HistList.creatHistList("Coverage", true);;
	
	/**
	 * <b>必须设定</b>
	 * 划分多少区域，每个区域多少interval
	 * @param binNum
	 * @param interval 等于2的话，就是每隔2个coverage统计一下，意思就是2reads覆盖，4reads覆盖的数量
	 * @param maxCoverageNum  最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并，所以可以往高里设定
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histListCoverNum.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	
	/** 记录覆盖度，有gap  */
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		histListCoverNum.addNum(oneSeqInfo.getReadsNumAll());
	}
	
	public HistList getResultHistList() {
		return histListCoverNum;
	}
}
