package com.novelbio.analysis.seq.sam;

/** 文件不正常结束时报错 */
public class ExceptionSamStrandErrorRuntime extends RuntimeException {
	public ExceptionSamStrandErrorRuntime() { }

    public ExceptionSamStrandErrorRuntime(final String s) {
        super(s);
    }

    public ExceptionSamStrandErrorRuntime(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public ExceptionSamStrandErrorRuntime(final Throwable throwable) {
        super(throwable);
    }
}
