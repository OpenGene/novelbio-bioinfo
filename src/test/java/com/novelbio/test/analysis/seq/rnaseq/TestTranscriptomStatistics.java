package com.novelbio.test.analysis.seq.rnaseq;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqfastaStatisticsCDS;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;

public class TestTranscriptomStatistics {
	TranscriptomStatistics transcriptomStatistics = new TranscriptomStatistics();
	protected void setUp() throws Exception {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertSeq();
	}
}
