package com.novelbio.analysis.seq.resequencing;

/** ��GUI��ʾ�Ķ��� */
public class SnpFilterDetailInfo {
	/** ���ж�ȡ���ֽ� */
	long allByte;
	long allLines;
	int findSnp;
	/** ����Ϊnullʱ��ʾ��Ҫ�������Ϣ */
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
