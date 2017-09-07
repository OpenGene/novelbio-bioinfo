package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUpType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP.CpGCalculator;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP.EnumBspCpGCalculateType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP.EnumCpGmethyType;
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
	private static final String ALIGN_SEP = ";";
	/** 0-1000 是1001位 */
	public static final int LENGTH_XAXIS = 1001;
	/** 区段的名字 */
	String name;
	
	/** 区段，注意里面分方向 */
	List<Align> lsAligns = new ArrayList<>();

	/**
	 * 区段的总长度，
	 * 如果为0则不标准化。如果指定长度，表示将区段标准化到指定的长度。
	 * 譬如我们现在要看全体genebody的reads覆盖情况，因为genebody的长度是不等长的，
	 * 这时候我们就可以把genebody都标准化为1000bp
	 */
	int lengthNormal = LENGTH_XAXIS;
	
	/** 把tss合并起来的标准化方式 */
	EnumTssPileUpType normalType;
	/**
	 * @param regionBed 
	 * 提取出来的区域信息，类似：
	 * tp53\tchr1:2345-3456;chr1:4567-6789\tcc<br>
	 * 第一列是区段名<br>
	 * 第二列是需要提取并绘图的染色体坐标区域。<br>
	 * 其中 chr1:2345-3456 表示正向提取 chr1:3456-2345表示反向提取。<br>
	 * 因为基因是有方向的，这里主要是为了指定基因的方向。<br><br>
	 * @param enumTssPileUpType 标准化方法，有cc, cn, pnl, plnl, pc, pn 这6种，具体可参考{@link EnumTssPileUpType}<br><br>
	 * @param length 设定标准化的长度
	 */
	public RegionBed(String regionBed, EnumTssPileUpType enumTssPileUpType, int length) {
		String[] ss = regionBed.split("\t");
		this.name = ss[0];
		
		String[] aligns = ss[1].split(";");
		for (String alignStr : aligns) {
			Align align = new Align(alignStr);
			if (align.isCis5to3() == null) {
				align.setCis5to3(true);
			}
			lsAligns.add(align);
		}
		normalType = enumTssPileUpType;
		lengthNormal = length;
	}
	
	public RegionBed(String name) {
		this.name = name;
	}
	
	/**
	 * 这两个参数都不参与toString()方法
	 * @param enumTssPileUpType 堆叠的方式
	 * @param length 最后切分的长度
	 */
	public RegionBed(EnumTssPileUpType enumTssPileUpType, int length) {
		normalType = enumTssPileUpType;
		lengthNormal = length;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addAlign(Align align) {
		lsAligns.add(align);
	}
	public void setLsAligns(List<Align> lsAligns) {
		this.lsAligns = lsAligns;
	}
	
	/**
	 * 给定mapReads，返回里面装载好序列的文件
	 * @param mapReads
	 * @return
	 */
	public RegionValue getRegionInfo(MapReadsAbs mapReads, ReadsCoverageHandleFactory readsCoverageHandleFactory) {
		List<double[]> lsValues = new ArrayList<>();
		for (Align align : lsAligns) {
			double[] value = mapReads.getRangeInfo(align, 0);
			if (value == null) {
				throw new ExceptionNBCChIPAlignError("cannot get Region of " + toString());
			}
			lsValues.add(value);
		}
		ReadsCoverageHandle readsCovnerageHandle = readsCoverageHandleFactory.generateReadsCoverageHandle(lsValues, lengthNormal, normalType);
		double[] values = readsCovnerageHandle.normalizeToValues();
		RegionValue regionValue = new RegionValue();
		regionValue.setName(name);
		regionValue.setValues(values);
		return regionValue;
	}
	
	public String toString() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(name);
		List<String> lsAlignStr = new ArrayList<>();
		for (Align align : lsAligns) {
			lsAlignStr.add(align.toString());
		}
		lsResult.add(ArrayOperate.cmbString(lsAlignStr, ALIGN_SEP));
		return ArrayOperate.cmbString(lsResult, "\t");
	}
	
	public static enum EnumTssPileUpType {
		/** 直接连起来，然后标准化为指定长度 */ 
		connect_norm("cn"),
		/** 直接连起来，然后把比指定长度长的区域剪掉，如果比指定长度短，则补0 */ 
		connect_cut("cc"),
		/** 堆叠起来，并且把每个align标准化到相同的长度 */
		pileup_norm_to_length("pnl"),
		/** 堆叠起来，并且把长度长于指定长度的align标准化到相同的长度，短的不管，直接合并 */
		pileup_long_norm_to_length("plnl"),
		/** 简单堆叠起来，不等长的region就直接堆叠起来，最后截取为指定长度 */
		pileup_cut("pc"),
		/** 简单堆叠起来，不等长的region就直接堆叠起来，最后标准化为指定长度 */
		pileup_norm("pn");
		
		String symbol;
		
		private EnumTssPileUpType(String symbol) {
			this.symbol = symbol;
		}
		
		static Map<String, EnumTssPileUpType> mapSymbol2TssType = new HashMap<>();
		static {
			mapSymbol2TssType.put(connect_norm.toString(), connect_norm);
			mapSymbol2TssType.put(connect_norm.symbol, connect_norm);

			mapSymbol2TssType.put(connect_cut.toString(), connect_cut);
			mapSymbol2TssType.put(connect_cut.symbol, connect_cut);
			
			mapSymbol2TssType.put(pileup_norm_to_length.toString(), pileup_norm_to_length);
			mapSymbol2TssType.put(pileup_norm_to_length.symbol, pileup_norm_to_length);
			
			mapSymbol2TssType.put(pileup_long_norm_to_length.toString(), pileup_long_norm_to_length);
			mapSymbol2TssType.put(pileup_long_norm_to_length.symbol, pileup_long_norm_to_length);

			mapSymbol2TssType.put(pileup_cut.toString(), pileup_cut);
			mapSymbol2TssType.put(pileup_cut.symbol, pileup_cut);
			
			mapSymbol2TssType.put(pileup_norm.toString(), pileup_norm);
			mapSymbol2TssType.put(pileup_norm.symbol, pileup_norm);
		}
		
		public static EnumTssPileUpType getPileupType(String info) {
			EnumTssPileUpType tssPileUp = mapSymbol2TssType.get(info);

			if (tssPileUp == null) {
				List<String> lsTypes = new ArrayList<>();
				for (EnumTssPileUpType pileUpType : EnumTssPileUpType.values()) {
					lsTypes.add(pileUpType.symbol);
				}
				throw new ExceptionNbcParamError("cannot find pileup type " + info
						+ "\npileup type can only be " + ArrayOperate.cmbString(lsTypes, ", ")
						);
			}
			return tssPileUp;
		}

	}
	
	public static class ReadsCoverageHandleFactory {
		/** CpG的计算方式 */
		CpGCalculator cpGCalculator;
		
		public void setCpGCalculator(CpGCalculator cpGCalculator) {
			this.cpGCalculator = cpGCalculator;
		}
		
		public ReadsCoverageHandle generateReadsCoverageHandle(List<double[]> lsValues, int normalizedLength, EnumTssPileUpType pileUpType) {
			ReadsCoverageHandle readsCoverageHandle = null;
			if (cpGCalculator == null) {
				readsCoverageHandle = new ReadsCoverageHandle(lsValues, normalizedLength, pileUpType);
			} else {
				ReadsCoverageHandleBsp readsCoverageHandleBsp = new ReadsCoverageHandleBsp(lsValues, normalizedLength, pileUpType);
				readsCoverageHandleBsp.setCpGCalculator(cpGCalculator);
				readsCoverageHandle = readsCoverageHandleBsp;
			}
			return readsCoverageHandle;
		}
		
	}
	
}


class ReadsCoverageHandle {

	List<double[]> lsValues;
	
	int normalizedLength = 0;
	EnumTssPileUpType normalType;
	
	public ReadsCoverageHandle(List<double[]> lsValues, int normalizedLength, EnumTssPileUpType pileUpType) {
		this.lsValues = lsValues;
		this.normalizedLength = normalizedLength;
		this.normalType = pileUpType;
	}
	
	protected double[] normalizeAndHandleValue(double[] values) {
		if (normalizedLength <= 0 || normalizedLength == values.length) {
			return values;
		}
		return MathComput.mySpline(values, normalizedLength);
	}
	/** 仅仅对数据进行简单处理，在这里就是直接返回 */
	protected double[] handleValue(double[] values) {
		return values;
	}
	/** 把给定的value弄成等长，多的截断，少的补0 */
	protected double[] cutToValue(double[] values) {
		if (normalizedLength <= 0 || normalizedLength == values.length) {
			return values;
		}
		double[] result = new double[normalizedLength];
		for (int i = 0; i < result.length; i++) {
			result[i] = i < values.length ? result[i] = values[i] : 0;
		}
		return result;
	}
	
	protected boolean isLongerThanLength(double[] values) {
		return values.length > normalizedLength;
	}
	
	public double[] normalizeToValues() {
		if (normalizedLength < 0) {
			throw new ExceptionNbcParamError("while use param " + normalType + ", length normal cannot less than 0");
		}
		
		double[] result = null;
		if (normalType == EnumTssPileUpType.connect_norm) {
			result = connectNorm(lsValues);
		} else if (normalType == EnumTssPileUpType.connect_cut) {
			result = connectCut(lsValues);
		} else if (normalType == EnumTssPileUpType.pileup_cut) {
			result = pileupCut(lsValues);
		} else if (normalType == EnumTssPileUpType.pileup_norm) {
			result = pileupNorm(lsValues);
		} else if (normalType == EnumTssPileUpType.pileup_norm_to_length) {
			result = pileupNormLen(lsValues);
		} else if (normalType == EnumTssPileUpType.pileup_long_norm_to_length) {
			result = pileupLongNormLen(lsValues);
		} else {
			throw new ExceptionNbcParamError("cannot find normalType " + normalType);
		}
		return result;
	}
	
	/** 把所有的value连起来，然后标准化到指定的长度
	 * @param lsValues
	 * @return
	 */
	private double[] connectNorm(List<double[]> lsValues) {
		double[] valueTmp = connect(lsValues);
		return normalizeAndHandleValue(valueTmp);
	}
	
	/** 把所有的value连起来，然后cut到指定的长度
	 * @param lsValues
	 * @return
	 */
	private double[] connectCut(List<double[]> lsValues) {
		double[] valueTmp = connect(lsValues);
		valueTmp = handleValue(valueTmp);
		return cutToValue(valueTmp);
	}
	
	/** 把每个value标准化到指定的长度，然后堆叠起来
	 * @param lsValues
	 * @return
	 */
	private double[] pileupNormLen(List<double[]> lsValues) {
		List<double[]> lsNormValues = new ArrayList<>();
		for (double[] ds : lsValues) {
			lsNormValues.add(normalizeAndHandleValue(ds));
		}
		return ArrayOperate.getSumList(lsNormValues);
	}
	
	/** 把长度大于{@link #lengthNormal}的value标准化到指定的长度，小于该长度的value不标准化。
	 * 然后把所有value堆叠起来
	 * @param lsValues
	 * @param lengthNormal 必须大于0
	 * @return
	 */
	private double[] pileupLongNormLen(List<double[]> lsValues) {
		List<double[]> lsNormValues = new ArrayList<>();
		for (double[] ds : lsValues) {
			double[] valueTmp = isLongerThanLength(ds)? normalizeAndHandleValue(ds) : handleValue(ds);
			lsNormValues.add(valueTmp);
		}
		double[] valueTmp = ArrayOperate.getSumList(lsNormValues);
		return cutToValue(valueTmp);
	}
	
	/** 把value堆叠起来，并把最后结果标准化(加权平均)为指定长度
	 * @param lsValues
	 * @return
	 */
	private double[] pileupNorm(List<double[]> lsValues) {
		List<double[]> lsValuesNew = new ArrayList<>();
		for (double[] ds : lsValues) {
			lsValuesNew.add(handleValue(ds));
		}
		double[] valueTmp = ArrayOperate.getSumList(lsValuesNew);
		
		if (normalizedLength <= 0 || normalizedLength == valueTmp.length) {
			return valueTmp;
		}
		return MathComput.mySpline(valueTmp, normalizedLength);		
	}

	
	/** 把value堆叠起来，最后截短为指定长度
	 * @param lsValues
	 * @return
	 */
	private double[] pileupCut(List<double[]> lsValues) {
		List<double[]> lsValuesNew = new ArrayList<>();
		for (double[] ds : lsValues) {
			lsValuesNew.add(handleValue(ds));
		}
		double[] valueTmp = ArrayOperate.getSumList(lsValuesNew);
		return cutToValue(valueTmp);
	}

	
	/** 把给定的value连起来 */
	private static double[] connect(List<double[]> lsValues) {
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
		return result;
	}

}

class ReadsCoverageHandleBsp extends ReadsCoverageHandle {
	CpGCalculator cpGCalculator;
	
	public void setCpGCalculator(CpGCalculator cpGCalculator) {
		this.cpGCalculator = cpGCalculator;
	}
	
	protected boolean isLongerThanLength(double[] values) {
		return Math.round((double)values.length/cpGCalculator.getMinWindow()) > normalizedLength;
	}
	
	public ReadsCoverageHandleBsp(List<double[]> lsValues, int normalizedLength, EnumTssPileUpType pileUpType) {
		super(lsValues, normalizedLength, pileUpType);
	}
	
	protected double[] normalizeAndHandleValue(double[] values) {
		if (normalizedLength <= 0) normalizedLength = values.length;
		return cpGCalculator.calculateCpGInfo(values, normalizedLength); 
	}
	/** 不改变长度，仅仅将甲基化的比值等计算出来 */
	protected double[] handleValue(double[] values) {
		return cpGCalculator.calculateCpGInfoLength(values); 
	}
}