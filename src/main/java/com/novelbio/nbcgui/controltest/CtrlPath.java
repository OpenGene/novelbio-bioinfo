package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestGene2Item;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.nbcgui.GUI.GuiGoJPanel;

public class CtrlPath extends CtrlGOPath {

	private static final Logger logger = Logger.getLogger(CtrlPath.class);
	/** 用单例模式  */
	private static CtrlPath ctrlPath = null;

	/**
	 * @param QtaxID
	 */
	public CtrlPath(int QtaxID) {
		functionTest = new FunctionTest(FunctionTest.FUNCTION_PATHWAY_KEGG, QtaxID);
	}

	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<StatisticTestResult> lsResultTest) {
		LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();

		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(StatisticTestResult.getTitleGo());
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
 
	
}
