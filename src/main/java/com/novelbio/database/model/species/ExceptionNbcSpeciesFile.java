package com.novelbio.database.model.species;

public class ExceptionNbcSpeciesFile extends RuntimeException {
    private static final long serialVersionUID = -8117884523763612183L;

	public ExceptionNbcSpeciesFile(String msg) {
	    super(msg);
    }
	
	public ExceptionNbcSpeciesFile(String msg, Throwable e) {
	    super(msg, e);
    }
}