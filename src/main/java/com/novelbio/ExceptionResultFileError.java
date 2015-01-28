package com.novelbio;

public class ExceptionResultFileError extends RuntimeException {

	private static final long serialVersionUID = -8044239997827692037L;

	public ExceptionResultFileError() {
		super();
	}
	
	public ExceptionResultFileError(String msg) {
		super(msg);
	}
	
	public ExceptionResultFileError(Throwable e) {
		super(e);
	}
	
	public ExceptionResultFileError(String msg, Throwable e) {
		super(msg, e);
	}
}
