package com.novelbio.test.junit.seq;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoTrans;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;

public class GffGeneIsoTest extends TestCase {
	@Test
	public void testGffGeneIsoCis()
	{
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis("aaa", "chr1", "fsefes");
		gffGeneIsoCis.add(new ExonInfo(0, 3, true));
		gffGeneIsoCis.add(new ExonInfo(5, 10, true));
		gffGeneIsoCis.add(new ExonInfo(20, 30, true));
		gffGeneIsoCis.add(new ExonInfo(40, 50, true));
		int aa = 0;
		gffGeneIsoCis.setCoord(37); 	aa = gffGeneIsoCis.getCod2ExInEnd();
		assertEquals(2, aa);
		aa = gffGeneIsoCis.getCod2ExInStart();
		assertEquals(6, aa);
		
		gffGeneIsoCis.setCoord(23); 	aa = gffGeneIsoCis.getCod2ExInEnd();
		assertEquals(7, aa);
		aa = gffGeneIsoCis.getCod2ExInStart();
		assertEquals(3, aa);

	}
	
	@Test
	public void testGffGeneIsoTrans()
	{
		GffGeneIsoTrans gffGeneIsoCis = new GffGeneIsoTrans("aaa", "chr1", "fsefes");
		gffGeneIsoCis.add(new ExonInfo(50, 40, false));
		gffGeneIsoCis.add(new ExonInfo(30, 10, false));
		gffGeneIsoCis.add(new ExonInfo(10, 5, false));
		gffGeneIsoCis.add(new ExonInfo(3, 0, false));
		int aa = 0;
		gffGeneIsoCis.setCoord(37); 	aa = gffGeneIsoCis.getCod2ExInEnd();
		assertEquals(6, aa);
		aa = gffGeneIsoCis.getCod2ExInStart();
		assertEquals(2, aa);
		gffGeneIsoCis.setCoord(23); 	
		aa = gffGeneIsoCis.getCod2ExInEnd();
		assertEquals(13, aa);
		aa = gffGeneIsoCis.getCod2ExInStart();
		assertEquals(7, aa);
 
	}
	
	
}
