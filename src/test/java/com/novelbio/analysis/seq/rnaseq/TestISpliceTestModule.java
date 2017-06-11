package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.rnaseq.ISpliceTestModule;
import com.novelbio.analysis.seq.rnaseq.ISpliceTestModule.SpliceTestFactory;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.sun.tools.javac.util.Convert;

public class TestISpliceTestModule {
	
	String txtMapCond2Group2Value = "src/test/resources/test_file/AlterSplicing/MapCond2Group2Value";
	String txtSite = "src/test/resources/test_file/AlterSplicing/Site";
	
	@Test
	public void testBalanceUnEqualPair() {
		List<List<Double>> lsPairs = new ArrayList<>();
		lsPairs.add(Lists.newArrayList(12.0, 31.0));
		lsPairs.add(Lists.newArrayList(31.0, 9.0));
		lsPairs.add(Lists.newArrayList(1.0, 9.0));
		lsPairs.add(Lists.newArrayList(13.0, 8.0));
		
		List<List<Double>> lsResult = ISpliceTestModule.balanceUnEqualPair(3,lsPairs);
		List<List<Double>> lsExpect = new ArrayList<>();
		lsExpect.add(Lists.newArrayList(9.25, 25.5));
		lsExpect.add(Lists.newArrayList(23.5, 9.0));
		lsExpect.add(Lists.newArrayList(10.0, 8.25));
		assertLsDouble(lsExpect, lsResult, 0.001);
		
		lsPairs.add(Lists.newArrayList(4.0, 6.0));
		lsResult = ISpliceTestModule.balanceUnEqualPair(3,lsPairs);
		lsExpect = new ArrayList<>();
		lsExpect.add(Lists.newArrayList(8.2, 21.6));
		lsExpect.add(Lists.newArrayList(19.6, 8.4));
		lsExpect.add(Lists.newArrayList(8.8, 7.8));
		assertLsDouble(lsExpect, lsResult, 0.001);
	}
	
	private void assertLsDouble(List<List<Double>> lsExpect, List<List<Double>> lsResult, double delta) {
		Assert.assertEquals(lsExpect.size(), lsResult.size());
		for (int i = 0; i < lsExpect.size(); i++) {
			double[] exps = convert(lsExpect.get(i));
			double[] ress = convert(lsResult.get(i));
			Assert.assertArrayEquals(exps, ress, delta);
		}
	}
	
	private double[] convert(List<Double> exps) {
		double[] dExp = new double[exps.size()];
		for (int j = 0; j < dExp.length; j++) {
			dExp[j] = exps.get(j);
		}
		return dExp;
	}
	
	@Test
	public void testGetMeanValue() {
		List<Number> lsValues = Lists.newArrayList(1,2,3,4,3,45,6,4,2);
		double result = SpliceTestRepeat.getMeanValue(lsValues);
		Assert.assertEquals(3.125, result, 0.0001);
	}
	
	@Test
	public void test() {
		ISpliceTestModule iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		ArrayListMultimap<String, Double> lsExp1 = getMapGroup2LsValue("SiteExp1");
		ArrayListMultimap<String, Double> lsExp2 = getMapGroup2LsValue("SiteExp2");
		Map<String, Map<String, double[]>> mapCond2Group2Values = getMapCond2Group2Value("MapJunc1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		
		double pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
		//================================================
		iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		lsExp1 = getMapGroup2LsValue("SiteJunc1");
		lsExp2 = getMapGroup2LsValue("SiteJunc2");
		mapCond2Group2Values = getMapCond2Group2Value("MapCounts1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
		//================================================
		iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		lsExp1 = getMapGroup2LsValue("SiteExp21");
		lsExp2 = getMapGroup2LsValue("SiteExp22");
		mapCond2Group2Values = getMapCond2Group2Value("MapCounts1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
		//================================================

		iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		lsExp1 = getMapGroup2LsValue("SiteJunc21");
		lsExp2 = getMapGroup2LsValue("SiteJunc22");
		mapCond2Group2Values = getMapCond2Group2Value("MapJunc1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
		//================================================

		iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		lsExp1 = getMapGroup2LsValue("SiteJunc31");
		lsExp2 = getMapGroup2LsValue("SiteJunc32");
		mapCond2Group2Values = getMapCond2Group2Value("MapJunc1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
		//================================================
		iSpliceTest = SpliceTestFactory.createSpliceModule(false);
		lsExp1 = getMapGroup2LsValue("SiteJunc51");
		lsExp2 = getMapGroup2LsValue("SiteJunc52");
		mapCond2Group2Values = getMapCond2Group2Value("MapJunc1");
		iSpliceTest.setMakeSmallValueBigger(true, 80, 2);
		iSpliceTest.setJuncReadsNum(25, 10);

		iSpliceTest.setLsRepeat2Value(mapCond2Group2Values, "Group1", lsExp1, "Group2", lsExp2);
		iSpliceTest.setNormalizedNum(300);
		iSpliceTest.setNormalizedNum(300);
		pvalueExp = iSpliceTest.calculatePvalue();
		System.out.println(pvalueExp);
	}
	
	private Map<String, Map<String,  double[]>> getMapCond2Group2Value(String mapId) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtMapCond2Group2Value);
		boolean isRead = false;
		Map<String, Map<String,  double[]>> mapResult = new HashMap<>();
		for (String content : txtRead.readlines()) {
			if (StringOperate.isEqual("#" +mapId, content)) {
				isRead = true;
				continue;
			}
			if (isRead && StringOperate.isRealNull(content)) break;
			if (!isRead) continue;
			
			String[] ss = content.split("\t");
			Map<String, double[]> mapTmp = mapResult.get(ss[0]);
			if (mapTmp == null) {
				mapTmp = new HashMap<>();
				mapResult.put(ss[0], mapTmp);
			}
			mapTmp.put(ss[1], new double[]{Double.parseDouble(ss[2])});
		}
		txtRead.close();
		return mapResult;
	}
	
	private ArrayListMultimap<String, Double> getMapGroup2LsValue(String siteId) {
		ArrayListMultimap<String, Double> mapResult = ArrayListMultimap.create();
		TxtReadandWrite txtRead = new TxtReadandWrite(txtSite);
		boolean isRead = false;
		PatternOperate patternOperate = new PatternOperate("\\w+=\\[\\d+\\.{0,1}\\d*, \\d+\\.{0,1}\\d*\\]");
		for (String content : txtRead.readlines()) {
			if (StringOperate.isEqual("#" +siteId, content)) {
				isRead = true;
				continue;
			}
			if (!isRead) continue;
			List<String> lsInfo = patternOperate.getPat(content);
			for (String key2Values : lsInfo) {
				String[] ss = key2Values.split("=");
				String key = ss[0].trim();
				String[] values = ss[1].replace("[", "").replace("]", "").split(",");
				for (String value : values) {
					mapResult.put(key, Double.parseDouble(value.trim()));
				}
			} 
			break;
		}
		txtRead.close();
		return mapResult;
	}
}
