package com.novelbio.bioinfo.mirna;

import com.novelbio.bioinfo.base.AlignExtend;
import com.novelbio.bioinfo.base.binarysearch.ListEle;
import com.novelbio.bioinfo.fasta.SeqFasta;

public class MirMature extends AlignExtend {
	MirPre mirPre;
		
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
		MirPre mirPre = getParent();
		if (mirPre == null || mirPre.getMirPreSeq() == null) {
			return null;
		}
		
		SeqFasta seqFastaMature = new SeqFasta();
		seqFastaMature.setName(getName());
		seqFastaMature.setSeq(mirPre.getMirPreSeq().toString().substring(getStartAbs()-1, getEndAbs()));
		seqFastaMature.setDNA(true);
		return seqFastaMature;
	}
	
	public MirPre getParent() {
		return mirPre;
	}

	@Override
	public String getName() {
		return mirAccID;
	}

	@Override
	public void setParent(ListEle<? extends AlignExtend> parent) {
		this.mirPre = (MirPre) parent;
		
	}
}
