package com.novelbio.analysis.seq.fastq;

import junit.framework.Assert;

import org.junit.Test;

public class TestFastQPE {
	@Test
	public void testReadPairend() {
		String file1 = "src/test/resources/test_file/fastq/PE/L_error.fq.gz";
		String file2 = "src/test/resources/test_file/fastq/PE/R_error.fq.gz";
		FastQ fastQ = new FastQ(file1);
		FastQ fastQ2 = new FastQ(file2);
		int i = 0;
		for (FastQRecord[] pe : fastQ.readlinesPE(fastQ2)) {
			i++;
			Assert.assertEquals(true, FastQRecord.isPairedByName(pe[0], pe[1]));
		}
		fastQ.close();
		fastQ2.close();
		Assert.assertEquals(1924, i);
	}
	
	@Test
	public void testReadPairend2() {
		FastQRecord[] pe = new FastQRecord[2];
		pe[0] = new FastQRecord();
		pe[1] = new FastQRecord();
		pe[0].setName("@V100000354L1C001R001000005/1");
		pe[1].setName("@V100000354L1C001R001000005/2");
		Assert.assertEquals(true, FastQRecord.isPairedByName(pe[0], pe[1]));

		pe[0].setName("@V100000354L1C001R001000015/1");
		pe[1].setName("@V100000354L1C001R001000005/2");
		Assert.assertEquals(false, FastQRecord.isPairedByName(pe[0], pe[1]));
	}

}
