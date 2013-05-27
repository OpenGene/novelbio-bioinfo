package com.novelbio.analysis.seq.fastq;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;

class FastQwriter extends RunProcess<Integer> {
	private static final Logger logger = Logger.getLogger(FastQwriter.class);
	/** 线程池，里面应该是已经处理好的reads单元 */
	Queue<Future<FastQrecordCopeUnit>> queue;
	
	/** 多线程时使用，记录写入了多少条记录 */
	int writeReadsNum = 0;
	boolean finishedRead = false;
	
	TxtReadandWrite txtSeqFile;
	
	FastQwriter fastQwriteMate;
	
	public FastQwriter() {}
	/**
	 * 自动判断 FastQ的格式
	 * @param seqFile
	 */
	public FastQwriter(String seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, true);
	}
	
	protected String getFileName() {
		return txtSeqFile.getFileName();
	}
	
	public void setFastQwriteMate(FastQwriter fastQwriteMate) {
		this.fastQwriteMate = fastQwriteMate;
	}
	
	/**
	 * 写入文本
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	protected void writeFastQRecordString(String fastQRecord1, String fastQRecord2) {
		if (fastQRecord1 == null || fastQRecord1.equals("")) {
			return;
		}
		
		if (fastQwriteMate == null) {
			txtSeqFile.writefileln(fastQRecord1);
		} else {
			if (fastQRecord2 == null || fastQRecord2.equals("")) {
				return;
			}
			txtSeqFile.writefileln(fastQRecord1);
			fastQwriteMate.txtSeqFile.writefileln(fastQRecord2);
		}
	}
	/**
	 * 写入文本
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecordString(String fastQRecord) {
		if (fastQRecord == null || fastQRecord.equals("")) {
			return;
		}
		txtSeqFile.writefileln(fastQRecord);
	}
	
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		String seq = fastQRecord.toString();
		if (seq != null) {
			txtSeqFile.writefileln(seq);
		}
	}
	/**
	 * 不关闭
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		for (FastQRecord fastQRecord : lsFastQRecords) {
			writeFastQRecord(fastQRecord);
		}
	}
	
	/**
	 * 写完后务必用此方法关闭
	 * 关闭输入流，并将fastQ写入转化为fastQ读取
	 */
	public void close() {
		try { 
			txtSeqFile.flash();
			txtSeqFile.close(); 
		} catch (Exception e) { }
	
		if (fastQwriteMate != null) {
			fastQwriteMate.close();
		}
	}
	
	/** 输入处理好的队列 */
	public void setQueue(Queue<Future<FastQrecordCopeUnit>> queue) {
		this.queue = queue;
	}
	/** 等读取结束后设定 */
	public void setFinishedRead(boolean finishedRead) {
		this.finishedRead = finishedRead;
	}
	
	public int getFilteredNum() {
		return writeReadsNum;
	}
	
	@Override
	protected void running() {
		while (!finishedRead || queue.size() != 0) {
			Future<FastQrecordCopeUnit> future = queue.poll();
			if (future == null) {
				continue;
			}
			while (!future.isDone()) {
				try { Thread.sleep(1); } catch (InterruptedException e) { }
			}

			FastQrecordCopeUnit fastQrecordFilterRun = null;
			try { fastQrecordFilterRun = future.get(); } catch (Exception e) { e.printStackTrace();}
			
			if (fastQrecordFilterRun != null && fastQrecordFilterRun.isFilterSucess()) {
				writeReadsNum++;
				setRunInfo(writeReadsNum);
				writeInFile(fastQrecordFilterRun);
				fastQrecordFilterRun = null;
			}
			future = null;
		}
	}
	
	private void writeInFile(FastQrecordCopeUnit fastQrecordFilterRun) {
		if (fastQrecordFilterRun.getFastQRecord1Filtered() != null) {
			if (fastQrecordFilterRun.isPairEnd()) {
				writeFastQRecordString(fastQrecordFilterRun.getFastQRecord1Filtered(), fastQrecordFilterRun.getFastQRecord2Filtered());
			} else {
				writeFastQRecordString(fastQrecordFilterRun.getFastQRecord1Filtered());
			}
		}
	}
	
}
