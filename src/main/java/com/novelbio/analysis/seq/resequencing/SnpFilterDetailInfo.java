package com.novelbio.analysis.seq.resequencing;

import com.novelbio.analysis.tools.Mas3.getProbID;

public class SnpFilterDetailInfo {
	/** 所有读取的字节 */
	long allByte;
	long allLines;
	int findSnp;
	/** 当不为null时表示完成了该样本的snp查找 */
	String fileName;
	public long getAllByte() {
		return allByte;
	}
	public long getAllLines() {
		return allLines;
	}
	public String getFileName() {
		return fileName;
	}
	public int getFindSnp() {
		return findSnp;
	}
}
