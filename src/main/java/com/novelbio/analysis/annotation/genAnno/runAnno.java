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
	//添加注释
	public static void anno() {
		try {
			String parentFile = "/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cuffDifK5vsWT5/";
//			AnnoQuery.anno(parentFile+"X2vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
//			AnnoQuery.anno(parentFile+"X3vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"K5-WT5_dif_splicing.xls", 9031, 3, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
			 
//			AnnoQuery.annoGeneID2symbol(parentFile+"gag_results.xls",9606,3,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
		try {
			String parentFile = "/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cuffDifK5vsWT5/";
//			AnnoQuery.anno(parentFile+"X2vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
//			AnnoQuery.anno(parentFile+"X3vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");
			
			AnnoQuery.anno(parentFile+"K5-WT5_diff_gene.xls", 9031, 3, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
			 
//			AnnoQuery.annoGeneID2symbol(parentFile+"gag_results.xls",9606,3,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}

//		try {
//			String parentFile = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/DGEexpress/Intersect3vs1、2/";
////			AnnoQuery.anno(parentFile+"X2vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
////			AnnoQuery.anno(parentFile+"X3vsX1_filter.xls", 39947, 1, false, 9606, 1e-10, "");
//			
//			AnnoQuery.anno(parentFile+"3与1、2都有差异的基因.xls", 39947, 1, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdr芯片.xls",39947,1,false,9606,1e-10);
//			 
////			AnnoQuery.annoGeneID2symbol(parentFile+"gag_results.xls",9606,3,false,9606,1e-10);
//		} catch (Exception e) {
//			e.printStackTrace()	;
//		}
	}
	
	//添加geneID，用于两列取交集用的
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/compareSICERvsPaper/";
		try {
//			AnnoQuery.annoGeneID(parentFile+"H3K4me3_peaks_Filter0.5-2.xls",10090,10,"/");
//			AnnoQuery.annoGeneID(parentFile+"H3K27me3_peaks_Filter0.5-2.xls",10090,10,"/");
			AnnoQuery.annoGeneIDTxt(parentFile+"nature2007K27seSort-W200-G600-E100_anno_-3k_body.xls",10090,5,"///");
//			AnnoQuery.annoGeneIDTxt(parentFile+"nature2007K27_peaks_Summit_+1k-2k_filterAnnotation.xls",10090,10,"/");
//			AnnoQuery.annoGeneID(parentFile+"summit extract.xls",10090,1,",");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Throwable t) {
		t.printStackTrace();
		}
	}
}
