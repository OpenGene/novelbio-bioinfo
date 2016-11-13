package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;

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
	/** 权重，用于画heatmap排序 */
	double score;
	
	/** 区段，注意里面分方向 */
	List<Align> lsAligns = new ArrayList<>();
	
	/** 把tss合并起来的标准化方式 */
	EnumTssPileUp normalType;
	/**
	 * 区段的总长度，
	 * 如果为0则不标准化。如果指定长度，表示将区段标准化到指定的长度。
	 * 譬如我们现在要看全体genebody的reads覆盖情况，因为genebody的长度是不等长的，
	 * 这时候我们就可以把genebody都标准化为1000bp
	 */
	int lengthNormal = 0;
	
	/**
	 * 给定mapReads，返回里面装载好序列的文件
	 * @param mapReads
	 * @return
	 */
	public RegionValue getRegionInfo(MapReads mapReads) {
		List<double[]> lsValues = new ArrayList<>();
		for (Align align : lsAligns) {
			double[] value = mapReads.getRangeInfo(align, 0);
			lsValues.add(value);
		}
		double[] values = EnumTssPileUp.normalizeValues(normalType, lsValues, lengthNormal);
		RegionValue regionValue = new RegionValue();
		regionValue.setName(name);
		regionValue.setScore(score);
		regionValue.setValues(values);
		return regionValue;
	}
	
	public static enum EnumTssPileUp {
		/** 直接连起来 */ 
		connect,
		/** 堆叠起来，并且把每个align标准化到相同的长度 */
		pileup_to_same_length,
		/** 堆叠起来，并且把长度长于指定长度的align标准化到相同的长度，短的不管，直接合并 */
		pileup_long_to_same_length,
		/** 简单堆叠起来，不等长的region就直接堆叠起来，不对长度进行标准化 */
		pileup;
		
		
		public static double[] normalizeValues(EnumTssPileUp normalType, List<double[]> lsValues, int lengthNormal) {
			double[] result = null;
			if (normalType == EnumTssPileUp.connect) {
				result = connect(lsValues, lengthNormal);
			} else if (normalType == EnumTssPileUp.pileup) {
				if (lengthNormal > 0) {
					throw new ExceptionNbcParamError("while use param " + normalType + ", length normal cannot bigger than 0");
				}
				result = pileup(lsValues);
			} else if (normalType == EnumTssPileUp.pileup_to_same_length) {
				if (lengthNormal <= 0) {
					throw new ExceptionNbcParamError("while use param " + normalType + ", length normal cannot less than 0");
				}
				result = pileupSameLen(lsValues, lengthNormal);
			} else if (normalType == EnumTssPileUp.pileup_long_to_same_length) {
				if (lengthNormal <= 0) {
					throw new ExceptionNbcParamError("while use param " + normalType + ", length normal cannot less than 0");
				}
				result = pileupLongSameLen(lsValues, lengthNormal);
			} else {
				throw new ExceptionNbcParamError("cannot find normalType " + normalType);
			}
			return result;
		}
		
		/** 把所有的value连起来，然后标准化到指定的长度
		 * @param lsValues
		 * @param lengthNormal 小于等于0就不标准化长度
		 * @return
		 */
		private static double[] connect(List<double[]> lsValues, int lengthNormal) {
			int size = 0;
			for (double[] ds : lsValues) {
				size += ds.length;
			}
			double[] result = new double[size];
			int i = 0;
			for (double[] ds : lsValues) {
				for (double d : ds) {
					result[i++] = d;
				}
			}
			return lengthNormal > 0? MathComput.mySpline(result, lengthNormal) : result;
		}
		/** 把每个value标准化到指定的长度，然后堆叠起来
		 * @param lsValues
		 * @param lengthNormal 小于等于0就不标准化长度
		 * @return
		 */
		private static double[] pileupSameLen(List<double[]> lsValues, int lengthNormal) {
			List<double[]> lsNormValues = new ArrayList<>();
			for (double[] ds : lsValues) {
				lsNormValues.add(MathComput.mySpline(ds, lengthNormal));
			}
			return ArrayOperate.getSumList(lsNormValues);
		}
		
		/** 把长度大于{@link #lengthNormal}的value标准化到指定的长度，小于该长度的value不标准化。
		 * 然后把所有value堆叠起来
		 * @param lsValues
		 * @param lengthNormal 必须大于0
		 * @return
		 */
		private static double[] pileupLongSameLen(List<double[]> lsValues, int lengthNormal) {
			List<double[]> lsNormValues = new ArrayList<>();
			for (double[] ds : lsValues) {
				double[] valueTmp = ds.length > lengthNormal? MathComput.mySpline(ds, lengthNormal) : ds;
				lsNormValues.add(valueTmp);
			}
			return ArrayOperate.getSumList(lsNormValues);
		}
		
		/** 把value堆叠起来
		 * @param lsValues
		 * @return
		 */
		private static double[] pileup(List<double[]> lsValues) {
			return ArrayOperate.getSumList(lsValues);
		}
		
	}
	
}
