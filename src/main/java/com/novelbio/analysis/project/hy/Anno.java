package com.novelbio.analysis.project.hy;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.fileOperate.FileOperate;

public class Anno {
	
	public static void main(String[] args) {
		String geneFileXls = "/home/zong0jie/桌面/146a-pictar.xls";
		anno(9606, 0, 1, "", geneFileXls);
	}
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	private static void anno(int QtaxID, int StaxID, int colAccID, String regex, String geneFileXls) {
		boolean blast = true;
		if (StaxID <= 0 ) {
			blast = false;
		}
		String txtOutFile = FileOperate.changeFileSuffix(geneFileXls, "_anno", null);
		AnnoQuery.annoGeneIDXls(geneFileXls, txtOutFile, QtaxID, 2, colAccID, regex, blast, StaxID);
	}
}
