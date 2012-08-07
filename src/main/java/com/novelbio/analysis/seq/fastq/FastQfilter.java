package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.RunGetInfo;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQfilter implements RunGetInfo<FastqRecordInfoFilter, FastQfilterRecord>{
	FastQRead fastQRead;
	FastQwrite fastqWrite;
	
	int allRawReadsNum, allFilteredReadsNum;
	
	/** 用作参数设定的 */
	FastQfilterRecord fastQfilterRecordParam;
	int threadStopNum = 0;
	ArrayList<FastQfilterRecord> lsFilter = new ArrayList<FastQfilterRecord>();
	
	public static void main(String[] args) {
		
		FastQfilter fastQfilter = new FastQfilter();
		fastQfilter.setInFastq("/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/BZ171-9522_GTGAAA_L003_R2_001.fastq.gz");
		fastQfilter.setOutFastq("/home/zong0jie/Desktop/BZ171-9522_GTGAAA_L003_R2_001.fastq_filter.txt");

		fastQfilter.setFilterThreadNum(5);
		fastQfilter.execute();

//		FastQ fastQ = new FastQ("/home/zong0jie/Desktop/BZ171-9522_GTGAAA_L003_R2_001.fastq.gz");
//		fastQ.setCaseLowAdaptor(true);
//		fastQ.filterReads();
//		System.out.println(dateTime.getEclipseTime());
		
	}


	public void setFastQRead(FastQRead fastQRead) {
		this.fastQRead = fastQRead;
	}
	public void setFastqWrite(FastQwrite fastqWrite) {
		this.fastqWrite = fastqWrite;
	}
	public void setInFastq(String fastqNameIn) {
		fastQRead = new FastQRead();
		fastQRead.setFastqFile(fastqNameIn);
	}
	
	public void setOutFastq(String fastqNameOut) {
		fastqWrite = new FastQwrite(fastqNameOut);
	}
	public void setFilterParam(FastQfilterRecord fastQfilterRecord) {
		this.fastQfilterRecordParam = fastQfilterRecord;
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
			fastqWrite.writeFastQRecord(info.fastQRecord);
		}
	}
	
	@Override
	public void done(FastQfilterRecord fastQfilterRecord) {
		synchronized (this) {
			threadStopNum++;
			allRawReadsNum = allRawReadsNum + fastQfilterRecord.getAllReadsNum();
			allFilteredReadsNum = allFilteredReadsNum + fastQfilterRecord.getFilteredReadsNum();
			if (threadStopNum == lsFilter.size()) {
				//TODO
				fastqWrite.close();
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
		for (FastQfilterRecord fastQfilterRecord : lsFilter) {
			fastQfilterRecord.setParam(fastQfilterRecordParam);
		}
		
		if (fastqWrite == null) {
			String fastqNameOut = FileOperate.changeFileSuffix(fastQRead.getFileName(), "_filtered", null);
			fastqWrite = new FastQwrite(fastqNameOut);
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

