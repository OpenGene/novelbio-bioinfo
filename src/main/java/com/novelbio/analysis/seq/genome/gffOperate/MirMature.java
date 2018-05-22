package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.listoperate.ListDetailAbs;

public class MirMature extends ListDetailAbs {
	String mirAccID;
	String evidence;
	public MirMature clone() {
		MirMature result = (MirMature) super.clone();
		return result;
	}
	
	public void setMirAccID(String mirAccID) {
		this.mirAccID = mirAccID;
	}
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	public String getMirAccID() {
		return mirAccID;
	}
	public String getEvidence() {
		return evidence;
	}
	
	/** 获得该miRNA成熟体的序列 */
	public SeqFasta getSeq() {
		if (listAbs == null) {
			return null;
		}
		MirPre mirPre = getParent();
		if (mirPre == null || mirPre.getMirPreSeq() == null) {
			return null;
		}
		
		SeqFasta seqFastaMature = new SeqFasta();
		seqFastaMature.setName(getNameSingle());
		seqFastaMature.setSeq(mirPre.getMirPreSeq().toString().substring(getStartAbs()-1, getEndAbs()));
		seqFastaMature.setDNA(true);
		return seqFastaMature;
	}
	
	public MirPre getParent() {
		if (super.getParent() == null) {
			return null;
		}
		return (MirPre)super.getParent();
	}
}
