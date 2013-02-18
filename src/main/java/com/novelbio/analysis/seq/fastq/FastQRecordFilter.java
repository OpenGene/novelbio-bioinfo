package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** 实际上是过滤的类，不过可以用其来设定过滤的参数
 * 过滤前要先设定{@link #setLsfFQrecordFilters()}}
 *  */
public class FastQRecordFilter {
	int phredOffset;
	int readsLenMin = 18;

	/** fastQ里面asc||码的指标与个数 */
	HashMap<Integer, Integer> mapFastQFilter;
	
	FQrecordFilterAdaptor fQrecordFilterAdaptor = new FQrecordFilterAdaptor();
	FQrecordFilterNNN fQrecordFilterNNN = new FQrecordFilterNNN();
	FQrecordFilterPloyAT fQrecordFilterPloyAT = new FQrecordFilterPloyAT();
	FQrecordFilterQC fQrecordFilterQC = new FQrecordFilterQC();
	FQrecordFilterLowcase fQrecordFilterLowcase = new FQrecordFilterLowcase();
	
	List<FQrecordFilter> lsFQrecordFilters;
	
	boolean isModifyQuality = false;
	
	/**
	 * 设定全局过滤指标
	 * @param QUALITY
	 */
	public void setQualityFilter(int QUALITY) {
		if (QUALITY == FastQ.QUALITY_NOTFILTER) {
			isModifyQuality = true;
			return;
		}
		mapFastQFilter = FastQ.getMapFastQFilter(QUALITY);
	}
	///////////////////////////////////////////  参数设置  ///////////////////////////////////////////////////////////////////////
	/** 序列最短多少 */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/** 如果有adapterL，则必须先设定该方法，否则会报错 */
	public void setFilterParamAdaptorLeft(String adaptorLeft) {
		fQrecordFilterAdaptor = new FQrecordFilterAdaptor();
		fQrecordFilterAdaptor.setSeqAdaptorL(adaptorLeft);
	}
	/** 如果有adapterR，则必须先设定该方法，否则会报错 */
	public void setFilterParamAdaptorRight(String adaptorRight) {
		fQrecordFilterAdaptor.setSeqAdaptorR(adaptorRight);
	}
	/** 最多错配，默认为4 */
	public void setFilterParamAdaptermaxMismach(int adaptermaxMismach) {
		fQrecordFilterAdaptor.setNumMM(adaptermaxMismach);
	}
	/** 最多连续错配，默认为2 */
	public void setFilterParamAdaptermaxConMismatch(int adaptermaxConMismatch) {
		fQrecordFilterAdaptor.setConNum(adaptermaxConMismatch);
	}
	/**最多错配比例，默认25 %，100为单位 */
	public void setFilterParamProportionMisMathch(int proportionMisMathch) {
		fQrecordFilterAdaptor.setPerMm(proportionMisMathch);
	}
	/** 默认为ture */
	public void setFilterParamAdaptorScanLeftStart(boolean adaptorScanLeftStart) {
		int mapNumLeft = 0;
		if (adaptorScanLeftStart) {
			mapNumLeft = 1;
		} else {
			mapNumLeft = -1;
		}
		fQrecordFilterAdaptor.setMapNumLeft(mapNumLeft);
	}
	/** 默认为ture */
	public void setFilterParamAdaptorScanRightStart(boolean adaptorScanRightStart) {
		int mapNumRight = 0;
		if (adaptorScanRightStart) {
			mapNumRight = 1;
		} else {
			mapNumRight = -1;
		}
		fQrecordFilterAdaptor.setMapNumRight(mapNumRight);
	}
	
	/** 默认为false */
	public void setFilterParamTrimPolyA_right(boolean trimPolyA_right) {
		fQrecordFilterPloyAT.setFilterA(trimPolyA_right);
	}
	/** 默认false */
	public void setFilterParamTrimPolyT_left(boolean trimPolyT_left) {
		fQrecordFilterPloyAT.setFilterT(trimPolyT_left);
	}
	
	/**默认true */
	public void setFilterParamTrimNNN(boolean trimNNN) {
		fQrecordFilterNNN.setTrimNNN(trimNNN);
	}	
	/**默认false */
	public void setFilterParamAdaptorLowercase(boolean adaptorLowercase) {
		fQrecordFilterLowcase.setFiterLowcase(adaptorLowercase);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void setPhredOffset(int phredOffset) {
		this.phredOffset = phredOffset;
	}
	
	/**
	 * 初始化过滤器
	 * 本方法内部确定过滤器的顺序 
	 */
	protected void fillLsfFQrecordFilters() {
		if (lsFQrecordFilters != null) {
			return;
		}
		List<FQrecordFilter> lsFQrecordFilterTmp = new ArrayList<FQrecordFilter>();
		lsFQrecordFilterTmp.add(fQrecordFilterAdaptor);
		lsFQrecordFilterTmp.add(fQrecordFilterPloyAT);
		lsFQrecordFilterTmp.add(fQrecordFilterNNN);
		lsFQrecordFilterTmp.add(fQrecordFilterLowcase);
		lsFQrecordFilterTmp.add(fQrecordFilterQC);
		
		fQrecordFilterQC.setMapFastQFilter(mapFastQFilter);
		
		lsFQrecordFilters = new ArrayList<FQrecordFilter>();
		for (FQrecordFilter fQrecordFilter : lsFQrecordFilterTmp) {
			if (fQrecordFilter.isUsing()) {
				fQrecordFilter.setFastqOffset(phredOffset);
				fQrecordFilter.setTrimMinLen(readsLenMin);
				lsFQrecordFilters.add(fQrecordFilter);
			}
		}
		return;
	}

	/** 没有通过过滤就返回false */
	public boolean filterFastQRecordSE(FastQRecord fastQRecord) {
		if (fastQRecord == null) {
			return false;
		}
		fastQRecord.setModifyQuality(isModifyQuality);
		boolean filtered = true;
		for (FQrecordFilter fQrecordFilter : lsFQrecordFilters) {
			if (!fQrecordFilter.filter(fastQRecord)) {
				filtered = false;
				break;
			}
		}
		
		return filtered;
	}
	/** 没有通过过滤就返回false */
	public boolean filterFastQRecordPE(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		if (fastQRecord1 == null || fastQRecord2 == null) {
			return false;
		}
		fastQRecord1.setModifyQuality(isModifyQuality);
		fastQRecord2.setModifyQuality(isModifyQuality);
		boolean filtered = true;
		for (FQrecordFilter fQrecordFilter : lsFQrecordFilters) {
			boolean filter1 = fQrecordFilter.filter(fastQRecord1);
			boolean filter2 = fQrecordFilter.filter(fastQRecord2);
			if (!filter1 || !filter2) {
				filtered = false;
				break;
			}
		}
		return filtered;
	}
}
