package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * 常规affy的注释文件，仅导入affy探针，其他注释通通不导入<br>
 * <b>如果导入不进去，考虑将ss[8]放入筛选条件</b><br>
 * 方法：取消那一行的注释
 * <b>导入之前的准备工作</b><br>
 * 1. 将格式调整为tab隔开，去除所有引号<br>
 * 2. 将所有无关ID，以及control探针全部去除
 * 3. 设定从第二列开始导入
 * @author zong0jie
 *
 */
public class NormAffy extends ImportPerLine {

	String dbInfo = "";
	/**
	 * 设定芯片来源，
	 * 如NovelBioConst.DBINFO_ATH_TAIR等
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#")) {
			return true;
		}
		String[] ss = lineContent.split("\t");
		if (ss[0].startsWith("Probe")) {
			return true;
		}
		GeneID geneID = new GeneID(ss[0], taxID);
		geneID.setUpdateDBinfo(dbInfo, true);
		if (!ss[18].equals("---")) {
			String[] ssGeneID = ss[18].split("///");
			geneID.setUpdateGeneID(ssGeneID[0].trim(), GeneID.IDTYPE_GENEID);
		}
		ArrayList<String> lsRefAccID = new ArrayList<String>();
//		addRefAccID(lsRefAccID, ss[8]);
		lsRefAccID.addAll(getLsRefAccID(ss[10])); lsRefAccID.addAll(getLsRefAccID(ss[14])); lsRefAccID.addAll(getLsRefAccID(ss[17]));
		lsRefAccID.addAll(getLsRefAccID(ss[19])); lsRefAccID.addAll(getLsRefAccID(ss[22]));
		lsRefAccID.addAll(getLsRefAccID(ss[23])); lsRefAccID.addAll(getLsRefAccID(ss[25]));
		geneID.setUpdateRefAccID(lsRefAccID);
		return geneID.update(false);
	}
	
	private List<String> getLsRefAccID(String cellInfo) {
		List<String> lsResult = new ArrayList<String>();
 		if (cellInfo.equals("---")) {
			return lsResult;
		}
		else {
			String[] info = cellInfo.split("///");
			for (String string : info) {
				lsResult.add(string);
			}
		}
 		return lsResult;
	}
	
	/**
	 * 给定target的fasta文件，整理成常规fasta文件，然后去和指定物种的序列做blast
	 * @param fastaFile
	 */
	public void toTargetFastaFile(String fastaFile) {
		String regx = "(?<=target:\\w{0,100}:).+?(?=;)";
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFile, regx, false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(fastaFile, "_modified", null));
	}
}
