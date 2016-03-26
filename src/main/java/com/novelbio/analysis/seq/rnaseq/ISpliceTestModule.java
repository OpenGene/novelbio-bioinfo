package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.stat.inference.ChiSquareTestImpl;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.inference.TestUtils;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;

/** 差异可变剪接的pvalue计算模块 */
public interface ISpliceTestModule {
	
	/** 设定输入值
	 * @param mapCond_Group2ReadsNum 样本--分组--测序量，用来做标准化的
	 * @param condTreat treat样本的condition名字
	 * @param mapTreat2LsValue treat位点--对应的reads数
	 * @param condCtrl treat样本的condition，名字
	 * @param mapCtrl2LsValue control位点--对应的reads数
	 */
	public void setLsRepeat2Value(Map<String, Map<String, double[]>> mapCond_Group2ReadsNum, String condTreat,
			ArrayListMultimap<String, Double> mapTreat2LsValue, String condCtrl, ArrayListMultimap<String, Double> mapCtrl2LsValue);
	
	public double calculatePvalue();
	
	/** 默认开启标准化 */
	public void setMakeSmallValueBigger(boolean makeSmallValueBigger, int numLessNeedBig, double fold);
	
	/**
	 * 设定junction数量，小于该数量的不会进行分析
	 * @param juncAllReadsNum 所有样本的junction数量必须大于该值，否则不进行计算，默认25
	 * @param juncSampleReadsNum 单个样本的junction数量必须大于该值，否则不进行计算，默认10
	 */
	public void setJuncReadsNum(int juncAllReadsNum, int juncSampleReadsNum);
	
	/** 目前设置为：exp设定为50，junction设定为200 */
	public void setNormalizedNum(int normalizedNum);
	
	/**
	 * @param isInt 返回结果是否为int型，junction reads就是int型，表达量就是double型
	 * @return
	 */
	public String getCondtionTreat(boolean isInt);
	/**
	 * @param isInt 返回结果是否为int型，junction reads就是int型，表达量就是double型
	 * @return
	 */
	public String getCondtionCtrl(boolean isInt);
	
	/** 返回该位点的reads情况，譬如SE类型，则skip有25条 exon的有30条，即为 25，30 */
	public int[] getReadsInfo();
	
	public String getSiteInfo();
	
	double getSpliceIndex();
	
	public static class SpliceTestFactory {
		/**
		 * 是否将n次重复的reads合并为一个bam文件，然后进行分析
		 * @param combine true：合并，false：考虑重复
		 * @return
		 */
		public static ISpliceTestModule createSpliceModule(boolean combine) {
			ISpliceTestModule iSpliceTestModule = null;
			if (combine) {
				iSpliceTestModule = new SpliceTestCombine();
			} else {
				iSpliceTestModule = new SpliceTestRepeat();
			}
			return iSpliceTestModule;
		}
	
	}
}

class SpliceTestRepeat implements ISpliceTestModule {
	boolean makeSmallValueBigger = true;

	/** 将reads的数量扩大2倍，这样可以获得更多的差异 */
	double fold = 2;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	
	/** 如果count数超过该值，就标准化 */
	int normalizedNum = 200;
	
	int numLessNeedBig = 80;
	
	/** 本组比较中最大测序量的reads数 */
	long maxReads = 0;
	/** 实验组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsTreat2LsValue;
	ArrayListMultimap<String, Double> mapTreat2LsValue;
	
	/** 对照组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsCtrl2LsValue;
	ArrayListMultimap<String, Double> mapCtrl2LsValue;
	
	List<int[]> lsTreatValue = new ArrayList<>();
	List<int[]> lsCtrlValue = new ArrayList<>();
	
	@Override
	public void setMakeSmallValueBigger(boolean makeSmallValueBigger, int numLessNeedBig, double fold) {
		this.makeSmallValueBigger = makeSmallValueBigger;
		this.numLessNeedBig = numLessNeedBig;
		this.fold = fold;
	}
	
	/** 设定junction数量，小于该数量的不会进行分析
	 * 
	 * @param juncAllReadsNum 所有样本的junction数量必须大于该值，否则不进行计算，默认25
	 * @param juncSampleReadsNum 单个样本的junction数量必须大于该值，否则不进行计算，默认10
	 */
	public void setJuncReadsNum(int juncAllReadsNum, int juncSampleReadsNum) {
	    this.juncAllReadsNum = juncAllReadsNum;
	    this.juncSampleReadsNum = juncSampleReadsNum;
    }
	
	@Override
	public void setNormalizedNum(int normalizedNum) {
		this.normalizedNum = normalizedNum;
	}
	
	public void setLsRepeat2Value(Map<String, Map<String, double[]>> mapCond_Group2ReadsNum, String condTreat,
			ArrayListMultimap<String, Double> mapTreat2LsValue, String condCtrl, ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		this.mapTreat2LsValue = mapTreat2LsValue;
		this.mapCtrl2LsValue = mapCtrl2LsValue;
		//倒序排列
		TreeSet<Long> setReadsNum = new TreeSet<>(new Comparator<Long>() {
			public int compare(Long o1, Long o2) {
				return -o1.compareTo(o2);
			}
		});
		for (double[] readsNum : mapCond_Group2ReadsNum.get(condTreat).values()) {
			setReadsNum.add((long) readsNum[0]);
		}
		for (double[] readsNum : mapCond_Group2ReadsNum.get(condCtrl).values()) {
			setReadsNum.add((long) readsNum[0]);
		}
		maxReads = setReadsNum.iterator().next();
		
		lsTreat2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condTreat), mapTreat2LsValue, getFold(mapTreat2LsValue));
		lsCtrl2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condCtrl), mapCtrl2LsValue, getFold(mapCtrl2LsValue));
		
		if (this.lsTreat2LsValue.size() < this.lsCtrl2LsValue.size()) {
			this.lsCtrl2LsValue = balanceUnEqualPair(lsTreat2LsValue.size(), lsCtrl2LsValue);
		} else if (this.lsTreat2LsValue.size() > this.lsCtrl2LsValue.size()) {
			this.lsTreat2LsValue = balanceUnEqualPair(lsCtrl2LsValue.size(), lsTreat2LsValue);
		}
	}
	
	private double getFold(ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		if (!makeSmallValueBigger) return 1;
		
		double valueAll = 0;
		for (Double value : mapCtrl2LsValue.values()) {
			valueAll += value;
		}
		if (valueAll < numLessNeedBig) {
			return fold;
		} else {
			return 1;
		}
	}
	
	/**
	 * @param mapGroup2Value
	 * @param mapTreat2LsValue
	 * @param fold 扩大倍数
	 * @return
	 */
	private List<List<Double>> normalizeLsDouble(Map<String, double[]> mapGroup2Value, 
			ArrayListMultimap<String, Double> mapTreat2LsValue, double fold) {
		List<List<Double>> lslsValue = new ArrayList<>();
		for (String group : mapTreat2LsValue.keySet()) {
			List<Double> lsDouble = mapTreat2LsValue.get(group);
			List<Double> lsDoubleNormal = new ArrayList<>();
			for (Double value : lsDouble) {
				lsDoubleNormal.add(value * fold * maxReads/mapGroup2Value.get(group)[0]);
			}
			lslsValue.add(lsDoubleNormal);
		}
		return lslsValue;
	}
	
	/** 如果实验组和对照组的重复不是一样多，那么平衡一下 */
	private List<List<Double>> balanceUnEqualPair(int lessSize, List<List<Double>> lslsMore) {
		List<List<Double>> lsRetain = new ArrayList<>();
		for (int i = lessSize; i < lslsMore.size(); i++) {
			lsRetain.add(lslsMore.get(i));
		}
		double[] combValue = combReadsNumPerRepeat(lsRetain);
		int combNum = lslsMore.size() - lessSize;//合并的list的个数
		double property = ((double)lslsMore.size() - lessSize)/lslsMore.size();
		List<List<Double>> lslsMoreNew = new ArrayList<>();
		for (int i = 0; i < lessSize; i++) {
			List<Double> lsValues = lslsMore.get(i);
			List<Double> lsValuesNew = new ArrayList<>();
			for (int j = 0; j < combValue.length; j++) {
				lsValuesNew.add(lsValues.get(j) * (1-property) + combValue[j] * property/combNum);
			}
			lslsMoreNew.add(lsValuesNew);
		}
		return lslsMoreNew;
	}

	private double[] combReadsNumPerRepeat(List<List<Double>> lsExp) {
		double[] result = new double[lsExp.get(0).size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (List<Double> ls : lsExp) {
				result[i] += ls.get(i);
			}
		}
		return result;
	}
	
	
	public double calculatePvalue() {
		int[] cond1 = combReadsNumInt(mapTreat2LsValue);
		int[] cond2 = combReadsNumInt(mapCtrl2LsValue);
		if (!filter(cond1, cond2, juncAllReadsNum, juncSampleReadsNum)) {
			lsTreatValue.add(cond1);
			lsCtrlValue.add(cond2);
			return 1.0;
		}

		double chiSquareValue = 0;
		for (int i = 0; i < lsTreat2LsValue.size(); i++) {
			List<Double> lsTreat_OneRepeat = lsTreat2LsValue.get(i);
			List<Double> lsCtrl_OneRepeat = lsCtrl2LsValue.get(i);
			int[] treatOne = getIntValue(lsTreat_OneRepeat);
			int[] ctrlOne = getIntValue(lsCtrl_OneRepeat);

			normalizeToLowValue(treatOne, normalizedNum);
			normalizeToLowValue(ctrlOne, normalizedNum);
			
			lsTreatValue.add(treatOne);
			lsCtrlValue.add(ctrlOne);
			chiSquareValue += chiSquareDataSetsComparison(treatOne, ctrlOne);
		}
		double df = (lsTreat2LsValue.size()) * (lsTreat2LsValue.get(0).size() ) - 1;
		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
		return 1 - chiSquaredDistribution.cumulativeProbability(chiSquareValue);
	}
	
	private int[] getIntValue(List<Double> lsDouble) {
		int[] result = new int[lsDouble.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsDouble.get(i).intValue();
		}
		return result;
	}
	
	/** 返回该位点的reads情况 */
	public int[] getReadsInfo() {
		double[] a2b = new double[2];
		for (String ctrl : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		for (String treat : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		int[] result = new int[]{(int) a2b[0], (int) a2b[1]};
		return result;
	}
	
	/** 返回该位点的reads情况 */
	public double getSpliceIndex() {
		double[] a2b1 = new double[2];
		double[] a2b2 = new double[2];
		for (String ctrl : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b1[i] += lsValue.get(i);
			}
		}
		for (String treat : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b2[i] += lsValue.get(i);
			}
		}
		if ((a2b1[0] <= 1 && a2b2[0] <= 1) ||(a2b1[1] <= 1 && a2b2[1] <= 1)) {
			return 1;
		}
		
		a2b1[0] += 1; a2b1[1] += 1; a2b2[0] +=1; a2b2[1] += 1;
		return Math.log10(a2b1[0]/a2b1[1]/(a2b2[0]/a2b2[1])) / Math.log10(2);
	}
	
	/** 
	 * 如果count数量太大，就将其标准化至一个比较低的值
	 * @param normalizedValue 大于该值就开始修正
	 */
	protected static void normalizeToLowValue(int[] condition, int normalizedValue) {
		int meanValue = (int) MathComput.mean(condition);
		if (meanValue < normalizedValue) {
			return;
		}
		for (int i = 0; i < condition.length; i++) {
			condition[i] = (int) ((double)condition[i]/meanValue * normalizedValue);
		}
	}
	
	protected static double chiSquareTestDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i] + 1;
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i] + 1;
		}
		try {
			return TestUtils.chiSquareTestDataSetsComparison(cond1Long, cond2Long);
		} catch (Exception e) {
			return 1.0;
		}
	}
	protected static double chiSquareDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i] + 1;
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i] + 1;
		}
		try {
			return TestUtils.chiSquareDataSetsComparison(cond1Long, cond2Long);
		} catch (Exception e) {
			return 1.0;
		}
	}
	
	/** 返回整理好的比较结果展示 */
	public String getCondtionTreat(boolean isInt) {
		return isInt? getConditionInt(mapTreat2LsValue) : getCondition(mapTreat2LsValue);
	}
	/** 返回整理好的比较结果展示 */
	public String getCondtionCtrl(boolean isInt) {
		return isInt? getConditionInt(mapCtrl2LsValue) : getCondition(mapCtrl2LsValue);
	}
	
	protected static String getConditionInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		if (mapGroup2LsValue == null || mapGroup2LsValue.size() == 0) {
			return "";
		}
		int[] junction = combReadsNumInt(mapGroup2LsValue);
		String condition = junction[0]+ "";
		for (int i = 1; i < junction.length; i++) {
			condition = condition + "::" + junction[i];
		}
		return condition;
	}
	
	protected static String getCondition(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		if (mapGroup2LsValue == null || mapGroup2LsValue.size() == 0) {
			return "";
		}
		double[] junction = combReadsNumDouble(mapGroup2LsValue);
		String condition = junction[0]+ "";
		for (int i = 1; i < junction.length; i++) {
			condition = condition + "::" + junction[i];
		}
		return condition;
	}
	
	private static int[] avgReadsNumInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		int[] result = combReadsNumInt(mapGroup2LsValue);
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/mapGroup2LsValue.size();
		}
		return result;
	}
	private static double[] avgReadsNumDouble(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		double[] result = combReadsNumDouble(mapGroup2LsValue);
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/mapGroup2LsValue.size();
		}
		return result;
	}
	
	
	private static int[] combReadsNumInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		int[] result = null;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);
			if (result == null) {
				result = new int[lsValues.size()];
			}
			for (int i = 0; i < result.length; i++) {
				result[i] += lsValues.get(i);
			}
		}
		return result;
	}
	private static double[] combReadsNumDouble(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		double[] result = null;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);
			if (result == null) {
				result = new double[lsValues.size()];
			}
			for (int i = 0; i < result.length; i++) {
				result[i] += lsValues.get(i);
			}
		}
		return result;
	}

	
	/**
	 *  某些情况不适合做分析，就过滤掉<br>
	 * 譬如：遇到类似 0:5 0:50<br>
	 * 和<br>
	 * 2：3：50<br>  4：2：50<br>
	 * 以及总reads过少的情况，就要删除不进行分析<br>
	 * 
	 * @param cond1
	 * @param cond2
	 * @param juncAllReadsSumMin
	 * @param juncReadsSampleMin
	 * @return
	 */
	protected static boolean filter(int[] cond1, int[] cond2, int juncAllReadsSumMin, int juncReadsSampleMin) {
		//遇到类似 0:5 0:50
		//2：3：50  4：2：50
		//等就要删除了
		int allReadsNum = MathComput.sum(cond1) + MathComput.sum(cond2);
		
		if (cond1.length <= 1 || cond2.length <= 1) {
			return false;
		}
		
		//判定基因表达差异太大也会过滤掉可变剪接
//		int readsNumLess = 0;
//		for (int i = 0; i < cond1.length; i++) {
//			if (cond1[i] <= allReadsNum/20 && cond2[i] <= allReadsNum/20) {
//				readsNumLess++;
//			}
//		}
//		if (cond1.length - readsNumLess <= 1) {
//			return false;
//		}
		
		if (MathComput.sum(cond1) < juncReadsSampleMin || MathComput.sum(cond2) < juncReadsSampleMin) {
			return false;
        }
		//总reads数太少也过滤
		if (allReadsNum < juncAllReadsSumMin) {
			return false;
		}
		return true;
	}

	@Override
	public String getSiteInfo() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int[] list : lsCtrlValue) {
			for (int double1 : list) {
				stringBuilder.append(double1 + ":");
			}
			stringBuilder.append("|");
		}
		stringBuilder.append("@");
		for (int[] list : lsTreatValue) {
			for (int double1 : list) {
				stringBuilder.append(double1 + ":");
			}
			stringBuilder.append("|");
		}
		return stringBuilder.toString();
	}

}

class SpliceTestRepeatNew implements ISpliceTestModule {
	boolean makeSmallValueBigger = true;

	/** 将reads的数量扩大2倍，这样可以获得更多的差异 */
	double fold = 2;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	
	/** 如果count数超过该值，就标准化 */
	int normalizedNum = 200;
	
	int numLessNeedBig = 80;
	
	/** 本组比较中最大测序量的reads数 */
	long maxReads = 0;
	/** 实验组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsTreat2LsValue;
	ArrayListMultimap<String, Double> mapTreat2LsValue;
	
	/** 对照组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsCtrl2LsValue;
	ArrayListMultimap<String, Double> mapCtrl2LsValue;
	
	List<int[]> lsTreatValue = new ArrayList<>();
	List<int[]> lsCtrlValue = new ArrayList<>();
	
	@Override
	public void setMakeSmallValueBigger(boolean makeSmallValueBigger, int numLessNeedBig, double fold) {
		this.makeSmallValueBigger = makeSmallValueBigger;
		this.numLessNeedBig = numLessNeedBig;
		this.fold = fold;
	}
	
	/** 设定junction数量，小于该数量的不会进行分析
	 * 
	 * @param juncAllReadsNum 所有样本的junction数量必须大于该值，否则不进行计算，默认25
	 * @param juncSampleReadsNum 单个样本的junction数量必须大于该值，否则不进行计算，默认10
	 */
	public void setJuncReadsNum(int juncAllReadsNum, int juncSampleReadsNum) {
	    this.juncAllReadsNum = juncAllReadsNum;
	    this.juncSampleReadsNum = juncSampleReadsNum;
    }
	
	@Override
	public void setNormalizedNum(int normalizedNum) {
		this.normalizedNum = normalizedNum;
	}
	
	public void setLsRepeat2Value(Map<String, Map<String, double[]>> mapCond_Group2ReadsNum, String condTreat,
			ArrayListMultimap<String, Double> mapTreat2LsValue, String condCtrl, ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		this.mapTreat2LsValue = mapTreat2LsValue;
		this.mapCtrl2LsValue = mapCtrl2LsValue;
		//倒序排列
		TreeSet<Long> setReadsNum = new TreeSet<>(new Comparator<Long>() {
			public int compare(Long o1, Long o2) {
				return -o1.compareTo(o2);
			}
		});
		for (double[] readsNum : mapCond_Group2ReadsNum.get(condTreat).values()) {
			setReadsNum.add((long) readsNum[0]);
		}
		for (double[] readsNum : mapCond_Group2ReadsNum.get(condCtrl).values()) {
			setReadsNum.add((long) readsNum[0]);
		}
		maxReads = setReadsNum.iterator().next();
		
		lsTreat2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condTreat), mapTreat2LsValue, getFold(mapTreat2LsValue));
		lsCtrl2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condCtrl), mapCtrl2LsValue, getFold(mapCtrl2LsValue));
		
		if (this.lsTreat2LsValue.size() < this.lsCtrl2LsValue.size()) {
			this.lsCtrl2LsValue = balanceUnEqualPair(lsTreat2LsValue.size(), lsCtrl2LsValue);
		} else if (this.lsTreat2LsValue.size() > this.lsCtrl2LsValue.size()) {
			this.lsTreat2LsValue = balanceUnEqualPair(lsCtrl2LsValue.size(), lsTreat2LsValue);
		}
	}
	
	private double getFold(ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		if (!makeSmallValueBigger) return 1;
		
		double valueAll = 0;
		for (Double value : mapCtrl2LsValue.values()) {
			valueAll += value;
		}
		if (valueAll < numLessNeedBig) {
			return fold;
		} else {
			return 1;
		}
	}
	
	/**
	 * @param mapGroup2Value
	 * @param mapTreat2LsValue
	 * @param fold 扩大倍数
	 * @return
	 */
	private List<List<Double>> normalizeLsDouble(Map<String, double[]> mapGroup2Value, 
			ArrayListMultimap<String, Double> mapTreat2LsValue, double fold) {
		List<List<Double>> lslsValue = new ArrayList<>();
		for (String group : mapTreat2LsValue.keySet()) {
			List<Double> lsDouble = mapTreat2LsValue.get(group);
			List<Double> lsDoubleNormal = new ArrayList<>();
			for (Double value : lsDouble) {
				lsDoubleNormal.add(value * fold * maxReads/mapGroup2Value.get(group)[0]);
			}
			lslsValue.add(lsDoubleNormal);
		}
		return lslsValue;
	}
	
	/** 如果实验组和对照组的重复不是一样多，那么平衡一下 */
	private List<List<Double>> balanceUnEqualPair(int lessSize, List<List<Double>> lslsMore) {
		List<List<Double>> lsRetain = new ArrayList<>();
		for (int i = lessSize; i < lslsMore.size(); i++) {
			lsRetain.add(lslsMore.get(i));
		}
		double[] combValue = combReadsNumPerRepeat(lsRetain);
		int combNum = lslsMore.size() - lessSize;//合并的list的个数
		double property = ((double)lslsMore.size() - lessSize)/lslsMore.size();
		List<List<Double>> lslsMoreNew = new ArrayList<>();
		for (int i = 0; i < lessSize; i++) {
			List<Double> lsValues = lslsMore.get(i);
			List<Double> lsValuesNew = new ArrayList<>();
			for (int j = 0; j < combValue.length; j++) {
				lsValuesNew.add(lsValues.get(j) * (1-property) + combValue[j] * property/combNum);
			}
			lslsMoreNew.add(lsValuesNew);
		}
		return lslsMoreNew;
	}

	private double[] combReadsNumPerRepeat(List<List<Double>> lsExp) {
		double[] result = new double[lsExp.get(0).size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (List<Double> ls : lsExp) {
				result[i] += ls.get(i);
			}
		}
		return result;
	}
	
	/** 在这里是新的算法
	
//	  |--------- Ctrl1 <---- chiCvT1 ----> Treat1 ------------|
//	  |	        chiC12                          chiT12            |
//    |          Ctrl2 <---- chiCvT2 ----> Treat2             |
//chiC14    chiC23                          chrT23      chiT14
//    |          Ctrl3 <---- chiCvT3 ----> Treat3             |
//	  |	        chiC34	                         chrT34           |
//	  |--------- Ctrl4 <---- chiCvT4 ----> Treat4 ------------|
//		
//		组间卡方 chiCvT = chiCvT1 + chiCvT2 + chiCvT3 + chiCvT4    自由度为4
//		组内卡放 chiIn = chiC12 + chiC23 + chiC34 + chiC14 + chiT12 + chiT23 + chiT34 + chiT14 自由度为8
//		总F值为 F = (chiCvT/(4*3)) / (chriIn/(8*3))
* 乘以3是因为 chi 自己还有自由度，是3
* @param lsTreat
* @param lsCtrl
* @return
*/
	private double calculatePvalue(List<int[]> lsTreat, List<int[]> lsCtrl) {
		double chiCvT = 0, chiIn = 0;
		int[] ctrlFirst = null, treatFirst = null;
		int[] ctrlLast = null, treatLast = null;
		
		int dfCvT= lsTreat.size();
		int dfIn = 0;
		for (int i = 0; i < lsTreat.size(); i++) {
			int[] treatOne = lsTreat.get(i);
			int[] ctrlOne = lsCtrl.get(i);
			
			if (i == 0) {
				ctrlFirst = ctrlOne;
				treatFirst = treatOne;
			}
			if (ctrlLast != null) {
				dfIn += 2;
				chiIn += chiSquareDataSetsComparison(ctrlLast, ctrlOne);
				chiIn += chiSquareDataSetsComparison(treatLast, treatOne);
			}
			if (i == lsTreat.size() - 1) {
				dfIn += 2;
				chiIn += chiSquareDataSetsComparison(ctrlFirst, ctrlOne);
				chiIn += chiSquareDataSetsComparison(treatFirst, treatOne);
			}
			
			chiCvT += chiSquareDataSetsComparison(treatOne, ctrlOne);
			ctrlLast = ctrlOne;
			treatLast = treatOne;
		}
		double f = 0;
		int dN = 0, dD = 0;
		dN = dfCvT;
		dD = dfIn;
		FDistribution fDistribution = new FDistribution(dN*3, dD*3);
		f = (chiCvT/dfCvT*3) / (chiIn/dfIn*3);
		double pvalue = 1-fDistribution.cumulativeProbability(f);
		return pvalue;
	}
	
	
	public double calculatePvalue() {
		int[] cond1 = combReadsNumInt(mapTreat2LsValue);
		int[] cond2 = combReadsNumInt(mapCtrl2LsValue);
		if (!filter(cond1, cond2, juncAllReadsNum, juncSampleReadsNum)) {
			lsTreatValue.add(cond1);
			lsCtrlValue.add(cond2);
			return 1.0;
		}
		List<int[]> lsTreat = new ArrayList<>();
		List<int[]> lsCtrl = new ArrayList<>();
		for (int i = 0; i < lsTreat2LsValue.size(); i++) {
			List<Double> lsTreat_OneRepeat = lsTreat2LsValue.get(i);
			List<Double> lsCtrl_OneRepeat = lsCtrl2LsValue.get(i);
			int[] treatOne = getIntValue(lsTreat_OneRepeat);
			int[] ctrlOne = getIntValue(lsCtrl_OneRepeat);
	
			normalizeToLowValue(treatOne, normalizedNum);
			normalizeToLowValue(ctrlOne, normalizedNum);
			lsTreat.add(treatOne);
			lsCtrl.add(ctrlOne);
			
			lsTreatValue.add(treatOne);
			lsCtrlValue.add(ctrlOne);
		}
		return calculatePvalue(lsTreat, lsCtrl);
	}
	
	
	private int[] getIntValue(List<Double> lsDouble) {
		int[] result = new int[lsDouble.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsDouble.get(i).intValue();
		}
		return result;
	}
	
	/** 返回该位点的reads情况 */
	public int[] getReadsInfo() {
		double[] a2b = new double[2];
		for (String ctrl : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		for (String treat : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		int[] result = new int[]{(int) a2b[0], (int) a2b[1]};
		return result;
	}
	
	/** 返回该位点的reads情况 */
	public double getSpliceIndex() {
		double[] a2b1 = new double[2];
		double[] a2b2 = new double[2];
		for (String ctrl : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b1[i] += lsValue.get(i);
			}
		}
		for (String treat : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b2[i] += lsValue.get(i);
			}
		}
		if ((a2b1[0] <= 1 && a2b2[0] <= 1) ||(a2b1[1] <= 1 && a2b2[1] <= 1)) {
			return 1;
		}
		
		a2b1[0] += 1; a2b1[1] += 1; a2b2[0] +=1; a2b2[1] += 1;
		return Math.log10(a2b1[0]/a2b1[1]/(a2b2[0]/a2b2[1])) / Math.log10(2);
	}
	
	/** 
	 * 如果count数量太大，就将其标准化至一个比较低的值
	 * @param normalizedValue 大于该值就开始修正
	 */
	protected static void normalizeToLowValue(int[] condition, int normalizedValue) {
		int meanValue = (int) MathComput.mean(condition);
		if (meanValue < normalizedValue) {
			return;
		}
		for (int i = 0; i < condition.length; i++) {
			condition[i] = (int) ((double)condition[i]/meanValue * normalizedValue);
		}
	}
	
	protected static double chiSquareDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i] + 1;
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i] + 1;
		}
		
		long[][] cond = new long[2][];
		cond[0] = cond1Long;
		cond[1] = cond2Long;
		try {
			ChiSquareTestImpl chiSquareTestImpl = new ChiSquareTestImpl();
			return chiSquareTestImpl.chiSquare(cond);
		} catch (Exception e) {
			return 1.0;
		}		
		
	}
	
	/** 返回整理好的比较结果展示 */
	public String getCondtionTreat(boolean isInt) {
		return isInt? getConditionInt(mapTreat2LsValue) : getCondition(mapTreat2LsValue);
	}
	/** 返回整理好的比较结果展示 */
	public String getCondtionCtrl(boolean isInt) {
		return isInt? getConditionInt(mapCtrl2LsValue) : getCondition(mapCtrl2LsValue);
	}
	
	protected static String getConditionInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		if (mapGroup2LsValue == null || mapGroup2LsValue.size() == 0) {
			return "";
		}
		int[] junction = combReadsNumInt(mapGroup2LsValue);
		String condition = junction[0]+ "";
		for (int i = 1; i < junction.length; i++) {
			condition = condition + "::" + junction[i];
		}
		return condition;
	}
	
	protected static String getCondition(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		if (mapGroup2LsValue == null || mapGroup2LsValue.size() == 0) {
			return "";
		}
		double[] junction = combReadsNumDouble(mapGroup2LsValue);
		String condition = junction[0]+ "";
		for (int i = 1; i < junction.length; i++) {
			condition = condition + "::" + junction[i];
		}
		return condition;
	}
	
	private static int[] avgReadsNumInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		int[] result = combReadsNumInt(mapGroup2LsValue);
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/mapGroup2LsValue.size();
		}
		return result;
	}
	private static double[] avgReadsNumDouble(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		double[] result = combReadsNumDouble(mapGroup2LsValue);
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/mapGroup2LsValue.size();
		}
		return result;
	}
	
	
	private static int[] combReadsNumInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		int[] result = null;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);
			if (result == null) {
				result = new int[lsValues.size()];
			}
			for (int i = 0; i < result.length; i++) {
				result[i] += lsValues.get(i);
			}
		}
		return result;
	}
	private static double[] combReadsNumDouble(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		double[] result = null;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);
			if (result == null) {
				result = new double[lsValues.size()];
			}
			for (int i = 0; i < result.length; i++) {
				result[i] += lsValues.get(i);
			}
		}
		return result;
	}

	
	/**
	 *  某些情况不适合做分析，就过滤掉<br>
	 * 譬如：遇到类似 0:5 0:50<br>
	 * 和<br>
	 * 2：3：50<br>  4：2：50<br>
	 * 以及总reads过少的情况，就要删除不进行分析<br>
	 * 
	 * @param cond1
	 * @param cond2
	 * @param juncAllReadsSumMin
	 * @param juncReadsSampleMin
	 * @return
	 */
	protected static boolean filter(int[] cond1, int[] cond2, int juncAllReadsSumMin, int juncReadsSampleMin) {
		//遇到类似 0:5 0:50
		//2：3：50  4：2：50
		//等就要删除了
		int allReadsNum = MathComput.sum(cond1) + MathComput.sum(cond2);
		
		if (cond1.length <= 1 || cond2.length <= 1) {
			return false;
		}
		
		//判定基因表达差异太大也会过滤掉可变剪接
//		int readsNumLess = 0;
//		for (int i = 0; i < cond1.length; i++) {
//			if (cond1[i] <= allReadsNum/20 && cond2[i] <= allReadsNum/20) {
//				readsNumLess++;
//			}
//		}
//		if (cond1.length - readsNumLess <= 1) {
//			return false;
//		}
		
		if (MathComput.sum(cond1) < juncReadsSampleMin || MathComput.sum(cond2) < juncReadsSampleMin) {
			return false;
        }
		//总reads数太少也过滤
		if (allReadsNum < juncAllReadsSumMin) {
			return false;
		}
		return true;
	}

	@Override
	public String getSiteInfo() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int[] list : lsCtrlValue) {
			for (int double1 : list) {
				stringBuilder.append(double1 + ":");
			}
			stringBuilder.append("|");
		}
		stringBuilder.append("@");
		for (int[] list : lsTreatValue) {
			for (int double1 : list) {
				stringBuilder.append(double1 + ":");
			}
			stringBuilder.append("|");
		}
		return stringBuilder.toString();
	}
}

class SpliceTestCombine implements ISpliceTestModule {
	boolean makeSmallValueBigger = true;
	/** 如果count数超过该值，就标准化 */
	int normalizedNum = 200;
	/** 实验组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	ArrayListMultimap<String, Double> mapTreat2LsValue;
	/** 对照组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	ArrayListMultimap<String, Double> mapCtrl2LsValue;
	
	/** 是否为fisher检验，false表示选择卡方检验 */
	boolean isFisher = true;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	
	/** 将reads的数量扩大2倍，这样可以获得更多的差异 */
	double fold = 2;
	
	int[] cond1;
	int[] cond2;
	
	/** 数量小于这个值的就会扩大 */
	int numLessNeedBig = 80;
	
	@Override
	public void setMakeSmallValueBigger(boolean makeSmallValueBigger, int numLessNeedBig, double fold) {
		this.makeSmallValueBigger = makeSmallValueBigger;
		this.numLessNeedBig = numLessNeedBig;
		this.fold = fold;
	}
	
	/** 设定junction数量，小于该数量的不会进行分析
	 * 
	 * @param juncAllReadsNum 所有样本的junction数量必须大于该值，否则不进行计算，默认25
	 * @param juncSampleReadsNum 单个样本的junction数量必须大于该值，否则不进行计算，默认10
	 */
	public void setJuncReadsNum(int juncAllReadsNum, int juncSampleReadsNum) {
	    this.juncAllReadsNum = juncAllReadsNum;
	    this.juncSampleReadsNum = juncSampleReadsNum;
    }
	
	/** 目前设置为：exp设定为50，junction设定为200 */
	public void setNormalizedNum(int normalizedNum) {
		this.normalizedNum = normalizedNum;
	}
	
	public void setLsRepeat2Value(Map<String, Map<String, double[]>> mapCond_Group2ReadsNum, String condTreat,
			ArrayListMultimap<String, Double> mapTreat2LsValue, String condCtrl, ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		this.mapTreat2LsValue = mapTreat2LsValue;
		this.mapCtrl2LsValue = mapCtrl2LsValue;
	}
	
	public double calculatePvalue() {
		cond1 = combReadsNumInt(mapTreat2LsValue);
		cond2 = combReadsNumInt(mapCtrl2LsValue);
		if (!SpliceTestRepeat.filter(cond1, cond2, juncAllReadsNum, juncSampleReadsNum)) {
			return 1.0;
		}

		SpliceTestRepeat.normalizeToLowValue(cond1, normalizedNum);
		SpliceTestRepeat.normalizeToLowValue(cond2, normalizedNum);
		
		if (isFisher) {
			int sum = (int) (cond1[0] + cond1[1] + cond2[0] + cond2[1]);
			FisherTest fisherTest = new FisherTest(sum + 3);
			return fisherTest.getTwoTailedP(cond1[0], cond1[1], cond2[0], cond2[1]);
		} else {
			return SpliceTestRepeat.chiSquareTestDataSetsComparison(cond1, cond2);
		}
	}
	
	private int[] combReadsNumInt(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		int[] result = null;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);
			if (result == null) {
				result = new int[lsValues.size()];
			}
			for (int i = 0; i < result.length; i++) {
				result[i] += lsValues.get(i);
			}
		}
		
		if (makeSmallValueBigger) {
			normalizeByNum(result);
		}
		return result;
	}
	
	private void normalizeByNum(int[] result) {
		if (!makeSmallValueBigger) return;
		
		int allNum = 0;
		for (int i : result) {
			allNum += i;
		}
		
		double fold = 1;
		if (allNum < numLessNeedBig) {
			fold = this.fold;
		}
		
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) (result[i] * fold);
		}
	
	}

	
	/** 返回整理好的比较结果展示 */
	public String getCondtionTreat(boolean isInt) {
		return isInt? SpliceTestRepeat.getConditionInt(mapTreat2LsValue) : SpliceTestRepeat.getCondition(mapTreat2LsValue);
	}
	/** 返回整理好的比较结果展示 */
	public String getCondtionCtrl(boolean isInt) {
		return isInt? SpliceTestRepeat.getConditionInt(mapCtrl2LsValue) : SpliceTestRepeat.getCondition(mapCtrl2LsValue);
	}
	
	/** 返回该位点的reads情况 */
	public int[] getReadsInfo() {
		double[] a2b = new double[2];
		for (String ctrl : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		for (String treat : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		int[] result = new int[]{(int) a2b[0], (int) a2b[1]};
		return result;
	}

	@Override
	public String getSiteInfo() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i : cond1) {
			stringBuilder.append(i+":");
		}
		stringBuilder.append("@");
		for (int i : cond2) {
			stringBuilder.append(i+":");
		}
		return stringBuilder.toString();
	}
	
	/** 返回该位点的reads情况 */
	@Override
	public double getSpliceIndex() {
		double[] a2b1 = new double[2];
		double[] a2b2 = new double[2];
		for (String ctrl : mapTreat2LsValue.keys()) {
			List<Double> lsValue = mapTreat2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b1[i] += lsValue.get(i);
			}
		}
		for (String treat : mapCtrl2LsValue.keys()) {
			List<Double> lsValue = mapCtrl2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b2[i] += lsValue.get(i);
			}
		}
		if ((a2b1[0] <= 1 && a2b2[0] <= 1) ||(a2b1[1] <= 1 && a2b2[1] <= 1)) {
			return 1;
		}
		
		a2b1[0] += 1; a2b1[1] += 1; a2b2[0] +=1; a2b2[1] += 1;
		return Math.log10(a2b1[0]/a2b1[1]/(a2b2[0]/a2b2[1])) / Math.log10(2);
	}
	
}
