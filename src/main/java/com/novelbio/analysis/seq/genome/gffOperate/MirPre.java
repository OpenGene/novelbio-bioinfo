package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.listoperate.ListBin;

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
	
	/** 因为一个miRpre只有1-2个mirMature，所以遍历效率也不低 */
	public MirMature searchMirName(String mirName) {
		for (MirMature mirMature : getLsElement()) {
			if (mirMature.getNameSingle().equalsIgnoreCase(mirName)) {
				return mirMature;
			}
		}
		return null;
	}
	
}
