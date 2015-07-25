package com.novelbio.analysis.seq.sam;

/** 文件不正常结束时报错 */
public class ExceptionSamError extends RuntimeException {
	public ExceptionSamError() { }

    public ExceptionSamError(final String s) {
        super(s);
    }

    public ExceptionSamError(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSamError(final Throwable throwable) {
        super(throwable);
    }
}
