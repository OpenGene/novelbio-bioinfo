package com.novelbio.analysis.tools.compare;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TestCombineTab {
	@Test
	public void testGetLsIntegers() {
		List<Integer> lsIntegers = CombineTab.getLsIntegers("1 2 3 4-6 9 12-14");
		List<Integer> lsIntegersExp = Lists.newArrayList(1,2,3,4,5,6,9,12,13,14);
		Assert.assertEquals(lsIntegers, lsIntegersExp);
	}
	
}
