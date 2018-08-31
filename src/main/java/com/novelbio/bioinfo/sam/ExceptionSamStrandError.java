package com.novelbio.bioinfo.sam;

/** 文件不正常结束时报错 */
public class ExceptionSamStrandError extends Exception {
	public ExceptionSamStrandError() { }

    public ExceptionSamStrandError(final String s) {
        super(s);
    }

    public ExceptionSamStrandError(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSamStrandError(final Throwable throwable) {
        super(throwable);
    }
}
