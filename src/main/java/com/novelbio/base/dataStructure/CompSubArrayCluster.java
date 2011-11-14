package com.novelbio.base.dataStructure;

import java.util.ArrayList;

/**
 * �ϲ�����ת¼���Ķ�����ר�����ڷ�Ӣ����Ŀ
 * @author zong0jie
 */
public class CompSubArrayCluster
{
	boolean cis;
	static double ratio = 0.6;
	public static void setRatio(double ratio) {
		CompSubArrayCluster.ratio = ratio;
	}
	ArrayList<CompSubArrayInfo> lsCompSubArrayInfosThis = new ArrayList<CompSubArrayInfo>();
	ArrayList<CompSubArrayInfo> lsCompSubArrayInfosComp = new ArrayList<CompSubArrayInfo>();
	
	public ArrayList<CompSubArrayInfo> getLsCompSubArrayInfosComp() {
		return lsCompSubArrayInfosComp;
	}
	public ArrayList<CompSubArrayInfo> getLsCompSubArrayInfosThis() {
		return lsCompSubArrayInfosThis;
	}
	//����������ǰ�ˣ�����ڱ�ת¼����˵�ģ�Ҳ����˵���Ϊtrans��ת¼������ôstartSiteʵ��Ϊ��
	double startSite;
	//������������
	double endSite;
	public double getLength() {
		return Math.abs(startSite - endSite) + 1;
	}
	/**
	 * ����������ǰ�ˣ�����ڱ�ת¼����˵�ģ�Ҳ����˵���Ϊtrans��ת¼������ôstartSiteʵ��Ϊ��
	 * @return
	 */
	public double getStartSite() {
		return startSite;
	}
	/**
	 * �����������ˣ�����ڱ�ת¼����˵�ģ�Ҳ����˵���Ϊtrans��ת¼������ôendSiteʵ��ΪС
	 * @return
	 */
	public double getEndSite() {
		return endSite;
	}
	/**
	 * �ڲ�û��������Ҫ��С����ļ��룬������ܻ����
	 * @param compSubArrayInfo
	 * @param ratio ������ֵ��ֻ�г�������ֵ�ŻὫelementװ��list��
	 * @return
	 * true���ɹ����� false��û�м���
	 */
	public boolean addCompElement(CompSubArrayInfo compSubArrayInfo)
	{
		if (lsCompSubArrayInfosComp.size() == 0 && lsCompSubArrayInfosThis.size() == 0) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			cis = compSubArrayInfo.isCis();
			startSite = compSubArrayInfo.getStart();
			endSite = compSubArrayInfo.getEnd();
			return true;
		}

		//��������Ĳ��ֳ���
		double[] region1 = compSubArrayInfo.getCell();
		double[] region2 = new double[]{Math.min(startSite, endSite), Math.max(startSite, endSite)};
		double tmpLen = ArrayOperate.cmpArray(region1, region2)[1];
		//overlap > ratio ���ҷ���һ��
		double thisLen = getLength();
		double compLen = compSubArrayInfo.getLen();
		if ((tmpLen/compSubArrayInfo.getLen() > ratio || tmpLen/getLength() > ratio) && compSubArrayInfo.cis == cis) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			
			//�趨���
			//˳ʽ�Ļ���0--1 0--1 
			if (compSubArrayInfo.isCis() && compSubArrayInfo.getStart() < startSite) {
				startSite = compSubArrayInfo.getStart();
			}
			if (!compSubArrayInfo.isCis() && compSubArrayInfo.getStart() > startSite) {
				startSite = compSubArrayInfo.getStart();
			}
			//�趨�յ�
			//˳ʽ�Ļ���0--1 0--1 
			if (compSubArrayInfo.isCis() && compSubArrayInfo.getEnd()> endSite) {
				endSite = compSubArrayInfo.getEnd();
			}
			
			//��ʽ��  <--  1--0   1---0
			else if ( !compSubArrayInfo.isCis() && compSubArrayInfo.getEnd() < endSite) {
				endSite = compSubArrayInfo.getEnd();
			}
			return true;
		}
		return false;
	}
	
	public double getCompScore()
	{
		if (lsCompSubArrayInfosComp.size() == 1 && lsCompSubArrayInfosThis.size() == 1) {
			double[] cell1 = lsCompSubArrayInfosComp.get(0).getCell();
			double[] cell2 = lsCompSubArrayInfosThis.get(0).getCell();
			if (cell1[0] == cell2[0] && cell1[1] == cell2[1]) {
				return 1;
			}
			else {
				return -0.8;
			}
		}
		if (lsCompSubArrayInfosComp.size() == 0 || lsCompSubArrayInfosThis.size() == 0) {
			return -1;
		}
		return -1.2;
	}
	/**
	 * ��ø�list�ķ���
	 * @param ls
	 * @return
	 */
	public static double getCompScore(ArrayList<CompSubArrayCluster> ls)
	{
		double score = 0;
		for (CompSubArrayCluster compSubArrayCluster : ls) {
			score = score + compSubArrayCluster.getCompScore();
		}
		return score;
	}
}