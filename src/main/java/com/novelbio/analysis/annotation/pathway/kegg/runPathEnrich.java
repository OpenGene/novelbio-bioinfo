package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.kGpath.PathEnrichNew;

public class runPathEnrich {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile = "/home/zong0jie/桌面/";
		String backGroundFile="/media/winE/Bioinformatics/GenomeData/mouse/mouseRefSeqBG.txt";
		try {
			String fileName = "MicroRNA的预测靶基因";
			String geneFile=parentFile+fileName+".xls";
			String resultExcel2003 = parentFile +fileName+"PathanalysisComb.xls";
			int[] colID = new int[2]; colID[0] = 1; colID[1] = 1;
			double up = 0;
			double down = -1;
			boolean blast = false;
			int queryTaxID = 9606;
			int subTaxID = 9606;
			double evalue = 1e-5;
			boolean sepID = false;
			String[] prix = new String[2]; prix[0] = ""; prix[1] = "down";//prix[1] = "down";
			//tring resultExcel2003Blast =parentFile+ "阳性药物-PathBlast.xls";

//		 PathEnrich.getPathRun(geneFile, 0, colID, up, down, backGroundFile, resultExcel2003+"old", blast,evalue, subTaxID);
		 PathEnrichNew.getPathRun(geneFile, queryTaxID, colID, sepID, up, down, prix, backGroundFile, resultExcel2003, blast, evalue, subTaxID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
