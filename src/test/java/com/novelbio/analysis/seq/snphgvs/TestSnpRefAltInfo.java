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
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("TAC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 348), snpRefAltInfo.getAlignRef());
		
		//头部和尾部都和alt一致
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACAT", "AT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 350), snpRefAltInfo.getAlignRef());
		
		//头部和尾部都和ref一致
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AT", "ATACAT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("ACAT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347), snpRefAltInfo.getAlignRef());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATAC", "AT");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("AC", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 347, 348), snpRefAltInfo.getAlignRef());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACACG", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("C", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 348).toString(), snpRefAltInfo.getAlignRef().toString());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACCAACG", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("G", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "ATACCAACG", "ATAGACTACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("CCA", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("GACT", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 348, 350).toString(), snpRefAltInfo.getAlignRef().toString());
		
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 345, "AT", "ATAGACG");
		snpRefAltInfo.copeInputVar();
		Assert.assertEquals("", snpRefAltInfo.getSeqRef());
		Assert.assertEquals("AGACG", snpRefAltInfo.getSeqAlt());
		Assert.assertEquals(new Align("chr1", 346, 347).toString(), snpRefAltInfo.getAlignRef().toString());
	}
	
	@Test
	public void testHgvsDul() {
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq("ATGCATTGCAGCAGCAGCAGCAGCAGCAGGGGG");
		
		List<Integer> lsNum = Lists.newArrayList(4,5,6,7,8,9,10,100);
		for (Integer stepLen : lsNum) {
			SnpRefAltInfo.setGetSeqLen(stepLen);
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "G", "GCAG");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());

			snpRefAltInfo.getStartPosition();
			snpRefAltInfo = new SnpRefAltInfo("chr1", 10, "AGCA", "A");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 26, 28).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 23, 25).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(26, snpRefAltInfo.getStartPosition());
			assertEquals(28, snpRefAltInfo.getEndPosition());
		}
		
		for (Integer stepLen : lsNum) {
			SnpRefAltInfo.setGetSeqLen(stepLen);
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "GCAG", "G");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 24, 26).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		}
		
		for (Integer stepLen : lsNum) {
			SnpRefAltInfo.setGetSeqLen(stepLen);
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 29, "G", "GCAG");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
			assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
			assertEquals(27, snpRefAltInfo.getStartPosition());
			assertEquals(29, snpRefAltInfo.getEndPosition());
			
			snpRefAltInfo = new SnpRefAltInfo("chr1", 26, "GCAG", "G");
			snpRefAltInfo.setSeqHash(seqHashStub);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
		}
		
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "G", "GCAGT");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		assertEquals(new Align("chr1", 17, 18).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 17, "GCAGC", "G");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		assertEquals(new Align("chr1", 18, 21).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 26, "GCAG", "G");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		assertEquals(new Align("chr1", 27, 29).toString(), snpRefAltInfo.getAlignRef().toString());
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 29, "G", "GCAG");
		snpRefAltInfo.setSeqHash(seqHashStub);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		assertEquals(new Align("chr1", 29, 30).toString(), snpRefAltInfo.getAlignRef().toString());
		assertEquals(new Align("chr1", 26, 27).toString(), snpRefAltInfo.getMoveDuplicate().toString());
		assertEquals(27, snpRefAltInfo.getStartPosition());
		assertEquals(29, snpRefAltInfo.getEndPosition());
	}
}
