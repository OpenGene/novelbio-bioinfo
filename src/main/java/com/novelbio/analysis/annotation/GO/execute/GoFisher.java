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
 * ��QGenID2GoInfo��õ���Ϣת��Ϊ��Rʶ��ĸ�ʽ��������R������ɼ���
 * @author zong0jie
 *
 */
public class GoFisher {
	
	static String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform/";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
	static String writeRFIle="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";
	
	/**
	 * ��Ҫ��R�ű��е�·����ͳһ
	 */
	static String Rresult="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";

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
	 * @param resultExcel2003 ��Ҫ�Ӻ�׺xls�������Զ���ӣ����ļ�����xls֮����������ϡ�_��ID��
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
	 * ��ElimFisher�ķ�������GO����
	 * @param geneFileXls
	 * @param GOClass P: biological Process F:molecular Function C: cellular Component
	 * @param colID ѡ���������е������С�0��accID��1��foldChange
	 * <b>ע����ʵ����</b>
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
	 * @param geneID string[] gene��ID
	 * @param GOClass
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param resultExcel2003
	 * @param prix sheet�ı���
	 * @throws Exception
	 */
	private static void getElimFisher(String[] geneID, String GOClass, String backGroundFile,int QtaxID,boolean blast, int StaxID,double evalue, String resultExcel2003, String prix) throws Exception
	{
		String resultGeneGofile = Rworkspace+"topGO/GeneGOInfo.txt";
		String resultBGGofile = Rworkspace+"topGO/BG2Go.txt";
		String resultGeneIDfile = Rworkspace+"topGO/GeneID.txt";
		//�����ı�����һ����¼ѡ��BP��MF��CC���ڶ�����¼д�ļ���Ĭ����GoResult.txt
		//�����������֣���ʾ��ʾ���ٸ�GOTerm ,���ĸ���¼GOInfo������ÿ��GO��Ӧ�Ļ����ļ���
		String parameter = Rworkspace + "topGO/parameter.txt";
		///////////////����Ϊд��parameter�ľ������////////////////////////////////////////////////////////////////////
		int NumGOID = 300;
		//����elimFisher�Ľ��table�ļ�
		String RGOresultTableFile = "GoResult.txt";
		//ÿ��GO���������еı�������
		//��ʽΪ
		//#GO:010101
		//NM_0110101
		String RGoInfo = "GOInfo.txt";
		////////////////////////////////////////////////////////////////////////////////////////////
		//��ò�������б�geneInfo�б�
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
		//����ÿ�������Ӧ�ľ�����Ϣ�������Ͳ���ȥ���ݿ�������
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
		//GOID��ӦGeneID��hash��
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( Rworkspace+"topGO/"+RGoInfo);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}
		ArrayList<String[]> lsResultTable = getElimFisherTable(Rworkspace+"topGO/"+RGOresultTableFile);
		//////////////////////������е�ÿһ��GO���������Ӧ��Gene����arrayList�б���/////////////////////////////////////////////
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
				String[] strTmpGo2Gene = new String[4];//��GO2Geneװ�����ļ���
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[1];//geneSymbol
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
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+Rworkspace+ "topGO.R";
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
			getFisher( QtaxID, TmpgeneID, GOClass, backGroundFile, resultExcel2003+"_"+keyID+".xls", "", blast,StaxID,evaule, true);
		}
	}
	
	
	/**
	 * �������ķ�������GO����
	 * @param geneFileXls
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param colID ѡ���������е������С�0��accID��1��foldChange
	 * <b>ע����ʵ����</b>
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param taxIDfile  taxID�ļ�  ���blast=F����ô��һ�� queryTaxID�����blast=T����ô��һ�� queryTaxID���ڶ��� subjectTaxID�������� evalue
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
	 * @param geneID �����accID
	 * @param GOClass : P: biological Process F:molecular Function C: cellular Component
	 * @param backGroundFile �����ļ�
	 * @param taxIDfile taxID�ļ�  ���blast=F����ô��һ�� queryTaxID�����blast=T����ô��һ�� queryTaxID���ڶ��� subjectTaxID�������� evalue
	 * @param resultGeneGofile ���Gene2Go
	 * @param writeFile ����Go�����������
	 * @param sepID �Ƿ�̽��ָtrue: ͬ�������̽�벻�ϲ��� false: ͬ�������̽��ϲ�
	 * @param blast :Ŀǰblast��֧�� sepID=false���������blast=Tʱ��sepID��Ч
	 * @param evalue blast��evalue
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
		lsGOinfo = (ArrayList<String[]>) obj[0];//�������fisher�������
		lsGoResult = (ArrayList<String[]>) obj[1];//����������н��
		TxtReadandWrite txtGoInfo=new TxtReadandWrite();
		txtGoInfo.setParameter(writeRFIle, true, false);
		int column[]=new int[4]; column[0]=2;column[1]=3;column[2]=4;column[3]=5;
		txtGoInfo.ExcelWrite(lsGOinfo, "\t", column, true, 1, 1);
		Rfisher();
		
		TxtReadandWrite txtRresult=new TxtReadandWrite();
		txtRresult.setParameter(Rresult, false, true);
		
		String[][] RFisherResult=txtRresult.ExcelRead("\t", 2, 2, txtRresult.ExcelRows(), txtRresult.ExcelColumns(2, "\t"));
		//�������յ�fisher���
		/**
		 * GOID GOTerm difGene AllDifGene GeneInGoID AllGene // Pvalue FDR enrichment logP 
		 * û�ӱ���
		 */
		ArrayList<String[]> lsFisherResult=new ArrayList<String[]>();
	
		//�Ѵ�R��ȡ��pvalue�Ⱥϲ���ȥ
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
		//����
        Collections.sort(lsFisherResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[6]); Double b=Double.parseDouble(arg1[6]);
                return a.compareTo(b);
            }
        });
		ArrayList<String[]> lsGOInfoResult = ArrayOperate.combArrayListHash(lsFisherResult, lsGoResult, 0, 3);
		
		int[] colNum = new int[6]; //��ȥtitle[0]="GOID";title[1]="GOTerm";title[2]="difGene";title[3]="AllDifGene";title[4]="GeneInGoID";title[5]="AllGene";title[6]="Pvalue";title[7]="FDR";title[8]="enrichment";title[9]="(-log2P)";
		colNum[0] = lsGoResult.get(0).length+0;colNum[1] = lsGoResult.get(0).length+1;colNum[2] = lsGoResult.get(0).length+2;
		colNum[3] = lsGoResult.get(0).length+3;colNum[4] = lsGoResult.get(0).length+4;colNum[5] = lsGoResult.get(0).length+5;
		
		lsGOInfoResult = ArrayOperate.listCope(lsGOInfoResult, colNum, false);
		final int colPvalue = lsGoResult.get(0).length+1;
		//����
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
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+Rworkspace+ "GOfisherBHfdr.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
}
