package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.novelbio.base.RunProcess;
class FastQfilterRecord extends RunProcess<FastqRecordInfoFilter> {
	int phredOffset;
	int readsLenMin;
	String adaptorLeft, adaptorRight;
	/** �����伸�� */
	int adaptermaxMismach;
	/** ����������伸�� */
	int adaptermaxConMismatch;
	/** �Ƿ���Ҫɾ����Щ */
	boolean trimPolyA_right = false, trimPolyT_left = false, trimNNN = true;
	/** PGM�������У�Сд��������adaptor */
	boolean adaptorLowercase = false;
	
	boolean adaptorScanLeftStart, adaptorScanRightStart;
	
	int allReadsNum, filteredReadsNum;
	
	ArrayBlockingQueue<FastQRecord> lsFastQRecords;
	
	//��Ҫ�ǿ���ȡ�Ƿ����
	FastQRead fastQRead;
	
	public void setLsFastQRecords(ArrayBlockingQueue<FastQRecord> lsFastQRecords) {
		this.lsFastQRecords = lsFastQRecords;
	}
	/** ��Ҫ�ǿ���ȡ�Ƿ���� */
	public void setFastQRead(FastQRead fastQRead) {
		this.fastQRead = fastQRead;
	}
	@Override
	protected void running() {
		try {
			filterReads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void filterReads() throws InterruptedException {
		int mapNumLeft = -1, mapNumRight = -1;
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
		

		while (true) {
			if (isReadingFinished()) {
				break;
			}
			if (flagStop) {
				break;
			}
			suspendCheck();
			
			FastQRecord fastQRecord = lsFastQRecords.poll();
			if (fastQRecord == null) {
				continue;
			}
			allReadsNum ++;
			if (copeFastQRecord(fastQRecord, mapNumLeft, mapNumRight)) {
				filteredReadsNum ++;
			}
		}
	}
	
	private boolean isReadingFinished() throws InterruptedException {
		while (lsFastQRecords.isEmpty()) {
			if (fastQRead.isFinished()) {
				return true;
			}
			Thread.sleep(10);
		}
		return false;
	}
	private boolean copeFastQRecord(FastQRecord fastQRecord, int mapNumLeft, int mapNumRight) {
		if (fastQRecord == null) {
			return false;
		}
		FastQRecord fastQRecordFilter = filterFastQRecord(fastQRecord, mapNumLeft, mapNumRight);
		if (fastQRecordFilter == null) {
			return false;
		}
		FastqRecordInfoFilter fastqRecordInfo = new FastqRecordInfoFilter(allReadsNum, fastQRecordFilter);
		setRunInfo(fastqRecordInfo);
		return true;
	}
	
		
	/** û��ͨ�����˾ͷ���null */
	private FastQRecord filterFastQRecord(FastQRecord fastQRecord, int mapNumLeft, int mapNumRight) {
		fastQRecord.setFastqOffset(phredOffset);
		fastQRecord.setTrimMinLen(readsLenMin);
		fastQRecord = fastQRecord.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, mapNumRight, adaptermaxMismach, adaptermaxConMismatch, 20);
		
		if (fastQRecord == null) return null;
		if (trimPolyA_right) {
			fastQRecord = fastQRecord.trimPolyAR(2);
			if (fastQRecord == null) return null;
		}
		if (trimPolyT_left) {
			fastQRecord = fastQRecord.trimPolyTL(2);
			if (fastQRecord == null) return null;
		}
		if (trimNNN) {
			fastQRecord = fastQRecord.trimNNN(2);
			if (fastQRecord == null) return null;
		}
		if (adaptorLowercase) {
			fastQRecord = fastQRecord.trimLowCase();
			if (fastQRecord == null) return null;
		}
		if (!fastQRecord.QC()) return null;
		
		return fastQRecord;
	}
}
class FastqRecordInfoFilter {
	long readsNum = 0;
	FastQRecord fastQRecord;
	public FastqRecordInfoFilter(long readsNum, FastQRecord fastQRecord) {
		this.readsNum = readsNum;
		this.fastQRecord = fastQRecord;
	}
}