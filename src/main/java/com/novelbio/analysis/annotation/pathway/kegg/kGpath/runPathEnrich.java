package com.novelbio.analysis.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;

public class runPathEnrich {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Microarray_WFL110423/������ ����оƬ/������ ����оƬ/chip result/GO/";
		String backGroundFile=parentFile + "hg18refseqBGwithDuplication.txt";
		try {
			String fileName = "WFL";			
			String geneFile=parentFile+fileName+".xls";
			String resultExcel2003 = parentFile +fileName+"PathanalysisComb.xls";
			int[] colID = new int[2]; colID[0] = 1; colID[1] = 2;
			double up = 1;
			double down = 1;
			boolean blast = false;
			int queryTaxID = 9606;
			int subTaxID = 9606;
			double evalue = 1e-5;
			boolean sepID = false;
			String[] prix = new String[2]; prix[0] = "532"; prix[1] = "635";//prix[1] = "down";
			//tring resultExcel2003Blast =parentFile+ "����ҩ��-PathBlast.xls";

//		 PathEnrich.getPathRun(geneFile, 0, colID, up, down, backGroundFile, resultExcel2003+"old", blast,evalue, subTaxID);
		 PathEnrichNew.getPathRun(geneFile, queryTaxID, colID, sepID, up, down, prix, backGroundFile, resultExcel2003, blast, evalue, subTaxID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}