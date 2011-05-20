package com.novelbio.analysis.annotation.GO.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import com.novelbio.analysis.annotation.GO.queryDB.QBlastGO;
import com.novelbio.analysis.annotation.GO.queryDB.QGenID2GoInfo;
import com.novelbio.analysis.annotation.GO.queryDB.QGenID2GoInfoSepID;
import com.novelbio.analysis.annotation.blast.blastRun;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;


/**
 * 将QGenID2GoInfo获得的信息转变为让R识别的格式，最后调用R程序完成计算
 * @author zong0jie
 *
 */
public class GoFisher {
	
	static String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform/";
	
	/**
	 * 需要和R脚本中的路径相统一
	 */
	static String writeRFIle="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";
	
	/**
	 * 需要和R脚本中的路径相统一
	 */
	static String Rresult="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";

	/**
	 * 用ElimFisher的方法进行Cluster的GO分析，
	 * @param geneFileXls
	 * @param GOClass   P: biological Process F:molecular Function C: cellular Component
	 * @param colID 选择差异基因中的哪两列。0：accID，1：ID
	 * 最后生成ID个excel表格，每个表格里面就是sheet1 GO分析 sheet2 GOInfo
	 * <b>注意是实际列</b>
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003 不要加后缀xls，程序自动添加，在文件名和xls之间程序会添加上“_组ID”
	 * @throws Exception
	 */
	public static void getGoRunElim(String geneFileXls,String GOClass,int[] colID,String backGroundFile,int QtaxID,
			boolean blast, int StaxID,double evalue, String resultExcel2003) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, excelGeneID.getRowCount(), excelGeneID.getColCount(2));
		//key：组ID，value：组内GeneList
		Hashtable<String, ArrayList<String>> hashID2GeneID = new Hashtable<String, ArrayList<String>>();
		
		for (int i = 0; i < geneID.length; i++) {
			//将分组装入hash表
			if (hashID2GeneID.containsKey(geneID[i][colID[1]].trim())) 
			{
				ArrayList<String> lsGeneID = hashID2GeneID.get(geneID[i][colID[1]].trim());
				lsGeneID.add(KGprepare.removeDot(geneID[i][colID[0]].trim()));
			}
			else {
				ArrayList<String> lsGeneID = new ArrayList<String>();
				lsGeneID.add(KGprepare.removeDot(geneID[i][colID[0]].trim()));
				hashID2GeneID.put(geneID[i][colID[1]].trim(), lsGeneID);
			}
		}
		Enumeration<String> keys=hashID2GeneID.keys();

		while(keys.hasMoreElements()){
			String keyID=(String)keys.nextElement();
			ArrayList<String> lsGeneID = hashID2GeneID.get(keyID);
			String[] TmpgeneID = new String[lsGeneID.size()];
			for (int i = 0; i < TmpgeneID.length; i++) {
				TmpgeneID[i] = lsGeneID.get(i);
			}
			getElimFisher(TmpgeneID, GOClass,backGroundFile ,QtaxID, blast,StaxID,evalue,resultExcel2003+"_"+keyID+".xls", "");
		}
	}
	
	
	
	
	
	/**
	 * 用ElimFisher的方法进行GO分析
	 * @param geneFileXls
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * @param colID 选择差异基因中的哪两列。0：accID，1：foldChange
	 * <b>注意是实际列</b>
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @throws Exception
	 */
	public static void getGoRunElim(String geneFileXls,String GOClass,int[] colID, double up,double down,String backGroundFile,int QtaxID,
			boolean blast, int StaxID,double evalue, String resultExcel2003) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		int rowCount = excelGeneID.getRowCount();
		int colCount = excelGeneID.getColCount(2);
		String[][] geneID = excelGeneID.ReadExcel(2, 1,rowCount, colCount);
		ArrayList<String> lsGeneUp = new ArrayList<String>();
		ArrayList<String> lsGeneDown = new ArrayList<String>();
		for (int i = 0; i < geneID.length; i++) {
			if (Double.parseDouble(geneID[i][colID[1]])<=down) {
				lsGeneDown.add(KGprepare.removeDot(geneID[i][colID[0]]));
			}
			else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
				lsGeneUp.add(KGprepare.removeDot(geneID[i][colID[0]]));
			}
		}
		String[] geneDownID = new String[lsGeneDown.size()];
		for (int i = 0; i < geneDownID.length; i++) {
			geneDownID[i] = lsGeneDown.get(i);
		}
		
		String[] geneUpID = new String[lsGeneUp.size()];
		for (int i = 0; i < geneUpID.length; i++) {
			geneUpID[i] = lsGeneUp.get(i);
		}
		System.out.println("start elim detail");
		if (geneUpID.length>0) {
			getElimFisher(geneUpID, GOClass,backGroundFile ,QtaxID, blast,StaxID,evalue,resultExcel2003, "Up");
		}
		if (geneDownID.length>0) {
			getElimFisher(geneDownID, GOClass,backGroundFile ,QtaxID, blast,StaxID,evalue,resultExcel2003, "Down");
		}
	}

	/**
	 * 
	 * @param geneID string[] gene的ID
	 * @param GOClass
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix sheet的标题
	 * @throws Exception
	 */
	private static void getElimFisher(String[] geneID, String GOClass, String backGroundFile,int QtaxID,boolean blast, int StaxID,double evalue, String resultExcel2003, String prix) throws Exception
	{
		String resultGeneGofile = Rworkspace+"topGO/GeneGOInfo.txt";
		String resultBGGofile = Rworkspace+"topGO/BG2Go.txt";
		String resultGeneIDfile = Rworkspace+"topGO/GeneID.txt";
		//参数文本，第一个记录选择BP、MF、CC，第二个记录写文件，默认是GoResult.txt
		//第三个是数字，表示显示多少个GOTerm ,第四个记录GOInfo，就是每个GO对应的基因文件名
		String parameter = Rworkspace + "topGO/parameter.txt";
		///////////////以下为写入parameter的具体参数////////////////////////////////////////////////////////////////////
		int NumGOID = 300;
		//包含elimFisher的结果table文件
		String RGOresultTableFile = "GoResult.txt";
		//每个GO里面所含有的背景基因
		//格式为
		//#GO:010101
		//NM_0110101
		String RGoInfo = "GOInfo.txt";
		////////////////////////////////////////////////////////////////////////////////////////////
		//获得差异基因列表，geneInfo列表
		String[] strGeneID = null;
		ArrayList<String[]> lsGeneInfo = null;
		if (blast) {
			QBlastGO qBlastGO = new QBlastGO();
			Object[] obj = qBlastGO.goAnalysis(GOClass, geneID, backGroundFile, QtaxID, StaxID,evalue,resultGeneGofile ,resultBGGofile,resultGeneIDfile );
			strGeneID = (String[]) obj[0];
			lsGeneInfo = (ArrayList<String[]>) obj[1];
		}
		else {
			QGenID2GoInfoSepID qGenID2GoInfoSepID = new QGenID2GoInfoSepID();
			Object[] obj = qGenID2GoInfoSepID.goAnalysis(geneID, GOClass, backGroundFile, QtaxID, resultGeneGofile, resultBGGofile, resultGeneIDfile);
			strGeneID = (String[]) obj[0];
			lsGeneInfo = (ArrayList<String[]>) obj[1];
			int[] colNum2 = new int[1]; colNum2[0] = 1;
			lsGeneInfo = ArrayOperate.listCope(lsGeneInfo, colNum2, false);
		}
		//保存每个基因对应的具体信息，这样就不用去数据库搜索了
		Hashtable<String, String[]> hashGeneInfo = new Hashtable<String, String[]>();
		for (String[] string : lsGeneInfo) {
			hashGeneInfo.put(string[0], string);
		}
		
		
		
		TxtReadandWrite txtParam = new TxtReadandWrite();
		txtParam.setParameter(parameter, true, false);
		String content = "";
		if (GOClass.equals("P")) {
			content = "BP";
		}
		else if (GOClass.equals("F")) {
			content = "MF";
		}
		else if (GOClass.equals("C")) {
			content = "CC";
		}
		content =content + " "+RGOresultTableFile+" "+ NumGOID + " " + RGoInfo;
		txtParam.writefile(content);
		RElimFisher();
		//GOID对应GeneID的hash表
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( Rworkspace+"topGO/"+RGoInfo);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}
		ArrayList<String[]> lsResultTable = getElimFisherTable(Rworkspace+"topGO/"+RGOresultTableFile);
		//////////////////////将结果中的每一个GO都获得其相应的Gene并在arrayList中保存/////////////////////////////////////////////
		/**
		 * 0:GOID
		 * 1:GOTerm
		 * 2:AccID
		 * 3:Symbol
		 */
		ArrayList<String[]> lsGO2GeneInfo = new ArrayList<String[]>();
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			ArrayList<String> lsTmpGeneID = hashGO2Gene.get(lsResultTable.get(i)[0]);
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			for (String string : lsCoGeneID)
			{
				String[] strTmpGo2Gene = new String[4];//将GO2Gene装入结果文件。
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[1];//geneSymbol
				lsGO2GeneInfo.add(strTmpGo2Gene);
			}
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<String[]> lsGoResult = ArrayOperate.combArrayListHash(lsResultTable, lsGO2GeneInfo, 1, 1);
		
		int[] colNum = new int[6]; //除去title[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		colNum[0] = lsGO2GeneInfo.get(0).length+0;colNum[1] = lsGO2GeneInfo.get(0).length+1;colNum[2] = lsGO2GeneInfo.get(0).length+2;
		colNum[3] = lsGO2GeneInfo.get(0).length+3;colNum[4] = lsGO2GeneInfo.get(0).length+4;colNum[5] = lsGO2GeneInfo.get(0).length+5;
		
		lsGoResult = ArrayOperate.listCope(lsGoResult, colNum, false);
		final int colPvalue = lsGoResult.get(0).length-4;//pvalue那一列
		//排序
        Collections.sort(lsGoResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[colPvalue]); Double b=Double.parseDouble(arg1[colPvalue]);
                return a.compareTo(b);
            }
        });
		 
    	String[] title=new String[8];
		title[0]="GOID";title[1]="GOTerm";title[2]="AccessID";title[3]="GeneSymbol";
		title[4]="P-Value";title[5]="FDR";title[6]="Enrichment";title[7]="(-log2P)";
		lsGoResult.add(0,title);

		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel2003);
		excelResult.WriteExcel(prix+"GoAnalysis", 1, 1, lsResultTable, true);
		excelResult.WriteExcel(prix+"GO2Gene", 1, 1,lsGoResult , true);
		excelResult.WriteExcel(prix+"Gene2GO", 1, 1,lsGeneInfo , true);
	}
	
	
	
	private static void RElimFisher() throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+Rworkspace+ "topGO.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	/**
	 * 读取RGoInfo文件，将里面的GO2Gene的信息保存为ArrayList--ArrayList-String
	 * hash--GOID-lsGeneID
	 * @return
	 * @throws Exception 
	 */
	private static Hashtable<String,ArrayList<String>> getGo2GeneBG(String RGoInfo) throws Exception
	{
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite();
		txtRGo2Gene.setParameter(RGoInfo, false, true);
		BufferedReader reader = txtRGo2Gene.readfile();
		String content = "";
		Hashtable<String, ArrayList<String>> hashGo2Gene = new Hashtable<String, ArrayList<String>>();
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
	
	
	/**
	 * 读取RGOresultTableFile文件，包含标题列
	 * 产生ArrayList-string[]<br>
	 * 0: GOID<br>	
	 * 1: GOTerm<br>	
	 * 2:"Significant" <br>	
	 * 3:"allNumSig"	<br>	
	 * 4:"Annotated"	<br>	
	 * 5:"allNumBG"	<br>	
	 * 6:"pvalue"	<br>	
	 * 7:"fdr"	<br>	
	 * 8:"foldEnrichment"	<br>	
	 * 9:"logP"
	 * @return
	 * @throws Exception 
	 */
	private static ArrayList<String[]> getElimFisherTable(String RGOresultTableFile) throws Exception
	{
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite();
		txtRGo2Gene.setParameter(RGOresultTableFile, false, true);
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
	
	
	
	
	
	////////////////////////////////    其    明    的    算    法    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * 用其明的方法进行Cluster的GO分析，默认不合并相同ID
	 * @param geneFileXls
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param colID 选择差异基因中的哪两列。0：accID，1：ID
	 * 最后生成ID个excel表格，每个表格里面就是sheet1 GO分析 sheet2 GOInfo
	 * <b>注意是实际列</b>
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param taxIDfile  taxID文件  如果blast=F，那么第一行 queryTaxID。如果blast=T，那么第一行 queryTaxID，第二行 subjectTaxID，第三行 evalue
	 * @param resultExcel2003 不要加后缀xls，程序自动添加，在文件名和xls之间程序会添加上“_组ID”
	 * @param blast
	 * @param sepID
	 * @throws Exception
	 */
	public static void getGoRunQM(int QtaxID,String geneFileXls,String GOClass,int[] colID,String backGroundFile,
			boolean blast, int StaxID, double evaule, String resultExcel2003) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, excelGeneID.getRowCount(), excelGeneID.getColCount(2));
		//key：组ID，value：组内GeneList
		Hashtable<String, ArrayList<String>> hashID2GeneID = new Hashtable<String, ArrayList<String>>();
		ArrayList<String> lsID = new ArrayList<String>();
		
		for (int i = 0; i < geneID.length; i++) {
			//将分组装入hash表
			if (hashID2GeneID.containsKey(geneID[i][colID[1]].trim())) 
			{
				ArrayList<String> lsGeneID = hashID2GeneID.get(geneID[i][colID[1]].trim());
				lsGeneID.add(CopeID.removeDot(geneID[i][colID[0]].trim()));
			}
			else {
				ArrayList<String> lsGeneID = new ArrayList<String>();
				lsGeneID.add(CopeID.removeDot(geneID[i][colID[0]].trim()));
				hashID2GeneID.put(geneID[i][colID[1]].trim(), lsGeneID);
			}
		}
		Enumeration<String> keys=hashID2GeneID.keys();

		while(keys.hasMoreElements()){
			String keyID=(String)keys.nextElement();
			ArrayList<String> lsGeneID = hashID2GeneID.get(keyID);
			String[] TmpgeneID = new String[lsGeneID.size()];
			for (int i = 0; i < TmpgeneID.length; i++) {
				TmpgeneID[i] = lsGeneID.get(i);
			}
			getFisher( QtaxID, TmpgeneID, GOClass, backGroundFile, resultExcel2003+"_"+keyID+".xls", "", blast,StaxID,evaule, true);
		}
	}
	
	
	/**
	 * 用其明的方法进行GO分析
	 * @param geneFileXls
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param colID 选择差异基因中的哪两列。0：accID，1：foldChange
	 * <b>注意是实际列</b>
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param taxIDfile  taxID文件  如果blast=F，那么第一行 queryTaxID。如果blast=T，那么第一行 queryTaxID，第二行 subjectTaxID，第三行 evalue
	 * @param resultExcel2003
	 * @param blast
	 * @param sepID
	 * @throws Exception
	 */
	public static void getGoRunQM(int QtaxID, String geneFileXls,String GOClass,int[] colID, double up,double down,String backGroundFile,String resultExcel2003,boolean blast,int StaxID, double evalue,boolean sepID) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, excelGeneID.getRowCount(), excelGeneID.getColCount(2));
		ArrayList<String> lsGeneUp = new ArrayList<String>();
		ArrayList<String> lsGeneDown = new ArrayList<String>();
		for (int i = 0; i < geneID.length; i++) {
			if (Double.parseDouble(geneID[i][colID[1]])<=down) {
				lsGeneDown.add(CopeID.removeDot(geneID[i][colID[0]]));
			}
			else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
				lsGeneUp.add(CopeID.removeDot(geneID[i][colID[0]]));
			}
		}
		String[] geneDownID = new String[lsGeneDown.size()];
		for (int i = 0; i < geneDownID.length; i++) {
			geneDownID[i] = lsGeneDown.get(i);
		}
		
		String[] geneUpID = new String[lsGeneUp.size()];
		for (int i = 0; i < geneUpID.length; i++) {
			geneUpID[i] = lsGeneUp.get(i);
		}
		if (geneUpID.length>0) {
			getFisher(QtaxID,geneUpID, GOClass,backGroundFile, resultExcel2003, "Up", blast, StaxID,evalue,sepID);
		}
		if (geneDownID.length>0) {
			getFisher(QtaxID,geneDownID, GOClass, backGroundFile, resultExcel2003, "Down", blast,StaxID,evalue, sepID);
		}
	}

	/**
	 * @param geneID 基因的accID
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param backGroundFile 背景文件
	 * @param taxIDfile taxID文件  如果blast=F，那么第一行 queryTaxID。如果blast=T，那么第一行 queryTaxID，第二行 subjectTaxID，第三行 evalue
	 * @param resultGeneGofile 结果Gene2Go
	 * @param writeFile 返回Go分析的整理表
	 * @param sepID 是否将探针分割，true: 同样基因的探针不合并， false: 同样基因的探针合并
	 * @param blast :目前blast不支持 sepID=false的情况，当blast=T时，sepID无效
	 * @param evalue blast的evalue
	 * @param 
	 * @throws Exception 
	 */
	private static void getFisher(int QtaxID,String[] geneID,String GOClass,String backGroundFile,String resultExcel2003,String prix,boolean blast,int StaxID,double evalue, boolean sepID) throws Exception
	{
		ArrayList<String[]> lsGOinfo;
		ArrayList<String[]> lsGoResult;
		Object[] obj ;
		if (blast)
		{
			QBlastGO qBlastGO = new QBlastGO();
			obj = qBlastGO.goAnalysis(QtaxID,geneID, GOClass,backGroundFile,evalue,StaxID);
		}
		else
		{
			if (sepID) 
			{
				QGenID2GoInfoSepID qGenID2GoInfoSepID=new QGenID2GoInfoSepID();
				obj = qGenID2GoInfoSepID.goAnalysis(QtaxID,geneID,GOClass, backGroundFile);
			}
			else 
			{
				QGenID2GoInfo qGenID2GoInfo=new QGenID2GoInfo();
				obj = qGenID2GoInfo.goAnalysis(QtaxID,geneID, backGroundFile);
			}
		}
		lsGOinfo = (ArrayList<String[]>) obj[0];//这个用于fisher检验计算
		lsGoResult = (ArrayList<String[]>) obj[1];//这个就是所有结局
		TxtReadandWrite txtGoInfo=new TxtReadandWrite();
		txtGoInfo.setParameter(writeRFIle, true, false);
		int column[]=new int[4]; column[0]=2;column[1]=3;column[2]=4;column[3]=5;
		txtGoInfo.ExcelWrite(lsGOinfo, "\t", column, true, 1, 1);
		Rfisher();
		
		TxtReadandWrite txtRresult=new TxtReadandWrite();
		txtRresult.setParameter(Rresult, false, true);
		
		String[][] RFisherResult=txtRresult.ExcelRead("\t", 2, 2, txtRresult.ExcelRows(), txtRresult.ExcelColumns(2, "\t"));
		//保存最终的fisher结果
		/**
		 * GOID GOTerm difGene AllDifGene GeneInGoID AllGene // Pvalue FDR enrichment logP 
		 * 没加标题
		 */
		ArrayList<String[]> lsFisherResult=new ArrayList<String[]>();
	
		//把从R读取的pvalue等合并上去
		for (int i = 0; i < lsGOinfo.size(); i++) {
			String[] tmp = lsGOinfo.get(i);
			String[] tmp2=new String[tmp.length+RFisherResult[i].length-4];
			for (int j = 0; j < tmp2.length; j++) {
				if( j<tmp.length)
				{
					tmp2[j]=tmp[j];
				}
				else 
				{
					tmp2[j]=RFisherResult[i][j-tmp.length+4];
				}
			}
			lsFisherResult.add(tmp2);
		}
		//排序
        Collections.sort(lsFisherResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[6]); Double b=Double.parseDouble(arg1[6]);
                return a.compareTo(b);
            }
        });
		ArrayList<String[]> lsGOInfoResult = ArrayOperate.combArrayListHash(lsFisherResult, lsGoResult, 0, 3);
		
		int[] colNum = new int[6]; //除去title[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		colNum[0] = lsGoResult.get(0).length+0;colNum[1] = lsGoResult.get(0).length+1;colNum[2] = lsGoResult.get(0).length+2;
		colNum[3] = lsGoResult.get(0).length+3;colNum[4] = lsGoResult.get(0).length+4;colNum[5] = lsGoResult.get(0).length+5;
		
		lsGOInfoResult = ArrayOperate.listCope(lsGOInfoResult, colNum, false);
		final int colPvalue = lsGoResult.get(0).length+1;
		//排序
        Collections.sort(lsGOInfoResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[colPvalue]); Double b=Double.parseDouble(arg1[colPvalue]);
                return a.compareTo(b);
            }
        });
		 
    	String[] title=new String[10];
		title[0]="GOID";title[1]="GOTerm";title[2]="DifGene";title[3]="AllDifGene";title[4]="GeneInGOID";
		title[5]="AllGene";title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)";
		lsFisherResult.add(0,title);
		
		
		
		if (blast)
		{
			String[] title2=new String[10];
			title2[0]="AccessID";title2[1]="GeneSymbol";title2[2]="Blast2Symbol";title2[3]="GOID";title2[4]="GOTerm";
			title2[5]="Evidence";title2[6]="P-Value";title2[7]="FDR";title2[8]="Enrichment";title2[9]="(-log2P)";
			lsGOInfoResult.add(0,title2);
		}
		else
		{
			String[] title2=new String[9];
			title2[0]="AccessID";title2[1]="GeneID";title2[2]="GeneSymbol";title2[3]="GOID";title2[4]="GOTerm";
			title2[5]="P-Value";title2[6]="FDR";title2[7]="Enrichment";title2[8]="(-log2P)";
			lsGOInfoResult.add(0,title2); 
			int[] colNum2 = new int[1]; colNum2[0] = 1;
			lsGOInfoResult = ArrayOperate.listCope(lsGOInfoResult, colNum2, false);
		}
		ExcelOperate excelGO = new ExcelOperate();
		excelGO.openExcel(resultExcel2003);
		excelGO.WriteExcel(prix+"GoAnalysis", 1, 1, lsFisherResult, true);
		excelGO.WriteExcel(prix+"Gene2Go", 1, 1, lsGOInfoResult, true);
	}
	
	
	private static void Rfisher() throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+Rworkspace+ "GOfisherBHfdr.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
}
