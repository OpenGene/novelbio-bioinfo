package com.novelbio.base.dataStructure;
/**
 * ��ȡһ��list��һ��һ�Ե�Ԫ�صĽӿ�
 * ������ArrayOperate�бȽ�����list֮��Ĳ���
 * ʵ����������ת¼��֮��Ĳ��������ת¼��֮��Ĳ���
 * �����п�Ժ����Ӣ����Ŀ
 * @author zong0jie
 *
 */
public interface CompSubArray {
	/**
	 * ��õ�Ԫ���ӣ�������ԶС�ڷ���
	 * @return
	 */
	double[] getCell();
	Boolean isCis5to3();
	/**
	 * ����cis���������
	 * @return
	 */
	double getStartCis();
	/**
	 * ����cis�������յ�
	 * @return
	 */
	double getEndCis();
}
