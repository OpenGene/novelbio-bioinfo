package com.novelbio.analysis.seq.sam;

import org.junit.Test;

public class TestSamToBamSort {
	@Test
	public void testConvert() {
		String inFile = "/home/novelbio/testJava/samReducer/samToBamSort_raw.sam";
		String outFile = "/home/novelbio/testJava/samReducer/TestSamToBamSort.sam";
		SamToBamSort samToBamSort = new SamToBamSort(outFile, new SamFile(inFile));
		samToBamSort.setAddMultiHitFlag(true);
		samToBamSort.setNeedSort(true);
		samToBamSort.setUsingTmpFile(true);
		samToBamSort.convert();
	}
}
