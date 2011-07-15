package com.novelbio.analysis.annotation.genAnno;


import org.apache.log4j.Logger;

public class runAnno {
	  private static final Logger logger
      = Logger.getLogger(runAnno.class);
	public static void main(String[] args) 
	{
//		anno();
		annoGeneID();
	}
	//添加注释
	public static void anno() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/result/annotation/";
		try {
			AnnoQuery.anno(parentFile+"H3K4me3_peaks_Filter1.5k_1.8k.xls",10090,10,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
		try {
			AnnoQuery.anno(parentFile+"H3K27me3_peaks_Filter2k_3k.xls",10090,10,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
		try {
			AnnoQuery.anno(parentFile+"H3K27me3_peaks_Filter2k_3k.xls",10090,6,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
	
	//添加geneID，用于两列取交集用的
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
