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
		String parent = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		anno(parent + "mouseHeartK0vsWT0outDifResult.xls",parent + "mouseHeartK0vsWT0outDifResult_Anno.xls",  10090, 1, false, 9606, 1e-10, "");
		anno(parent + "mouseMEF_K0vsWT0outDifResult.xls",parent + "mouseMEFK0vsWT0outDifResult_Anno.xls",  10090, 1, false, 9606, 1e-10, "");
		anno(parent + "mouseMEF_K2vsWT2outDifResult.xls",parent + "mouseMEF_K2vsWT2outDifResult_Anno.xls",  10090, 1, false, 9606, 1e-10, "");
	
		parent = "/media/winF/NBC/Project/Project_FY/chicken/";
		anno(parent + "chickenK0vsWT0outDifResult.xls",parent + "chickenK0vsWT0outDifResult_Anno.xls",  9031, 1, false, 9606, 1e-10, "");
		anno(parent + "chickenK5vsWT5outDifResult.xls",parent + "chickenK5vsWT5outDifResult_Anno.xls",  9031, 1, false, 9606, 1e-10, "");	
	}
	/**
	 * 
	 * ��arraytools�Ľ�����geneID,û��geneID�򽫱�accID������ȥ
	 * ����ڵ�colNum�еĺ��棬ֱ��д��excel�ļ�
	 * <b>��һ��һ���Ǳ�����</b>
	 * ���Ƚ�ָ��accID�е�ÿһ����regx�иȻ�󽫽������geneID����uniID��ֻ�ҵ�һ������geneID��uniID����Ŀ��Ȼ�󽫸�geneID��uniIDװ��excel
	 * @param excelFile
	 * @param taxID
	 * @param colNum ʵ����
	 * @param regx������ʽ ���Ϊ""���и�
	 */
	public static void annoGeneIDXls(String excelFile, int taxID,int colNum,String regx) {
		colNum--;
		ExcelOperate excelAnno = new ExcelOperate();
		excelAnno.openExcel(excelFile);
		//ȫ����ȡ����һ��Ϊtitle
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
	 * ��arraytools�Ľ�����annotation
	 * ����ڵ�colNum�еĺ��棬ֱ��д��excel�ļ�
	 * <b>��һ��һ���Ǳ�����</b>
	 * @param excelFile
	 * @param out
	 * @param taxID
	 * @param colNum ʵ����
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @param regx  ��cell�����Ƿ����һ�����ϵ�ID��Ʃ��accID1,accID2 ����������ģ�����spliteȥ�и��ֻȡ��һ�������Ϊ""���и�
	 */
	public static void anno(String excelFile,String out,int taxID,int colNum,boolean blast,int StaxID,double evalue,String regx) {
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
			CopedID copedID = new CopedID(accID, taxID);
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
