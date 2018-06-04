package com.novelbio.analysis.gwas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.novelbio.analysis.gwas.combinesnp.ClusterKmean;
import com.novelbio.analysis.gwas.combinesnp.CombineSnp;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

import smile.clustering.KMeans;

public class TestGwas {
	private static final Logger logger = LoggerFactory.getLogger(TestGwas.class);
	
	public static void main1(String[] args) {
		String chrFile = "/home/novelbio/test/plink/chrAll.fa";
//		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/test/plink/realdata/NB_final_snp.bim");
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/test/plink/realdata/NB_final_snp.bim.addchr", true);
//		for (String content : txtRead.readlines()) {
//			txtWrite.writefileln("chr"+content);
//		}
//		txtRead.close();
//		txtWrite.close();
//		PlinkBimChangeBase plinkBimChangeBase = new PlinkBimChangeBase(chrFile);
//		plinkBimChangeBase.addAnnoFromRef("/home/novelbio/test/plink/realdata/NB_final_snp.bim.addchr", "/home/novelbio/test/plink/realdata/NB_final_snp.bim.addchr.addbase");
		
//		String chrFile = "/home/novelbio/test/plink/chrAll.fa";
		String gffFile = "/home/novelbio/test/plink/all.gff3";
		
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		SnpAnnoFactory snpAnnoFactory = new SnpAnnoFactory();
		snpAnnoFactory.setGffChrAbs(gffChrAbs);
		
		SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo("chr8", 13041137, "T", "C");
		System.out.print(snpInfo.getMapIso2Hgvsp().values().iterator().next().getHgvsp());
	}
	
	public static void main2(String[] args) {
		String plinkBim = "/home/novelbio/test/plink/619-40maf.addchr.bim";
		String plinkPed =  "/home/novelbio/test/plink/plink.ped";
		String plinkBimCorrect = "/home/novelbio/test/plink/realdata/NB_final_snp.bim.addchr.addbase";
//		String plinkBimNeedSnpIndex = "/home/novelbio/test/plink/plink.bim.index";

		String chrFile = "/home/novelbio/test/plink/chrAll.fa";
		String gffFile = "/home/novelbio/test/plink/all.gff3";
		
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		PlinkMapReader plinkMapReader = new PlinkMapReader();
		plinkMapReader.setGffChrAbs(gffChrAbs);
		plinkMapReader.setPlinkMap(plinkBimCorrect);
		plinkMapReader.initial();
		
		Map<Integer, int[]> mapNum = new TreeMap<>();
		
		SnpAnnoFactory snpAnnoFactory = new SnpAnnoFactory();
		snpAnnoFactory.setGffChrAbs(gffChrAbs);
		while (plinkMapReader.readNext()) {
			Map<Allele, Set<String>> mapSnp2SetIsoName = plinkMapReader.getLsAllelesCurrentGene();
			if (mapSnp2SetIsoName.isEmpty()) {
				continue;
			}
			String geneName = plinkMapReader.getGeneCurrent().getNameSingle();
			if (mapSnp2SetIsoName.size() > 8) {
				int num = mapSnp2SetIsoName.size();
				if (!mapNum.containsKey(num)) {
					int[] value = new int[1];
					value[0] = 0;
					mapNum.put(num, value);
				}
				int[] value = mapNum.get(num);
				value[0] += 1;
				System.out.println(geneName + "\t" + mapSnp2SetIsoName.size());
				for (Allele allele : mapSnp2SetIsoName.keySet()) {
					System.out.print(allele+ "\t:\t");
					SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo(allele.getRefID(), allele.getPosition(), allele.getRefBase(), allele.getAltBase());
					System.out.print(snpInfo.getMapIso2Hgvsc().values().iterator().next().getHgvsc());
					System.out.print("\t");
					if (snpInfo.getMapIso2Hgvsp().values().iterator().next().isNeedHgvsp()) {
						try {
							System.out.print(snpInfo.getMapIso2Hgvsp().values().iterator().next().getHgvsp());
						} catch (Exception e) {
							System.out.print("anno_error");
						}
					}
					System.out.println();
				}
			}
		}
		for (Integer num : mapNum.keySet()) {
			System.out.println(num + "\t" + mapNum.get(num)[0]);
		}
	}
	
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			printHelp();
			System.exit(1);
		}
		for (String string : args) {
			if (string.toLowerCase().contains("help")) {
				printHelp();
				System.exit(1);
			}
		}
		
		Options opts = new Options();
		opts.addOption("r2min", true, "r2min");
		opts.addOption("maxCluster", true, "maxCluster");
		opts.addOption("permutationNum", true, "permutationNum");
		
		opts.addOption("plinkBim", true, "plinkBim");
		opts.addOption("plinkPed", true, "plinkPed");
		opts.addOption("chrFile", true, "chrFile");
		opts.addOption("gffFile", true, "gffFile");
		opts.addOption("out", true, "out");

		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			printHelp();
			System.exit(1);
		}
		
		String plinkBim = cliParser.getOptionValue("plinkBim", "");
		String plinkPed = cliParser.getOptionValue("plinkPed", "");
		String chrFile = cliParser.getOptionValue("chrFile", "");
		String gffFile = cliParser.getOptionValue("gffFile", "");
		String out = cliParser.getOptionValue("out", "");
		String r2min = cliParser.getOptionValue("r2min", "");
		String maxCluster = cliParser.getOptionValue("maxCluster", "");
		String permutationNum = cliParser.getOptionValue("permutationNum", "");

		
		FileOperate.createFolders(FileOperate.getPathName(out));
		String plinkBimCorrect = FileOperate.changeFileSuffix(plinkBim, ".correct", null);

//		String plinkBim = "/home/novelbio/test/hongjun-gwas/619-40maf.bim";
//		String plinkPed =  "/home/novelbio/test/hongjun-gwas/plink.ped";
//		String plinkBimCorrect = "/home/novelbio/test/hongjun-gwas/619-40maf.bim.anno";
//		
//		String chrFile = "/home/novelbio/test/hongjun-gwas/chrome/oryza_sativa.IRGSP-1.0.dna.fa";
//		String gffFile = "/home/novelbio/test/hongjun-gwas/chrome/Oryza_sativa.IRGSP-1.0.39.gff3";
//		String out = "/home/novelbio/test/hongjun-gwas/result/";
//		String r2min = "0.8";
//		String maxCluster = "10";
//		String permutationNum = "3";
		
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		
		PlinkPedReader.createPlinkPedIndex(plinkPed);
		if (!FileOperate.isFileExistAndBigThan0(plinkBimCorrect)) {
			PlinkBimChangeBase plinkMapAddBase = new PlinkBimChangeBase(chrFile);
			plinkMapAddBase.addAnnoFromRef(plinkBim, plinkBimCorrect);
		}
		
		System.out.println("finish anno");
		TestGwas testGwas = new TestGwas();
		testGwas.setGffChrAbs(gffChrAbs);
		testGwas.setPlinkPed(plinkPed);
		testGwas.setPlinkBimCorrect(plinkBimCorrect);
		if (!StringOperate.isRealNull(r2min)) {
			testGwas.setR2Cluster(Double.parseDouble(r2min.trim()));
		}
		if (!StringOperate.isRealNull(maxCluster)) {
			testGwas.setMaxCluster(Integer.parseInt(maxCluster.trim()));
		}
		if (!StringOperate.isRealNull(permutationNum)) {
			testGwas.setPermutationNum(Integer.parseInt(permutationNum.trim()));
		}
		
		testGwas.setOutput(out + "permutation.plinkmap", out + "permutation.plinkmapconvertor", out + "permutation.plinkped.pre");
		testGwas.cluster();
		System.out.println("finish permutation");
		try {
			GwasFormat.convertPlinkCsv2plinkPed(out + "permutation.plinkped.pre", out + "permutation.plink.ped");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("finish");
	}
	
	private static void printHelp() {
		System.err.println("java -jar testGwas.jar --plinkBim plink.bim --plinkPed plink.ped --r2min 0.8 --maxCluster 10 --permutationNum 3  --chrFile chrFile --gffFile gffFile --out outPath");
		System.err.println();
		System.err.println("r2min: cluster the snp have r2 bigger than the value, default is 0.2");
		System.err.println("maxCluster: cluster num less then the value, default is 0, means no cluster number limit");
		System.err.println("permutationNum: number n in c(m,n), default is 3, while 0 means n=m");
		System.err.println();
		System.err.println("example:");
		System.err.println("java -jar testGwas.jar --plinkBim /home/novelbio/plink.bim --r2min 0.8 --maxCluster 10 --permutationNum 3 --plinkPed /home/novelbio/plink.ped"
				+ " --chrFile /home/novelbio/chrFile --gffFile /home/novelbio/gffFile --out /home/novelbio/permutation");
	}
	
	PlinkPedReader plinkPedReader;

	GffChrAbs gffChrAbs;
	String plinkBimCorrect;
	String plinkPed;

	String plinkMapOut;
	String plinkMapConvertorOut;
	String plinkPedPreOut;
	
	//=======聚类专用===============
	/** 聚类时r2超过这个值的聚在一起 */
	double r2Cluster = 0.2;
	int maxCluster = 0;
	int permutationNum=3;
	//==============================
	
	/**
	 * 聚类时r2超过这个值的聚在一起 
	 * @param r2Cluster
	 */
	public void setR2Cluster(double r2Cluster) {
		this.r2Cluster = r2Cluster;
	}
	public void setMaxCluster(int maxCluster) {
		this.maxCluster = maxCluster;
	}
	public void setPermutationNum(int permutationNum) {
		this.permutationNum = permutationNum;
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setPlinkBimCorrect(String plinkBimCorrect) {
		this.plinkBimCorrect = plinkBimCorrect;
	}
	public void setPlinkPed(String plinkPed) {
		this.plinkPed = plinkPed;
	}
	
	public void setOutput(String plinkMap, String plinkMapConvertor, String plinkPedPre) {
		this.plinkMapOut = plinkMap;
		this.plinkMapConvertorOut = plinkMapConvertor;
		this.plinkPedPreOut = plinkPedPre;
	}
	
	protected void cluster() {
		int index = 0;
		PlinkMapReader plinkMapReader = new PlinkMapReader();
		plinkMapReader.setGffChrAbs(gffChrAbs);
		plinkMapReader.setPlinkMap(plinkBimCorrect);
		plinkMapReader.initial();
		plinkPedReader = new PlinkPedReader(plinkPed);
		
		TxtReadandWrite txtWritePlinkMap = new TxtReadandWrite(plinkMapOut, true);
		TxtReadandWrite txtWritePlinkMapConvertor = new TxtReadandWrite(plinkMapConvertorOut, true);
		TxtReadandWrite txtWritePlinkPedPre = new TxtReadandWrite(plinkPedPreOut, true);

		List<String> lsSamples = plinkPedReader.getLsAllSamples();
		txtWritePlinkPedPre.writefileln(ArrayOperate.cmbString(lsSamples, "\t"));
		int i = 0;
		while (plinkMapReader.readNext()) {
			List<Allele> lsAlleles = plinkMapReader.getLsAllelesCurrent();
			if (lsAlleles.isEmpty()) {
				continue;
			}
			Map<String, List<Allele>> mapSample2LsAllele = new LinkedHashMap<>();
			for (String sample : lsSamples) {
				List<Allele> lsAlleleSample = plinkPedReader.getLsAlleleFromSample(sample, lsAlleles);
				mapSample2LsAllele.put(sample, lsAlleleSample);
			}
			if (lsAlleles.size() > 5) {
				CombineSnp combineSnp = new CombineSnp();
				combineSnp.setR2(r2Cluster);
				combineSnp.setMaxClusterNum(maxCluster);
				combineSnp.setMapSample2LsAlleles(mapSample2LsAllele);
				combineSnp.merge();
				mapSample2LsAllele = combineSnp.getMapSample2LsAllelesResult();
			}
		
			String geneName = plinkMapReader.getGeneCurrent().getNameSingle();
			Permutation permutation = new Permutation();
			permutation.setMaxSnpNum(permutationNum);
			permutation.setLsAlle(mapSample2LsAllele.values().iterator().next());
			permutation.setGeneInfo(plinkMapReader.getGeneCurrent().getRefID(), geneName);
			permutation.setIndex(index);
			permutation.generatePermutation();
			txtWritePlinkMap.writefileln(permutation.toStringMap());
			txtWritePlinkMapConvertor.writefileln(permutation.toStringConvertor());
			index = permutation.getIndexNew();
			
			if (i++ % 500 == 0) {
				logger.info("read {} genes", i);
			}

			for (List<Integer> lsIndex : permutation.getLsPermutations()) {
				ClusterKmean clusterKmean = new ClusterKmean();
				for (String sampleName : mapSample2LsAllele.keySet()) {
					clusterKmean.addSample2LsAllele(sampleName, Permutation.getLsAlleleFromLsIndex(mapSample2LsAllele.get(sampleName), lsIndex));
				}
				clusterKmean.cluster();
				List<String> lsValues = null;
				try {
					lsValues = clusterKmean.getClusterResult();
				} catch (Exception e) {
					clusterKmean.cluster();
					lsValues = clusterKmean.getClusterResult();
				}
			
				txtWritePlinkPedPre.writefileln(ArrayOperate.cmbString(lsValues, ","));
			}
		}
		txtWritePlinkMap.close();
		txtWritePlinkMapConvertor.close();
		txtWritePlinkPedPre.close();
	}
	
}

