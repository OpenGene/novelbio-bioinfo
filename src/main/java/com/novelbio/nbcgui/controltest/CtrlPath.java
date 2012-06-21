package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.generalConf.NovelBioConst;

public class CtrlPath extends CtrlGOPath {

	private static final Logger logger = Logger.getLogger(CtrlPath.class);
	/** 用单例模式  */
	private static CtrlPath ctrlPath = null;

	/**
	 * @param elimGo
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public static CtrlPath getInstance(int QtaxID, boolean blast, double evalue, int... StaxID) {
		ctrlPath = new CtrlPath(QtaxID, blast, evalue, StaxID);
		return ctrlPath;
	}
	/**
	 * 返回已有的GtrlGO
	 * @return
	 */
	public static CtrlPath getInstance() {
		return ctrlPath;
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
	private CtrlPath( int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		super(QtaxID, blast, evalue);
		functionTest = new FunctionTest(FunctionTest.FUNCTION_PATHWAY_KEGG,
					QtaxID, blast, evalue, StaxID);
	}

	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<String[]> lsResultTest) {
			LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
			hashResult.put("Pathway_Result", lsResultTest);
			ArrayList<String[]> lsGene2PathPvalue = functionTest.getGene2ItemPvalue();
			hashResult.put("Gene2Path", lsGene2PathPvalue);
		return hashResult;
	}
	
	@Override
	protected void copeFile(String prix, String excelPath) {
	}
	@Override
	String getGene2ItemFileName(String fileName) {
		return FileOperate.changeFileSuffix(fileName, "_Path_Item", "txt");
	}
	@Override
	String[] getResultTitle() {
		String[] title = new String[10];
		title[0] = "PathID"; title[1] = "PathTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInPath"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		return title;
	}
}
