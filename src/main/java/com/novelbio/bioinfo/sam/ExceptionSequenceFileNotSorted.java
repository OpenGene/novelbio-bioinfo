package com.novelbio.bioinfo.sam;

/** 文件没有排序 */
public class ExceptionSequenceFileNotSorted extends RuntimeException {
	public ExceptionSequenceFileNotSorted() { }

    public ExceptionSequenceFileNotSorted(final String s) {
        super(s);
    }

    public ExceptionSequenceFileNotSorted(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSequenceFileNotSorted(final Throwable throwable) {
        super(throwable);
    }
}
