package com.novelbio.analysis.seq.fastq;

import java.util.concurrent.ArrayBlockingQueue;

class FastQfilterRecord extends FastQRecordCope<FastqRecordInfoFilter> {
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
	int mapNumLeft = -1, mapNumRight = -1;
	
	int allReadsNum, filteredReadsNum;
	
	ArrayBlockingQueue<FastQRecord> lsFastQRecords;
	
	@Override
	protected void copeInfo() {
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
	}
	
	protected void copeFastQRecord(FastQRecord fastQRecord) {
		allReadsNum ++;
		
		if (fastQRecord == null) return;
		FastQRecord fastQRecordFilter = filterFastQRecord(fastQRecord, mapNumLeft, mapNumRight);
		if (fastQRecordFilter == null) return;
		
		filteredReadsNum ++;

		FastqRecordInfoFilter fastqRecordInfo = new FastqRecordInfoFilter(allReadsNum, fastQRecordFilter);
		setRunInfo(fastqRecordInfo);
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