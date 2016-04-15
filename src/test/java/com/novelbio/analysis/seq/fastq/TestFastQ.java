package com.novelbio.analysis.seq.fastq;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestFastQ {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
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
	}
	
	@Test
	public void testReadPairedWithExcept() {
		String file = "src/test/resources/test_file/fastq/interleaved_with_many_error.fq";

		thrown.expect(ExceptionFastq.class);
		thrown.expectMessage(file+" fastq file error on line: 8616\nAlready read 96.72% of the file, if you think it almost finish, just use the tmp.fq.gz file. ");
		
		FastQ fastQ = new FastQ(file);
		int i = 0;
		for (FastQRecord[] fastQRecords : fastQ.readlinesInterleavedPE()) {
			i++;
	        	Assert.assertEquals(fastQRecords[0].getName(), fastQRecords[1].getName());
        }
		Assert.assertEquals(1062, i);
	}
}
