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
import com.novelbio.analysis.gwas.convertformat.Mid2Ped;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.geneanno.Gene2Go;

import smile.clustering.KMeans;

public class TestGwas {
	private static final Logger logger = LoggerFactory.getLogger(TestGwas.class);
			
	public static void main2(String[] args) {
		String out = "/media/winE/mywork/hongjun-gwas/result-Os01g0883800-nochange/remove-deletion/";
		Mid2Ped mid2Ped = new Mid2Ped();
		mid2Ped.setStartNum(0);
		mid2Ped.convert2Ped(out + "permutation.plink.ped.pre", out + "permutation.plink.map.other", out + "permutation.plink.ped");			
	}
	
	public static void main(String[] args) {
//		if (args == null || args.length == 0) {
//			printHelp();
//			System.exit(1);
//		}
//		for (String string : args) {
//			if (string.toLowerCase().contains("help")) {
//				printHelp();
//				System.exit(1);
//			}
//		}
//		
//		Options opts = new Options();
//		opts.addOption("r2min", true, "r2min");
//		opts.addOption("maxCluster", true, "maxCluster");
//		opts.addOption("permutationNum", true, "permutationNum");
//		
//		opts.addOption("plinkBim", true, "plinkBim");
//		opts.addOption("plinkPed", true, "plinkPed");
//		opts.addOption("chrFile", true, "chrFile");
//		opts.addOption("gffFile", true, "gffFile");
//		opts.addOption("out", true, "out");
//		opts.addOption("tss", true, "tss");
//
//		CommandLine cliParser = null;
//		try {
//			cliParser = new GnuParser().parse(opts, args);
//		} catch (Exception e) {
//			printHelp();
//			System.exit(1);
//		}
//		
//		String plinkBim = cliParser.getOptionValue("plinkBim", "");
//		String plinkPed = cliParser.getOptionValue("plinkPed", "");
//		String chrFile = cliParser.getOptionValue("chrFile", "");
//		String gffFile = cliParser.getOptionValue("gffFile", "");
//		String out = cliParser.getOptionValue("out", "");
//		String r2min = cliParser.getOptionValue("r2min", "");
//		String variationCutoff = cliParser.getOptionValue("variationCutoff", "");
//		String maxCluster = cliParser.getOptionValue("maxCluster", "");
//		String permutationNum = cliParser.getOptionValue("permutationNum", "");
//		String tss = cliParser.getOptionValue("tss", "");
//		
//		FileOperate.createFolders(FileOperate.getPathName(out));
//		String plinkBimCorrect = FileOperate.changeFileSuffix(plinkBim, ".correct", null);

//		String plinkBim = "/media/winE/mywork/hongjun-gwas/619-29mio.bim";
//		String plinkPed =  "/media/winE/mywork/hongjun-gwas/619-29mio.ped";
//		String plinkBimCorrect = "/media/winE/mywork/hongjun-gwas/619-29mio.bim.anno";
//		
//		String chrFile = "/media/winE/mywork/hongjun-gwas/oryza_sativa.IRGSP-1.0.dna.fa";
//		String gffFile = "/media/winE/mywork/hongjun-gwas/Oryza_sativa.IRGSP-1.0.39.gff3";
//		String out = "/media/winE/mywork/hongjun-gwas/result-Os01g0883800-nochange/";
		
		String parent = "/media/winE/mywork/hongjun-gwas/result-Os01g0883800-nochange/remove-deletion/";
		String plinkBim = parent+"permutation.plink.map";
		String plinkPed =  parent+"permutation.plink.ped";
		String plinkBimCorrect = parent+"permutation.plink.map.anno";
		
		String chrFile = "/media/winE/mywork/hongjun-gwas/oryza_sativa.IRGSP-1.0.dna.fa";
		String gffFile = "/media/winE/mywork/hongjun-gwas/Oryza_sativa.IRGSP-1.0.39.gff3";
		String out = parent+"test";
		
		String r2min = "0.8";
		String maxCluster = "10";
		String permutationNum = "3";
		String tss = "2000";
		String variationCutoff = "0.05";
		String parallelNum = "80";

		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		GffHashGene gffHashGene = new GffHashGene();
		gffHashGene.addGeneNameFlag("ID=gene:");
		gffHashGene.addTranscriptNameFlag("ID=transcript:");
		gffHashGene.readGffFile(gffFile);
		gffChrAbs.setGffHash(gffHashGene);
		
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
		if (!StringOperate.isRealNull(tss)) {
			testGwas.setTss(Integer.parseInt(tss.trim()));
		}
		if (!StringOperate.isRealNull(variationCutoff)) {
			testGwas.setVariationCutoff(Double.parseDouble(variationCutoff.trim()));
		}
		if (!FileOperate.isFileExistAndBigThan0(out + "permutation.plink.map")
				||
				!FileOperate.isFileExistAndBigThan0(out + "permutation.plink.map.convertor")
				||
				!FileOperate.isFileExistAndBigThan0(out + "permutation.plink.ped.pre")
				) {
			testGwas.setOutput(out + "permutation.plink.map", out + "permutation.plink.map.convertor", out + "permutation.plink.ped.pre");
			testGwas.cluster();
		}

		System.out.println("finish permutation");
		
		if (!FileOperate.isFileExistAndBigThan0(out + "permutation.plink.ped")) {
			Mid2Ped mid2Ped = new Mid2Ped();
			if (!StringOperate.isRealNull(parallelNum)) {
				mid2Ped.setConsistentSampleNum(Integer.parseInt(parallelNum));
			}
			mid2Ped.setStartNum(0);
			mid2Ped.convert2Ped(out + "permutation.plink.ped.pre", out + "permutation.plink.map.other", out + "permutation.plink.ped");			
		}
		System.out.println("finish");
	}
	
	private static void printHelp() {
		System.err.println("java -jar testGwas.jar --plinkBim plink.bim --plinkPed plink.ped --r2min 0.8 --tss 1500 --variationCutoff 0.05 --maxCluster 10 --permutationNum 3  --chrFile chrFile --gffFile gffFile --out outPath");
		System.err.println();
		System.err.println("r2min: cluster the snp have r2 bigger than the value, default is 0.2");
		System.err.println("maxCluster: cluster num less then the value, default is 0, means no cluster number limit");
		System.err.println("permutationNum: number n in c(m,n), default is 3, while 0 means n=m");
		System.err.println("tss: tss length, snp in tss will used to analysis");
		System.err.println("variationCutoff: snp variation less this will be cutoff, default is 0.05");

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
	double variationCutoff = 0.05;
	int maxCluster = 0;
	int permutationNum=3;
	//==============================
	int tss = 1500;
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
	public void setTss(int tss) {
		this.tss = tss;
	}
	public void setVariationCutoff(double variationCutoff) {
		this.variationCutoff = variationCutoff;
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
		plinkMapReader.setTss(tss);
		plinkMapReader.setGffChrAbs(gffChrAbs);
		plinkMapReader.setPlinkMap(plinkBimCorrect);
		plinkMapReader.initial();
		plinkPedReader = new PlinkPedReader(plinkPed);
		
		TxtReadandWrite txtWritePlinkMap = new TxtReadandWrite(plinkMapOut, true);
		TxtReadandWrite txtWritePlinkMapOld = new TxtReadandWrite(plinkMapOut+".old", true);
		TxtReadandWrite txtWritePlinkMapConvertor = new TxtReadandWrite(plinkMapConvertorOut, true);
		TxtReadandWrite txtWritePlinkMapConvertorOld = new TxtReadandWrite(plinkMapConvertorOut+".old", true);

		TxtReadandWrite txtWritePlinkPedPre = new TxtReadandWrite(plinkPedPreOut, true);
		
		List<String> lsSamples = plinkPedReader.getLsAllSamples();
		txtWritePlinkPedPre.writefileln(ArrayOperate.cmbString(lsSamples, "\t"));
		int i = 0;
		while (plinkMapReader.readNext()) {
			List<Allele> lsAlleles = plinkMapReader.getLsAllelesCurrent();
			if (lsAlleles.isEmpty()) {
				continue;
			}
			String geneName = plinkMapReader.getGeneCurrent().getNameSingle();
			if (!geneName.contains("Os01g0883800")) {
				continue;
			}
			
			if (i++ %10 ==0) {
				logger.info("read {} genes", i);
			}
//			if (i>1000) {
//				break;
//			}
//			if (i<2911) {
//				continue;
//			}
			if (lsAlleles.size() > 500) {
				logger.info("gene {} have too many snps {}", geneName, lsAlleles.size());
			}
			Map<String, List<Allele>> mapSample2LsAllele = new LinkedHashMap<>();
			for (String sample : lsSamples) {
				List<Allele> lsAlleleSample = plinkPedReader.getLsAlleleFromSample(sample, lsAlleles);
				mapSample2LsAllele.put(sample, lsAlleleSample);
			}

			if (lsAlleles.size() > 5) {
				CombineSnp combineSnp = new CombineSnp();
				combineSnp.setGeneName(geneName);
				combineSnp.setR2(r2Cluster);
				combineSnp.setVariationCutoff(variationCutoff);
//				combineSnp.setMaxClusterNum(maxCluster);
				combineSnp.setMaxClusterNum(500);
				combineSnp.setMapSample2LsAlleles(mapSample2LsAllele);
				try {
					combineSnp.merge();
				} catch (Exception e) {
					logger.error("combine gene {} error", geneName);
					txtWritePlinkMapOld.close();
					txtWritePlinkMap.close();
					txtWritePlinkMapConvertor.close();
					txtWritePlinkMapConvertorOld.close();
					txtWritePlinkPedPre.close();
					throw e;
				}

				mapSample2LsAllele = combineSnp.getMapSample2LsAllelesResult();
				if (mapSample2LsAllele.values().iterator().next().isEmpty()) {
					continue;
				}
			}
		
			Permutation permutation = new Permutation();
			permutation.setMaxSnpNum(permutationNum);
			permutation.setLsAlle(mapSample2LsAllele.values().iterator().next());
			permutation.setGeneInfo(plinkMapReader.getGeneCurrent().getRefID(), geneName);
			permutation.setIndex(index);
			permutation.generatePermutation();
			
			for (List<Integer> lsIndex : permutation.getLsPermutations()) {
				ClusterKmean clusterKmean = new ClusterKmean();
				for (String sampleName : mapSample2LsAllele.keySet()) {
					clusterKmean.addSample2LsAllele(sampleName, Permutation.getLsAlleleFromLsIndex(mapSample2LsAllele.get(sampleName), lsIndex));
				}
				clusterKmean.cluster();
				Allele allele = permutation.getAllele(lsIndex);
				List<String[]> lsValues = clusterKmean.getClusterResult();
				lsValues = CombineSnp.modifyList(lsValues, allele, variationCutoff);
				
				if (lsValues == null) {
					continue;
				}
				
				String value = cmbSnps(lsValues);
				txtWritePlinkPedPre.writefileln(value);
				
				String map = permutation.toStringMap(lsIndex);
				txtWritePlinkMap.writefileln(map);
				txtWritePlinkMapConvertor.writefileln(permutation.toStringConvertor(lsIndex));
				permutation.indexAdd();
				if (StringOperate.isRealNull(value) || StringOperate.isRealNull(map)) {
					txtWritePlinkMap.close();
					txtWritePlinkMapConvertor.close();
					txtWritePlinkMapOld.close();
					txtWritePlinkMapConvertorOld.close();
					txtWritePlinkPedPre.close();
					throw new RuntimeException("error on gene " + geneName);
				}
			}
			txtWritePlinkMapOld.writefileln(permutation.toStringMap());
			txtWritePlinkMapConvertorOld.writefileln(permutation.toStringConvertor());
			index = permutation.getIndexNew();
		}
		txtWritePlinkMap.close();
		txtWritePlinkMapConvertor.close();
		txtWritePlinkPedPre.close();
		txtWritePlinkMapOld.close();
		txtWritePlinkMapConvertorOld.close();
	}
	
	public static boolean isClusterRight(List<String> lsValues) {
		Set<String> setSnps = new HashSet<>();
		for (String snpUnit : lsValues) {
			String snp = snpUnit.toCharArray()[0]+"";
			setSnps.add(snp);
		}
		if (setSnps.size() > 3
				|| (setSnps.size() == 3 && !setSnps.contains("0"))
				|| (setSnps.size() ==2 && setSnps.contains("0"))
				|| setSnps.size() == 1
				) {
			return false;
		}
		return true;
	}
	
	public static String cmbSnps(List<String[]> lsValues ) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lsValues.size(); i++) {
			String[] ss = lsValues.get(i);
			if (i > 0) {
				stringBuilder.append("\t");
			}
			stringBuilder.append(ss[0] + ss[1]);
		}
		return stringBuilder.toString();
	}
	
}

