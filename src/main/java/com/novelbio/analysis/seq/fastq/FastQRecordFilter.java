package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.base.multithread.txtreadcopewrite.MTRecordCope;
import com.novelbio.base.multithread.txtreadcopewrite.MTrecordCoper;

/** ʵ�����ǹ��˵��࣬���������������趨���˵Ĳ��� */
public class FastQRecordFilter {
	int phredOffset;
	int readsLenMin = 18;
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
	
	/** ��reads�����е�base quality��ֵ��С�����ֵ�ͻᱻ�е� */
	int trimEndFilterQuality = 14;
	
	int mapNumLeft = -1, mapNumRight = -1;
	/** fastQ����asc||���ָ������� */
	HashMap<Integer, Integer> mapFastQFilter = new HashMap<Integer, Integer>();
	
	/**
	 * �趨ȫ�ֹ���ָ��
	 * @param QUALITY
	 */
	public void setQualityFilter(int QUALITY) {
		mapFastQFilter = FastQ.getMapFastQFilter(QUALITY);
	}
	///////////////////////////////////////////  ��������  ///////////////////////////////////////////////////////////////////////
	/** ������̶��� */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
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
		if (adaptorScanLeftStart) {
			mapNumLeft = 1;
		} else {
			mapNumLeft = -1;
		}
	}
	/** Ĭ��Ϊture */
	public void setFilterParamAdaptorScanRightStart(boolean adaptorScanRightStart) {
		if (adaptorScanRightStart) {
			mapNumRight = 1;
		} else {
			mapNumRight = -1;
		}
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
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void setPhredOffset(int phredOffset) {
		this.phredOffset = phredOffset;
	}
	
	/** û��ͨ�����˾ͷ���false */
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
		if (trimNNN && !fastQRecord.trimNNN(2, trimEndFilterQuality)) {
			return false;
		}
		if (adaptorLowercase && !fastQRecord.trimLowCase()) {
			return false;
		}
		if (!fastQRecord.QC()) return false;
		return true;
	}
	
	/** û��ͨ�����˾ͷ���false */
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
		if (trimNNN && (!fastQRecord1.trimNNN(2, trimEndFilterQuality) || !fastQRecord2.trimNNN(2, trimEndFilterQuality)) ) {
			return false;
		}
		if (adaptorLowercase && (!fastQRecord1.trimLowCase() || !fastQRecord2.trimLowCase()) ) {
			return false;
		}
		if (!fastQRecord1.QC() && !fastQRecord2.QC()) return false;
		return true;
	}
	
}
