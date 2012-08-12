package com.novelbio.test.analysis.seq.resequencing;

import com.novelbio.analysis.seq.resequencing.SampleDetail;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;

import junit.framework.TestCase;

public class TestSampleDetail extends TestCase{
	SampleDetail sampleDetail2A = new SampleDetail();
	@Override
	protected void setUp() throws Exception {

		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
				super.tearDown();
	}
	
	public void test() {
		sampleDetail2A.setSampleRefHomoNum(1, 1);
		sampleDetail2A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail2A.setSampleSnpIndelHomoNum(0, 0);
	
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHomo);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHeto);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.UnKnown);
		assertEquals(false, sampleDetail2A.isQualified());
		

	}
}
