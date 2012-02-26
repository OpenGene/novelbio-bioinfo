package com.novelbio.analysis.annotation.functiontest;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.generalConf.NovelBioConst;

public class ElimGOFunTest extends NovelGOFunTest{
	private static final Logger logger = Logger.getLogger(ElimGOFunTest.class);
	public ElimGOFunTest(ArrayList<CopedID> lsCopedIDsTest, ArrayList<CopedID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast, GoType);
	}
	public ElimGOFunTest(boolean blast,String GoType, double evalue, int...blastTaxID) {
		super(blast, GoType, evalue, blastTaxID);
		this.GoType = GoType;
	}
	
	public ElimGOFunTest() {}
	
	int NumGOID = 300;
	
	/**
	 * �趨����
	 */
	public void setNumGOID(int NumGOID) {
		this.NumGOID = NumGOID;
	}

	public ArrayList<String[]> getItem2GenePvalue() {
		ArrayList<String[]> lsTestResult = null;
		try {
			//ͬʱ��ʼ����	 strGeneID �� lsGeneID
			lsTestResult = getTestResult();
		} catch (Exception e) {
			logger.error("error");
		}
		//lsGeneID�Ѿ���getTestResult()�г�ʼ������
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
	 * @param run �Ƿ�Ҫ������һ��
	 * @return ����ӱ�����<br>
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
	 * û�оͷ���null
	 */
	public ArrayList<String[]> getTestResult()
	{
		if (lsTestResult != null && lsTestResult.size() > 0)
			return lsTestResult;
		if (!setStrGeneID())
			return null;
		TxtReadandWrite txtParam = new TxtReadandWrite(NovelBioConst.R_WORKSPACE_TOPGO_PARAM, true);
		String content = "";
		if (GoType.equals(Go2Term.GO_BP)) 
			content = "BP";
		else if (GoType.equals(Go2Term.GO_MF)) 
			content = "MF";
		else if (GoType.equals(Go2Term.GO_CC)) 
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
		lsTestResult = txtRGo2Gene.ExcelRead("\t", 2, 2, txtRGo2Gene.ExcelRows(), txtRGo2Gene.ExcelColumns("\t"), 0);
		//ȥ��"��
		for (String[] strings : lsTestResult) 
		{
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].replace("\"", "");
			}
		}

		return lsTestResult;
	}

	
	String[] strGeneID = null;
	ArrayList<String> lsGeneID = null;
	private boolean setStrGeneID()
	{
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
		
		strGeneID = new String[lsTest.size()];//����elim����
		lsGeneID = new ArrayList<String>();//��strGeneIDһ���Ķ���
		for (int i = 0; i < lstest.size(); i++) {
			strGeneID[i] = lstest.get(i)[0];
			lsGeneID.add(strGeneID[i]);
		}
		return true;
	}

	/**
	 * ����������
	 * ��elim��GO2Gene�ĳ������Go2Gene ��List������
	 * @param hashElimGo2Gene topGO�������е�go2gene����Ϣ
	 * @oaram lsResultTable topGO�����ɵ�GO�����table
	 * @param lsGeneID �ôη����ĵ����в�������б�
	 * @return
	 * Go����������Go2Gene���<br>
	 * blast��<br>
	 * title2[0]="GOID";title2[1]="GOTerm"
	 * 			title2[2]="QueryID";title2[3]="QuerySymbol";title2[4]="Description";title2[5]="Evalue";title2[6]="subjectSymbol";<br>
			title2[7]="Description";<br>
			��blast��<br>title2[0]="GOID";title2[1]="GOTerm"
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
			//ĳ��GO�������е����б�������
			ArrayList<String> lsTmpGeneID = hashElimGo2Gene.get(lsResultTable.get(i)[0]);
			//���ĳ��GO�������е����в������
			ArrayList<String> lsCoGeneID = ArrayOperate.getCoLs(lsTmpGeneID, lsGeneID);
			//ÿһ��Go����Ӧ�Ļ���
			for (String string : lsCoGeneID)
			{
				ArrayList<CopedID> lscopedIDs = hashgene2CopedID.get(string);
				//ÿһ�����������еĶ��copedID��Ҳ���Ƕ����ͬ��accID
				for (CopedID copedID : lscopedIDs) {
					String[] tmpresultRaw = copedID.getAnno(blast);
					String[] anno = copyAnno(copedID.getAccID(), tmpresultRaw);
					String[] result = new String[anno.length + 2];
					result[0] = lsResultTable.get(i)[0]; result[1] = lsResultTable.get(i)[1]; result[2] = copedID.getAccID();
					int m = 4;
					for (int j = 3; j < result.length; j++) {
						result[j] = anno[m - 3];
						if (m-3 == 3 ) {
							m++;
						}
						m++;
					}
					lsResult.add(result);
				}
			}
		}
		return lsResult;
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
}
