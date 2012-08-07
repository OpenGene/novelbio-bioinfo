package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

/** ʵ�����ǹ��˵��࣬���������������趨���˵Ĳ��� */
public class FastQfilterRecord extends FastQRecordCope<FastqRecordInfoFilter> {
	int phredOffset;
	int quality = FastQfile.QUALITY_MIDIAN;
	int readsLenMin;
	String adaptorLeft, adaptorRight;
	/** �����伸�� */
	int adaptermaxMismach = 4;
	/** ����������伸�� */
	int adaptermaxConMismatch = 2;
	int proportionMisMathch = 25;
	
	/** �Ƿ���Ҫɾ����Щ */
	boolean trimPolyA_right = false, trimPolyT_left = false, trimNNN = true;
	/** PGM�������У�Сд��������adaptor */
	boolean adaptorLowercase = false;
	
	boolean adaptorScanLeftStart = true, adaptorScanRightStart= true;
	int mapNumLeft = -1, mapNumRight = -1;
	
	int allReadsNum, filteredReadsNum;
	/** fastQ����asc||���ָ������� */
	HashMap<Integer, Integer> mapFastQFilter = new HashMap<Integer, Integer>();
	
	/**
	 * �趨ȫ�ֹ���ָ��
	 * 
	 * @param QUALITY
	 */
	private void setQualityFilter(int QUALITY) {
		if (QUALITY == FastQfile.QUALITY_HIGM) {
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 3);
			mapFastQFilter.put(20, 7);
		} else if (QUALITY == FastQfile.QUALITY_LOW) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 4);
			mapFastQFilter.put(13, 10);
			mapFastQFilter.put(20, 20);
		} else if (QUALITY == FastQfile.QUALITY_MIDIAN
				|| QUALITY == FastQfile.QUALITY_MIDIAN_PAIREND) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		} else if (QUALITY == FastQfile.QUALITY_LOW_454) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 6);
			mapFastQFilter.put(13, 15);
			mapFastQFilter.put(20, 50);
		} else {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		}
	}
	/** ������̶��� */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/** FastQfile.QUALITY_MIDIAN�� */
	public void setFilterParamQuality(int quality) {
		this.quality = quality;
	}
	public void setFilterParamAdaptorLeft(String adaptorLeft) {
		this.adaptorLeft = adaptorLeft;
	}
	public void setFilterParamAdaptorRight(String adaptorRight) {
		this.adaptorRight = adaptorRight;
	}
	/** �����䣬Ĭ��Ϊ4 */
	public void setFilterParamAdaptermaxMismach(int adaptermaxMismach) {
		this.adaptermaxMismach = adaptermaxMismach;
	}
	/** ����������䣬Ĭ��Ϊ2 */
	public void setFilterParamAdaptermaxConMismatch(int adaptermaxConMismatch) {
		this.adaptermaxConMismatch = adaptermaxConMismatch;
	}
	/**�����������Ĭ��25 % */
	public void setFilterParamProportionMisMathch(int proportionMisMathch) {
		this.proportionMisMathch = proportionMisMathch;
	}
	public void setAdaptorScanLeftStart(boolean adaptorScanLeftStart) {
		this.adaptorScanLeftStart = adaptorScanLeftStart;
	}
	public void setAdaptorScanRightStart(boolean adaptorScanRightStart) {
		this.adaptorScanRightStart = adaptorScanRightStart;
	}
	/** ��һ��fastqFilterRecord�Ĳ������뱾���� */
	protected void setParam(FastQfilterRecord fastQfilterRecordParam) {
		readsLenMin = fastQfilterRecordParam.readsLenMin;
		quality = fastQfilterRecordParam.quality;
		adaptorLeft = fastQfilterRecordParam.adaptorLeft;
		adaptorRight = fastQfilterRecordParam.adaptorRight;
		adaptermaxMismach = fastQfilterRecordParam.adaptermaxMismach;
		adaptermaxConMismatch = fastQfilterRecordParam.adaptermaxConMismatch;
		proportionMisMathch = fastQfilterRecordParam.proportionMisMathch;
		adaptorScanLeftStart = fastQfilterRecordParam.adaptorScanLeftStart;
		adaptorScanRightStart = fastQfilterRecordParam.adaptorScanRightStart;
	}
	protected void setPhredOffset(int phredOffset) {
		this.phredOffset = phredOffset;
	}
	protected int getAllReadsNum() {
		return allReadsNum;
	}
	protected int getFilteredReadsNum() {
		return filteredReadsNum;
	}
	@Override
	protected void copeInfo() {
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
		setQualityFilter(quality);
	}
	
	protected void copeFastQRecord(FastQRecord fastQRecord) {
		allReadsNum ++;
		
		if (fastQRecord == null) return;
		boolean filterSucess = filterFastQRecord(fastQRecord, mapNumLeft, mapNumRight);
		if (!filterSucess) return;
		
		filteredReadsNum ++;

		FastqRecordInfoFilter fastqRecordInfo = new FastqRecordInfoFilter(allReadsNum, fastQRecord);
		setRunInfo(fastqRecordInfo);
	}
		
	/** û��ͨ�����˾ͷ���null */
	private boolean filterFastQRecord(FastQRecord fastQRecord, int mapNumLeft, int mapNumRight) {
		fastQRecord.setFastqOffset(phredOffset);
		fastQRecord.setTrimMinLen(readsLenMin);
		fastQRecord.setMapFastqFilter(mapFastQFilter);
		boolean filterSucess = fastQRecord.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, 
				mapNumRight, adaptermaxMismach, adaptermaxConMismatch, proportionMisMathch);
		
		if (!filterSucess) return false;
		if (trimPolyA_right && !fastQRecord.trimPolyAR(2)) {
			return false;
		}
		if (trimPolyT_left && !fastQRecord.trimPolyTL(2)) {
			return false;
		}
		if (trimNNN && !fastQRecord.trimNNN(2)) {
			return false;
		}
		if (adaptorLowercase && !fastQRecord.trimLowCase()) {
			return false;
		}
		if (!fastQRecord.QC()) return false;
		return true;
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