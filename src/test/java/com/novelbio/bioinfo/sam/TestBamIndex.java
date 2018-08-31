package com.novelbio.bioinfo.sam;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.sam.BamIndex;
import com.novelbio.bioinfo.sam.SamFile;

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
