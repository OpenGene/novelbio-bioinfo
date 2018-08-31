package com.novelbio.bioinfo.fastq;

public class ExceptionFastq extends RuntimeException {
	public ExceptionFastq(String msg) {
		super(msg);
	}
	
	public ExceptionFastq(String msg, Exception e) {
		super(msg, e);
	}
	
	public ExceptionFastq(Exception e) {
		super(e);
	}
	
	public ExceptionFastq(String msg, Throwable e) {
		super(msg, e);
	}
}
