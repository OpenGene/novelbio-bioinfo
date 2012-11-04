package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

import com.novelbio.base.multithread.txtreadcopewrite.MTRecordCope;
import com.novelbio.base.multithread.txtreadcopewrite.MTrecordCoper;

/** 实际上是过滤的类，不过可以用其来设定过滤的参数 */
public class FastQRecordFilter {
	int phredOffset;
	int readsLenMin = 18;
	String adaptorLeft, adaptorRight;
	/** 最多错配几个 */
	int adaptermaxMismach = 4;
	/** 最多连续错配几个 */
	int adaptermaxConMismatch = 2;
	int proportionMisMathch = 25;
	
	/** 是否需要删除这些 */
	boolean trimPolyA_right = false, trimPolyT_left = false, trimNNN = true;
	/** PGM的数据中，小写的序列是adaptor */
	boolean adaptorLowercase = false;

	int mapNumLeft = -1, mapNumRight = -1;
	/** fastQ里面asc||码的指标与个数 */
	HashMap<Integer, Integer> mapFastQFilter = new HashMap<Integer, Integer>();
	
	/**
	 * 设定全局过滤指标
	 * @param QUALITY
	 */
	public void setQualityFilter(int QUALITY) {
		if (QUALITY == FastQ.QUALITY_HIGM) {
			mapFastQFilter.put(10, 1);
			mapFastQFilter.put(13, 3);
			mapFastQFilter.put(20, 5);
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
			mapFastQFilter.put(20, 30);
		} else {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		}
	}
	///////////////////////////////////////////  参数设置  ///////////////////////////////////////////////////////////////////////
	/** 序列最短多少 */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	public void setFilterParamAdaptorLeft(String adaptorLeft) {
		this.adaptorLeft = adaptorLeft;
	}
	public void setFilterParamAdaptorRight(String adaptorRight) {
		this.adaptorRight = adaptorRight;
	}
	/** 最多错配，默认为4 */
	public void setFilterParamAdaptermaxMismach(int adaptermaxMismach) {
		this.adaptermaxMismach = adaptermaxMismach;
	}
	/** 最多连续错配，默认为2 */
	public void setFilterParamAdaptermaxConMismatch(int adaptermaxConMismatch) {
		this.adaptermaxConMismatch = adaptermaxConMismatch;
	}
	/**最多错配比例，默认25 % */
	public void setFilterParamProportionMisMathch(int proportionMisMathch) {
		this.proportionMisMathch = proportionMisMathch;
	}
	/** 默认为ture */
	public void setFilterParamAdaptorScanLeftStart(boolean adaptorScanLeftStart) {
		if (adaptorScanLeftStart) {
			mapNumLeft = 1;
		} else {
			mapNumLeft = -1;
		}
	}
	/** 默认为ture */
	public void setFilterParamAdaptorScanRightStart(boolean adaptorScanRightStart) {
		if (adaptorScanRightStart) {
			mapNumRight = 1;
		} else {
			mapNumRight = -1;
		}
	}
	/** 默认为false */
	public void setFilterParamTrimPolyA_right(boolean trimPolyA_right) {
		this.trimPolyA_right = trimPolyA_right;
	}
	/** 默认false */
	public void setFilterParamTrimPolyT_left(boolean trimPolyT_left) {
		this.trimPolyT_left = trimPolyT_left;
	}
	/**默认true */
	public void setFilterParamTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}	
	/**默认false */
	public void setFilterParamAdaptorLowercase(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void setPhredOffset(int phredOffset) {
		this.phredOffset = phredOffset;
	}
	
	/** 没有通过过滤就返回false */
	public boolean filterFastQRecordSE(FastQRecord fastQRecord) {
		if (fastQRecord == null) return false;
		
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
	
	/** 没有通过过滤就返回false */
	public boolean filterFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		if (fastQRecord1 == null && fastQRecord2 == null) return false;
		
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
	/** 第二端 */
	public void setFastQRecord2(FastQRecord fastQRecord2) {
		this.fastQRecord2 = fastQRecord2;
		singleEnd = false;
	}
}