package com.novelbio.test.junit.seq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.genomeNew2.GffChrHanYanChrom;
import com.novelbio.analysis.seq.mapping.SAMtools;

import junit.framework.TestCase;

public class SAMtoolsTest extends TestCase{
	int testFlag = 0;
	@Before
	public void setUp() throws Exception
	{
		testFlag = 0;
	}
	
	@Test
	public void test()
	{
//		145 = 128 + 16 + 1
		testFlag = 145;
		int[] flag = SAMtools.getFlag(testFlag);
		String resultFlag = ""; 
		for (int i : flag) {
			resultFlag = resultFlag + i;
		}
		assertEquals("1000100100",resultFlag);
		
		
		testFlag = 163;
		flag = SAMtools.getFlag(testFlag);
		resultFlag = ""; 
		for (int i : flag) {
			resultFlag = resultFlag + i;
		}
		assertEquals("1100010100",resultFlag);
		
		testFlag = 99;
		flag = SAMtools.getFlag(testFlag);
		resultFlag = ""; 
		for (int i : flag) {
			resultFlag = resultFlag + i;
		}
		assertEquals("1100011000",resultFlag);
		
		
		testFlag = 99+512;
		flag = SAMtools.getFlag(testFlag);
		resultFlag = ""; 
		for (int i : flag) {
			resultFlag = resultFlag + i;
		}
		assertEquals("1100011001",resultFlag);
		
		testFlag = 97;
		flag = SAMtools.getFlag(testFlag);
		resultFlag = ""; 
		for (int i : flag) {
			resultFlag = resultFlag + i;
		}
		assertEquals("1000011000",resultFlag);
		
	}
	
	@After
	public void  clear() {
		
	}
	
}
