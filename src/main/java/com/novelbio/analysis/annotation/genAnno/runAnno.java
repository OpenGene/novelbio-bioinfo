package com.novelbio.analysis.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		anno();
	}
	//添加注释
	public static void anno() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/Microarray_XLY110623/";
		try {
			AnnoQuery.anno(parentFile+"Leg fc2_annotation.xls",10090,1,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
	
	//添加geneID，用于两列取交集用的
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/ChIPSeq_CDG110225/result/compare/comparek4k27/";
		try {
			AnnoQuery.annoGeneID(parentFile+"plosOneESWk4+k27.xls",10090,6,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
	}
}
