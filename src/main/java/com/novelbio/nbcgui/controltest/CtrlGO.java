package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
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
	
	/** 用单例模式 */
	private static CtrlGO ctrlGO = null;

	GOtype GOClass = GOtype.BP;
	GoAlgorithm goAlgorithm = GoAlgorithm.classic;
	int[] staxID;
	
	/**
	 * @param elimGo
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public static CtrlGO getInstance(GoAlgorithm goAlgorithm, GOtype GOClass, int QtaxID, boolean blast, double evalue, int... StaxID) {
		ctrlGO = new CtrlGO(goAlgorithm, GOClass, QtaxID, blast, evalue, StaxID);
		return ctrlGO;
	}
	/**
	 * 返回已有的GtrlGO
	 * @return
	 */
	public static CtrlGO getInstance() {
		return ctrlGO;
	}
	/**
	 * @param elimGo
	 * @param geneFileXls
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param colAccID
	 * @param colFC
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 */
	private CtrlGO(GoAlgorithm goAlgorithm, GOtype GOClass, int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		super(QtaxID, blast, evalue);
		this.staxID = StaxID;
		this.goAlgorithm = goAlgorithm;
		this.GOClass = GOClass;
		if (goAlgorithm != GoAlgorithm.novelgo) {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_ELIM, QtaxID, blast, evalue, StaxID);
			functionTest.setGOAlgorithm(goAlgorithm);
		} else {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_NOVELBIO, QtaxID, blast, evalue, StaxID);
		}
		functionTest.setGOtype(GOClass);
	}
	@Override
	protected void copeFile(String prix, String excelPath) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			FileOperate.moveFile(FileOperate.changeFileSuffix(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null),
					FileOperate.getParentPathName(excelPath), FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf", true);
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
		if (blast) {
			suffix = suffix + "_blast";
			MathComput.sort(staxID, true);//排个序
			for (int i : staxID) {
				suffix = suffix + "_" + i;
			}
		}
		suffix = suffix + "_" + GOClass.getOneWord();
		return FileOperate.changeFileSuffix(fileName, suffix, "txt");
	}
	
}
