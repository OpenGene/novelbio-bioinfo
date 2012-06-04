package com.novelbio.test.junit;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestArrayOperate extends TestCase {
	double[] aa = null;double[] bb = null;double[] cc = null;
	Integer[] aaa = null;Integer[] bbb = null;Integer[] ccc = null;
	
	
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
	
	@Test
	public void testIndeltArray()
	{
		ArrayList<int[]> lsIndelInfo = new ArrayList<int[]>();
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{1,-1});
		lsIndelInfo.add(new int[]{1,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,null);
		ccc = new Integer[]{null,null,2,3,4,5,6,7,8,9};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
		
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{1,-1});
		lsIndelInfo.add(new int[]{1,2});
		lsIndelInfo.add(new int[]{2,2});
		lsIndelInfo.add(new int[]{3,-1});
		lsIndelInfo.add(new int[]{5,2});
		lsIndelInfo.add(new int[]{6,-1});
		lsIndelInfo.add(new int[]{9,-1});
		lsIndelInfo.add(new int[]{10,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,0);
		ccc = new Integer[]{0,0,0,0,2,4,0,0,5,7,8,0,0};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
		
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{1,2});
		lsIndelInfo.add(new int[]{3,2});
		lsIndelInfo.add(new int[]{5,2});
		lsIndelInfo.add(new int[]{6,2});
		lsIndelInfo.add(new int[]{10,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,0);
		ccc = new Integer[]{0,0,1,2,0,0,3,4,0,0,5,0,0,6,7,8,9,0,0};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
	}
	
}
