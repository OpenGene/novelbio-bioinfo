package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.RunProcess;
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
import com.novelbio.database.model.modcopeid.GeneID;
/**
 * 批量注释
 * @author zong0jie
 *
 */
public class AnnoQuery extends RunProcess<AnnoQuery.AnnoQueryDisplayInfo>{
//	public static void main(String[] args) {
//		String geneFile = "/media/winF/NBC/Project/RNA-Seq_CR_20111201/CR.xls";
//		String txtOut = FileOperate.changeFileSuffix(geneFile, "_annotest", null);
//		AnnoQuery annoQuery = new AnnoQuery();
//		annoQuery.setColAccIDFrom1(1);
//		annoQuery.setFirstLineFrom1(2);
//		annoQuery.setGeneIDFile(geneFile);
//		annoQuery.writeTo(txtFile);
//	}
	
	ArrayList<String[]> lsGeneID;
	ArrayList<String[]> lsResult;
	int colAccID = 0;
	String regex = "";
	boolean blast = false;
	int taxIDthis = 0;
	int taxIDblastTo = 0;
	
	int firstLine = 0;
	/**
	 * 可以输入txt或excel
	 * @param geneIDfile
	 */
	public void setGeneIDFile(String geneIDfile) {
		lsGeneID = ExcelTxtRead.readLsExcelTxt(geneIDfile, 1);
	}
	/** 输入待查找的gene列表信息 */
	public void setLsGeneID(ArrayList<String[]> lsGeneID) {
		this.lsGeneID = lsGeneID;
	}
	/** 第一行从哪里开始，实际行 */
	public void setFirstLineFrom1(int firstLine) {
		if (firstLine >= 1) {
			this.firstLine = firstLine - 1;
		}
	}
	public void setColAccIDFrom1(int colAccID) {
		this.colAccID = colAccID - 1;
	}
	public ArrayList<String[]> getLsResult() {
		return lsResult;
	}
	public void writeTo(String txtFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtFile, true);
		txtWrite.ExcelWrite(lsResult, "\t", 1, 1);
	}
	public void setTaxIDthis(int taxIDthis) {
		this.taxIDthis = taxIDthis;
	}
	public void setTaxIDblastTo(int taxIDblastTo) {
		this.taxIDblastTo = taxIDblastTo;
	}
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	@Override
	protected void running() {
		anno();
	}
	
	private void anno() {
		lsResult = new ArrayList<String[]>();
		if (firstLine >= 1) {
			lsResult.add(getTitle(lsGeneID.get(firstLine - 1), blast));
		}
		for (int i = firstLine; i < lsGeneID.size(); i++) {
			String accID = lsGeneID.get(i)[colAccID];
			if (regex != null && !regex.equals("")) {
				accID = accID.split(regex)[0];
			}
			String[] tmpResult = null;
			if (blast) {
				tmpResult = getInfoBlast(lsGeneID.get(i), taxIDthis, taxIDblastTo, 1e-10, accID);
			}
			else {
				tmpResult = getInfo(lsGeneID.get(i), taxIDthis, accID);
			}
			lsResult.add(tmpResult);
			
			suspendCheck();
			setRunInfo(i, tmpResult);
			if (flagStop) {
				break;
			}
		}
	}
	
	private void setRunInfo(int num, String[] tmpInfo) {
		AnnoQueryDisplayInfo annoQueryDisplayInfo = new AnnoQueryDisplayInfo();
		annoQueryDisplayInfo.countNum = num;
		annoQueryDisplayInfo.tmpInfo = tmpInfo;
		runGetInfo.setRunningInfo(annoQueryDisplayInfo);
	}
	
	public String[] getTitle() {
		return getTitle(lsGeneID.get(firstLine - 1), blast);
	}
	private static String[] getTitle(String[] title, boolean blast) {
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
	private static String[] getInfo(String[] info, int taxID, String accID) {
		String[] result = ArrayOperate.copyArray(info, info.length + 2);
		result[result.length - 1] = "";
		result[result.length - 2] = "";
		GeneID copedID = new GeneID(accID, taxID);
		if (copedID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
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
	private static String[] getInfoBlast(String[] info, int taxID, int subTaxID, double evalue, String accID) {
		String[] result = ArrayOperate.copyArray(info, info.length + 5);
		result[result.length - 1] = "";result[result.length - 2] = "";
		result[result.length - 3] = "";result[result.length - 4] = "";
		result[result.length - 5] = "";
		GeneID copedID = new GeneID(accID, taxID);
		if (copedID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
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
			GeneID copedID = new GeneID(accID, taxID);
			String[] resString = ArrayOperate.copyArray(string, string.length + 1);
			resString[resString.length - 1] = copedID.getGenUniID();
			txtOut.writefileln(resString);
		}
		txtOut.close();
	}
	
	public static class AnnoQueryDisplayInfo {
		long countNum;
		String[] tmpInfo;
		public void setCountNum(long countNum) {
			this.countNum = countNum;
		}
		public void setTmpInfo(String[] tmpInfo) {
			this.tmpInfo = tmpInfo;
		}
		public long getCountNum() {
			return countNum;
		}
		public String[] getTmpInfo() {
			return tmpInfo;
		}
	}
}


