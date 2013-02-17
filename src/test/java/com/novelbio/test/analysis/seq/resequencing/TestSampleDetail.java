package com.novelbio.test.analysis.seq.resequencing;

import org.junit.Test;

import com.novelbio.analysis.seq.resequencing.SnpGroupFilterInfo;
import com.novelbio.analysis.seq.resequencing.SnpIndelHomoHetoType;
import com.novelbio.analysis.seq.resequencing.SnpLevel;

import junit.framework.TestCase;

public class TestSampleDetail extends TestCase{
	SnpGroupFilterInfo sampleDetail2A = new SnpGroupFilterInfo();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Test
	public void testHomo() {
		sampleDetail2A.setSampleSnpRegion(SnpLevel.RefHomo, 1, 1);
		sampleDetail2A.setSampleSnpRegion(SnpLevel.HetoMid, 0, 0);
		sampleDetail2A.setSampleSnpRegion(SnpLevel.SnpHomo, 0, 0);
	
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHomo);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHetoMid);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.UnKnown);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.UnKnown);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.RefHomo);
		assertEquals(true, sampleDetail2A.isQualified());
	}
	
	@Test
	public void testHetoMore() {
		sampleDetail2A.setSampleSnpRegion(SnpLevel.RefHomo, 0, 0);
		sampleDetail2A.setSampleSnpRegion(SnpLevel.HetoMore, 1, 1);
	
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHetoLess);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHetoMore);
		assertEquals(true, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.SnpHetoMid);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.UnKnown);
		assertEquals(false, sampleDetail2A.isQualified());
		
		sampleDetail2A.clearData();
		sampleDetail2A.addSnpIndelHomoHetoType(SnpIndelHomoHetoType.RefHomo);
		assertEquals(false, sampleDetail2A.isQualified());
	}
}
