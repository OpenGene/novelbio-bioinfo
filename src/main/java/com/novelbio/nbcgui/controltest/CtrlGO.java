package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.generalConf.NovelBioConst;
import com.sun.tools.doclets.formats.html.resources.standard;

public class CtrlGO extends CtrlGOPath{

	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	/** 用单例模式 */
	private static CtrlGO ctrlGO = null;

	String GOClass = Go2Term.GO_BP;
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
	public static CtrlGO getInstance(GoAlgorithm goAlgorithm, String GOClass, int QtaxID, boolean blast, double evalue, int... StaxID) {
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
	private CtrlGO(GoAlgorithm goAlgorithm, String GOClass, int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		super(QtaxID, blast, evalue);
		this.staxID = StaxID;
		this.goAlgorithm = goAlgorithm;
		this.GOClass = GOClass;
		if (goAlgorithm != GoAlgorithm.novelgo) {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_ELIM,
					QtaxID, blast, evalue, StaxID);
		} else {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_NOVELBIO,
					QtaxID, blast, evalue, StaxID);
		}
		functionTest.setGOtype(GOClass);
	}
	public void setGOalgorithm(GoAlgorithm goAlgorithm) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			ctrlGO.setGOalgorithm(goAlgorithm);
		}
	}
	@Override
	protected void copeFile(String prix, String excelPath) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			FileOperate.moveFile(FileOperate.changeFileSuffix(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null),
					FileOperate.getParentPathName(excelPath), FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf", true);
		}
	}
	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<String[]> lsResultTest) {
			LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
			hashResult.put("GO_Result", lsResultTest);
			if (goAlgorithm != GoAlgorithm.novelgo) {
				FileOperate.changeFileSuffixReal(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null);
				ArrayList<String[]> lsGene2GO = functionTest.getGene2Item();
				hashResult.put("Gene2GO", lsGene2GO);
				
				ArrayList<String[]> lsGO2Gene = functionTest.getItem2GenePvalue();
				hashResult.put("GO2Gene", lsGO2Gene);
				
			}
			else {
				ArrayList<String[]> lsGene2GOPvalue = functionTest.getGene2ItemPvalue();
				hashResult.put("Gene2GO", lsGene2GOPvalue);
			}
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
		if (GOClass.equals(Go2Term.GO_BP)) {
			suffix = suffix + "_" + Go2Term.FUN_SHORT_BIO_P;
		} else if (GOClass.equals(Go2Term.GO_CC)) {
			suffix = suffix + "_" + Go2Term.FUN_SHORT_CEL_C;
		} else if (GOClass.equals(Go2Term.GO_MF)) {
			suffix = suffix + "_" + Go2Term.FUN_SHORT_MOL_F;
		} else if (GOClass.equals(Go2Term.GO_ALL)) {
			suffix = suffix + "_ALL";
		}
		return FileOperate.changeFileSuffix(fileName, suffix, "txt");
	}
	
	@Override
	String[] getResultTitle() {
		String[] title = new String[10];
		title[0] = "GOID"; title[1] = "GOTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInGOID"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		return title;
	}
}
