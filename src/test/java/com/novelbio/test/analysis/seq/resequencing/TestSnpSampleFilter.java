package com.novelbio.test.analysis.seq.resequencing;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;

public class TestSnpSampleFilter extends TestCase{
	SnpSampleFilter snpSampleFilter = new SnpSampleFilter();
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
				super.tearDown();
	}
	
	public void testIndelHetoLess() {
		SnpIndelHomoHetoType snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, 3, 100, 103);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		int numSnp = 3;
		int numRef = 100;
		int numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		
		numSnp = 10;
		numRef = 100;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
	
		numSnp = 1;
		numRef = 10;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		
		numSnp = 1;
		numRef = 3;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		
		numSnp = 1;
		numRef = 30;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
	}
	
	public void testIndelHeto() {
		SnpIndelHomoHetoType snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, 3, 100, 103);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		int numSnp = 12;
		int numRef = 100;
		int numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHeto, snpIndelHomoHetoType);

		numSnp = 15;
		numRef = 100;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHeto, snpIndelHomoHetoType);
		
		numSnp = 2;
		numRef = 4;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHeto, snpIndelHomoHetoType);
		
		numSnp = 2;
		numRef = 17;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHeto, snpIndelHomoHetoType);
	}
	
	public void testIndelHetoMore() {
		SnpIndelHomoHetoType snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, 3, 100, 103);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		int numSnp = 100;
		int numRef = 10;
		int numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoMore, snpIndelHomoHetoType);
		
		numSnp = 100;
		numRef = 2;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHomo, snpIndelHomoHetoType);
		
		numSnp = 10;
		numRef = 1;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoMore, snpIndelHomoHetoType);
		
		numSnp = 3;
		numRef = 1;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoMore, snpIndelHomoHetoType);
	}
	
	public void testIndelRefHomo() {
		SnpIndelHomoHetoType snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, 3, 100, 103);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		int numSnp = 3;
		int numRef = 100;
		int numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.IndelHetoLess, snpIndelHomoHetoType);
		
		numSnp = 0;
		numRef = 10;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.RefHomo, snpIndelHomoHetoType);
		
		numSnp = 1;
		numRef = 100;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.RefHomo, snpIndelHomoHetoType);
		
		numSnp = 1;
		numRef = 50;
		numAll = numSnp + numRef;
		snpIndelHomoHetoType = snpSampleFilter.getSnpIndelType(SnpIndelType.DELETION, numSnp, numRef, numAll);
		assertEquals(SnpIndelHomoHetoType.RefHomo, snpIndelHomoHetoType);
	}
}
