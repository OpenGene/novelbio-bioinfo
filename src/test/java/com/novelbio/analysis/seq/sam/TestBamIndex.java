package com.novelbio.analysis.seq.sam;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;

public class TestBamIndex {
	
	@Test
	public void testIndex() {
		SamFile samFile = new SamFile("src/test/resources/test_file/sam/wheat_sorted.bam");
		BamIndex bamIndex = new BamIndex(samFile);
		bamIndex.index();
		Assert.assertEquals(true, FileOperate.isFileExistAndBigThanSize("src/test/resources/test_file/sam/wheat_sorted.bam.bai", 0));
		Assert.assertEquals(false, FileOperate.isFileExistAndBigThanSize("src/test/resources/test_file/sam/wheat_sorted.bam_tmp.bai", 0));
		
	}
}
