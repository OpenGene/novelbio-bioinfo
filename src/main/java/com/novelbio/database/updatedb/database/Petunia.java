package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqFastaHash;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
public class Petunia {
	public static void main(String[] args) {
		GeneID copedID = new GeneID("PH_TC2092", 0);
		System.out.println(copedID.getTaxID());
		;
//		upDateInfo();
//		updateAffy2AccID(4102, "/media/winE/Bioinformatics/BLAST/result/petunia/Nembgen2plantGDBIDCoped.txt");
//		upDateBlastInfo();
	}
	
	
	public static void getSeq() {
		String seqIn = "/home/zong0jie/桌面/矮牵牛/Petunia_x_hybrida.mRNA.PUT.fasta";
		String regx = "PUT-159a-Petunia_x_hybrida-\\d+";
		String seqOut = FileOperate.changeFileSuffix(seqIn, "_Coped", null);
		SeqFastaHash seqFastaHash = new SeqFastaHash(seqIn,regx, false);
		seqFastaHash.writeToFile(seqOut);
	}
	
	public static void upDateInfo() {
		String seqIn = "/media/winE/Bioinformatics/GenomeData/petunia/Petunia_x_hybrida.mRNA.PUT_Coped.fasta";
		String regx = "PUT-159a-Petunia_x_hybrida-\\d+";
		SeqFastaHash seqFastaHash = new SeqFastaHash(seqIn,regx, false);
		ArrayList<SeqFasta> lsSeqFastas = seqFastaHash.getSeqFastaAll();
		for (SeqFasta seqFasta : lsSeqFastas) {
			GeneID copedID = new GeneID(seqFasta.getSeqName(), 4102);
			copedID.setUpdateDBinfo(DBAccIDSource.PlantGDB, true);
			copedID.update(true);
		}
	}
	
	public static void upDateBlastInfo() {
		String filepath = "/media/winE/Bioinformatics/BLAST/result/petunia/pet2Ath";
		TxtReadandWrite txtBlast = new TxtReadandWrite(filepath, false);
		for (String content : txtBlast.readlines()) {
			String[] ss = content.split("\t");
			GeneID copedID = new GeneID(ss[0], 4102);
			copedID.setUpdateDBinfo(DBAccIDSource.PlantGDB, true);
//			copedID.setUpdateBlastInfo(ss[1], DBAccIDSource.TAIR_ATH.toString(), 3702, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
			copedID.update(true);
		}
		
		
	}
	
	private static void updateAffy2AccID(int taxID, String affy2) {
		TxtReadandWrite txtRead = new TxtReadandWrite(affy2, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			GeneID copedID = new GeneID(ss[0], taxID);
			copedID.setUpdateRefAccID(ss[1]);
			copedID.setUpdateDBinfo(DBAccIDSource.Array_Nemblgen, true);
			copedID.update(true);
		}
	}
}
