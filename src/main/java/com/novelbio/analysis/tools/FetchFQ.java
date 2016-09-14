package com.novelbio.analysis.tools;

import java.util.Iterator;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FetchFQ {

	FastQ fqLeft, fqRight;
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	FastQ fqLeftResult, fqRightResult;
	
	public static void main(String[] args) {
//		String fastqLeft = "/run/media/novelbio/A/bianlianle/project/fastq/WGC048907_NBC150910-2_BT474-naiyao_combined_R1_part.fastq.gz";
//		String fastqRight = "//run/media/novelbio/A/bianlianle/project/fastq/WGC048907_NBC150910-2_BT474-naiyao_combined_R2_part.fastq.gz";
//		String seqIdfile = "/run/media/novelbio/A/bianlianle/project/fastq/id.txt";
//		long readsNum = 10;
		String fastqLeft = args[0];
		String fastqRight = args[1];
//		long readsNum =  Long.parseLong(args[2]);
		String seqIdfile =args[2];
		FetchFQ fetchFQ = new FetchFQ();
		fetchFQ.setFastqLeft(fastqLeft);
		fetchFQ.setFastqRight(fastqRight);
		fetchFQ.initial();
//		fetchFQ.fetchFQ(readsNum);
		fetchFQ.fetchFQBySeqID(seqIdfile);
		
		
		fetchFQ.close();
		
		
		String samBamFile = "";
		SamFile samFile = new SamFile(samBamFile);
		
	}
	public void initial() {
		if (fqRight != null) {
			itFqPE =  fqLeft.readlinesPE(fqRight).iterator();
		} else {
			itFqSE = fqLeft.readlines().iterator();
		}
	}
	public void setFastqLeft(String fastqLeft) {
		this.fqLeft = new FastQ(fastqLeft);
		fqLeftResult = new FastQ(FileOperate.changeFileSuffix(fastqLeft, "_part", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	public void setFastqRight(String fastqRight) {
		this.fqRight = new FastQ(fastqRight);
		fqRightResult = new FastQ(FileOperate.changeFileSuffix(fastqRight, "_part", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	protected void writeFastq(FastQRecord[] fqPE) {
		if (fqPE[0] != null) {
			fqLeftResult.writeFastQRecord(fqPE[0]);
		}
		if (fqPE[1] != null) {
			fqRightResult.writeFastQRecord(fqPE[1]);
		}
	}
	public void fetchFQ(long readsNum) {
		long i = 1;
		while (true) {
			FastQRecord[] fqPE = null;
			if (itFqPE != null) {
				fqPE = itFqPE.next();
			} else {
				FastQRecord fqSE = itFqSE.next();
				fqPE = new FastQRecord[]{fqSE, null};
			}
			writeFastq(fqPE);
			if ( i++ >= readsNum) {
				break;
			}
		}
	}
	
	public void fetchFQBySeqID(String seqIdFile) {
		TxtReadandWrite seqIdFileReadandWrite = new TxtReadandWrite(seqIdFile);
		for (String seqId : seqIdFileReadandWrite.readlines()) {
			writeFQBySeqID(seqId);
		}
		seqIdFileReadandWrite.close();
	}
	
	/**
	 * get fasta by read name
	 * */
	public void writeFQBySeqID(String seqID) {
		String leftSeqName = "";
		long i = 1;
		while (true) {
			FastQRecord[] fqPE = null;
			if (itFqPE != null) {
				fqPE = itFqPE.next();
			} else {
				FastQRecord fqSE = itFqSE.next();
				fqPE = new FastQRecord[]{fqSE, null};
			}
			leftSeqName = fqPE[0].getName();
			if (leftSeqName.indexOf(seqID)>-1) {
				writeFastq(fqPE);
				break;
			}
		}
	}
	
	protected void  close() {
		fqLeftResult.close();
		if (fqRightResult != null) {
			fqRightResult.close();
		}
	}
}
