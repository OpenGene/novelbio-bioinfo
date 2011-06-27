package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.FriceDAO.DaoFCGene2GoInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.service.ServAnno;

public class AnnoQuery {
	/**
	 * 
	 * 给arraytools的结果添加geneID,没有geneID则将本accID附加上去
	 * 添加在第colNum列的后面，直接写入excel文件
	 * <b>第一行一定是标题行</b>
	 * 首先将指定accID列的每一项用regx切割，然后将结果查找geneID或者uniID，只找第一个含有geneID或uniID的项目，然后将该geneID或uniID装入excel
	 * @param excelFile
	 * @param taxID
	 * @param colNum 实际列
	 * @param regx正则表达式 如果为""则不切割
	 */
	public static void annoGeneID(String excelFile, int taxID,int colNum,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//全部读取，第一行为title
		String[][] geneInfo = excelAnno.ReadExcel(1, 1, excelAnno.getRowCount(), excelAnno.getColCount(2));
		ArrayList<String[]> lsgenAno = new ArrayList<String[]>();
		for (int i = 1; i < geneInfo.length; i++) {
			String[] accID = null;
			if (regx.equals("")) {
				accID = new String[1];
				try {
					accID[0] = CopeID.removeDot(geneInfo[i][colNum]);
				} catch (Exception e) {
					accID[0] = "error";
				}
			}
			else {
				try {
					accID = geneInfo[i][colNum].split(regx);
				} catch (Exception e) {
					accID[0] = "error";
				}
				
			}
			String thisaccID = accID[0];
			for (int j = 0; j < accID.length; j++) {
				ArrayList<String> lsTmpaccID = ServAnno.getNCBIUni(CopeID.removeDot(accID[j]), taxID);
				if (!lsTmpaccID.get(0).equals("accID")) {
					thisaccID = lsTmpaccID.get(1);
					break;
				}
			}
			String[] tmpAno = new String[1];
			tmpAno[0] =thisaccID; 
			lsgenAno.add(tmpAno);
		}
		String[][] geneAno = new String[geneInfo.length][lsgenAno.get(0).length];
		geneAno[0][0] = "geneID/uniID";
		for (int i = 1; i < geneAno.length; i++) {
			for (int j = 0; j < geneAno[0].length; j++) {
				geneAno[i][j] = lsgenAno.get(i-1)[j];
			}
		}
		String[][] dataResult = ArrayOperate.combArray(geneInfo, geneAno, colNum+1);
		excelAnno.WriteExcel(1, 1, dataResult);
	}	
	/**
	 * 给arraytools的结果添加annotation
	 * 添加在第colNum列的后面，直接写入excel文件
	 * <b>第一行一定是标题行</b>
	 * @param excelFile
	 * @param taxID
	 * @param colNum 实际列
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param regx 如果为""则不切割
	 */
	public static void anno(String excelFile, int taxID,int colNum,boolean blast,int StaxID,double evalue,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//全部读取，第一行为title
		String[][] geneInfo = excelAnno.ReadExcel(1, 1, excelAnno.getRowCount(), excelAnno.getColCount(2));
		ArrayList<String[]> lsgenAno = new ArrayList<String[]>();
		for (int i = 1; i < geneInfo.length; i++) {
			String[] accID = null;
			if (regx.equals("")) {
				accID = new String[1];
				accID[0] =CopeID.removeDot(geneInfo[i][colNum]);
			}
			else {
				accID = geneInfo[i][colNum].split(regx);
			}
			String thisaccID = geneInfo[i][colNum];
			for (int j = 0; j < accID.length; j++) {
				ArrayList<String> lsTmpaccID = ServAnno.getNCBIUni(CopeID.removeDot(accID[j]), taxID);
				if (!lsTmpaccID.get(0).equals("accID")) {
					thisaccID = accID[j];
					break;
				}
			}
			String[] tmpAno = ServAnno.getAnno(thisaccID,taxID, blast, StaxID, evalue);
			lsgenAno.add(tmpAno);
		}
		String[][] geneAno = new String[geneInfo.length][lsgenAno.get(0).length];
		geneAno[0][0] = "Symbol";geneAno[0][1] = "Description";
		if (blast) {
			geneAno[0][2] = "subjectTaxID"; geneAno[0][3] = "evalue";
			geneAno[0][4] = "symbol"; geneAno[0][5] = "description";
		}
		for (int i = 1; i < geneAno.length; i++) {
			for (int j = 0; j < geneAno[0].length; j++) {
				geneAno[i][j] = lsgenAno.get(i-1)[j];
			}
		}
		String[][] dataResult = ArrayOperate.combArray(geneInfo, geneAno, colNum+1);
		excelAnno.WriteExcel(1, 1, dataResult);
	}
	

	
}
