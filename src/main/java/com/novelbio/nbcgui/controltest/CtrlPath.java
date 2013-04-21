package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestGene2Item;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlPath extends CtrlGOPath {
	private static final Logger logger = Logger.getLogger(CtrlPath.class);

	/**
	 * @param QtaxID
	 */
	public CtrlPath() {
		functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_PATHWAY_KEGG);
	}

	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<StatisticTestResult> lsResultTest) {
		LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();

		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(StatisticTestResult.getTitlePath());
		for (StatisticTestResult statisticTestResult : lsResultTest) {
			lsResult.add(statisticTestResult.toStringArray());
		}
		hashResult.put("Pathway_Result", lsResult);

		ArrayList<StatisticTestGene2Item> lsGene2PathPvalue = functionTest.getGene2ItemPvalue();
		ArrayList<String[]> lsGene2GoInfo = new ArrayList<String[]>();
		lsGene2GoInfo.add(lsGene2PathPvalue.get(0).getTitle());
		for (StatisticTestGene2Item statisticTestGene2Item : lsGene2PathPvalue) {
			lsGene2GoInfo.addAll(statisticTestGene2Item.toStringLs());
		}
		hashResult.put("Gene2Path", lsGene2GoInfo);

		return hashResult;
	}
	
	@Override
	protected void copeFile(String prix, String excelPath) {
	}
	@Override
	String getGene2ItemFileName(String fileName) {
		String suffix = "_Path_Item";
		int[] blastTaxID = functionTest.getBlastTaxID();
		if (functionTest.isBlast()) {
			suffix = suffix + "_blast";
			MathComput.sort(blastTaxID, true);//排个序
			for (int i : blastTaxID) {
				suffix = suffix + "_" + i;
			}
		}
		return FileOperate.changeFileSuffix(fileName, suffix, "txt");
	}

	@Override
	protected void clear() {
		functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_PATHWAY_KEGG);
	}
	
}
