package com.novelBio.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		annoGeneID();
	}
	//添加注释
	public static void anno() {
		String parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/单核晚期三倍变化/单核晚期三倍变化/";
		try {
			AnnoQuery.anno(parentFile+"DWKYvs9522.xls",0,1,false,9606,1e-10);
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
		try {
			AnnoQuery.anno(parentFile+"DWKYvsBY.xls",0,1,false,9606,1e-10);
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
		try {
			AnnoQuery.anno(parentFile+"DWBYvs9522.xls",0,1,false,9606,1e-10);
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
	
	//添加geneID，用于两列取交集用的
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/ChIPSeq_CDG110225/result/compare/compare2plos/";
		try {
			AnnoQuery.annoGeneID(parentFile+"k0_peakFilter.xls",10090,10,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
		try {
			AnnoQuery.annoGeneID(parentFile+"k4_peakFilter.xls",10090,10,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
		try {
			AnnoQuery.annoGeneID(parentFile+"W0_peakFilter.xls",10090,10,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
		try {
			AnnoQuery.annoGeneID(parentFile+"W4_peakFilter.xls",10090,10,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
		try {
			AnnoQuery.annoGeneID(parentFile+"Chromatin states of analyzed promoters in mES cells.xls",10090,6,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
	}
}
