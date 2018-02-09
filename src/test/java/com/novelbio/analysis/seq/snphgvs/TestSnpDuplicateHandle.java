package com.novelbio.analysis.seq.snphgvs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.analysis.seq.mapping.Align;

public class TestSnpDuplicateHandle {
	SeqHashStub seqHashStub = new SeqHashStub();

	@Test
	public void testRealign() {
		testRealignInsertion(4);
		testRealignDeletion(4);
		testRealignInsertion(100);
		testRealignDeletion(100);
	}
	
	private void testRealignInsertion(int seqLen) {
		String seq = "ATCGCCCTACC AGCT GATCAAGCT GATCAAGCT GATCAAGCT GAT ACACCCTACCC";
		seqHashStub.setSeq(seq.replace(" ", ""));
		
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("chr1:30-31"), "", "GCT GATCAA".replace(" ", ""), "A");		
		snpIndelRealignHandle.setSeqLen(seqLen);
		snpIndelRealignHandle.handleSeqAlign(seqHashStub);
		assertEquals(EnumHgvsVarType.Duplications, snpIndelRealignHandle.getVarType());
		assertEquals(EnumHgvsVarType.Duplications, snpIndelRealignHandle.getVarType());
		assertEquals(11, snpIndelRealignHandle.getStartBefore());
		assertEquals(45, snpIndelRealignHandle.getStartAfter());
		assertEquals('C', snpIndelRealignHandle.getBeforeBase());
		
		assertEquals(-19, snpIndelRealignHandle.getMoveBefore());
		assertEquals(15, snpIndelRealignHandle.getMoveAfter());
		
		Align alignRealign = snpIndelRealignHandle.moveAlign(snpIndelRealignHandle.getMoveBefore());
		assertEquals("chr1:11-12", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqChangeShort());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());
		
		alignRealign = snpIndelRealignHandle.moveAlign(snpIndelRealignHandle.getMoveAfter());
		assertEquals("chr1:45-46", alignRealign.toString());
		assertEquals("T", snpIndelRealignHandle.getSeqChangeShort());
		assertEquals("CAAGCTGAT", snpIndelRealignHandle.getSeqChange());
	}
	
	private void testRealignDeletion(int seqLen) {
		String seq = "ATCGCCCTACC AGCT GATCAAGCT GATCAAGCT GATCAAGCT GAT ACACCCTACCC";
		seqHashStub.setSeq(seq.replace(" ", ""));
		
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("chr1:31-39"), "GCT GATCAA".replace(" ", ""), "", "A");
		snpIndelRealignHandle.setSeqLen(seqLen);
		snpIndelRealignHandle.handleSeqAlign(seqHashStub);
		assertEquals(EnumHgvsVarType.Deletions, snpIndelRealignHandle.getVarType());
		assertEquals(12, snpIndelRealignHandle.getStartBefore());
		assertEquals(37, snpIndelRealignHandle.getStartAfter());
		assertEquals('C', snpIndelRealignHandle.getBeforeBase());
		
		assertEquals(-19, snpIndelRealignHandle.getMoveBefore());
		assertEquals(6, snpIndelRealignHandle.getMoveAfter());
		
		Align alignRealign = snpIndelRealignHandle.moveAlign(-1);
		assertEquals("chr1:30-38", alignRealign.toString());
		assertEquals("A", snpIndelRealignHandle.getSeqChangeShort());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());

		alignRealign = snpIndelRealignHandle.moveAlign(snpIndelRealignHandle.getMoveBefore());
		assertEquals("chr1:12-20", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqChangeShort());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());
		
		alignRealign = snpIndelRealignHandle.moveAlign(snpIndelRealignHandle.getMoveAfter());
		assertEquals("chr1:37-45", alignRealign.toString());
		assertEquals("T", snpIndelRealignHandle.getSeqChangeShort());
		assertEquals("CAAGCTGAT", snpIndelRealignHandle.getSeqChange());
	}
}
