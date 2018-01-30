package com.novelbio.analysis.seq.snphgvs;

import org.junit.Test;

import junit.framework.Assert;

public class TestSnpRefAltHgvspSimple {
	
	@Test
	public void testGetAAstartNum() {
		int num = SnpRefAltHgvsp.getStartSame(new char[]{'a','c','t','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(5, num);
		
		num = SnpRefAltHgvsp.getEndSame(new char[]{'a','c','t','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(5, num);
		
		num = SnpRefAltHgvsp.getStartSame(new char[]{'a','c','t','a','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(3, num);
		
		num = SnpRefAltHgvsp.getEndSame(new char[]{'a','c','t','a','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(2, num);
	}
}
