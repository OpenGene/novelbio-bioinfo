package com.novelbio.database.upDateDB.execute;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.net.ssl.SSLContext;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.upDateDB.dataBase.UpDateFriceDB;
import com.novelbio.database.upDateDB.gOextract.AffyChipGO;
import com.novelbio.database.upDateDB.gOextract.UniProtGo;
import com.novelbio.database.upDateDB.idConvert.RiceID;
public class runIDconvert {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try
		{
		
			// AgilentIDmodify.getInfo(9823, "/media/winE/Bioinformatics/Agilent/÷Ì/0912_026440_1291690870959/AllAnnotations/026440_D_AA_20100525.txt", 2, "/media/winE/Bioinformatics/Agilent/÷Ì/agilentPig.txt", "Agilent0912");
			// AgilentIDmodify.getInfo(9823, "/media/winE/Bioinformatics/Agilent/÷Ì/0804_020109_1291691130807/AllAnnotations/020109_D_AA_20100525.txt", 2, "/media/winE/Bioinformatics/Agilent/÷Ì/agilentPig2.txt", "Agilent0804");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/BLAST/result/susAgilent2RefSeqNCBIID.txt", "/media/winE/Bioinformatics/BLAST/result/out");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/Agilent/÷Ì/agilentPig.txt", "/media/winE/Bioinformatics/Agilent/÷Ì/out2");
			//UpDateFriceDB.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/cope/copesusAffy2humPro");
			//UpDateFriceDB.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/cope/copesus2humPro");
			//AffyIDmodify.getInfo(10090, "/home/zong0jie/◊¿√Ê/tmp/Mouse430_2.na31.annot.csvTT/Mouse430_2.na31.annot.xls", 2, "/home/zong0jie/◊¿√Ê/tmp/Mouse430_2.na31.annot.csvTT/Mouse430", "affyRice31");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/Agilent/caw/AgilentBta023647.txt", "/media/winE/Bioinformatics/Agilent/caw/out1");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/Agilent/caw/AgilentBta015354.txt", "/media/winE/Bioinformatics/Agilent/caw/out2");
			//RiceID.rapDB("/media/winE/Bioinformatics/GenomeData/Rice/RapDB/GFF3_representative/GFF3_representative/RAP_genes.gff3", "/media/winE/Bioinformatics/GenomeData/Rice/RapDB/GFF3_representative/GFF3_representative/RAP_genesOutOfNCBIID.gff3");
			//RiceID.getRAP2MSU("/media/winE/Bioinformatics/GenomeData/Rice/RapDB/RAP-MSU.txt/RAP-MSU.txt", "/media/winE/Bioinformatics/GenomeData/Rice/RapDB/RAP-MSU.txt/RAP-MSUOut.txt");
			//RiceID.getAffyID2LOC("/media/winE/Bioinformatics/Affymetrix/rice/affyidtolocidnew.txt", "/media/winE/Bioinformatics/Affymetrix/rice/affyidtolocidOut.txt");
			RiceID.rapDBGO("/media/winE/Bioinformatics/GenomeData/Rice/RapDB/GFF3_representative/GFF3_representative/RAP_genes.gff3");
			System.out.println("ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
