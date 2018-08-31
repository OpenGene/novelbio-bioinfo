package com.novelbio.software;

public class ExceptionNBCsoft extends RuntimeException{
	private static final long serialVersionUID = 1869737340449615011L;

	public ExceptionNBCsoft(String info) {
		super(info);
	}
	
	public ExceptionNBCsoft(String info, Throwable t) {
		super(info, t);
	}
}
