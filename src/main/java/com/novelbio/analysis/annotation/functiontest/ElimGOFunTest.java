package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;

public class ElimGOFunTest extends NovelGOFunTest{
	private static final Logger logger = Logger.getLogger(ElimGOFunTest.class);

	/** 和strGeneID一样的东西 */
	ArrayList<String> lsGeneID = null;
	
	TopGO topGO = new TopGO();
	
	public ElimGOFunTest() {}
	public ElimGOFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast, GoType);
	}
	public ElimGOFunTest(boolean blast,String GoType, double evalue, int...blastTaxID) {
		super(blast, GoType, evalue, blastTaxID);
		this.GoType = GoType;
	}

	/** 设定展示多少个GO */
	public void setDisplayGoNum(int NumGOID) {
		topGO.setDisplayGoNum(NumGOID);
	}

	public ArrayList<String[]> getItem2GenePvalue() {
		ArrayList<String[]> lsTestResult = null;
		try {
			//同时初始化了	 strGeneID 和 lsGeneID
			lsTestResult = getTestResult();
		} catch (Exception e) {
			logger.error("error");
		}
		//lsGeneID已经在getTestResult()中初始化过了
		ArrayList<String[]> lsAnno = getElimGo2Gene(lsTestResult, lsGeneID);
		ArrayList<String[]> lsResult = null;
		if (blast) {
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 0);
		}
		else {
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 0);
		}
		int[] includeCol = new int[9];
		includeCol[0] = 0; includeCol[1] = 1; includeCol[2] = 2; includeCol[3] = 3; includeCol[4] = 4; 
		 includeCol[5] = lsResult.get(0).length - 4; includeCol[6] = lsResult.get(0).length - 3; includeCol[7] = lsResult.get(0).length - 2; 
		 includeCol[8] = lsResult.get(0).length - 1; 
		ArrayList<String[]> lsFinal = ArrayOperate.listCope(lsResult, includeCol, true);
    	String[] title=new String[9];
		title[0]="GOID";title[1]="GOTerm";title[2]="QueryID";title[3]="GeneSymbol";title[4]="Description";
		title[5]="P-Value";title[6]="FDR";title[7]="Enrichment";title[8]="(-log2P)";
		lsFinal.add(0,title);
		return lsFinal;
	}
	/**
	 * @param run 是否要新运行一次
	 * @return 结果加标题了<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1到n:item信息 <br>  
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 * 没有就返回null
	 */
	public ArrayList<String[]> getTestResult() {
		if (lsTestResult != null && lsTestResult.size() > 0)
			return lsTestResult;
		if (!setStrGeneID())
			return null;
		lsTestResult = doTest();
		return lsTestResult;
	}
	/**
	 * 填充strGeneID：待写入文本的geneID，可以被topGO识别并计算
	 * @return
	 */
	private boolean setStrGeneID() {
		ArrayList<String[]> lstest = new ArrayList<String[]>();
		for (String[] strings : lsTest) {
			if (strings[1] == null || strings[1].trim().equals("")) {
				continue;
			}
			lstest.add(strings);
		}
		if (lstest.size() == 0) {
			return false;
		}
		lsGeneID = new ArrayList<String>();//和strGeneID一样的东西
		for (int i = 0; i < lstest.size(); i++) {
			lsGeneID.add( lstest.get(i)[0]);
		}
		return true;
	}
	
	protected ArrayList<String[]> doTest() {
		topGO.setGoType(GoType);
		topGO.setLsBG(lsBG);
		topGO.setLsGene(lsGeneID);
		topGO.run();
		return topGO.getLsTestResult();
	}
	
	/**
	 * 不包含标题
	 * 将elim的GO2Gene改成正规的Go2Gene 的List并返回
	 * @param lsResultTable topGO所生成的GO结果的table
	 * @param lsGeneID 该次分析的的所有差异基因列表
	 * @return
	 * Go富集分析的Go2Gene表格<br>
	 * blast：<br>
	 * title2[0]="GOID";title2[1]="GOTerm"
	 * 			title2[2]="QueryID";title2[3]="QuerySymbol";title2[4]="Description";title2[5]="Evalue";title2[6]="subjectSymbol";<br>
			title2[7]="Description";<br>
			不blast：<br>title2[0]="GOID";title2[1]="GOTerm"
						title2[2]="QueryID";title2[3]="QuerySymbol";title2[4]="Description";<br>
	 */
	private ArrayList<String[]> getElimGo2Gene(ArrayList<String[]> lsResultTable, ArrayList<String> lsGeneID) {
		HashMap<String, ArrayList<String>> hashGo2LsGene = null;
		hashGo2LsGene = topGO.getGo2GeneAll();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 1; i < lsResultTable.size(); i++) {
			ArrayList<String> lsTmpGeneID = hashGo2LsGene.get(lsResultTable.get(i)[0]); //某个GO中所含有的所有背景基因
			ArrayList<String> lsGO2GeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID); //获得某个GO中所含有的所有差异基因
			
			for (String string : lsGO2GeneID) {
				ArrayList<GeneID> lscopedIDs = hashgene2CopedID.get(string);
				//每一个基因所含有的多个copedID，也就是多个不同的accID
				for (GeneID copedID : lscopedIDs) {
					String[] tmpresultRaw = copedID.getAnno(blast);
					String[] anno = copyAnno(copedID.getAccID(), tmpresultRaw);
					String[] result = new String[anno.length + 2];
					result[0] = lsResultTable.get(i)[0]; result[1] = lsResultTable.get(i)[1]; result[2] = copedID.getAccID();
					
					int m = 4;
					for (int j = 3; j < result.length; j++) {
						result[j] = anno[m - 3];
						m++;
					}
					
					lsResult.add(result);
				}
			}
		}
		return lsResult;
	}
}
