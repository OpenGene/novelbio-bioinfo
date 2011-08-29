package com.novelbio.analysis.annotation.pathway.kegg;

import com.novelbio.analysis.annotation.pathway.kegg.kGpath.Scr2Target;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;


/**
 * ������������source to target�ı��Ҹо�����sorce to target��ʱ��Ҫ������ϲ�
 * @author zong0jie
 *
 */
public class runSrc2Trg {
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFIle = "/home/zong0jie/����/";

		int QtaxID = 9606;
		boolean blast = false;
		int StaxID = 9606;
		try {
			String readExcel = parentFIle + "GO��PATHWAY����.xls";
			String scr2trg = parentFIle + "GO��PATHWAY����InterSectiontrg.txt";
			String attr = parentFIle + "GO��PATHWAY����InterSectionatttxt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			Scr2Target.getGene2RelateKo("",accID,scr2trg,
					attr , QtaxID, blast, StaxID, 1e-5);
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
