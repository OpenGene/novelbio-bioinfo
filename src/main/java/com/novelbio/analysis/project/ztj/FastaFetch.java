package com.novelbio.analysis.project.ztj;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FastaFetch {
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("(?<=consensus\\:Soybean\\:).+?(?=;)", Pattern.CASE_INSENSITIVE);
		Matcher ma = null;
		ma = pattern.matcher(">consensus:Soybean:Gma.10049.1.A1_at; gb|AW156249; /DB_XREF=se21e03.y1 /CLONE=GENOME SYSTEMS CLONE ID: Gm-c1015-1877");
		if (ma.find()) {
			System.out.println(ma.group());
		}
		
		getSeq("/media/winE/Bioinformatics/BLAST/sourceSeq/soybean/Soybean.consensus", 
				"(?<=consensus\\:Soybean\\:).+?(?=;)", "/media/winE/Bioinformatics/BLAST/sourceSeq/soybean/Soybean.coped.consensus");
	}
	public static void getSeq(String chrFile, String regx, String outFile) {
		SeqFastaHash seqHash = new SeqFastaHash(chrFile, regx, false, false);
		ArrayList<SeqFasta> lsSeqFastas = seqHash.getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (SeqFasta seqFasta : lsSeqFastas) {
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
	
	}
}
