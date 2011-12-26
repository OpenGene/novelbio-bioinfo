package com.novelbio.analysis.project.cr;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.statusAckLog;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class Annotation {
	public static void main(String[] args) {
		annotation();
	}
	public static void annotation() {
		String excelIn = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/CR.xls";
		String excelOut = FileOperate.changeFileSuffix(excelIn, "_anno", null);
//		excelIn = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/CR.xls";
//		excelOut = FileOperate.changeFileSuffix(excelIn, "_anno", null);
//		AnnoQuery.anno(excelIn, excelOut, 9606, 1, false, 9606, 1e-10, "");
		
		excelIn = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/DGEDif_E6dnvsNS6d.xls";
		excelOut = FileOperate.changeFileSuffix(excelIn, "_anno", null);
		AnnoQuery.anno(excelIn, excelOut, 9606, 1, false, 9606, 1e-10, "");
		
		excelIn = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/DGEDif_E6dvsE6dn.xls";
		excelOut = FileOperate.changeFileSuffix(excelIn, "_anno", null);
		AnnoQuery.anno(excelIn, excelOut, 9606, 1, false, 9606, 1e-10, "");
		
		excelIn = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/DGEDif_E6dvsNS6d.xls";
		excelOut = FileOperate.changeFileSuffix(excelIn, "_anno", null);
		AnnoQuery.anno(excelIn, excelOut, 9606, 1, false, 9606, 1e-10, "");
	}
}
