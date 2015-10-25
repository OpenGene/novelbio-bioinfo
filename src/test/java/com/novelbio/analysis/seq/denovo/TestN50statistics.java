package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.novelbio.analysis.ExceptionNBCsoft;


/** 测试N50的统计 */
public class TestN50statistics {
	public void testN50() {
		List<Integer> lsSeqLen = new ArrayList<Integer>();
		lsSeqLen.add(100);
		lsSeqLen.add(20);
		lsSeqLen.add(150);
		lsSeqLen.add(110);
		lsSeqLen.add(120);
		lsSeqLen.add(120);
		lsSeqLen.add(120);
		lsSeqLen.add(120);
		lsSeqLen.add(120);
		lsSeqLen.add(120);
		
		int num = 50;
//		N50statistics n50 = new N50statistics();
//		n50.set(lsSeqLen);
//		
//		Assert.assertEquals(110, n50.getN50Len(50));
//		Assert.assertEquals(130, n50.getN50Len(40));
//		Assert.assertEquals(2, n50.);
//		Assert.assertEquals(3, n50.getN50Num(40));
		
		try{
//				n50.getN50Len(101);
				Assert.fail("No exception thrown.");
	    }catch(Exception ex){
	    	Assert.assertTrue(ex instanceof ExceptionNBCsoft);
	    }
		
		
	}
}
