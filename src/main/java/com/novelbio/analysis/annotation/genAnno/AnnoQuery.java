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
/**
 * ����ע��
 * @author zong0jie
 *
 */
public class AnnoQuery {
	public static void main(String[] args) {
		String parent = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		annoGeneIDXls(parent + "MEF_WT2vsWT0outDifResult.xls",parent + "mouseMEF_WT2vsWT0outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);
		annoGeneIDXls(parent + "MEF_K02vsK00outDifResult.xls",parent + "mouseMEF_K02vsK00outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);

//		annoGeneIDXls(parent + "mouseMEF_K0vsWT0outDifResult.xls",parent + "mouseMEF_K0vsWT0outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);
//		annoGeneIDXls(parent + "mouseMEF_K2vsWT2outDifResult.xls",parent + "mouseMEF_K2vsWT2outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);
//		annoGeneIDXls(parent + "chickenK0vsWT0outDifResult.xls",parent + "mouseHeartK0vsWT0outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);
//		annoGeneIDXls(parent + "chickenK5vsWT5outDifResult.xls",parent + "mouseHeartK0vsWT0outDifResult_Anno_new.xls",  10090, 1, 1, "",false, 9606);
	}
	/**
	 * 
	 * 
	 * ��arraytools�Ľ�����geneID,û��geneID�򽫱�accID������ȥ
	 * ����ڵ�colNum�еĺ��棬ֱ��д��excel�ļ�
	 * @param txtExcelFile
	 * @param txtOut
	 * @param taxID
	 * @param firstLines
	 * @param colNum ʵ����
	 * @param regx ������ʽ ���Ϊ""���и�
	 * @param blast
	 * @param StaxID
	 */
	public static void annoGeneIDXls(String txtExcelFile, String txtOutFile, int taxID, int firstLines,int colNum,String regex, boolean blast, int StaxID) {
		ArrayList<String[]> lsGeneID = ExcelTxtRead.readLsExcelTxt(txtExcelFile, 1);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		if (firstLines <= 1) {
			firstLines = 1;
		}
		if (firstLines > 1) {
			lsResult.add(getTitle(lsGeneID.get(firstLines - 2), blast));
		}
		for (int i = firstLines - 1; i < lsGeneID.size(); i++) {
			String accID = lsGeneID.get(i)[colNum-1];
			if (regex != null && !regex.equals("")) {
				accID = accID.split(regex)[0];
			}
			String[] tmpResult = null;
			if (blast) {
				tmpResult = getInfoBlast(lsGeneID.get(i), taxID, StaxID, 1e-10, accID);
			}
			else {
				tmpResult = getInfo(lsGeneID.get(i), taxID, accID);
			}
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	private static String[] getTitle(String[] title, boolean blast)
	{
		if (!blast) {
			title = ArrayOperate.copyArray(title, title.length + 2);
			title[title.length - 1] = "Description";
			title[title.length - 2] = "Symbol";
		}
		else {
			title = ArrayOperate.copyArray(title,  title.length + 5);
			title[title.length - 5] = "Description";
			title[title.length - 4] = "Symbol";
			title[title.length - 3] = "Blast_evalue";
			title[title.length - 2] = "Blast_Symbol";
			title[title.length - 1] = "Blast_Description";
		}
		return title;
	}
	/**
	 * ע�����ݣ�����Ҫblast
	 * @param info ����һ����Ϣ
	 * @param taxID ����
	 * @param accColNum �����info���ĸ�column��ʵ��column
	 * @return
	 */
	private static String[] getInfo(String[] info, int taxID, String accID)
	{
		String[] result = ArrayOperate.copyArray(info, info.length + 2);
		result[result.length - 1] = "";
		result[result.length - 2] = "";
		CopedID copedID = new CopedID(accID, taxID);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			return result;
		}
		else {
			result[result.length - 2] = copedID.getSymbol();
			result[result.length - 1] = copedID.getDescription();
		}
		return result;
	}
	/**
	 * ע�����ݣ�����Ҫblast
	 * @param info ����һ����Ϣ
	 * @param taxID ����
	 * @param accColNum �����info���ĸ�column��ʵ��column
	 * @return
	 */
	private static String[] getInfoBlast(String[] info, int taxID, int subTaxID, double evalue, String accID)
	{
		String[] result = ArrayOperate.copyArray(info, info.length + 5);
		result[result.length - 1] = "";result[result.length - 2] = "";
		result[result.length - 3] = "";result[result.length - 4] = "";
		result[result.length - 5] = "";
		CopedID copedID = new CopedID(accID, taxID);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			return result;
		}
		else {
			copedID.setBlastInfo(evalue, subTaxID);
			String[] anno = copedID.getAnno(true);
			result[result.length - 5] = anno[0];
			result[result.length - 4] = anno[1];
			result[result.length - 3] = anno[3];
			result[result.length - 2] = anno[4];
			result[result.length - 1] = anno[5];
		}
		return result;
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
