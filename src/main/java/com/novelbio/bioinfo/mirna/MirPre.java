package com.novelbio.bioinfo.mirna;

import com.novelbio.bioinfo.base.binarysearch.ListEle;
import com.novelbio.bioinfo.fasta.SeqFasta;

public class MirPre extends ListEle<MirMature> {
	
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
			if (mirMature.getName().equalsIgnoreCase(mirName)) {
				return mirMature;
			}
		}
		return null;
	}
	
}
