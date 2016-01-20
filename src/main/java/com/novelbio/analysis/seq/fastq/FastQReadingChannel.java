package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.tools.FileObject;

import org.apache.log4j.Logger;

import com.novelbio.GuiAnnoInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

public class FastQReadingChannel extends RunProcess<GuiAnnoInfo> {
	private static final Logger logger = Logger.getLogger(FastQReadingChannel.class);
	
	List<FastQ[]> lsFastqReader;
	FastQ[] fqWrite = new FastQ[2];
	
	/** 队列最大数量 */
	int maxNumReadInLs = 3000;
	ThreadPoolExecutor executorPool;
	ArrayBlockingQueue<Future<FastQrecordCopeUnit>> queueResult;
	
	/** 过滤器 */
	List<FQrecordCopeInt> lsFQrecordCopeLeft = new ArrayList<FQrecordCopeInt>();
	List<FQrecordCopeInt> lsFQrecordCopeRight = new ArrayList<FQrecordCopeInt>();
	
	/**是否输出过滤的结果，默认为true
	 * false 表示仅进行 fastqc工作
	 */
	boolean isOutputResult = true;
	boolean isCheckFormat = true;
	
	
	/** 输入的FastQ是否为双端，务必一致 */
	public void setFastQRead(List<FastQ[]> lsFastQs) {
		this.lsFastqReader = lsFastQs;
	}
	/** 是否输出过滤文件，false一般用来仅输出fastqc结果 */
	public void setOutputResult(boolean isOutputResult) {
		this.isOutputResult = isOutputResult;
	}
	/** 是否检查fastq文件的格式，true检查，false不检查<br>
	 * 默认为true
	 * @param isCheckFormat
	 */
	public void setCheckFormat(boolean isCheckFormat) {
		this.isCheckFormat = isCheckFormat;
	}
	/** 默认是3000 */
	public void setMaxNumReadInLs(int maxNumReadInLs) {
		this.maxNumReadInLs = maxNumReadInLs;
	}
	/** 设定好new了写入的FastQ对象 */
	public void setFastQWrite(FastQ fastqWrite1, FastQ fastqWrite2) {
		if (fastqWrite2 != null) {
			fastqWrite1.fastQwrite.setFastQwriteMate(fastqWrite2.fastQwrite);
		}
		this.fqWrite[0] = fastqWrite1;
		this.fqWrite[1] = fastqWrite2;
	}
	
	public void clearLsFQcoper() {
		lsFQrecordCopeLeft.clear();
		lsFQrecordCopeRight.clear();
	}
	
	/** 当过滤结束后，可以用该方法返回过滤好的结果 */
	public FastQ[] getFqFiltered() {
		return fqWrite;
	}
	
	/** 不设定或如果fastQRecordFilter 为null，表示不过滤 */
	public void setFilter(FastQFilter fastQRecordFilter, int phredOffset) {
		if (fastQRecordFilter != null) {
			fastQRecordFilter.setPhredOffset(phredOffset);
			lsFQrecordCopeLeft.addAll(fastQRecordFilter.getLsFQfilter());
			lsFQrecordCopeRight.addAll(fastQRecordFilter.getLsFQfilter());
		}
	}
	
	/** 设定质检
	 * 可以在添加过滤器前添加QC，在过滤器添加后还可以再添加一次QC模块
	 * 这样就可以同时检测过滤前和过滤后的QC了
	 */
	public void setFastQC(FastQC fastQCLeft, FastQC fastQCRight) {
		lsFQrecordCopeLeft.addAll(fastQCLeft.getLsModules());
		if (fastQCRight != null) {
			lsFQrecordCopeRight.addAll(fastQCRight.getLsModules());
		}
	}
	
	public void setThreadNum(int threadFilterNum) {
		if (threadFilterNum > 8) {
			threadFilterNum = 8;
		}
		if (threadFilterNum <= 0) {
			threadFilterNum = 1;
		}
		executorPool = new ThreadPoolExecutor(threadFilterNum, (int)(threadFilterNum*1.5), 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(maxNumReadInLs));
		//输出过滤的结果
		if (isOutputResult) {
			queueResult = new ArrayBlockingQueue<Future<FastQrecordCopeUnit>>(maxNumReadInLs);
			fqWrite[0].fastQwrite.setQueue(queueResult);
		}
	
	}
	
	public void runChannel() {
		if (executorPool == null) {
			setThreadNum(4);
		}

		if (lsFastqReader == null || lsFastqReader.size() == 0) return;
		try {
			if (isOutputResult) {
				fqWrite[0].fastQwrite.setFinishedRead(false);
				Thread thread = new Thread(fqWrite[0].fastQwrite);
				thread.setDaemon(true);
				thread.start();
			}

			if (lsFastqReader.get(0).length == 2) {
				readPE();
			} else {
				readSE();
			}
			
			if (isOutputResult) {
				closeThread();
			}
			executorPool.shutdown();
			executorPool = null;
			queueResult = null;
		} catch (Exception e) {
			closeThread();
			executorPool.shutdown();
			executorPool = null;
			queueResult = null;
			throw e;
		}
	}
	
	private void closeThread() {
		fqWrite[0].fastQwrite.setFinishedRead(true);
		while (fqWrite[0].fastQwrite.isRunning()) {
			try { Thread.sleep(100); 	} catch (InterruptedException e) { e.printStackTrace(); }
		}
		fqWrite[0].close();
		try {
			fqWrite[1].close();
		} catch (Exception e) {
			logger.error("close thread error", e);
		}
	}
	
	private void readSE() {
		long readsNum = 0, readByte = 0;
		
		for (FastQ[] fastQs : lsFastqReader) {
			if (flagStop) break;
			fastQs[0].fastQRead.setCheckFormat(isCheckFormat);
			for (FastQRecord fastQRecord : fastQs[0].readlines()) {
				readsNum++;
				wait_To_Cope_AbsQueue();
				if (flagStop) break;
				
				FastQrecordCopeUnit fastQrecordFilterRun = new FastQrecordCopeUnit();
				fastQrecordFilterRun.setFastQRecordFilter(lsFQrecordCopeLeft, lsFQrecordCopeRight);
				fastQrecordFilterRun.setFastQRecordSE(fastQRecord);
				Future<FastQrecordCopeUnit> future = executorPool.submit(fastQrecordFilterRun);
				if (isOutputResult) {
					queueResult.add(future);
				}
				
				if (readsNum % 500000 == 0) {
					setGUIinfo(readByte, readsNum, fastQs[0]);
				}
			}
			fastQs[0].close();
			if (isOutputResult) {
				fqWrite[0].fastQwrite.flash();
			}
		
		}
	}
	
	private void readPE() {
		long readsNum = 0, readByte = 0;
		for (FastQ[] fastQs : lsFastqReader) {
			if (flagStop) break;
			
			fastQs[0].fastQRead.setCheckFormat(isCheckFormat);
			fastQs[1].fastQRead.setCheckFormat(isCheckFormat);
			
			fastQs[0].fastQRead.setFastQReadMate(fastQs[1].fastQRead);

			int notPairedNum = 0;
			for (FastQRecord[] fastQRecord : fastQs[0].fastQRead.readlinesPE()) {
				readsNum++;
				wait_To_Cope_AbsQueue();
				if (flagStop) break;
				
				FastQrecordCopeUnit fastQrecordFilterRun = new FastQrecordCopeUnit();
				fastQrecordFilterRun.setFastQRecordFilter(lsFQrecordCopeLeft, lsFQrecordCopeRight);
				
				fastQrecordFilterRun.setFastQRecordSE(fastQRecord[0]);
				fastQrecordFilterRun.setFastQRecordPE(fastQRecord[1]);
				
				Future<FastQrecordCopeUnit> future = executorPool.submit(fastQrecordFilterRun);
				if (isOutputResult) {
					queueResult.add(future);
				}
				if (readsNum % 500000 == 0) {
					setGUIinfo(readByte, readsNum, fastQs[0]);
				}
			}
			fastQs[0].close();
			fastQs[1].close();
			if (isOutputResult) {
				fqWrite[0].fastQwrite.flash();
			}
		}
	}
	
	/** 将中间结果发送到GUI */
	private void setGUIinfo(long readByte, long readsNum, FastQ fastQ) {
		readByte = readByte + fastQ.getReadByte();
		GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
		guiAnnoInfo.setDouble(readByte);
		guiAnnoInfo.setNum(readsNum);
		guiAnnoInfo.setInfo("Filter File:" + fastQ.getReadFileName() + " percentage:" + (fastQ.getReadPercentage() * 100) + "%");
		setRunInfo(guiAnnoInfo);
	}
	
	/** 等待处理线程将AbsQueue队列中的记录处理掉 */
	protected void wait_To_Cope_AbsQueue() {
		suspendCheck();
		if (isOutputResult && fqWrite[0].fastQwrite.getRunThreadStat() != RunThreadStat.running) {
			throw new ExceptionFastq(fqWrite[0].fastQwrite.getFileName() + " fastq write error", fqWrite[0].fastQwrite.getException());
		}

		while (executorPool.getQueue().size() == maxNumReadInLs || (queueResult != null && queueResult.size() == maxNumReadInLs)) {
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
	}
	
	@Override
	public void running() {
		runChannel();
	}
	
}

