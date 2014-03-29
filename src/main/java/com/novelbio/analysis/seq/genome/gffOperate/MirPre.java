package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.listOperate.ListBin;

public class MirPre extends ListBin<MirMature> {
	SeqFasta seqMirPre;
	public void setMirPreSeq(String mirPreSeq) {
		seqMirPre = new SeqFasta();
		seqMirPre.setName(getName());
		seqMirPre.setSeq(mirPreSeq);
		seqMirPre.setDNA(true);
	}
	public SeqFasta getMirPreSeq() {
		return seqMirPre;
	}
	
}
