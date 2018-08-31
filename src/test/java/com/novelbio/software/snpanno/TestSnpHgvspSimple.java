package com.novelbio.software.snpanno;

import org.junit.Test;

import com.novelbio.software.snpanno.SnpIsoHgvsp;

import junit.framework.Assert;

public class TestSnpHgvspSimple {
	
	@Test
	public void testGetAAstartNum() {
		int num = SnpIsoHgvsp.getStartSame(new char[]{'a','c','t','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(5, num);
		
		num = SnpIsoHgvsp.getEndSame(new char[]{'a','c','t','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(5, num);
		
		num = SnpIsoHgvsp.getStartSame(new char[]{'a','c','t','a','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(3, num);
		
		num = SnpIsoHgvsp.getEndSame(new char[]{'a','c','t','a','c','g'}, new char[]{'a','c','t','c','g'});
		Assert.assertEquals(2, num);
	}
}
