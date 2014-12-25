package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestSpliceTestModule extends TestCase {
	
	public void testBalance() {
		SpliceTestRepeat spliceTestModule = new SpliceTestRepeat();
		List<List<Double>> lsSite2LsGroup = new ArrayList<>();
		List<Double> lsSite1 = new ArrayList<>();
		lsSite1.add(27.0); lsSite1.add(24.0); lsSite1.add(33.0);
		List<Double> lsSite2 = new ArrayList<>();
		lsSite2.add(36.0); lsSite2.add(21.0); lsSite2.add(27.0);
		List<Double> lsSite3 = new ArrayList<>();
		lsSite3.add(45.0); lsSite3.add(33.0); lsSite3.add(30.0);
		List<Double> lsSite4 = new ArrayList<>();
		lsSite4.add(21.0); lsSite4.add(45.0); lsSite4.add(37.0);
		lsSite2LsGroup.add(lsSite1); lsSite2LsGroup.add(lsSite2); lsSite2LsGroup.add(lsSite3); lsSite2LsGroup.add(lsSite4);
				
		List<List<Double>> lsSite2LsGroup2 = new ArrayList<>();
		lsSite1 = new ArrayList<>();
		lsSite1.add(28.0); lsSite1.add(20.0); lsSite1.add(33.0);
		lsSite2 = new ArrayList<>();
		lsSite2.add(34.0); lsSite2.add(22.0); lsSite2.add(29.0);
//		lsSite3 = new ArrayList<>();
//		lsSite3.add(44.0); lsSite3.add(33.0); lsSite3.add(30.0);
		lsSite2LsGroup2.add(lsSite1); lsSite2LsGroup2.add(lsSite2);
		
		List<List<Double>> listResult = new ArrayList<>();
//		spliceTestModule.setLsRepeat2Value(true, lsSite2LsGroup, lsSite2LsGroup2);
//		
//		assertEquals(30.0, spliceTestModule.getLsTreat2LsValue().get(0).get(0));
//		assertEquals(31.5, spliceTestModule.getLsTreat2LsValue().get(0).get(1));
//		assertEquals(33.25, spliceTestModule.getLsTreat2LsValue().get(0).get(2));
	}
	
	
	public void testConvert() {
//		SpliceTestModule spliceTestModule = new SpliceTestModule();
		List<List<Double>> lsSite2LsGroup = new ArrayList<>();
		List<Double> lsSite1 = new ArrayList<>();
		lsSite1.add(1.0); lsSite1.add(2.0); lsSite1.add(3.0);
		List<Double> lsSite2 = new ArrayList<>();
		lsSite2.add(10.0); lsSite2.add(20.0); lsSite2.add(30.0);
		List<Double> lsSite3 = new ArrayList<>();
		lsSite3.add(100.0); lsSite3.add(200.0); lsSite3.add(300.0);
		lsSite2LsGroup.add(lsSite1); lsSite2LsGroup.add(lsSite2); lsSite2LsGroup.add(lsSite3);
		
		List<List<Double>> listResult = new ArrayList<>();
//		spliceTestModule.convert(lsSite2LsGroup, listResult);
		
		assertEquals(1.0, listResult.get(0).get(0));
		assertEquals(10.0, listResult.get(0).get(1));
		assertEquals(100.0, listResult.get(0).get(2));
		
		assertEquals(2.0, listResult.get(1).get(0));
		assertEquals(20.0, listResult.get(1).get(1));
		assertEquals(200.0, listResult.get(1).get(2));
		
		assertEquals(3.0, listResult.get(2).get(0));
		assertEquals(30.0, listResult.get(2).get(1));
		assertEquals(300.0, listResult.get(2).get(2));
	}
}
