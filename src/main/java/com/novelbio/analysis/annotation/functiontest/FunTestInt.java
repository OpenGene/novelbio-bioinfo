package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import com.novelbio.analysis.annotation.copeID.CopedID;

public interface FunTestInt {
	
	/**
	 * 设定物种
	 * @param taxID
	 */
	public void setTaxID(int taxID);
	/**
	 * 获得当前物种
	 * @return
	 */
	public int getTaxID();
	
	/**
	 * 给定accID的list，设定待检验的样本
	 * @param lsCopedID
	 */
	public void setLsTestAccID(ArrayList<String> lsAccID);
	/**
	 * 给定accID的copedID，设定待检验的样本
	 * @param lsCopedID
	 */
	public void setLsTest(ArrayList<CopedID> lsCopedIDs);
	/**
	 * 最好能第一时间设定
	 * 读取genUniID item,item格式的表
	 * @param fileName
	 */
	public void setLsBGItem(String fileName);
	
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum);
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGCopedID(ArrayList<CopedID> lsBGaccID);
	/**
	 * 每次设置新的LsCopedTest后必须重置
	 */
	ArrayList<String[]> lsAnno = null;
	/**
	 * 待修正
	 * 返回Gene2ItemPvalue
	 * @param Type
	 * @return
	 */
	public ArrayList<String[]> getGene2ItemPvalue();
	
	/**
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * 对结果排个序
	 * @return 结果没加标题<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1到n:item信息 <br>  
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
	 * 根据不同的Test有不同的情况
	 * 一般如下
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 * @return
	 */
	public ArrayList<String[]> getGene2Item();
	/**
	 * 目前只能设定GO的type
	 */
	public void setDetailType(String GOtype);
	/**
	 * GO2GeneID，目前只有elimGO才有
	 * @return
	 */
	public ArrayList<String[]> getItem2GenePvalue();
}
