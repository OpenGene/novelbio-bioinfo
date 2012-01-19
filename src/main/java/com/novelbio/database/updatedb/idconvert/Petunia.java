package com.novelbio.database.updatedb.idconvert;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;

public class Petunia {
	public static void main(String[] args) {
		updateAffy2AccID(4102, "/media/winE/Bioinformatics/BLAST/result/petunia/Nembgen2plantGDBIDCoped.txt");
	}
	
	
	public static void getSeq()
	{
		String seqIn = "/home/zong0jie/×ÀÃæ/°«Ç£Å£/Petunia_x_hybrida.mRNA.PUT.fasta";
		String regx = "PUT-159a-Petunia_x_hybrida-\\d+";
		String seqOut = FileOperate.changeFileSuffix(seqIn, "_Coped", null);
		SeqFastaHash seqFastaHash = new SeqFastaHash(seqIn,regx, false, false);
		seqFastaHash.writeToFile(seqOut);
	}
	
	public static void upDateInfo() {
		String seqIn = "/home/zong0jie/×ÀÃæ/°«Ç£Å£/Petunia_x_hybrida.mRNA.PUT.fasta";
		String regx = "PUT-159a-Petunia_x_hybrida-\\d+";
		SeqFastaHash seqFastaHash = new SeqFastaHash(seqIn,regx, false, false);
		ArrayList<SeqFasta> lsSeqFastas = seqFastaHash.getSeqFastaAll();
		for (SeqFasta seqFasta : lsSeqFastas) {
			CopedID copedID = new CopedID(seqFasta.getSeqName(), 4102);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_PLANTGDB_ACC, true);
			copedID.update(true);
		}
	}
	
	public static void upDateBlastInfo()
	{
		String filepath = "/media/winE/Bioinformatics/BLAST/result/petunia/pet2Ath";
		TxtReadandWrite txtBlast = new TxtReadandWrite(filepath, false);
		for (String content : txtBlast.readlines()) {
			String[] ss = content.split("\t");
			CopedID copedID = new CopedID(ss[0], 4102);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_PLANTGDB_ACC, true);
			copedID.setUpdateBlastInfo(ss[1], NovelBioConst.DBINFO_ATH_TAIR, 3702, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
			copedID.update(true);
		}
		
		
	}
	
	private static void updateAffy2AccID(int taxID, String affy2)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(affy2, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			CopedID copedID = new CopedID(ss[0], taxID);
			copedID.setUpdateRefAccID(ss[1]);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ROCHE, true);
			copedID.update(true);
		}
	}
}
