package com.novelbio.database.model.species;


public class ExceptionNbcSpeciesNotExist extends RuntimeException {
    private static final long serialVersionUID = -8590285350120327033L;

	public ExceptionNbcSpeciesNotExist(String msg) {
		super(msg);
	}
	
	public ExceptionNbcSpeciesNotExist(String msg, Throwable t) {
		super(msg, t);
	}
}
