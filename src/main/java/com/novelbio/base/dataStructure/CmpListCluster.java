package com.novelbio.base.dataStructure;

import java.util.ArrayList;

/**
 * �ϲ�����list��Ԫ�ص�cluster
 * @author zong0jie
 */
public class CmpListCluster<T extends CompSubArray>
{
	
	public static String FLAGTHIS = "flagthis";
	public static String FLAGCOMP = "flagcomp";
	
	static double ratio = 0.6;
	/**
	 * ��������overlap������ͬһ�����ڵ�Ԫ��
	 * ת¼���Ļ�һ��������0.6��peakһ������Ϊ0
	 * @param ratio
	 */
	public static void setRatio(double ratio) {
		CmpListCluster.ratio = ratio;
	}
	ArrayList<T> lsCompSubArrayInfosThis = new ArrayList<T>();
	ArrayList<T> lsCompSubArrayInfosComp = new ArrayList<T>();
	
	public ArrayList<T> getLsCompSubArrayInfosComp() {
		return lsCompSubArrayInfosComp;
	}
	public ArrayList<T> getLsCompSubArrayInfosThis() {
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
	 * ����������ǰ��
	 * @return
	 */
	public double getStartSite() {
		return startSite;
	}
	/**
	 * ������������
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
	public boolean addCompElement(T compSubArrayInfo)
	{
		if (lsCompSubArrayInfosComp.size() == 0 && lsCompSubArrayInfosThis.size() == 0) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			startSite = compSubArrayInfo.getStartCis();
			endSite = compSubArrayInfo.getEndCis();
			return true;
		}

		//��������Ĳ��ֳ���
		double[] region1 = compSubArrayInfo.getCell();
		double[] region2 = new double[]{Math.min(startSite, endSite), Math.max(startSite, endSite)};
		double tmpLen = ArrayOperate.cmpArray(region1, region2)[1];
		//overlap > ratio ���ҷ���һ��
		if ((tmpLen/compSubArrayInfo.getLen() > ratio || tmpLen/getLength() > ratio)) {
			if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGTHIS)) {
				lsCompSubArrayInfosThis.add(compSubArrayInfo);
			}
			else if (compSubArrayInfo.getFlag().equals(CompSubArrayInfo.FLAGCOMP)) {
				lsCompSubArrayInfosComp.add(compSubArrayInfo);
			}
			//�趨���
			if (compSubArrayInfo.getStartAbs() < startSite) {
				startSite = compSubArrayInfo.getStartAbs();
			}
			//�趨�յ�
			//˳ʽ�Ļ���0--1 0--1 
			if ( compSubArrayInfo.getEndAbs()> endSite) {
				endSite = compSubArrayInfo.getEndAbs();
			}
			return true;
		}
		return false;
	}
	/**
	 * �ӵ�Ԫ�ıȽϴ��
	 * @return
	 */
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
	public static<T extends CompSubArray> double getCompScore(ArrayList<CmpListCluster<T>> ls)
	{
		double score = 0;
		for (CmpListCluster<T> compSubArrayCluster : ls) {
			score = score + compSubArrayCluster.getCompScore();
		}
		return score;
	}
}