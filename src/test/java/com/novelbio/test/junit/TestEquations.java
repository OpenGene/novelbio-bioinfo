package com.novelbio.test.junit;

import org.junit.Test;

import com.novelbio.base.dataStructure.Equations;

import junit.framework.TestCase;

public class TestEquations extends TestCase{
	Equations equations = new Equations();
	@Test
	public void testgetYinside()
	{
		double y = equations.getYinside(new double[]{0,0}, new double[]{1,1}, 0.2);
		assertEquals(0.2, y);
		y = equations.getYinside(new double[]{0,1}, new double[]{1,1.5}, 0.2);
		assertEquals(1.1, y);
		y = equations.getYinside(new double[]{2,3}, new double[]{4,1}, 3.5);
		assertEquals(1.5, y);
	}
}
