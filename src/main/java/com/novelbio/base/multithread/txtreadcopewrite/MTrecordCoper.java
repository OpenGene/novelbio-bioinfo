package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.AbstractQueue;

import com.novelbio.base.multithread.RunProcess;

public abstract class MTrecordCoper<T extends MTRecordCope> extends RunProcess<MTRecordCope>{
	
	protected MTRecoreReader<?, ? extends MTRecordRead> mtOneThreadReader;
	/** ��ȡ�õ������ݾͱ����������� */
	protected AbstractQueue<? extends MTRecordRead> absQueue;
	
	/** ��Ҫ�ǿ���ȡ�Ƿ���� */
	public void setReader(MTRecoreReader<?, ? extends MTRecordRead> mtOneThreadReader) {
		this.mtOneThreadReader = mtOneThreadReader;
	}
	/** �趨����ȡ��list�������̹߳���һ��list */
	public void setLsRecords(AbstractQueue<? extends MTRecordRead> absQueue) {
		this.absQueue = absQueue;
	}
	@Override
	protected void running() {
		copeBeforeRun();
		try {
			copeLsReads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean isReadingFinished() throws InterruptedException {
		while (absQueue.isEmpty()) {
			if (mtOneThreadReader.isFinished()) {
				return true;
			}
			Thread.sleep(20);
		}
		return false;
	}
	/** ��Ҫ��running֮ǰ�������Ϣ��Ʃ������������趨�ȣ�����Ҫ�Ļ����� */
	protected abstract void copeBeforeRun();
	
	/** �������� */
	protected abstract void copeLsReads() throws InterruptedException;
}
