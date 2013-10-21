package com.novelbio.analysis.coexp.simpCoExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.modgeneid.GeneID;


public class SimpCoExp {
	private static Logger logger = Logger.getLogger(SimpCoExp.class);
	/**
	 * 
	 * @param inFile
	 * @param columnID
	 * @param taxID
	 * @param pvalueCutOff
	 * @param outFile
	 * @param filterNoDB
	 * @throws Exception
	 */
	public static void getCoExpInfo(String inFile,int[] columnID,int taxID,double pvalueCutOff,String outFile, boolean filterNoDB) throws Exception
	{
		ArrayList<String[]> info = ExcelTxtRead.readLsExcelTxt(inFile, columnID, 1, -1);
		getData(info, taxID, pvalueCutOff, outFile,filterNoDB);
	}
	
	
	/**
	 * 
	 * @param inFile 读取excel文件，第一行为标题行默认文件格式为
	 * 0: geneID<br>
	 * 1: geneID<br>
	 * 2: pearson<br>
	 * 3: pvalue<br>
	 * 4: fdr<br>
	 * 结果产生一个新的excle文件
	 * @param columnID
	 * @param taxID
	 * @param outFile
	 * @throws Exception
	 */
	public static void getCoExpDegree(String inFile,int taxID,String outFile) throws Exception {
		ArrayList<String[]> lsinfo = ExcelTxtRead.readLsExcelTxt(inFile, 2);
		Object[] obj = annotationScr2Trg(lsinfo, taxID);
		ArrayList<String[]> lsScr2Trg = (ArrayList<String[]>) obj[0];
		ArrayList<String[]> lsResult = (ArrayList<String[]>) obj[1];
		String[] anoTitle = new String[6];
		anoTitle[0] = "GeneID"; anoTitle[1] = "GeneSymbol"; anoTitle[2] = "Description"; anoTitle[3] = "InDegree";anoTitle[4] = "OutDegree";anoTitle[5] = "Degree";
		lsResult.add(0, anoTitle);
		
		String[] coExpTitle = new String[9];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "Symbol";coExpTitle[2] = "Description";
		coExpTitle[3] = "GeneID";coExpTitle[4] = "Symbol";coExpTitle[5] = "Description";coExpTitle[6] = "pearson";coExpTitle[7] = "pvalue";coExpTitle[8] = "fdr";
		lsScr2Trg.add(0,coExpTitle);

		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.newExcelOpen(outFile);
		String sheet1 = "GeneInteraction";
		excelCoExp.WriteExcel(sheet1, 1, 1, lsResult);
		String sheet2 = "Attribute";
		excelCoExp.WriteExcel(sheet2, 1, 1, lsScr2Trg);
	}
	
	/**
	 * 
	 * @param inFile 读取excel文件，第一行为标题行默认文件格式为
	 * 0: geneID<br>
	 * 1: geneID<br>
	 * 结果产生一个新的excle文件
	 * @param columnID
	 * @param taxID
	 * @param outFile
	 * @throws Exception
	 */
	public static void getCoExpDegreeNormal(String inFile,int taxID,String outFile) throws Exception {
		ArrayList<String[]> lsinfo = ExcelTxtRead.readLsExcelTxt(inFile, 2);
		Object[] obj = annotationScr2Trg(lsinfo, taxID);
		 ArrayList<String[]> lsScr2Trg = (ArrayList<String[]>) obj[0];
		 ArrayList<String[]> lsResult = (ArrayList<String[]>) obj[1];
		String[] anoTitle = new String[6];
		anoTitle[0] = "GeneID"; anoTitle[1] = "GeneSymbol"; anoTitle[2] = "Description"; anoTitle[3] = "InDegree";anoTitle[4] = "OutDegree";anoTitle[5] = "Degree";
		lsResult.add(0, anoTitle);
		
		String[] coExpTitle = new String[6];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "Symbol";coExpTitle[2] = "Description";
		coExpTitle[3] = "GeneID";coExpTitle[4] = "Symbol";coExpTitle[5] = "Description";
		lsScr2Trg.add(0,coExpTitle);

		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.newExcelOpen(outFile);
		String sheet1 = "GeneInteraction";
		excelCoExp.WriteExcel(sheet1, 1, 1, lsScr2Trg);
		String sheet2 = "Attribute";
		excelCoExp.WriteExcel(sheet2, 1, 1, lsResult);
	}
	
	/**
	 * 
	 * 给定原始数据，将需要的列挑选出来，并调用R获得简单共表达结果
	 * @param rawData 第一列为geneID,最后可通过其查找具体信息。后几列为gene表达值，第一行有信息，是title
	 * @param taxID
	 * @param pearsonCutOff
	 * @param pvalueCutOff
	 * @param outFile
	 * @param filterNoDB 是否将没有db的过滤掉
	 * @throws Exception
	 */
	private static void getData(ArrayList<String[]> rawData,int taxID,double pvalueCutOff,String outFile, boolean filterNoDB) throws Exception 
	{
		ArrayList<CoexpGenInfo> lsCoexpInfo = new ArrayList<CoexpGenInfo>();
		//将rawData注释上，没有symbol和description的通通去除，结果保存在lsRawData中
		for (int i = 1; i < rawData.size(); i++) {
			double[] dou = new double[rawData.get(0).length - 1]; //获得每一行的表达值
			for (int j = 0; j < dou.length; j++) {
				dou[j] = Double.parseDouble(rawData.get(i)[j+1]);
			}
			CoexpGenInfo coexpGenInfo = new CoexpGenInfo(rawData.get(i)[0], taxID, dou);
			if (filterNoDB && coexpGenInfo.getCopedID().getAccID().equals(GeneID.IDTYPE_ACCID)) {
				continue;
			}
			lsCoexpInfo.add(coexpGenInfo); //如果有注释信息，则装入list
		}
		
		
		//获得pearson算好的内容，第一列为基因，第二列为基因，第三列：pearson值，第四列 pvalue，第五列 fdr，注意后续处理要去除其中的引号
		ArrayList<String[]> lsCoExpValue = calCoExp(lsCoexpInfo, pvalueCutOff,filterNoDB);
		//安pvalue排序
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
        List<String[]> lsCoExpResult =  null;
        if (lsCoExpValue.size() > 1500000) {
        	 lsCoExpResult = lsCoExpValue.subList(0, 1500000);
		}
        else {
        	 lsCoExpResult = lsCoExpValue;
		}
        
		//加标题
		String[] coExpTitle = new String[5];
		coExpTitle[0] = "GeneID";coExpTitle[1] = "GeneID";coExpTitle[2] = "pearson";coExpTitle[3] = "pvalue";coExpTitle[4] = "fdr";
		lsCoExpResult.add(0,coExpTitle);

//		ExcelOperate excelCoExp = new ExcelOperate();
//		excelCoExp.openExcel(outFile, false);
//		String sheet1 = "GeneInteraction";
//		excelCoExp.WriteExcel(sheet1, 1, 1, lsCoExpResult);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (String[] strings : lsCoExpResult) {
			txtOut.writefileln(strings);
		}
		txtOut.close();
	}
	/**
	 * 获得pearson算好的内容，第一列为基因，第二列为基因，第三列：pearson值，第四列 pvalue，第五列 fdr，注意后续处理要去除其中的引号
	 * @param lsCoexpGenInfos
	 */
	private static ArrayList<String[]> calCoExp(List<CoexpGenInfo> lsCoexpGenInfos , double pvalueFilter,boolean filterNoDB) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		CoexPair.setFirst(lsCoexpGenInfos);
		for (int i = 0; i < lsCoexpGenInfos.size() -1 ; i++) {
			for (int j = i+1 ; j < lsCoexpGenInfos.size(); j++) {
				CoexPair coexPair = new CoexPair(lsCoexpGenInfos.get(i), lsCoexpGenInfos.get(j));
				if (coexPair.getPvalue() > pvalueFilter) {
					continue;
				}
				String[] tmpresult = new String[4];
				if (!filterNoDB) {
					tmpresult[0] = lsCoexpGenInfos.get(i).getCopedID().getAccID();
					tmpresult[1] = lsCoexpGenInfos.get(j).getCopedID().getAccID();
				}
				else {
					tmpresult[0] = lsCoexpGenInfos.get(i).getCopedID().getSymbol();
					tmpresult[1] = lsCoexpGenInfos.get(j).getCopedID().getSymbol();
				}
//				tmpresult[0] = lsCoexpGenInfos.get(i).getCopedID().getSymbo();
//				tmpresult[1] = lsCoexpGenInfos.get(j).getCopedID().getSymbo();

				tmpresult[2] = coexPair.getCorValue()+"";
				tmpresult[3] = coexPair.getPvalue()+"";
				lsResult.add(tmpresult);
			}
		}
		ArrayList<String[]> lsResultFdr = addFdr(lsResult, 4);
		return lsResultFdr;
	}
	
	/**
	 * 指定某一列，计算该列pvalue所对应的fdr并装入list
	 * @param lsInfo 输入数据，某一列应该是pvalue
	 * @param colNum 实际列
	 * @return
	 */
	private static ArrayList<String[]> addFdr(ArrayList<String[]> lsInfo, int colNum)
	{
		colNum -- ;
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (String[] strings : lsInfo) {
			lsPvalue.add(Double.parseDouble(strings[colNum]));
		}
		ArrayList<Double> lsfdr = MathComput.pvalue2Fdr(lsPvalue);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < lsInfo.size(); i++) {
			String[] tmpResult = new String[lsInfo.get(i).length+1];
			for (int j = 0; j < lsInfo.get(i).length; j++) {
				tmpResult[j] = lsInfo.get(i)[j];
			}
			tmpResult[tmpResult.length-1] = lsfdr.get(i)+"";
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	/**
	 * 给定src2target，进行注释，不管scr和trg是否在数据库存在，都进行计数
	 * @param result
	 * 0: geneID
	 * 1: geneID
	 * 2: pearson
	 * 3: pvalue
	 * 4: fdr
	 * @return 
	 * 返回 object[2]<br>
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
	 * 3: InDegree<br>
	 * 4: OutDegree<br>
	 * 5: Degree
	 * @throws Exception 
	 */
	private static Object[] annotationScr2Trg(ArrayList<String[]> result,int taxID) throws Exception {
		/**
		 * key accID
		 * value 0:accID 1:symbol 2: Description 3:InDegree 4:OutDegree 5:AllDegree
		 */
		Hashtable<String,String[]> hashAccID = new Hashtable<String, String[]>();
		for (int i = 0; i < result.size(); i++) {
			//将geneID1装入hash，并计算degree
			if (hashAccID.containsKey(result.get(i)[0])) {
				String[] tmpResult = hashAccID.get(result.get(i)[0]);
				tmpResult[4] = Integer.parseInt(tmpResult[4]) +1 +"";
				tmpResult[5] = Integer.parseInt(tmpResult[5]) +1 +"";
			}
			else {
				GeneID copedID = new GeneID(result.get(i)[0], taxID);
				String[] tmpResult = new String[6];
				///////////////////////////////////////////////////////////////////////////////////
				//这一段放if里：只有当数据库中有时才计数
				//放在if外：不管数据库中是否含有都计数
				tmpResult[0] = result.get(i)[0];tmpResult[1] = copedID.getSymbol(); tmpResult[2] = copedID.getDescription();
				tmpResult[3] = 0+"";tmpResult[4] = 1+"";tmpResult[5] = 1+"";
				hashAccID.put(result.get(i)[0], tmpResult);//这一段放if里：只有当数据库中有时才计数
				////////////////////////////////////////////////////////////////////////////////
			}
			
			//将geneID2装入hash，并计算degree
			if (hashAccID.containsKey(result.get(i)[1])) {
				String[] tmpResult = hashAccID.get(result.get(i)[1]);
				tmpResult[3] = Integer.parseInt(tmpResult[3]) +1 +"";
				tmpResult[5] = Integer.parseInt(tmpResult[5]) +1 +"";
			}
			else {
				GeneID copedID = new GeneID(result.get(i)[1], taxID, false);
				///////////////////////////////////////////////////////////////////////////////////
				//这一段放if里：只有当数据库中有时才计数
				//放在if外：不管数据库中是否含有都计数
				String[] tmpResult = new String[6];
				tmpResult[0] = result.get(i)[1];tmpResult[1] = copedID.getSymbol(); tmpResult[2] = copedID.getDescription();
				tmpResult[3] = 1+"";tmpResult[4] = 0+"";tmpResult[5] = 1+"";
				hashAccID.put(result.get(i)[1], tmpResult);
			}
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
