package com.novelbio.analysis.tools;

import junit.framework.TestCase;

public class TestLumpyResultChange extends TestCase{

	String inpuVcfFile = "/home/novelbio/tmp/HCV_6WC_test.vcf";
	String string = "";
	String string2 = "";
//	VariantContext variantContext = new VariantContext();
	
	public void testIsBNDType () {
		LumpyResultChange lumpyResultChange = new LumpyResultChange();
		assertEquals(false, lumpyResultChange.isBNDType(string));
		assertEquals(true, lumpyResultChange.isBNDType(string2));
	}
	
	public void testGetBase() {
		LumpyResultChange lumpyResultChange = new LumpyResultChange();
		assertEquals("A", lumpyResultChange.getBase(string));
		assertEquals("G", lumpyResultChange.getBase(string2));
	}
	
	
	
}
