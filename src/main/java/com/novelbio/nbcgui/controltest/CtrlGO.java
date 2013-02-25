package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.ElimGOFunTest;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.NovelGOFunTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestGene2Item;
import com.novelbio.analysis.annotation.functiontest.StatisticTestItem2Gene;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.generalConf.NovelBioConst;
import com.sun.tools.doclets.formats.html.resources.standard;

public class CtrlGO extends CtrlGOPath{
	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	
	GOtype GOClass = GOtype.BP;
	GoAlgorithm goAlgorithm = GoAlgorithm.classic;
	int goLevel = -1;
	
	/**
	 * 必须第一时间设定，这个就会初始化检验模块
	 * 如果重新设定了该算法，则所有参数都会清空
	 * @param goAlgorithm
	 */
	public void setGoAlgorithm(GoAlgorithm goAlgorithm) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_GO_ELIM);
			((ElimGOFunTest) functionTest).setAlgorithm(goAlgorithm);
		} else {
			functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_GO_NOVELBIO);
		}
	}
	
	/** GO的层级分析，只有当算法为NovelGO时才能使用 */
	public void setGOlevel(int levelNum) {
		if (functionTest instanceof NovelGOFunTest) {
			goLevel = levelNum;
			((NovelGOFunTest) functionTest).setGOlevel(levelNum);
		}
	}
	
	public void setGOType(GOtype goType) {
		functionTest.setDetailType(goType);
	}

	@Override
	protected void copeFile(String prix, String excelPath) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			String goMapFileSource = FileOperate.changeFileSuffix(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null);
			String goMapFileTargetName = FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf";
			FileOperate.moveFile(goMapFileSource, FileOperate.getParentPathName(excelPath), goMapFileTargetName, true);
		}
	}
	
	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<StatisticTestResult> lsResultTest) {
			LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
			////////////////////////
			ArrayList<String[]> lsResult = new ArrayList<String[]>();
			lsResult.add(StatisticTestResult.getTitleGo());
			for (StatisticTestResult statisticTestResult : lsResultTest) {
				lsResult.add(statisticTestResult.toStringArray());
			}
			hashResult.put("GO_Result", lsResult);
			////////////////////////
			ArrayList<StatisticTestGene2Item> lsGene2GO = functionTest.getGene2ItemPvalue();
			ArrayList<String[]> lsGene2GoInfo = new ArrayList<String[]>();
			lsGene2GoInfo.add(lsGene2GO.get(0).getTitle());
			for (StatisticTestGene2Item statisticTestGene2Item : lsGene2GO) {
				lsGene2GoInfo.addAll(statisticTestGene2Item.toStringLs());
			}
			
//			if (goAlgorithm != GoAlgorithm.novelgo) {
				hashResult.put("Gene2GO", lsGene2GoInfo);
//			}
			
			if (goAlgorithm != GoAlgorithm.novelgo) {
				FileOperate.changeFileSuffixReal(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null);
			}
			ArrayList<StatisticTestItem2Gene> lsGO2Gene = functionTest.getItem2GenePvalue();
			ArrayList<String[]> lsGo2GeneResult = new ArrayList<String[]>();
			lsGo2GeneResult.add(StatisticTestItem2Gene.getTitleGO());
			for (StatisticTestItem2Gene statisticTestItem2GeneElimGo : lsGO2Gene) {
				lsGo2GeneResult.addAll(statisticTestItem2GeneElimGo.toStringsLs());
			}
			hashResult.put("GO2Gene", lsGo2GeneResult);
			
		return hashResult;
	}
	
	@Override
	String getGene2ItemFileName(String fileName) {
		String suffix = "_GO_Item";
		int[] blastTaxID = functionTest.getBlastTaxID();
		if (functionTest.isBlast()) {
			suffix = suffix + "_blast";
			MathComput.sort(blastTaxID, true);//排个序
			for (int i : blastTaxID) {
				suffix = suffix + "_" + i;
			}
		}
		suffix = suffix + "_" + GOClass.getOneWord();
		if (goLevel > 0) {
			suffix = suffix + "_" + goLevel + "Level";
		}
		return FileOperate.changeFileSuffix(fileName, suffix, "txt");
	}

	@Override
	protected void clear() {
		GOClass = GOtype.BP;
		goAlgorithm = GoAlgorithm.classic;
		goLevel = -1;
		functionTest = null;
	}
	
	
}
