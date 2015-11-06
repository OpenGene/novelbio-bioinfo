package com.novelbio.analysis.seq.fasta;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.IndexedFastaSequenceFileHadoop;
import htsjdk.samtools.reference.ReferenceSequence;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;

public class TestChrSeqHash {
	String filePath = "src/test/resources/test_file/reference/testTrinity.fa";
	
	@Test
	public void testGetSeq() throws IOException {
		IndexedFastaSequenceFileHadoop indexedFastaSequenceFileRaw =
				new IndexedFastaSequenceFileHadoop(FileOperate.getFile(filePath));
		IndexedFastaSequenceFile indexedFastaSequenceFile =
				new IndexedFastaSequenceFile(FileOperate.getFile(filePath));
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(filePath);
		SeqFastaHash seqFastaHash = new SeqFastaHash(filePath);
		ReferenceSequence ref = indexedFastaSequenceFileRaw.getSubsequenceAt("Contig1", 62, 235);
		String seq1 = getSeq(ref.getBases());
		ReferenceSequence ref2 = indexedFastaSequenceFile.getSubsequenceAt("Contig1", 62, 235);
		String seq2 = getSeq(ref2.getBases());
		SeqFasta seqFasta = chrSeqHash.getSeq("Contig1", 62, 235);
		String seq3 = seqFasta.toString();
		SeqFasta seqFasta2 = seqFastaHash.getSeq("Contig1", 62, 235);
		String seq4 = seqFasta2.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		Assert.assertEquals(seq1, seq4);
		//==================
		
		ref = indexedFastaSequenceFileRaw.getSubsequenceAt("Contig7", 654, 1754);
		seq1 = getSeq(ref.getBases());
		ref2 = indexedFastaSequenceFile.getSubsequenceAt("Contig7", 654, 1754);
		seq2 = getSeq(ref2.getBases());
		seqFasta = chrSeqHash.getSeq("Contig7", 654, 1754);
		seq3 = seqFasta.toString();
		seqFasta2 = seqFastaHash.getSeq("Contig7", 654, 1754);
		seq4 = seqFasta2.toString();

		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		Assert.assertEquals(seq1, seq4);
		//=======================
		
		ref = indexedFastaSequenceFileRaw.getSubsequenceAt("Contig1", 1, 12);
		seq1 = getSeq(ref.getBases());
		ref2 = indexedFastaSequenceFile.getSubsequenceAt("Contig1", 1, 12);
		seq2 = getSeq(ref2.getBases());
		seqFasta = chrSeqHash.getSeq("Contig1", 1, 12);
		seq3 = seqFasta.toString();
		seqFasta2 = seqFastaHash.getSeq("Contig1", 1, 12);
		seq4 = seqFasta2.toString();

		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		Assert.assertEquals(seq1, seq4);
		//=======================
		
		ref = indexedFastaSequenceFileRaw.getSubsequenceAt("Contig1", 1, 88);
		seq1 = getSeq(ref.getBases());
		ref2 = indexedFastaSequenceFile.getSubsequenceAt("Contig1", 1, 88);
		seq2 = getSeq(ref2.getBases());
		seqFasta = chrSeqHash.getSeq("Contig1", 1, 88);
		seq3 = seqFasta.toString();
		seqFasta2 = seqFastaHash.getSeq("Contig1", 1, 88);
		seq4 = seqFasta2.toString();

		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		Assert.assertEquals(seq1, seq4);
		//=======================
		seqFasta = chrSeqHash.getSeq("Contig2");
		seq3 = seqFasta.toString();
		seqFasta2 = seqFastaHash.getSeq("Contig2");
		seq4 = seqFasta2.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq3, seq4);
		
		//=======================
		seqFasta = chrSeqHash.getSeq("Contig1");
		seq3 = seqFasta.toString();
		seqFasta2 = seqFastaHash.getSeq("Contig1");
		seq4 = seqFasta2.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq3, seq4);
		
		

		
		indexedFastaSequenceFileRaw.close();
		indexedFastaSequenceFile.close();
		chrSeqHash.close();
		seqFastaHash.close();

	}
	
	private String getSeq(byte[] readInfo) {
		StringBuilder sequence = new StringBuilder();
		for (byte b : readInfo) {
			char seq = (char)b;
			sequence.append(seq);
		}
		return sequence.toString();
	}
}
