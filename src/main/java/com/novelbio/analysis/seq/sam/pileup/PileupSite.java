package com.novelbio.analysis.seq.sam.pileup;

import java.util.ArrayList;
import java.util.List;

public class PileupSite {
	String refID;
	long startSite;
	List<PileupUnit> lsPileUp = new ArrayList<PileupUnit>();

	public static class PileupUnit {
		/** 可以有单碱基，以及插入和缺失等 */
		String base;
		/** 0 表示起点，1表示中间，2表示结尾 */
		int startEnd;
		/** 本base的质量 */
		char quality;
	}
	
	
}


