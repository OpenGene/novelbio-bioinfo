package com.novelbio.analysis.gwas.combinesnp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.gwas.Allele;
import com.novelbio.analysis.gwas.ExceptionNBCPlink;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.ArrayOperate;

import smile.clustering.KMeans;

public class ClusterKmean {
	/** 输入数据 */
	Map<String, List<Allele>> mapSample2LsAllele = new LinkedHashMap<>();
	/**  聚类后的结果 */
	Map<String, Integer> mapSample2ClusterResult = new LinkedHashMap<>();
	
	public void addSample2LsAllele(String sampleName, List<Allele> lsAllele) {
		mapSample2LsAllele.put(sampleName, lsAllele);
	}
	
	public void cluster() {
		cluster(2);
	}
	
	/**
	 * 如果转成碱基的话，仅支持分两类
	 * @param clusterNum
	 */
	public void cluster(int clusterNum) {
		if (mapSample2LsAllele.values().iterator().next().size() == 1) {
			return;
		}
		double[][] data = generateData();
		KMeans kMeans = new KMeans(data, clusterNum);
		int[] result = kMeans.getClusterLabel();
		boolean isMajor0 = isMajor0(result);
		
		int i = 0;
		for (String sampleName : mapSample2LsAllele.keySet()) {
			int num = result[i++];
			//这个操作是保证0为聚类数量较多的那一项
			if (!isMajor0) {
				num = num == 0 ? 1 : 0;
			}
			mapSample2ClusterResult.put(sampleName, num);
		}
	}
	
	/**
	 * 编号为1的类型是否数量比编号为0的多
	 * @param result
	 * @return
	 */
	private boolean isMajor0(int[] result) {
		int num0 = 0, num1 = 0;
		for (int i : result) {
			if (i == 0) {
				num0++;
			} else if (i == 1) {
				num1++;
			} else {
				throw new RuntimeException();
			}
		}
		return num0 >= num1;
	}
	
	public Map<String, Integer> getMapSample2ClusterResult() {
		return mapSample2ClusterResult;
	}
	/** 返回聚类的结果，用int型 */
	public List<Integer> getClusterResultInt() {
		List<Integer> lsResult = new ArrayList<>();
		for (Integer clusterValue : mapSample2ClusterResult.values()) {
			lsResult.add(clusterValue);
		}
		if (lsResult.isEmpty()) {
			throw new RuntimeException();
		}
		return lsResult;
	}
	
	public List<String[]> getClusterResult() {
		List<String[]> lsResult = new ArrayList<>();
		if (mapSample2LsAllele.values().iterator().next().size() == 1) {
			for (List<Allele> lsAllele : mapSample2LsAllele.values()) {
				lsResult.add(new String[] {lsAllele.get(0).getAllele1(), lsAllele.get(0).getAllele2()});
			}
		} else {
			for (Integer clusterValue : mapSample2ClusterResult.values()) {
				lsResult.add(getResult(clusterValue));
			}
		}
		if (lsResult.isEmpty()) {
			throw new RuntimeException();
		}
		return lsResult;
	}
	
	public Map<String, Allele> getClusterResultMap() {
		Map<String, Allele> mapSample2Allele = new LinkedHashMap<>();
		if (mapSample2LsAllele.values().iterator().next().size() == 1) {
			for (String sample : mapSample2LsAllele.keySet()) {
				mapSample2Allele.put(sample, mapSample2LsAllele.get(sample).get(0));
			}
		} else {
			for (String sample : mapSample2ClusterResult.keySet()) {
				mapSample2Allele.put(sample, getResultAllele(mapSample2ClusterResult.get(sample)));
			}
		}
		if (mapSample2Allele.isEmpty()) {
			throw new RuntimeException();
		}
		return mapSample2Allele;
	}
	
	private double[][] generateData() {
		double[][] data = new double[mapSample2LsAllele.size()][mapSample2LsAllele.values().iterator().next().size()];
		int i = 0;
		for (List<Allele> lsAlleles : mapSample2LsAllele.values()) {
			for (int j = 0; j < lsAlleles.size(); j++) {
				//TODO 如果空缺怎么处理
				data[i][j] = lsAlleles.get(j).getFrq();
			}
			i++;
		}
		return data;
	}
	
	private String[] getResult(int number) {
		if (number == 0) {
			return new String[] {getMajor(), getMajor()};
		} else if (number == 1) {
			return new String[] {getMinor(), getMinor()};
		} else {
			throw new ExceptionNBCPlink("cannot have this condition, number is " + number);
		}
	}
	
	private Allele getResultAllele(int number) {
		Allele allele = new Allele();
		//我也不知道这个该写成什么
		allele.setIndex(mapSample2LsAllele.values().iterator().next().get(0).getIndex());
		List<String> lsMarker = new ArrayList<>();
		for (Allele alleleRaw : mapSample2LsAllele.values().iterator().next()) {
			lsMarker.add(alleleRaw.getMarker());
		}
		allele.setChrID(mapSample2LsAllele.values().iterator().next().get(0).getRefID());
		allele.setMarker(ArrayOperate.cmbString(lsMarker, SepSign.SEP_INFO_SIMPLE));
		allele.setRef(getMajor());
		allele.setAlt(getMinor());
		allele.setIsRefMajor(true);
		if (number == 0) {
			allele.setAllele1(getMajor());
			allele.setAllele2(getMajor());
		} else if (number == 1) {
			allele.setAllele1(getMinor());
			allele.setAllele2(getMinor());
		} else {
			throw new ExceptionNBCPlink("cannot have this condition, number is " + number);
		}
		return allele;
	}
	
	public static String getMajor() {
		return "A";
	}
	public static String getMinor() {
		return "T";
	}
}
