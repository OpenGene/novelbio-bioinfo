package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.ArrayOperate;

public class VarInfo extends Align {
	/**
	 * 起点偏移多少bp，譬如起点落在了deletion中，那么alt中没有，就要去掉 
	 * 如:
	 * atg atac[g]cg ca[g]
	 * atg - -- -- --  cag
	 */
	int startBias = 0;
	/** 终点偏移多少bp，譬如终点落在了deletion中，同{@link #startBias} */ 
	int endBias = 0;
	/** 起点和终点中间包含的indel信息 */
	List<IndelForRef> lsIndelForRefs;
	
	
	public VarInfo() {}
	
	public VarInfo(String chrID, int start, int end) {
		super(chrID, start, end);
	}
	
	public VarInfo(String chrInfo) {
		super(chrInfo);
	}
	
	/**
	 * 起点偏移多少bp，譬如起点落在了deletion中，那么alt中没有，就要去掉 
	 * 如:
	 * atg atac[g]cg ca[g]
	 * atg - -- -- --  cag
	 */
	public void setStartBias(int startBias) {
		this.startBias = startBias;
	}
	/** 终点偏移多少bp，譬如终点落在了deletion中，同{@link #startBias} */ 
	public void setEndBias(int endBias) {
		this.endBias = endBias;
	}
	public int getStartBias() {
		return startBias;
	}
	public int getEndBias() {
		return endBias;
	}
	public void setLsIndelForRefs(List<IndelForRef> lsIndelForRefs) {
		this.lsIndelForRefs = lsIndelForRefs;
	}
	public List<IndelForRef> getLsIndelForRefs() {
		if (ArrayOperate.isEmpty(lsIndelForRefs)) {
			return new ArrayList<>();
		}
		return lsIndelForRefs;
	}
}
