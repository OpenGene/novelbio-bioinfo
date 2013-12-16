package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.TestUtils;

import com.novelbio.base.dataStructure.MathComput;


public class SpliceTestModule {
	public static void main(String[] args) {
		List<List<Double>> lsTreat = new ArrayList<>();
		List<Double> lsRep1 = new ArrayList<>();
		lsRep1.add(10.0); lsRep1.add(40.0);
		lsTreat.add(lsRep1);
		List<Double> lsRep2 = new ArrayList<>();
		lsRep2.add(10.0); lsRep2.add(40.0);
		lsTreat.add(lsRep2);
		
		List<List<Double>> lsCtrl = new ArrayList<>();
		lsRep1 = new ArrayList<>();
		lsRep1.add(10.0); lsRep1.add(40.0);
		lsCtrl.add(lsRep1);
		lsRep2 = new ArrayList<>();
		lsRep2.add(42.0); lsRep2.add(18.0);
		lsCtrl.add(lsRep2);
		
		
		SpliceTestModule spliceTestModule = new SpliceTestModule();
		spliceTestModule.setLsRepeat2Value(true, lsTreat, lsCtrl);
		System.out.println(spliceTestModule.calculatePvalue());
	}
	
	/** 实验组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsTreat2LsValue;
	/** 对照组若干重复下的表达情况
	 * list--每个group--下若干位点的表达值
	 */
	List<List<Double>> lsCtrl2LsValue;
	
	public void setLsRepeat2Value(boolean IsRepeat2Value, List<List<Double>> lsTreat2LsValue, List<List<Double>> lsCtrl2LsValue) {
		if (IsRepeat2Value) {
			this.lsTreat2LsValue = lsTreat2LsValue;
			this.lsCtrl2LsValue = lsCtrl2LsValue;
		} else {
			this.lsTreat2LsValue = new ArrayList<>();
			this.lsCtrl2LsValue = new ArrayList<>();
			convert(lsTreat2LsValue, this.lsTreat2LsValue);
			convert(lsCtrl2LsValue, this.lsCtrl2LsValue);
		}
		
		if (this.lsTreat2LsValue.size() < this.lsCtrl2LsValue.size()) {
			this.lsCtrl2LsValue = balanceUnEqualPair(lsTreat2LsValue.size(), lsCtrl2LsValue);
		} else if (this.lsTreat2LsValue.size() > this.lsCtrl2LsValue.size()) {
			this.lsTreat2LsValue = balanceUnEqualPair(lsCtrl2LsValue.size(), lsTreat2LsValue);
		}
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
	
	public List<List<Double>> getLsCtrl2LsValue() {
		return lsCtrl2LsValue;
	}
	public List<List<Double>> getLsTreat2LsValue() {
		return lsTreat2LsValue;
	}
	
	/** 仅供测试 */
	protected void convert(List<List<Double>> lsSite2LsRepeat, List<List<Double>> lsRepeat2SiteResult) {
		for (int i = 0; i < lsSite2LsRepeat.get(0).size(); i++) {
			List<Double> lsRepeat = new ArrayList<>();
			for (int j = 0; j < lsSite2LsRepeat.size(); j++) {
				lsRepeat.add(lsSite2LsRepeat.get(j).get(i));
			}
			lsRepeat2SiteResult.add(lsRepeat);
		}
	}
	
	private int[] combReadsNumJun(List<List<Double>> lsJunc) {
		int[] result = new int[lsJunc.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) MathComput.sum(lsJunc.get(i));
		}
		return result;
	}
	
	private int[] combReadsNumExpInt(List<List<Double>> lsExp) {
		int[] result = new int[lsExp.get(0).size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
			for (List<Double> ls : lsExp) {
				result[i] += ls.get(i);
			}
		}
		return result;
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
		double chiSquareValue = 0;
		for (int i = 0; i < lsTreat2LsValue.size(); i++) {
			List<Double> lsTreat_OneRepeat = lsTreat2LsValue.get(i);
			List<Double> lsCtrl_OneRepeat = lsCtrl2LsValue.get(i);
			chiSquareValue += TestUtils.chiSquareDataSetsComparison(getDoubleValue(lsTreat_OneRepeat), getDoubleValue(lsCtrl_OneRepeat));
		}
		double df = (lsTreat2LsValue.size()) * (lsTreat2LsValue.get(0).size() ) - 1;
		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
		return 1 - chiSquaredDistribution.cumulativeProbability(chiSquareValue);
	}
	
	private long[] getDoubleValue(List<Double> lsDouble) {
		long[] result = new long[lsDouble.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsDouble.get(i).longValue();
		}
		return result;
	}
	
	
}
