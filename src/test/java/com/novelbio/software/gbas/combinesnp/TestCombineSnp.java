package com.novelbio.software.gbas.combinesnp;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.novelbio.software.gbas.Allele;
import com.novelbio.software.gbas.combinesnp.CombineSnp;

import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.Linkage;
import smile.clustering.linkage.UPGMALinkage;

public class TestCombineSnp {
	
	@Test
	public void testCalculateR2fromAlleles() {
		Map<String, List<Allele>> mapSample2LsAlleles = new HashMap<>();
		mapSample2LsAlleles.put("s1", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s2", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s3", getSampleLsAllele("AA BB AA BB"));

		mapSample2LsAlleles.put("s4", getSampleLsAllele("AA Bb AA BB"));
		mapSample2LsAlleles.put("s5", getSampleLsAllele("AA Bb AA BB"));
		mapSample2LsAlleles.put("s6", getSampleLsAllele("AA Bb AA BB"));
		
		mapSample2LsAlleles.put("s7", getSampleLsAllele("Aa BB AA BB"));
		mapSample2LsAlleles.put("s8", getSampleLsAllele("Aa BB AA BB"));
		mapSample2LsAlleles.put("s9", getSampleLsAllele("Aa BB AA BB"));
		
		mapSample2LsAlleles.put("s10", getSampleLsAllele("Aa Bb Aa BB"));
		mapSample2LsAlleles.put("s11", getSampleLsAllele("Aa Bb Aa Bb"));
		mapSample2LsAlleles.put("s12", getSampleLsAllele("Aa Bb Aa Bb"));
		
		CombineSnp combineSnp = new CombineSnp();
		combineSnp.setMapSample2LsAlleles(mapSample2LsAlleles);
		double[][] distance = combineSnp.calculateDistanceFromAlleles();
		double[][] distanceExp = new double[4][4];
		distanceExp[0] = new double[] {0, 1, 0.667, 0.8};
		distanceExp[1] = new double[] {1, 0, 0.667, 0.8};
		distanceExp[2] = new double[] {0.667, 0.667, 0, 0.4};
		distanceExp[3] = new double[] {0.8, 0.8, 0.4, 0};
		for (int i = 0; i < 4; i++) {
			assertArrayEquals(distanceExp[i], distance[i], 0.01);
		}
		Linkage linkage = new UPGMALinkage(distance);
		HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(new UPGMALinkage(distance));
		int[] num = hierarchicalClustering.partition(3);
		int[] num2 = hierarchicalClustering.partition(0.2);

		double[] high = hierarchicalClustering.getHeight();
		int[][] result = hierarchicalClustering.getTree();
		System.out.println();
	}
	
	@Test
	public void testCalculateR2fromAlleles2() {
		Map<String, List<Allele>> mapSample2LsAlleles = new HashMap<>();
		mapSample2LsAlleles.put("s1", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s2", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s3", getSampleLsAllele("AA BB AA BB"));

		mapSample2LsAlleles.put("s4", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s5", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s6", getSampleLsAllele("AA Bb AA Bb"));
		
		mapSample2LsAlleles.put("s7", getSampleLsAllele("Aa BB Aa BB"));
		mapSample2LsAlleles.put("s8", getSampleLsAllele("Aa BB Aa BB"));
		mapSample2LsAlleles.put("s9", getSampleLsAllele("Aa BB Aa BB"));
		
		mapSample2LsAlleles.put("s10", getSampleLsAllele("Aa Bb Aa Bb"));
		mapSample2LsAlleles.put("s11", getSampleLsAllele("Aa Bb Aa Bb"));
		mapSample2LsAlleles.put("s12", getSampleLsAllele("Aa Bb Aa Bb"));
		
		CombineSnp combineSnp = new CombineSnp();
		combineSnp.setMapSample2LsAlleles(mapSample2LsAlleles);
		double[][] distance = combineSnp.calculateDistanceFromAlleles();
//		double[][] distanceExp = new double[4][4];
//		distanceExp[0] = new double[] {0, 1, 0.667, 0.8};
//		distanceExp[1] = new double[] {1, 0, 0.667, 0.8};
//		distanceExp[2] = new double[] {0.667, 0.667, 0, 0.4};
//		distanceExp[3] = new double[] {0.8, 0.8, 0.4, 0};
//		for (int i = 0; i < 4; i++) {
//			assertArrayEquals(distanceExp[i], distance[i], 0.01);
//		}
		Linkage linkage = new UPGMALinkage(distance);
		HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(new UPGMALinkage(distance));
		int[] num = hierarchicalClustering.partition(3);
		int[] num2 = hierarchicalClustering.partition(0.2);

		double[] high = hierarchicalClustering.getHeight();
		int[][] result = hierarchicalClustering.getTree();
		System.out.println();
	}
	
	@Test
	public void testCalculateR2fromAlleles3() {
		Map<String, List<Allele>> mapSample2LsAlleles = new HashMap<>();
		mapSample2LsAlleles.put("s1", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s2", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s3", getSampleLsAllele("AA BB AA BB"));

		mapSample2LsAlleles.put("s4", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s5", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s6", getSampleLsAllele("AA Bb AA Bb"));
		
		mapSample2LsAlleles.put("s7", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s8", getSampleLsAllele("AA BB AA BB"));
		mapSample2LsAlleles.put("s9", getSampleLsAllele("AA BB AA BB"));
		
		mapSample2LsAlleles.put("s10", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s11", getSampleLsAllele("AA Bb AA Bb"));
		mapSample2LsAlleles.put("s12", getSampleLsAllele("AA Bb AA Bb"));
		
		CombineSnp combineSnp = new CombineSnp();
		combineSnp.setMapSample2LsAlleles(mapSample2LsAlleles);
		double[][] distance = combineSnp.calculateDistanceFromAlleles();
//		double[][] distanceExp = new double[4][4];
//		distanceExp[0] = new double[] {0, 1, 0.667, 0.8};
//		distanceExp[1] = new double[] {1, 0, 0.667, 0.8};
//		distanceExp[2] = new double[] {0.667, 0.667, 0, 0.4};
//		distanceExp[3] = new double[] {0.8, 0.8, 0.4, 0};
//		for (int i = 0; i < 4; i++) {
//			assertArrayEquals(distanceExp[i], distance[i], 0.01);
//		}
		Linkage linkage = new UPGMALinkage(distance);
		HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(new UPGMALinkage(distance));
		int[] num = hierarchicalClustering.partition(2);
		int[] num2 = hierarchicalClustering.partition(0.2);

		double[] high = hierarchicalClustering.getHeight();
		int[][] result = hierarchicalClustering.getTree();
		System.out.println();
	}
	
	/**
	 * @param alleles AA AT TA
	 * 其中第一个是 ref，第二个是实际的碱基
	 * @return
	 */
	private List<Allele> getSampleLsAllele(String alleles) {
		List<Allele> lsResult = new ArrayList<>();
		String[] ss = alleles.split(" ");
		for (String unit : ss) {
			Allele allele = new Allele();
			char[] chararray = unit.toCharArray();
			allele.setAllele1(chararray[1]);
			allele.setAllele2(chararray[1]);
			allele.setRef(chararray[0]);
			lsResult.add(allele);
		}
		return lsResult;
	}
	
}
