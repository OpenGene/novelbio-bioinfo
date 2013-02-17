package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;

import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;

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
	public void setLsTestAccID(Collection<String> lsAccID);
	/**
	 * 给定accID的copedID，设定待检验的样本
	 * @param lsCopedID
	 */
	public void setLsTestGeneID(Collection<GeneID> lsCopedIDs);
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
	public void setLsBGCopedID(Collection<GeneID> lsBGaccID);
	/**
	 * 待修正
	 * 返回Gene2ItemPvalue
	 * @param Type
	 * @return
	 */
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue();
	
	/**
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * 对结果排个序
	 * @return 结果没加标题<br>
	 * @throws Exception 
	 */
	public ArrayList<StatisticTestResult> getTestResult();

	/**
	 * 目前只能设定GO的type
	 */
	public void setDetailType(GOtype GOtype);
	/**
	 * GO2GeneID，目前只有elimGO才有
	 * @return
	 */
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue();
	/**
	 * 保存本LsBG的信息
	 * @param txtBGItem
	 */
	public void saveLsBGItem(String txtBGItem);
}
