package com.novelbio.analysis.seq.fasta;

import org.junit.Assert;
import org.junit.Test;

public class TestChrBaseIter {
	
	@Test
	public void testReadBase() {
		String reference = "src/test/resources/test_file/reference/testTrinity.fa";
		String contigName = "Contig6";
		
		ChrBaseIter chrBaseIter = new ChrBaseIter(reference);
		ChrSeqHash chrSeqHash = new ChrSeqHash(reference);
		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();
		for (Base base : chrBaseIter.readBase(contigName)) {
			i++;
			Assert.assertEquals(i, base.getPosition());
			stringBuilder.append(base.getBase());
		}
		
		SeqFasta seqFasta = chrSeqHash.getSeq(contigName);
		
		Assert.assertEquals(seqFasta.toString(), stringBuilder.toString());
		
		chrBaseIter.close();
		chrSeqHash.close();
	}
	
	@Test
	public void testReadBase2() {
		String reference = "src/test/resources/test_file/reference/testTrinity.fa";
		String contigName = "Contig6";
		
		ChrBaseIter chrBaseIter = new ChrBaseIter(reference);
		ChrSeqHash chrSeqHash = new ChrSeqHash(reference);
		int i = 149;
		StringBuilder stringBuilder = new StringBuilder();
		for (Base base : chrBaseIter.readBase(contigName, 150, 500)) {
			i++;
			Assert.assertEquals(i, base.getPosition());
			stringBuilder.append(base.getBase());
		}
		
		SeqFasta seqFasta = chrSeqHash.getSeq(contigName, 150, 500);
		
		Assert.assertEquals(seqFasta.toString(), stringBuilder.toString());
		
		chrBaseIter.close();
		chrSeqHash.close();
	}
	
}
