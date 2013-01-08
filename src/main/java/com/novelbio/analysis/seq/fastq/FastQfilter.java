package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.novelbio.base.multithread.RunProcess;

class FastQfilter extends RunProcess<FastQrecordFilterRun> {
	public static void main(String[] args) {
		FastQ fastQ1 = new FastQ("/media/winF/NBC/Project/Project_HXW/20121018/中山医院 蒋家好/10A 10B/test/10B_1.fq");
		FastQ fastQ2 = new FastQ("/media/winF/NBC/Project/Project_HXW/20121018/中山医院 蒋家好/10A 10B/test/10B_2.fq");
		FastQRecordFilter fastQfilterRecord = new FastQRecordFilter();
		fastQfilterRecord.setFilterParamTrimNNN(true);
		fastQfilterRecord.setFilterParamReadsLenMin(50);
		fastQ1.setFilter(fastQfilterRecord);
		fastQ1.filterReads(fastQ2);
	}
	Logger logger = Logger.getLogger(FastQfilter.class);
	
	FastQReader fastQReader;
	FastQthreadWrite fastQthreadWrite = new FastQthreadWrite();
	
	int allFilteredReadsNum;
	
	/** 用作参数设定的 */
	FastQRecordFilter fastQRecordFilter;
	
	/** 队列最大数量 */
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
		fastQReader.readsNum = 0;
		for (FastQRecord fastQRecord : fastQReader.readlines(false)) {
			fastQReader.readsNum ++;
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastQrecordFilterRun fastQrecordFilterRun = new FastQrecordFilterRun();
			fastQrecordFilterRun.setFastQRecordFilter(fastQRecordFilter);
			fastQrecordFilterRun.setFastQRecordSE(fastQRecord);
			Future<FastQrecordFilterRun> future = executorPool.submit(fastQrecordFilterRun);
			queueResult.add(future);
			
			if (fastQReader.readsNum % 50000 == 0) {
				setRunInfo(fastQrecordFilterRun);
			}
		}
	}
	private void readPE() {
		fastQReader.readsNum = 0;
		try { fastQReader.fastQReadMate.readsNum = 0; } catch (Exception e) { }
	
		for (FastQRecord[] fastQRecord : fastQReader.readlinesPE(false)) {
			fastQReader.readsNum ++;
			try { fastQReader.fastQReadMate.readsNum++; } catch (Exception e) { }
			
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastQrecordFilterRun fastQrecordFilterRun = new FastQrecordFilterRun();
			fastQrecordFilterRun.setFastQRecordFilter(fastQRecordFilter);
			fastQrecordFilterRun.setFastQRecordPE(fastQRecord[0], fastQRecord[1]);
			Future<FastQrecordFilterRun> future = executorPool.submit(fastQrecordFilterRun);
			queueResult.add(future);
			if (fastQReader.readsNum % 50000 == 0) {
				setRunInfo(fastQrecordFilterRun);
			}
		}
	}
	
	/** 等待处理线程将AbsQueue队列中的记录处理掉 */
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

