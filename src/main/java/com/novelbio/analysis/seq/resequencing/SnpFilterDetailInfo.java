package com.novelbio.analysis.seq.resequencing;

import com.novelbio.analysis.tools.Mas3.getProbID;

public class SnpFilterDetailInfo {
	/** ���ж�ȡ���ֽ� */
	long allByte;
	long allLines;
	int findSnp;
	/** ����Ϊnullʱ��ʾ����˸�������snp���� */
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
