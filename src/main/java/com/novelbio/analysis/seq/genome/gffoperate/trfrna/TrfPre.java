package com.novelbio.analysis.seq.genome.gffoperate.trfrna;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.listoperate.ListBin;

public class TrfPre extends ListBin<TrfMature> {
	SeqFasta seqTrfPre;
	public void setTrfPreSeq(String mirPreSeq) {
		seqTrfPre = new SeqFasta();
		seqTrfPre.setName(getName());
		seqTrfPre.setSeq(mirPreSeq);
		seqTrfPre.setDNA(true);
	}
	public SeqFasta getSeq() {
		return seqTrfPre;
	}
	
	/** 因为一个miRpre只有1-2个mirMature，所以遍历效率也不低 */
	public TrfMature searchTrfName(String trfName) {
		for (TrfMature mirMature : getLsElement()) {
			if (mirMature.getNameSingle().equalsIgnoreCase(trfName)) {
				return mirMature;
			}
		}
		return null;
	}
	
}
