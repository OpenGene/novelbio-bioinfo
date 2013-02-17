package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.GOtype;
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
	public FunctionTest(String functionType, int taxID) {
		if (functionType.equals(FUNCTION_GO_NOVELBIO)) {
			funTest = new NovelGOFunTest();
		}
		else if (functionType.equals(FUNCTION_GO_ELIM)) {
			funTest = new ElimGOFunTest();
		}
		else if (functionType.equals(FUNCTION_PATHWAY_KEGG)) {
			funTest = new KEGGPathwayFunTest();
		}
		else {
			logger.error("unknown functiontest: "+ functionType);
			return;
		}
		funTest.setTaxID(taxID);
	}
	
	/**
	 * 设定blast
	 * @param blastevalue
	 * @param blasttaxID
	 */
	public void setBlastInfo(double blastevalue, int... blasttaxID) {
		funTest.setBlast(blastevalue, blasttaxID);
	}
	public boolean isBlast() {
		return funTest.isBlast();
	}
	/** 比对到哪些物种上去了 */
	public int[] getBlastTaxID() {
		return funTest.getBlastTaxID();
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
	public void setLsTestAccID(Collection<String> lsCopedID) {
		funTest.setLsTestAccID(lsCopedID);
	}

	@Override
	public void setLsTestGeneID(Collection<GeneID> lsCopedIDs) {
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
	 * @param fileName 文本名
	 * @param colNum 读取第几列，也就是accID所在的列
	 * @param outLsItemFile 输出的文本名
	 */
	public void setLsBGAccID(String fileName, int colNum, String outLsItemFile) {
		funTest.setLsBGAccID(fileName, colNum);
		ArrayList<GeneID2LsItem> lsBG = funTest.getLsBG();
		TxtReadandWrite txtOut = new TxtReadandWrite(outLsItemFile, true);
		for (GeneID2LsItem geneID2LsGO : lsBG) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	@Override
	public void setLsBGCopedID(Collection<GeneID> lsBGaccID) {
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
	public void setDetailType(GOtype GOtype) {
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
