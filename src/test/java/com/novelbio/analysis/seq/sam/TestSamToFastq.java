package com.novelbio.analysis.seq.sam;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import com.novelbio.analysis.seq.sam.SamToFastq.EnumSamToFastqType;

public class TestSamToFastq {
	
	@Test
	public void testSamRecordAllReads() {
		SamToFastq samToFastq = new SamToFastq();
		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.AllReads);
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));

		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.MappedReads);
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));
		
		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.MappedReadsOnlyOne);
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));
		
		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.MappedReadsPairend);
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));
		
		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.UnmappedReads);
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));
		
		samToFastq.setOutFileInfo(true, "/home/novelbio/test.fastq", EnumSamToFastqType.UnmappedReadsBoth);
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(true, true)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(true, false)));
		Assert.assertEquals(false, samToFastq.isCanAddSamRecord(generateSamRecord(false, true)));
		Assert.assertEquals(true, samToFastq.isCanAddSamRecord(generateSamRecord(false, false)));
	}
	
	private SamRecord generateSamRecord(boolean isMapped, boolean isMateMapped) {
		SamRecord samRecord =  PowerMockito.mock(SamRecord.class);
		PowerMockito.when(samRecord.isHavePairEnd()).thenReturn(true);

		PowerMockito.when(samRecord.isMapped()).thenReturn(isMapped);
		PowerMockito.when(samRecord.isMateMapped()).thenReturn(isMateMapped);
		return samRecord;
	}
}
