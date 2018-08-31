package com.novelbio.bioinfo.fasta;

public class ExceptionSeqFasta extends RuntimeException {
	public ExceptionSeqFasta() {
		super();
	}
	
	public ExceptionSeqFasta(Throwable e) {
		super(e);
	}
	
	public ExceptionSeqFasta(String msg) {
		super(msg);
	}
	
	public ExceptionSeqFasta(String msg, Throwable e) {
		super(msg, e);
	}
}
