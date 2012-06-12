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
import com.novelbio.base.fileOperate.FileOperate;
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
 * 批量注释
 * @author zong0jie
 *
 */
public class AnnoQuery {
	public static void main(String[] args) {
		String geneFile = "/home/zong0jie/桌面/Gene.xls";
		String out = FileOperate.changeFileSuffix(geneFile, "_geneID", null);
		addGeneID(geneFile, out, 1, 9606);
	}
	/**
	 * 给arraytools的结果添加geneID,没有geneID则将本accID附加上去
	 * 添加在第colNum列的后面，直接写入excel文件
	 * @param txtExcelFile
	 * @param txtOut
	 * @param taxID
	 * @param firstLines
	 * @param colNum 实际列
	 * @param regx 正则表达式 如果为""则不切割
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
			title[title.length - 5] = "Symbol";
			title[title.length - 4] = "Description";
			title[title.length - 3] = "Blast_evalue";
			title[title.length - 2] = "Blast_Symbol";
			title[title.length - 1] = "Blast_Description";
		}
		return title;
	}
	/**
	 * 注释数据，不需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
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
	 * 注释数据，需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
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
	
	public static void addGeneID(String geneFile, String out, int colGeneID, int taxID) {
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		colGeneID--;
		ArrayList<String[]> lsGeneInfo = ExcelTxtRead.readLsExcelTxt(geneFile, 1);
		for (String[] string : lsGeneInfo) {
			String accID = string[colGeneID];
			CopedID copedID = new CopedID(accID, taxID);
			String[] resString = ArrayOperate.copyArray(string, string.length + 1);
			resString[resString.length - 1] = copedID.getGenUniID();
			txtOut.writefileln(resString);
		}
		txtOut.close();
	}
}
