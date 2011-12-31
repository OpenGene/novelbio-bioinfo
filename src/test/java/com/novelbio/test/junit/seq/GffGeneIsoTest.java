package com.novelbio.test.junit.seq;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoTrans;

public class GffGeneIsoTest extends TestCase {
	@Test
	public void testGffGeneIsoCis()
	{
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis("aaa", "chr1", "fsefes");
		ArrayList<int[]> lsIsoform = new ArrayList<int[]>();
		lsIsoform.add(new int[]{0,3});
		lsIsoform.add(new int[]{5,10});
		lsIsoform.add(new int[]{20,30});
		lsIsoform.add(new int[]{40,50});
		gffGeneIsoCis.setLsIsoform(lsIsoform);
		int aa = gffGeneIsoCis.getLoc2ExInEnd(37);
		assertEquals(2, aa);
		aa = gffGeneIsoCis.getLoc2ExInStart(37);
		assertEquals(6, aa);
		aa = gffGeneIsoCis.getLoc2ExInEnd(23);
		assertEquals(7, aa);
		aa = gffGeneIsoCis.getLoc2ExInStart(23);
		assertEquals(3, aa);
	}
	
	@Test
	public void testGffGeneIsoTrans()
	{
		GffGeneIsoTrans gffGeneIsoCis = new GffGeneIsoTrans("aaa", "chr1", "fsefes");
		ArrayList<int[]> lsIsoform = new ArrayList<int[]>();
	
		
		lsIsoform.add(new int[]{50,40});
		lsIsoform.add(new int[]{30,20});
		lsIsoform.add(new int[]{10,5});
		lsIsoform.add(new int[]{3,0});
		
		gffGeneIsoCis.setLsIsoform(lsIsoform);
		
		int aa = gffGeneIsoCis.getLoc2ExInEnd(37);
		assertEquals(6, aa);
		aa = gffGeneIsoCis.getLoc2ExInStart(37);
		assertEquals(2, aa);
		aa = gffGeneIsoCis.getLoc2ExInEnd(23);
		assertEquals(3, aa);
		aa = gffGeneIsoCis.getLoc2ExInStart(23);
		assertEquals(7, aa);
	}
	
	
}
