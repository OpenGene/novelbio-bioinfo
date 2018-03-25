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
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
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
		FileOperate.createFolders(FileOperate.getPathName(out));
		String plinkBimCorrect = FileOperate.changeFileSuffix(plinkBim, ".correct", null);

//		String plinkBim = "/home/novelbio/test/plink/619-40maf.addchr.bim";
//		String plinkPed =  "/home/novelbio/test/plink/plink.ped";
//		String plinkBimCorrect = "/home/novelbio/test/plink/619-40maf.bim.anno";
//		
//		String chrFile = "/home/novelbio/test/plink/chrAll.fa";
//		String gffFile = "/home/novelbio/test/plink/all.gff3";
//		String out = "/home/novelbio/test/plink/";
		
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
		testGwas.setOutput(out + "permutation.plinkmap", out + "permutation.plinkmapconvertor", out + "permutation.plinkped.pre");
		testGwas.cluster();
		System.out.println("finish permutation");
		try {
			GwasFormat.convertPlinkCsv2plinkPed(out + "permutation.plinkped.pre", out + "permutation.plink.ped");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void printHelp() {
		System.err.println("java -jar testGwas.jar --plinkBim plink.bim --plinkPed plink.ped --chrFile chrFile --gffFile gffFile --out outPath");
		System.err.println("example:");
		System.err.println("java -jar testGwas.jar --plinkBim /home/novelbio/plink.bim --plinkPed /home/novelbio/plink.ped"
				+ " --chrFile /home/novelbio/chrFile --gffFile /home/novelbio/gffFile --out /home/novelbio/permutation");
	}
	
	PlinkPedReader plinkPedReader;

	GffChrAbs gffChrAbs;
	String plinkBimCorrect;
	String plinkPed;

	String plinkMapOut;
	String plinkMapConvertorOut;
	String plinkPedPreOut;
	
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
			Map<Allele, Set<String>> mapSnp2SetIsoName = plinkMapReader.getLsAllelesCurrentGene();
			if (mapSnp2SetIsoName.isEmpty()) {
				continue;
			}
			String geneName = plinkMapReader.getGeneCurrent().getNameSingle();
			Permutation permutation = new Permutation();
			permutation.setMapSnp2SetIsoName(mapSnp2SetIsoName);
			permutation.setGeneInfo(plinkMapReader.getGeneCurrent().getRefID(), geneName);
			permutation.setIndex(index);
			permutation.generatePermutation();
			txtWritePlinkMap.writefileln(permutation.toStringMap());
			txtWritePlinkMapConvertor.writefileln(permutation.toStringConvertor());
			index = permutation.getIndexNew();
			
			Map<String, List<Allele>> mapSample2LsAllele = new LinkedHashMap<>();
//			logger.info("read snp from gene " + geneName);
			if (i++ % 500 == 0) {
				logger.info("read {} genes", i);
			}
			
			for (String sample : lsSamples) {
				List<Allele> lsAlleleSample = getLsAlleleFromSample(sample, permutation.getLsAlleleFinal());
				mapSample2LsAllele.put(sample, lsAlleleSample);
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

	private List<Allele> getLsAlleleFromSample(String sample, List<Allele> lsAlleles) {
		if (lsAlleles.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Allele> lsAlleleResult = new ArrayList<>();

		int start = lsAlleles.get(0).getIndex();

		Iterator<Allele> itAllelesRef = lsAlleles.iterator();
		Iterator<Allele> itAllelesSample = plinkPedReader.readAllelsFromSample(sample, start).iterator();
		
		Allele alleleRef = itAllelesRef.next();
		Allele alleleSample = itAllelesSample.next();
		while (true) {
			if (alleleRef.getIndex() == alleleSample.getIndex() ) {
				alleleSample.setRef(alleleRef);
				lsAlleleResult.add(alleleSample);
				if (!itAllelesRef.hasNext()) {
					break;
				}
				alleleRef = itAllelesRef.next();
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() > alleleSample.getIndex()) {
				if (!itAllelesSample.hasNext()) {
					throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
				}
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() < alleleSample.getIndex()) {
				throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
			}
		}
		return lsAlleleResult;
	}
	
}

class Permutation {
	/** 每一组就一个排列组合 */
	List<List<Integer>> lsNumber = new ArrayList<>();
	Map<Allele, Set<String>> mapSnp2SetIsoName;
	List<Allele> lsAlleleFinal;
	String geneName;
	String chrId;
	
	/** 输入的index */
	int index;
	/** 结束之后的index */
	int indexNew;
	
	public void setMapSnp2SetIsoName(Map<Allele, Set<String>> mapSnp2SetIsoName) {
		this.mapSnp2SetIsoName = mapSnp2SetIsoName;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getIndexNew() {
		return indexNew;
	}
	public void setGeneInfo(String chrId, String geneName) {
		this.chrId = chrId;
		this.geneName = geneName;
	}
	
	public List<Allele> getLsAlleleFinal() {
		return lsAlleleFinal;
	}
	
	public List<List<Integer>> getLsPermutations() {
		List<List<Integer>> lsNumberFinal = new ArrayList<>();
		//把单个的也加进去
//		for (int i = 0; i < lsAlleleFinal.size(); i++) {
//			lsNumberFinal.add(Lists.newArrayList(i));
//		}
		for (List<Integer> list : lsNumber) {
//			if (list.size() == 1) {
//				continue;
//			}
			lsNumberFinal.add(list);
		}
		return lsNumberFinal;
	}
	
	/**
	 * 给定lsIndex，譬如 0，1，3，4
	 * 返回 具体的listAllele
	 * @return
	 */
	public static List<Allele> getLsAlleleFromLsIndex(List<Allele> lsAlleles, List<Integer> lsIndex) {
		List<Allele> lsAlleleNew = new ArrayList<>();
		for (Integer index : lsIndex) {
			lsAlleleNew.add(lsAlleles.get(index));
		}
		return lsAlleleNew;
	}
	
	public void generatePermutation() {
		permutation(mapSnp2SetIsoName.size());
		filterByIso();
	}
	
	protected void permutation(int number) {
		Integer[] lsIndex = new Integer[number];
		for (int i = 0; i < lsIndex.length; i++) {
			lsIndex[i] = i;
		}
		lsNumber.add(ArrayOperate.converArray2List(lsIndex));
		//排列组合，暂时停用
//		for (int i = 2; i <= lsIndex.length; i++) {
//			combinationSelect(lsIndex, 0, new Integer[i], 0);
//		}
	}
	
	//TODO 尚未测试
	protected void filterByIso() {
		List<List<Integer>> lsPermutationFinal = new ArrayList<>();
		lsAlleleFinal = new ArrayList<>(mapSnp2SetIsoName.keySet());
		//TODO 暂时停用
//		for (List<Integer> lsPermutationUnit : lsNumber) {
//			Set<String> setOverlap = new HashSet<>();
//			for (Integer index : lsPermutationUnit) {
//				Set<String> setIsoThis = mapSnp2SetIsoName.get(lsAlleleFinal.get(index));
//				if (setOverlap.isEmpty()) {
//					setOverlap = setIsoThis;
//					continue;
//				}
//				Set<String> setOverlapTmp = new HashSet<>();
//				for (String isoName : setIsoThis) {
//					if (setOverlap.contains(isoName)) {
//						setOverlapTmp.add(isoName);
//					}
//				}
//				setOverlap = setOverlapTmp;
//				if (setOverlap.isEmpty()) {
//					break;
//				}
//			}
//			if (!setOverlap.isEmpty()) {
//				lsPermutationFinal.add(lsPermutationUnit);
//			}
//		}
//		lsNumber = lsPermutationFinal;
	}
	
	/** 
	 * 计算组合数，即C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
	public static long combination(int n, int m) {
		long factN = factorial(n);
		long factN_M = factorial(n - m);
		long factM = factorial(m);
		long value = (n >= m) ? factN / factN_M / factM : 0;
		return value;
	}
	
	/** 
	 * 计算组合数，即C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
	public static long combinationNew(int n, int m) {
//		Set<Integer> setNfact = new HashSet<>();
//		Set<Integer> setMfact = new HashSet<>();
//		Set<Integer> setN_Mfact = new HashSet<>();
//		for (int i = 1; i <= n; i++) {
//			setNfact.add(i);
//		}
//		for (int i = 1; i <= m; i++) {
//			setMfact.add(i);
//		}
//		for (int i = 1; i <= n-m; i++) {
//			setN_Mfact.add(i);
//		}
//		for (Integer integer : setN_Mfact) {
//			
//		}
		if (m == 0 || m == n) {
			return 1;
		}
		long factNtoM = n-m > m ? factorial(n, n-m+1) : factorial(n, m+1);
		long factN_M = n-m > m ? factorial(m) : factorial(n - m);
		long value = (n >= m) ? factNtoM / factN_M : 0;
		return value;
	}
	/** 
	 * 计算阶乘数，即n! = n * (n-1) * ... * 2 * 1 
	 * @param n 
	 * @return 
	 */  
	public static long factorial(int n) {
		long value = (n > 1) ? n * factorial(n - 1) : 1;
		if (value < 0) {
			throw new RuntimeException("factorial error, " + n + "! larger than Long!");
		}
		return value;
	}
	/** 
	 * 计算阶乘数，即n! = n * (n-1) * ... * m
	 * @param n 
	 * @return 
	 */  
	public static long factorial(int n, int m) {
		if (m == n) {
			return m;
		} else if (m > n) {
			throw new RuntimeException(m + " is bigger than " + n);
		}
		long value = (n > 1) ? n * factorial(n - 1, m) : 1;  
		if (value < 0) {
			throw new RuntimeException("factorial error, " + n + "!/"+m+"! larger than Long!");
		}
		return value;
	}
    /** 
     * 组合选择 
     * @param lsIndex 待选列表 
     * @param dataIndex 待选开始索引 
     * @param lsResult 前面（resultIndex-1）个的组合结果 
     * @param resultIndex 选择索引，从0开始 
     */  
	private void combinationSelect(Integer[] lsIndex, int dataIndex, Integer[] lsResult, int resultIndex) {
		int resultLen = lsResult.length;
		int resultCount = resultIndex + 1;
		if (resultCount > resultLen) { // 全部选择完时，输出组合结果
			lsNumber.add(ArrayOperate.converArray2List(lsResult));
			return;
		}

		// 递归选择下一个  
		for (int i = dataIndex; i < lsIndex.length + resultCount - resultLen; i++) {
			lsResult[resultIndex] = lsIndex[i];
			combinationSelect(lsIndex, i + 1, lsResult, resultIndex + 1);
		}
	}
	
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 注意最后无 \n 结尾
	 */
	public String toStringMap() {
		indexNew = index;
		List<String> lsResult = new ArrayList<>();
		for (List<Integer> list : getLsPermutations()) {
			List<String> lsResultUnit = new ArrayList<>();
			Allele alleleFirst = lsAlleleFinal.get(list.get(0));
			lsResultUnit.add(chrId);
			lsResultUnit.add(geneName+ "." +(indexNew));
			lsResultUnit.add("0");
			lsResultUnit.add(alleleFirst.getPosition() + "");
			if (list.size() == 1) {
				if (alleleFirst.isRefMajor()) {
					lsResultUnit.add(alleleFirst.getAltBase());
					lsResultUnit.add(alleleFirst.getRefBase());
				} else {
					lsResultUnit.add(alleleFirst.getRefBase());
					lsResultUnit.add(alleleFirst.getAltBase());
				}
			} else {
				lsResultUnit.add(ClusterKmean.getMajor());
				lsResultUnit.add(ClusterKmean.getMinor());
			}
			lsResult.add(ArrayOperate.cmbString(lsResultUnit, "\t"));
			indexNew++;
		}
		return ArrayOperate.cmbString(lsResult, "\n");
	}
	
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 组合前的snp与组合后的snp的对照表
	 * 注意最后无 \n 结尾
	 */
	public String toStringConvertor() {
		indexNew = index;
		List<String> lsResult = new ArrayList<>();
		for (List<Integer> list : getLsPermutations()) {
			for (Integer indexAllele : list) {
				Allele allele = lsAlleleFinal.get(indexAllele);
				lsResult.add(allele.toString() + "\t" + geneName+ "." + indexNew);
			}
			indexNew++;
		}
		return ArrayOperate.cmbString(lsResult, "\n");
	}
	

	
}

class ClusterKmean {
	/** 输入数据 */
	Map<String, List<Allele>> mapSample2LsAllele = new LinkedHashMap<>();
	/**  聚类后的结果 */
	Map<String, Integer> mapSample2ClusterResult = new LinkedHashMap<>();
	
	public void addSample2LsAllele(String sampleName, List<Allele> lsAllele) {
		mapSample2LsAllele.put(sampleName, lsAllele);
	}
	
	public void cluster() {
		if (mapSample2LsAllele.values().iterator().next().size() == 1) {
			return;
		}
		double[][] data = generateData();
		KMeans kMeans = new KMeans(data, 2);
		int[] result = kMeans.getClusterLabel();
		int i = 0;
		for (String sampleName : mapSample2LsAllele.keySet()) {
			mapSample2ClusterResult.put(sampleName, result[i++]);
		}
	}
	
	public List<String> getClusterResult() {
		List<String> lsResult = new ArrayList<>();
		if (mapSample2LsAllele.values().iterator().next().size() == 1) {
			for (List<Allele> lsAllele : mapSample2LsAllele.values()) {
				lsResult.add(lsAllele.get(0).getAllele1() + lsAllele.get(0).getAllele2());
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
	
	private String getResult(int number) {
		if (number == 0) {
			return getMajor()+getMajor();
		} else if (number == 1) {
			return getMinor()+getMinor();
		} else {
			throw new ExceptionNBCPlink("cannot have this condition, number is " + number);
		}
	}
	public static String getMajor() {
		return "A";
	}
	public static String getMinor() {
		return "T";
	}
}
