package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import com.novelbio.analysis.annotation.copeID.CopedID;

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
	public void setLsTest(ArrayList<CopedID> lsCopedIDs);
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
	public void setLsBGCopedID(ArrayList<CopedID> lsBGaccID);
	/**
	 * ÿ�������µ�LsCopedTest���������
	 */
	ArrayList<String[]> lsAnno = null;
	/**
	 * ������
	 * ����Gene2ItemPvalue
	 * @param Type
	 * @return
	 */
	public ArrayList<String[]> getGene2ItemPvalue();
	
	/**
	 * �������Ľ����ElimGO��Ҫ���Ǹ÷���
	 * �Խ���Ÿ���
	 * @return ���û�ӱ���<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>  
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 */
	public ArrayList<String[]> getTestResult();

	/**
	 * ���ݲ�ͬ��Test�в�ͬ�����
	 * һ������
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 * @return
	 */
	public ArrayList<String[]> getGene2Item();
	/**
	 * Ŀǰֻ���趨GO��type
	 */
	public void setDetailType(String GOtype);
	/**
	 * GO2GeneID��Ŀǰֻ��elimGO����
	 * @return
	 */
	public ArrayList<String[]> getItem2GenePvalue();
}
