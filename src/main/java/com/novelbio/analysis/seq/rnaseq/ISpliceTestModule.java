package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.stat.inference.ChiSquareTestImpl;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.htmlparser.filters.IsEqualFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.dataStructure.ArrayOperate;
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
	
	/**
	 * 获得差异可变剪接PSI的值<br>
	 * 公式为<br>
	 * PSI = IR/(IR+ER)<br>
	 * 其中IR为该位点包含的reads数，ER为跳过该位点的reads数<br>
	 * 暂时仅考虑junction reads<br>
	 * @return
	 * key: group
	 * value: psi
	 */
	public double getPsi(boolean isCtrl);
	
	public static double getPsi(ArrayListMultimap<String, Double> mapGroup2LsValue) {
		double IR = 0, ER = 0;
		for (String group : mapGroup2LsValue.keySet()) {
			List<Double> lsValues = mapGroup2LsValue.get(group);//第一个是跳过，第二个是连上
			IR += lsValues.get(0);
			if (lsValues.size() == 1) {
				ER += 0;
			} else {
				ER += lsValues.get(1);
			}
		}
		if (IR+ER==0) {
			return 1;
		}
		return IR/(IR+ER);
	}
	
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
	
	public static double chiSquareDataSetsComparison(int[] cond1, int[] cond2) {
		if (cond1.length != 2 || cond2.length != 2) {
			throw new ExceptionNbcParamError("Splicing chiSquare does only support 2vs2");
		}
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
		double chiValue = 0;
		try {
			ChiSquareTestImpl chiSquareTestImpl = new ChiSquareTestImpl();
			chiValue = chiSquareTestImpl.chiSquare(cond);
		} catch (Exception e) {
		}
		return chiValue;
	}
	
	/** 如果实验组和对照组的重复不是一样多，那么平衡一下
	 * 注意把数量少的拿出来分给数量多的
	 * @param lessSize
	 * @param lslsMore
	 * @return
	 */
	@VisibleForTesting
	public static List<List<Double>> balanceUnEqualPair(int lessSize, List<List<Double>> lslsMore) {
		Collections.sort(lslsMore, (o1, o2) -> {
			Double sum1 = MathComput.sum(o1);
			Double sum2 = MathComput.sum(o2);
			int compare = -sum1.compareTo(sum2);
			if (compare == 0) {
				compare = o1.get(0).compareTo(o2.get(0));
			}
			if (compare == 0) {
				compare = o1.get(1).compareTo(o2.get(1));
			}
			return compare;
		});
		
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
	
	public static double[] combReadsNumPerRepeat(List<List<Double>> lsExp) {
		double[] result = new double[lsExp.get(0).size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (List<Double> ls : lsExp) {
				result[i] += ls.get(i);
			}
		}
		return result;
	}
	
	/** 返回该位点的reads情况 */
	public static double getSpliceIndex(ArrayListMultimap<String, Double> mapTreat2LsValue, ArrayListMultimap<String, Double> mapCtrl2LsValue) {
		double[] a1b1 = new double[2];
		double[] a2b2 = new double[2];
		for (String treat : mapTreat2LsValue.keySet()) {
			List<Double> lsValue = mapTreat2LsValue.get(treat);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a1b1[i] += lsValue.get(i);
			}
		}
		for (String ctrl : mapCtrl2LsValue.keySet()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b2[i] += lsValue.get(i);
			}
		}
		if ((a1b1[0] <= 1 && a2b2[0] <= 1) || (a1b1[1] <= 1 && a2b2[1] <= 1)) {
			return 0;
		}
		
		a1b1[0] += 1; a1b1[1] += 1; a2b2[0] +=1; a2b2[1] += 1;
		return Math.log10(a1b1[0]/a1b1[1]/(a2b2[0]/a2b2[1])) / Math.log10(2);
	}
	
}

class SpliceTestRepeat implements ISpliceTestModule {
	boolean makeSmallValueBigger = true;
	
	/**
	 * include:exclude值和必须要大于该值 
	 * 某一对比较,譬如 include:23 exclude:34 他们之和大于 minSum
	 * 也就是要过滤 include:2 exclude:8 这种
	 */
	int minSum = 10; 
	/**
	 * inclue或exclude中某一个值必须要大于该值
	 * 某一对比较 include和exclude,他们中必须有某个值大于 minSite
	 * 也就是要过滤include:7 exclude:6 这种
	 *  include:20 exclude:3 这种会保留
	 */
	int minSite = 8;
	
	/** 将reads的数量扩大2倍，这样可以获得更多的差异 */
	double fold = 2;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	
	/** 如果count数超过该值，就标准化 */
	int normalizedNum = 200;
	/** reads数量小于该值就需要扩大倍数 */
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
	
	/**
	 * 是否计算本次比较,如果没有通过过滤,则不进行本次比较
	 */
	boolean isFilter = false;
	
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
		
		ArrayListMultimap<String, Double> mapTreat2LsValueNew = filterList(mapTreat2LsValue, minSum, minSite);
		ArrayListMultimap<String, Double> mapCtrl2LsValueNew = filterList(mapCtrl2LsValue, minSum, minSite);
		isFilter = isFilterNull(mapTreat2LsValueNew.size(), mapTreat2LsValue.size(),
				mapCtrl2LsValueNew.size(), mapCtrl2LsValue.size());
		//过滤掉了意味着不需要后续计算了
		if (isFilter) return;
		
		lsTreat2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condTreat), mapTreat2LsValueNew, getFold(mapTreat2LsValueNew));
		lsCtrl2LsValue = normalizeLsDouble(mapCond_Group2ReadsNum.get(condCtrl), mapCtrl2LsValueNew, getFold(mapCtrl2LsValueNew));
		
		if (this.lsTreat2LsValue.size() < this.lsCtrl2LsValue.size()) {
			this.lsCtrl2LsValue = ISpliceTestModule.balanceUnEqualPair(lsTreat2LsValue.size(), lsCtrl2LsValue);
		} else if (this.lsTreat2LsValue.size() > this.lsCtrl2LsValue.size()) {
			this.lsTreat2LsValue = ISpliceTestModule.balanceUnEqualPair(lsCtrl2LsValue.size(), lsTreat2LsValue);
		}
	}
	
	/**
	 * 过滤
	 * @param mapCond2LsValue 一对一对的include:exclude 所以里面一个List-Double长度恒为2,第一个是include,第二个是exclude
	 * @param minSum include:exclude值和必须要大于该值 
	 * 某一对比较,譬如 include:23 exclude:34 他们之和大于 minSum
	 * 也就是要过滤 include:2 exclude:8 这种
	 * @return minSite inclue或exclude中某一个值必须要大于该值
	 * 某一对比较 include和exclude,他们中必须有某个值大于 minSite
	 * 也就是要过滤include:7 exclude:6 这种
	 *  include:20 exclude:3 这种会保留
	 */
	private ArrayListMultimap<String, Double> filterList(ArrayListMultimap<String, Double> mapCond2LsValue, int minSum, int minSite) {
		ArrayListMultimap<String, Double> mapCond2LsValueNew = ArrayListMultimap.create();
		for (String group : mapCond2LsValue.keySet()) {
			List<Double> list = mapCond2LsValue.get(group);
			//某一对比较,譬如 include:23 exclude:34 他们之和大于10
			//也就是要过滤 include:2 exclude:8 这种
			if (MathComput.sum(list) <= minSum) {
				continue;
			}
			//某一对比较 include和exclude,他们中必须有某个值大于8
			//也就是要过滤include:7 exclude:6 这种
			// include:20 exclude:3 这种会保留
			boolean isAnyValueBiggerThanNum = false;
			for (Double values : list) {
				if (values >= minSite) {
					isAnyValueBiggerThanNum = true;
				}
			}
			if (!isAnyValueBiggerThanNum) {
				continue;
			}
			mapCond2LsValueNew.putAll(group, list);
		}
		return mapCond2LsValueNew;
	}
	
	/** 过滤掉太多值后是否还进行分析 */
	private boolean isFilterNull(int treatFilter, int treatAll, int ctrlFilter, int ctrlAll) {
		double ratioTreat = (double)(treatAll-treatFilter)/treatAll;
		double ratioCtrl = (double)(ctrlAll-ctrlFilter)/ctrlAll;
		boolean isFilter = (ratioTreat > 0.4 && ratioCtrl > 0.4)|| ratioTreat>0.55 || ratioCtrl > 0.55;
		return isFilter;
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
	
	public double getPsi(boolean isCtrl) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = isCtrl ? mapCtrl2LsValue : mapTreat2LsValue;
		return ISpliceTestModule.getPsi(mapGroup2LsValue);
	}
	
	public double calculatePvalue() {
		if (isFilter) return 1.0;
		
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
		double pvalue = calculatePvalueMax(lsTreat, lsCtrl);
		//和过滤掉的区分下
		if (pvalue == 1.0) pvalue = 0.99999999;
		return pvalue;
	}
	
	
	private int[] getIntValue(List<Double> lsDouble) {
		int[] result = new int[lsDouble.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsDouble.get(i).intValue();
		}
		return result;
	}
	
	/** 计算个偏大的pvalue */
	private double calculatePvalueMax(List<int[]> lsTreat, List<int[]> lsCtrl) {
//		
//		List<int[]> lsTreatNew = copyList(lsTreat, 0);
//		List<int[]> lsCtrlNew = copyList(lsCtrl, 0);
//		Collections.sort(lsTreatNew, new CompareValuesExp());
//		Collections.sort(lsCtrlNew, new CompareValuesExp());

		double pvalue1 = calculatePvalue3(lsTreat, lsCtrl);
		
//		lsTreatNew = copyList(lsTreat, 0);
//		lsCtrlNew = copyList(lsCtrl, 0);
//		Collections.sort(lsTreatNew, new CompareValuesRatio());
//		Collections.sort(lsCtrlNew, new CompareValuesRatio());
//		double pvalue2 = calculatePvalue(lsTreatNew, lsCtrlNew);
//
//		return Math.max(pvalue1, pvalue2);
		return pvalue1;
	}
	
	private void printLs(List<int[]> lsValues) {
		List<String> lsInfo = new ArrayList<>();
		for (int[] is : lsValues) {
			lsInfo.add(is[0] + ":" + is[1]);
		}
		System.out.println(ArrayOperate.cmbString(lsInfo, "\t"));
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
//		如果组间卡方不差异，则不计算组内卡方，直接返回组间卡方所对应的pvalue
//
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
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(ctrlLast, ctrlOne);
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(treatLast, treatOne);
			}
			if (i == lsTreat.size() - 1) {
				dfIn += 2;
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(ctrlFirst, ctrlOne);
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(treatFirst, treatOne);
			}
			
			chiCvT += ISpliceTestModule.chiSquareDataSetsComparison(treatOne, ctrlOne);
			ctrlLast = ctrlOne;
			treatLast = treatOne;
		}
		double f = 0;
		int dN = 0, dD = 0;
		dN = dfCvT;
		dD = dfIn;
		
		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfCvT*3);
		double pvalueCvT = 1 - chiSquaredDistribution.cumulativeProbability(chiCvT);
		
		if (pvalueCvT > 0.6) {
			return pvalueCvT;
		}
		
		FDistribution fDistribution = new FDistribution(dN*3, dD*3);
		f = (chiCvT/dfCvT*3) / (chiIn/dfIn*3);
		double pvalue = 1 - fDistribution.cumulativeProbability(f);
		return Math.max(pvalueCvT, pvalue);
	}
	
	/** 在这里是新的算法
	
//	  |--------- Ctrl1 <---- chiCvT1 ----> Treat1 ------------|
//	  |	        chiC12                          chiT12            |
//  |          Ctrl2 <---- chiCvT2 ----> Treat2             |
//chiC14    chiC23                          chrT23      chiT14
//  |          Ctrl3 <---- chiCvT3 ----> Treat3             |
//	  |	        chiC34	                         chrT34           |
//	  |--------- Ctrl4 <---- chiCvT4 ----> Treat4 ------------|
//		
//		组间卡方 chiCvT = chiCvT1 + chiCvT2 + chiCvT3 + chiCvT4    自由度为4
//		如果组间卡方不差异，则不计算组内卡方，直接返回组间卡方所对应的pvalue
//
//		组内卡放 chiIn = chiC12 + chiC23 + chiC34 + chiC14 + chiT12 + chiT23 + chiT34 + chiT14 自由度为8
//		总F值为 F = (chiCvT/(4*3)) / (chriIn/(8*3))
* 乘以3是因为 chi 自己还有自由度，是3
* @param lsTreat
* @param lsCtrl
* @return
*/
	private double calculatePvalue2(List<int[]> lsTreat, List<int[]> lsCtrl) {

		double chiCvT = 0, chiIn = 0;
		int[] ctrlFirst = null, treatFirst = null;
		int[] ctrlLast = null, treatLast = null;
		
		int dfCvT= lsTreat.size();
		int dfIn = 0;
		List<ChiCvsT> lsChiCvT = new ArrayList<>();
		
		for (int i = 0; i < lsTreat.size(); i++) {
			int[] treatOne = lsTreat.get(i);
			int[] ctrlOne = lsCtrl.get(i);
			
			if (i == 0) {
				ctrlFirst = ctrlOne;
				treatFirst = treatOne;
			}
			if (ctrlLast != null) {
				dfIn += 2;
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(ctrlLast, ctrlOne);
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(treatLast, treatOne);
			}
			if (i == lsTreat.size() - 1) {
				dfIn += 2;
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(ctrlFirst, ctrlOne);
				chiIn += ISpliceTestModule.chiSquareDataSetsComparison(treatFirst, treatOne);
			}
			
			for (int j = 0; j < lsCtrl.size(); j++) {
				int[] ctrlThis = lsCtrl.get(j);
				double chiTmp = ISpliceTestModule.chiSquareDataSetsComparison(treatOne, ctrlThis);
				lsChiCvT.add(new ChiCvsT(i, j, chiTmp));
			}
			ctrlLast = ctrlOne;
			treatLast = treatOne;
		}
		double f = 0;
		int dN = 0, dD = 0;
		dN = dfCvT;
		dD = dfIn;
		chiCvT = ChiCvsT.getChiMerge2(lsChiCvT, lsTreat.size());

		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfCvT*3);
		double pvalueCvT = 1 - chiSquaredDistribution.cumulativeProbability(chiCvT);
		
		if (pvalueCvT > 0.5) {
			return pvalueCvT;
		}
		
		FDistribution fDistribution = new FDistribution(dN*3, dD*3);
		f = (chiCvT/dfCvT*3) / (chiIn/dfIn*3);
		double pvalue = 1 - fDistribution.cumulativeProbability(f);
		return Math.max(pvalueCvT, pvalue);
	}
	
	
	/** 在这里是新的算法
	
//	  |--------- Ctrl1 <---- chiCvT1 ----> Treat1 ------------|
//	  |	        chiC12                          chiT12            |
//  |          Ctrl2 <---- chiCvT2 ----> Treat2             |
//chiC14    chiC23                          chrT23      chiT14
//  |          Ctrl3 <---- chiCvT3 ----> Treat3             |
//	  |	        chiC34	                         chrT34           |
//	  |--------- Ctrl4 <---- chiCvT4 ----> Treat4 ------------|
//		
//		组间卡方 chiCvT = chiCvT1 + chiCvT2 + chiCvT3 + chiCvT4    自由度为4，配对采用选择两两间chi最小的配对模式
//		如果组间卡方不差异，则不计算组内卡方，直接返回组间卡方所对应的pvalue
//
//		组内卡放 chiIn = chiC12 + chiC23 + chiC34 + chiC14 + chiT12 + chiT23 + chiT34 + chiT14 自由度为8
 * 组内方差为 Ctrl1 vs Other 取平均数 这种
//		总F值为 F = (chiCvT/(4*3)) / (chriIn/(8*3))
* 乘以3是因为 chi 自己还有自由度，是3
* @param lsTreat
* @param lsCtrl
* @return
*/
	private double calculatePvalue3(List<int[]> lsTreat, List<int[]> lsCtrl) {
		double chiIn = 0;
		int dfCvT= lsTreat.size();
		int dfIn = 0;
		List<ChiCvsT> lsChiCvT = new ArrayList<>();
		
		for (int i = 0; i < lsTreat.size(); i++) {
			int[] treat = lsTreat.get(i);
			List<Double> lsDfInTreat = new ArrayList<>(); 
			List<Double> lsDfInCtrl = new ArrayList<>(); 
			for (int j = 0; j < lsCtrl.size(); j++) {
				int[] ctrl = lsCtrl.get(j);
				double chiTmp = ISpliceTestModule.chiSquareDataSetsComparison(treat, ctrl);
				lsChiCvT.add(new ChiCvsT(i, j, chiTmp));
			}
			for (int j = 0; j < lsTreat.size(); j++) {
				if (j == i) continue;
				lsDfInTreat.add(ISpliceTestModule.chiSquareDataSetsComparison(lsTreat.get(i), lsTreat.get(j)));
			}
			for (int j = 0; j < lsCtrl.size(); j++) {
				if (j == i) continue;
				lsDfInCtrl.add(ISpliceTestModule.chiSquareDataSetsComparison(lsCtrl.get(i), lsCtrl.get(j)));
			}
			chiIn += getMeanValue(lsDfInTreat);
			chiIn += getMeanValue(lsDfInCtrl);
			dfIn += 2;
		}
		double f = 0;
		int dN = lsTreat.size();
		int dD = dfIn;
		double chiCvT = ChiCvsT.getChiMerge2(lsChiCvT, lsTreat.size());

		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfCvT*3);
		double pvalueCvT = 1 - chiSquaredDistribution.cumulativeProbability(chiCvT);
		
		if (pvalueCvT > 0.5) {
			return pvalueCvT;
		}
		
		FDistribution fDistribution = new FDistribution(dN*3, dD*3);
		f = (chiCvT/dfCvT*3) / (chiIn/dfIn*3);
		double pvalue = 1 - fDistribution.cumulativeProbability(f);
		return Math.max(pvalueCvT, pvalue);
	}
	

	/** 在这里是新的算法
	
//	  |--------- Ctrl1 <---- chiCvT1 ----> Treat1 ------------|
//	  |	        chiC12                          chiT12            |
//  |          Ctrl2 <---- chiCvT2 ----> Treat2             |
//chiC14    chiC23                          chrT23      chiT14
//  |          Ctrl3 <---- chiCvT3 ----> Treat3             |
//	  |	        chiC34	                         chrT34           |
//	  |--------- Ctrl4 <---- chiCvT4 ----> Treat4 ------------|
//		
//		组间卡方 chiCvT = chiCvT1 + chiCvT2 + chiCvT3 + chiCvT4    自由度为4，配对采用选择两两间chi最小的配对模式
//		如果组间卡方不差异，则不计算组内卡方，直接返回组间卡方所对应的pvalue
//
//		组内卡放 chiIn = chiC12 + chiC23 + chiC34 + chiC14 + chiT12 + chiT23 + chiT34 + chiT14 自由度为8
 * 组内方差为 Ctrl1 vs Other 取平均数 这种
//		总F值为 F = (chiCvT/(4*3)) / (chriIn/(8*3))
* 乘以3是因为 chi 自己还有自由度，是3
* @param lsTreat
* @param lsCtrl
* @return
*/
	private double calculatePvalue4(List<int[]> lsTreat, List<int[]> lsCtrl) {
		double chiIn = 0;
		double chiCvT = 0;
		int dfCvT = 0;
		int dfIn = 0;
		List<ChiCvsT> lsChiCvT = new ArrayList<>();
		
		for (int i = 0; i < lsTreat.size(); i++) {
			int[] treat = lsTreat.get(i);
			for (int j = 0; j < lsCtrl.size(); j++) {
				int[] ctrl = lsCtrl.get(j);
				chiCvT+=ISpliceTestModule.chiSquareDataSetsComparison(treat, ctrl);
				dfCvT++;
			}
			for (int j = i+1; j < lsTreat.size(); j++) {
				if (j == i) continue;
				chiIn+= ISpliceTestModule.chiSquareDataSetsComparison(lsTreat.get(i), lsTreat.get(j));
				dfIn++;
			}
			for (int j = i+1; j < lsCtrl.size(); j++) {
				if (j == i) continue;
				chiIn+= ISpliceTestModule.chiSquareDataSetsComparison(lsCtrl.get(i), lsCtrl.get(j));
				dfIn++;
			};
		}
		double f = 0;
		int dN = lsTreat.size();
		int dD = dfIn;

		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfCvT*3);
		double pvalueCvT = 1 - chiSquaredDistribution.cumulativeProbability(chiCvT);
		
		if (pvalueCvT > 0.5) {
			return pvalueCvT;
		}
		
		FDistribution fDistribution = new FDistribution(dN*3, dD*3);
		f = (chiCvT/dfCvT*3) / (chiIn/dfIn*3);
		double pvalue = 1 - fDistribution.cumulativeProbability(f);
		return Math.max(pvalueCvT, pvalue);
	}
	
	/** 去除大于2.5倍标准差的数字，再算均值 */
	@VisibleForTesting
	protected static double getMeanValue(List<? extends Number> lsValues) {
		double sdThresholdFold = 2.5;//去除超过2.5倍标准差的异常点
		
		double[] values = new double[lsValues.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = lsValues.get(i).doubleValue();
		}
		StandardDeviation standardDeviation = new StandardDeviation();
		double sd = standardDeviation.evaluate(values);
		double mean = MathComput.mean(values);
		double thresholdUp = mean + sd*sdThresholdFold;
		double thresholdDown = mean - sd*sdThresholdFold;
		
		double sum = 0;
		int allNum = 0;
		for (Number d : lsValues) {
			double dd = d.doubleValue();
			if (dd >= thresholdDown && dd<= thresholdUp) {
				sum += dd;
				allNum++;
			}
		}
		return sum/allNum;
	}
	
	static class ChiCvsT {
		String group1;
		String group2;
		Double chi;
		
		public ChiCvsT(int group1, int group2, double chi) {
			this.group1 = "ga" + group1;
			this.group2 = "gb" + group2;
			this.chi = chi;
		}
	
		public String getGroup1() {
			return group1;
		}
		public String getGroup2() {
			return group2;
		}
		public Double getChi() {
			return chi;
		}
		
		public static double getChiMerge(List<ChiCvsT> lsChiCvsTs) {
			Collections.sort(lsChiCvsTs, (o1, o2) -> { return -o1.getChi().compareTo(o2.getChi());});
			ArrayListMultimap<String, ChiCvsT> mapGroup2LsChis = ArrayListMultimap.create();
			for (ChiCvsT chiCvsT : lsChiCvsTs) {
				mapGroup2LsChis.put(chiCvsT.getGroup1(), chiCvsT);
				mapGroup2LsChis.put(chiCvsT.getGroup2(), chiCvsT);
			}
			List<ChiCvsT> lsFinal = new ArrayList<>();
			Set<String> setGroup = new HashSet<>();
			for (ChiCvsT chiCvsT : lsChiCvsTs) {
				if (setGroup.contains(chiCvsT.getGroup1()) || setGroup.contains(chiCvsT.getGroup2())) {
					continue;
				}
				List<ChiCvsT> ls1 = mapGroup2LsChis.get(chiCvsT.getGroup1());
				ChiCvsT minChi1 = getMinCvsT(ls1);
				if (setGroup.contains(minChi1.getGroup1()) || setGroup.contains(minChi1.getGroup2())) {
					continue;
				}
				lsFinal.add(minChi1);
				setGroup.add(minChi1.getGroup1());
				setGroup.add(minChi1.getGroup2());
				//============================
				List<ChiCvsT> ls2 = mapGroup2LsChis.get(chiCvsT.getGroup2());
				ChiCvsT minChi2 = getMinCvsT(ls2);
				if (setGroup.contains(minChi2.getGroup1()) || setGroup.contains(minChi2.getGroup2())) {
					continue;
				}
				lsFinal.add(minChi1);
				setGroup.add(minChi1.getGroup1());
				setGroup.add(minChi1.getGroup2());
			}
			double result = 0;
			for (ChiCvsT chiCvsT : lsFinal) {
				result += chiCvsT.getChi();
			}
			return result;
		}
		
		public static double getChiMerge2(List<ChiCvsT> lsChiCvsTs, int size) {
			Collections.sort(lsChiCvsTs, (o1, o2) -> { return o1.getChi().compareTo(o2.getChi());});
			Map<String, int[]> mapGroup2Sum = new HashMap<>();
			for (int i = 0; i < lsChiCvsTs.size(); i++) {
				ChiCvsT chiCvsT = lsChiCvsTs.get(i);
				int[] num1 = getValueFromMap(mapGroup2Sum, chiCvsT.getGroup1());
				num1[0] += i;
				int[] num2 = getValueFromMap(mapGroup2Sum, chiCvsT.getGroup2());
				num2[0] += i;
			}
			
			List<String[]> lsGroup2Num = new ArrayList<>();
			for (String groupId : mapGroup2Sum.keySet()) {
				lsGroup2Num.add(new String[]{groupId, mapGroup2Sum.get(groupId)[0] + ""});
			}
			Collections.sort(lsGroup2Num, (o1, o2) -> { return -Integer.valueOf(o1[1]).compareTo(Integer.valueOf(o2[1]));});

			
			ArrayListMultimap<String, ChiCvsT> mapGroup2LsChis = ArrayListMultimap.create();
			for (ChiCvsT chiCvsT : lsChiCvsTs) {
				mapGroup2LsChis.put(chiCvsT.getGroup1(), chiCvsT);
				mapGroup2LsChis.put(chiCvsT.getGroup2(), chiCvsT);
			}
			List<ChiCvsT> lsFinal = new ArrayList<>();
			Set<String> setGroup = new HashSet<>();
			for (String[] group2Num : lsGroup2Num) {
				String group = group2Num[0];
				if (setGroup.contains(group) ) {
					continue;
				}
				List<ChiCvsT> ls1 = mapGroup2LsChis.get(group);
				Collections.sort(ls1, (o1, o2) -> { return o1.getChi().compareTo(o2.getChi());});
				for (ChiCvsT chiCvsT : ls1) {
					if (setGroup.contains(chiCvsT.getGroup1()) || setGroup.contains(chiCvsT.getGroup2())) {
						continue;
					}
					lsFinal.add(chiCvsT);
					setGroup.add(chiCvsT.getGroup1());
					setGroup.add(chiCvsT.getGroup2());
					break;
				}
			}
			double result = 0;
			for (ChiCvsT chiCvsT : lsFinal) {
				result += chiCvsT.getChi();
			}
			if (lsFinal.size() != size) {
				throw new RuntimeException();
			}
 			return result;
		}
		
		private static int[] getValueFromMap(Map<String, int[]> mapGroup2Sum, String groupId) {
			int[] num = mapGroup2Sum.get(groupId);
			if (num == null) {
				num = new int[]{0};
				mapGroup2Sum.put(groupId, num);
			}
			return num;
		}
		
		private static ChiCvsT getMinCvsT(List<ChiCvsT> lsChis) {
			ChiCvsT minChi = null;
			for (ChiCvsT chiCvsT : lsChis) {
				if (minChi == null || chiCvsT.getChi() < minChi.getChi()) {
					minChi = chiCvsT;
				}
			}
			return minChi;
		}
	}
	
	class CompareValuesExpSimple implements Comparator<int[]> {
		@Override
		public int compare(int[] or1, int[] or2) {
			int[] o1 = copyArray(or1, 0), o2 = copyArray(or2, 0);
			Double sum1 = (double)o1[0]+o1[1], sum2 = (double)o2[0]+o2[1];
			int result = sum1.compareTo(sum2);
			if (result == 0) {
				result = Integer.valueOf(o1[0]).compareTo(Integer.valueOf(o2[0]));
			}
			if (result == 0) {
				result = Integer.valueOf(o1[1]).compareTo(Integer.valueOf(o2[1]));
			}
			return result;
		}
	}
	
	class CompareValuesRatio implements Comparator<int[]> {
		@Override
		public int compare(int[] or1, int[] or2) {
			int[] o1 = copyArray(or1, 0), o2 = copyArray(or2, 0);
			o1[0]++; o1[1]++; o2[0]++; o2[1]++; //防止除数为0
			
			Double ratio1 = (double)o1[0]/o1[1], ratio2 = (double)o2[0]/o2[1];
			double ratio_ratio = Math.log10(ratio1/ratio2)/Math.log10(2);
			
			Double sum1 = (double)o1[0]+o1[1], sum2 = (double)o2[0]+o2[1];
			double ratio_exp = Math.log10(sum1/sum2)/Math.log10(2);
			 
			if (Math.abs(ratio_ratio) > 3) {
				return ratio_ratio > 0? 1 : -1;
			}
			return sum1.compareTo(sum2);
//			if (Math.abs(ratio_exp) > 0.112) {
//				return ratio_exp > 0? 1 : -1;
//			}
//			return ratio1.compareTo(ratio2);
		}
	}
	
	class CompareValuesExp implements Comparator<int[]> {
		@Override
		public int compare(int[] or1, int[] or2) {
			int[] o1 = copyArray(or1, 0), o2 = copyArray(or2, 0);
			o1[0]++; o1[1]++; o2[0]++; o2[1]++; //防止除数为0
			
			Double ratio1 = (double)o1[0]/o1[1], ratio2 = (double)o2[0]/o2[1];
			double ratio_ratio = Math.log10(ratio1/ratio2)/Math.log10(2);
			
			Double sum1 = (double)o1[0]+o1[1], sum2 = (double)o2[0]+o2[1];
			double ratio_exp = Math.log10(sum1/sum2)/Math.log10(2);
			 
			if (Math.abs(ratio_exp) > 0.1) {
				return ratio_exp > 0? 1 : -1;
			}
			return ratio1.compareTo(ratio2);
		}

	}
	private int[] copyArray(int[] raw, int addNum) {
		int[] result = new int[raw.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = raw[i] + addNum;
		}
		return result;
	}
	private List<int[]> copyList(List<int[]> lsInput, int addNum) {
		List<int[]> lsResult = new ArrayList<>();
		for (int[] is : lsInput) {
			lsResult.add(copyArray(is, addNum));
		}
		return lsResult;
	}
	/** 返回该位点的reads情况 */
	public int[] getReadsInfo() {
		double[] a2b = new double[2];
		for (String ctrl : mapCtrl2LsValue.keySet()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		for (String treat : mapTreat2LsValue.keySet()) {
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
		return ISpliceTestModule.getSpliceIndex(mapTreat2LsValue, mapCtrl2LsValue);
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
			condition = junction[i] + "::" + condition;
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
			condition = junction[i] + "::" + condition;
		}
		return condition;
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
	private static final Logger logger = LoggerFactory.getLogger(SpliceTestCombine.class);
	
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
		if (mapTreat2LsValue == null) {
			logger.error("stop");
		}
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
	
	public 	ArrayListMultimap<String, Double> getMapCtrl2LsValue() {
		return mapCtrl2LsValue;
	}
	public ArrayListMultimap<String, Double> getMapTreat2LsValue() {
		return mapTreat2LsValue;
	}
	
	public double getPsi(boolean isCtrl) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = isCtrl ? mapCtrl2LsValue : mapTreat2LsValue;
		return ISpliceTestModule.getPsi(mapGroup2LsValue);
	}
	
	/** 返回该位点的reads情况 */
	public int[] getReadsInfo() {
		double[] a2b = new double[2];
		for (String ctrl : mapCtrl2LsValue.keySet()) {
			List<Double> lsValue = mapCtrl2LsValue.get(ctrl);
			for (int i = 0; i < 2; i++) {
				if (i >= lsValue.size()) continue;
				
				a2b[i] += lsValue.get(i);
			}
		}
		for (String treat : mapTreat2LsValue.keySet()) {
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
	public double getSpliceIndex() {
		return ISpliceTestModule.getSpliceIndex(mapTreat2LsValue, mapCtrl2LsValue);
	}
	
}
