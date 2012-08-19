package com.novelbio.analysis.seq.fastq;

import com.novelbio.base.multithread.RunProcess;
import com.novelbio.base.multithread.txtreadcopewrite.MTmulitCopeInfo;
import com.novelbio.base.multithread.txtreadcopewrite.MTrecordCoper;

class FastQfilter extends MTmulitCopeInfo<FastQRecordFilter, FastqRecordInfoFilter>{
	FastQwrite fastqWrite;
	boolean isPairEnd = false;
	int allRawReadsNum, allFilteredReadsNum;
	
	/** 用作参数设定的 */
	FastQRecordFilter fastQfilterRecordParam;
	
	
	public void setFastqWrite(FastQwrite fastqWrite) {
		this.fastqWrite = fastqWrite;
	}
	public void setFilterParam(FastQRecordFilter fastQfilterRecord) {
		this.fastQfilterRecordParam = fastQfilterRecord;
	}
	public void setIsPairEnd(boolean isPairEnd) {
		this.isPairEnd = isPairEnd;
	}
	public void setFilterThreadNum(int threadFilterNum) {
		for (int i = 0; i < threadFilterNum; i++) {
			FastQRecordFilter fastqFilterRecord = new FastQRecordFilter();
			//TODO 设定过滤参数
			fastqFilterRecord.adaptorLowercase = false;
			fastqFilterRecord.phredOffset = 33;
			fastqFilterRecord.readsLenMin = 21;
			addMTcopedRecord(fastqFilterRecord);
		}
	}

	@Override
	protected void copeReadInfo(FastqRecordInfoFilter info) {
		if (info.singleEnd) {
			fastqWrite.writeFastQRecord(info.fastQRecord1);
		}
		else {
			fastqWrite.writeFastQRecord(info.fastQRecord1, info.fastQRecord2);
		}
	}
	@Override
	protected void doneOneThread(RunProcess<FastqRecordInfoFilter> runProcess) {
		MTrecordCoper<FastqRecordInfoFilter> fastQRecordCope  = (MTrecordCoper)runProcess;
		FastQRecordFilter fastQfilterRecord = (FastQRecordFilter)fastQRecordCope;
		allRawReadsNum = allRawReadsNum + fastQfilterRecord.getAllReadsNum();
		allFilteredReadsNum = allFilteredReadsNum + fastQfilterRecord.getFilteredReadsNum();
	}
	@Override
	protected void doneAllThread() {
		fastqWrite.close();
	}

	@Override
	public void beforeExecute() {
		for (FastQRecordFilter fastQfilterRecord : lsCopeRecorders) {
			fastQfilterRecord.setParam(fastQfilterRecordParam);
			fastQfilterRecord.setIsPairEnd(isPairEnd);
		}
	}


}

