package com.novelbio.analysis.project.fy;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;

public class Annotation {

	
	public static void main(String[] args) {
		
//		anno("/home/zong0jie/×ÀÃæ/WT0-WT5_diff-gene.xls");
		anno("/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/gene_exp.diff.xls");
		anno("/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/splicing.Out5.xls");
	}
	
	
	
	public static void anno(String txtFile) {
		try {
			AnnoQuery.anno(txtFile, 10090, 3, false, 9606, 1e-10, "");//GeneID2symbol(parentFile+"tdrÐ¾Æ¬.xls",39947,1,false,9606,1e-10);
			 
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
}
