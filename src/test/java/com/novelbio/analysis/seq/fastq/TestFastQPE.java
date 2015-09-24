package com.novelbio.analysis.seq.fastq;

import junit.framework.Assert;

import org.junit.Test;

public class TestFastQPE {
	@Test
	public void testReadPairend() {
		String file1 = "src/test/resources/test_file/fastq/PE/Lerror.fq";
		String file2 = "src/test/resources/test_file/fastq/PE/Rerror.fq";
		FastQ fastQ = new FastQ(file1);
		FastQ fastQ2 = new FastQ(file2);
		int i = 0;
		for (FastQRecord[] pe : fastQ.readlinesPE(fastQ2)) {
			i++;
			Assert.assertEquals(true, FastQRecord.isPairedByName(pe[0], pe[1]));
		}
		fastQ.close();
		fastQ2.close();
		Assert.assertEquals(1930, i);
	}
}
