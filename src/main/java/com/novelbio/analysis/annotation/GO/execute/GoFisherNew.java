package com.novelbio.analysis.annotation.GO.execute;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import com.novelbio.analysis.annotation.GO.queryDB.QgeneID2Go;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.copeID.FisherTest;
import com.novelbio.analysis.annotation.copeID.ItemInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.service.ServGo;



/**
 * 将QGenID2GoInfo获得的信息转变为让R识别的格式，最后调用R程序完成计算
 * @author zong0jie
 *
 */
public class GoFisherNew {
	
//	static String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform/";
	
	/**
	 * 需要和R脚本中的路径相统一
	 */
//	static String writeRFIle="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";
	
	/**
	 * 需要和R脚本中的路径相统一
	 */
//	static String Rresult="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";

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
	 * @param resultExcel2003 不要加后缀els，程序自动添加，在文件名和xls之间程序会添加上“_组ID”
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
//			getElimFisher(TmpgeneID, GOClass,backGroundFile ,QtaxID, blast,StaxID,evalue,resultExcel2003+"_"+keyID+".xls", "");
		}
	}
	
	
	
	
	
	/**
	 * 	 * 用ElimFisher的方法进行GO分析
	 * @param geneFileXls
	 * @param sepID
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * 	 * <b>注意是实际列</b>
	 * @param colID 选择差异基因中的哪两列。0：accID，1：foldChange。如果col[0] == col[1] 那么说明不考虑差异列
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix string[2] 在excel中的前缀分别是什么，第一个是up的，第二个是down的
	 * @param 每个分析最多显示多少个GOID
	 * @throws Exception
	 */
	public static void getGoRunElim(String geneFileXls,boolean sepID,String GOClass,int[] colID, double up,double down,String backGroundFile,int QtaxID,
			boolean blast, int StaxID,double evalue, String resultExcel2003,String resultPicName,String[] prix,int NumGo) throws Exception
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
			if (colID[1] == colID[0]) {
				lsGeneUp.add(geneID[i][colID[0]]);
			}
			else if (Double.parseDouble(geneID[i][colID[1]])<=down) {
				lsGeneDown.add(geneID[i][colID[0]]);
			}
			else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
				lsGeneUp.add(geneID[i][colID[0]]);
			}
			
		}
		
		ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(backGroundFile, 1, "\t");
		ArrayList<String> lsBGID = new ArrayList<String>();
		for (String[] strings : lsBGIDAll) {
			lsBGID.add(strings[0]);
		}
		ArrayList<String[]> lsGeneUpCope = CopeID.getGenID(prix[0],lsGeneUp, QtaxID,sepID);
		ArrayList<String[]> lsGeneDownCope = CopeID.getGenID(prix[1],lsGeneDown, QtaxID,sepID);
		ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG",lsBGID, QtaxID,sepID);
		
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel2003);
		
		if (lsGeneUpCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult = getElimFisher(prix[0],lsGeneUpCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue,NumGo);
			excelResult.WriteExcel(prix[0]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResult.get(1) , true);
			excelResult.WriteExcel(prix[0]+"Gene2GO", 1, 1,lsResult.get(2) , true);

			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
					FileOperate.getParentName(resultPicName), FileOperate.getName(resultPicName)+prix[0]+".pdf",true);
			
		}
		if (lsGeneDownCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult =getElimFisher(prix[1],lsGeneDownCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue,NumGo);
			excelResult.WriteExcel(prix[1]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[1]+"GO2Gene", 1, 1,lsResult.get(1) , true);
			excelResult.WriteExcel(prix[1]+"Gene2GO", 1, 1,lsResult.get(2) , true);
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
					FileOperate.getParentName(resultPicName), FileOperate.getName(resultPicName)+prix[1]+".pdf",true);
		}
	}

	/**
	 * @param condition 是分析哪个时期的信息 譬如上调，下调 或 背景，要和前面对应
	 * @param lsAccID 经过整理的accID<br>
	 * * arraylist-string[3]<br>
0: ID类型："geneID"或"uniID"或"accID"<br>
1: accID<br>
2: 具体转换的ID
	 * @param lsBGAccID 经过整理的BG的accID<br>
	 * * arraylist-string[3]<br>
0: ID类型："geneID"或"uniID"或"accID"<br>
1: accID<br>
2: 具体转换的ID
	 * @param GOClass Go的类型 P: biological Process F:molecular Function C: cellular Component 如果GOClass为""那么背景产生全部，但是分析时按照BP进行，不过结果会有问题。
	 * 所以""仅仅是为了产生背景而使用
	 * @param sepID 
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param NumGOID 最后显示多少个GO
	 * @throws Exception
	 * @return 三个list
	 * 第一个 lsResultTable,Go富集分析的结果表格<br>
	 * 0: GOID<br>
1: GOTerm<br>
2:"Significant"<br>
3:"allNumSig"<br>
4:"Annotated"<br>
5:"allNumBG"<br>
6:"pvalue"<br>
7:"fdr"<br>
8:"foldEnrichment"<br>
9:"logP"<br>
	 * 第二个lsGoResultFinal Go富集分析的Go2gene表格<br>
	 * 	title[0]="GOID";title[1]="GOTerm";title[2]="AccessID";title[3]="GeneSymbol";<br>
		title[4]="P-Value";title[5]="FDR";title[6]="Enrichment";title[7]="(-log2P)<br>
	 * 第三个lsGeneInfoFinal Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence";title2[6]="Evalue";title2[7]="subjectSymbol";<br>
			title2[8]="Description";title2[9]="GOID";;title2[10]="GOTerm";title2[11]="Evide<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidenc<br>
	 */
	private static ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<String[]>  lsBGAccID,String GOClass, boolean sepID,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
	{
		//获得差异基因列表，geneInfo列表
		String[] strGeneID = null;
		ArrayList<ArrayList<String[]>> lsGenGoInfo = QgeneID2Go.getGenGoInfo(lsAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsGeneInfo = lsGenGoInfo.get(0); //gene总体信息
		ArrayList<String[]> lsGene2Go = lsGenGoInfo.get(1);//gene go,go,go信息
		ArrayList<String[]> lsGo2Gene = null;//Go2Gene的信息，仅在blast时才有用
		strGeneID = new String[lsGene2Go.size()];//用于elim检验
		for (int i = 0; i < strGeneID.length; i++) {
			strGeneID[i] = lsGene2Go.get(i)[0];
		}
		///////////////保存每个基因对应的具体信息，这样就不用去数据库搜索了/////////////
		Hashtable<String, String[]> hashGeneInfo = new Hashtable<String, String[]>();
		if (sepID) {
			for (String[] string : lsGeneInfo) {
				hashGeneInfo.put(string[0], string);
			}
		}
		else {
			for (String[] string : lsGeneInfo) {
				hashGeneInfo.put(string[1], string);
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////////////
		if (blast) {
			lsGo2Gene = lsGenGoInfo.get(2); //这个只有在NBCfisher中才会使用
		}
		
		ArrayList<ArrayList<String[]>> lsBGGenGoInfo = QgeneID2Go.getGenGoInfo(lsBGAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsBGGene2Go = lsBGGenGoInfo.get(1);//gene go,go,go信息
		
		
		/////////////////////////topGo的参数///////////////////////
		TxtReadandWrite txtParam = new TxtReadandWrite();
		txtParam.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_PARAM, true, false);
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
		else if (GOClass.equals("")) {
			content = "BP";
		}
		content =content + " "+NovelBioConst.R_WORKSPACE_TOPGO_GORESULT+" "+ NumGOID + " " + NovelBioConst.R_WORKSPACE_TOPGO_GOINFO;
		txtParam.writefile(content);
		txtParam.close();
		/////////////////////////TopGo的BG///////////////////////
		TxtReadandWrite txtTopGoBG = new TxtReadandWrite();
		txtTopGoBG.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_BGGeneGo, true, false);
		txtTopGoBG.ExcelWrite(lsBGGene2Go, "\t", 1, 1);

		txtTopGoBG.close();
		///////////////TopGo的待分析geneID/////////////////////////////////////////////
		TxtReadandWrite txtGenID= new TxtReadandWrite();
		txtGenID.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_GENEID, true, false);
		txtGenID.Rwritefile(strGeneID);
		txtTopGoBG.close();
		//////////////////////////////////////////////////////////////////////
		RElimFisher();
		//GOID对应GeneID的hash表
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( NovelBioConst.R_WORKSPACE_TOPGO_GOINFO);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}
		ArrayList<String[]> lsResultTable = getElimFisherTable(NovelBioConst.R_WORKSPACE_TOPGO_GORESULT);
		//////////////////////将结果中的每一个GO都获得其相应的Gene并在arrayList中保存/////////////////////////////////////////////
		/**
		 * 0:GOID
		 * 1:GOTerm
		 * 2:AccID
		 * 3:Symbol/accID
		 */
		ArrayList<String[]> lsGO2GeneInfo = new ArrayList<String[]>();
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			//某个GO中所含有的所有背景基因
			ArrayList<String> lsTmpGeneID = hashGO2Gene.get(lsResultTable.get(i)[0]);
			//获得某个GO中所含有的所有差异基因
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			for (String string : lsCoGeneID)
			{
				String[] strTmpGo2Gene = new String[4];//将GO2Gene装入结果文件。
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[2];//geneSymbol
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
            	Double a=Double.parseDouble(arg0[colPvalue].replace("<", "")); Double b=Double.parseDouble(arg1[colPvalue].replace("<", ""));
                return a.compareTo(b);
            }
        });
        
        ArrayList<String[]> lsGoResultFinal = CopeID.copeCombineID(condition,lsGoResult, 2, 2, sepID);
        ArrayList<String[]> lsGeneInfoFinal = CopeID.copeCombineID(condition,lsGeneInfo, 1, 0, sepID);
        
		 ////////////////////////////////////////加标题////////////////////////////////////////////////////
    	String[] title=new String[8];
		title[0]="GOID";title[1]="GOTerm";title[2]="AccessID";title[3]="GeneSymbol";
		title[4]="P-Value";title[5]="FDR";title[6]="Enrichment";title[7]="(-log2P)";
		lsGoResultFinal.add(0,title);
		/////////////////////////////////////////////////////////////////////////////////////
		if (blast) {
			int[] colDel = new int[3]; 
			colDel[0] = 1; colDel[1] = 8; colDel[2] = 9; 
			lsGeneInfoFinal = ArrayOperate.listCope(lsGeneInfoFinal, colDel, false);
		        
			String[] title2=new String[12];
			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";
			title2[4]="GOTerm";title2[5]="Evidence";title2[6]="Evalue";title2[7]="subjectSymbol";
			title2[8]="Description";title2[9]="GOID";title2[10]="GOTerm";title2[11]="Evidence";
			lsGeneInfoFinal.add(0,title2);
		}
		else {
			int[] colDel = new int[1]; 
			colDel[0] = 1;
			lsGeneInfoFinal = ArrayOperate.listCope(lsGeneInfoFinal, colDel, false);
			
			String[] title2=new String[12];
			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";
			title2[4]="GOTerm";title2[5]="Evidence";
			lsGeneInfoFinal.add(0,title2);
		}
		////////////////////////////////////////////////////////////////////////////////////
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		lsResult.add(lsResultTable);
		lsResult.add(lsGoResultFinal);
		lsResult.add(lsGeneInfoFinal);
		return lsResult;
	}
	
	
	
	private static void RElimFisher() throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+NovelBioConst.R_WORKSPACE_TOPGO_RSCRIPT;
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
//			getFisher( QtaxID, TmpgeneID, GOClass, backGroundFile, resultExcel2003+"_"+keyID+".xls", "", blast,StaxID,evaule, true);
		}
	}

	/**
	 * 	 * 用ElimFisher的方法进行GO分析
	 * @param geneFileXls
	 * @param sepID
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * 	 * <b>注意是实际列</b>
	 * @param colID 选择差异基因中的哪两列。0：accID，1：foldChange
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix string[2] 在excel中的前缀分别是什么，第一个是up的，第二个是down的
	 * @throws Exception
	 */
	public static void getGoRunNBC(String geneFileXls,boolean sepID,String GOClass,int[] colID, double up,double down,String backGroundFile,int QtaxID,
			boolean blast, int StaxID,double evalue, String resultExcel2003,String[] prix) throws Exception
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
				lsGeneDown.add(geneID[i][colID[0]]);
			}
			else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
				lsGeneUp.add(geneID[i][colID[0]]);
			}
		}
		
		ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(backGroundFile, 1, "\t");
		ArrayList<String> lsBGID = new ArrayList<String>();
		for (String[] strings : lsBGIDAll) {
			lsBGID.add(strings[0]);
		}
		ArrayList<String[]> lsGeneUpCope = CopeID.getGenID(prix[0],lsGeneUp, QtaxID,sepID);
		ArrayList<String[]> lsGeneDownCope = CopeID.getGenID(prix[1],lsGeneDown, QtaxID,sepID);
		ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG",lsBGID, QtaxID,sepID);
		
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel2003);
		
		if (lsGeneUpCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult = getNBCFisher(prix[0],lsGeneUpCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue);
			excelResult.WriteExcel(prix[0]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[0]+"Gene2GO", 1, 1,lsResult.get(1) , true);
			if (blast) {
				excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResult.get(2) , true);
			}
		}
		if (lsGeneDownCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult =getNBCFisher(prix[1],lsGeneDownCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue);
			excelResult.WriteExcel(prix[1]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[1]+"Gene2GO", 1, 1,lsResult.get(1) , true);
			if (blast) {
				excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResult.get(2) , true);
			}
		}
	}
	
	/**
	 * @param condition 是分析哪个时期的信息 譬如上调，下调 或 背景，要和前面对应
	 * @param lsAccID 经过整理的accID<br>
	 * * arraylist-string[3]<br>
0: ID类型："geneID"或"uniID"或"accID"<br>
1: accID<br>
2: 具体转换的ID
	 * @param lsBGAccID 经过整理的BG的accID<br>
	 * * arraylist-string[3]<br>
0: ID类型："geneID"或"uniID"或"accID"<br>
1: accID<br>
2: 具体转换的ID
	 * @param GOClass Go的类型 P: biological Process F:molecular Function C: cellular Component 如果GOClass为""那么背景产生全部，但是分析时按照BP进行，不过结果会有问题。
	 * 所以""仅仅是为了产生背景而使用
	 * @param sepID 
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @throws Exception
	 * @return 三个list
	 * 第一个 lsResultTable,Go富集分析的结果表格<br>
	 * 0: GOID<br>
1: GOTerm<br>
2:"Significant"<br>
3:"allNumSig"<br>
4:"Annotated"<br>
5:"allNumBG"<br>
6:"pvalue"<br>
7:"fdr"<br>
8:"foldEnrichment"<br>
9:"logP"<br>

	 * 第二个lsGeneInfoFinal Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence";title2[6]="Evalue";title2[7]="subjectSymbol";<br>
			title2[8]="Description";title2[9]="GOID";;title2[10]="GOTerm";title2[11]="Evidence"<br>
			title[12]="P-Value";title[13]="FDR";title[14]="Enrichment";title[15]="(-log2P)<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence"<br>
			title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
		<b>如果blast，才会有第三个</b>
		Go2gene表格<br>
	 * 	title[0]="GOID";title[1]="GOTerm";title[2]="evidence";title[3]="queryID";title[4]="querySymbol";
	 * title[5]="subjectSymbol“<br>
		title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
	 */
	private static ArrayList<ArrayList<String[]>> getNBCFisher(String condition,ArrayList<String[]> lsAccID,ArrayList<String[]> lsBGAccID,String GOClass,boolean sepID,int QtaxID,
			boolean blast,int StaxID,double evalue) throws Exception
	{
		///////////////////  获得数据  ////////////////////////////////////////////////////////////
		ArrayList<ArrayList<String[]>> lsGoInfo = QgeneID2Go.getGenGoInfo(lsAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<ArrayList<String[]>> lsGoInfoBG = QgeneID2Go.getGenGoInfo(lsBGAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsGeneInfo = lsGoInfo.get(0);
		ArrayList<String[]> lsGene2Go =  lsGoInfo.get(1);
		ArrayList<String[]> lsGo2Gen = null;
		if (blast) {
			lsGo2Gen = lsGoInfo.get(2);
		}
		
		ArrayList<String[]> lsGene2GoBG = lsGoInfoBG.get(1);
		ArrayList<String[]> lsFisherResult = FisherTest.getFisherResult(lsGene2Go, lsGene2GoBG, new ItemInfo() {
			public String[] getItemName(String ItemID) {
				HashMap<String, String[]> hashGo2Term = ServGo.getHashGo2Term();
				String[] goTerm = new String[1];
				goTerm[0] = hashGo2Term.get(ItemID)[2];
				return goTerm;
			}
		});

		
	    ////////////////////////////////////////   加标题   //////////   FisherResult   //////////////////////////////////////////
		String[] title = new String[10];
		title[0] = "GOID"; title[1] = "GOTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInGOID"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		lsFisherResult.add(0,title);
		/////////////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////   处理 以及 加标题   //////////   gene2GOInfo   //////////////////////////////////////////

	    //加上accID
		lsGeneInfo = CopeID.copeCombineID(condition,lsGeneInfo, 1, 0, sepID);
					//////////////////////////////  Gene2Go是否需要加上pvalue  ///////////////////////////////////
		ArrayList<String[]> lsGoInfoPvalue = ArrayOperate.combArrayListHash(lsFisherResult, lsGeneInfo, 0, 4);
		final int colpValue=lsGeneInfo.get(0).length+6;
		//排序
	    Collections.sort(lsGoInfoPvalue,new Comparator<String[]>(){
	        public int compare(String[] arg0, String[] arg1) {
	        	Double a=Double.parseDouble(arg0[colpValue]); Double b=Double.parseDouble(arg1[colpValue]);
	            return a.compareTo(b);
	        }
	    });
	    			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    String[] titleGen2GoInfo = null;
	    if (blast) {
	    	titleGen2GoInfo = new String[16];
	    	int[] colNum = new int[9]; //除去title[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		    colNum[0] = 1; colNum[1] = 8; colNum[2] = 9; 
		    colNum[3] = lsGeneInfo.get(0).length+0;colNum[4] = lsGeneInfo.get(0).length+1;colNum[5] = lsGeneInfo.get(0).length+2;
			colNum[6] = lsGeneInfo.get(0).length+3;colNum[7] = lsGeneInfo.get(0).length+4;colNum[8] = lsGeneInfo.get(0).length+5;
			lsGoInfoPvalue = ArrayOperate.listCope(lsGoInfoPvalue, colNum, false);
			
			titleGen2GoInfo[0] = "QueryID"; titleGen2GoInfo[1] = "QuerySymbol"; titleGen2GoInfo[2] = "Description"; titleGen2GoInfo[3] = "GOID"; titleGen2GoInfo[4] = "GOTerm";
			titleGen2GoInfo[5] = "Evidence"; 
			titleGen2GoInfo[6] = "Evalue"; titleGen2GoInfo[7] = "SubjectSymbol"; titleGen2GoInfo[8] = "Description";titleGen2GoInfo[9] = "GOID";
			titleGen2GoInfo[10] = "GOTerm";titleGen2GoInfo[11] = "Evidence";
						//////////////////////////////Gene2Go是否需要加上pvalue  ///////////////////////////////////
			titleGen2GoInfo[12] = "P-Value"; titleGen2GoInfo[13] = "FDR"; titleGen2GoInfo[14] = "Enrichment"; titleGen2GoInfo[15] = "(-log2P)";
		}
	    else {
	    	titleGen2GoInfo = new String[10];
	    	int[] colNum = new int[7]; //除去title[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		    colNum[0] = 1;
		    colNum[1] = lsGeneInfo.get(0).length+0;colNum[2] = lsGeneInfo.get(0).length+1;colNum[3] = lsGeneInfo.get(0).length+2;
			colNum[4] = lsGeneInfo.get(0).length+3;colNum[5] = lsGeneInfo.get(0).length+4;colNum[6] = lsGeneInfo.get(0).length+5;
			lsGoInfoPvalue = ArrayOperate.listCope(lsGoInfoPvalue, colNum, false);
			
			titleGen2GoInfo[0] = "QueryID"; titleGen2GoInfo[1] = "Symbol"; titleGen2GoInfo[2] = "Description"; titleGen2GoInfo[3] = "GOID"; titleGen2GoInfo[4] = "GOTerm";
			titleGen2GoInfo[5] = "Evidence";
			titleGen2GoInfo[6] = "P-Value"; titleGen2GoInfo[7] = "FDR"; titleGen2GoInfo[8] = "Enrichment"; titleGen2GoInfo[9] = "(-log2P)";
		}
	    lsGoInfoPvalue.add(0,titleGen2GoInfo);
	    
	    ////////////////////////////////////////   处理 以及 加标题   //////////   GO2Gene   //////////////////////////////////////////  
	    ArrayList<String[]> lsGo2GenFinal = null;
		if (blast) {
			lsGo2Gen =  CopeID.copeCombineID(condition,lsGo2Gen, 4, 3, sepID);
			
			lsGo2GenFinal = ArrayOperate.combArrayListHash(lsFisherResult, lsGo2Gen, 0, 0);
			int[] colNum = new int[7]; //除去title[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		    colNum[0] = 4;
		    colNum[1] = lsGo2Gen.get(0).length+0;colNum[2] = lsGo2Gen.get(0).length+1;colNum[3] = lsGo2Gen.get(0).length+2;
			colNum[4] = lsGo2Gen.get(0).length+3;colNum[5] = lsGo2Gen.get(0).length+4;colNum[6] = lsGo2Gen.get(0).length+5;
			lsGo2GenFinal = ArrayOperate.listCope(lsGo2GenFinal, colNum, false);
			
			String[] titleGo2Gen = new String[10];
			titleGo2Gen[0] = "GOID"; titleGo2Gen[1] = "GOTerm"; titleGo2Gen[2] = "Evidence"; titleGo2Gen[3] = "QueryID"; titleGo2Gen[4] = "QuerySymbol";
			titleGo2Gen[5] = "SubjectSymbol";
			titleGo2Gen[6] = "P-Value"; titleGo2Gen[7] = "FDR"; titleGo2Gen[8] = "Enrichment"; titleGo2Gen[9] = "(-log2P)";
			lsGo2GenFinal.add(0,titleGo2Gen);
		}
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		lsResult.add(lsFisherResult);lsResult.add(lsGoInfoPvalue);
		if (blast) {
			lsResult.add(lsGo2GenFinal);
		}
		return lsResult;
	}
	
	
}
