package com.novelbio.analysis.seq.sam;

/** 文件不正常结束时报错 */
public class SamErrorException extends RuntimeException {
	public SamErrorException() { }

    public SamErrorException(final String s) {
        super(s);
    }

    public SamErrorException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    public SamErrorException(final Throwable throwable) {
        super(throwable);
    }
}
