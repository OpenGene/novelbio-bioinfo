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
		String chrFile = "src/test/resources/test_file/denovo/merged.cap3.singlets.fa";
		N50statistics n50 = new N50statistics(chrFile);
		n50.doStatistics();
		n50.getLsNinfo();
		Assert.assertEquals(1705, n50.getAllContigsLen());
		Assert.assertEquals(266, n50.getN50Len());
		
		try{
//				n50.getN50Len(101);
	    }catch(Exception ex){
	    	Assert.assertTrue(ex instanceof ExceptionNBCsoft);
	    }
		
		
	}
}
