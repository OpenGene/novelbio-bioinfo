package com.novelbio.analysis.annotation.functiontest;

public class ExceptionFunctionTest extends RuntimeException {
	public ExceptionFunctionTest() {
		super();
	}
	
	public ExceptionFunctionTest(Throwable e) {
		super(e);
	}
	
	public ExceptionFunctionTest(String msg, Throwable e) {
		super(msg, e);
	}
}
