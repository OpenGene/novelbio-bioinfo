package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 功能分析的类
 * @author zong0jie
 */
public class FunctionTest implements FunTestInt {
	public static final String FUNCTION_GO_NOVELBIO = "gene ontology";
	public static final String FUNCTION_GO_ELIM = "gene ontology elim";
	public static final String FUNCTION_PATHWAY_KEGG = "pathway kegg";
	public static final Logger logger = Logger.getLogger(FunctionTest.class);
	AbstFunTest funTest = null;
	
	/**
	 * 选择一种检验方式FUNCTION_GO_NOVELBIO等
	 * 是否blast，如果blast那么blast到哪几个物种
	 * @param functionType
	 */
	public FunctionTest(String functionType, int taxID, boolean blast, double blastevalue, int... blasttaxID) {
		if (functionType.equals(FUNCTION_GO_NOVELBIO)) {
			funTest = new NovelGOFunTest(blast, Go2Term.GO_BP, blastevalue, blasttaxID);
		}
		else if (functionType.equals(FUNCTION_GO_ELIM)) {
			funTest = new ElimGOFunTest(blast, Go2Term.GO_BP, blastevalue, blasttaxID);
		}
		else if (functionType.equals(FUNCTION_PATHWAY_KEGG)) {
			funTest = new KEGGPathwayFunTest(blast, blastevalue, blasttaxID);
		}
		else {
			logger.error("unknown functiontest: "+ functionType);
			return;
		}
		funTest.setTaxID(taxID);
	}
	/**
	 * 只能用于GO分析中
	 */
	public void setGOtype(String goType) {
		funTest.setGoType(goType);
	}
	/**
	 * 只能用于GO分析中
	 */
	public void setGOAlgorithm(GoAlgorithm goAlgorithm) {
		try {
			((ElimGOFunTest)funTest).setAlgorithm(goAlgorithm);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		funTest.setLsTestAccID(lsCopedID);
	}

	@Override
	public void setLsTestGeneID(ArrayList<GeneID> lsCopedIDs) {
		funTest.setLsTestGeneID(lsCopedIDs);
	}

	@Override
	public void setLsBGItem(String fileName) {
		funTest.setLsBGItem(fileName);
	}

	@Override
	public void setLsBGAccID(String fileName, int colNum) {
		funTest.setLsBGAccID(fileName, colNum);
	}
	/**
	 * 读取AccID文件，然后将Item保存至相应的文件夹中
	 * @param fileName
	 * @param colNum
	 * @param outLsItem
	 */
	public void setLsBGAccID(String fileName, int colNum, String outLsItem) {
		funTest.setLsBGAccID(fileName, colNum);
		ArrayList<GeneID2LsItem> lsBG = funTest.getLsBG();
		TxtReadandWrite txtOut = new TxtReadandWrite(outLsItem, true);
		for (GeneID2LsItem geneID2LsGO : lsBG) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	@Override
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
		funTest.setLsBGCopedID(lsBGaccID);
	}

	@Override
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue() {
		return funTest.getGene2ItemPvalue();
	}

	@Override
	public ArrayList<StatisticTestResult> getTestResult() {
		return funTest.getTestResult();
	}

	@Override
	public void setDetailType(String GOtype) {
		funTest.setDetailType(GOtype);
	}

	@Override
	public void setTaxID(int taxID) {
		funTest.setTaxID(taxID);		
	}

	@Override
	public int getTaxID() {
		return funTest.getTaxID();
	}

	@Override
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		return funTest.getItem2GenePvalue();
	}

	@Override
	public void saveLsBGItem(String txtBGItem) {
		funTest.saveLsBGItem(txtBGItem);
	}
	
}
