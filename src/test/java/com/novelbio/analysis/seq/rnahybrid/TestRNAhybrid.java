package com.novelbio.analysis.seq.rnahybrid;

import junit.framework.TestCase;

import org.junit.Test;

public class TestRNAhybrid extends TestCase {
	@Test
	public void testRead() {
		RNAhybrid rnaHybrid = new RNAhybrid();
		for (HybridRNAUnit rnaHybridUnit : rnaHybrid.readPerlines("/media/winD/plant_miRNA_predict/rnahybrid.out")) {
			System.out.println(rnaHybridUnit.isSeedPerfectMatch());
			System.out.println(rnaHybridUnit.getAlign());
			System.out.println();
		}
	}
	
	
	@Test
	public void test() {
		HybridRNAUnit hybridHybrid = new HybridRNAUnit();
		String[] seq = new String[]{"target 5'  C  G            A   G 3'",
				"            GU  GCU UCUUCUG UUG    ",
				"            CG  UGA AGAAGAC AGC    ",
				"miRNA  3' CA  AG   G             5'"};
		hybridHybrid.setSeqAndAlign(seq);
		hybridHybrid.setQname("queryName");
		hybridHybrid.setSname("SubjectName");
		
		String[] value = hybridHybrid.getAlign().split("\n");
		assertEquals("queryName  :CACGAGUGAGAGAAGAC-AGC-", value[0]);
		assertEquals("              ||  ||| ||||||| |||", value[1]);
		assertEquals("SubjectName:-CGUG-GCU-UCUUCUGAUUGG", value[2]);
		assertEquals("||  ||| ||||||| |||", hybridHybrid.align);
		assertEquals(19, hybridHybrid.alignLen);
		assertEquals(2, hybridHybrid.startSpaceNum);
		System.out.println(hybridHybrid.getAlign());
	}
}
