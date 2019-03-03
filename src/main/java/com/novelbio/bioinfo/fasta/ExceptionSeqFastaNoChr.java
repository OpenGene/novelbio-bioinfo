package com.novelbio.bioinfo.fasta;

public class ExceptionSeqFastaNoChr extends RuntimeException {
	public ExceptionSeqFastaNoChr() {
		super();
	}
	
	public ExceptionSeqFastaNoChr(Throwable e) {
		super(e);
	}
	
	public ExceptionSeqFastaNoChr(String msg) {
		super(msg);
	}
	
	public ExceptionSeqFastaNoChr(String msg, Throwable e) {
		super(msg, e);
	}
}
