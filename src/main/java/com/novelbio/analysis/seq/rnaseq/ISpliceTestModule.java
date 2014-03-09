package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
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
	/** 实验组和对照组的junction reads数量加起来小于这个数，就返回1 */
	static int junctionReadsMinNum = 10;
	
	/** 将reads的数量扩大5倍，这样可以获得更多的差异 */
	static int foldbig = 3;
	/** 将reads的数量扩大3倍，这样可以获得更多的差异 */
	static int foldMid = 3;
	/** 如果reads数量过少，可以考虑扩大3倍 */
	static int foldsmall = 3;
	
	/** 如果count数超过该值，就标准化 */
	int normalizedNum = 400;
	
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
	
	private int getFold(ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		double valueAll = 0;
		for (Double value : mapCtrl2LsValue.values()) {
			valueAll += value;
		}
		if (valueAll < 30) {
			return foldbig;
		} else if (valueAll >= 20 && valueAll < 80) {
			return foldMid;
		} else {
			return foldsmall;
		}
	}
	
	/**
	 * @param mapGroup2Value
	 * @param mapTreat2LsValue
	 * @param fold 扩大倍数
	 * @return
	 */
	private List<List<Double>> normalizeLsDouble(Map<String, double[]> mapGroup2Value, 
			ArrayListMultimap<String, Double> mapTreat2LsValue, int fold) {
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
		if (!filter(cond1, cond2)) {
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

	
	/** 某些情况不适合做分析，就过滤掉<br>
	 * 譬如：遇到类似 0:5 0:50<br>
	 * 和<br>
	 * 2：3：50<br>  4：2：50<br>
	 * 以及总reads过少的情况，就要删除不进行分析<br>
	 */
	protected static boolean filter(int[] cond1, int[] cond2) {
		//遇到类似 0:5 0:50
		//2：3：50  4：2：50
		//等就要删除了
		int allReadsNum = MathComput.sum(cond1) + MathComput.sum(cond2);
		int readsNumLess = 0;
		for (int i = 0; i < cond1.length; i++) {
			try {
				if (cond1[i] <= allReadsNum/20 && cond2[i] <= allReadsNum/20) {
					readsNumLess++;
				}
			} catch (Exception e) {
				if (cond1[i] <= allReadsNum/20 && cond2[i] <= allReadsNum/20) {
					readsNumLess++;
				}
			}
		
		}
		if (cond1.length - readsNumLess <= 1) {
			return false;
		}
		
		//总reads数太少也过滤
		if (MathComput.sum(cond1) + MathComput.sum(cond1) < junctionReadsMinNum) {
			return false;
		}
		return true;
	}

}

class SpliceTestCombine implements ISpliceTestModule {
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
	boolean isFisher = false;
	
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
		int[] cond1 = combReadsNumInt(mapTreat2LsValue);
		int[] cond2 = combReadsNumInt(mapCtrl2LsValue);
		if (!SpliceTestRepeat.filter(cond1, cond2)) {
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
		return result;
	}

	
	/** 返回整理好的比较结果展示 */
	public String getCondtionTreat(boolean isInt) {
		return isInt? SpliceTestRepeat.getConditionInt(mapTreat2LsValue) : SpliceTestRepeat.getCondition(mapTreat2LsValue);
	}
	/** 返回整理好的比较结果展示 */
	public String getCondtionCtrl(boolean isInt) {
		return isInt? SpliceTestRepeat.getConditionInt(mapCtrl2LsValue) : SpliceTestRepeat.getCondition(mapCtrl2LsValue);
	}
}
