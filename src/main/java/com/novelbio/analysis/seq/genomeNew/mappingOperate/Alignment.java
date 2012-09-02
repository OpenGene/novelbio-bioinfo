package com.novelbio.analysis.seq.genomeNew.mappingOperate;

public interface Alignment {
	int getStartAbs();
	int getEndAbs();
	int getStartCis();
	int getEndCis();
	Boolean isCis5to3();
	int Length();
	String getRefID();
}
