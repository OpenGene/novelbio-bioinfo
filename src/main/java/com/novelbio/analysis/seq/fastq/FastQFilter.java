package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.base.ExceptionNullParam;

/** 实际上是过滤的类，不过可以用其来设定过滤的参数
 * 过滤前要先设定{@link #setLsfFQrecordFilters()}}
 *  */
public class FastQFilter {
	int phredOffset;
	int readsLenMin = 18;
	int readsLenMax = -1;
	boolean isFiltered = true;
	/** fastQ里面asc||码的指标与cutoff比例，意思 碱基质量小于 指定值 的数目 大于该比例 就会被过滤掉 */
	Map<Integer, Double> mapFastQFilter;
	
	FQrecordFilterAdaptor fQrecordFilterAdaptor;
	FQrecordFilterNNN fQrecordFilterNNN = new FQrecordFilterNNN();
	FQrecordFilterPloyAT fQrecordFilterPloyAT = new FQrecordFilterPloyAT();
	FQrecordFilterQC fQrecordFilterQC = new FQrecordFilterQC();
	FQrecordFilterLowcase fQrecordFilterLowcase = new FQrecordFilterLowcase();
	FQrecordFilterModifyQuality fQrecordFilterModifyQuality = new FQrecordFilterModifyQuality();
	
	List<FQrecordCopeInt> lsFQrecordFilters;
		
	/**
	 * 设定全局过滤指标
	 * @param QUALITY
	 */
	public void setQualityFilter(String QUALITY) {
		if (QUALITY.toLowerCase().contains(FastQ.FASTQ_QUALITY_CHANGE_TO_BEST.toLowerCase())) {
			fQrecordFilterModifyQuality.setModifyQuality(true);
			return;
		}
		mapFastQFilter = FastQ.getMapQuality2Num(QUALITY);
		if (mapFastQFilter == null) {
			throw new ExceptionNullParam("Error Quality Filter Value:" + QUALITY);
		}
	}
	///////////////////////////////////////////  参数设置  ///////////////////////////////////////////////////////////////////////
	/** 设定是否过滤，false表示不过滤直接跳过 */
	public void setIsFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
	/** 序列最短多少 */
	public void setFilterParamReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/** 序列最短多少 */
	public void setFilterParamReadsLenMax(int readsLenMax) {
		this.readsLenMax = readsLenMax;
	}
	/** 如果有adapterL，则必须先设定该方法，否则会报错 */
	public void setFilterParamAdaptorLeft(String adaptorLeft) {
		if (fQrecordFilterAdaptor == null && adaptorLeft != null && !adaptorLeft.equals("")) {
			fQrecordFilterAdaptor = new FQrecordFilterAdaptor();
		}
		if (adaptorLeft != null && !adaptorLeft.equals("")) {
			fQrecordFilterAdaptor.setSeqAdaptorL(adaptorLeft);
		}
	}
	/** 如果有adapterR，则必须先设定该方法，否则会报错 */
	public void setFilterParamAdaptorRight(String adaptorRight) {
		if (fQrecordFilterAdaptor == null && adaptorRight != null && !adaptorRight.equals("")) {
			fQrecordFilterAdaptor = new FQrecordFilterAdaptor();
		}
		if (adaptorRight != null && !adaptorRight.equals("")) {
			fQrecordFilterAdaptor.setSeqAdaptorR(adaptorRight);
		}
	}
	/** <b>务必在设定了{@link #setFilterParamAdaptorLeft(String)}后设定</b><br>
	 * 最多错配，默认为4
	 */
	public void setFilterParamAdaptermaxMismach(int adaptermaxMismach) {
		fQrecordFilterAdaptor.setNumMM(adaptermaxMismach);
	}
	/** <b>务必在设定了{@link #setFilterParamAdaptorLeft(String)}后设定</b><br>
	 *最多连续错配，默认为1 */
	public void setFilterParamAdaptermaxConMismatch(int adaptermaxConMismatch) {
		fQrecordFilterAdaptor.setConNum(adaptermaxConMismatch);
	}
	/**
	 * <b>务必在设定了{@link #setFilterParamAdaptorLeft(String)}后设定</b><br>
	 * 最多错配比例，默认25 %，100为单位 */
	public void setFilterParamProportionMisMathch(int proportionMisMathch) {
		fQrecordFilterAdaptor.setPerMm(proportionMisMathch);
	}
	/** 
	 * <b>务必在设定了{@link #setFilterParamAdaptorLeft(String)}后设定</b><br>
	 * 默认为ture */
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
	public void setFilterParamTrimNNN(boolean trimNNN, int qualityCutoff) {
		fQrecordFilterNNN.setTrimNNN(trimNNN, qualityCutoff);
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
	 * 初始化过滤器，在过滤之前再开始初始化
	 * 本方法内部确定过滤器的顺序 
	 */
	protected void fillLsfFQrecordFilters() {
		if (lsFQrecordFilters != null) {
			return;
		}
		List<FQrecordFilter> lsFQrecordFilterTmp = new ArrayList<FQrecordFilter>();
		if (fQrecordFilterAdaptor != null) {
			lsFQrecordFilterTmp.add(fQrecordFilterAdaptor);
		}
		lsFQrecordFilterTmp.add(fQrecordFilterPloyAT);
		lsFQrecordFilterTmp.add(fQrecordFilterNNN);
		lsFQrecordFilterTmp.add(fQrecordFilterLowcase);
		lsFQrecordFilterTmp.add(fQrecordFilterQC);
		
		fQrecordFilterQC.setMapFastQFilter(mapFastQFilter);
		
		lsFQrecordFilters = new ArrayList<FQrecordCopeInt>();
		for (FQrecordFilter fQrecordFilter : lsFQrecordFilterTmp) {
			if (fQrecordFilter.isUsing()) {
				fQrecordFilter.setFastqOffset(phredOffset);
				fQrecordFilter.setTrimMinLen(readsLenMin);
				fQrecordFilter.setTrimMaxLen(readsLenMax);
				lsFQrecordFilters.add(fQrecordFilter);
			}
		}
		return;
	}
	
	public List<FQrecordCopeInt> getLsFQfilter() {
		if (!isFiltered) {
			return new ArrayList<FQrecordCopeInt>();
		}
		fillLsfFQrecordFilters();
		return lsFQrecordFilters;
	}
	/** 是否过滤 */
	public boolean isFiltered() {
		return isFiltered;
	}
}
