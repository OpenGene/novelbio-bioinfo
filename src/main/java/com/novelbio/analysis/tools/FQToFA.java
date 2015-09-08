package com.novelbio.analysis.tools;

import java.util.Iterator;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.fileOperate.FileOperate;

public class FQToFA {
	FastQ fqLeft, fqRight;
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	public static void main(String[] args) {
		String faFile = "/home/novelbio/bianlianle/test/test.fa";
		String faFileResult = "/home/novelbio/bianlianle/test/getresult.fa";
		SeqFastaHash seqFastaHash = new SeqFastaHash (faFile);
		String name = "chr1 name dec";
		SeqFasta seq = seqFastaHash.getSeq(name);
		SeqFasta subSeqFasta = seq.getSubSeq(1, 4, true);
		
		System.out.println("all seq " + seq.toString());
		System.out.println("subSeqFasta seq " + subSeqFasta.toString());
		
//		SeqFastaHash seqFastaHashResult = new SeqFastaHash (faFileResult);
		
//		seqFastaHashResult.writeToFile(name);
//		seqFastaHah.g
//		SeqFasta SeqFasta= new SeqFasta();
//		SeqFasta.
		
		
	}
	public void initial() {
		if (fqRight != null) {
			itFqPE =  fqLeft.readlinesPE(fqRight).iterator();
		} else {
			itFqSE = fqLeft.readlines().iterator();
		}
	}
	public void FQToFA() {
		
	}
	public void setFastqLeft(String fastqLeft) {
		this.fqLeft = new FastQ(fastqLeft);
		
//		fqLeftModify = new FastQ(FileOperate.changeFileSuffix(fastqLeft, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	public void setFastqRight(String fastqRight) {
		this.fqRight = new FastQ(fastqRight);
//		fqRightModify = new FastQ(FileOperate.changeFileSuffix(fastqRight, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
}
