package com.novelbio.analysis.seq.resequencing.statistics;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;

/**
 * ����AT��CG�ĸ��Ƕ�
 * @author zong0jie
 */
public class StatisticsContinueATorCGCoverge {
	private static Logger logger = Logger.getLogger(StatisticsContinueATorCGCoverge.class);
	
	/** ����1CG��2CG��3CG....��reads���Ƕ� */
	BoxPlotList boxPlotList = new BoxPlotList();
	/** �����AT��CG������ */
	int maxContinueATorCG = 80;
	
	SeqType seqType = SeqType.AT;
	
	/** ÿ��2��cgͳ��һ�£���˼����2CG��4CG��reads���Ƕ� */
	int cgInterval = 2;
	
	
	/**
	 * @param statisticAT true: ͳ��AT 
	 * false: ͳ��CG
	 */
	public StatisticsContinueATorCGCoverge(boolean statisticAT) {
		if (statisticAT) {
			seqType = SeqType.AT;
		} else {
			seqType = SeqType.CG;
		}
	}
	/**
	 * ÿ��2��cgͳ��һ�£���˼����ͳ��2CG��4CG��reads���Ƕ�
	 * @param cgInterval
	 */
	public void setCgInterval(int cgInterval) {
		this.cgInterval = cgInterval;
	}

	/** �趨�����CG����������������Ͳ�ͳ���� */
	public void setMaxContinueATorCG(int maxContinueCG) {
		this.maxContinueATorCG = maxContinueCG;
	}

	public BoxPlotList getBoxPlotList() {
		return boxPlotList;
	}
	
	/**
	 * ��ȡCGBoxPlotList ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
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
//	 * ���ڸþ���Ͳ�����ͳ��
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
//	 * ��¼AT����CG������
//	 * �����м�Ͽ��������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ��������
//	 * @param seqFastaGap SeqFasta seqFastaGap = gffChrAbs.getSeqHash().getSeq(oneSeqInfo.getRefID(), 
//				oneSeqInfo.getOneSeqInfoLast().getRefSnpIndelStart() + 1, oneSeqInfo.getRefSnpIndelStart() - 1);
//	 * @param oneSeqInfoGapEdge  gap�ϱ�Ե���Ǹ�site
//	 * @return ����Gap�����һλsite
//	 */
//	public void countOneSeqInfoGap(SeqFasta seqFastaGap, OneSeqInfo oneSeqInfo) {
//		if (seqFastaGap == null) {
//			countOneSeqInfo(oneSeqInfo);
//			return;
//		}
//		String seqGap = seqFastaGap.toString();
//		// �����м�Ͽ����������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ��������
//		// gapǰ����Ǹ�λ��
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
//		// ����һ����ȡ��������û����ȫ��ȡ����
//		if (oneSeqInfoGapEdgeNext.getRefSnpIndelStart() + 1 != oneSeqInfo.getRefSnpIndelStart() ) {
//			logger.error("Gap ����");
//		}
//	}

	/** 
	 * ǰһ����C��ǰһ����A����¼ǰ������C��rawsƽ���������� ��ʽ����key��4C_10��value��num��������3
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
