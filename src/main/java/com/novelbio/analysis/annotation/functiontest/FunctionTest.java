package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.GeneID;

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
	
	public static void main(String[] args) {
		FunctionTest functionTest = new FunctionTest(FUNCTION_GO_ELIM, 39947, true, 1e-10, 3702);
		functionTest.setLsBGAccID("/media/winE/Bioinformatics/GenomeData/Rice/RiceAffyBG.txt", 1);
		functionTest.saveLsBGItem("/media/winE/Bioinformatics/GenomeData/Rice/RiceAffyBG2GOBlast.txt");
	}
	
	
	/**
	 * 选择一种检验方式FUNCTION_GO_NOVELBIO等
	 * 是否blast，如果blast那么blast到哪几个物种
	 * @param functionType
	 */
	public FunctionTest(String functionType, int taxID, boolean blast, double blastevalue, int... blasttaxID)
	{
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
	public void setGOtype(String goType)
	{
		funTest.setGoType(goType);
	}
	
	
	@Override
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		funTest.setLsTestAccID(lsCopedID);
	}

	@Override
	public void setLsTest(ArrayList<GeneID> lsCopedIDs) {
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
	/**
	 * 读取AccID文件，然后将Item保存至相应的文件夹中
	 * @param fileName
	 * @param colNum
	 * @param outLsItem
	 */
	public void setLsBGAccID(String fileName, int colNum, String outLsItem) {
		funTest.setLsBGAccID(fileName, colNum);
		ArrayList<String[]> lsBG = funTest.getLsBG();
		TxtReadandWrite txtOut = new TxtReadandWrite(outLsItem, true);
		txtOut.ExcelWrite(lsBG, "\t", 1, 1);
	}
	@Override
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
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

	@Override
	public void saveLsBGItem(String txtBGItem) {
		funTest.saveLsBGItem(txtBGItem);
	}
	
}
