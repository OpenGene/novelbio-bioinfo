package com.novelbio.analysis.project.fy;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.fileOperate.FileOperate;

public class Annotation {

	
	public static void main(String[] args) {
		
//		anno("/home/zong0jie/×ÀÃæ/WT0-WT5_diff-gene.xls");
//		anno("/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/gene_exp.diff.xls");
//		anno("/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/splicing.Out5.xls");
		anno("/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifK0vsWT0ensembl/gene_exp.diff");
		anno("/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifK5vsWT5ensembl/gene_exp.diff");
		anno("/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifWT0vsWT5ensembl/gene_exp.diff");
	}
	
	
	
	public static void anno(String txtFile) {
		try {
			AnnoQuery.annoGeneIDXls(txtFile, FileOperate.changeFileSuffix(txtFile, "_anno", "xls"), 9031, 2, 1, "", true, 9606);
		} catch (Exception e) {
			e.printStackTrace()	;
		}
	}
}
