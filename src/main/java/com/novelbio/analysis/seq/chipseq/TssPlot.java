package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUp;
import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 绘制tss、tes等图，要求输入bed文件，然后根据bed文件的结果来画图
 * @author zong0jie
 * @data 2016年11月9日
 */
public class TssPlot {
	private static final Logger logger = Logger.getLogger(TssPlot.class);
	/** 0-1000 是1001位 */
	private static final int LENGTH_XAXIS = 1001;

	
	/** x轴，长度必须和splitNum对应 */
	double[] xAxis;
	
	MapReads mapReads;
	
	EnumTssPileUp enumTssPileUp = EnumTssPileUp.pileup_to_same_length;
	
	/** 绘制tss的具体信息，主要就是reads堆叠后的信息 */
	List<RegionBed> lsRegions;
	
	
	public static void main(String[] args) {
		String bamFile = null;
		int extend = 150;
		int invNum = 10;
		boolean isUniqueReads = true;
		boolean isUniqueMapping = true;
		EnumMapNormalizeType normalType = EnumMapNormalizeType.allreads;
		String chrFai = "";
		String regionBedFile = "";
		
		String outTssMerge = "";
		String outTssSep = "";
		
		MapReads mapReads = new MapReads();
		mapReads.setAlignSeqReader(new SamFile(bamFile));
		mapReads.setFilter(isUniqueReads, extend);
		mapReads.setInvNum(invNum);
		mapReads.setisUniqueMapping(isUniqueMapping);
		mapReads.setNormalType(normalType);
		mapReads.setChrFai(chrFai);
		mapReads.setTagLength(300);
		mapReads.run();
		
		TssPlot tssPlot = new TssPlot();
		tssPlot.setMapReads(mapReads);
		tssPlot.setLsRegions(regionBedFile);
		tssPlot.writeToFileMerge(outTssMerge);
		tssPlot.writeToFileSep(outTssSep);
	}
	
	public void setxAxis(double[] xAxis) {
		this.xAxis = xAxis;
	}
	/** 设定读取的region信息 */
	public void setLsRegions(String regionBedFile) {
		lsRegions = new ArrayList<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(regionBedFile);
		boolean isXaxis = false;
		for (String content : txtRead.readlines()) {
			if (isXaxis) {
				setXaxis(content.trim().substring(1));
				continue;
			}
			
			if (content.trim().startsWith("#")) {
				if (content.trim().toLowerCase().startsWith("#xaxis")) {
					isXaxis = true;
				}
				continue;
			}
			lsRegions.add(new RegionBed(content));
		}
		txtRead.close();
	}
	
	private void setXaxis(String content) {
		String[] ss = content.trim().split(" ");
		xAxis = new double[ss.length];
		for (int i = 0; i < ss.length; i++) {
			xAxis[i] = Double.parseDouble(ss[1]);
		}
	}
	
	/** 设定读取的region信息 */
	public void setLsRegions(List<RegionBed> lsRegions) {
		this.lsRegions = lsRegions;
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

	/** 给定一系列的region区域，获取其覆盖的位点信息 */
	public List<RegionValue> getSiteRegion() {
		List<RegionValue> lsRegionValues = new ArrayList<>();
		for (RegionBed regionBed : lsRegions) {
			RegionValue regionValue = regionBed.getRegionInfo(mapReads);
			normRegionValue(regionValue);
			lsRegionValues.add(regionValue);
		}
		return lsRegionValues;
	}
	
	private void normRegionValue(RegionValue regionValue) {
		int splitNum = xAxis == null ? LENGTH_XAXIS : xAxis.length;
		
		if (enumTssPileUp == EnumTssPileUp.pileup 
				|| (enumTssPileUp == EnumTssPileUp.pileup_long_to_same_length && regionValue.getLen() < splitNum)
			) {
			regionValue.setLen(splitNum, false);
		} else if (enumTssPileUp == EnumTssPileUp.pileup_to_same_length) {
			regionValue.setLen(splitNum, true);
		}
	}
	
	/** 依次把所有的bed文件都写出来 */
	public void writeToFileSep(String outfile) {
		List<RegionValue> lsRegionValues = getSiteRegion();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outfile, true);
		txtWrite.writefileln("#XAXIS\t" + ArrayOperate.cmbString(getXaxis(), RegionValue.SEP_VALUE));
		for (RegionValue regionValue : lsRegionValues) {
			txtWrite.writefileln(regionValue.toString());
		}
		txtWrite.close();
	}
	
	/** 依次把所有的bed文件都写出来 */
	public void writeToFileMerge(String outfile) {
		List<RegionValue> lsRegionValues = getSiteRegion();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outfile, true);
		txtWrite.writefileln("#XAXIS\t");
		txtWrite.writefileln(ArrayOperate.cmbString(getXaxis(), RegionValue.SEP_VALUE));
		RegionValue regionValue = RegionValue.mergeRegions(lsRegionValues, enumTssPileUp, getXaxis().length);
		txtWrite.writefileln(regionValue.toString());
		txtWrite.close();
	}
	
	private double[] getXaxis() {
		if (xAxis == null) {
			xAxis = new double[LENGTH_XAXIS];
			for (int i = 0; i < xAxis.length; i++) {
				xAxis[i] = i;
			}
		}
		return xAxis;
	}
	
}

