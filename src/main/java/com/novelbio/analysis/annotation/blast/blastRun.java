package com.novelbio.analysis.annotation.blast;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.updatedb.database.UpDateFriceDB;
import com.novelbio.database.updatedb.database.UpDateNBCDBFile;


public class blastRun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//			Blast2DB.copeBlastResult("/media/winE/Bioinformatics/BLAST/result/zebrafish/dre_nr2hsa_refseq", 7955, 9606, NovelBioConst.DBINFO_NCBI_ACC_GenralID, NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, "/media/winE/Bioinformatics/BLAST/result/zebrafish/dre_nr2hsa_refseq2BlastDB", false, null);
		
//			Blast2DB.getSeqForBlast("/home/zong0jie/×ÀÃæ/tairDB/seq/TAIR10_pep_20101214", false, "AT\\wG\\d{5}", false, "/home/zong0jie/×ÀÃæ/tairDB/seq/TAIR10_pep_Modify");
//			Blast2DB.copeBlastResult("/media/winE/Bioinformatics/BLAST/result/rice/tigrrice2tairath", 39947, 3702, NovelBioConst.DBINFO_RICE_TIGR, NovelBioConst.DBINFO_ATH_TAIR, 
//					"/media/winE/Bioinformatics/BLAST/result/rice/tigrrice2tairath_modify", false, null);
			UpDateNBCDBFile.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/rice/tigrrice2tairath_modify");
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
