package com.novelbio.analysis.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		anno();
	}
	//���ע��
	public static void anno() {
		String parentFile = "/home/zong0jie/����/";
 
		try {
			AnnoQuery.anno(parentFile+"9522�����ܱ�.xls",39947,1,false,9606,1e-10,"/");
			AnnoQuery.anno(parentFile+"BY�����ܱ�.xls",39947,1,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
 
	}
	
	//���geneID����������ȡ�����õ�
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/ChIPSeq_CDG110225/result/compare/comparek4k27/";
		try {
			AnnoQuery.annoGeneID(parentFile+"plosOneESWk4+k27.xls",10090,6,"/");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
		}
	}
}
