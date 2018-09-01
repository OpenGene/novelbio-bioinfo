package com.novelbio.test.junit.seq;

import org.junit.Test;

import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;

import junit.framework.TestCase;

public class GffGeneIsoTest extends TestCase {
	@Test
	public void testGffGeneIsoCis() {
		GffGene gffDetailGene = new GffGene("chr1","sefes", true);
		GffIso gffGeneIsoCis = GffIso.createGffGeneIso("aaa",gffDetailGene.getNameSingle(), gffDetailGene, GeneType.mRNA, true);
		gffGeneIsoCis.add(new ExonInfo(true, 0, 3));
		gffGeneIsoCis.add(new ExonInfo(true,5, 10));
		gffGeneIsoCis.add(new ExonInfo(true,20, 30));
		gffGeneIsoCis.add(new ExonInfo(true,40, 50));
		int aa = 0;
		aa = gffGeneIsoCis.getCod2ExInEnd(37);
		assertEquals(2, aa);
		aa = gffGeneIsoCis.getCod2ExInStart(37);
		assertEquals(6, aa);
		
		aa = gffGeneIsoCis.getCod2ExInEnd(23);
		assertEquals(7, aa);
		aa = gffGeneIsoCis.getCod2ExInStart(23);
		assertEquals(3, aa);
		
		aa = gffGeneIsoCis.getLenExon(2);
		assertEquals(6, aa);
		
		aa = gffGeneIsoCis.getLenExon(0);
		assertEquals(32, aa);
		
		aa = gffGeneIsoCis.getLenExon(2);
		assertEquals(6, aa);
		
		aa = gffGeneIsoCis.getEnd();
		assertEquals(50, aa);
		
		aa = gffGeneIsoCis.getLocDistmRNA(2, 41);
		assertEquals(20, aa);
		
		aa = gffGeneIsoCis.getLocDistmRNA(3, 5);
		assertEquals(1, aa);
		
		aa = gffGeneIsoCis.getNumCodInEle(3);
		assertEquals(1, aa);
		
		aa = gffGeneIsoCis.getNumCodInEle(4);
		assertEquals(-1, aa);
	}

	@Test
	public void testGffGeneIsoTrans() {
		GffGene gffDetailGene = new GffGene("chr1","sefes", false);
		GffIso gffGeneIsoTrans = GffIso.createGffGeneIso("aaa",gffDetailGene.getNameSingle(), gffDetailGene, GeneType.mRNA, false);

		gffGeneIsoTrans.add(new ExonInfo(false, 50, 40));
		gffGeneIsoTrans.add(new ExonInfo(false, 30, 20));
		gffGeneIsoTrans.add(new ExonInfo(false, 10, 5));
		gffGeneIsoTrans.add(new ExonInfo(false, 3, 0));
		int aa = gffGeneIsoTrans.getCod2ExInEnd(37);
		assertEquals(6, aa);
		aa = gffGeneIsoTrans.getCod2ExInStart(37);
		assertEquals(2, aa);

		aa = gffGeneIsoTrans.getCod2ExInEnd(23);
		assertEquals(3, aa);
		
		aa = gffGeneIsoTrans.getCod2ExInStart(23);
		assertEquals(7, aa);
		
		aa = gffGeneIsoTrans.getNumCodInEle(3);
		assertEquals(4, aa);
		
		aa = gffGeneIsoTrans.getNumCodInEle(4);
		assertEquals(-3, aa);
		
		aa = gffGeneIsoTrans.getNumCodInEle(15);
		assertEquals(-2, aa);
		
		aa = gffGeneIsoTrans.getNumCodInEle(35);
		assertEquals(-1, aa);
	}
	
	
}
