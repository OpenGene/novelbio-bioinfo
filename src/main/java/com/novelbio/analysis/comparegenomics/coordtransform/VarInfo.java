package com.novelbio.analysis.comparegenomics.coordtransform;

import com.novelbio.analysis.seq.mapping.Align;

public class VarInfo extends Align {
	String ref;
	String alt;
	public void setRef(String ref) {
		this.ref = ref;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public String getRef() {
		return ref;
	}
	public String getAlt() {
		return alt;
	}
}
