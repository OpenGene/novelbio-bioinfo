package com.novelbio.analysis.seq.snphgvs;

public class ExceptionNBCSnpHgvs extends RuntimeException {
	private static final long serialVersionUID = 3281480531727342133L;

	public ExceptionNBCSnpHgvs(String msg) {
		super(msg);
	}
	
	public ExceptionNBCSnpHgvs(String msg, Throwable t) {
		super(msg, t);
	}
}
