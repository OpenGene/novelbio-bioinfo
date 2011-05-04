package com.novelBio.coexp.simpCoExp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import javax.security.auth.callback.LanguageCallback;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.MathComput;

import DAO.FriceDAO.DaoFCGene2GoInfo;

import entity.friceDB.Gene2GoInfo;
import entity.friceDB.NCBIID;

public class SimpCoExp {
	
	
	
	static String RworkSpace = "/media/winE/Bioinformatics/R/practice_script/platform/coExp";
	/**
	 * 
	 * @param inFile ��ȡexcel�ļ�
	 * @param columnID ��ȡ�ļ���
	 * @param taxID
	 * @param pearsonCutOff
	 * @param pvalueCutOff
	 * @param coExpID 0-4֮�������ûɶ���𣬾���ͬʱ���ж������ʱ�ò�ͬ��ģ�飬������ͬʱ����4��coExp����
	 * @param outFile
	 * @throws Exception
	 */
	public static void getCoExpInfo(String inFile,int[] columnID,int taxID,double pearsonCutOff,double pvalueCutOff,int coExpID,String outFile) throws Exception
	{
		String[][] info = ExcelTxtRead.readExcel(inFile, columnID, 1, 0);
		getData(coExpID,info, taxID, pearsonCutOff, pvalueCutOff, outFile);
	}
	
	
	/**
	 * 
	 * @param inFile ��ȡexcel�ļ�����һ��Ϊ������Ĭ���ļ���ʽΪ
	 * 0: geneID<br>
	 * 1: geneID<br>
	 * 2: pearson<br>
	 * 3: pvalue<br>
	 * 4: fdr<br>
	 * �������һ���µ�excle�ļ�
	 * @param columnID
	 * @param taxID
	 * @param outFile
	 * @throws Exception
	 */
	public static void getCoExpDegree(String inFile,int taxID,String outFile) throws Exception
	{
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(inFile);
		String[][] info = excelOperate.ReadExcel(2, 1, excelOperate.getRowCount(), excelOperate.getColCount(1));
		Object[] obj = annotationScr2Trg(info, taxID);
		 ArrayList<String[]> lsScr2Trg = (ArrayList<String[]>) obj[0];
		 ArrayList<String[]> lsResult = (ArrayList<String[]>) obj[1];
		String[] anoTitle = new String[4];
		anoTitle[0] = "GeneID"; anoTitle[1] = "GeneSymbol"; anoTitle[2] = "Description"; anoTitle[3] = "degree";
		lsResult.add(0, anoTitle);
		
		String[] coExpTitle = new String[9];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "Symbol";coExpTitle[2] = "Description";
		coExpTitle[3] = "GeneID";coExpTitle[4] = "Symbol";coExpTitle[5] = "Description";coExpTitle[6] = "pearson";coExpTitle[7] = "pvalue";coExpTitle[8] = "fdr";
		lsScr2Trg.add(0,coExpTitle);

		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.newExcelOpen(outFile);
		String sheet1 = "GeneInteraction";
		excelCoExp.createNewSheet(sheet1);
		excelCoExp.WriteExcel(sheet1, 1, 1, lsScr2Trg, true);
		String sheet2 = "Attribute";
		excelCoExp.createNewSheet(sheet2);
		excelCoExp.WriteExcel(sheet2, 1, 1, lsResult, true);
	}
	
	/**
	 * 
	 * @param inFile ��ȡexcel�ļ�����һ��Ϊ������Ĭ���ļ���ʽΪ
	 * 0: geneID<br>
	 * 1: geneID<br>
	 * �������һ���µ�excle�ļ�
	 * @param columnID
	 * @param taxID
	 * @param outFile
	 * @throws Exception
	 */
	public static void getCoExpDegreeNormal(String inFile,int taxID,String outFile) throws Exception
	{
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(inFile);
		String[][] info = excelOperate.ReadExcel(2, 1, excelOperate.getRowCount(), excelOperate.getColCount(1));
		Object[] obj = annotationScr2Trg(info, taxID);
		 ArrayList<String[]> lsScr2Trg = (ArrayList<String[]>) obj[0];
		 ArrayList<String[]> lsResult = (ArrayList<String[]>) obj[1];
		String[] anoTitle = new String[4];
		anoTitle[0] = "GeneID"; anoTitle[1] = "GeneSymbol"; anoTitle[2] = "Description"; anoTitle[3] = "degree";
		lsResult.add(0, anoTitle);
		
		String[] coExpTitle = new String[6];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "Symbol";coExpTitle[2] = "Description";
		coExpTitle[3] = "GeneID";coExpTitle[4] = "Symbol";coExpTitle[5] = "Description";
		lsScr2Trg.add(0,coExpTitle);

		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.newExcelOpen(outFile);
		String sheet1 = "GeneInteraction";
		excelCoExp.createNewSheet(sheet1);
		excelCoExp.WriteExcel(sheet1, 1, 1, lsScr2Trg, true);
		String sheet2 = "Attribute";
		excelCoExp.createNewSheet(sheet2);
		excelCoExp.WriteExcel(sheet2, 1, 1, lsResult, true);
	}
	
	/**
	 * ����ԭʼ���ݣ�����Ҫ������ѡ������������R��ü򵥹�������
	 * @param rawData ��һ��ΪgeneID,����ͨ������Ҿ�����Ϣ������Ϊgene����ֵ����һ������Ϣ����title
	 * @param RworkSpace ���ĸ��ļ����µ���R����������Ϣ�����ڸ��ļ����µ�/coExpression/���ļ�����,�������ν�Ƿ�ӡ�/��
	 * @throws Exception 
	 */
	private static void getData(int coexpID,String[][] rawData,int taxID,double pearsonCutOff,double pvalueCutOff,String outFile) throws Exception 
	{
		if (!RworkSpace.endsWith(File.separator)) {  
			RworkSpace = RworkSpace + File.separator;  
		}
		//��rawDataע���ϣ�û��symbol��description��ͨͨȥ�������������lsRawData��
		ArrayList<String[]> lsRawData = new ArrayList<String[]>();
		for (int i = 0; i < rawData.length; i++) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(rawData[i][0]);
			if (taxID>0) 	ncbiid.setTaxID(taxID);
			ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
			String[] tmpResult = null;
			if (lsGene2GoInfos != null && lsGene2GoInfos.size() > 0) 
			{
				////////////////////////////////////////////////////////////////////////////////
				Gene2GoInfo gene2GoInfo = lsGene2GoInfos.get(0);
				rawData[i][0] = gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
				lsRawData.add(rawData[i]);
			}
		}
		
		TxtReadandWrite txtSimpCoExp = new TxtReadandWrite();
		txtSimpCoExp.setParameter(RworkSpace+"tmp"+coexpID+"/Data.txt", true, false);
		txtSimpCoExp.ExcelWrite(lsRawData, "\t", 1, 1);
		txtSimpCoExp.setParameter(RworkSpace+"tmp"+coexpID+"/parameter.txt", true, false);
		txtSimpCoExp.writefile(pearsonCutOff+" "+pvalueCutOff);
		getRcoExp(RworkSpace,coexpID);
		txtSimpCoExp.setParameter(RworkSpace+"tmp"+coexpID+"/result.txt", false, true);
		//���pearson��õ����ݣ���һ��Ϊ���򣬵ڶ���Ϊ���򣬵����У�pearsonֵ�������� pvalue�������� fdr��ע���������Ҫȥ�����е�����
		String[][] result = txtSimpCoExp.ExcelRead("\t", 2, 2, txtSimpCoExp.ExcelRows(), txtSimpCoExp.ExcelColumns("\t"));
		ArrayList<String[]> lsCoExpValue = new ArrayList<String[]>();
		for (String[] strings : result) {
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].replace("\"", "");
			}
			lsCoExpValue.add(strings);
		}
		//��pvalue����
        Collections.sort(lsCoExpValue,new Comparator<String[]>(){
			public int compare(String[] arg0, String[] arg1) {
				if (Double.parseDouble(arg0[3])<Double.parseDouble(arg1[3]) ) {
					return -1;
				}
				else if (Double.parseDouble(arg0[3])==Double.parseDouble(arg1[3])) {
					return 0;	
				}
				else {
					return 1;
				}
			}
        });
        ArrayList<String[]> lsCoExpResult = new ArrayList<String[]>();
        if (lsCoExpValue.size()>50000) {
        	result = new String[50000][5];
        	for (int i = 0; i < result.length; i++) {
				result[i] = lsCoExpValue.get(i);
				lsCoExpResult.add(result[i]);
			}
		}
        else {
			result = new String[lsCoExpValue.size()][5];
			for (int i = 0; i < lsCoExpValue.size(); i++) {
				result[i] = lsCoExpValue.get(i);
				lsCoExpResult.add(result[i]);
			}
		}
        
		//�ӱ���
		String[] coExpTitle = new String[5];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "GeneID";coExpTitle[2] = "pearson";coExpTitle[3] = "pvalue";coExpTitle[4] = "fdr";
		lsCoExpResult.add(0,coExpTitle);

		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.openExcel(outFile, false);
		String sheet1 = "GeneInteraction";
		excelCoExp.createNewSheet(sheet1);
		excelCoExp.WriteExcel(sheet1, 1, 1, lsCoExpResult,true);
		/**
		��һ��������attribute
			ArrayList<String[]> lsResult = annotation(result, taxID);
			String[] anoTitle = new String[4];
			anoTitle[0] = "GeneID"; anoTitle[1] = "GeneSymbol"; anoTitle[2] = "Description"; anoTitle[3] = "degree";
			lsResult.add(0, anoTitle);
		String sheet2 = "Attribute";
		excelCoExp.createNewSheet(sheet1);
		excelCoExp.WriteExcel(sheet2, 1, 1, lsResult, true);
	  **/
	 
	}
	
	
	
	private static void getRcoExp(String RworkSpace,int coExpID) throws Exception{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+RworkSpace+ "simpleCoExpTmp"+coExpID+".R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
	
	/**
	 * �����Ѿ���õ�ֵ������ע��,ͬʱ��result[][]�����pvalue���¼���fdr
	 * @param result
	 * 0: geneID
	 * 1: geneID
	 * 2: pearson
	 * 3: pvalue
	 * 4: fdr
	 * @return 
	 * ����ArrayList-String[]
	 * 0: GeneID
	 * 1: GeneSymbol
	 * 2: Description
	 * 3: degree
	 * @throws Exception 
	 */
	private static ArrayList<String[]> annotation(String[][] result,int taxID) throws Exception
	{
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (int i = 0; i < result.length; i++) {
			lsPvalue.add(Double.parseDouble(result[i][3]));
		}
		ArrayList<Double> lsFdr = MathComput.pvalue2Fdr(lsPvalue);
		/**
		 * key accID
		 * value 0:accID 1:symbol 2: Description 3:degree
		 */
		Hashtable<String,String[]> hashAccID = new Hashtable<String, String[]>();
		for (int i = 0; i < result.length; i++) 
		{
			result[i][4] = lsFdr.get(i)+"";
			//��geneID1װ��hash��������degree
			if (hashAccID.containsKey(result[i][0])) {
				String[] tmpResult = hashAccID.get(result[i][0]);
				tmpResult[3] = Integer.parseInt(tmpResult[3]) +1 +"";
			}
			else 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(result[i][0]);
				if (taxID>0) 	ncbiid.setTaxID(taxID);
				ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
				String[] tmpResult = null;
				if (lsGene2GoInfos != null && lsGene2GoInfos.size() > 0) 
				{
					///////////////////////////////////////////////////////////////////////////////////
					//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
					//����if�⣺�������ݿ����Ƿ��ж�����
					tmpResult = new String[4];
					tmpResult[0] = result[i][0];tmpResult[1] = ""; tmpResult[2] = "";
					tmpResult[3] = 1+"";
					////////////////////////////////////////////////////////////////////////////////
					Gene2GoInfo gene2GoInfo = lsGene2GoInfos.get(0);
					tmpResult[1] = gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
					tmpResult[2] = gene2GoInfo.getGeneInfo().getDescription();
					hashAccID.put(result[i][0], tmpResult);//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
				}
				//hashAccID.put(result[i][0], tmpResult);//����if�⣺�������ݿ����Ƿ��ж�����
			}
			//��geneID2װ��hash��������degree
			if (hashAccID.containsKey(result[i][1])) {
				String[] tmpResult = hashAccID.get(result[i][1]);
				tmpResult[3] = Integer.parseInt(tmpResult[3]) +1 +"";
			}
			else 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(result[i][1]);
				if (taxID>0) 	ncbiid.setTaxID(taxID);
				ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
				if (lsGene2GoInfos != null && lsGene2GoInfos.size() > 0) 
				{
					///////////////////////////////////////////////////////////////////////////////////
					//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
					//����if�⣺�������ݿ����Ƿ��ж�����
					String[] tmpResult = new String[4];
					tmpResult[0] = result[i][1];tmpResult[1] = ""; tmpResult[2] = "";
					tmpResult[3] = 1+"";
					hashAccID.put(result[i][1], tmpResult);
					////////////////////////////////////////////////////////////////////////////////
					Gene2GoInfo gene2GoInfo = lsGene2GoInfos.get(0);
					tmpResult[1] = gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
					tmpResult[2] = gene2GoInfo.getGeneInfo().getDescription();
				}
			}
		}
		
		Enumeration<String> keys=hashAccID.keys();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		while(keys.hasMoreElements()){
			String key=keys.nextElement();
			String[] tmpResult = hashAccID.get(key);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	
	/**
	 * ����src2target������ע�ͣ�����scr��trg�Ƿ������ݿ���ڣ������м���
	 * @param result
	 * 0: geneID
	 * 1: geneID
	 * 2: pearson
	 * 3: pvalue
	 * 4: fdr
	 * @return 
	 * ���� object[2]<br>
	 * <b>0:ArrayListString[n]</b><br>
	 * 0:scr<br>
	 * 1:scrSymbol<br>
	 * 2:scrDescription<br>
	 * 3:trg<br>
	 * 4:trgSymbol<br>
	 * 5:trgDescription<br>
	 * 6: pearson
	 * 7: pvalue
	 * 8: fdr
	 * <b>1 ArrayList-String[4]</b><br>
	 * 0: GeneID<br>
	 * 1: GeneSymbol<br>
	 * 2: Description<br>
	 * 3: degree<br>
	 * @throws Exception 
	 */
	private static Object[] annotationScr2Trg(String[][] result,int taxID) throws Exception
	{
		/**
		 * key accID
		 * value 0:accID 1:symbol 2: Description 3:degree
		 */
		Hashtable<String,String[]> hashAccID = new Hashtable<String, String[]>();
		for (int i = 0; i < result.length; i++) 
		{
			//��geneID1װ��hash��������degree
			if (hashAccID.containsKey(result[i][0])) {
				String[] tmpResult = hashAccID.get(result[i][0]);
				tmpResult[3] = Integer.parseInt(tmpResult[3]) +1 +"";
			}
			else 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(result[i][0]);
				if (taxID>0) 	ncbiid.setTaxID(taxID);
				ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
				String[] tmpResult = null;
				///////////////////////////////////////////////////////////////////////////////////
				//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
				//����if�⣺�������ݿ����Ƿ��ж�����
				tmpResult = new String[4];
				tmpResult[0] = result[i][0];tmpResult[1] = ""; tmpResult[2] = "";
				tmpResult[3] = 1+"";
				hashAccID.put(result[i][0], tmpResult);//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
				////////////////////////////////////////////////////////////////////////////////
				if (lsGene2GoInfos != null && lsGene2GoInfos.size() > 0) 
				{
					Gene2GoInfo gene2GoInfo = lsGene2GoInfos.get(0);
					tmpResult[1] = gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
					tmpResult[2] = gene2GoInfo.getGeneInfo().getDescription();
				}
				//hashAccID.put(result[i][0], tmpResult);//����if�⣺�������ݿ����Ƿ��ж�����
			}
			//��geneID2װ��hash��������degree
			if (hashAccID.containsKey(result[i][1])) {
				String[] tmpResult = hashAccID.get(result[i][1]);
				tmpResult[3] = Integer.parseInt(tmpResult[3]) +1 +"";
			}
			else 
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(result[i][1]);
				if (taxID>0) 	ncbiid.setTaxID(taxID);
				ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
				///////////////////////////////////////////////////////////////////////////////////
				//��һ�η�if�ֻ�е����ݿ�����ʱ�ż���
				//����if�⣺�������ݿ����Ƿ��ж�����
				String[] tmpResult = new String[4];
				tmpResult[0] = result[i][1];tmpResult[1] = ""; tmpResult[2] = "";
				tmpResult[3] = 1+"";
				hashAccID.put(result[i][1], tmpResult);
				////////////////////////////////////////////////////////////////////////////////
				if (lsGene2GoInfos != null && lsGene2GoInfos.size() > 0) 
				{
					Gene2GoInfo gene2GoInfo = lsGene2GoInfos.get(0);
					tmpResult[1] = gene2GoInfo.getGeneInfo().getSymbol().split("//")[0];
					tmpResult[2] = gene2GoInfo.getGeneInfo().getDescription();
				}
				
			}
		}
		String[][] scrAnno = new String[result.length][2];//scr��annotation
		String[][] trgAnno = new String[result.length][2];//trg��annotation
		for (int j = 0; j < result.length; j++) {
			hashAccID.get(result);
		}
		
		
		
		Enumeration<String> keys=hashAccID.keys();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		while(keys.hasMoreElements()){
			String key=keys.nextElement();
			String[] tmpResult = hashAccID.get(key);
			lsResult.add(tmpResult);
		}
		ArrayList<String[]> lsScr2Trg = new ArrayList<String[]>();
		for (String[] strings : result) {
			String[] tmpScr2Trg= new String[strings.length+4];
			tmpScr2Trg[0] = strings[0];
			tmpScr2Trg[1] = hashAccID.get(strings[0])[1];
			tmpScr2Trg[2] = hashAccID.get(strings[0])[2];
			tmpScr2Trg[3] = strings[1];
			tmpScr2Trg[4] = hashAccID.get(strings[1])[1];
			tmpScr2Trg[5] = hashAccID.get(strings[1])[2];
			for (int j = 6; j < tmpScr2Trg.length; j++) {
				tmpScr2Trg[6] = strings[6-4];
			}
			lsScr2Trg.add(tmpScr2Trg);
		}
		Object[] obj = new Object[2];
		obj[0] = lsScr2Trg;
		obj[1] = lsResult;
		
		return obj;
	}
	
	
	
	
	
	
	
}