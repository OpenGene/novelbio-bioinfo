package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.copeID.CopedID;

/**
 * 功能分析的类
 * @author zong0jie
 */
public class FunctionTest implements FunTestInt{
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
	public FunctionTest(String functionType, int taxID, boolean blast, double blastevalue, int... blasttaxID)
	{
		if (functionType.equals(FUNCTION_GO_NOVELBIO)) {
			funTest = new NovelGOFunTest(blast, GOInfoAbs.GO_BP, blastevalue, blasttaxID);
		}
		else if (functionType.equals(FUNCTION_GO_ELIM)) {
			funTest = new ElimGOFunTest(blast, GOInfoAbs.GO_BP, blastevalue, blasttaxID);
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
	
	@Override
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		funTest.setLsTestAccID(lsCopedID);
	}

	@Override
	public void setLsTest(ArrayList<CopedID> lsCopedIDs) {
		funTest.setLsTest(lsCopedIDs);
	}

	@Override
	public void setLsBGItem(String fileName) {
		funTest.setLsBGItem(fileName);
	}

	@Override
	public void setLsBGAccID(String fileName, int colNum) {
		funTest.setLsBGAccID(fileName, colNum);
	}

	@Override
	public void setLsBGCopedID(ArrayList<CopedID> lsBGaccID) {
		funTest.setLsBGCopedID(lsBGaccID);
	}

	@Override
	public ArrayList<String[]> getGene2ItemPvalue() {
		return funTest.getGene2ItemPvalue();
	}

	@Override
	public ArrayList<String[]> getTestResult() {
		return funTest.getTestResult();
	}

	@Override
	public ArrayList<String[]> getGene2Item() {
		return funTest.getGene2Item();
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
	public ArrayList<String[]> getItem2GenePvalue() {
		// TODO Auto-generated method stub
		return funTest.getItem2GenePvalue();
	}
	
}
