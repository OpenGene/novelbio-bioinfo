package com.novelbio.analysis.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		anno();
	}
	//添加注释
	public static void anno() {
		String parentFile = "/home/zong0jie/桌面/";
 
		try {
			AnnoQuery.anno(parentFile+"9522趋势总表.xls",39947,1,false,9606,1e-10,"/");
			AnnoQuery.anno(parentFile+"BY趋势总表.xls",39947,1,false,9606,1e-10,"/");
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
