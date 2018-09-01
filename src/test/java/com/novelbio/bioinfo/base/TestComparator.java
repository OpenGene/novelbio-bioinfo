package com.novelbio.bioinfo.base;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.base.Alignment.ComparatorAlignment;

public class TestComparator {
	
	@Test
	public void testComparator() {
		ComparatorAlignment comparatorAlignment = new ComparatorAlignment();
		Align align1 = new Align("chr1", 200, 300);
		Align align2 = new Align("chr1", 200, 400);
		int result = comparatorAlignment.compare(align1, align2);
		Assert.assertEquals(-1, result);
		
		align1 = new Align("chr1", 200, 300);
		align2 = new Align("chr1", 100, 400);
		result = comparatorAlignment.compare(align1, align2);
		Assert.assertEquals(1, result);
		
		align1 = new Align("chr1", 300, 200);
		align2 = new Align("chr1", 400, 100);
		result = comparatorAlignment.compare(align1, align2);
		Assert.assertEquals(1, result);
	}
}
