package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2GoInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.Uni2GoInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
@Deprecated
public class AnnoQuery {
	public static void main(String[] args) {
		String parent = "/media/winE/NBC/Project/Project_Q_Lab/tophat/GO/";
		anno(parent + "2vs0gene_exp.diff.xls",parent + "2txtOut.txt",  4577, 1, true, 3702, 1e-10, "");
		anno(parent + "3vs0gene_exp.diff.xls",parent + "3txtOut.txt",  4577, 1, true, 3702, 1e-10, "");
	}
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
	public static void annoGeneIDXls(String excelFile, int taxID,int colNum,String regx) {
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
	 * 
	 * 给arraytools的结果添加annotation
	 * 添加在第colNum列的后面，直接写入excel文件
	 * <b>第一行一定是标题行</b>
	 * @param excelFile
	 * @param out
	 * @param taxID
	 * @param colNum 实际列
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param regx 如果为""则不切割
	 */
	public static void anno(String excelFile, String out,int taxID,int colNum,boolean blast,int StaxID,double evalue,String regx) {
		colNum--;
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxtFile(excelFile, 1, 1, -1, -1);
		ArrayList<String[]> lsTmpAnno = new ArrayList<String[]>();
		for (int i = 1; i < lsInfo.size(); i++) {
			String accID = "";
			if (regx.equals("")) {
				accID = lsInfo.get(i)[colNum];//CopeID.removeDot(geneInfo[i][colNum]);
			}
			else {
				accID = lsInfo.get(i)[colNum].split(regx)[0];
			}
			CopedID copedID = new CopedID(accID, taxID, false);
			copedID.setBlastInfo(evalue, StaxID);
			String[] out2 = copedID.getAnno(blast);
			lsTmpAnno.add(ArrayOperate.combArray(new String[]{copedID.getAccID()}, out2, 0));
		}
		lsTmpAnno.add(0,ArrayOperate.combArray(new String[]{"AccID"}, CopedID.getTitleAnno(blast), 0));
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		txtOut.ExcelWrite(lsTmpAnno, "\t", 1, 1);
//		ArrayList<String[]> lsResult = ArrayOperate.combArray(lsInfo, lsTmpAnno, colNum+1);
//		ExcelTxtRead.writeLsExcelTxt(excelFile, lsResult, 1, 1, -1, -1);
	}
	
}
