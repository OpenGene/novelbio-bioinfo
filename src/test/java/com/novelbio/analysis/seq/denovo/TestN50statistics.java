package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.ExceptionNBCsoft;


/** 测试N50的统计 */
public class TestN50statistics {
	@Test
	public void testN50() {
//		List<Integer> lsSeqLen = new ArrayList<Integer>();
//		lsSeqLen.add(100);
//		lsSeqLen.add(20);
//		lsSeqLen.add(30);
//		lsSeqLen.add(40);
//		lsSeqLen.add(50);
//		lsSeqLen.add(130);
//		lsSeqLen.add(120);
//		lsSeqLen.add(110);
//		lsSeqLen.add(90);
//		lsSeqLen.add(80);
//		
//		int num = 50;
//		n50.set(lsSeqLen);
		
//		String chrFile = "/home/novelbio/git/NBCplatform/src/test/resources/test_file/denovo/merged.cap3.singlets.fa";
		String chrFile = "/home/novelbio/git/NBCplatform/src/test/resources/test_file/denovo/Trinity.fasta.gz";
		N50statistics n50 = new N50statistics(chrFile);
		n50.doStatistics();
		n50.getLsNinfo();
//		Assert.assertEquals(1705, n50.getAllContigsLen());
//		Assert.assertEquals(275, n50.getN50Len());
//		Assert.assertEquals(266, n50.getN50Len());	
		
		Assert.assertEquals(198559, n50.getAllContigsLen());
		
//		Trinity计算出来的N50结果为5519,我们计算出来的是5460;	
		Assert.assertEquals(5519, n50.getN50Len());

		Assert.assertEquals(5460, n50.getN50Len());
		
//		Assert.assertEquals(1705, n50.getAllContigsLen());
//		Assert.assertEquals(130, n50.getN50Len(40));
//		Assert.assertEquals(2, n50.);
//		Assert.assertEquals(3, n50.getN50Num(40));
		
		
		
		
		try{
//				n50.getN50Len(101);
	    }catch(Exception ex){
	    	Assert.assertTrue(ex instanceof ExceptionNBCsoft);
	    }
		
		
	}
}
