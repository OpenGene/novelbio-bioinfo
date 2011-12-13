package com.novelbio.database.updatedb.bast;

import com.novelbio.analysis.annotation.blast.Blast2DB;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.database.updatedb.database.UpDateFriceDB;
import com.novelbio.database.updatedb.database.UpDateNBCDBFile;

public class BlastMaize {
	public static void main(String[] args) {
		BlastMaize blastMaize = new BlastMaize();
//		blastMaize.getSeq("/home/zong0jie/×ÀÃæ/zeamaize/ZmB73_5a_WGS_translations.fasta/ZmB73_5a_WGS_translations.fasta");
//		try {
//			Blast2DB.copeBlastResult("/media/winE/Bioinformatics/BLAST/result/maize/maize2AthFinal5b.txt", 4577, 3702, "MGDB", "TAIR7",
//					"/media/winE/Bioinformatics/BLAST/result/maize/maize2AthFinal5bCoped.txt", false, null);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
			UpDateNBCDBFile.upDateBlastInfo("/media/winE/Bioinformatics/BLAST/result/maize/maize2AthFinal5bCoped.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getSeq(String fastaFileMaize)
	{
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFileMaize, "(?<=parent_gene=)\\w+", true, false);
		seqFastaHash.writeFileSep("/home/zong0jie/×ÀÃæ/zeamaize/ZmB73_5a_WGS_translations.fasta/blastQ", "maize", new int[]{-1,-1}, false, 80);
	}
	
 }
