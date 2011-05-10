package com.novelbio.analysis.annotation.blast;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.upDateDB.dataBase.UpDateFriceDB;


public class blastRun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			/**
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/DataBase/btaProtein/protein.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",false,"/media/winE/Bioinformatics/BLAST/DataBase/btaProtein/proReady.fa",false);
			System.out.println("ok");
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/DataBase/btaRNA/rna.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",false,"/media/winE/Bioinformatics/BLAST/DataBase/btaRNA/rnaReady.fa",false);
			System.out.println("ok");
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/DataBase/hsaProtein/protein.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",true,"/media/winE/Bioinformatics/BLAST/DataBase/hsaProtein/proReady.fa",false);
			System.out.println("ok");
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rna.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",true,"/media/winE/Bioinformatics/BLAST/DataBase/hsaRNA/rnaReady.fa",false);
			System.out.println("ok");
			
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/sourceSeq/Sus_scrofa_protein.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",false,"/media/winE/Bioinformatics/BLAST/sourceSeq/Sus_scrofa_proteinReady.fa",false);
			System.out.println("ok");
			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/sourceSeq/Sus_scrofa_rna.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",false,"/media/winE/Bioinformatics/BLAST/sourceSeq/Sus_scrofa_rnaReady.fa",false);
			System.out.println("ok");
			**/
//			Blast2DB.getAffySeq("/media/winE/Bioinformatics/Affymetrix/Pig Porcine/Porcine_1.target/Porcine.target", "/media/winE/Bioinformatics/BLAST/sourceSeq/Pig.fa");
//			Blast2DB.prepareSeqGetGeneID("/media/winE/Bioinformatics/BLAST/DataBase/pigRNA/rna.fa",false,"(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})",false,"/media/winE/Bioinformatics/BLAST/DataBase/pigRNA/rnaReady.fa",false);
//			Blast2DB.copeA2GeneID("/media/winE/Bioinformatics/BLAST/result/susAgilent2RefSeq.txt", 9823, "Agilent", "/media/winE/Bioinformatics/BLAST/result/susAgilent2RefSeqNCBIID.txt");
//			
//			
//			Blast2DB.copeBlastResult("/media/winE/Bioinformatics/BLAST/result/cow/cowRefRNA2humRefProblastx", 9913, 9606, "RefSeq", "RefSeq", "/media/winE/Bioinformatics/BLAST/result/cow/cope/cowRefRNA2humRefProblastx");
//			Blast2DB.getBlastA2B("/media/winE/Bioinformatics/BLAST/result/susAgilent2Affy.txt", "/media/winE/Bioinformatics/BLAST/result/cope/copesusAffy2humPro", "/media/winE/Bioinformatics/BLAST/result/cope/copeAgilent2humPro");
//			UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/BLAST/result/cow/cope/btaAgilent2Refseq061130", "/media/winE/Bioinformatics/BLAST/result/cow/cope/out1");
//			UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/BLAST/result/cow/cope/btaAgilent2Refseq0904", "/media/winE/Bioinformatics/BLAST/result/cow/cope/out2");
//			Blast2DB.copeA2GeneID("/media/winE/Bioinformatics/BLAST/result/cow/btaAgilent2Refseq090410", 9913, "AgilentBta0904", "/media/winE/Bioinformatics/BLAST/result/cow/cope/btaAgilent2Refseq0904");
//			UpDateFriceDB.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/cow/cope/cowRefPro2humRefPro");
//			UpDateFriceDB.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/cow/cope/cowRefRNA2humRefProblastx");
			//GetSeq.getNrTaxSeq("Sus scrofa", 9823, "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/nr",  "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/nrPig", 3);
//			GetSeq.getNtTaxSeq(9823, "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/nt",  "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/ntPig", 3);
//			Blast2DB.copeA2GeneID("/media/winE/Bioinformatics/BLAST/result/pig/susAff2PigNT", 9823, "AffyPig", "/media/winE/Bioinformatics/BLAST/result/pig/susAff2PigNTNCBIID", true, HashDB.getHashGenID(9823));
//			GetSeq.getNrTaxSeq("Danio rerio", 7955, "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/nr", "/media/winE/Bioinformatics/BLAST/sourceSeq/zebrafish/Danio_rerio_nr", 3);
//			GetSeq.getNtTaxSeq(7955, "/media/winE/Bioinformatics/NCBI/Sequence/nrAndnt/nt", "/media/winE/Bioinformatics/BLAST/sourceSeq/zebrafish/Danio_rerio_nt", 3);
			Blast2DB.copeBlastResult("/media/winE/Bioinformatics/BLAST/result/zebrafish/dre_nr2hsa_refseq", 7955, 9606, NovelBioConst.DBINFO_NCBI_ACC_GenralID, NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, "/media/winE/Bioinformatics/BLAST/result/zebrafish/dre_nr2hsa_refseq2BlastDB", false, null);
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
