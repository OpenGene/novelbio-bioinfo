package com.novelbio.analysis.seq.sam;

/** bam index无法正常读取的报错 */
public class ExceptionSamNoIndexError extends RuntimeException {
	public ExceptionSamNoIndexError() { }

    public ExceptionSamNoIndexError(final String s) {
        super(s);
    }

    public ExceptionSamNoIndexError(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSamNoIndexError(final Throwable throwable) {
        super(throwable);
    }
}
