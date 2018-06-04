package com.novelbio.analysis.gwas.combinesnp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.gwas.Allele;
import com.novelbio.analysis.gwas.Permutation;
import com.novelbio.base.dataStructure.ArrayOperate;

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
	/** r平方超过多少的聚在一起 */
	public void setR2(double r2) {
		if (r2 > 1 || r2 < 0) {
			r2 = 0;
		}
		this.r2 = r2;
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
			//TODO
			mapSample2LsAllelesOut.put(sample, new ArrayList<>());
		}
	}
	
	public Map<String, List<Allele>> getMapSample2LsAllelesResult() {
		return mapSample2LsAllelesOut;
	}
	
	public void merge() {
		double[][] distances = calculateDistanceFromAlleles();
		clustering(distances);
		mergeSnps();
	}
	
	/** 计算某个基因其snp的ld的r平方，用于聚类 */
	@VisibleForTesting
	protected double[][] calculateDistanceFromAlleles() {
		List<List<String[]>> lsInfos = new ArrayList<>();
		int alleleNum = mapSample2LsAllelesIn.values().iterator().next().size();
		for (int i = 0; i < alleleNum; i++) {
			lsInfos.add(new ArrayList<>());
		}
		//================= 按照snp位点产生了一个snp一个list这种 =========
		for (String sample : mapSample2LsAllelesIn.keySet()) {
			List<Allele> lsAllelesSample = mapSample2LsAllelesIn.get(sample);
			for (int i = 0; i < lsAllelesSample.size(); i++) {
				Allele allele = lsAllelesSample.get(i);
				lsInfos.get(i).add(new String[] {allele.getAllele1(), allele.getAllele2()});
			}
		}
		//================== 开始计算相关性  =============================
		List<Allele> lsAlleles = mapSample2LsAllelesIn.values().iterator().next();
		int snpNum = lsAlleles.size();
		double[][] distance = new double[snpNum][snpNum];
		for (int i = 0; i < snpNum-1; i++) {
			distance[i][i] = 0;
			for (int j = i+1; j < snpNum; j++) {
				LDcalculate lDcalculate = new LDcalculate();
				lDcalculate.setLsRef2AltSite1(lsInfos.get(i));
				lDcalculate.setLsRef2AltSite2(lsInfos.get(j));
				lDcalculate.setRefa(lsAlleles.get(i).getRefBase());
				lDcalculate.setRefb(lsAlleles.get(j).getRefBase());
				lDcalculate.calculate();
				distance[i][j] = 1-lDcalculate.getR2();
				distance[j][i] = 1-lDcalculate.getR2();
			}
		}
		distance[snpNum-1][snpNum-1] = 0;
		return distance;
	}
	
	private void clustering(double[][] distances) {
		Linkage linkage = new UPGMALinkage(distances);
		HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(linkage);
		//相似度小于0.2的合并在一起，这里的0.2是1-r^2
		height = hierarchicalClustering.getHeight();
		int[] clusters = null;
		try {
			clusters = hierarchicalClustering.partition(1-r2);
		} catch (Exception e) {
			List<Allele> lsAlleles = mapSample2LsAllelesIn.values().iterator().next();
			logger.error(lsAlleles.get(0).toString());
			logger.error(lsAlleles.get(lsAlleles.size()-1).toString());

			System.err.println(ArrayOperate.cmbString(height, "\t"));
			System.err.println("distance is:");
			for (double[] info : distances) {
				System.err.println(ArrayOperate.cmbString(info, "\t"));
			}
			throw e;
		}
		int clusterNum = getClusterNum(clusters);
		
		if (maxClusterNum > 0 && clusterNum > maxClusterNum) {
			for (double heightUnit : height) {
				if (heightUnit <= 1-r2) {
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
