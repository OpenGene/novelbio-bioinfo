package com.novelbio.listOperate;

public class ExceptionGff extends RuntimeException {

	private static final long serialVersionUID = 3909876473807752259L;

	ExceptionGff(String msg) {
		super(msg);
	}
	
	ExceptionGff(String msg, Throwable e) {
		super(msg, e);
	}
	
	ExceptionGff(Throwable e) {
		super(e);
	}
}
