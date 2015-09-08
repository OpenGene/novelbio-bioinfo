package com.novelbio.analysis.seq.fastq;

import org.junit.Assert;
import org.junit.Test;

public class TestFastQ {
	@Test
	public void testReadPairend() {
		String file = "src/test/resources/test_file/fastq/interleaved.fq";
		FastQ fastQ = new FastQ(file);
		int i = 0;
		for (FastQRecord[] fastQRecords : fastQ.readlinesInterleavedPE()) {
			i++;
	        	Assert.assertEquals(fastQRecords[0].getName(), fastQRecords[1].getName());
        }
		Assert.assertEquals(1065, i);
		
		file = "src/test/resources/test_file/fastq/interleaved_with_error.fq";
		fastQ = new FastQ(file);
		i = 0;
		for (FastQRecord[] fastQRecords : fastQ.readlinesInterleavedPE()) {
			i++;
	        	Assert.assertEquals(fastQRecords[0].getName(), fastQRecords[1].getName());
        }
		Assert.assertEquals(1062, i);
		
		file = "src/test/resources/test_file/fastq/leftend.fq";
		fastQ = new FastQ(file);
		i = 0;

		try {
			for (FastQRecord[] fastQRecords : fastQ.readlinesInterleavedPE()) {
				i++;
		        	Assert.assertEquals(fastQRecords[0].getName(), fastQRecords[1].getName());
	        }
			Assert.fail("No Exception thrown.");
		} catch (Exception e) {
			Assert.assertEquals(true, e instanceof ExceptionFastq);
		}
		
		Assert.assertEquals(0, i);
		
		
		file = "src/test/resources/test_file/fastq/interleaved_with_many_error.fq";
		fastQ = new FastQ(file);
		i = 0;
		for (FastQRecord[] fastQRecords : fastQ.readlinesInterleavedPE()) {
			i++;
	        	Assert.assertEquals(fastQRecords[0].getName(), fastQRecords[1].getName());
        }
		Assert.assertEquals(1062, i);
	}
}
