package com.novelbio.analysis.seq.resequencing;

/** 给GUI显示的东西 */
public class SnpFilterDetailInfo {
	/** 所有读取的字节 */
	long allByte;
	long allLines;
	int findSnp;
	/** 当不为null时表示需要输出的信息 */
	String showMessage;
	public long getAllByte() {
		return allByte;
	}
	public long getAllLines() {
		return allLines;
	}
	public String getMessage() {
		return showMessage;
	}
	public int getFindSnp() {
		return findSnp;
	}
}
