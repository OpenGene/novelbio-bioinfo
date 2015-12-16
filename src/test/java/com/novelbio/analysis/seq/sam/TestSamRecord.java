package com.novelbio.analysis.seq.sam;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.mapping.StrandSpecific;

public class TestSamRecord extends TestCase {
	public void testIsCis5to3ConsiderStrand() {
		assertEquals(null, SamRecord.isCis5to3ConsiderStrand(StrandSpecific.NONE, true, true));
		
		assertEquals(new Boolean(true), SamRecord.isCis5to3ConsiderStrand(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND, 
				true, true));
		
		assertEquals(new Boolean(false), SamRecord.isCis5to3ConsiderStrand(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND,
				true, false));
		
		assertEquals(new Boolean(false), SamRecord.isCis5to3ConsiderStrand(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND,
				false, true));
		
		assertEquals(new Boolean(true), SamRecord.isCis5to3ConsiderStrand(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND,
				false, false));


	}
}
