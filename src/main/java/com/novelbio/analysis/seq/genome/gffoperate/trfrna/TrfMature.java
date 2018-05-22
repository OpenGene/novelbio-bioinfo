package com.novelbio.analysis.seq.genome.gffoperate.trfrna;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.listoperate.ListDetailAbs;

public class TrfMature extends ListDetailAbs {
	
	String trfLoc;
	
	public TrfMature(String parentId, String name) {
		super(parentId, name, true);
	}
	
	public TrfMature clone() {
		TrfMature result = (TrfMature) super.clone();
		return result;
	}
	
	public void setTrfLoc(String trfLoc) {
		this.trfLoc = trfLoc;
	}
	
	public String getTrfLoc() {
		return trfLoc;
	}
	
	/** 获得该trfRNA成熟体的序列 */
	public SeqFasta getSeq() {
		if (listAbs == null) {
			return null;
		}
		TrfPre trfPre = getParent();
		if (trfPre == null || trfPre.getSeq() == null) {
			return null;
		}
		
		SeqFasta seqFastaMature = new SeqFasta();
		seqFastaMature.setName(getNameSingle());
		seqFastaMature.setSeq(trfPre.getSeq().toString().substring(getStartAbs()-1, getEndAbs()));
		seqFastaMature.setDNA(true);
		return seqFastaMature;
	}
	
	public TrfPre getParent() {
		if (super.getParent() == null) {
			return null;
		}
		return (TrfPre)super.getParent();
	}
}
