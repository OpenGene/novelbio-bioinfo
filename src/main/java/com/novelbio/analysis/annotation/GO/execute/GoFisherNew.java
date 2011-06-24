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
 * ��QGenID2GoInfo��õ���Ϣת��Ϊ��Rʶ��ĸ�ʽ��������R������ɼ���
 * @author zong0jie
 *
 */
public class GoFisherNew {
	
//	static String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform/";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
//	static String writeRFIle="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
//	static String Rresult="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";

	/**
	 * ��ElimFisher�ķ�������Cluster��GO������
	 * @param geneFileXls
	 * @param GOClass   P: biological Process F:molecular Function C: cellular Component
	 * @param colID ѡ���������е������С�0��accID��1��ID
	 * �������ID��excel���ÿ������������sheet1 GO���� sheet2 GOInfo
	 * <b>ע����ʵ����</b>
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003 ��Ҫ�Ӻ�׺els�������Զ���ӣ����ļ�����xls֮����������ϡ�_��ID��
	 * @throws Exception
	 */
	public static void getGoRunElim(String geneFileXls,String GOClass,int[] colID,String backGroundFile,int QtaxID,
			boolean blast, int StaxID,double evalue, String resultExcel2003) throws Exception
	{
		colID[0]--;colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, excelGeneID.getRowCount(), excelGeneID.getColCount(2));
		//key����ID��value������GeneList
		Hashtable<String, ArrayList<String>> hashID2GeneID = new Hashtable<String, ArrayList<String>>();
		
		for (int i = 0; i < geneID.length; i++) {
			//������װ��hash��
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
	 * 	 * ��ElimFisher�ķ�������GO����
	 * @param geneFileXls
	 * @param sepID
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * 	 * <b>ע����ʵ����</b>
	 * @param colID ѡ���������е������С�0��accID��1��foldChange�����col[0] == col[1] ��ô˵�������ǲ�����
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix string[2] ��excel�е�ǰ׺�ֱ���ʲô����һ����up�ģ��ڶ�����down��
	 * @param ÿ�����������ʾ���ٸ�GOID
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
	 * @param condition �Ƿ����ĸ�ʱ�ڵ���Ϣ Ʃ���ϵ����µ� �� ������Ҫ��ǰ���Ӧ
	 * @param lsAccID ���������accID<br>
	 * * arraylist-string[3]<br>
0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
1: accID<br>
2: ����ת����ID
	 * @param lsBGAccID ���������BG��accID<br>
	 * * arraylist-string[3]<br>
0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
1: accID<br>
2: ����ת����ID
	 * @param GOClass Go������ P: biological Process F:molecular Function C: cellular Component ���GOClassΪ""��ô��������ȫ�������Ƿ���ʱ����BP���У���������������⡣
	 * ����""������Ϊ�˲���������ʹ��
	 * @param sepID 
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param NumGOID �����ʾ���ٸ�GO
	 * @throws Exception
	 * @return ����list
	 * ��һ�� lsResultTable,Go���������Ľ�����<br>
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
	 * �ڶ���lsGoResultFinal Go����������Go2gene���<br>
	 * 	title[0]="GOID";title[1]="GOTerm";title[2]="AccessID";title[3]="GeneSymbol";<br>
		title[4]="P-Value";title[5]="FDR";title[6]="Enrichment";title[7]="(-log2P)<br>
	 * ������lsGeneInfoFinal Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence";title2[6]="Evalue";title2[7]="subjectSymbol";<br>
			title2[8]="Description";title2[9]="GOID";;title2[10]="GOTerm";title2[11]="Evide<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidenc<br>
	 */
	private static ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<String[]>  lsBGAccID,String GOClass, boolean sepID,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
	{
		//��ò�������б�geneInfo�б�
		String[] strGeneID = null;
		ArrayList<ArrayList<String[]>> lsGenGoInfo = QgeneID2Go.getGenGoInfo(lsAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsGeneInfo = lsGenGoInfo.get(0); //gene������Ϣ
		ArrayList<String[]> lsGene2Go = lsGenGoInfo.get(1);//gene go,go,go��Ϣ
		ArrayList<String[]> lsGo2Gene = null;//Go2Gene����Ϣ������blastʱ������
		strGeneID = new String[lsGene2Go.size()];//����elim����
		for (int i = 0; i < strGeneID.length; i++) {
			strGeneID[i] = lsGene2Go.get(i)[0];
		}
		///////////////����ÿ�������Ӧ�ľ�����Ϣ�������Ͳ���ȥ���ݿ�������/////////////
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
			lsGo2Gene = lsGenGoInfo.get(2); //���ֻ����NBCfisher�вŻ�ʹ��
		}
		
		ArrayList<ArrayList<String[]>> lsBGGenGoInfo = QgeneID2Go.getGenGoInfo(lsBGAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsBGGene2Go = lsBGGenGoInfo.get(1);//gene go,go,go��Ϣ
		
		
		/////////////////////////topGo�Ĳ���///////////////////////
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
		/////////////////////////TopGo��BG///////////////////////
		TxtReadandWrite txtTopGoBG = new TxtReadandWrite();
		txtTopGoBG.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_BGGeneGo, true, false);
		txtTopGoBG.ExcelWrite(lsBGGene2Go, "\t", 1, 1);

		txtTopGoBG.close();
		///////////////TopGo�Ĵ�����geneID/////////////////////////////////////////////
		TxtReadandWrite txtGenID= new TxtReadandWrite();
		txtGenID.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_GENEID, true, false);
		txtGenID.Rwritefile(strGeneID);
		txtTopGoBG.close();
		//////////////////////////////////////////////////////////////////////
		RElimFisher();
		//GOID��ӦGeneID��hash��
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( NovelBioConst.R_WORKSPACE_TOPGO_GOINFO);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}
		ArrayList<String[]> lsResultTable = getElimFisherTable(NovelBioConst.R_WORKSPACE_TOPGO_GORESULT);
		//////////////////////������е�ÿһ��GO���������Ӧ��Gene����arrayList�б���/////////////////////////////////////////////
		/**
		 * 0:GOID
		 * 1:GOTerm
		 * 2:AccID
		 * 3:Symbol/accID
		 */
		ArrayList<String[]> lsGO2GeneInfo = new ArrayList<String[]>();
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			//ĳ��GO�������е����б�������
			ArrayList<String> lsTmpGeneID = hashGO2Gene.get(lsResultTable.get(i)[0]);
			//���ĳ��GO�������е����в������
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			for (String string : lsCoGeneID)
			{
				String[] strTmpGo2Gene = new String[4];//��GO2Geneװ�����ļ���
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[2];//geneSymbol
				lsGO2GeneInfo.add(strTmpGo2Gene);
			}
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<String[]> lsGoResult = ArrayOperate.combArrayListHash(lsResultTable, lsGO2GeneInfo, 1, 1);
		
		int[] colNum = new int[6]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		colNum[0] = lsGO2GeneInfo.get(0).length+0;colNum[1] = lsGO2GeneInfo.get(0).length+1;colNum[2] = lsGO2GeneInfo.get(0).length+2;
		colNum[3] = lsGO2GeneInfo.get(0).length+3;colNum[4] = lsGO2GeneInfo.get(0).length+4;colNum[5] = lsGO2GeneInfo.get(0).length+5;
		
		lsGoResult = ArrayOperate.listCope(lsGoResult, colNum, false);
		final int colPvalue = lsGoResult.get(0).length-4;//pvalue��һ��
		//����
        Collections.sort(lsGoResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[colPvalue].replace("<", "")); Double b=Double.parseDouble(arg1[colPvalue].replace("<", ""));
                return a.compareTo(b);
            }
        });
        
        ArrayList<String[]> lsGoResultFinal = CopeID.copeCombineID(condition,lsGoResult, 2, 2, sepID);
        ArrayList<String[]> lsGeneInfoFinal = CopeID.copeCombineID(condition,lsGeneInfo, 1, 0, sepID);
        
		 ////////////////////////////////////////�ӱ���////////////////////////////////////////////////////
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
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+NovelBioConst.R_WORKSPACE_TOPGO_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	/**
	 * ��ȡRGoInfo�ļ����������GO2Gene����Ϣ����ΪArrayList--ArrayList-String
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
	 * ��ȡRGOresultTableFile�ļ�������������
	 * ����ArrayList-string[]<br>
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
		//ȥ��"��
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
	
	
	
	
	
	////////////////////////////////    ��    ��    ��    ��    ��    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * �������ķ�������Cluster��GO������Ĭ�ϲ��ϲ���ͬID
	 * @param geneFileXls
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param colID ѡ���������е������С�0��accID��1��ID
	 * �������ID��excel���ÿ������������sheet1 GO���� sheet2 GOInfo
	 * <b>ע����ʵ����</b>
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param taxIDfile  taxID�ļ�  ���blast=F����ô��һ�� queryTaxID�����blast=T����ô��һ�� queryTaxID���ڶ��� subjectTaxID�������� evalue
	 * @param resultExcel2003 ��Ҫ�Ӻ�׺xls�������Զ���ӣ����ļ�����xls֮����������ϡ�_��ID��
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
		//key����ID��value������GeneList
		Hashtable<String, ArrayList<String>> hashID2GeneID = new Hashtable<String, ArrayList<String>>();
		ArrayList<String> lsID = new ArrayList<String>();
		
		for (int i = 0; i < geneID.length; i++) {
			//������װ��hash��
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
	 * 	 * ��ElimFisher�ķ�������GO����
	 * @param geneFileXls
	 * @param sepID
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * 	 * <b>ע����ʵ����</b>
	 * @param colID ѡ���������е������С�0��accID��1��foldChange
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix string[2] ��excel�е�ǰ׺�ֱ���ʲô����һ����up�ģ��ڶ�����down��
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
	 * @param condition �Ƿ����ĸ�ʱ�ڵ���Ϣ Ʃ���ϵ����µ� �� ������Ҫ��ǰ���Ӧ
	 * @param lsAccID ���������accID<br>
	 * * arraylist-string[3]<br>
0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
1: accID<br>
2: ����ת����ID
	 * @param lsBGAccID ���������BG��accID<br>
	 * * arraylist-string[3]<br>
0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
1: accID<br>
2: ����ת����ID
	 * @param GOClass Go������ P: biological Process F:molecular Function C: cellular Component ���GOClassΪ""��ô��������ȫ�������Ƿ���ʱ����BP���У���������������⡣
	 * ����""������Ϊ�˲���������ʹ��
	 * @param sepID 
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @throws Exception
	 * @return ����list
	 * ��һ�� lsResultTable,Go���������Ľ�����<br>
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

	 * �ڶ���lsGeneInfoFinal Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence";title2[6]="Evalue";title2[7]="subjectSymbol";<br>
			title2[8]="Description";title2[9]="GOID";;title2[10]="GOTerm";title2[11]="Evidence"<br>
			title[12]="P-Value";title[13]="FDR";title[14]="Enrichment";title[15]="(-log2P)<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence"<br>
			title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
		<b>���blast���Ż��е�����</b>
		Go2gene���<br>
	 * 	title[0]="GOID";title[1]="GOTerm";title[2]="evidence";title[3]="queryID";title[4]="querySymbol";
	 * title[5]="subjectSymbol��<br>
		title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
	 */
	private static ArrayList<ArrayList<String[]>> getNBCFisher(String condition,ArrayList<String[]> lsAccID,ArrayList<String[]> lsBGAccID,String GOClass,boolean sepID,int QtaxID,
			boolean blast,int StaxID,double evalue) throws Exception
	{
		///////////////////  �������  ////////////////////////////////////////////////////////////
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

		
	    ////////////////////////////////////////   �ӱ���   //////////   FisherResult   //////////////////////////////////////////
		String[] title = new String[10];
		title[0] = "GOID"; title[1] = "GOTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInGOID"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		lsFisherResult.add(0,title);
		/////////////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////   ���� �Լ� �ӱ���   //////////   gene2GOInfo   //////////////////////////////////////////

	    //����accID
		lsGeneInfo = CopeID.copeCombineID(condition,lsGeneInfo, 1, 0, sepID);
					//////////////////////////////  Gene2Go�Ƿ���Ҫ����pvalue  ///////////////////////////////////
		ArrayList<String[]> lsGoInfoPvalue = ArrayOperate.combArrayListHash(lsFisherResult, lsGeneInfo, 0, 4);
		final int colpValue=lsGeneInfo.get(0).length+6;
		//����
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
	    	int[] colNum = new int[9]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		    colNum[0] = 1; colNum[1] = 8; colNum[2] = 9; 
		    colNum[3] = lsGeneInfo.get(0).length+0;colNum[4] = lsGeneInfo.get(0).length+1;colNum[5] = lsGeneInfo.get(0).length+2;
			colNum[6] = lsGeneInfo.get(0).length+3;colNum[7] = lsGeneInfo.get(0).length+4;colNum[8] = lsGeneInfo.get(0).length+5;
			lsGoInfoPvalue = ArrayOperate.listCope(lsGoInfoPvalue, colNum, false);
			
			titleGen2GoInfo[0] = "QueryID"; titleGen2GoInfo[1] = "QuerySymbol"; titleGen2GoInfo[2] = "Description"; titleGen2GoInfo[3] = "GOID"; titleGen2GoInfo[4] = "GOTerm";
			titleGen2GoInfo[5] = "Evidence"; 
			titleGen2GoInfo[6] = "Evalue"; titleGen2GoInfo[7] = "SubjectSymbol"; titleGen2GoInfo[8] = "Description";titleGen2GoInfo[9] = "GOID";
			titleGen2GoInfo[10] = "GOTerm";titleGen2GoInfo[11] = "Evidence";
						//////////////////////////////Gene2Go�Ƿ���Ҫ����pvalue  ///////////////////////////////////
			titleGen2GoInfo[12] = "P-Value"; titleGen2GoInfo[13] = "FDR"; titleGen2GoInfo[14] = "Enrichment"; titleGen2GoInfo[15] = "(-log2P)";
		}
	    else {
	    	titleGen2GoInfo = new String[10];
	    	int[] colNum = new int[7]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		    colNum[0] = 1;
		    colNum[1] = lsGeneInfo.get(0).length+0;colNum[2] = lsGeneInfo.get(0).length+1;colNum[3] = lsGeneInfo.get(0).length+2;
			colNum[4] = lsGeneInfo.get(0).length+3;colNum[5] = lsGeneInfo.get(0).length+4;colNum[6] = lsGeneInfo.get(0).length+5;
			lsGoInfoPvalue = ArrayOperate.listCope(lsGoInfoPvalue, colNum, false);
			
			titleGen2GoInfo[0] = "QueryID"; titleGen2GoInfo[1] = "Symbol"; titleGen2GoInfo[2] = "Description"; titleGen2GoInfo[3] = "GOID"; titleGen2GoInfo[4] = "GOTerm";
			titleGen2GoInfo[5] = "Evidence";
			titleGen2GoInfo[6] = "P-Value"; titleGen2GoInfo[7] = "FDR"; titleGen2GoInfo[8] = "Enrichment"; titleGen2GoInfo[9] = "(-log2P)";
		}
	    lsGoInfoPvalue.add(0,titleGen2GoInfo);
	    
	    ////////////////////////////////////////   ���� �Լ� �ӱ���   //////////   GO2Gene   //////////////////////////////////////////  
	    ArrayList<String[]> lsGo2GenFinal = null;
		if (blast) {
			lsGo2Gen =  CopeID.copeCombineID(condition,lsGo2Gen, 4, 3, sepID);
			
			lsGo2GenFinal = ArrayOperate.combArrayListHash(lsFisherResult, lsGo2Gen, 0, 0);
			int[] colNum = new int[7]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
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
