package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.listOperate.HistList;

/**
 * 连续AT或CG的分布
 * @author zong0jie
 */
public class StatisticsContinueATorCGdestribution {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGdestribution.class);
	
	/** 绘制1CG，2CG，3CG....的分布 */
	HistList histList;
	/** 最长连续AT或CG的数量 */
	int maxContinueATorCG = 80;
	
	SeqType seqType = SeqType.AT;
	
	/** 每隔2个cg统计一下，意思就是2CG，4CG的reads覆盖度 */
	int cgInterval = 2;
	
	/**
	 * @param statisticAT true: 统计AT 
	 * false: 统计CG
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
	 * 每隔2个cg统计一下，意思就是统计2CG，4CG的reads分布
	 * @param cgInterval
	 */
	public void setCgInterval(int cgInterval) {
		this.cgInterval = cgInterval;
	}

	/** 设定最长连续CG的数量，超过这个就不统计了 */
	public void setMaxContinueATorCG(int maxContinueCG) {
		this.maxContinueATorCG = maxContinueCG;
	}
	
	/**
	 * <b>必须设定</b>
	 * 划分多少区域，每个区域多少interval
	 * @param binNum
	 * @param interval 等于2的话，就是每隔2个CG统计一下，意思就是2CG的数量，4CG的数量
	 * @param maxCoverageNum  最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并，所以可以往高里设定
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histList.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	
	public HistList getHistList() {
		return histList;
	}
	
	/** 
	 * 前一个是C当前一个是A，记录前面连续C的raws平均数的数量 形式例如key是4C_10，value是num数量例如3
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
