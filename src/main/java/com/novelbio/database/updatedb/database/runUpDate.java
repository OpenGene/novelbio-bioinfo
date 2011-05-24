package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.annotation.blast.Blast2DB;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.updatedb.idconvert.RiceID;


public class runUpDate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String gffRapDB =NovelBioConst.GENOME_PATH_RICE_RAPDB_GFF_GENE;
		String Rap2MSUFile ="/media/winE/Bioinformatics/GenomeData/Rice/RapDB/RAP-MSU.txt";
		String affyidtolocid ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/affyidtolocidnew.txt";
		String gffTigrRice ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.gff3Cope";
		String tigrGoSlim ="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/all.GOSlim_assignment";
		
		String rapdbGeneIDoutFile = "/media/winE/Bioinformatics/GenomeData/Rice/outfile/gffRapDBout";
		String rapdbMSUoutFile = "/media/winE/Bioinformatics/GenomeData/Rice/outfile/MSUout";
		String AffIDoutFile = "/media/winE/Bioinformatics/GenomeData/Rice/outfile/affyOut";
		String tigrGeneIDoutFile = "/media/winE/Bioinformatics/GenomeData/Rice/outfile/gffTigrOut";
		try {

			RiceID.tigrDescription(gffTigrRice);
			RiceID.rapDBGO(gffRapDB);
			RiceID.tigrGO(tigrGoSlim);
			
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
