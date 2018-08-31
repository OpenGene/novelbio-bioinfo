package com.novelbio.bioinfo.sam;

/** bam index无法正常读取的报错 */
public class ExceptionSamIndexError extends RuntimeException {
	public ExceptionSamIndexError() { }

    public ExceptionSamIndexError(final String s) {
        super(s);
    }

    public ExceptionSamIndexError(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSamIndexError(final Throwable throwable) {
        super(throwable);
    }
}
