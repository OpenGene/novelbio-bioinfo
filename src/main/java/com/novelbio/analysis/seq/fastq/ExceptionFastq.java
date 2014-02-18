package com.novelbio.analysis.seq.fastq;

public class ExceptionFastq extends RuntimeException {
	public ExceptionFastq(String msg) {
		super(msg);
	}
	
	public ExceptionFastq(String msg, Exception e) {
		super(msg, e);
	}
}
