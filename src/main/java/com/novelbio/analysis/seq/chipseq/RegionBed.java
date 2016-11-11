package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.RegionInfo;
import com.novelbio.analysis.seq.mapping.Align;

/**
 * 提取出来的区域信息，类似：
 * tp53\tchr1:2345-3456;chr1:4567-6789
 * 第一列是区段名
 * 第二列是需要提取并绘图的染色体坐标区域。
 * 其中 chr1:2345-3456 表示正向提取 chr1:3456-2345表示反向提取。
 * 因为基因是有方向的，这里主要是为了指定基因的方向。
 * @author zong0jie
 * @data 2016年11月9日
 */
public class RegionBed {
	/** 区段的名字 */
	String name;
	/** 默认区段是首尾相连 */
	boolean isPileup = false;
	
	/** 区段，注意里面分方向 */
	List<Align> lsAligns = new ArrayList<>();
	
	EnumMapNormalizeType mapNormType = EnumMapNormalizeType.allreads;
	
	/**
	 * 区段的总长度，
	 * 如果为0则不标准化。如果指定长度，表示将区段标准化到指定的长度。
	 * 譬如我们现在要看全体genebody的reads覆盖情况，因为genebody的长度是不等长的，
	 * 这时候我们就可以把genebody都标准化为1000bp
	 */
	int lengthNormal = 0;
	
	public RegionInfo getRegionInfo(MapReads mapReads) {
		for (Align align : lsAligns) {
			mapReads.getRangeInfo(align, 0);
		}
	}
	
	
	public static enum EnumTssPileUp {
		/** 直接连起来 */ 
		connect,
		/** 堆叠起来，并且把每个align标准化到相同的长度 */
		pileup_same_length,
		/** 简单堆叠起来，不等长的region就直接堆叠起来，不对长度进行标准化 */
		pileup,

	}
	
}
