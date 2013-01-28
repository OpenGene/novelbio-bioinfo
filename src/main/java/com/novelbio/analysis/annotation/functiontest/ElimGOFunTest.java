package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;

public class ElimGOFunTest extends NovelGOFunTest{
	private static final Logger logger = Logger.getLogger(ElimGOFunTest.class);

	/** 和strGeneID一样的东西 */
	Set<String> setTestGeneUniID = null;
	
	TopGO topGO = new TopGO();
	
	public ElimGOFunTest() {}
	public ElimGOFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast, GOtype GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast, GoType);
	}
	public ElimGOFunTest(boolean blast,GOtype GoType, double evalue, int...blastTaxID) {
		super(blast, GoType, evalue, blastTaxID);
		this.GoType = GoType;
	}
	public void setAlgorithm(GoAlgorithm goAlgorithm) {
		topGO.setGoAlgrithm(goAlgorithm);
	}
	/** 设定展示多少个GO */
	public void setDisplayGoNum(int NumGOID) {
		topGO.setDisplayGoNum(NumGOID);
	}
	
	/**
	 * 没有就返回null
	 */
	public ArrayList<StatisticTestResult> getTestResult() {
		if (lsTestResult != null && lsTestResult.size() > 0)
			return lsTestResult;
		if (!setStrGeneID()) {
			return null;
		}
		ArrayList<String[]> lsInfo = doTest();
		for (String[] strings : lsInfo) {
			StatisticTestResult statisticTestResult = new StatisticTestResult(strings[0]);
			statisticTestResult.setItemTerm(getItemTerm(strings[0]));
			statisticTestResult.setDifGeneNum(Integer.parseInt(strings[2]),Integer.parseInt(strings[3]));
			statisticTestResult.setGeneNum(Integer.parseInt(strings[4]),Integer.parseInt(strings[5]));
			statisticTestResult.setPvalue(Double.parseDouble(strings[6]));
			lsTestResult.add(statisticTestResult);
		}
		StatisticTestResult.setFDR(lsTestResult);
		return lsTestResult;
	}
	/**
	 * 填充strGeneID：待写入文本的geneID，可以被topGO识别并计算
	 * @return
	 */
	private boolean setStrGeneID() {
		setTestGeneUniID = new HashSet<String>();//和strGeneID一样的东西
		for (GeneID2LsItem geneID2LsItem : lsTest) {
			if (!geneID2LsItem.isValidate()) {
				continue;
			}
			setTestGeneUniID.add(geneID2LsItem.getGeneUniID());
		}
		if (setTestGeneUniID.size() == 0) {
			return false;
		}
		return true;
	}
	
	private ArrayList<String[]> doTest() {
		topGO.setGoType(GoType);
		ArrayList<String> lsBG = new ArrayList<String>();
		for (GeneID2LsItem geneID2LsItem : lsBGGeneID2Items) {
			lsBG.add(geneID2LsItem.toString());
		}
		
		topGO.setLsBG(lsBG);
		topGO.setLsGene(setTestGeneUniID);
		topGO.run();
		return topGO.getLsTestResult();
	}

	/**
	 * 不包含标题
	 * 将elim的GO2Gene改成正规的Go2Gene 的List并返回
	 * @return
	 * Go富集分析的Go2Gene表格<br>
	 */
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		ArrayList<StatisticTestResult> lsTestResult = getTestResult();
		ArrayList<StatisticTestItem2Gene> lStatisticTestItem2GeneElimGos = new ArrayList<StatisticTestItem2Gene>();
		
		ArrayListMultimap<String, String> hashGo2LsGene = topGO.getGo2GeneUniIDAll();
		
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			List<String> lsTmpGeneUniID = hashGo2LsGene.get(statisticTestResult.getItemName());
			for (String geneUniID : lsTmpGeneUniID) {
				if (!setTestGeneUniID.contains(geneUniID)) {
					continue;
				}
				//同一个geneUniID对应的不同accID
				List<GeneID> lscopedIDs = mapGeneUniID2LsGeneID.get(geneUniID);
				StatisticTestItem2Gene statisticTestItem2GeneElimGo = new StatisticTestItem2Gene();
				statisticTestItem2GeneElimGo.setStatisticTestResult(statisticTestResult);
				statisticTestItem2GeneElimGo.setLsGeneIDs(lscopedIDs);
				statisticTestItem2GeneElimGo.setBlast(blast);
				lStatisticTestItem2GeneElimGos.add(statisticTestItem2GeneElimGo);
			}
		}
		return lStatisticTestItem2GeneElimGos;
	}
}
