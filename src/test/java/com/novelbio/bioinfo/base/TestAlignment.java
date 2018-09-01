package com.novelbio.bioinfo.base;

import org.junit.Test;

import com.novelbio.bioinfo.base.Alignment;

import junit.framework.Assert;

public class TestAlignment {
	@Test
	public void testGetDistance() {
		Alignment align1 = new Align("chr1:123-400");
		Align align2 = new Align("chr1:700-900");
		int distance = Alignment.getDistance(align1, align2);
		Assert.assertEquals(300, distance);
		
		align1 = new Align("chr1:400-123");
		align2 = new Align("chr1:900-700");
		distance = Alignment.getDistance(align1, align2);
		Assert.assertEquals(300, distance);
	}
}
