package com.novelbio.analysis.annotation.genAnno;


import org.apache.log4j.Logger;

public class runAnno {
	  private static final Logger logger
      = Logger.getLogger(runAnno.class);
	public static void main(String[] args) 
	{
		anno();
//		annoGeneID();
	}
	//ÃÌº”◊¢ Õ
	public static void anno() {
		String parentFile = "/home/zong0jie/◊¿√Ê/hiv interaction/hiv interaction/";
		try {
			AnnoQuery.anno(parentFile+"gag_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"gag_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"nef_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"nef_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"pol_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"pol_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"rev_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"rev_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"tat_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"tat_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"vif_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"vif_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"vpr_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"vpr_results.xls", 0,3, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"vpu_results.xls", 0,8, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr–æ∆¨.xls",39947,1,false,9606,1e-10);
			AnnoQuery.anno(parentFile+"vpu_results.xls", 0,3, false, 9606, 1e-10, "");
//			AnnoQuery.annoGeneID2symbol(parentFile+"gag_results.xls",9606,3,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
	
	//ÃÌº”geneID£¨”√”⁄¡Ω¡–»°ΩªºØ”√µƒ
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/result/getSummitSeq/";
		try {
//			AnnoQuery.annoGeneID(parentFile+"H3K4me3_peaks_Filter0.5-2.xls",10090,10,"/");
//			AnnoQuery.annoGeneID(parentFile+"H3K27me3_peaks_Filter0.5-2.xls",10090,10,"/");
			AnnoQuery.annoGeneID(parentFile+"summit extract.xls",10090,2,",");
			AnnoQuery.annoGeneID(parentFile+"summit extract.xls",10090,1,",");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Throwable t) {
		
		}
	}
}
