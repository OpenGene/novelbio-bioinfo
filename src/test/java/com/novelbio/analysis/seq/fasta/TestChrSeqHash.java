package com.novelbio.analysis.seq.fasta;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.IndexedFastaSequenceFileHadoop;
import htsjdk.samtools.reference.ReferenceSequence;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;

public class TestChrSeqHash {
	String filePath = "/media/nbfs/nbCloud/public/nbcplatform/genome/species/9925/ncbi/ChromFa/chi_ref_CHIR_1.0_chrall2.fa";
	
	@Test
	public void testGetSeq() throws IOException {
		IndexedFastaSequenceFileHadoop indexedFastaSequenceFileRaw =
				new IndexedFastaSequenceFileHadoop(FileOperate.getFile(filePath));
		IndexedFastaSequenceFile indexedFastaSequenceFile =
				new IndexedFastaSequenceFile(FileOperate.getFile(filePath));
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(filePath);
		
		ReferenceSequence ref = indexedFastaSequenceFileRaw.getSubsequenceAt("chr14", 7374508, 7378508);
		String seq1 = getSeq(ref.getBases());
		ReferenceSequence ref2 = indexedFastaSequenceFile.getSubsequenceAt("chr14", 7374508, 7378508);
		String seq2 = getSeq(ref2.getBases());
		SeqFasta seqFasta = chrSeqHash.getSeq("chr14", 7374508, 7378508);
		String seq3 = seqFasta.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		
		//==================
		ref = indexedFastaSequenceFileRaw.getSubsequenceAt("chr16", 77674508, 77678508);
		seq1 = getSeq(ref.getBases());
		ref2 = indexedFastaSequenceFile.getSubsequenceAt("chr16", 77674508, 77678508);
		seq2 = getSeq(ref2.getBases());
		seqFasta = chrSeqHash.getSeq("chr16", 77674508, 77678508);
		seq3 = seqFasta.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		//=======================
		ref = indexedFastaSequenceFileRaw.getSubsequenceAt("chr1", 1, 15654);
		seq1 = getSeq(ref.getBases());
		ref2 = indexedFastaSequenceFile.getSubsequenceAt("chr1", 1, 15654);
		seq2 = getSeq(ref2.getBases());
		seqFasta = chrSeqHash.getSeq("chr1", 1, 15654);
		seq3 = seqFasta.toString();
		
		Assert.assertEquals(seq1, seq2);
		Assert.assertEquals(seq1, seq3);
		
		
		
		indexedFastaSequenceFileRaw.close();
		indexedFastaSequenceFile.close();
		chrSeqHash.close();
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
