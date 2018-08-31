package com.novelbio.software.tssplot;

public class ExceptionNBCChIPAlignError extends RuntimeException {
	public ExceptionNBCChIPAlignError(String msg) {
		super(msg);
	}
	
	public ExceptionNBCChIPAlignError(String msg, Throwable t) {
		super(msg, t);
	}
}
