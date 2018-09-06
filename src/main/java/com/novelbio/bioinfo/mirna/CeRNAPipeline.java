package com.novelbio.bioinfo.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.tools.compare.CompareSimple;

/**
 * piRNA的分析流程，代码没有优化，仅仅是完成业务逻辑。
 * 如果以后没有大的调整不打算进行优化
 * @author zong0jie
 * @data 2016年7月25日
 */
public class CeRNAPipeline {
	String miRNAdif;
	
	String circRNAexp;
	String circRNAdif;
	String circMirTarget;
	
	String lncRNAdif;
	String lncMirTarget;

	String mRNAdif;
	String mrnaMirTarget;

	/** 结尾需要有"/" */
	String outpath;
	String prefix = "";
	
	
	public static void main(String[] args) {
		String inPath = "/hdfs:/nbCloud/staff/zongjie/ErpFile/";
		CeRNAPipeline ceRNAPipeline = new CeRNAPipeline("/hdfs:/nbCloud/staff/zongjie/ErpFile/out/");
		String circRNAexp = inPath + "Expression.CircRNA.txt";
		String circRNAdif = inPath + "GvsN.diff .CircRNA.txt";
		String lncRNAdif = inPath + "GvsN.diff.ncRNA.txt";
		String miRNAdif = inPath + "GvsN.diff .miRNA.txt";
		String mRNAdif = inPath + "GvsN.diff.mRNA.txt";
		
		ceRNAPipeline.setCircRNAexpAndDif(circRNAexp, circRNAdif);
		ceRNAPipeline.setLncRNAexpAndDif(lncRNAdif);
		ceRNAPipeline.setMiRNAexpAndDif(miRNAdif);
		ceRNAPipeline.setmRNAexpAndDif(mRNAdif);
		
		String mirCircTarget = inPath + "GvsN.miranda_miRNA_CircRNA.txt";
		String mirLncTarget = inPath + "GvsN.miranda_miRNA_ncRNA.txt";
		String mirMRNATarget = inPath + "GvsN.miranda_miRNA_mRNA.txt";

		ceRNAPipeline.setCircMirTarget(mirCircTarget);
		ceRNAPipeline.setLncMirTarget(mirLncTarget);
		ceRNAPipeline.setMrnaMirTarget(mirMRNATarget);
		
		ceRNAPipeline.intersection();
		ceRNAPipeline.combineMiRNA2TargetRNA();
		ceRNAPipeline.negativeAnalysis();
		ceRNAPipeline.ceRNA();
	}
	
	public CeRNAPipeline(String outPath) {
		this.outpath = FileOperate.addSep(outPath);
		FileOperate.createFolders(outpath);
		FileOperate.createFolders(outpath + ".tmp/");
	}
	
	private String getOutputTmp() {
		return outpath + ".tmp/";
	}
	
	public void setMiRNAexpAndDif(String miRNAdif) {
		FileOperate.validateFileExistAndBigThan0(miRNAdif);

		this.miRNAdif = getOutputTmp() + FileOperate.getFileName(miRNAdif);
		removeDifGeneExpCounts(miRNAdif, this.miRNAdif, "miRNA");
	}
	
	public void setCircRNAexpAndDif(String circRNAexp, String circRNAdif) {
		FileOperate.validateFileExistAndBigThan0(circRNAexp);
		FileOperate.validateFileExistAndBigThan0(circRNAdif);
		
		this.circRNAexp = getOutputTmp() + FileOperate.getFileName(circRNAexp);
		removeExpCounts(circRNAexp, this.circRNAexp, "GeneName");
		this.circRNAdif = getOutputTmp() + FileOperate.getFileName(circRNAdif);
		removeDifGeneExpCounts(circRNAdif, this.circRNAdif, "CircRNA");
	}
	
	public void setLncRNAexpAndDif(String lncRNAdif) {
		FileOperate.validateFileExistAndBigThan0(lncRNAdif);

		this.lncRNAdif = getOutputTmp() + FileOperate.getFileName(lncRNAdif);
		removeDifGeneExpCounts(lncRNAdif, this.lncRNAdif, "ncRNA");
	}
	
	public void setmRNAexpAndDif(String mRNAdif) {
		FileOperate.validateFileExistAndBigThan0(mRNAdif);

		this.mRNAdif = getOutputTmp() + FileOperate.getFileName(mRNAdif);
		removeDifGeneExpCounts(mRNAdif, this.mRNAdif, "mRNA");
	}
	
	public void setCircMirTarget(String circMirTarget) {
		this.circMirTarget = getOutputTmp() + FileOperate.getFileName(circMirTarget);
		renameCirc(circMirTarget, this.circMirTarget);
	}
	public void setLncMirTarget(String lncMirTarget) {
		this.lncMirTarget = getOutputTmp() + FileOperate.getFileName(lncMirTarget);
		changeMirTargetName(lncMirTarget, this.lncMirTarget, "SubjectID", "ncRNA");
	}
	public void setMrnaMirTarget(String mrnaMirTarget) {
		this.mrnaMirTarget = getOutputTmp() + FileOperate.getFileName(mrnaMirTarget);
		changeMirTargetName(mrnaMirTarget, this.mrnaMirTarget, "SubjectID", "mRNA");
	}
	
	private void changeMirTargetName(String inFile, String outFile, String changeName, String changeTo) {
		String title = TxtReadandWrite.readFirstLine(inFile);
		String[] ss = title.split("\t");
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].equals(changeName)) {
				ss[i] = changeTo;
			}
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(inFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.writefileln(ss);
		for (String content : txtRead.readlines(2)) {
			txtWrite.writefileln(content);
		}
		
		txtRead.close();
		txtWrite.close();
	}
	
	/** 把miRNAtarget中的circ的名字改掉。本来是类似
	 * chr12_97483430_97561047_chr12_97561047_97483431_-77616_RMST
	 * 删除前面的
	 * chr12_97483430_97561047_
	 * 修改为
	 * chr12_97561047_97483431_-77616_RMST
	 */
	private void renameCirc(String circFileIn, String circFileOut) {
		int colNum = getTargetNameCol(circFileIn, "CircRNA", "SubjectID") - 1;
		TxtReadandWrite txtRead = new TxtReadandWrite(circFileIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(circFileOut, true);
		int i = 0;
		for (String content : txtRead.readlines()) {
			if (i++ == 0) {
				
				String[] ss = content.split("\t");
				ss[0] = "miRNA";
				for (int m = 0; m < ss.length; m++) {
					if (ss[m].equals("SubjectID")) {
						ss[m] = "CircRNA";
					}
				}
				
				txtWrite.writefileln(ss);
				continue;
			}
			String[] ss = content.split("\t");
//			ss[colNum] = changeName(ss[colNum]);
			txtWrite.writefileln(ss);
		}
		txtRead.close();
		txtWrite.close();
	}
	PatternOperate patternOperate = new PatternOperate("[a-z,A-Z]+_{0,1}\\d*_\\d+_\\d+_-{0,1}\\d+");

	/** 把miRNAtarget中的circ的名字改掉。本来是类似
	 * chr12_97483430_97561047_chr12_97561047_97483431_-77616_RMST
	 * 删除前面的
	 * chr12_97483430_97561047_
	 * 修改为
	 * chr12_97561047_97483431_-77616_RMST
	 */
	private String changeName(String circSeqName) {
		List<String> lsPat = patternOperate.getPat(circSeqName);
		return lsPat.get(0);
	}
	
	/** 第一步，把基因注释信息与差异基因取交集 */
	public void intersection() {
		String outPath = outpath + prefix;
		FileOperate.createFolders(FileOperate.getPathName(outPath));
		intersection(true, circRNAexp, circRNAdif, outPath + "circRNAtable.xls", getTargetNameCol(circRNAexp, "CircId"), 1);
	}
	
	/** 第二步，把miRNA的差异信息比对到靶基因上去 */
	public void combineMiRNA2TargetRNA() {
		String outPath = outpath + "TargetPredict/" + prefix;
		FileOperate.createFolders(FileOperate.getPathName(outPath));

		intersection(true, miRNAdif, circMirTarget, outPath + "miRNA-TargetCirc.xls");
		intersection(true, miRNAdif, lncMirTarget, outPath + "miRNA-TargetLnc.xls");
		intersection(true, miRNAdif, mrnaMirTarget, outPath + "miRNA-TargetmRNA.xls");
	}
	
	/** 第三步，miRNA与相应的基因做负相关分析，也就是把miRNA上调，但是circRNA下调这种基因挑选出来 */
	public void negativeAnalysis() {
		String outPathDif = outpath + prefix;
		String outPathTarget = outpath + "TargetPredict/" + prefix;
		
		String outPath = outpath + "NegativeAnalysis/";
		FileOperate.createFolders(FileOperate.getPathName(outPath));

		negativeAnalysis(outPathTarget + "miRNA-TargetCirc.xls", outPathDif + "circRNAtable.xls", outPath + "mirRNA-Circ-Negative.xls", true);
		negativeAnalysis(outPathTarget + "miRNA-TargetLnc.xls", lncRNAdif, outPath + "mirRNA-lnc-Negative.xls", true);
		negativeAnalysis(outPathTarget + "miRNA-TargetmRNA.xls", mRNAdif, outPath + "mirRNA-mRNA-Negative.xls", false);
	}

	private void negativeAnalysis(String miTarget, String rnaTable, String outPut, boolean isRemoveMirnaInfo) {
		String tmp1 = FileOperate.changeFileSuffix(outPut, ".tmp1", null);
		String tmp2 = FileOperate.changeFileSuffix(outPut, ".tmp2", null);
		intersection(false, miTarget, rnaTable, tmp1, getTargetNameCol(miTarget, "ncRNA", "CircRNA", "mRNA"), 1);
		getNegativeResult(tmp1, tmp2, "Log2FC");
		if (isRemoveMirnaInfo) {
			removeMiRNAInfo(tmp2, outPut);
		} else {
			FileOperate.moveFile(true, tmp2, outPut);
		}
	}
	
	private void removeMiRNAInfo(String fileIn, String fileOut) {
		String[] title = TxtReadandWrite.readFirstLine(fileIn).split("\t");
		Set<Integer> setExclude = new HashSet<>();
		for (int i = 1; i < title.length; i++) {
			setExclude.add(i);

			String col = title[i];
			if (col.toLowerCase().contains("style")) {
				break;
			}
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(fileIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileOut, true);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			List<String> lsResult = new ArrayList<>();
			for (int i = 0; i < ss.length; i++) {
				if (setExclude.contains(i)) {
					continue;
				}
				lsResult.add(ss[i]);
			}
			txtWrite.writefileln(lsResult.toArray(new String[0]));
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/** 获取指定列的列号，从1开始计数
	 * @param txtMirTarget 给定的文件
	 * @param colName 按照优先级给定的列名。譬如有一列吃不准是 cirRNA,AccId,GeneName，所以
	 * @return
	 */
	private int getTargetNameCol(String txtMirTarget, String... colName) {
		TreeMap<Integer, Integer> mapCol2Name = new TreeMap<>();
		String[] title = TxtReadandWrite.readFirstLine(txtMirTarget).split("\t");
		for (int i = 0; i < title.length; i++) {
			for (int j = 0; j < colName.length; j++) {
				if (StringOperate.isEqualIgnoreCase(title[i], colName[j])) {
					mapCol2Name.put(j, i+1);
				}
			}
		}
		if (mapCol2Name.isEmpty()) {
			throw new ExceptionNbcParamError("cannot find " + ArrayOperate.cmbString(colName, ",") + " in " + txtMirTarget);
		}
		return mapCol2Name.values().iterator().next();
	}
	
	/** ceRNA分析，就是把负相关得到的结果，根据miRNA取交集 */
	public void ceRNA() {
		String outPathNegative = outpath + "NegativeAnalysis/" + prefix;
		String outPath = outpath + "ceRNA/" + prefix;

		FileOperate.createFolders(FileOperate.getPathName(outPath));
		
		intersection(false, outPathNegative + "mirRNA-mRNA-Negative.xls", outPathNegative + "mirRNA-Circ-Negative.xls" , outPath + "miRNA-mRNA-CircRNA",1,1);
		intersection(false, outPathNegative + "mirRNA-mRNA-Negative.xls", outPathNegative + "mirRNA-lnc-Negative.xls" , outPath + "miRNA-mRNA-lncRNA",1,1);
	}
	
	private void intersection(boolean isExtractColToFirstCol, String expFile, String difFile, String outFile) {
		intersection(isExtractColToFirstCol, expFile, difFile, outFile, 1, 1);
	}
	
	private void intersection(boolean isExtractColToFirstCol, String expFile, String difFile, String outFile, int compareCol1, int compareCol2) {
		intersection(isExtractColToFirstCol, expFile, difFile, outFile, Lists.newArrayList(compareCol1), Lists.newArrayList(compareCol2));
	}
	
	private void intersection(boolean isExtractColToFirstCol, String expFile, String difFile, String outFile, List<Integer> lsCompare1, List<Integer> lsCompare2) {
		FileOperate.deleteFileFolder(outFile);
		CompareSimple compareSimple = new CompareSimple();
		compareSimple.setCompareColNum(lsCompare1);
		if (!ArrayOperate.isEmpty(lsCompare2)) {
			compareSimple.setCompareColNum2(lsCompare2);
		}
		compareSimple.setExtractColToFirstCol(isExtractColToFirstCol);
		compareSimple.setFile1(expFile, null);
		compareSimple.setFile2(difFile, null);
		compareSimple.readFiles();
		List<String[]> lsCircCombine = compareSimple.getLsOverlapInfoWithTitle();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		txtWrite.ExcelWrite(lsCircCombine);
		txtWrite.close();
	}
	
	/** 给定一个difGene的结果文件，删除其中的表达量那几列<br>
		title类似<br>
		AccID	Case	Con	FoldChange	Log2FC	P-Value	FDR	Style	nj-o	v13	v7	p3-a	p6-a	p9-c<br>
		需要删除后面的表达值，仅留下<br>
		AccID	Case	Con	FoldChange	Log2FC	P-Value	FDR	Style<br>
		那么这里 colNumNeed 表示前8列<br>
	 * @param lsColsNeedRemove 从1开始计算
	 */
	private void removeDifGeneExpCounts(String txtExp, String txtExpRemoveCounts, String accName) {
		String title = TxtReadandWrite.readFirstLine(txtExp);
		String[] ss = title.split("\t");
		boolean isCanStop = false;
		List<Integer> lsNeed = new ArrayList<>();
		Set<String> setNameInclude = new HashSet<>();
		setNameInclude.add("p-value"); setNameInclude.add("pvalue");
		setNameInclude.add("fdr"); setNameInclude.add("style");
		
		int colNum = 0;
		for (String colName : ss) {
			if (colName.equalsIgnoreCase("log2fc")) {
				lsNeed.remove(lsNeed.size() - 1);
				lsNeed.remove(lsNeed.size() - 1);
			}
			if (setNameInclude.contains(colName.toLowerCase())) {
				isCanStop = true;
			}
			if (isCanStop && !setNameInclude.contains(colName.toLowerCase())) {
				break;
			}
			lsNeed.add(colNum);
			colNum++;
		}
		remainTxtCols(txtExp, txtExpRemoveCounts, lsNeed, accName);
	}
	
	/** 给定一个difGene的结果文件，删除其中的表达量那几列<br>
	 * title类似<br>
	 * AccID	Case	Con	FoldChange	Log2FC	P-Value	FDR	Style	nj-o	v13	v7	p3-a	p6-a	p9-c<br>
	 * 需要删除后面的表达值，仅留下<br>
	 * AccID	Case	Con	FoldChange	Log2FC	P-Value	FDR	Style<br>
	 * 那么这里 colNumNeed 表示前8列<br>
	 * @param txtExp 输入文件
	 * @param txtExpRemoveCounts 输出文件
	 * @param colNameAfter 最后一列的名字，
	 * 譬如AccID	Case	Con	FoldChange	Log2FC	P-Value	FDR	Style	nj-o	v13	v7	p3-a	p6-a	p9-c，就给定Style，Style之后的列都会被删除
	 * 
	 */
	private void removeExpCounts(String txtExp, String txtExpRemoveCounts, String colNameAfter) {
		String title = TxtReadandWrite.readFirstLine(txtExp);
		String[] ss = title.split("\t");
		List<Integer> lsNeedCol = new ArrayList<>();
		int colNum = 0;
		for (String colName : ss) {
			lsNeedCol.add(colNum++);
			if (colName.equalsIgnoreCase(colNameAfter)) {
				break;
			}
		}
		remainTxtCols(txtExp, txtExpRemoveCounts, lsNeedCol, null);
	}

	/** 仅保留某个文本的前几列 */
	private void remainTxtCols(String txtIn, String txtOut, List<Integer> lsColRemain, String accName) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtOut, true);
		int m = 0;
		for (String content : txtRead.readlines()) {

			String[] ssRaw = content.split("\t");
			String[] ssNew = new String[lsColRemain.size()];
			for (int i = 0; i < lsColRemain.size(); i++) {
				ssNew[i] = ssRaw[lsColRemain.get(i)];
			}
			if (m++ == 0 && !StringOperate.isRealNull(accName)) {
				ssNew[0] = accName;	
			}
			txtWrite.writefileln(ssNew);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/** 根据logFC进行筛选
	 * mRNA的logFC和miRNA的logFC应该是相反的
	 * @param fileIn
	 * @param fileOut
	 * @param logFC
	 */
	private void getNegativeResult(String fileIn, String fileOut, String logFC) {
		String[] titles = TxtReadandWrite.readFirstLine(fileIn).split("\t");
		List<Integer> lsLogFc = new ArrayList<>();
		for (int i = 0; i < titles.length; i++) {
			if (titles[i].contains(logFC)) {
				lsLogFc.add(i);
			}
		}
		if (lsLogFc.size() > 2) {
			throw new ExceptionNbcParamError("find " + lsLogFc.size() + " column of " + logFC + ", can only deal with 2 columns");
		}
		Integer[] colLogFc = lsLogFc.toArray(new Integer[0]);
		TxtReadandWrite txtRead = new TxtReadandWrite(fileIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileOut, true);
		txtWrite.writefileln(titles);
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			double fc1 = Double.parseDouble(ss[colLogFc[0]]);
			double fc2 = Double.parseDouble(ss[colLogFc[1]]);
			//TODO 可能存在fc为NA的情况
			if (fc1*fc2 >= 0) {
				continue;
			}
			txtWrite.writefileln(ss);
		}
		txtRead.close();
		txtWrite.close();
	}
}
