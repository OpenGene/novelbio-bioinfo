package com.novelBio.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		annoGeneID();
	}
	//���ע��
	public static void anno() {
		String parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/�������������仯/�������������仯/";
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
