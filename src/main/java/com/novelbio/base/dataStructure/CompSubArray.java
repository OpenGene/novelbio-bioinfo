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
	/**
	 * �������������
	 * @return
	 */
	double getStartAbs();
	/**
	 * ����յ��������
	 * @return
	 */
	double getEndAbs();
	/**
	 * ���ñ�ǩ������this�黹��compare��
	 * ��CmpListCluster��static������ѡ
	 * @return
	 */
	String getFlag();
	/**
	 * ���ñ�ǩ������this�黹��compare��
	 * ��CmpListCluster��static������ѡ
	 * @return
	 */
	void setFlag(String flag);
	/**
	 * ��������Ԫ�ĳ���
	 * @return
	 */
	double getLen();
}
