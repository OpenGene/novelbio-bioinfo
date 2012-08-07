package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;

import com.novelbio.base.RunGetInfo;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQfilter implements RunGetInfo<FastqRecordInfoFilter>{
	FastQRead fastQRead;
	ArrayList<FastQfilterRecord> lsFilter = new ArrayList<FastQfilterRecord>();
	int threadStopNum = 0;
	FastQwrite fastqOut;
	
	static DateTime dateTime = new DateTime();
	public static void main(String[] args) {
		
		dateTime.setStartTime();
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
	
	public void setInFastq(String fastqNameIn) {
		fastQRead = new FastQRead(fastqNameIn);
	}
	
	public void setOutFastq(String fastqNameOut) {
		fastqOut = new FastQwrite(fastqNameOut);
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
			fastqOut.writeFastQRecord(info.fastQRecord);
		}
	}
	
	@Override
	public void done() {
		synchronized (this) {
			threadStopNum++;
			if (threadStopNum == lsFilter.size()) {
				//TODO
				fastqOut.closeWrite();
				System.out.println("finished");
				System.out.println(dateTime.getEclipseTime());
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
		if (fastqOut == null) {
			String fastqNameOut = FileOperate.changeFileSuffix(fastQRead.getFileName(), "_filtered", null);
			fastqOut = new FastQwrite(fastqNameOut);
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

