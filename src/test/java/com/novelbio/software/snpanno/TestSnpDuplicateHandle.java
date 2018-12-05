package com.novelbio.software.snpanno;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.software.snpanno.SnpIndelRealignHandle;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

public class TestSnpDuplicateHandle {
	SeqHashStub seqHashStub = new SeqHashStub();

	@Test
	public void testRealign() {
		testRealignInsertion(4);
		testRealignDeletion(4);
		testRealignInsertion(100);
		testRealignDeletion(100);
		
		testRealignSimple();
		testRealignSimple2();
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
		
		assertEquals(34, snpIndelRealignHandle.getMoveBefore());
		
		Align alignRealign = snpIndelRealignHandle.moveAlignBefore(snpIndelRealignHandle.getMoveBefore());
		assertEquals("chr1:11-12", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqHead());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());
		
		alignRealign = snpIndelRealignHandle.moveAlignToAfter();
		assertEquals("chr1:45-46", alignRealign.toString());
		assertEquals("T", snpIndelRealignHandle.getSeqHead());
		assertEquals("CAAGCTGAT", snpIndelRealignHandle.getSeqChangeRight());
		
		alignRealign = snpIndelRealignHandle.moveAlignToBefore();
		assertEquals("chr1:11-12", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqHeadLeft());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChangeLeft());
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
		
		assertEquals(25, snpIndelRealignHandle.getMoveBefore());
		
		Align alignRealign = snpIndelRealignHandle.moveAlignBefore(7);
		assertEquals("chr1:30-38", alignRealign.toString());
		assertEquals("A", snpIndelRealignHandle.getSeqHead());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());

		alignRealign = snpIndelRealignHandle.moveAlignBefore(snpIndelRealignHandle.getMoveBefore());
		assertEquals("chr1:12-20", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqHead());
		assertEquals("AGCTGATCA", snpIndelRealignHandle.getSeqChange());
		
		alignRealign = snpIndelRealignHandle.moveAlignToAfter();
		assertEquals("chr1:37-45", alignRealign.toString());
		assertEquals("T", snpIndelRealignHandle.getSeqHead());
		assertEquals("CAAGCTGAT", snpIndelRealignHandle.getSeqChangeRight());
	}
	
	private void testRealignSimple() {
		String seq = "ATCGCCCTACATGCAGAG";
		seqHashStub.setSeq(seq.replace(" ", ""));
		
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("chr1:11-12"), "", "CTGA", "A");
		snpIndelRealignHandle.handleSeqAlign(seqHashStub);
		assertEquals(EnumHgvsVarType.Insertions, snpIndelRealignHandle.getVarType());
		assertEquals(10, snpIndelRealignHandle.getStartBefore());
		assertEquals(11, snpIndelRealignHandle.getStartAfter());
		assertEquals('C', snpIndelRealignHandle.getBeforeBase());
		
		assertEquals(1, snpIndelRealignHandle.getMoveBefore());
		
		Align alignRealign = snpIndelRealignHandle.moveAlignBefore(1);
		assertEquals("chr1:10-11", alignRealign.toString());
		assertEquals("C", snpIndelRealignHandle.getSeqHead());
		assertEquals("ACTG", snpIndelRealignHandle.getSeqChange());
	}
	
	private void testRealignSimple2() {
		String seq = "AT CG CG CG CG CG CG CG CG";
		seqHashStub.setSeq(seq.replace(" ", ""));
		
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("chr1:14-15"), "", "CG", "G");
		snpIndelRealignHandle.setSeqLen(2);
		snpIndelRealignHandle.handleSeqAlign(seqHashStub);
		assertEquals(EnumHgvsVarType.Duplications, snpIndelRealignHandle.getVarType());
		assertEquals(8, snpIndelRealignHandle.getStartBefore());
		assertEquals(18, snpIndelRealignHandle.getStartAfter());
		assertEquals('G', snpIndelRealignHandle.getBeforeBase());
		
		assertEquals(10, snpIndelRealignHandle.getMoveBefore());
		snpIndelRealignHandle.moveAlignBefore(10);
		assertEquals("chr1:8-9", snpIndelRealignHandle.getRealign().toString());
		assertEquals("CG", snpIndelRealignHandle.getSeqAlt());
	}
	
}
