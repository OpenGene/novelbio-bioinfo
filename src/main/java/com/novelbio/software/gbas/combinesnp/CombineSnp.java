package com.novelbio.software.gbas.combinesnp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.software.gbas.Allele;
import com.novelbio.software.gbas.Permutation;

import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.Linkage;
import smile.clustering.linkage.UPGMALinkage;

/**
 * 将连锁的位点合并为一个，这里采用 Hierarchical Clustering 进行聚类，距离使用LD r^2 
 * 然后将待合并的位点用kmeans聚类，获取聚类后的位点
 * @author zong0jie
 * @data 2018年6月3日
 */
public class CombineSnp {
	private static final Logger logger = LoggerFactory.getLogger(CombineSnp.class);
	
	Map<String, List<Allele>> mapSample2LsAllelesIn;
	Map<String, List<Allele>> mapSample2LsAllelesOut = new LinkedHashMap<>();

	/** 相似度小于0.2的合并在一起，这里的0.2是1-r^2 */
	int[] clusters;
	
	/** 聚类的相似度，越小表示相似度越高 */
	double[] height;
	
	double r2;
	int maxClusterNum;
	
	double variationCutoff = 0.05;
	
	String geneName;
	
	/** 如果N太多，是否需要将N替换成别的碱基，因为N多很可能意味着这里缺失
	 * 而直接过滤效果不佳
	 */
	boolean isChangeN = true;
	
	public void setChangeN(boolean isChangeN) {
		this.isChangeN = isChangeN;
	}
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	/** r平方超过多少的聚在一起 */
	public void setR2(double r2) {
		if (r2 > 1 || r2 < 0) {
			r2 = 0;
		}
		this.r2 = r2;
	}
	public void setVariationCutoff(double variationCutoff) {
		this.variationCutoff = variationCutoff;
	}
	/**
	 * 聚类最多多少个cluster
	 * 因为就算聚类之后，依然会出现很多个cluster以至于后面很难分析
	 * 因此这里可以强行设定cluster
	 */
	public void setMaxClusterNum(int maxClusterNum) {
		this.maxClusterNum = maxClusterNum;
	}
	
	public void setMapSample2LsAlleles(Map<String, List<Allele>> mapSample2LsAllelesIn) {
		this.mapSample2LsAllelesIn = mapSample2LsAllelesIn;
		for (String sample : mapSample2LsAllelesIn.keySet()) {
			mapSample2LsAllelesOut.put(sample, new ArrayList<>());
		}
	}
	
	public void modifyMapSample2LsAlleles(Map<String, List<Allele>> mapSample2LsAllelesIn) {
		for (String sample : mapSample2LsAllelesIn.keySet()) {
			mapSample2LsAllelesOut.put(sample, new ArrayList<>());
		}
	}
	
	public Map<String, List<Allele>> getMapSample2LsAllelesResult() {
		return mapSample2LsAllelesOut;
	}
	
	public void merge() {
		double[][] distances = calculateDistanceFromAlleles();
		if (distances == null) {
			mapSample2LsAllelesOut = mapSample2LsAllelesIn;
			return;
		}
		clustering(distances);
		mergeSnps();
	}
	
	/** 计算某个基因其snp的ld的r平方，用于聚类 */
	@VisibleForTesting
	protected double[][] calculateDistanceFromAlleles() {
		List<List<String[]>> lsInfosRaw = new ArrayList<>();
		int alleleNum = mapSample2LsAllelesIn.values().iterator().next().size();
		for (int i = 0; i < alleleNum; i++) {
			lsInfosRaw.add(new ArrayList<>());
		}
		//================= 按照snp位点产生了一个snp一个list这种 =========
		for (String sample : mapSample2LsAllelesIn.keySet()) {
			List<Allele> lsAllelesSample = mapSample2LsAllelesIn.get(sample);
			for (int i = 0; i < lsAllelesSample.size(); i++) {
				Allele allele = lsAllelesSample.get(i);
				lsInfosRaw.get(i).add(new String[] {allele.getAllele1(), allele.getAllele2()});
			}
		}
		
		List<Allele> lsAllelesRaw = mapSample2LsAllelesIn.values().iterator().next();
		List<Allele> lsAlleles = new ArrayList<>();
		List<List<String[]>> lsInfos = new ArrayList<>();
		//将list中没有变异的位点去除
		List<Integer> lsSnpsNeed = new ArrayList<>();
		
		for (int i = 0; i < lsInfosRaw.size(); i++) {
			List<String[]> lsSite = lsInfosRaw.get(i);
			Allele allele = lsAllelesRaw.get(i).clone();
			if (allele.getStartAbs() == 3514 || allele.getStartAbs() == 3568) {
				logger.info("stop");
			}
			//这里不过滤
			lsSite = modifyList(lsSite, allele, variationCutoff, true);
			if (lsSite != null) {
				lsSnpsNeed.add(i);
				lsInfos.add(lsSite);
				lsAlleles.add(allele);
			}
		}
		
		int maxSnpInGene = 500;
		if (lsSnpsNeed.size() > maxSnpInGene) {
			List<List<String[]>> lsNeedForSnpInfo = new ArrayList<>();
			logger.info("gene {} have too many snps {}, so reduce it", geneName, lsSnpsNeed.size());
			List<Integer> lsSnpNeedNew = new ArrayList<>();
			int skipNum = (int)Math.round((double)lsSnpsNeed.size()/500);
			for (int i = 0; i < lsSnpsNeed.size(); i+=skipNum) {
				lsSnpNeedNew.add(lsSnpsNeed.get(i));
				lsNeedForSnpInfo.add(lsInfos.get(i));
			}
			lsInfos = lsNeedForSnpInfo;
			lsSnpsNeed = lsSnpNeedNew;
		}

		mapSample2LsAllelesIn = filterMapSample2LsAllele(mapSample2LsAllelesIn, lsSnpsNeed);
		if (lsAlleles.size() <= 1) {
			return null;
		}
		//================== 开始计算相关性  =============================
		int snpNum = lsSnpsNeed.size();
		double[][] distance = new double[snpNum][snpNum];
		for (int i = 0; i < snpNum-1; i++) {
			distance[i][i] = 0;
			for (int j = i+1; j < snpNum; j++) {
				LDcalculate lDcalculate = new LDcalculate();
				List<String[]> lsRef = lsInfos.get(i);
				List<String[]> lsAlt = lsInfos.get(j);
				Allele alleleRef = lsAlleles.get(i);
				Allele alleleAlt = lsAlleles.get(j);

				lDcalculate.setLsRef2AltSite1(lsRef);
				lDcalculate.setLsRef2AltSite2(lsAlt);

				lDcalculate.calculate();
				distance[i][j] = 1-lDcalculate.getMaxR2Ddot();
				distance[j][i] = 1-lDcalculate.getMaxR2Ddot();
				if ((distance[i][j]+"").equals("NaN")) {
					System.out.println(getSnps(lsRef));
					System.out.println(getSnps(lsAlt));

					
					logger.info("stop");
					
					lDcalculate = new LDcalculate();
					lsRef = lsInfos.get(i);
					lsAlt = lsInfos.get(j);
					alleleRef = lsAlleles.get(i);
					alleleAlt = lsAlleles.get(j);

					lDcalculate.setLsRef2AltSite1(lsRef);
					lDcalculate.setLsRef2AltSite2(lsAlt);
					lDcalculate.calculate();
					distance[i][j] = 1-lDcalculate.getMaxR2Ddot();
					distance[j][i] = 1-lDcalculate.getMaxR2Ddot();
					
				}
			}
		}
		distance[snpNum-1][snpNum-1] = 0;
		return distance;
	}
	
	private String getSnps(List<String[]> lsSnps) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(lsSnps.get(0)[0]);
		for (int i = 1; i < lsSnps.size(); i++) {
			stringBuilder.append(" " + lsSnps.get(i)[0]);
		}
		return stringBuilder.toString();
	}
	
	private Map<String, List<Allele>> filterMapSample2LsAllele(Map<String, List<Allele>> mapSample2LsAlleles,List<Integer> lsSnpsNeed) {
		Map<String, List<Allele>> mapSample2LsAllelesFiltered = new LinkedHashMap<>();
		for (String sampleName : mapSample2LsAlleles.keySet()) {
			List<Allele> lsAlleles = mapSample2LsAlleles.get(sampleName);
			List<Allele> lsAllelesFilter = new ArrayList<>();
			for (Integer index : lsSnpsNeed) {
				lsAllelesFilter.add(lsAlleles.get(index));
			}
			mapSample2LsAllelesFiltered.put(sampleName, lsAllelesFilter);
		}
		return mapSample2LsAllelesFiltered;
	}
	
	/**
	 * 过滤，如果本位点仅有一个位点，则返回null
	 * 如果位点排序发现N的数量更多，则将N替换为变异位点
	 * @param lsSite
	 * @param allele
	 * @return
	 */
	public static List<String[]> modifyList(List<String[]> lsSite, Allele allele, double variationCutoff, boolean isChangeN) {
		Map<String, int[]> mapAllele2Num = new HashMap<>();
		for (String[] alleles : lsSite) {
			int[] num = mapAllele2Num.get(alleles[0]);
			if (num == null) {
				num = new int[]{0};
				mapAllele2Num.put(alleles[0], num);
			}
			num[0]++;
		}
		if (mapAllele2Num.size() == 1) {
			return null;
		}
		
		
		List<String[]> lsSite2Num = new ArrayList<>();
		for (String alleleStr : mapAllele2Num.keySet()) {
			lsSite2Num.add(new String[] {alleleStr, mapAllele2Num.get(alleleStr)[0]+""});
		}
		Collections.sort(lsSite2Num, (site2Num1, site2Num2) ->{
			Integer site1 = Integer.parseInt(site2Num1[1]);
			Integer site2 = Integer.parseInt(site2Num2[1]);
			return -site1.compareTo(site2);
		} );
		int lessSnpNum = Integer.parseInt(lsSite2Num.get(1)[1]);
		if ((double)lessSnpNum/lsSite.size() < variationCutoff) {
			return null;
		}
		
		if (!isChangeN) {
			int alleleNum = 0;
			for (String alleleStr : mapAllele2Num.keySet()) {
				if (alleleStr.equals("N") || alleleStr.equals("0")) {
					continue;
				}
				alleleNum++;
			}
			if (variationCutoff > 0 && alleleNum < 2) {
				return null;
			}
			if (alleleNum > 2) {
				throw new RuntimeException("allele error");
			}
			
			int small = 10000000;
			int sum = 0;
	
			for (String alleleStr : mapAllele2Num.keySet()) {
				if (alleleStr.equals("N") || alleleStr.equals("0")) {
					continue;
				}
				if (small > mapAllele2Num.get(alleleStr)[0]) {
					small = mapAllele2Num.get(alleleStr)[0];
				}
				sum+=mapAllele2Num.get(alleleStr)[0];
			}
			if ((double)small/sum < variationCutoff) {
				return null;
			}
			return lsSite;
		}
		
		String site1 = lsSite2Num.get(0)[0];
		String site2 = lsSite2Num.get(1)[0];
		if ((site2.equals("N") || site2.equals("0")) && lsSite2Num.size() > 2) {
			int numN = Integer.parseInt(lsSite2Num.get(1)[1]);
			int numOther = Integer.parseInt(lsSite2Num.get(2)[1]);
			//如果N和另一个位点数量差不多，相差小于2倍，就维持不变
			if (numN <= numOther * 2) {
				site2 = lsSite2Num.get(2)[0];
			}
		}
		if (allele.getAltBase() == null) {
			if (allele.getRefBase().equals(site1)) {
				allele.setAlt(site2);
			} else {
				allele.setAlt(site1);
			}
		}
		//正常情况
		if (site1.equals(allele.getRefBase()) && site2.equals(allele.getAltBase())
			|| site1.equals(allele.getAltBase()) && site2.equals(allele.getRefBase())
			) {
			return lsSite;
		}
		if (site1.equals(allele.getRefBase())) {
			//change site2 to alt
			for (String[] strings : lsSite) {
				if (strings[0].equals(site1)) {
					continue;
				} else if (strings[0].equals(site2)) {
					strings[0] = allele.getAltBase();
					strings[1] = allele.getAltBase();
				} else {
					strings[0] = "N";
					strings[1] = "N";
				}
			}
		} else if (site1.equals(allele.getAltBase())) {
			//change site2 to ref
			for (String[] strings : lsSite) {
				if (strings[0].equals(site1)) {
					continue;
				} else if (strings[0].equals(site2)) {
					strings[0] = allele.getRefBase();
					strings[1] = allele.getRefBase();
				} else {
					strings[0] = "N";
					strings[1] = "N";
				}
			}
		} else if (site2.equals(allele.getRefBase())) {
			// change site1 to alt
			for (String[] strings : lsSite) {
				if (strings[0].equals(site2)) {
					continue;
				} else if (strings[0].equals(site1)) {
					strings[0] = allele.getAltBase();
					strings[1] = allele.getAltBase();
				} else {
					strings[0] = "N";
					strings[1] = "N";
				}
			}
		} else if (site2.equals(allele.getAltBase())) {
			// change site1 to ref
			for (String[] strings : lsSite) {
				if (strings[0].equals(site2)) {
					continue;
				} else if (strings[0].equals(site1)) {
					strings[0] = allele.getRefBase();
					strings[1] = allele.getRefBase();
				} else {
					strings[0] = "N";
					strings[1] = "N";
				}
			}
		} else {
			// change site1 to ref
			// change site2 to alt
			for (String[] strings : lsSite) {
				if (strings[0].equals(site1)) {
					strings[0] = allele.getRefBase();
					strings[1] = allele.getRefBase();
				} else if (strings[0].equals(site2)) {
					strings[0] = allele.getAltBase();
					strings[1] = allele.getAltBase();
				} else {
					strings[0] = "N";
					strings[1] = "N";
				}
			}
		}
		return lsSite;
	}
	
	private void clustering(double[][] distances) {
		Linkage linkage = new UPGMALinkage(distances);
		HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(linkage);
		//相似度小于0.2的合并在一起，这里的0.2是1-r^2
		double heightSet = 1-r2;
		height = hierarchicalClustering.getHeight();
		if (heightSet > height[height.length - 1]) {
			heightSet = height[height.length-1];
		}
		int[] clusters = null;
		try {
			clusters = hierarchicalClustering.partition(heightSet);
		} catch (Exception e) {
			List<Allele> lsAlleles = mapSample2LsAllelesIn.values().iterator().next();
			logger.error(lsAlleles.get(0).toString());
			logger.error(lsAlleles.get(lsAlleles.size()-1).toString());
			System.out.println();
			System.err.println(ArrayOperate.cmbString(height, "\t"));
			System.err.println("distance is:");
			for (double[] info : distances) {
				System.err.println(ArrayOperate.cmbString(info, "\t"));
			}
			try {
				clusters = hierarchicalClustering.partition(maxClusterNum);
			} catch (Exception e2) {
				throw e2;
			}
		
		}
		int clusterNum = getClusterNum(clusters);
		
		if (maxClusterNum > 0 && clusterNum > maxClusterNum) {
			for (double heightUnit : height) {
				if (heightUnit <= heightSet) {
					continue;
				}
				if (heightUnit >= 1) {
					break;
				}
				clusters = hierarchicalClustering.partition(heightUnit);
				if (getClusterNum(clusters) <= maxClusterNum) {
					break;
				}
			}
		}
		this.clusters = clusters;
	}
	
	private int getClusterNum(int[] clusters) {
		Set<Integer> setInt = new HashSet<>();
		for (Integer integer : clusters) {
			setInt.add(integer);
		}
		return setInt.size();
	}
	
	private void mergeSnps() {
		ArrayListMultimap<Integer, Integer> mapCluster2Indexs = ArrayListMultimap.create();
		for (int i = 0; i < clusters.length; i++) {
			mapCluster2Indexs.put(clusters[i], i);
		}
		for (Integer cluster : mapCluster2Indexs.keySet()) {
			List<Integer> lsIndexs = mapCluster2Indexs.get(cluster);
			if (lsIndexs.size() == 1) {
				for (String sample : mapSample2LsAllelesIn.keySet()) {
					Allele allele = mapSample2LsAllelesIn.get(sample).get(lsIndexs.get(0));
					List<Allele> lsAlleles = mapSample2LsAllelesOut.get(sample);
					lsAlleles.add(allele);
				}
			} else {
				combineSnpByIndexs(lsIndexs);
			}
		}
	}
	
	private void combineSnpByIndexs(List<Integer> lsIndex) {
		ClusterKmean clusterKmean = new ClusterKmean();
		for (String sampleName : mapSample2LsAllelesIn.keySet()) {
			clusterKmean.addSample2LsAllele(sampleName, Permutation.getLsAlleleFromLsIndex(mapSample2LsAllelesIn.get(sampleName), lsIndex));
		}
		clusterKmean.cluster();
		Map<String, Allele> mapSample2CombineSnp = clusterKmean.getClusterResultMap();
		for (String sample : mapSample2LsAllelesIn.keySet()) {
			Allele allele = mapSample2CombineSnp.get(sample);
			List<Allele> lsAlleles = mapSample2LsAllelesOut.get(sample);
			lsAlleles.add(allele);
		}
	}
	
}
