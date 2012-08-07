package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.base.RunProcess;

public abstract class FastQRecordCope<T> extends RunProcess<T>{
	ArrayBlockingQueue<FastQRecord> lsFastQRecords;
	
	//��Ҫ�ǿ���ȡ�Ƿ����
	FastQRead fastQRead;
	
	public void setLsFastQRecords(ArrayBlockingQueue<FastQRecord> lsFastQRecords) {
		this.lsFastQRecords = lsFastQRecords;
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
			
			FastQRecord fastQRecord = lsFastQRecords.poll();
			if (fastQRecord == null) {
				continue;
			}
			copeFastQRecord(fastQRecord);
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
	/** �����������ԴԴ���ϵ�����record��ֻҪ��������ĵ��� FastQRecord�ͺ� */
	protected abstract void copeFastQRecord(FastQRecord fastQRecord);
}
