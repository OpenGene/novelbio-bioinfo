package com.novelbio.analysis.seq.snphgvs;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.mapping.Align;

public class TestSnpRefAltInfo {
	
	@Test
	public void testCopeInputVar1() {
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATAC", "A");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("TAC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 348), snpRefAltInfo.getAlignRef());
		
		//头部和尾部都和alt一致
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACAT", "AT");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 350), snpRefAltInfo.getAlignRef());
		
		//头部和尾部都和ref一致
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AT", "ATACAT");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347), snpRefAltInfo.getAlignRef());
		
		//头部和尾部都和ref一致
		//注意这里比较的是尾部
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AT", "ATACAT");
		snpRefAltInfo.copeInputVar(false);
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("ATAC", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 344, 345), snpRefAltInfo.getAlignRef());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATAC", "AT");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("AC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 348), snpRefAltInfo.getAlignRef());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACACG", "ATAGACG");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("C", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 348).toString(), snpRefAltInfo.getAlignRef().toString());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACCAACG", "ATAGACG");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACCAACG", "ATAGACTACG");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("GACT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRef().toString());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AT", "ATAGACG");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("AGACG", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AC", "TA");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("AC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("TA", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 345, 346).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "A", "T");
		snpRefAltInfo.copeInputVar(true);
		Assert.assertEquals("A", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("T", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 345, 345).toString(), snpRefAltInfo.getAlignRef().toString());
	}
	
	@Test
	public void testHgvsDul() {
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("ATGCATTGCAGCAGCAGCAGCAGCAGCAGGGGG");
		
		SnpRefAltInfo snpRefAltInfo0 = new SnpRefAltInfo("chr1", 5, "ATTG", "A");
		snpRefAltInfo0.setSeqHash(seqHashStub);
		snpRefAltInfo0.initial();
		Assert.assertFalse(snpRefAltInfo0.isDup());
		assertEquals(new Align("chr1", 6, 8).toString(), snpRefAltInfo0.getAlignRef().toString());
		
		List<Integer> lsNum = Lists.newArrayList(4,5,6,7,8,9,10,100);
		for (Integer stepLen : lsNum) {
			SnpRefAltInfo.setGetSeqLen(stepLen);
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "G", "GCAG");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.initial();
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());

			snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "GCAG", "G");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.initial();
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 24, 26).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			
			snpRefAltInfo.getStartPosition();
			snpRefAltInfo = new SnpRefAltInfo("chr1", 10, "AGCA", "A");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.initial();
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 24, 26).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
		}
		
		for (Integer stepLen : lsNum) {
			SnpRefAltInfo.setGetSeqLen(stepLen);
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 29, "G", "GCAG");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.initial();
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
			
			snpRefAltInfo = new SnpRefAltInfo("chr1", 28, "A", "AGCA");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.initial();
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
			
			snpRefAltInfo = new SnpRefAltInfo("chr1", 26, "GCAG", "G");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar(true);
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
		}
		
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "G", "GCAGT");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		assertEquals(new Align("chr1", 17, 18).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "GCAGC", "G");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		assertEquals(new Align("chr1", 18, 21).toString(), snpRefAltInfo.getAlignRef().toString());
				
		snpRefAltInfo = new SnpRefAltInfo("chr1", 3, "G", "GG");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		assertEquals(new Align("chr1", 3, 4).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 2, 3).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(3, snpRefAltInfo.getStartPosition());
		assertEquals(3, snpRefAltInfo.getEndPosition());
		snpRefAltInfo.setIsDupMoveLast(true);
		assertEquals(2, snpRefAltInfo.getStartReal());
		assertEquals(3, snpRefAltInfo.getEndReal());
	}
	
	@Test
	public void testHgvsDul2() {
		//CGCGCAGATCATCA
		//TTT-ATCAC-[GCGCAGATCATCAC]-GCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-ATCACGCGCAGATC-TTTT
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCTTTT");
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 8, "C", "CGCGCAGATCATCAC");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 17, 18).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 3, 4).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(4, snpRefAltInfo.getStartPosition());
		assertEquals(17, snpRefAltInfo.getEndPosition());
		
		//CGCGCAGATCATCA
		//TTT-[ATCACGCGCAGATC]-ATCACGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 3, "TATCACGCGCAGATC", "T");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 7, "ACGCGCAGATCATCA", "A");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "CATCACGCGCAGATC", "C");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		
		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTTTTTTTTTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 26, "CATCACGCGCAGATC", "C");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.initial();
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 27, 40).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 13, 26).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(27, snpRefAltInfo.getStartPosition());
		assertEquals(40, snpRefAltInfo.getEndPosition());
	}
}
