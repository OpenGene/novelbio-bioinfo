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
import com.novelbio.database.model.modgeneid.GeneID;

public class ElimGOFunTest extends NovelGOFunTest{
	private static final Logger logger = Logger.getLogger(ElimGOFunTest.class);

	/** ��strGeneIDһ���Ķ��� */
	Set<String> setTestGeneUniID = null;
	
	TopGO topGO = new TopGO();
	
	public ElimGOFunTest() {}
	public ElimGOFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast, GoType);
	}
	public ElimGOFunTest(boolean blast,String GoType, double evalue, int...blastTaxID) {
		super(blast, GoType, evalue, blastTaxID);
		this.GoType = GoType;
	}
	public void setAlgorithm(GoAlgorithm goAlgorithm) {
		topGO.setGoAlgrithm(goAlgorithm);
	}
	/** �趨չʾ���ٸ�GO */
	public void setDisplayGoNum(int NumGOID) {
		topGO.setDisplayGoNum(NumGOID);
	}
	
	/**
	 * û�оͷ���null
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
	 * ���strGeneID����д���ı���geneID�����Ա�topGOʶ�𲢼���
	 * @return
	 */
	private boolean setStrGeneID() {
		setTestGeneUniID = new HashSet<String>();//��strGeneIDһ���Ķ���
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
	 * ����������
	 * ��elim��GO2Gene�ĳ������Go2Gene ��List������
	 * @return
	 * Go����������Go2Gene���<br>
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
				//ͬһ��geneUniID��Ӧ�Ĳ�ͬaccID
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
