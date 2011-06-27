package com.novelbio.analysis.annotation.genAnno;

public class runAnno {
	public static void main(String[] args) 
	{
		anno();
		annoGeneID();
	}
	//���ע��
	public static void anno() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/Microarray_XLY110623/TKO_FH/";
		try {
			AnnoQuery.anno(parentFile+"TKO VS FH D4 Down1.5.xls",10090,1,false,9606,1e-10,"/");
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
	
	//���geneID����������ȡ�����õ�
	public static void annoGeneID() {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/Microarray_XLY110623/TKO_FH/";
		try {
			AnnoQuery.annoGeneID(parentFile+"TKO VS FH D4 Down1.5.xls",10090,1,"");
//			AnnoQuery.anno(parentFile+"heterovswt.xls",0,1,false,9606,1e-10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
