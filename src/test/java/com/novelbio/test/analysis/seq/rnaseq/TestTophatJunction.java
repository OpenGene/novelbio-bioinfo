package com.novelbio.test.analysis.seq.rnaseq;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.rnaseq.TophatJunction;
//TODO 还没写完
public class TestTophatJunction extends TestCase {
	TophatJunction tophatJunction = new TophatJunction();
	protected void setUp() throws Exception {
		tophatJunction.setJunFile("KO", "src/main/resources/Test/TestTophatJunction/KOjunctions.bed");
		tophatJunction.setJunFile("WT", "src/main/resources/Test/TestTophatJunction/WTjunctions.bed");
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertEquals(145, tophatJunction.getJunctionSite("KO", "chr1", 4782733));
		assertEquals(146, tophatJunction.getJunctionSite("WT", "chr1", 4782733));
	}

}
