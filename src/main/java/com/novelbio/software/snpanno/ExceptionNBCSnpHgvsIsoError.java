package com.novelbio.software.snpanno;

/** gff文件错误，导致后面没有序列了
 * 譬如序列为 
 * ATG-CAT-CGG-CA
 * 最后是CA，无法组成一个氨基酸
 * @author zong0jie
 * @data 2019年1月21日
 */
public class ExceptionNBCSnpHgvsIsoError extends RuntimeException {
	private static final long serialVersionUID = 3281480531727342133L;

	public ExceptionNBCSnpHgvsIsoError(String msg) {
		super(msg);
	}
	
	public ExceptionNBCSnpHgvsIsoError(String msg, Throwable t) {
		super(msg, t);
	}
}
