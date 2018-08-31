package com.novelbio.bioinfo.fasta;

import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqfastaStatisticsCDS;

import junit.framework.TestCase;

public class TestSeqfastaStatisticsCDS extends TestCase{
	String AAseq;
	SeqfastaStatisticsCDS statisticSeqCdsInfo = new SeqfastaStatisticsCDS(new SeqFasta());
	protected void setUp() throws Exception {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertSeqStatistics();
	}
	
	private void assertSeqStatistics() {
		AAseq = "feswtxdhrt*fseeg*geseresr*Mstgeseg*sgeeesgsetetest*";
		statisticSeqCdsInfo.calculateFullLengthCDS(AAseq, 1, false);
		assertEquals(15, statisticSeqCdsInfo.getAllAAlen());
		assertEquals(8, statisticSeqCdsInfo.getMstartAAlen());
		assertEquals(1, statisticSeqCdsInfo.getOrfAllLen());
		assertEquals(35, statisticSeqCdsInfo.getStartIndexAllAA());
		assertEquals(26, statisticSeqCdsInfo.getStartIndexMAA());
		assertEquals(true, statisticSeqCdsInfo.isFullCds());
		
		AAseq = "feswtxdhrt*fseeg*geseresr*Mstgeseg*sgeeesgsetetest";
		statisticSeqCdsInfo.calculateFullLengthCDS(AAseq, 1, false);
		assertEquals(15, statisticSeqCdsInfo.getAllAAlen());
		assertEquals(8, statisticSeqCdsInfo.getMstartAAlen());
		assertEquals(1, statisticSeqCdsInfo.getOrfAllLen());
		assertEquals(35, statisticSeqCdsInfo.getStartIndexAllAA());
		assertEquals(26, statisticSeqCdsInfo.getStartIndexMAA());
		assertEquals(true, statisticSeqCdsInfo.isFullCds());

		AAseq = "feswtxdhrt*fseeg*geseresr*Mstgesegsgeeesgsetetest";
		statisticSeqCdsInfo.calculateFullLengthCDS(AAseq, 1, false);
		assertEquals(23, statisticSeqCdsInfo.getAllAAlen());
		assertEquals(23, statisticSeqCdsInfo.getMstartAAlen());
		assertEquals(1, statisticSeqCdsInfo.getOrfAllLen());
		assertEquals(26, statisticSeqCdsInfo.getStartIndexAllAA());
		assertEquals(26, statisticSeqCdsInfo.getStartIndexMAA());
		assertEquals(false, statisticSeqCdsInfo.isFullCds());

	}
}
