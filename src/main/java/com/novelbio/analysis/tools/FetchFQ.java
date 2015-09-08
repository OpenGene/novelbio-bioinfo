package com.novelbio.analysis.tools;

import java.util.Iterator;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.fileOperate.FileOperate;

public class FetchFQ {

	FastQ fqLeft, fqRight;
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	FastQ fqLeftResult, fqRightResult;
	
	public static void main(String[] args) {
//		String fastqLeft = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_CYR32-1_combined_R1.fastq.gz";
//		String fastqRight = "/home/novelbio/bianlianle/RemoveContaminate/WGC035398_hunhewenku-GR0211_CYR32-1_combined_R2.fastq.gz";
//		long readsNum = 10;
		String fastqLeft = args[0];
		String fastqRight = args[1];
		long readsNum =  Long.parseLong(args[2]);
		
		FetchFQ fetchFQ = new FetchFQ();
		fetchFQ.setFastqLeft(fastqLeft);
		fetchFQ.setFastqRight(fastqRight);
		fetchFQ.initial();
		fetchFQ.fetchFQ(readsNum);
		fetchFQ.close();
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
	protected void  close() {
		fqLeftResult.close();
		if (fqRightResult != null) {
			fqRightResult.close();
		}
	}
}
