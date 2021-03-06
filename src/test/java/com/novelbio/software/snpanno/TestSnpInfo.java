package com.novelbio.software.snpanno;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.software.snpanno.SnpInfo;

public class TestSnpInfo {
	
	@Test
	public void testCopeInputVar1() {
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 345, "ATAC", "A");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("TAC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 348), snpRefAltInfo.getAlignRefRight());
		
		//头部和尾部都和alt一致
		snpRefAltInfo = new SnpInfo("chr1", 345, "ATACAT", "AT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 350), snpRefAltInfo.getAlignRefRight());
		
		//头部和尾部都和ref一致
		snpRefAltInfo = new SnpInfo("chr1", 345, "AT", "ATACAT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347), snpRefAltInfo.getAlignRefRight());
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "ATAC", "AT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("AC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 348), snpRefAltInfo.getAlignRefRight());
		
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "ATACACG", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("C", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 348).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "ATACCAACG", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "ATACCAACG", "ATAGACTACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("GACT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "AT", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("AGACG", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "AC", "TA");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("AC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("TA", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 345, 346).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "A", "T");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("A", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("T", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 345, 345).toString(), snpRefAltInfo.getAlignRefRight().toString());
		
		snpRefAltInfo = new SnpInfo("chr1", 345, "TATAAA", "TAAA");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("TA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 348).toString(), snpRefAltInfo.getAlignRefRight().toString());
	}
	
	@Test
	public void testHgvsDul() {
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("ATGCATTGCAGCAGCAGCAGCAGCAGCAGGGGG");
		
		SnpInfo snpRefAltInfo0 = new SnpInfo("chr1", 5, "ATTG", "A");
		snpRefAltInfo0.initial(seqHashStub);
		Assert.assertFalse(snpRefAltInfo0.isDup());
		assertEquals(new Align("chr1", 6, 8).toString(), snpRefAltInfo0.getAlignRefRight().toString());
		
		List<Integer> lsNum = Lists.newArrayList(4,5,6,7,8,9,10,100);
		for (Integer stepLen : lsNum) {
			SnpInfo.setGetSeqLen(stepLen);
			SnpInfo snpInfo = new SnpInfo("chr1", 14, "GCAG", "GCAGCAG");
			snpInfo.initial(seqHashStub);
			assertEquals(new Align("chr1", 29, 30).toString(), snpInfo.getAlignRefRight().toString());
			assertEquals("chr1\t29\tG\tGCAG", snpInfo.toStringModify());
			assertEquals(27, snpInfo.getStartPosition());
			assertEquals(29, snpInfo.getEndPosition());
			snpInfo.moveAlignBefore(3);
			assertEquals(new Align("chr1", 26, 27).toString(), snpInfo.getAlign().toString());


			snpInfo = new SnpInfo("chr1", 17, "GCAG", "G");
			snpInfo.initial(seqHashStub);
			assertEquals(new Align("chr1", 27, 29).toString(), snpInfo.getAlignRefRight().toString());
			snpInfo.moveAlignBefore(3);
			assertEquals(new Align("chr1", 24, 26).toString(), snpInfo.getAlign().toString());
			
			snpInfo.getStartPosition();
			snpInfo = new SnpInfo("chr1", 10, "AGCA", "A");
			snpInfo.initial(seqHashStub);
			assertEquals(27, snpInfo.getStartPosition());
			assertEquals(29, snpInfo.getEndPosition());
			assertEquals(new Align("chr1", 27, 29).toString(), snpInfo.getAlignRefRight().toString());
			snpInfo.moveAlignBefore(3);
			assertEquals(new Align("chr1", 24, 26).toString(), snpInfo.getAlign().toString());

		}
		
		for (Integer stepLen : lsNum) {
			SnpInfo.setGetSeqLen(stepLen);
			SnpInfo snpRefAltInfo = new SnpInfo("chr1", 29, "G", "GCAG");
			snpRefAltInfo.initial(seqHashStub);
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRefRight().toString());
			snpRefAltInfo.moveAlignBefore(3);
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getAlign().toString());
			
			snpRefAltInfo = new SnpInfo("chr1", 28, "A", "AGCA");
			snpRefAltInfo.initial(seqHashStub);
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRefRight().toString());
			snpRefAltInfo.moveAlignBefore(3);
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getAlign().toString());
			
			snpRefAltInfo = new SnpInfo("chr1", 26, "GCAG", "G");
			snpRefAltInfo.initial(seqHashStub);
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRefRight().toString());
		}
		
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 17, "G", "GCAGT");
		snpRefAltInfo.initial(seqHashStub);
		assertEquals(new Align("chr1", 20, 21).toString(), snpRefAltInfo.getAlignRefRight().toString());
		assertEquals("chr1\t20\tG\tGTCAG", snpRefAltInfo.toStringModify());

		snpRefAltInfo = new SnpInfo("chr1", 17, "GCAGC", "G");
		snpRefAltInfo.initial(seqHashStub);
		assertEquals(new Align("chr1", 18, 21).toString(), snpRefAltInfo.getAlignRefRight().toString());
				
		snpRefAltInfo = new SnpInfo("chr1", 3, "G", "GG");
		snpRefAltInfo.initial(seqHashStub);
		assertEquals(3, snpRefAltInfo.getStartPosition());
		assertEquals(3, snpRefAltInfo.getEndPosition());
		assertEquals(new Align("chr1", 3, 4).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(1);
		assertEquals(new Align("chr1", 2, 3).toString(), snpRefAltInfo.getAlign().toString());

	}
	
	@Test
	public void testHgvsDul2() {
		//CGCGCAGATCATCA
		//TTT-ATCAC-[GCGCAGATCATCAC]-GCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-ATCACGCGCAGATC-TTTT
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCTTTT");
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 8, "C", "CGCGCAGATCATCAC");
		snpRefAltInfo.initial(seqHashStub);
		assertEquals(4, snpRefAltInfo.getStartPosition());
		assertEquals(17, snpRefAltInfo.getEndPosition());
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 17, 18).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(14);
		assertEquals(new Align("chr1", 3, 4).toString(), snpRefAltInfo.getAlign().toString());

		
		//CGCGCAGATCATCA
		//TTT-[ATCACGCGCAGATC]-ATCACGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpInfo("chr1", 3, "TATCACGCGCAGATC", "T");
		snpRefAltInfo.initial(seqHashStub);
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(14);
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getAlign().toString());

		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpInfo("chr1", 7, "ACGCGCAGATCATCA", "A");
		snpRefAltInfo.initial(seqHashStub);
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(14);
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getAlign().toString());
		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpInfo("chr1", 17, "CATCACGCGCAGATC", "C");
		snpRefAltInfo.initial(seqHashStub);
		assertEquals(18, snpRefAltInfo.getStartPosition());
		assertEquals(31, snpRefAltInfo.getEndPosition());
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(new Align("chr1", 18, 31).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(14);
		assertEquals(new Align("chr1", 4, 17).toString(), snpRefAltInfo.getAlign().toString());

		
		
		//TTT-ATCA-[CGCGCAGATC-ATCA]-CGCGCAGATC-TTTT
		//TTT-ATCACGCGCAGATC-TTTT
		seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("TTTTTTTTTTTTATCACGCGCAGATCATCACGCGCAGATCTTTT");
		snpRefAltInfo = new SnpInfo("chr1", 26, "CATCACGCGCAGATC", "C");
		snpRefAltInfo.initial(seqHashStub);
		Assert.assertTrue(snpRefAltInfo.isDup());
		assertEquals(27, snpRefAltInfo.getStartPosition());
		assertEquals(40, snpRefAltInfo.getEndPosition());
		assertEquals(new Align("chr1", 27, 40).toString(), snpRefAltInfo.getAlignRefRight().toString());
		snpRefAltInfo.moveAlignBefore(14);
		assertEquals(new Align("chr1", 13, 26).toString(), snpRefAltInfo.getAlign().toString());

	}
}
