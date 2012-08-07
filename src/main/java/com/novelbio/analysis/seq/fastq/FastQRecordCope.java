package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.base.RunProcess;

public abstract class FastQRecordCope<T> extends RunProcess<T>{
	ArrayBlockingQueue<FastQRecord> lsFastQRecords;
	
	//主要是看读取是否完毕
	FastQRead fastQRead;
	
	public void setLsFastQRecords(ArrayBlockingQueue<FastQRecord> lsFastQRecords) {
		this.lsFastQRecords = lsFastQRecords;
	}
	/** 主要是看读取是否完毕 */
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
	
	/** 处理序列 */
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
	/** 需要在running之前处理的信息，譬如计数、参数设定等，不需要的话留空 */
	protected abstract void copeInfo();
	/** 本类会网里面源源不断的输入record，只要处理这里的单个 FastQRecord就好 */
	protected abstract void copeFastQRecord(FastQRecord fastQRecord);
}
