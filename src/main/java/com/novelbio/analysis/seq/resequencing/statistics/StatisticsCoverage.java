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
	
	/** 间隔最大区间，大于该距离就不进行统计，等于依然统计 */
	private int gapMaxNum;
	/** reads 覆盖的百分比统计 */
	HistList histListCoverPercentage;
	/** reads 覆盖的数量统计 */
	HistList histListCoverNum;
	
	int maxCoverageNum = 1000;
	/** 每隔2个coverage统计一下，意思就是2reads覆盖，4reads覆盖的数量 */
	int coverageInterval = 2;
	int binNum = 100;
	/**
	 * @param statisticAT true: 统计AT 
	 * false: 统计CG
	 */
	public StatisticsCoverage(String histName) {
		histListCoverPercentage = HistList.creatHistList(histName + "CoverPercentage", true);
	}
	/**
	 * 划分多少区域，每个区域多少interval
	 * @param binNum
	 * @param interval 等于2的话，就是每隔2个coverage统计一下，意思就是2reads覆盖，4reads覆盖的数量
	 * @param maxCoverageNum
	 */
	public void setBinNum(int binNum, int interval, int maxCoverageNum) {
		histListCoverNum.setBinAndInterval(binNum, interval, maxCoverageNum);
	}
	/**
	 * 大于该距离就不进行统计
	 * @param gapMaxNum
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * 获取CGBoxPlotList 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
	 * @return
	 */
	public void setHistList() {
		
	}
	

	/**
	 * 记录AT或者CG的数量
	 * @param oneSeqInfo
	 */
	public void countOneCGAndATCover(OneSeqInfo oneSeqInfo) {
		if (!oneSeqInfo.isSameSiteType_And_Not_N()) {
			recordCG_rawsLength2Num(oneSeqInfo);
			return;
		}
		//有gap
		if (oneSeqInfo.isGapWithOneSeqLast()) {
			if (oneSeqInfo.getGapLengthWithLastSeq() > gapMaxNum) {
				recordCG_rawsLength2Num(oneSeqInfo);
				return;
			}
			getNextSeqInfoInGap_And_Statistics(oneSeqInfo);
		}
	}
	
	/**
	 * 考虑中间断开的情况，Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析 最后返回Gap末尾的那个OneSeqInfo
	 * @param oneSeqInfoGapEdge  gap上边缘的那个site
	 * @param seqGap gap序列
	 * @return 返回Gap的最后一位site
	 */
	private void getNextSeqInfoInGap_And_Statistics(OneSeqInfo oneSeqInfo) {
		SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
				oneSeqInfo.getOneSeqInfoLast().getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
		if (seqFastaGap == null) {
			recordCG_rawsLength2Num(oneSeqInfo);
			return;
		}
		String seqGap = seqFastaGap.toString();
		// 考虑中间断开的情况，从Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
		// gap前面的那个位点
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
		// 测试一下提取的序列有没有完全提取出来
		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
			logger.error("Gap 出错");
		}
	}

	/** 
	 * 前一个是C当前一个是A，记录前面连续C的raws平均数的数量 形式例如key是4C_10，value是num数量例如3
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
	 * 根据输入的连续CG的数量，获得在boxplot中查找的key
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
	 * 四舍五入
	 * @param num
	 * @return
	 */
	private static int norm4To5(Double num) {
		num = num + 0.5;
		return num.intValue();
	}


}
