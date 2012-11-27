package com.novelbio.analysis.seq.fastq;

import java.util.Queue;
import java.util.concurrent.Future;

import com.novelbio.base.multithread.RunProcess;

/**
 * 将blockqueen中的东西写入文本
 * @author zong0jie
 */
public class FastQthreadWrite extends RunProcess<Integer>{
	FastQwrite fastQwrite;

	Queue<Future<FastQrecordFilterRun>> queue;
	
	int filteredNum = 0;
	boolean finishedRead = false;
	
	public void setFastQwrite(FastQwrite fastQwrite) {
		this.fastQwrite = fastQwrite;
	}
	
	/** 输入处理好的队列 */
	public void setQueue(Queue<Future<FastQrecordFilterRun>> queue) {
		this.queue = queue;
	}
	/** 等读取结束后设定 */
	public void setFinishedRead(boolean finishedRead) {
		this.finishedRead = finishedRead;
	}
	
	public int getFilteredNum() {
		return filteredNum;
	}
	
	@Override
	protected void running() {
		while (!finishedRead || queue.size() != 0) {
			Future<FastQrecordFilterRun> future = queue.poll();
			if (future == null) {
				continue;
			}
			while (!future.isDone()) {
				try { Thread.sleep(1); } catch (InterruptedException e) { }
			}

			FastQrecordFilterRun fastQrecordFilterRun = null;
			try { fastQrecordFilterRun = future.get(); } catch (Exception e) { e.printStackTrace();}
			
			if (fastQrecordFilterRun != null && fastQrecordFilterRun.isFilterSucess()) {
				filteredNum++;
				setRunInfo(filteredNum);
				writeInFile(fastQrecordFilterRun);
				fastQrecordFilterRun = null;
			}
			future = null;
		}
	}
	
	private void writeInFile(FastQrecordFilterRun fastQrecordFilterRun) {
		if (fastQrecordFilterRun.getFastQRecord1Filtered() != null) {
			if (fastQrecordFilterRun.isPairEnd()) {
				fastQwrite.writeFastQRecordString(fastQrecordFilterRun.getFastQRecord1Filtered(), fastQrecordFilterRun.getFastQRecord2Filtered());
			} else {
				fastQwrite.writeFastQRecordString(fastQrecordFilterRun.getFastQRecord1Filtered());
			}
		}
	}
	public void close() {
		try {
			fastQwrite.close();
		} catch (Exception e) {
		}
	}
}
