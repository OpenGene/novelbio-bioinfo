package com.novelbio.database.model.species;

public class ExceptionNbcSpeciesFileAbstract extends RuntimeException {
    private static final long serialVersionUID = -8117884523763612183L;

	public ExceptionNbcSpeciesFileAbstract(String msg) {
	    super(msg);
    }
	
	public ExceptionNbcSpeciesFileAbstract(String msg, Throwable e) {
	    super(msg, e);
    }
}
