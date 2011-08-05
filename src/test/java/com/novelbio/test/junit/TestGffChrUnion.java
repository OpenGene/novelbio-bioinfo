package com.novelbio.test.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.CopyOfGffChrUnion;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashUCSCgene;

public class TestGffChrUnion extends TestCase {
	double[] input;
	double[] tmpResult = null;
	@Before
	public void setUp() throws Exception
	{
		input = new double[]{4,1,2,3,4,5,6,7,8};
	}
	
	@Test
	public void test()
	{
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, 2, 4,-1);
//		assertEquals(new double[]{2,3,4,5,6,7,8}, tmpResult);
		
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, 2, 6,-1);
//		assertEquals(new double[]{2,3,4,5,6,7,8,-1,-1}, tmpResult);
		
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, 2, -1,-1);
//		assertEquals(new double[]{2,3,4,5,6,7,8,-1,-1,-1,-1}, tmpResult);
		
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, 3, -1,-1);
//		assertEquals(new double[]{1,2,3, 4,5,6,7,8,-1,-1,-1,-1}, tmpResult);
		
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, 5, -1,-1);
//		assertEquals(new double[]{-1,-1,1,2,3, 4,5,6,7,8,-1,-1,-1,-1}, tmpResult);
		tmpResult = CopyOfGffChrUnion.setDouble(input, 6, 8, -1, -1,-1);
//		assertEquals(new double[]{-1,-1,-1,1,2,3, 4,5,6,7,8,-1,-1,-1,-1}, tmpResult);
		System.out.println("aa");
	}
	
	@After
	public void  clear() {
		input = null;
		
	}
	
}