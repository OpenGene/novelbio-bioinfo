package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

import com.novelbio.base.multithread.txtreadcopewrite.MTRecordCope;
import com.novelbio.base.multithread.txtreadcopewrite.MTrecordCoper;

/** ʵ�����ǹ��˵��࣬���������������趨���˵Ĳ��� */
public class FastQRecordFilter extends MTrecordCoper<FastqRecordInfoFilter> {
	int phredOffset;
	int quality = FastQ.QUALITY_MIDIAN;
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
	//��Ҫ�ǿ���ȡ�Ƿ����
	boolean pairEnd = false;
	
	/**
	 * �趨ȫ�ֹ���ָ��
	 * 
	 * @param QUALITY
	 */
	private void setQualityFilter(int QUALITY) {
		if (QUALITY == FastQ.QUALITY_HIGM) {
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 3);
			mapFastQFilter.put(20, 7);
		} else if (QUALITY == FastQ.QUALITY_LOW) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 4);
			mapFastQFilter.put(13, 10);
			mapFastQFilter.put(20, 20);
		} else if (QUALITY == FastQ.QUALITY_MIDIAN
				|| QUALITY == FastQ.QUALITY_MIDIAN_PAIREND) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		} else if (QUALITY == FastQ.QUALITY_LOW_454) {
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
	///////////////////////////////////////////  ��������  ///////////////////////////////////////////////////////////////////////
	/** ������̶��� */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/** FastQfile.QUALITY_MIDIAN�ȣ�Ĭ���е� */
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
	/** Ĭ��Ϊture */
	public void setFilterParamAdaptorScanLeftStart(boolean adaptorScanLeftStart) {
		this.adaptorScanLeftStart = adaptorScanLeftStart;
	}
	/** Ĭ��Ϊture */
	public void setFilterParamAdaptorScanRightStart(boolean adaptorScanRightStart) {
		this.adaptorScanRightStart = adaptorScanRightStart;
	}
	/** Ĭ��Ϊfalse */
	public void setFilterParamTrimPolyA_right(boolean trimPolyA_right) {
		this.trimPolyA_right = trimPolyA_right;
	}
	/** Ĭ��false */
	public void setFilterParamTrimPolyT_left(boolean trimPolyT_left) {
		this.trimPolyT_left = trimPolyT_left;
	}
	/**Ĭ��true */
	public void setFilterParamTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}	
	/**Ĭ��false */
	public void setFilterParamAdaptorLowercase(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}

	/** ��һ��fastqFilterRecord�Ĳ������뱾���� */
	protected void setParam(FastQRecordFilter fastQfilterRecordParam) {
		readsLenMin = fastQfilterRecordParam.readsLenMin;
		quality = fastQfilterRecordParam.quality;
		adaptorLeft = fastQfilterRecordParam.adaptorLeft;
		adaptorRight = fastQfilterRecordParam.adaptorRight;
		adaptermaxMismach = fastQfilterRecordParam.adaptermaxMismach;
		adaptermaxConMismatch = fastQfilterRecordParam.adaptermaxConMismatch;
		proportionMisMathch = fastQfilterRecordParam.proportionMisMathch;
		adaptorScanLeftStart = fastQfilterRecordParam.adaptorScanLeftStart;
		adaptorScanRightStart = fastQfilterRecordParam.adaptorScanRightStart;
		trimPolyA_right = fastQfilterRecordParam.trimPolyA_right;
		trimNNN = fastQfilterRecordParam.trimNNN;
		trimPolyT_left = fastQfilterRecordParam.trimPolyT_left;
		adaptorLowercase = fastQfilterRecordParam.adaptorLowercase;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void setIsPairEnd(boolean isPairEnd) {
		this.pairEnd = isPairEnd;
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
	protected void copeBeforeRun() {
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
		setQualityFilter(quality);
	}

	/** �������� */
	protected void copeLsReads() throws InterruptedException {
		while (true) {
			if (isReadingFinished()) {
				break;
			}
			if (flagStop) {
				break;
			}
			suspendCheck();
			FastqRecordInfoRead fastQrecordInfo = (FastqRecordInfoRead) absQueue.poll();
			if (fastQrecordInfo == null) {
				continue;
			}
			if (!pairEnd) {
				copeFastQRecordSE(fastQrecordInfo.fastQRecord[0]);
			}
			else {
				copeFastQRecordPE(fastQrecordInfo.fastQRecord[0], fastQrecordInfo.fastQRecord[1]);
			}
		}
	}
	
	protected void copeFastQRecordSE(FastQRecord fastQRecord) {
		allReadsNum ++;
		
		if (fastQRecord == null) return;
		boolean filterSucess = filterFastQRecordSE(fastQRecord, mapNumLeft, mapNumRight);
		if (!filterSucess) return;
		
		filteredReadsNum ++;

		FastqRecordInfoFilter fastqRecordInfo = new FastqRecordInfoFilter(allReadsNum, fastQRecord);
		setRunInfo(fastqRecordInfo);
	}

	
	/** û��ͨ�����˾ͷ���null */
	private boolean filterFastQRecordSE(FastQRecord fastQRecord, int mapNumLeft, int mapNumRight) {
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

	protected void copeFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		allReadsNum ++;
		
		if (fastQRecord1 == null && fastQRecord2 == null) return;
		boolean filterSucess = filterFastQRecordPE(fastQRecord1, fastQRecord2, mapNumLeft, mapNumRight);
		if (!filterSucess) return;
		
		filteredReadsNum ++;
		
		FastqRecordInfoFilter fastqRecordInfo = new FastqRecordInfoFilter(allReadsNum, fastQRecord1);
		fastqRecordInfo.setFastQRecord2(fastQRecord2);
		setRunInfo(fastqRecordInfo);
	}
	
	/** û��ͨ�����˾ͷ���null */
	private boolean filterFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2, int mapNumLeft, int mapNumRight) {
		fastQRecord1.setFastqOffset(phredOffset);
		fastQRecord1.setTrimMinLen(readsLenMin);
		fastQRecord1.setMapFastqFilter(mapFastQFilter);
		boolean filterSucess1 = fastQRecord1.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, 
				mapNumRight, adaptermaxMismach, adaptermaxConMismatch, proportionMisMathch);
		
		fastQRecord2.setFastqOffset(phredOffset);
		fastQRecord2.setTrimMinLen(readsLenMin);
		fastQRecord2.setMapFastqFilter(mapFastQFilter);
		boolean filterSucess2 = fastQRecord2.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, 
				mapNumRight, adaptermaxMismach, adaptermaxConMismatch, proportionMisMathch);
		
		if (!filterSucess1 || ! filterSucess2) return false;
		if (trimPolyA_right && (!fastQRecord1.trimPolyAR(2) || !fastQRecord2.trimPolyAR(2)) ) {
			return false;
		}
		if (trimPolyT_left && (!fastQRecord1.trimPolyTL(2) || !fastQRecord2.trimPolyTL(2)) ) {
			return false;
		}
		if (trimNNN && (!fastQRecord1.trimNNN(2) || !fastQRecord2.trimNNN(2)) ) {
			return false;
		}
		if (adaptorLowercase && (!fastQRecord1.trimLowCase() || !fastQRecord2.trimLowCase()) ) {
			return false;
		}
		if (!fastQRecord1.QC() && !fastQRecord2.QC()) return false;
		return true;
	}
	
}
class FastqRecordInfoFilter implements MTRecordCope {
	long readsNum = 0;
	FastQRecord fastQRecord1;
	FastQRecord fastQRecord2;
	boolean singleEnd = true;
	public FastqRecordInfoFilter(long readsNum, FastQRecord fastQRecord) {
		this.readsNum = readsNum;
		this.fastQRecord1 = fastQRecord;
	}
	/** �ڶ��� */
	public void setFastQRecord2(FastQRecord fastQRecord2) {
		this.fastQRecord2 = fastQRecord2;
		singleEnd = false;
	}
}