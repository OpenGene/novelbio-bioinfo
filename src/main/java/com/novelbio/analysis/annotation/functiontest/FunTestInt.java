package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import com.novelbio.database.model.modgeneid.GeneID;

public interface FunTestInt {
	
	/**
	 * �趨����
	 * @param taxID
	 */
	public void setTaxID(int taxID);
	/**
	 * ��õ�ǰ����
	 * @return
	 */
	public int getTaxID();
	
	/**
	 * ����accID��list���趨�����������
	 * @param lsCopedID
	 */
	public void setLsTestAccID(ArrayList<String> lsAccID);
	/**
	 * ����accID��copedID���趨�����������
	 * @param lsCopedID
	 */
	public void setLsTestGeneID(ArrayList<GeneID> lsCopedIDs);
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡgenUniID item,item��ʽ�ı�
	 * @param fileName
	 */
	public void setLsBGItem(String fileName);
	
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum);
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
	 * @param fileName
	 */
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID);
	/**
	 * ������
	 * ����Gene2ItemPvalue
	 * @param Type
	 * @return
	 */
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue();
	
	/**
	 * �������Ľ����ElimGO��Ҫ���Ǹ÷���
	 * �Խ���Ÿ���
	 * @return ���û�ӱ���<br>
	 * @throws Exception 
	 */
	public ArrayList<StatisticTestResult> getTestResult();

	/**
	 * Ŀǰֻ���趨GO��type
	 */
	public void setDetailType(String GOtype);
	/**
	 * GO2GeneID��Ŀǰֻ��elimGO����
	 * @return
	 */
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue();
	/**
	 * ���汾LsBG����Ϣ
	 * @param txtBGItem
	 */
	public void saveLsBGItem(String txtBGItem);
}
