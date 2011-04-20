package com.novelBio.annotation.pathway.kegg.kGpath;

import java.util.ArrayList;

import entity.friceDB.NCBIID;
import entity.kegg.KGCKo2Gen;
import entity.kegg.KGCentry2Gen;
import entity.kegg.KGCgen2Entry;
import entity.kegg.KGCgen2Ko;
import entity.kegg.KGIDkeg2Ko;
import entity.kegg.KGrelation;
import entity.kegg.noUseKGCentry2Ko2Gen;
import entity.kegg.KGIDgen2Keg;
import entity.kegg.KGentry;
import DAO.KEGGDAO.DaoKCdetail;
import DAO.KEGGDAO.DaoKEntry;
import DAO.KEGGDAO.DaoKIDgen2Keg;
import DAO.KEGGDAO.DaoKRealtion;

public class runPathEnrich {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Microarray_CDG110415/";
		String backGroundFile=parentFile + "mouseAffyBG.txt";
		try {
			String fileName = "wangyanruOld";			
			String geneFile=parentFile+fileName+".xls";
			String resultExcel2003 = parentFile +fileName+"PathanalysisComb.xls";
			int[] colID = new int[2]; colID[0] = 1; colID[1] = 2;
			double up = 1;
			double down = 1;
			boolean blast = false;
			int queryTaxID = 10090;
			int subTaxID = 9606;
			double evalue = 1e-5;
			boolean sepID = false;
			String[] prix = new String[2]; prix[0] = "up"; prix[1] = "down";
			//tring resultExcel2003Blast =parentFile+ "阳性药物-PathBlast.xls";

	//	 PathEnrich.getPathRun(geneFileXls, 9913, colID, up, down, bgFile, resultExcel2003Blast, blast,evalue, subTaxID);
		 PathEnrichNew.getPathRun(geneFile, queryTaxID, colID, sepID, up, down, prix, backGroundFile, resultExcel2003, blast, evalue, subTaxID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String fileName = "wangyanruNew";			
			String geneFile=parentFile+fileName+".xls";
			String resultExcel2003 = parentFile +fileName+"PathanalysisComb.xls";
			int[] colID = new int[2]; colID[0] = 1; colID[1] = 2;
			double up = 1;
			double down = 1;
			boolean blast = false;
			int queryTaxID = 10090;
			int subTaxID = 9606;
			double evalue = 1e-5;
			boolean sepID = false;
			String[] prix = new String[2]; prix[0] = "up"; prix[1] = "down";
			//tring resultExcel2003Blast =parentFile+ "阳性药物-PathBlast.xls";

	//	 PathEnrich.getPathRun(geneFileXls, 9913, colID, up, down, bgFile, resultExcel2003Blast, blast,evalue, subTaxID);
		 PathEnrichNew.getPathRun(geneFile, queryTaxID, colID, sepID, up, down, prix, backGroundFile, resultExcel2003, blast, evalue, subTaxID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
