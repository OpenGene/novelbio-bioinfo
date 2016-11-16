package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUp;

/**
 * 最终用于tss图的数值，都是y轴的值
 * @author zong0
 *
 */
public class RegionValue {
	String name;
	double score;
	double[] values;
	
	protected void setName(String name) {
		this.name = name;
	}
	protected void setScore(double score) {
		this.score = score;
	}
	protected void setValues(double[] values) {
		this.values = values;
	}
	public double[] getValues() {
		return values;
	}
	public String getName() {
		return name;
	}
	public double getScore() {
		return score;
	}
	
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(name); sBuilder.append("\t");
		sBuilder.append(score+"\t");
		for (int i = 0; i < values.length; i++) {
			if (i == 0) {
				sBuilder.append(values[i]);
			} else {
				sBuilder.append(";" + values[i]);
			}
		}
		return sBuilder.toString();
	}
	
	/**
	 * 给定一系列的value--譬如tss的reads堆叠值，把它们合并起来
	 * @param lsRegionValues 一系列reads堆叠值
	 * @param normalizeType reads堆叠的方式
	 * @param length 标准化的长度
	 * @return
	 */
	public static RegionValue mergeRegions(List<RegionValue> lsRegionValues, EnumTssPileUp normalizeType, int length) {
		List<double[]> lsValues = new ArrayList<>();
		for (RegionValue regionValue : lsRegionValues) {
			lsValues.add(regionValue.getValues());
		}
		double[] mergedValues = EnumTssPileUp.normalizeValues(normalizeType, lsValues, length);
		RegionValue regionValue = new RegionValue();
		regionValue.setName("merge");
		regionValue.setScore(0);
		regionValue.setValues(mergedValues);
		return regionValue;
	}
	
}
