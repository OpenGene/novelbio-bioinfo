package com.novelbio.analysis.diffexpress;

public class ExceptionFreemarker extends RuntimeException {
	private static final long serialVersionUID = -6579762181197192946L;

	public ExceptionFreemarker(String msg) {
		super(msg);
	}
	
	public ExceptionFreemarker(String msg, Exception e) {
		super(msg, e);
	}
	
}
