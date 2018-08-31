package com.novelbio.bioinfo.base;

public class ExceptionNBCAlignError extends RuntimeException {
	public ExceptionNBCAlignError(String msg) {
		super(msg);
	}
	
	public ExceptionNBCAlignError(String msg, Throwable t) {
		super(msg, t);
	}
}
