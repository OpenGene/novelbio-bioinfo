package com.novelbio.analysis.seq.chipseq;

import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.RegionInfo;

/**
 * 绘制tss、tes等图，要求输入bed文件，然后根据bed文件的结果来画图
 * @author zong0jie
 * @data 2016年11月9日
 */
public class TssPlot {
	private static final Logger logger = Logger.getLogger(TssPlot.class);
	
	/**
	 * 结果图片分割为1000份
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
	int splitNum = 1000;
	
	MapReads mapReads;
	EnumMapNormalizeType mapNormType = EnumMapNormalizeType.allreads;

	/** 绘制tss的具体信息，主要就是reads堆叠后的信息 */
	List<RegionInfo> lsRegions;
	
	
	
	String sampleName;
	
	public TssPlot() { }
	
	/** 样本名 */
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	
	/** 设定切割分数，默认为1000 
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
	public void setSplitNum(int splitNum) {
		this.splitNum = splitNum;
	}
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public MapReads getMapReads() {
		return mapReads;
	}
	/** 
	 * 设定本方法后<b>不需要</b>运行{@link #fillLsMapInfos()}<br>
	 * 用来做给定区域的图。mapinfo中设定坐标位点和value
	 * 这个和输入gene，2选1。谁先设定选谁
	 *  */
	public void setSiteRegion(List<RegionInfo> lsMapInfos) {
		this.lsRegions = RegionInfo.getCombLsMapInfoBigScore(lsMapInfos, 1000, true);
	}
  

}

