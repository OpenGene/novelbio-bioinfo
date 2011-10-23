package com.novelbio.analysis.annotation.functiontest;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

public class ElimGOFunTest extends NovelGOFunTest{
	private static final Logger logger = Logger.getLogger(ElimGOFunTest.class);
	public ElimGOFunTest(ArrayList<CopedID> lsCopedIDsTest, ArrayList<CopedID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast, GoType);
	}
	public ElimGOFunTest(boolean blast,String GoType, double evalue, int...blastTaxID) {
		super(blast, GoType, evalue, blastTaxID);
		this.GoType = GoType;
	}
	int NumGOID = 300;
	
	/**
	 * 设定参数
	 */
	public void setNumGOID(int NumGOID) {
		this.NumGOID = NumGOID;
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
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 6);
		}
		else {
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 3);
		}
		return lsResult;
	}
	
	/**
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
	 */
	public ArrayList<String[]> getTestResult()
	{
		setStrGeneID();
		TxtReadandWrite txtParam = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_PARAM, true);
		String content = "";
		if (GoType.equals(GOInfoAbs.GO_BP)) 
			content = "BP";
		else if (GoType.equals(GOInfoAbs.GO_MF)) 
			content = "MF";
		else if (GoType.equals(GOInfoAbs.GO_CC)) 
			content = "CC";
		content =content + " "+NovelBioConst.R_WORKSPACE_TOPGO_GORESULT+" "+ NumGOID + " " + NovelBioConst.R_WORKSPACE_TOPGO_GOINFO;
		txtParam.writefile(content); txtParam.close();
		//BG
		TxtReadandWrite txtTopGoBG = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_BGGeneGo, true);
		txtTopGoBG.ExcelWrite(lsBG, "\t", 1, 1); txtTopGoBG.close();
		///////////////待分析geneID/////////////////////////////////////////////
		TxtReadandWrite txtGenID= new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_GENEID, true);
		txtGenID.Rwritefile(strGeneID); txtGenID.close();
		//执行
		String command=NovelBioConst.R_SCRIPT + NovelBioConst.R_WORKSPACE_TOPGO_RSCRIPT;
		CmdOperate cmdOperate = new CmdOperate(command);
		cmdOperate.doInBackground();
		//读取
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_GORESULT, false);
		ArrayList<String[]> lsElimTable = txtRGo2Gene.ExcelRead("\t", 2, 2, txtRGo2Gene.ExcelRows(), txtRGo2Gene.ExcelColumns("\t"), 0);
		//去除"号
		for (String[] strings : lsElimTable) 
		{
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].replace("\"", "");
			}
		}
		String[] title = new String[10];
		title[0] = "GOID"; title[1] = "GOTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInGOID"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		lsElimTable.add(0,title);
		return lsElimTable;
	}

	
	String[] strGeneID = null;
	ArrayList<String> lsGeneID = null;
	private void setStrGeneID()
	{
		String[] strGeneID = new String[lsTest.size()];//用于elim检验
		ArrayList<String> lsGeneID = new ArrayList<String>();//和strGeneID一样的东西
		for (int i = 0; i < strGeneID.length; i++) {
			strGeneID[i] = lsTest.get(i)[0];
			lsGeneID.add(strGeneID[i]);
		}
	}
	
	
	
	/**
	 * 不包含标题
	 * 将elim的GO2Gene改成正规的Go2Gene 的List并返回
	 * @param hashElimGo2Gene topGO中所含有的go2gene的信息
	 * @oaram lsResultTable topGO所生成的GO结果的table
	 * @param lsGeneID 该次分析的的所有差异基因列表
	 * @return
	 * Go富集分析的Go2Gene表格<br>
	 * blast：<br>title2[0]="GOID";title2[1]="GOTerm"
	 * 			title2[2]="QueryID";title2[3]="QuerySymbol";title2[4]="Description";title2[5]="Evalue";title2[6]="subjectSymbol";<br>
			title2[7]="Description";<br>
			不blast：<br>title2[0]="GOID";title2[1]="GOTerm"
						title2[2]="QueryID";title2[3]="QuerySymbol";title2[4]="Description";<br>
	 */
	private ArrayList<String[]> getElimGo2Gene(ArrayList<String[]> lsResultTable, ArrayList<String> lsGeneID)
	{
		HashMap<String, ArrayList<String>> hashElimGo2Gene = null;
		try {
			hashElimGo2Gene = getGo2GeneBG(NovelBioConst.R_WORKSPACE_TOPGO_GOINFO);
		} catch (Exception e) {
			logger.error("ElimFisher stopped: "+ NovelBioConst.R_WORKSPACE_TOPGO_GOINFO + " file error");
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			//某个GO中所含有的所有背景基因
			ArrayList<String> lsTmpGeneID = hashElimGo2Gene.get(lsResultTable.get(i)[0]);
			//获得某个GO中所含有的所有差异基因
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			//每一个Go所对应的基因
			for (String string : lsCoGeneID)
			{
				ArrayList<CopedID> lscopedIDs = hashgene2CopedID.get(string);
				CopedID copedIDFirst = lscopedIDs.get(0);
				//每一个基因所含有的多个copedID，也就是多个不同的accID
				for (CopedID copedID : lscopedIDs) {
					String[] anno = copedID.getAnnoInfo(blast);
					String[] result = new String[anno.length + 2];
					result[0] = lsResultTable.get(i)[0]; result[1] = lsResultTable.get(i)[1];
					for (int j = 2; j < result.length; j++) {
						result[j] = anno[j-2];
					}
					lsResult.add(result);
				}
			}
		}
		return lsResult;
	}
	/**
	 * 读取RGoInfo文件，将里面的GO2Gene的信息保存为ArrayList--ArrayList-String
	 * hash--GOID-lsGeneID
	 * @return
	 * @throws Exception 
	 */
	private static HashMap<String,ArrayList<String>> getGo2GeneBG(String RGoInfo) throws Exception
	{
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(RGoInfo, false);
		BufferedReader reader = txtRGo2Gene.readfile();
		String content = "";
		HashMap<String, ArrayList<String>> hashGo2Gene = new HashMap<String, ArrayList<String>>();
		ArrayList<String> lsGOGene = null;
		while((content = reader.readLine())!= null)
		{
			
			if (content.startsWith("#")) {
				lsGOGene = new ArrayList<String>();
				hashGo2Gene.put(content.replace("#", "").trim(), lsGOGene);
				continue;
			}
			if (content.trim().equals("")) {
				continue;
			}
			lsGOGene.add(content.trim());
		}
		return hashGo2Gene;
	}
}
