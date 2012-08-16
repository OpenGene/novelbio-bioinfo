package com.novelbio.analysis.seq.fastq;

import com.novelbio.base.RunProcess;
import com.novelbio.base.multithread.txtreadcopewrite.MTmulitCopeInfo;
import com.novelbio.base.multithread.txtreadcopewrite.MTrecordCoper;

class FastQfilter extends MTmulitCopeInfo<FastQfilterRecorder, FastqRecordInfoFilter>{
	FastQReader fastQRead;
	FastQwrite fastqWrite;
	boolean isPairEnd = false;
	int allRawReadsNum, allFilteredReadsNum;
	
	/** 用作参数设定的 */
	FastQfilterRecorder fastQfilterRecordParam;
	
	boolean isFinished = false;

	public void setFastqWrite(FastQwrite fastqWrite) {
		this.fastqWrite = fastqWrite;
	}
	public void setFilterParam(FastQfilterRecorder fastQfilterRecord) {
		this.fastQfilterRecordParam = fastQfilterRecord;
	}
	public void setIsPairEnd(boolean isPairEnd) {
		this.isPairEnd = isPairEnd;
	}
	public void setFilterThreadNum(int threadFilterNum) {
		for (int i = 0; i < threadFilterNum; i++) {
			FastQfilterRecorder fastqFilterRecord = new FastQfilterRecorder();
			//TODO 设定过滤参数
			fastqFilterRecord.adaptorLowercase = false;
			fastqFilterRecord.phredOffset = 33;
			fastqFilterRecord.readsLenMin = 21;
			addMTcopedRecord(fastqFilterRecord);
		}
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
	protected void doneOneThread(RunProcess<FastqRecordInfoFilter> runProcess) {
		MTrecordCoper<FastqRecordInfoFilter> fastQRecordCope  = (MTrecordCoper)runProcess;
		FastQfilterRecorder fastQfilterRecord = (FastQfilterRecorder)fastQRecordCope;
		allRawReadsNum = allRawReadsNum + fastQfilterRecord.getAllReadsNum();
		allFilteredReadsNum = allFilteredReadsNum + fastQfilterRecord.getFilteredReadsNum();
	}
	@Override
	protected void doneAllThread() {
		fastqWrite.close();
		fastQRead.close();
		isFinished = true;
	}
	@Override
	public void threadSuspend() {
		fastQRead.threadSuspend();
		for (FastQfilterRecorder fastqFilterRecord : lsCopeRecorders) {
			fastqFilterRecord.threadSuspend();
		}
	}

	@Override
	public void threadResume() {
		fastQRead.threadResume();
		for (FastQfilterRecorder fastqFilterRecord : lsCopeRecorders) {
			fastqFilterRecord.threadResume();
		}
	}

	@Override
	public void threadStop() {
		fastQRead.threadStop();
		for (FastQfilterRecorder fastqFilterRecord : lsCopeRecorders) {
			fastqFilterRecord.threadStop();
		}
	}

	@Override
	public void beforeExecute() {
		isFinished = false;
		for (FastQfilterRecorder fastQfilterRecord : lsCopeRecorders) {
			fastQfilterRecord.setParam(fastQfilterRecordParam);
			fastQfilterRecord.setIsPairEnd(isPairEnd);
		}
	}


}

