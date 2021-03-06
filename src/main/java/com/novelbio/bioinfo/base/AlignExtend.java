package com.novelbio.bioinfo.base;

import com.novelbio.bioinfo.base.binarysearch.ListEle;
import com.novelbio.bioinfo.base.binarysearch.ListSearch;


/**
 * 在align的基础上添加了name等属性
 * 主要用于{@link ListSearch}
 * @author zong0jie
 * 重写了hashcode和equals方法，为 chrID+start+end
 */
public abstract class AlignExtend extends Align {	
	
	public abstract String getName();
	
	public abstract void setParent(ListEle<? extends AlignExtend> parent);

	
	protected int tss2UpGene = LOC_ORIGINAL;
	/** 本基因终点到下一个基因边界的距离 */
	protected int tes2DownGene = LOC_ORIGINAL;
	
	/** 本基因起点到上一个基因边界的距离  */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/** 本基因起点到上一个基因边界的距离 */
	public int getTss2UpGene() {
		return tss2UpGene;
	}
}
