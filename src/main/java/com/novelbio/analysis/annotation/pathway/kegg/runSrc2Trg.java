package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.kGpath.Scr2Target;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.fileOperate.FileOperate;


/**
 * 给定基因，生成source to target的表。我感觉在做sorce to target的时候要将基因合并
 * @author zong0jie
 *
 */
public class runSrc2Trg {
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
//		getInfo();
		
		String parentFIle = "/home/zong0jie/桌面/";

		int QtaxID = 9606;
		boolean blast = false;
		int StaxID = 4932;
		try {
			String readExcel = parentFIle + "UCSChg19RefseqDuplicateID.xls";
			
			
			
			String scr2trg = parentFIle + FileOperate.getFileNameSep(readExcel)[0]+"InterSectiontrg.txt";
			String attr = parentFIle + FileOperate.getFileNameSep(readExcel)[0]+"InterSectionatt.txt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			Scr2Target.getGene2RelateKo("path:hsa04150",accID,scr2trg, attr , QtaxID, blast, StaxID, 1e-5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			String readExcel = parentFIle + "603 vs 601.xls";
//			String scr2trg = parentFIle + "603 vs 601Trgnoblast.txt";
//			String attr = parentFIle + "603 vs 601Attrnoblast.txt";
//			String[] accID = KGprepare.getAccID(1, 1,readExcel);
//			Scr2Target.getGene2RelateKo("",accID,scr2trg,
//					attr , QtaxID, blast, StaxID, 1e-5);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	private static void getInfo()
	{
		String parentFIle = "/home/zong0jie/桌面/";

		int QtaxID = 9606;
		boolean blast = false;
		int StaxID = 4932;
		try {
			String readExcel = parentFIle + "显著GO PATH交集基因.xls";
			
			
			
			String scr2trg = parentFIle + FileOperate.getFileNameSep(readExcel)[0]+"InterSectiontrg.txt";
			String attr = parentFIle + FileOperate.getFileNameSep(readExcel)[0]+"InterSectionatt.txt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			ArrayList<String> lsKeggID = new ArrayList<String>();
			for (String string : accID) {
				lsKeggID.add(string);
			}
			Scr2Target.getGene2RelateKo2("", lsKeggID, scr2trg, attr, QtaxID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			String readExcel = parentFIle + "603 vs 601.xls";
//			String scr2trg = parentFIle + "603 vs 601Trgnoblast.txt";
//			String attr = parentFIle + "603 vs 601Attrnoblast.txt";
//			String[] accID = KGprepare.getAccID(1, 1,readExcel);
//			Scr2Target.getGene2RelateKo("",accID,scr2trg,
//					attr , QtaxID, blast, StaxID, 1e-5);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
