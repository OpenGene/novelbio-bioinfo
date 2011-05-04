package com.novelBio.annotation.pathway.kegg;

import com.novelBio.annotation.pathway.kegg.kGpath.Scr2Target;
import com.novelBio.annotation.pathway.kegg.prepare.KGprepare;

import entity.friceDB.NCBIID;
import entity.friceDB.UniProtID;
import entity.kegg.KGCgen2Entry;
import DAO.KEGGDAO.DaoKCdetail;

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
		String parentFIle = "/media/winE/NBC/Project/Microarray_YXF110318/scr2trg/new/";

		int QtaxID = 0;
		boolean blast = true;
		int StaxID = 9606;
		try {
			String readExcel = parentFIle + "402 vs 401.xls";
			String scr2trg = parentFIle + "402 vs 401Trgnoblast.txt";
			String attr = parentFIle + "402 vs 401Attrnoblast.txt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			Scr2Target.getGene2RelateKo("",accID,scr2trg,
					attr , QtaxID, blast, StaxID, 1e-5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String readExcel = parentFIle + "602 vs 601.xls";
			String scr2trg = parentFIle + "602 vs 601Trgnoblast.txt";
			String attr = parentFIle + "602 vs 601Attrnoblast.txt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			Scr2Target.getGene2RelateKo("",accID,scr2trg,
					attr , QtaxID, blast, StaxID, 1e-5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String readExcel = parentFIle + "603 vs 601.xls";
			String scr2trg = parentFIle + "603 vs 601Trgnoblast.txt";
			String attr = parentFIle + "603 vs 601Attrnoblast.txt";
			String[] accID = KGprepare.getAccID(1, 1,readExcel);
			Scr2Target.getGene2RelateKo("",accID,scr2trg,
					attr , QtaxID, blast, StaxID, 1e-5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
