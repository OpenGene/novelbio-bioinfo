package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUpType;
import com.novelbio.analysis.seq.chipseq.RegionBed.ReadsCoverageHandleFactory;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;

/**
 * 最终用于tss图的数值，都是y轴的值
 * @author zong0
 *
 */
public class RegionValue {
	/** 输出的value值中间的分隔符 */
	public static final String SEP_VALUE = " ";
	
	String name;
	double[] values;
	
	protected void setName(String name) {
		this.name = name;
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
	
	/** 返回values的长度 */
	public int getLen() {
		return values.length;
	}
	
	/** 把region设定为指定的长度，譬如value为1000位，但是我现在只需要500位。<br>
	 * @param isZoom 是否缩放。<br>
	 * true 把value通过加权平均进行缩放<br>
	 * false 直接把value延长(空位用0填充)，或缩短(减去右侧)<br>
	 */
	public void setLen(int length, boolean isZoom) {
		if (length <= 0 || length == values.length) return;
		
		double[] result = new double[length];
		if (isZoom) {
			result = MathComput.mySpline(values, length);
		} else {
			for (int i = 0; i < result.length; i++) {
				result[i] = i < values.length ? values[i] : 0;
			}
		}
		values = result;
	}
	
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(name); sBuilder.append("\t");
		sBuilder.append(ArrayOperate.cmbString(values, SEP_VALUE));
		return sBuilder.toString();
	}
	
	/**
	 * 给定一系列的value--譬如tss的reads堆叠值，把它们合并起来
	 * @param lsRegionValues 一系列reads堆叠值
	 * @param normalizeType reads堆叠的方式
	 * @param length 标准化的长度
	 * @return
	 */
	public static RegionValue mergeRegions(List<RegionValue> lsRegionValues, EnumTssPileUpType normalizeType, int length) {
		List<double[]> lsValues = new ArrayList<>();
		for (RegionValue regionValue : lsRegionValues) {
			lsValues.add(regionValue.getValues());
		}
		ReadsCoverageHandle readsCoverageHandle = new ReadsCoverageHandle(lsValues, length, normalizeType);
		double[] mergedValues = readsCoverageHandle.normalizeToValues();
		RegionValue regionValue = new RegionValue();
		regionValue.setName("merge");
		regionValue.setValues(mergedValues);
		return regionValue;
	}
	
}
