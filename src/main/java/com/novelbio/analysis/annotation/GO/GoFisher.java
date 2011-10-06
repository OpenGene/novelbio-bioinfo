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
 * service �㣬ʵ��GO��fisher����
 * ��������Fisher��elimFisher
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
	 * AccID2CopedID�Ķ��ձ�
	 */
	HashMap<String, CopedID> hashAcc2CopedID = new HashMap<String, CopedID>();
	/**
	 * gene2CopedID�Ķ��ձ����accID��Ӧͬһ��geneID��ʱ��������hash������
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
	 * ����������lsTestCopedID����ΪҪ���ж�ȡtaxID
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
	 * ����
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
	 * ָ����go����pathway
	 * @return ���û�ӱ���<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>  
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
	 * ��List-CopedIDת��Ϊ
	 * geneID goID,goID,goID����ʽ
	 * ������genUniIDȥ����
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
			//ȥ���࣬accID��ͬȥ��
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
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="GOID";title2[7]="GOTerm";title2[8]="Evidence"<br>
			title[9]="P-Value";title[10]="FDR";title[11]="Enrichment";title[12]="(-log2P)<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence"<br>
			title[6]="P-Value";title[7]="FDR";title[8]="Enrichment";title[9]="(-log2P)<br>
	 */
	public ArrayList<String[]> getGO2Info(String GOType)
	{
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<AGene2Go> lsGen2Go = null;
			//��þ����GO��Ϣ
			if (blast)
				lsGen2Go = copedID.getGene2GO(GOType);
			else
				lsGen2Go = copedID.getGene2GO(GOType);
			if (lsGen2Go == null || lsGen2Go.size() == 0) {
				continue;
			}
			//GOǰ��ĳ�����Ϣ�����,Symbol��description��
			String[] tmpresult = getAnnoInfo(copedID, blast);
			//GO��Ϣ�����
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
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 */
	public ArrayList<String[]> getPath2Info()
	{
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<KGpathway> lsKgPathway = null;
			//��þ����pathway��Ϣ
			if (blast)
				lsKgPathway = copedID.getKegPath();
			else
				lsKgPathway = copedID.getKegPathBlast();
			if (lsKgPathway == null || lsKgPathway.size() == 0) {
				continue;
			}
			//GOǰ��ĳ�����Ϣ�����,Symbol��description��
			String[] tmpresult = getAnnoInfo(copedID, blast);
			//GO��Ϣ�����
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
	 * ��ø�copedID��annotation��Ϣ
	 * @param copedID
	 * @param blast
	 * @return
	 * 	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";<br>
	 * title2[3]="Evalue";
	 * title2[4]="subjectSymbol";
			title2[5]="Description";<br>
			��blast��<br>
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
	 * ���ɵ�gomapͼ������ΪNovelBioConst.R_WORKSPACE_TOPGO_GOMAP+condition
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
	public static ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<ArrayList<String[]>> lsBGGenGoInfo,String GOClass, boolean sepID,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
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
		
		
		ArrayList<String[]> lsBGGene2Go = lsBGGenGoInfo.get(1);//gene go,go,go��Ϣ
		
		
		/////////////////////////topGo�Ĳ���///////////////////////
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
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
				FileOperate.getParentPathName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP), FileOperate.getFileName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP)+condition,true);
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
	/**
	 * ���ɵ�gomapͼ������ΪNovelBioConst.R_WORKSPACE_TOPGO_GOMAP+condition
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
	public ArrayList<ArrayList<String[]>> getElimFisher(String condition, ArrayList<String[]>  lsAccID,ArrayList<String[]> lsBGGenGoInfo,String GOClass,int QtaxID,boolean blast, int StaxID,double evalue,int NumGOID) throws Exception
	{
		
		//��ò�������б�geneInfo�б�
		String[] strGeneID = null;
//		ArrayList<ArrayList<String[]>> lsGenGoInfo = QgeneID2Go.getGenGoInfo(lsAccID, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		ArrayList<String[]> lsGeneInfo = getGO2Info(GOClass);
//		ArrayList<String[]> lsGene2Go = lsGenGoInfo.get(1);//gene go,go,go��Ϣ
		ArrayList<String[]> lsGo2Gene = null;//Go2Gene����Ϣ������blastʱ������
		strGeneID = new String[lsTest.size()];//����elim����
		for (int i = 0; i < strGeneID.length; i++) {
			strGeneID[i] = lsTest.get(i)[0];
		}		
		
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
		//////////////////////////////////////////////////////////////////////
		ArrayList<String[]>  lsResultTable = RElimFisher(strGeneID);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, 
				FileOperate.getParentPathName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP), FileOperate.getFileName(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP)+condition,true);
		//GOID��ӦGeneID��hash��
		Hashtable<String,ArrayList<String>> hashGO2Gene = getGo2GeneBG( NovelBioConst.R_WORKSPACE_TOPGO_GOINFO);
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (int i = 0; i < strGeneID.length; i++) {
			lsGeneID.add(strGeneID[i]);
		}		
		
		
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
	
	/**
	 * ��ȡRGOresultTableFile�ļ�������������
	 * ����ArrayList-string[]<br>
	 * @param strGeneID genUniID��array
	 * @param NumGOID ��ʾ���ٸ�GO
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
		///////////////������geneID/////////////////////////////////////////////
		TxtReadandWrite txtGenID= new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_GENEID, true);
		txtGenID.Rwritefile(strGeneID); txtGenID.close();
		//ִ��
		String command=NovelBioConst.R_SCRIPT + NovelBioConst.R_WORKSPACE_TOPGO_RSCRIPT;
		CmdOperate cmdOperate = new CmdOperate(command);
		cmdOperate.doInBackground();
		//��ȡ
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_GORESULT, false);
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
	/**
	 * ��ȡRGoInfo�ļ����������GO2Gene����Ϣ����ΪArrayList--ArrayList-String
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
	 * ��elim��GO2Gene�ĳ������Go2Gene ��List������
	 * @param hashElimGo2Gene topGO�������е�go2gene����Ϣ
	 * @oaram lsGO2GeneInfo topGO�����ɵ�GO�����table
	 * @param lsGeneID �ôη����ĵ����л����б�
	 */
	private void getElimGo2Gene(HashMap<String, ArrayList<String>> hashElimGo2Gene, ArrayList<String[]> lsResultTable, ArrayList<String> lsGeneID)
	{
		for (int i = 1; i < lsResultTable.size(); i++) 
		{
			//ĳ��GO�������е����б�������
			ArrayList<String> lsTmpGeneID = hashElimGo2Gene.get(lsResultTable.get(i)[0]);
			//���ĳ��GO�������е����в������
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			for (String string : lsCoGeneID)
			{
				String[] strTmpGo2Gene = new String[4];//��GO2Geneװ�����ļ���
				strTmpGo2Gene[0] = lsResultTable.get(i)[0];strTmpGo2Gene[1] = lsResultTable.get(i)[1]; strTmpGo2Gene[2] = string;
				strTmpGo2Gene[3] = hashGeneInfo.get(string)[2];//geneSymbol
				hashElimGo2Gene.add(strTmpGo2Gene);
			}
		}
		
		
		
		
	}
	
	
	
	
	
	
	
}
