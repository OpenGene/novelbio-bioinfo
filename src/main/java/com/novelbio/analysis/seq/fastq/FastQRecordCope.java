package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.base.RunProcess;
/** �����Ҫ��ȡfastq�ļ����Ϳ��Լ̳и��࣬Ȼ��ʵ��copeFastQRecord() �������� */
public abstract class FastQRecordCope<T> extends RunProcess<T>{
	ArrayBlockingQueue<FastQRecord[]> lsFastQRecords;
	//��Ҫ�ǿ���ȡ�Ƿ����
	FastQRead fastQRead;
	boolean pairEnd = false;

	public void setLsFastQRecords(ArrayBlockingQueue<FastQRecord[]> lsFastQRecords) {
		this.lsFastQRecords = lsFastQRecords;
	}
	public void setIsPairEnd(boolean isPairEnd) {
		this.pairEnd = isPairEnd;
	}
	/** ��Ҫ�ǿ���ȡ�Ƿ���� */
	public void setFastQRead(FastQRead fastQRead) {
		this.fastQRead = fastQRead;
	}
	
	@Override
	protected void running() {
		copeInfo();
		try {
			copeLsReads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** �������� */
	protected void copeLsReads() throws InterruptedException {
		while (true) {
			if (isReadingFinished()) {
				break;
			}
			if (flagStop) {
				break;
			}
			suspendCheck();
			FastQRecord[] fastQRecord = lsFastQRecords.poll();
			if (fastQRecord == null) {
				continue;
			}
			if (!pairEnd) {
				copeFastQRecordSE(fastQRecord[0]);
			}
			else {
				copeFastQRecordPE(fastQRecord[0], fastQRecord[1]);
			}
		}
	}
	
	protected boolean isReadingFinished() throws InterruptedException {
		while (lsFastQRecords.isEmpty()) {
			if (fastQRead.isFinished()) {
				return true;
			}
			Thread.sleep(10);
		}
		return false;
	}
	/** ��Ҫ��running֮ǰ�������Ϣ��Ʃ������������趨�ȣ�����Ҫ�Ļ����� */
	protected abstract void copeInfo();
	/** �����������ԴԴ���ϵ�����record��ֻҪ��������ĵ��� FastQRecord�ͺ�
	 * �������Ƕ��߳���Ƶģ���������ⲿGUI��Ҫ��ȡ��Ϣ��
	 * ����ڱ����������� setRunInfo(FastqRecordInfoFilter fastqRecordInfo);<br><br>
	 *  �����ȹ���Ҳ�����ڱ������ڲ����
	 * */
	protected abstract void copeFastQRecordSE(FastQRecord fastQRecord);
	
	/** �����������ԴԴ���ϵ�����record��ֻҪ��������ĳɶ� FastQRecord�ͺ�
	 * �������Ƕ��߳���Ƶģ���������ⲿGUI��Ҫ��ȡ��Ϣ��
	 * ����ڱ����������� setRunInfo(FastqRecordInfoFilter fastqRecordInfo);<br><br>
	 *  �����ȹ���Ҳ�����ڱ������ڲ����
	 * */
	protected abstract void copeFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2);
}
