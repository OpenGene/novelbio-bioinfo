package com.novelbio.analysis.tools.snvintersection;

public interface ReadFirstIterator<T> {

	public abstract void next();

	public abstract void reset();

	public abstract boolean isAvailable();

	public abstract T current();

}