package com.novelbio.test.junit.seq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataStructure.MathComput;

import junit.framework.TestCase;

public class MathComputTest extends TestCase{
	
	double[] treatNum = null;
	double[] testResult = null;
	double[] result = null;
	@Before
	public void setUp() throws Exception
	{
		
	}
	@Test
	public void testMySplineHY()
	{
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
		testResult = MathComput.mySplineHY(treatNum, 3, 6, 3);
		result = new double[]{ 2,5,8,11,14,17};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
		testResult = MathComput.mySplineHY(treatNum, 3, 5, 3);
		result = new double[]{ 2,5,8,11,14,17};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
		testResult = MathComput.mySplineHY(treatNum, 3, 7, 3);
		result = new double[]{ 3,6,9,12,15,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
		testResult = MathComput.mySplineHY(treatNum, 3, 10, 3);
		result = new double[]{ 3,6,9,12,15,18,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		testResult = MathComput.mySplineHY(treatNum, 3, 10, 3);
		result = new double[]{ 3,6,9,12,15,18,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		testResult = MathComput.mySplineHY(treatNum, 3, 10, 3);
		result = new double[]{ 3,6,9,12,15,18,21};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{-1,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		testResult = MathComput.mySplineHY(treatNum, 3, 11, 3);
		result = new double[]{ -1,3,6,9,12,15,18,21};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{-1,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		testResult = MathComput.mySplineHY(treatNum, 3, 10, 3);
		result = new double[]{ 2,5,8,11,14,17,20,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		testResult = MathComput.mySplineHY(treatNum, 3, 1, 3);
		result = new double[]{ 3,6,9,12,15,18,21};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		treatNum = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		testResult = MathComput.mySplineHY(treatNum, 3, 1, 3);
		result = new double[]{ 3,6,9,12,15,18,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		treatNum = new double[]{-3, -2, -1,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
		testResult = MathComput.mySplineHY(treatNum, 4, 12, 2);
		result = new double[]{ -3,2,6,10,14,18,0};
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], testResult[i]);
		}
		
		
		
		
		
		
	}
	@After
	public void  clear() {
		treatNum = null;
		testResult = null;
		result = null;
	}
}
