package com.novelbio.analysis.coexp.simpcoexp;

import com.novelbio.analysis.coexp.simpcoexp.CoExp;
import com.novelbio.analysis.coexp.simpcoexp.DegreeAnnotation;
import com.novelbio.database.domain.modgeneid.GeneID;


public class TestCoExp {
	public static void main(String[] args) {
		DegreeAnnotation degreeAnnotation = new DegreeAnnotation();
		degreeAnnotation.readData("D:/zongjie/Desktop/交集基因信号值中位数CON_Coexp.xls.xls");
		degreeAnnotation.writeToFile("D:/zongjie/Desktop/交集基因信号值中位数CON_Degree.xls");
		
//		GeneID geneID = new GeneID("tp53", 9606);
//		System.out.println(geneID.getDescription());
	}
	public static void testCoExp(String[] args) {
		CoExp coExp = new CoExp();
		coExp.readTxtExcel("D:/zongjie/Desktop/交集基因信号值中位数CON.xls.xls", new int[]{1,2,3,4});
		coExp.setPvalueCutoff(0.05);
		coExp.writeToExcel("D:/zongjie/Desktop/交集基因信号值中位数CON_Coexp.xls.xls");
	}
}
