package com.novelbio.bioinfo.fasta;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.fasta.Base;
import com.novelbio.bioinfo.fasta.ChrBaseIter;
import com.novelbio.bioinfo.fasta.ChrSeqHash;
import com.novelbio.bioinfo.fasta.SeqFasta;

public class TestChrBaseIter {
	
	@Test
	public void testReadBase() {
		String reference = "src/test/resources/test_file/reference/testTrinity.fa";
		String contigName = "Contig6";
		
		ChrBaseIter chrBaseIter = new ChrBaseIter(reference);
		ChrSeqHash chrSeqHash = new ChrSeqHash(reference);
		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			for (Base base : chrBaseIter.readBase(contigName)) {
				i++;
				Assert.assertEquals(i, base.getPosition());
				stringBuilder.append(base.getBase());
			}
		} catch (Exception e) {
			// TODO: handle exception
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
	@Test
	public void testReadBase3() {
		String reference = "src/test/resources/test_file/reference/testTrinity.fa";
		String contigName = "Contig6";
		
		ChrBaseIter chrBaseIter = new ChrBaseIter(reference);
		ChrSeqHash chrSeqHash = new ChrSeqHash(reference);
		int i = 59;
		StringBuilder stringBuilder = new StringBuilder();
		for (Base base : chrBaseIter.readBase(contigName, 60, 120)) {
			i++;
			Assert.assertEquals(i, base.getPosition());
			stringBuilder.append(base.getBase());
		}
		
		SeqFasta seqFasta = chrSeqHash.getSeq(contigName, 60, 120);
		Assert.assertEquals("CTTGGATCCATTGGGTCAAGCTCATCCTCCTCAGCGCGGCCTCTTTTTCGGTTATCTTTTT", seqFasta.toString());
		Assert.assertEquals(seqFasta.toString(), stringBuilder.toString());
		
		chrBaseIter.close();
		chrSeqHash.close();
	}
	
	@Test
	public void testReadBase4() {
		String reference = "src/test/resources/test_file/reference/testTrinity.fa";
		String contigName = "Contig6";
		
		ChrBaseIter chrBaseIter = new ChrBaseIter(reference);
		ChrSeqHash chrSeqHash = new ChrSeqHash(reference);
		StringBuilder stringBuilder = new StringBuilder();
		for (Base base : chrBaseIter.readBase(contigName, 61, 121)) {
			stringBuilder.append(base.getBase());
		}
		
		SeqFasta seqFasta = chrSeqHash.getSeq(contigName, 61, 121);
		Assert.assertEquals("TTGGATCCATTGGGTCAAGCTCATCCTCCTCAGCGCGGCCTCTTTTTCGGTTATCTTTTTT", seqFasta.toString());
		Assert.assertEquals(seqFasta.toString(), stringBuilder.toString());
		
		chrBaseIter.close();
		chrSeqHash.close();
	}
}
