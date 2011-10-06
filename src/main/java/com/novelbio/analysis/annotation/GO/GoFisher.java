package com.novelbio.analysis.annotation.GO;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.GO.queryDB.QgeneID2Go;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.copeID.FisherTest;
import com.novelbio.analysis.annotation.copeID.ItemInfo;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.kegg.KGpathway;

/**
 * service 层，实现GO的fisher检验
 * 包括常规Fisher和elimFisher
 * @author zong0jie
 *
 */
public class GoFisher {
	private static final Logger logger = Logger.getLogger(GoFisher.class);
	public static final String TEST_GO = "go";
	public static final String TEST_KEGGPATH = "KEGGpathway";
	
	public GoFisher(ArrayList<CopedID> lsCopedIDsTest, ArrayList<CopedID> lsCopedIDsBG, boolean blast, String GOtype)
	{
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
		this.GOType = GOtype;
	}
	
	public GoFisher(boolean blast, String GOtype)
	{
		this.blast = blast;
		this.GOType = GOtype;
	}

	int taxID = 0;
	boolean blast = false;
	
	ArrayList<CopedID> lsCopedIDsTest = null;
	ArrayList<CopedID> lsCopedIDsBG = null;
	
	ArrayList<String[]> lsTest = null;
	ArrayList<String[]> lsBG = null;
	
	String BGfile = "";
	String GOType = "";
	/**
	 * AccID2CopedID的对照表
	 */
	HashMap<String, CopedID> hashAcc2CopedID = new HashMap<String, CopedID>();
	/**
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 */
	HashMap<String, ArrayList<CopedID>> hashgene2CopedID = new HashMap<String, ArrayList<CopedID>>();
	
	public void setTest(ArrayList<String> lsCopedID, int taxID) {
		lsCopedIDsTest = new ArrayList<CopedID>();
		for (String string : lsCopedID) {
			CopedID copedID = new CopedID(string, taxID, false);
			lsCopedIDsTest.add(copedID);
		}
	}
	
	public void setLsCopedID(ArrayList<CopedID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		lsTest = convert2GO(lsCopedIDsTest);
	}
	
	public void setLsBG(String fileName) {
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		TxtReadandWrite txtReadBG = new TxtReadandWrite(fileName, false);
		lsBG = txtReadBG.ExcelRead("\t", 1, 1, txtReadBG.ExcelRows(), 2, 1);
	}
	
	
	
	
	/**
	 * 必须先设置lsTestCopedID，因为要从中读取taxID
	 * @param fileName
	 */
	public void setLsBGaccID(String fileName) {
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		String[][] accID = null;
		try {
			accID = ExcelTxtRead.readExcel(fileName, new int[]{1}, 1, -1);
		} catch (Exception e) {
			try {
				accID = ExcelTxtRead.readtxtExcel(fileName, "\t",new int[]{1}, 1, -1);
			} catch (Exception e2) {
				logger.error("BG accID file is not correct: "+ fileName);
			}
		}
		int taxID = lsCopedIDsTest.get(0).getTaxID();
		for (String[] strings : accID) {
			lsCopedIDsBG.add(new CopedID(strings[0], taxID, false));
		}
	}
	/**
	 * 返回
	 * @param Type
	 * @return
	 */
	public ArrayList<String[]> getResult(String Type) {
		ArrayList<String[]> lsTest = null;
		try {
			lsTest = normalTest(Type);
		} catch (Exception e) {
			logger.error("error");
		}
		
		ArrayList<String[]> lsAnno = null;
		if (Type.equals(TEST_GO)) {
			lsAnno = getGO2Info(GOType);
		}
		else if (Type.equals(TEST_KEGGPATH)) {
			lsAnno = getPath2Info();
		}
		ArrayList<String[]> lsResult = null;
		if (blast) {
			 lsResult = ArrayOperate.combArrayListHash(lsTest, lsAnno, 0, 6);
		}
		else {
			 lsResult = ArrayOperate.combArrayListHash(lsTest, lsAnno, 0, 3);
		}
		return lsResult;
		
	}
	
	
	
	/**
	 * 指定算go还是pathway
	 * @return 结果没加标题<br>
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
	public ArrayList<String[]> normalTest(String Type) throws Exception
	{
		ArrayList<String[]> lsTestResult = null;
		if (Type.equals(TEST_GO)) {
			lsTestResult = FisherTest.getFisherResult(lsTest, lsBG, new ItemInfo() {
				@Override
				public String[] getItemName(String ItemID) {
					String[] GoTerm = new String[1];
					GoTerm[0] = Go2Term.getHashGo2Term().get(ItemID).getGoTerm();
					return GoTerm;
				}
			});
		}
		else if (Type.equals(TEST_KEGGPATH)) {
			lsTestResult = FisherTest.getFisherResult(lsTest, lsBG, new ItemInfo() {
				@Override
				public String[] getItemName(String ItemID) {
					String[] KeggTerm = new String[1];
					KeggTerm[0] = KGpathway.getHashKGpath().get(ItemID).getTitle();
					return KeggTerm;
				}
			});
		}
		return lsTestResult;
	}
	
	/**
	 * 将List-CopedID转化为
	 * geneID goID,goID,goID的样式
	 * 并按照genUniID去冗余
	 */
	private ArrayList<String[]> convert2GO(ArrayList<CopedID> lsCopedIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
			for (CopedID copedID : lsCopedIDs) {
				if (hashGenUniID.contains(copedID.getGenUniID())) {
					continue;
				}
				hashGenUniID.add(copedID.getGenUniID());
				ArrayList<AGene2Go> lsGO = null;
				if (blast) 
					lsGO = copedID.getGene2GOBlast(GOType);
				else 
					lsGO = copedID.getGene2GO(GOType);
				
				if (lsGO == null || lsGO.size() == 0) {
					continue;
				}
				String[] tmpResult = new String[2];
				tmpResult[0] = copedID.getGenUniID();
				for (AGene2Go aGene2Go : lsGO) {
					if (tmpResult[1] == null || tmpResult[1].trim().equals("")) {
						tmpResult[1] = aGene2Go.getGOID();
					}
					else {
						 tmpResult[1] = tmpResult[1] + "," + aGene2Go.getGOID();
					}
				}
				lsResult.add(tmpResult);
			}
			return lsResult;
	}
	
	
	private void fillCopedIDInfo()
	{
		for (CopedID copedID : lsCopedIDsTest) {
			//去冗余，accID相同去掉
			if (hashAcc2CopedID.containsKey(copedID.getAccID())) {
				continue;
			}
			hashAcc2CopedID.put(copedID.getAccID(), copedID);
			if (hashgene2CopedID.containsKey(copedID.getGenUniID())) {
				hashgene2CopedID.get(copedID.getGenUniID()).add(copedID);
			}
			else {
				ArrayList<CopedID> lstmp = new ArrayList<CopedID>();
				lstmp.add(copedID);
				hashgene2CopedID.put(copedID.getGenUniID(), lstmp);
			}
		}
	}
	/**
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="GOID";title2[7]="GOTerm";title2[8]="Evidence"<br>
			title[9]="P-Value";title[10]="FDR";title[11]="Enrichment";title[12]="(-log2P)<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence"<br>
			title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
	 */
	public ArrayList<String[]> getGO2Info(String GOType)
	{
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<AGene2Go> lsGen2Go = null;
			//获得具体的GO信息
			if (blast)
				lsGen2Go = copedID.getGene2GO(GOType);
			else
				lsGen2Go = copedID.getGene2GO(GOType);
			if (lsGen2Go == null || lsGen2Go.size() == 0) {
				continue;
			}
			//GO前面的常规信息的填充,Symbol和description等
			String[] tmpresult = getAnnoInfo(copedID, blast);
			//GO信息的填充
			for (AGene2Go aGene2Go : lsGen2Go) {
				String[] result = null;
				if (blast)
					result = ArrayOperate.copyArray(tmpresult, 9);
				else
					result = ArrayOperate.copyArray(tmpresult, 6);
				result[result.length -1] = aGene2Go.getEvidence();
				result[result.length -2] = aGene2Go.getGOTerm();
				result[result.length -3] =aGene2Go.getGOID();
				lsFinal.add(result);
			}
		}
		return lsFinal;
	}
	/**
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 */
	public ArrayList<String[]> getPath2Info()
	{
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<KGpathway> lsKgPathway = null;
			//获得具体的pathway信息
			if (blast)
				lsKgPathway = copedID.getKegPath();
			else
				lsKgPathway = copedID.getKegPathBlast();
			if (lsKgPathway == null || lsKgPathway.size() == 0) {
				continue;
			}
			//GO前面的常规信息的填充,Symbol和description等
			String[] tmpresult = getAnnoInfo(copedID, blast);
			//GO信息的填充
			for (KGpathway kGpathway : lsKgPathway) {
				String[] result = null;
				if (blast)
					result = ArrayOperate.copyArray(tmpresult, 8);
				else
					result = ArrayOperate.copyArray(tmpresult, 5);
				result[result.length -1] = kGpathway.getTitle();
				result[result.length -2] = kGpathway.getPathName();
				lsFinal.add(result);
			}
		}
		return lsFinal;
	}
	/**
	 * 获得该copedID的annotation信息
	 * @param copedID
	 * @param blast
	 * @return
	 * 	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";<br>
	 * title2[3]="Evalue";
	 * title2[4]="subjectSymbol";
			title2[5]="Description";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";<br>
	 */
	private String[] getAnnoInfo(CopedID copedID, boolean blast) {
		String[] tmpresult = null;
		if (blast)
			tmpresult = new String[6];
		else
			tmpresult = new String[3];
		tmpresult[0] = copedID.getAccID(); tmpresult[1] = copedID.getSymbo(); tmpresult[2] = copedID.getDescription();
		if (blast) {
			if (copedID.getCopedIDLsBlast() != null && copedID.getLsBlastInfos() != null && copedID.getLsBlastInfos().size() > 0) {
				for (int i = 0; i < copedID.getLsBlastInfos().size(); i++) {
					if (tmpresult[3].trim().equals("")) {
						tmpresult[3] = copedID.getLsBlastInfos().get(i).getEvalue() + "";
						tmpresult[4] = copedID.getCopedIDLsBlast().get(i).getSymbo();
						tmpresult[5] = copedID.getCopedIDLsBlast().get(i).getDescription();
					}
					else {
						tmpresult[3] = tmpresult[3] + "//" + copedID.getLsBlastInfos().get(i).getEvalue();
						tmpresult[4] = tmpresult[4] + "//" + copedID.getCopedIDLsBlast().get(i).getSymbo();
						tmpresult[5] = tmpresult[5] + "//" + copedID.getCopedIDLsBlast().get(i).getDescription();
					}
				}
			}
		}
		return tmpresult;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////// Elim Fisher //////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
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
		FileOperate.delAllFile(NovelBioConst.R_WORKSPACE_TOPGO);
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
		ArrayList<ArrayList<String[]>> lsBGGenGoInfo = QgeneID2Go.getGenGoInfo(lsGeneBG, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		
		if (lsGeneUpCope.size()>0) {
			ArrayList<ArrayList<String[]>> lsResult = getElimFisher(prix[0],lsGeneUpCope, lsBGGenGoInfo, GOClass, sepID, QtaxID, blast, StaxID, evalue,NumGo);
			excelResult.WriteExcel(prix[0]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResult.get(1) , true);
			excelResult.WriteExcel(prix[0]+"Gene2GO", 1, 1,lsResult.get(2) , true);

			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP+prix[0], 
					FileOperate.getParentPathName(resultPicName), FileOperate.getFileName(resultPicName)+prix[0]+".pdf",true);
		}
		if (lsGeneDownCope.size()>0) {
			
			
			ArrayList<ArrayList<String[]>> lsResult =getElimFisher(prix[1],lsGeneDownCope, lsBGGenGoInfo, GOClass, sepID, QtaxID, blast, StaxID, evalue,NumGo);
			excelResult.WriteExcel(prix[1]+"GoAnalysis", 1, 1, lsResult.get(0), true);
			excelResult.WriteExcel(prix[1]+"GO2Gene", 1, 1,lsResult.get(1) , true);
			excelResult.WriteExcel(prix[1]+"Gene2GO", 1, 1,lsResult.get(2) , true);
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP+prix[1], 
					FileOperate.getParentPathName(resultPicName), FileOperate.getFileName(resultPicName)+prix[1]+".pdf",true);
		}
	}

	/**
	 * 生成的gomap图重命名为NovelBioConst.R_WORKSPACE_TOPGO_GOMAP+condition
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
	public static ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<ArrayList<String[]>> lsBGGenGoInfo,String GOClass, boolean sepID,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
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
		
		
		ArrayList<String[]> lsBGGene2Go = lsBGGenGoInfo.get(1);//gene go,go,go信息
		
		
		/////////////////////////topGo的参数///////////////////////
		TxtReadandWrite txtParam = new TxtReadandWrite();
		txtParam.setParameter(NovelBioConst.R_WORKSPACE_TOPGO_PARAM, true, false);
		String content = "";
		if (GOClass.equals(GOInfoAbs.GO_BP)) 
			content = "BP";
		else if (GOClass.equals(GOInfoAbs.GO_MF)) 
			content = "MF";
		else if (GOClass.equals(GOInfoAbs.GO_CC)) 
			content = "CC";
		
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
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
				FileOperate.getParentPathName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP), FileOperate.getFileName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP)+condition,true);
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
	/**
	 * 生成的gomap图重命名为NovelBioConst.R_WORKSPACE_TOPGO_GOMAP+condition
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
	public ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<String[]> lsBGGenGoInfo,String GOClass,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
	{
		
		//获得差异基因列表，geneInfo列表
		String[] strGeneID = null;
//		ArrayList<ArrayList<String[]>> lsGenGoInfo = QgeneID2Go.getGenGoInfo(lsAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsGeneInfo = getGO2Info(GOClass);
//		ArrayList<String[]> lsGene2Go = lsGenGoInfo.get(1);//gene go,go,go信息
		ArrayList<String[]> lsGo2Gene = null;//Go2Gene的信息，仅在blast时才有用
		strGeneID = new String[lsTest.size()];//用于elim检验
		for (int i = 0; i < strGeneID.length; i++) {
			strGeneID[i] = lsTest.get(i)[0];
		}		
		
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
		//////////////////////////////////////////////////////////////////////
		ArrayList<String[]>  lsResultTable = RElimFisher(strGeneID);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
				FileOperate.getParentPathName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP), FileOperate.getFileName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP)+condition,true);
		//GOID对应GeneID的hash表
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( NovelBioConst.R_WORKSPACE_TOPGO_GOINFO);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}		
		
		
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
	
	/**
	 * 读取RGOresultTableFile文件，包含标题列
	 * 产生ArrayList-string[]<br>
	 * @param strGeneID genUniID的array
	 * @param NumGOID 显示多少个GO
	 * @return
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
	 */
	private ArrayList<String[]> RElimFisher(String[] strGeneID, int NumGOID){
		TxtReadandWrite txtParam = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_PARAM, true);
		String content = "";
		if (GOType.equals(GOInfoAbs.GO_BP)) 
			content = "BP";
		else if (GOType.equals(GOInfoAbs.GO_MF)) 
			content = "MF";
		else if (GOType.equals(GOInfoAbs.GO_CC)) 
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
	/**
	 * 将elim的GO2Gene改成正规的Go2Gene 的List并返回
	 * @param hashElimGo2Gene topGO中所含有的go2gene的信息
	 * @oaram lsGO2GeneInfo topGO所生成的GO结果的table
	 * @param lsGeneID 该次分析的的所有基因列表
	 */
	private void getElimGo2Gene(HashMap<String, ArrayList<String>> hashElimGo2Gene, ArrayList<String[]> lsResultTable, ArrayList<String> lsGeneID)
	{
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			//某个GO中所含有的所有背景基因
			ArrayList<String> lsTmpGeneID = hashElimGo2Gene.get(lsResultTable.get(i)[0]);
			//获得某个GO中所含有的所有差异基因
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			for (String string : lsCoGeneID)
			{
				String[] strTmpGo2Gene = new String[4];//将GO2Gene装入结果文件。
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[2];//geneSymbol
				hashElimGo2Gene.add(strTmpGo2Gene);
			}
		}
		
		
		
		
	}
	
	
	
	
	
	
	
}
