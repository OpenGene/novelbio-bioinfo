package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class CtrlGO extends CtrlGOPath{

	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	/**
	 * 用单例模式
	 */
	private static CtrlGO ctrlGO = null;

	String GOClass = Go2Term.GO_BP;
	boolean elimGo = true;

	/**
	 * @param elimGo
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public static CtrlGO getInstance(boolean elimGo, String GOClass, int QtaxID, boolean blast, double evalue, int... StaxID) {
		ctrlGO = new CtrlGO(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
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
	private CtrlGO(boolean elimGo, String GOClass, int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		super(QtaxID, blast, evalue);
		this.elimGo = elimGo;
		this.GOClass = GOClass;
		if (elimGo) {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_ELIM,
					QtaxID, blast, evalue, StaxID);
		} else {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_NOVELBIO,
					QtaxID, blast, evalue, StaxID);
		}
		functionTest.setGOtype(GOClass);
	}

	@Override
	protected void copeFile(String prix, String excelPath) {
		if (elimGo) {
			FileOperate.moveFile(FileOperate.changeFileSuffix(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null),
					FileOperate.getParentPathName(excelPath), FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf", true);
		}
	}
	@Override
	protected LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<String[]> lsResultTest) {
			LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
			hashResult.put("GO_Result", lsResultTest);
			if (elimGo) {
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
	void setLsBGAccIDsave(String fileName) {
		functionTest.setLsBGAccID(fileName, 1,FileOperate.changeFileSuffix(fileName, "_GOItem", "txt"));
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
