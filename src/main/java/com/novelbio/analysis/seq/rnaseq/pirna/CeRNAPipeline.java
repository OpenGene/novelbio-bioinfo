package com.novelbio.analysis.seq.rnaseq.pirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.tools.compare.CompareSimple;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * piRNA的分析流程，代码没有优化，仅仅是完成业务逻辑。
 * 如果以后没有大的调整不打算进行优化
 * @author zong0jie
 * @data 2016年7月25日
 */
public class CeRNAPipeline {
//	String miRNAexp;
	String miRNAdif;
	
	String circRNAexp;
	String circRNAdif;
	String circMirTarget;
	
//	String lncRNAexp;
	String lncRNAdif;
	String lncMirTarget;

//	String mRNAexp;
	String mRNAdif;
	String mrnaMirTarget;

	/** 结尾需要有"/" */
	String outpath;
	String prefix;
	
	
	public static void main(String[] args) {
		String inPath = "/media/winE/test/ceRNA流程输入输出文件/CeRNA例子/";
		CeRNAPipeline ceRNAPipeline = new CeRNAPipeline("/media/winE/test/ceRNA流程输入输出文件/CeRNA例子/out/");
		String circRNAexp = inPath + "circExp.txt";
		String circRNAdif = inPath + "GvsN_FC2_Pvalue0.05_CircRNA.txt";
		String lncRNAdif = inPath + "GvsN_FC2_FDR0.05_ncRNA.txt";
		String miRNAdif = inPath + "GvsN_miRNA.txt";
		String mRNAdif = inPath + "GvsN_FC2_FDR0.05_mRNA.txt";
		
		ceRNAPipeline.setCircRNAexpAndDif(circRNAexp, circRNAdif);
		ceRNAPipeline.setLncRNAexpAndDif(lncRNAdif);
		ceRNAPipeline.setMiRNAexpAndDif(miRNAdif);
		ceRNAPipeline.setmRNAexpAndDif(mRNAdif);
		
		String mirCircTarget = inPath + "GvsN_miRNA-CircRNA-TargetGene.txt";
		String mirLncTarget = inPath + "GvsN_miRNA-ncRNA-TargetGene.txt";
		String mirMRNATarget = inPath + "GvsN_miRNA-mRNA-TargetGene.txt";

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
		FileOperate.createFolders(outPath + "tmp/");
	}
	
	public void setMiRNAexpAndDif(String miRNAdif) {
		FileOperate.validateFileExistAndBigThan0(miRNAdif);

		this.miRNAdif = miRNAdif;
	}
	
	public void setCircRNAexpAndDif(String circRNAexp, String circRNAdif) {
		FileOperate.validateFileExistAndBigThan0(circRNAexp);
		FileOperate.validateFileExistAndBigThan0(circRNAdif);

		this.circRNAexp = circRNAexp;
		this.circRNAdif = circRNAdif;
	}
	
	public void setLncRNAexpAndDif(String lncRNAdif) {
		FileOperate.validateFileExistAndBigThan0(lncRNAdif);

		this.lncRNAdif = lncRNAdif;
	}
	
	public void setmRNAexpAndDif(String mRNAdif) {
		FileOperate.validateFileExistAndBigThan0(mRNAdif);

		this.mRNAdif = mRNAdif;
	}
	
	public void setCircMirTarget(String circMirTarget) {
		this.circMirTarget = circMirTarget;
	}
	public void setLncMirTarget(String lncMirTarget) {
		this.lncMirTarget = lncMirTarget;
	}
	public void setMrnaMirTarget(String mrnaMirTarget) {
		this.mrnaMirTarget = mrnaMirTarget;
	}
	
	/** 第一步，把基因注释信息与差异基因取交集 */
	public void intersection() {
		String outPath = outpath + "Difgene/" + prefix;
//		intersection(miRNAexp, miRNAdif, outPath + "miRNAtable.xls");
		intersection(circRNAexp, circRNAdif, outPath + "circRNAtable.xls");
//		intersection(lncRNAexp, lncRNAdif, outPath + "lncRNAtable.xls");
//		intersection(mRNAexp, mRNAdif, outPath + "mRNAtable.xls");
	}
	
	/** 第二步，把miRNA的差异信息比对到靶基因上去 */
	public void combineMiRNA2TargetRNA() {
		String outPath = outpath + "TargetPredict/" + prefix;
		intersection(miRNAdif, circMirTarget, outPath + "miRNA-TargetCirc.xls");
		intersection(miRNAdif, lncMirTarget, outPath + "miRNA-TargetLnc.xls");
		intersection(miRNAdif, mrnaMirTarget, outPath + "miRNA-TargetmRNA.xls");
	}
	
	/** 第三步，miRNA与相应的基因做负相关分析，也就是把miRNA上调，但是circRNA下调这种基因挑选出来 */
	public void negativeAnalysis() {
		String outPathDif = outpath + "Difgene/" + prefix;
		String outPathTarget = outpath + "TargetPredict/" + prefix;

		String outPath = outpath + "NegativeAnalysis/";
		negativeAnalysis(outPathTarget + "miRNA-TargetCirc.xls", outPathDif + "circRNAtable.xls", outPath + "mirRNA-Circ-Negative.xls");
		negativeAnalysis(outPathTarget + "miRNA-TargetLnc.xls", lncRNAdif, outPath + "mirRNA-lnc-Negative.xls");
		negativeAnalysis(outPathTarget + "miRNA-TargetmRNA.xls", mRNAdif, outPath + "mirRNA-mRNA-Negative.xls");
	}

	private void negativeAnalysis(String miTarget, String rnaTable, String outPut) {
		String tmp1 = FileOperate.changeFileSuffix(outPut, ".tmp1", null);
		String tmp2 = FileOperate.changeFileSuffix(outPut, ".tmp2", null);
		intersection(miTarget, rnaTable, tmp1, 9, 1);
		getNegativeResult(tmp1, tmp2, "Log2FC");
		removeColTxt(tmp2, outPut, "Case", "Control");
	}
	
	/** ceRNA分析，就是把负相关得到的结果，根据miRNA取交集 */
	public void ceRNA() {
		String outPathNegative = outpath + "NegativeAnalysis/" + prefix;
		String outPath = outpath + "ceRNA/" + prefix;
		intersection(outPathNegative + "mirRNA-mRNA-Negative.xls", outPathNegative + "mirRNA-Circ-Negative.xls" , outPath + "miRNA-mRNA-CircRNA");
		intersection(outPathNegative + "mirRNA-mRNA-Negative.xls", outPathNegative + "mirRNA-lnc-Negative.xls" , outPath + "miRNA-mRNA-lncRNA");

	}
	
	private void intersection(String expFile, String difFile, String outFile) {
		intersection(expFile, difFile, outFile, 1, 1);
	}
	
	private void intersection(String expFile, String difFile, String outFile, int compareCol1, int compareCol2) {
		CompareSimple compareSimple = new CompareSimple();
		compareSimple.setCompareColNum(compareCol1);
		if (compareCol2 > 0) {
			compareSimple.setCompareColNum2(compareCol2);
		}
		compareSimple.setFile1(expFile, null);
		compareSimple.setFile2(difFile, null);
		compareSimple.readFiles();
		List<String[]> lsCircCombine = compareSimple.getLsOverlapInfoWithTitle();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile);
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
	private void removeExpCounts(String txtExp, String txtExpRemoveCounts) {
		String title = TxtReadandWrite.readFirstLine(txtExp);
		String[] ss = title.split("\t");
		boolean isCanStop = false;
		int colNumNeed = 0;

		for (String colName : ss) {
			colNumNeed++;
			if (colName.equalsIgnoreCase("P-Value") || colName.equalsIgnoreCase("pvalue") || colName.equalsIgnoreCase("fdr")) {
				isCanStop = true;
			}
			if (isCanStop) {
				if (!colName.equalsIgnoreCase("fdr") && !colName.equalsIgnoreCase("Style")) {
					break;
				}
			}
		}
		remainTxtCols(txtExp, txtExpRemoveCounts, colNumNeed);
	}
	
	/** 仅保留某个文本的前几列 */
	private void remainTxtCols(String txtIn, String txtOut, int colRemainNum) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtOut, true);
		for (String content : txtRead.readlines()) {
			String[] ssRaw = content.split("\t");
			String[] ssNew = new String[colRemainNum];
			for (int i = 0; i < colRemainNum; i++) {
				ssNew[i] = ssRaw[i];
			}
			txtWrite.writefileln(ssNew);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/** 删除指定名字的列，注意是区分大小写的
	 * @param txtIn
	 * @param txtOut
	 * @param colName 需要删除的列，只要是以该字段结尾就可以删除，如colName为Case，则 prefix_Case 这种就可以删除
	 */
	private void removeColTxt(String txtIn, String txtOut, String... colName) {
		List<String> lsColSkipStr = new ArrayList<>();
		for (String col : colName) {
			lsColSkipStr.add(col);
		}
		Set<Integer> setColSkip = new HashSet<>();
		String[] ss = TxtReadandWrite.readFirstLine(txtIn).split("\t");
		for (int i = 0; i < ss.length; i++) {
			for (String string : lsColSkipStr) {
				if (ss[i].endsWith(string)) {
					setColSkip.add(i);
				}
			}
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(txtIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtOut, true);
		for (String content : txtRead.readlines()) {
			String[] ssRaw = content.split("\t");
			List<String> lsOut = new ArrayList<>();
			for (int i = 0; i < ssRaw.length; i++) {
				if (setColSkip.contains(i)) {
					continue;
				}
				lsOut.add(ssRaw[i]);
			}
			String[] ssNew = lsOut.toArray(new String[0]);
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
