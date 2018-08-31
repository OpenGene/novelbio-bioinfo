package com.novelbio.bioinfo.gff;


public class ExceptionNbcGFF extends RuntimeException {
	private static final long serialVersionUID = -725691436212513467L;

	public ExceptionNbcGFF(String info) {
		super(info);
	}
	
	public ExceptionNbcGFF(String info, Throwable t) {
		super(info, t);
	}
}
