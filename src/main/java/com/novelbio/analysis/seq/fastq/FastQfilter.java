package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.novelbio.base.multithread.RunProcess;

class FastQfilter extends RunProcess<FastQrecordFilterRun> {
	public static void main(String[] args) {
		FastQ fastQ1 = new FastQ("/media/winF/NBC/Project/Project_HXW/20121018/��ɽҽԺ ���Һ�/10A 10B/test/10B_1.fq");
		FastQ fastQ2 = new FastQ("/media/winF/NBC/Project/Project_HXW/20121018/��ɽҽԺ ���Һ�/10A 10B/test/10B_2.fq");
		FastQRecordFilter fastQfilterRecord = new FastQRecordFilter();
		fastQfilterRecord.setFilterParamTrimNNN(true);
		fastQfilterRecord.setFilterParamReadsLenMin(50);
		fastQ1.setFilter(fastQfilterRecord);
		fastQ1.filterReads(fastQ2);
	}
	Logger logger = Logger.getLogger(FastQfilter.class);
	
	FastQReader fastQReader;
	FastQthreadWrite fastQthreadWrite = new FastQthreadWrite();
	
	int allReadsNum, allFilteredReadsNum;
	
	/** ���������趨�� */
	FastQRecordFilter fastQRecordFilter;
	
	/** ����������� */
	int maxNumReadInLs = 5000;
	ThreadPoolExecutor executorPool;
	ArrayBlockingQueue<Future<FastQrecordFilterRun>> queueResult;
	
	public FastQfilter(FastQReader fastQReader, FastQwrite fastqWrite) {
		this.fastQReader = fastQReader;
		fastQthreadWrite.setFastQwrite(fastqWrite);
	}
	
	public void setFilter(FastQRecordFilter fastQRecordFilter) {
		this.fastQRecordFilter = fastQRecordFilter;
	}
	
	public void setThreadNum(int threadFilterNum) {
		if (threadFilterNum > 5) {
			threadFilterNum = 5;
		}
		if (threadFilterNum <= 0) {
			threadFilterNum = 1;
		}
		executorPool = new ThreadPoolExecutor(threadFilterNum, (int)(threadFilterNum*1.5), 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(maxNumReadInLs));
		queueResult = new ArrayBlockingQueue<Future<FastQrecordFilterRun>>(maxNumReadInLs);
		fastQthreadWrite.setQueue(queueResult);
	}
	
	public void filtering() {
		fastQRecordFilter.setPhredOffset(fastQReader.getOffset());
		fastQthreadWrite.setFinishedRead(false);
		Thread thread = new Thread(fastQthreadWrite);
		thread.start();
		if (fastQReader.isPairEnd()) {
			readPE();
		} else {
			readSE();
		}
		fastQthreadWrite.setFinishedRead(true);
		while (!fastQthreadWrite.isFinished()) {
			try { Thread.sleep(100); 	} catch (InterruptedException e) { e.printStackTrace(); }
		}
		allFilteredReadsNum = fastQthreadWrite.getFilteredNum();
		executorPool.shutdown();
		logger.error(executorPool.getPoolSize());
		logger.error(executorPool.getQueue().size());
		fastQReader.close();
		fastQthreadWrite.close();
	}
	
	private void readSE() {
		allReadsNum = 0;
		for (FastQRecord fastQRecord : fastQReader.readlines(false)) {
			allReadsNum++;
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastQrecordFilterRun fastQrecordFilterRun = new FastQrecordFilterRun();
			fastQrecordFilterRun.setFastQRecordFilter(fastQRecordFilter);
			fastQrecordFilterRun.setFastQRecordSE(fastQRecord);
			Future<FastQrecordFilterRun> future = executorPool.submit(fastQrecordFilterRun);
			queueResult.add(future);
			
			if (allReadsNum % 50000 == 0) {
				setRunInfo(fastQrecordFilterRun);
			}
		}
	}
	private void readPE() {
		allReadsNum = 0;
		for (FastQRecord[] fastQRecord : fastQReader.readlinesPE(false)) {
			allReadsNum++;
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastQrecordFilterRun fastQrecordFilterRun = new FastQrecordFilterRun();
			fastQrecordFilterRun.setFastQRecordFilter(fastQRecordFilter);
			fastQrecordFilterRun.setFastQRecordPE(fastQRecord[0], fastQRecord[1]);
			Future<FastQrecordFilterRun> future = executorPool.submit(fastQrecordFilterRun);
			queueResult.add(future);
			if (allReadsNum % 50000 == 0) {
				setRunInfo(fastQrecordFilterRun);
			}
		}
	}
	
	/** �ȴ������߳̽�AbsQueue�����еļ�¼����� */
	protected void wait_To_Cope_AbsQueue() {
		suspendCheck();
		while (executorPool.getQueue().size() == maxNumReadInLs || queueResult.size() == maxNumReadInLs) {
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
	}
	
	@Override
	protected void running() {
		filtering();
	}
	
}

