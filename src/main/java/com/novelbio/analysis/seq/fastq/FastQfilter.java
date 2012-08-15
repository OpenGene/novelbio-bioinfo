package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;

import com.novelbio.base.RunGetInfo;
import com.novelbio.base.RunProcess;

class FastQfilter implements RunGetInfo<FastqRecordInfoFilter>{
	FastQRead fastQRead;
	FastQwrite fastqWrite;
	boolean isPairEnd = false;
	int allRawReadsNum, allFilteredReadsNum;
	
	/** 用作参数设定的 */
	FastQfilterRecord fastQfilterRecordParam;
	int threadStopNum = 0;
	ArrayList<FastQfilterRecord> lsFilter = new ArrayList<FastQfilterRecord>();
	
	boolean isFinished = false;
	
	public void setFastQRead(FastQRead fastQRead) {
		this.fastQRead = fastQRead;
	}
	public void setFastqWrite(FastQwrite fastqWrite) {
		this.fastqWrite = fastqWrite;
	}
	public void setFilterParam(FastQfilterRecord fastQfilterRecord) {
		this.fastQfilterRecordParam = fastQfilterRecord;
	}
	public void setIsPairEnd(boolean isPairEnd) {
		this.isPairEnd = isPairEnd;
	}
	public void setFilterThreadNum(int threadFilterNum) {
		for (int i = 0; i < threadFilterNum; i++) {
			FastQfilterRecord fastqFilterRecord = new FastQfilterRecord();
			//TODO 设定过滤参数
			fastqFilterRecord.adaptorLowercase = false;
			fastqFilterRecord.phredOffset = 33;
			fastqFilterRecord.readsLenMin = 21;
			this.lsFilter.add(fastqFilterRecord);
		}
		fastQRead.setLsFilterReads(lsFilter);
	}

	@Override
	public void setRunningInfo(FastqRecordInfoFilter info) {
		synchronized (this) {
			if (info.singleEnd) {
				fastqWrite.writeFastQRecord(info.fastQRecord1);
			}
			else {
				fastqWrite.writeFastQRecord(info.fastQRecord1, info.fastQRecord2);
			}
		}
	}
	
	@Override
	public void done(RunProcess<FastqRecordInfoFilter> runProcess) {
		synchronized (this) {
			threadStopNum++;
			FastQfilterRecord fastQfilterRecord = (FastQfilterRecord) runProcess;
			allRawReadsNum = allRawReadsNum + fastQfilterRecord.getAllReadsNum();
			allFilteredReadsNum = allFilteredReadsNum + fastQfilterRecord.getFilteredReadsNum();
			if (threadStopNum == lsFilter.size()) {
				//TODO
				fastqWrite.close();
				fastQRead.close();
				isFinished = true;
			}
		}
	}

	@Override
	public void threadSuspend() {
		fastQRead.threadSuspend();
		for (FastQfilterRecord fastqFilterRecord : lsFilter) {
			fastqFilterRecord.threadSuspend();
		}
	}

	@Override
	public void threadResume() {
		fastQRead.threadResume();
		for (FastQfilterRecord fastqFilterRecord : lsFilter) {
			fastqFilterRecord.threadResume();
		}
	}

	@Override
	public void threadStop() {
		fastQRead.threadStop();
		for (FastQfilterRecord fastqFilterRecord : lsFilter) {
			fastqFilterRecord.threadStop();
		}
	}

	@Override
	public void execute() {
		isFinished = false;
		 
		for (FastQfilterRecord fastQfilterRecord : lsFilter) {
			fastQfilterRecord.setParam(fastQfilterRecordParam);
//			fastQfilterRecord.setIsPairEnd(isPairEnd);
		}
		
		Thread thread = new Thread(fastQRead);
		thread.start();
		for (FastQfilterRecord fastqFilterRecord : lsFilter) {
			fastqFilterRecord.setRunGetInfo(this);
			thread = new Thread(fastqFilterRecord);
			thread.start();
		}
	}

}

