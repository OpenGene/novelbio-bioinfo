package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.base.RunProcess;
/** 如果想要读取fastq文件，就可以继承该类，然后实现copeFastQRecord() 方法即可 */
public abstract class FastQRecordCope<T> extends RunProcess<T>{
	ArrayBlockingQueue<FastQRecord[]> lsFastQRecords;
	//主要是看读取是否完毕
	FastQRead fastQRead;
	boolean pairEnd = false;

	public void setLsFastQRecords(ArrayBlockingQueue<FastQRecord[]> lsFastQRecords) {
		this.lsFastQRecords = lsFastQRecords;
	}
	public void setIsPairEnd(boolean isPairEnd) {
		this.pairEnd = isPairEnd;
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
	/** 需要在running之前处理的信息，譬如计数、参数设定等，不需要的话留空 */
	protected abstract void copeInfo();
	/** 本类会网里面源源不断的输入record，只要处理这里的单个 FastQRecord就好
	 * 本方法是多线程设计的，所以如果外部GUI需要获取信息，
	 * 则可在本方法中设置 setRunInfo(FastqRecordInfoFilter fastqRecordInfo);<br><br>
	 *  计数等工作也可以在本方法内部完成
	 * */
	protected abstract void copeFastQRecordSE(FastQRecord fastQRecord);
	
	/** 本类会网里面源源不断的输入record，只要处理这里的成对 FastQRecord就好
	 * 本方法是多线程设计的，所以如果外部GUI需要获取信息，
	 * 则可在本方法中设置 setRunInfo(FastqRecordInfoFilter fastqRecordInfo);<br><br>
	 *  计数等工作也可以在本方法内部完成
	 * */
	protected abstract void copeFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2);
}
