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
public class StatisticsContinueATorCGCoverge {
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
	 * 获取CGBoxPlotList 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
	 * @return
	 */
	public void setCGBoxPlotList() {
		for (int i = 0; i < maxContinueATorCG; i = i + cgInterval) {
			HistList histList = HistList.creatHistList(i + "", true);
			histList.setBinAndInterval(1000, 1, 2000);
			boxPlotList.addHistList(histList);
		}
	}
	
//	/**
//	 * 大于该距离就不进行统计
//	 * @param gapMaxNum
//	 */
//	public void setGapMaxNum(int gapMaxNum) {
//		this.gapMaxNum = gapMaxNum;
//	}
//
//	public void setGffChrAbs(GffChrAbs gffChrAbs) {
//		this.gffChrAbs = gffChrAbs;
//	}
//	
//	/**
//	 * 记录AT或者CG的数量
//	 * 考虑中间断开的情况，Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
//	 * @param seqFastaGap SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
//				oneSeqInfo.getOneSeqInfoLast().getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
//	 * @param oneSeqInfoGapEdge  gap上边缘的那个site
//	 * @return 返回Gap的最后一位site
//	 */
//	public void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
//		if (seqFastaGap == null) {
//			countOneSeqInfo(oneSeqInfo);
//			return;
//		}
//		String seqGap = seqFastaGap.toString();
//		// 考虑中间断开的情况，从Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
//		// gap前面的那个位点
//		OneSeqInfo oneSeqInfoGapEdgeUp = oneSeqInfo.getOneSeqInfoLast();
//		OneSeqInfo oneSeqInfoGapEdgeNext = null;
//		char[] chrGapSeq = seqGap.toCharArray();
//		for (int i = 0; i < chrGapSeq.length; i++) {
//			String oneSeq = chrGapSeq[i] + "";
//			oneSeqInfoGapEdgeNext = oneSeqInfoGapEdgeUp.getOneSeqInfoNext(oneSeq);
//			oneSeqInfoGapEdgeUp.clearOneSeqInfoLast();
//			
//			countOneSeqInfo(oneSeqInfoGapEdgeNext);
//			oneSeqInfoGapEdgeUp = oneSeqInfoGapEdgeNext;
//		}
//		// 测试一下提取的序列有没有完全提取出来
//		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
//			logger.error("Gap 出错");
//		}
//	}

	/** 
	 * 前一个是C当前一个是A，记录前面连续C的raws平均数的数量 形式例如key是4C_10，value是num数量例如3
	 */
	public void countOneSeqInfo(OneSeqInfo oneSeqInfo) {
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
