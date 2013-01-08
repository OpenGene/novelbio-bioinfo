package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;

/**
 * 连续AT或CG的覆盖度
 * @author zong0jie
 */
public class StatisticsContinueATorCGCoverge implements StatisticsUnit {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGCoverge.class);
	
	/** 绘制1CG，2CG，3CG....的reads覆盖度 */
	BoxPlotList boxPlotList = new BoxPlotList();
	/** 最长连续AT或CG的数量 */
	int maxContinueATorCG = 80;
	
	SeqType seqType = SeqType.AT;
	
	/** 每隔2个cg统计一下，意思就是2CG，4CG的reads覆盖度 */
	int cgInterval = 2;
	
	
	/**
	 * @param statisticAT true: 统计AT 
	 * false: 统计CG
	 */
	public StatisticsContinueATorCGCoverge(boolean statisticAT) {
		if (statisticAT) {
			seqType = SeqType.AT;
		} else {
			seqType = SeqType.CG;
		}
	}
	/** 清空画图元素 */
	public void clean() {
		boxPlotList.clean();
	}
	
	/**
	 * 每隔2个cg统计一下，意思就是统计2CG，4CG的reads覆盖度
	 * @param cgInterval
	 */
	public void setCgInterval(int cgInterval) {
		this.cgInterval = cgInterval;
	}

	/** 设定最长连续CG的数量，超过这个就不统计了 */
	public void setMaxContinueATorCG(int maxContinueCG) {
		this.maxContinueATorCG = maxContinueCG;
	}

	public BoxPlotList getBoxPlotList() {
		return boxPlotList;
	}
	
	/**
	 * 必须先设定setCgInterval 和 setMaxContinueATorCG
	 * 设定CGBoxPlotList 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
	 * @return
	 */
	public void setBoxPlotList() {
		for (int i = cgInterval; i <= maxContinueATorCG; i = i + cgInterval) {
			HistList histList = HistList.creatHistList(i + "", true);
			histList.setBinAndInterval(1000, 1, 2000);
			boxPlotList.addHistList(histList);
		}
	}
	
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
		OneSeqInfo oneSeqInfoLast = oneSeqInfo.getOneSeqInfoLast();
		if (oneSeqInfoLast == null) {
			return;
		}
		Integer cgNum = oneSeqInfoLast.getSameSiteNum();
		HistList histList = boxPlotList.getHistList(getKeyCGnum(cgNum));
		
		double coverageAvg = oneSeqInfoLast.getSameSiteNumAvg();
		if (oneSeqInfoLast.getSiteSeqType() == seqType) {
			if (histList == null) {
				logger.error("stop");
				String a = getKeyCGnum(cgNum);
				System.out.println(a);
			}
			histList.addNum(norm4To5(coverageAvg));
		}
	}
	
	/**
	 * 根据输入的连续CG的数量，获得在boxplot中查找的key
	 * @return
	 */
	private String getKeyCGnum(int cgNum) {
		if (cgNum > maxContinueATorCG) {
			cgNum = maxContinueATorCG;
		}
		double intervalNum = (double)cgNum/cgInterval;
		int num = (int)Math.ceil(intervalNum);
		return num*cgInterval + "";
	}
	/**
	 * 四舍五入
	 * @param num
	 * @return
	 */
	private static int norm4To5(Double num) {
		num = num + 0.5;
		return num.intValue();
	}

}
