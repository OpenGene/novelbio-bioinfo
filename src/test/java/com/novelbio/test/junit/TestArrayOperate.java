package com.novelbio.test.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestArrayOperate extends TestCase {
	double[] aa = null;double[] bb = null;double[] cc = null;
	@Test
	public void testCuttArray()
	{
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 3, 2, -1);
		cc = new double[]{1,2,3,4,5,6};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 5, 2, -1);
		cc = new double[]{-1,-1,1,2,3,4,5,6};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 5, 6, -1);
		cc = new double[]{-1,-1,1,2,3,4,5,6,7,8,9,-1};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 3, -1);
		cc = new double[]{3,4,5,6,7,8,9};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 4, -1);
		cc = new double[]{3,4,5,6,7,8,9,-1};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 2, -1);
		cc = new double[]{3,4,5,6,7,8};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 6, 2, -1);
		cc = new double[]{-1,1,2,3,4,5,6,7,8};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
	}
	

}
