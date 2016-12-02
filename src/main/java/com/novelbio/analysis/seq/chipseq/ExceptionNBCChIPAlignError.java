package com.novelbio.analysis.seq.chipseq;

public class ExceptionNBCChIPAlignError extends RuntimeException {
	public ExceptionNBCChIPAlignError(String msg) {
		super(msg);
	}
	
	public ExceptionNBCChIPAlignError(String msg, Throwable t) {
		super(msg, t);
	}
}
