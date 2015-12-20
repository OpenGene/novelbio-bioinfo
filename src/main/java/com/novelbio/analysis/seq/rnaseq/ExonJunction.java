package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.GuiAnnoInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonClusterExtract;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonClusterSite;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.ExonSplicingTest.PvalueCalculate;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamMapReads;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.listOperate.ListAbs;

/**
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * 每次跑之前要清空
 * @author zong0jie
 */
public class ExonJunction extends RunProcess<GuiAnnoInfo> {
	public static void main(String[] args) {
		long timeEclipse1 = test();
		System.out.println(timeEclipse1);
	}
	
	public static long wwwSimulation() {
		String parentPath = "/media/winE/NBCsource/otherResource/www/simulation10/";
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		
		List<Align> lsAligns = new ArrayList<>();
//		lsAligns.add(new Align("1", 2588282, 2588574));
		
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(new GffHashGene(parentPath + "genes.gtf"));
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffChrAbs.getGffHashGene());
		exonJunction.setgenerateNewIso(true);
		exonJunction.setLsReadRegion(lsAligns);
		exonJunction.setOneGeneOneSpliceEvent(false);
		exonJunction.addBamSorted("Ex", parentPath + "exclusion.bam");
		exonJunction.addBamSorted("In", parentPath + "inclusion.bam");
		exonJunction.setCompareGroups("Ex", "In");
		exonJunction.setResultFile(parentPath + "result-sep-exon");
		exonJunction.setJunctionMinAnchorLen(0);
//		exonJunction.setStrandSpecific(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND);
		exonJunction.run();
		exonJunction = null;
		return dateUtil.getElapseTime();
	}

	public static long test() {
		//TODO
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(new Align("1:45567281-45649767"));
//		lsAligns.add(new Align("1:7205126-27246005"));
//		lsAligns.add(new Align("11", 1, 250088574));

		DateUtil dateUtil = new DateUtil();
//		dateUtil.setStartTime();
//		System.out.println("start");
//		Species species = new Species(9606);
//		species.setVersion("hg19_GRCh37");
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(new GffHashGene("/home/novelbio/NBCresource/www/genes_modify.gtf"));
		gffChrAbs.close();
		ExonJunction exonJunction = new ExonJunction();
//		exonJunction.setGffHashGene(new GffHashGene(GffType.GTF, "/home/zong0jie/Test/rnaseq/paper/chicken/raw_ensembl_genes/chicken_ensemble_KO-WT-merged.gtf"));
		exonJunction.setGffHashGene(gffChrAbs.getGffHashGene());
		exonJunction.setgenerateNewIso(true);
		exonJunction.setNewIsoReadsNum(15);
		exonJunction.setLsReadRegion(lsAligns);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String parentPath = "/home/novelbio/NBCresource/www/";
		exonJunction.addBamSorted("KD", parentPath + "KD.accepted.bam");
		exonJunction.addBamSorted("WT", parentPath + "WT.accepted.bam");
		exonJunction.setCompareGroups("KD", "WT");
//		exonJunction.setStrandSpecific(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND);
		exonJunction.setResultFile(parentPath + "result_20151220-mse");

		exonJunction.run();
		exonJunction = null;
		return dateUtil.getElapseTime();
	}
	
	
	public static long test2() {
		//TODO
		List<Align> lsAligns = new ArrayList<>();
//		lsAligns.add(new Align("11:65083629-65660215"));
//		lsAligns.add(new Align("1:7205126-27246005"));
//		lsAligns.add(new Align("11", 1, 250088574));

		DateUtil dateUtil = new DateUtil();
//		dateUtil.setStartTime();
//		System.out.println("start");
//		Species species = new Species(9606);
//		species.setVersion("hg19_GRCh37");
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(new GffHashGene("/media/winE/iii/hg19_filter.psl.gtf"));
		ExonJunction exonJunction = new ExonJunction();
//		exonJunction.setGffHashGene(new GffHashGene(GffType.GTF, "/home/zong0jie/Test/rnaseq/paper/chicken/raw_ensembl_genes/chicken_ensemble_KO-WT-merged.gtf"));
		exonJunction.setGffHashGene(gffChrAbs.getGffHashGene());
		exonJunction.setgenerateNewIso(true);
		exonJunction.setNewIsoReadsNum(15);
		exonJunction.setLsReadRegion(lsAligns);
		exonJunction.setOneGeneOneSpliceEvent(false);
		String parentPath = "/media/winE/iii/";
		exonJunction.addBamSorted("ctr", parentPath + "ctr1.chr20_51504-1271232.bam");
		exonJunction.addBamSorted("ctr", parentPath + "ctr2.chr20_51504-1271232.bam");
		exonJunction.addBamSorted("ctr", parentPath + "ctr3.chr20_51504-1271232.bam");

		exonJunction.addBamSorted("h2", parentPath + "H1.chr20_51504-1271232.bam");
		exonJunction.addBamSorted("h2", parentPath + "H2.chr20_51504-1271232.bam");
		exonJunction.addBamSorted("h2", parentPath + "H3.chr20_51504-1271232.bam");

		exonJunction.setCompareGroups("ctr", "h2");
//		exonJunction.setStrandSpecific(StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND);
		exonJunction.setResultFile(parentPath + "result_20151007");

		exonJunction.run();
		exonJunction = null;
		return dateUtil.getElapseTime();
	}
	
	private static Logger logger = Logger.getLogger(ExonJunction.class);
	private static String stopGeneName = "ENSG00000163531";
		
	GffHashGene gffHashGene = null;
	/** 没有重建转录本的老iso的名字，用于后面计算可变剪接所在exon number的 */
	Set<String> setIsoName_No_Reconstruct;
	
	StrandSpecific strandSpecific = StrandSpecific.NONE;
	/** 全体差异基因的外显子
	 * ls--
	 * ls：gene
	 * ExonSplicingTest：difexon
	 *  */
	List<List<ExonClusterSite>> lsSplicingTests;
	List<ExonSplicingTest> lsResult;
	/** 
	 * 一个基因可能有多个可变剪接事件，但是我们可以只挑选其中最显著的那个可变剪接事件
	 * 也可以输出全部的可变剪接事件
	 * 每个基因只有一个可变剪接事件
	 */
	boolean oneGeneOneSpliceEvent = true;
	
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	/** 本次比较的condition */
	String condition1, condition2;
	/** condition到排序的bam文件 */
	ArrayListMultimap<String, AlignSamReading> mapCond2SamReader = ArrayListMultimap.create();
	ArrayListMultimap<String, SamFile> mapCond2SamFile = ArrayListMultimap.create();
	
	/** 每个样本都有多少 reads */
	Map<String, Map<String, double[]>> mapCond_group2ReadsNum = new HashMap<>();
	
	double pvalue = 0.05;//表示差异可变剪接的事件的pvalue阈值
	
	String resultFile;
	/** 是否合并文件--也就是不考虑重复，默认为true，也就是合并文件 **/
	boolean isCombine = true;
	/** 是否提取序列 */
	SeqHash seqHash;
	//TODO 默认设置为false
	boolean isLessMemory = false;
	boolean isReconstructIso = true;
	boolean isReconstructRI = false;
	/**
	 * 读取区域，调试用。设定之后就只会读取这个区域的reads
	 */
	List<Align> lsReadReagion;

	/** junction的pvalue所占的比重
	 * 小于0 或者大于1 表示动态比重
	 */
	double pvalueJunctionProp = -1;
	/** 是否仅使用 unique mapped reads 来做分析 */
	boolean isUseUniqueMappedReads = false;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	/** pvalue超过这个值就不进行fdr计算 */
	double fdrCutoff = 1;
	
	/** 至少有15条reads支持的junction才会用于重建转录本 */
	int newIsoReadsNum = 15;
	
	int intronMinLen = 12;
	
	/** 小于6bp的alt5和alt3都可能是假的 */
	int minDifLen = 6;
	
	Map<String, Long> mapChrId2Len;
	
	public ExonJunction() {
//		List<Align> lsAligns = new ArrayList<>();
//		lsAligns.add(new Align("chr1:145663570-145864207"));
//		setLsReadRegion(lsAligns);
	}

	/** 至少有多少条reads支持的junction才会用于重建转录本 */
	public void setNewIsoReadsNum(int newIsoReadsNum) {
		this.newIsoReadsNum = newIsoReadsNum;
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
	public void setFdrCutoff(double fdrCutoff) {
	    this.fdrCutoff = fdrCutoff;
    }
	/**
	 * pvalue的计算是合并exon表达pvalue和junction pvalue 
	 * junction的pvalue所占的比重
	 * 小于0 或者大于1 表示动态比重，也就是从exon的长度上推断pvalue
	 */
	public void setPvalueJunctionProp(double pvalueJunctionProp) {
		this.pvalueJunctionProp = pvalueJunctionProp;
	}
	public void setUseUniqueMappedReads(boolean isUseUniqueMappedReads) {
		this.isUseUniqueMappedReads = isUseUniqueMappedReads;
	}
	/**
	 * 表示差异可变剪接的事件的pvalue阈值，仅用于统计差异可变剪接事件的数量，不用于可变剪接的筛选
	 * @param pvalue
	 */
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	/** 是否重建转录本，调用一下就重建<br>
	 * 在run之前最后一个设定
	 */
	public void setgenerateNewIso(boolean isReconstructIso) {
		this.isReconstructIso = isReconstructIso;
	}
	/** 是否重建长的exon，然后可以检测RI事件
	 * 因为重建转录本只是根据juncReads重建，但并不重建exon，
	 * 本参数可以检测内含子的reads覆盖情况，然后如果覆盖度到位，就会重建exon
	 * 但是因为需要检测内含子是否连续，所以对于mapreads的采样要求就比较高，会吃很多内存
	 * @param isReconstructRI
	 */
	public void setReconstructRI(boolean isReconstructRI) {
		this.isReconstructRI = isReconstructRI;
	}
	/** 是否合并文件--也就是不考虑重复，默认为true，也就是合并文件 **/
	public void setCombine(boolean isCombine) {
		this.isCombine = isCombine;
	}
	/**
	 * 是否采用节省内存模式
	 * 如果节省内存就用SamMapReads 速度慢
	 * 如果耗内存就用MapReads 速度快
	 * 默认false
	 * @param isLessMemory
	 */
	public void setIsLessMemory(boolean isLessMemory) {
		this.isLessMemory = isLessMemory;
	}
	
	/** 设定建库的方式 */
	public void setStrandSpecific(StrandSpecific strandSpecific) {
		if (strandSpecific == StrandSpecific.UNKNOWN) {
			return;
		}
		this.strandSpecific = strandSpecific;
	}
	/** 设定最短的intron长度，默认为25,也就是说小于25bp（<25）的都认为是deletion，该reads不加入可变剪接考察 */
	public void setIntronMinLen(int intronMinLen) {
		this.intronMinLen = intronMinLen;
	}
	
	/** 设定junction reads的接头最短长度，譬如reads的一头搭到了某个exon上，如果这个长度小于该指定长度,默认为5(<5)，则该reads不加入可变剪接考察 */
	public void setJunctionMinAnchorLen(int junctionMinAnchorLen) {
		tophatJunction.setJunctionMinAnchorLen(junctionMinAnchorLen);
	}
	/**
	 * 设定输出
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	
	public void setLsReadRegion(List<Align> lsAligns) {
		if (lsAligns != null && lsAligns.size() > 0) {
			lsReadReagion = lsAligns;
		}
	}
	/** 
	 * 一个基因可能有多个可变剪接事件，但是我们可以只挑选其中最显著的那个可变剪接事件
	 * 也可以输出全部的可变剪接事件, <b>默认为true</b><br>
	 * @param oneGeneOneSpliceEvent <br> true:  每个基因只有一个可变剪接事件<br>
	 * false: 每个基因输出全部可变剪接事件
	 */
	public void setOneGeneOneSpliceEvent(boolean oneGeneOneSpliceEvent) {
		this.oneGeneOneSpliceEvent = oneGeneOneSpliceEvent;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		this.setIsoName_No_Reconstruct = gffHashGene.getSetIsoID();
		lsSplicingTests = new ArrayList<>();
	}
	/**
	 * 如果seqhash为true，则提取序列
	 * @param seqHash
	 */
	public void setSeqHash(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	
	protected ArrayList<Align> getLsDifIsoGene() {
		ArrayList<Align> lsAlignments = new ArrayList<Align>();
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
			Align align = new Align(gffDetailGene.getRefID(), gffDetailGene.getStartCis(), gffDetailGene.getEndCis());
			lsAlignments.add(align);
		}
		return lsAlignments;
	}

	public void setCompareGroups(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	public void addBamSorted(String condition, String sortedBamFile) {
		setCondition.add(condition);
		SamFile samFile = new SamFile(sortedBamFile);
		if (mapChrId2Len == null) {
			mapChrId2Len = samFile.getMapChrID2Length();
		}
		AlignSamReading samFileReading = new AlignSamReading(samFile);
		mapCond2SamReader.put(condition, samFileReading);
		mapCond2SamFile.put(condition, samFile);
	}
	
	public Map<String, Long> getMapChrId2Len() {
		return mapChrId2Len;
	}
	
	public void running() {
		tophatJunction.setIntronMinLen(intronMinLen);
		if (!isCombine) {
			for (String condition : mapCond2SamFile.keys()) {
				if (mapCond2SamFile.get(condition).size() == 1) {
					isCombine = true;
					break;
				}
			}
		}

		runByChrome();
	}
	
	private void runWithoutChrome() {
		MapReads mapReads = null;		
		if (isReconstructIso) {
			int invNum = isReconstructRI? 3 : 15;
			mapReads = getMapReads(mapCond2SamReader.values().iterator().next(), invNum);
		}
		
		loadJunctionBam(mapReads);
		tophatJunction.conclusion();
		
		suspendCheck();
		logger.info("finish junction reads");
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo2("Get Junction Event");
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(1.0);
			lsRegion.add(0.0);
			lsRegion.add((double) gffHashGene.getGffDetailAll().size());
			guiAnnoInfo.setLsNumInfo(lsRegion);
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		GenerateNewIso generateNewIso = null;
		if (isReconstructIso) {
			generateNewIso = new GenerateNewIso(tophatJunction, mapReads, strandSpecific, isReconstructRI);
			generateNewIso.setMinIntronLen(intronMinLen);
			generateNewIso.setGffHash(gffHashGene);
			generateNewIso.setNewIsoReadsNum(newIsoReadsNum);
		}
		fillLsAll_Dif_Iso_Exon(generateNewIso);
		if (isReconstructIso) {
			generateNewIso.clear();
			generateNewIso = null;
			mapReads.clear();
			mapReads = null;
			System.gc();
		}
		
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(1.0);
			lsRegion.add(0.0);
			lsRegion.add((double) gffHashGene.getGffDetailAll().size());
			guiAnnoInfo.setLsNumInfo(lsRegion);
			guiAnnoInfo.setInfo2("Reading Exp");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		loadExp();
		setSplicingType();
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(2.0);
			lsRegion.add(0.0);
			lsRegion.add((double) (lsSplicingTests.size()));
			guiAnnoInfo.setLsNumInfo(lsRegion);
			guiAnnoInfo.setInfo2("Doing Test");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		setCompareGroups(condition1, condition2);
		lsResult = getTestResult_FromIso();
		ExonSplicingTest.sortAndFdr(lsResult, fdrCutoff);
		
		if (resultFile != null) {
			String outFile = "";
			if (FileOperate.isFileDirectory(resultFile)) {
				outFile = resultFile + condition1 +"vs" + condition2 + ".txt";
			} else {
				outFile = FileOperate.changeFileSuffix(resultFile, "_"+condition1 +"vs" + condition2, "txt");
			}
			writeToFile(outFile, lsResult);
		}
	}
	
	private void runByChrome() {
		lsResult = new ArrayList<>();
		Set<String> setChrIdRaw = mapChrId2Len.keySet();
		Map<String, String> mapChrIdLowcase2Id = getMapChrIdLowcase2Id(setChrIdRaw);
		Set<String> setChrId = new HashSet<>();
		//仅读取lsReadReagion中的记录
		if (lsReadReagion == null || lsReadReagion.isEmpty()) {
			setChrId = setChrIdRaw;
		} else {
			for (Align align : lsReadReagion) {
				String chrIdLowcase = align.getRefID().toLowerCase();
				if (mapChrIdLowcase2Id.containsKey(chrIdLowcase)) {
					setChrId.add(mapChrIdLowcase2Id.get(chrIdLowcase));
				}
			}
		}
		
		int i = 0;
		for (String chrId : setChrId) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2(condition1 +"vs" + condition2 + " calculate chromesome: " + chrId);
				List<Double> lsRegion = new ArrayList<>();
				lsRegion.add((double) i);
				lsRegion.add(0.0);
				lsRegion.add(3.0);
				guiAnnoInfo.setLsNumInfo(lsRegion);
				runGetInfo.setRunningInfo(guiAnnoInfo);
				i++;
			}
			
			lsResult.addAll(runByChrome(chrId));
		}
		ExonSplicingTest.sortAndFdr(lsResult, fdrCutoff);
		
		if (resultFile != null) {
			String outFile = "";
			if (FileOperate.isFileDirectory(resultFile)) {
				outFile = resultFile + condition1 +"vs" + condition2 + ".txt";
			} else {
				outFile = FileOperate.changeFileSuffix(resultFile, "_"+condition1 +"vs" + condition2, "txt");
			}
			writeToFile(outFile, lsResult);
		}
		
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo2("Finish");
			guiAnnoInfo.setInfo("");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
	}
	
	private Map<String, String> getMapChrIdLowcase2Id(Set<String> setChrId) {
		Map<String, String> mapChrIdLowcase2Id = new HashMap<>();
		for (String chrId : setChrId) {
			mapChrIdLowcase2Id.put(chrId.toLowerCase(), chrId);
        }
		return mapChrIdLowcase2Id;
	}
	
	private List<ExonSplicingTest> runByChrome(String chrId) {
		MapReads mapReads = null;		
		if (isReconstructIso) {
			int invNum = isReconstructRI? 3 : 15;
			mapReads = getMapReads(mapCond2SamReader.values().iterator().next(), invNum);
		}
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo("Load Junction Info");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		loadJunctionBam(mapReads, chrId);
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setDouble(1.0);
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}

		tophatJunction.conclusion();
		
		logger.info("finish junction reads");

		GenerateNewIso generateNewIso = null;
		if (isReconstructIso) {
			generateNewIso = new GenerateNewIso(tophatJunction, mapReads, strandSpecific, isReconstructRI);
			generateNewIso.setMinIntronLen(intronMinLen);
			generateNewIso.setGffHash(gffHashGene);
			generateNewIso.setNewIsoReadsNum(newIsoReadsNum);
		}
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo("Find Splice Site");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		fillLsAll_Dif_Iso_Exon(generateNewIso, chrId);
		if (isReconstructIso) {
			generateNewIso.clear();
			generateNewIso = null;
			mapReads.clear();
			mapReads = null;
			System.gc();
		}
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setDouble(2.0);
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo("Load Expression Info");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		loadExp(chrId);
		setSplicingType();
		
		setCompareGroups(condition1, condition2);
		List<ExonSplicingTest> lsExonSplicingTests = getTestResult_FromIso(chrId);
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setDouble(2.0);
			guiAnnoInfo.setInfo2("");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		return lsExonSplicingTests;
	}
	
	/**
	 * 获得文件长度
	 * @return
	 */
	public long getFileLength() {
		long fileLength = 0;
		for (String condition : mapCond2SamReader.keySet()) {
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				fileLength = fileLength + FileOperate.getFileSizeLong(samFileReading.getFirstSamFile().getFileName());
			}
		}
		return fileLength;
	}
	
	private void loadJunctionBam(MapReads mapReads) {
		loadJunctionBam(mapReads, null);
	}
	
	private void loadJunctionBam(MapReads mapReads, String chrId) {
		AlignSeqReading samFileReadingLast = null;
		tophatJunction.setStrandSpecific(strandSpecific);

		for (String condition : mapCond2SamReader.keySet()) {
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			int i = 0;
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				
				i++;
				String group = i+"";
				tophatJunction.setCondition(condition, group);
				samFileReading.clearOther();
				samFileReading.getFirstSamFile().indexMake();
				if (samFileReadingLast != null) {
					samFileReading.setReadInfo(0L, samFileReadingLast.getReadByte());
				}
				
				if (!StringOperate.isRealNull(chrId)) {
					List<Alignment> lsAlignments = new ArrayList<>();
					long chrLen = mapChrId2Len.get(chrId);
					lsAlignments.add(new Align(chrId, 0, (int)chrLen));
					samFileReading.setLsAlignments(lsAlignments);
				}

				samFileReading.setLsAlignments(lsReadReagion);
				SamFileStatistics samStatistics = new SamFileStatistics(condition);
				samStatistics.setStandardData(mapChrId2Len);
				samFileReading.addAlignmentRecorder(tophatJunction);
				samFileReading.addAlignmentRecorder(samStatistics);
				if (mapReads != null) {
					samFileReading.addAlignmentRecorder(mapReads);
				}
				samFileReading.setUniqueMapping(isUseUniqueMappedReads);
				samFileReading.run();
				
				Map<String, double[]> mapGroup2Num = mapCond_group2ReadsNum.get(condition);
				if (mapGroup2Num == null) {
					mapGroup2Num = new HashMap<>();
				}
				mapGroup2Num.put(group, new double[]{samStatistics.getReadsNum(MappingReadsType.Mapped)});
				mapCond_group2ReadsNum.put(condition, mapGroup2Num);
				samFileReading.clearRecorder();
				samFileReadingLast = samFileReading;
			}
		}
			
		samFileReadingLast = null;
	}
	
	private void fillLsAll_Dif_Iso_Exon(GenerateNewIso generateNewIso) {
		fillLsAll_Dif_Iso_Exon(generateNewIso, null);
	}
	
	
	/** 从全基因组中获取差异的可变剪接事件，放入lsSplicingTest中 */
	private void fillLsAll_Dif_Iso_Exon(GenerateNewIso generateNewIso, String chrId) {
		List<GffDetailGene> lsGffDetailGenes = gffHashGene.getLsGffDetailGenes();

		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
//			logger.debug(gffDetailGene.getNameSingle());
			
			if (!StringOperate.isRealNull(chrId) && !gffDetailGene.getRefID().equalsIgnoreCase(chrId)) {
				continue;
			}
			
			gffDetailGene = GenerateNewIso.getGeneWithSameStrand(gffDetailGene);
			//TODO 设置断点
			if (gffDetailGene.getName().contains(stopGeneName)) {
				logger.debug("stop");
			}

			logger.info("reconstruct splicing event " + gffDetailGene.getNameSingle());
			reconstructIso(generateNewIso, gffDetailGene);
			gffDetailGene.removeDupliIso();
			
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
			List<ExonClusterSite> lsExonSplicingTest = getGeneDifExon(gffDetailGene);
			if (lsExonSplicingTest.size() == 0) {
				continue;
			}
			lsSplicingTests.add(lsExonSplicingTest);
		}
		logger.debug("finish");
	}
	
	private void reconstructIso(GenerateNewIso generateNewIso, GffDetailGene gffDetailGene) {
		if (gffDetailGene != null && generateNewIso != null) {
			generateNewIso.setGffDetailGene(gffDetailGene);
			generateNewIso.reconstructGffDetailGene();
		}
	}
	
	/**
	 * 计数，看有多少iso与gffDetailGene同方向，如果只有一个，则跳过该基因
	 * @param gffDetailGene
	 * @return
	 */
	private boolean isOnlyOneIso(GffDetailGene gffDetailGene) {
		int tmpIso = 0;
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.isCis5to3() == gffDetailGene.isCis5to3()) {
				tmpIso++;
			}
		}
		return tmpIso <=1 ? true : false;
	}
	/**
	 * 获得每个 gffDetailGene中的所有差异exon，包装成 LsExonSplicingTest 并返回
	 * @param gffDetailGene
	 * @return
	 */
	private List<ExonClusterSite> getGeneDifExon(GffDetailGene gffDetailGene) {
		//TODO 设置断点
		if (gffDetailGene.getName().contains(stopGeneName)) {
			logger.debug("stop");
		}
		//TODO
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGene);
		List<ExonClusterSite> lsExonSplicingTestResult = exonClusterExtract.getLsDifExonSite();
		if (!lsExonSplicingTestResult.isEmpty()) {
			for (ExonClusterSite exonSite : lsExonSplicingTestResult) {
				exonSite.generateExonTestUnit(juncAllReadsNum, juncSampleReadsNum,
						setIsoName_No_Reconstruct, pvalueJunctionProp, isCombine,
						mapCond_group2ReadsNum, setCondition, tophatJunction, minDifLen);
				
			}
		}
		return lsExonSplicingTestResult;
	}
	
	private void loadExp() {
		loadExp(null);
	}
	
	private void loadExp(String chrId) {
		AlignSamReading samFileReadingLast = null;
		for (String condition : mapCond2SamReader.keySet()) {
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			int i = 0;
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				i++;
				String group = i+"";
				samFileReading.clearOther();
				if (samFileReadingLast != null) {
					samFileReading.setReadInfo(0L, samFileReadingLast.getReadByte());
				}
				samFileReadingLast = samFileReading;
				
				if (!StringOperate.isRealNull(chrId)) {
					List<Alignment> lsAlignments = new ArrayList<>();
					long chrLen = mapChrId2Len.get(chrId);
					lsAlignments.add(new Align(chrId, 0, (int)chrLen));
					samFileReading.setLsAlignments(lsAlignments);
				}
				
				samFileReading.setLsAlignments(lsReadReagion);
				add_RetainIntron_Into_SamReading(chrId, condition, i+"", samFileReading);
				MapReadsAbs mapReadsAbs = null;
				if (isLessMemory) {
					mapReadsAbs = getSamMapReads(samFileReading);
				} else {
					mapReadsAbs = getMapReads(samFileReading, 15);
					samFileReading.addAlignmentRecorder((MapReads)mapReadsAbs);
				}
				samFileReading.setUniqueMapping(isUseUniqueMappedReads);
				samFileReading.run();
				samFileReading.clearRecorder();
				addMapReadsInfo(chrId, condition, group, mapReadsAbs);
			}
		}
		
		System.gc();
	}
	
	private void add_RetainIntron_Into_SamReading(String chrId, String condition, String group,  AlignSamReading samFileReading) {
		List<PredictRetainIntron> lsRetainIntrons = new ArrayList<>();
		for (List<ExonClusterSite> lsExonSplicingTests : lsSplicingTests) {
			for (ExonClusterSite exonClusterSite : lsExonSplicingTests) {
				for (ExonSplicingTest exonSplicingTest : exonClusterSite.getLsExonSplicingTests()) {
					if (!StringOperate.isRealNull(chrId) && !exonSplicingTest.getExonCluster().getRefID().equalsIgnoreCase(chrId)) {
						continue;
					}
					lsRetainIntrons.addAll(exonSplicingTest.getLsRetainIntron());
				}
			}
		}
		
		for (PredictRetainIntron predictRetainIntron : lsRetainIntrons) {
			predictRetainIntron.setCondition_DifGroup(condition, group);
		}
		samFileReading.addColAlignmentRecorder(lsRetainIntrons);
	}
	
	private MapReadsAbs getSamMapReads(AlignSamReading samFileReading) {
		SamMapReads samMapReads = new SamMapReads(samFileReading.getFirstSamFile(), strandSpecific);
		samMapReads.setisUniqueMapping(true);
		samMapReads.setNormalType(EnumMapNormalizeType.no_normalization);
		return samMapReads;
	}
	
	/**
	 * @param samFileReading
	 * @param invNum 采样频率，也就是每多少位点采一次样
	 * @return
	 */
	private MapReads getMapReads(AlignSeqReading samFileReading, int invNum) {
		MapReads mapReads = new MapReads();
		mapReads.setInvNum(invNum);
		mapReads.setNormalType(EnumMapNormalizeType.no_normalization);
		mapReads.setisUniqueMapping(true);
		mapReads.prepareAlignRecord(samFileReading.getFirstSamFile().readFirstLine());
		//TODO 可以考虑从gtf文件中获取基因组长度然后给MapReads使用
		mapReads.setMapChrID2Len(mapChrId2Len);
		return mapReads;
	}

	/** 将表达信息加入统计 */
	private void addMapReadsInfo(String chrId, String condition, String group, MapReadsAbs mapReads) {
		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		int num = 0;
		for (List<ExonClusterSite> lsExonTest : lsSplicingTests) {
			for (ExonClusterSite exonClusterSite : lsExonTest) {
				if (!StringOperate.isRealNull(chrId) && !exonClusterSite.getCurrentExonCluster().getRefID().equalsIgnoreCase(chrId)) {
					continue;
				}
				
				if (exonClusterSite.getCurrentExonCluster().getParentGene().getName().contains(stopGeneName)) {
					logger.debug("stop");
				}
				exonClusterSite.addMapCondition2MapReads(condition, group, mapReads);
			}
			if (num % 100 == 0) {
				logger.info("do " + num + " events");
			}
			num ++;
		}
	}
	
	private void setSplicingType() {
		for (List<ExonClusterSite> lstest : lsSplicingTests) {
			for (ExonClusterSite clusterSite : lstest) {
				if (clusterSite.getCurrentExonCluster().getParentGene().getName().contains(stopGeneName)) {
					logger.debug("stop");
				}
				
				logger.info("Set Splicing Type " + clusterSite.getCurrentExonCluster().getParentGene().getName());
				clusterSite.setSpliceType2Value();				
			}
		}
	}
	
	private List<ExonSplicingTest> getTestResult_FromIso() {
		return getTestResult_FromIso(null);
	}
	
	/** 获得检验完毕的test
	 * @param num 已经跑掉几个测试了，这个仅仅用在gui上
	 * @return
	 */
	private List<ExonSplicingTest> getTestResult_FromIso(String chrId) {
		setConditionWhileConditionIsNull();

		List<ExonSplicingTest> lsResult = new ArrayList<>();
		
		for (List<ExonClusterSite> lsIsoExonSplicingTests : lsSplicingTests) {
			if (lsIsoExonSplicingTests.isEmpty()) continue;
			
			if (lsIsoExonSplicingTests.get(0).getCurrentExonCluster().getParentGene().getName().contains("AP003068.9")) {
				logger.debug("");
			}
			
			if (!StringOperate.isRealNull(chrId) && !lsIsoExonSplicingTests.get(0).getCurrentExonCluster().getRefID().equalsIgnoreCase(chrId)) {
				continue;
			}
			
			List<ExonSplicingTest> lsIsoExonSplicingResult = doTest(lsIsoExonSplicingTests);
			if (lsIsoExonSplicingResult.isEmpty()) {
				logger.info("gene " + lsIsoExonSplicingTests.get(0).getCurrentExonCluster().getParentGene().getNameSingle() + " have only unknown splicing site");
				continue;
			}
			lsIsoExonSplicingResult = combineMXE(lsIsoExonSplicingResult);
			lsIsoExonSplicingResult = combineMultiSE(lsIsoExonSplicingResult);
			if (oneGeneOneSpliceEvent) {
				lsResult.add(lsIsoExonSplicingResult.get(0));
			} else {
				lsResult.addAll(lsIsoExonSplicingResult);
			}
			if (flagStop) {
				break;
			}
			suspendCheck();
		}
		
		//去除相同位点，仅选择pvalue小的那一个
		Map<String, ExonSplicingTest> mapKey2SpliceTest = new LinkedHashMap<>();
		for (ExonSplicingTest exonSplicingTest : lsResult) {
			if (exonSplicingTest.getExonCluster().getStartAbs() == 204984027) {
				logger.debug("");
			}
			String key = exonSplicingTest.getSpliceSite().trim();
			ExonSplicingTest testOld = mapKey2SpliceTest.get(key);
			if (testOld != null) {
				if (testOld.getPvalue() > exonSplicingTest.getPvalue()) {
					continue;
                }
				if (testOld.getPvalue() == exonSplicingTest.getPvalue()) {
					 int geneLenOld = testOld.getExonCluster().getParentGene().getLongestSplitMrna().getLenExon(0);
					 int geneLen = exonSplicingTest.getExonCluster().getParentGene().getLongestSplitMrna().getLenExon(0);
					 if (geneLenOld >= geneLen) {
						 continue;
	                }
                }
		
            }
			mapKey2SpliceTest.put(key, exonSplicingTest);
        }
		
		lsResult = new ArrayList<>(mapKey2SpliceTest.values());
		return lsResult;
	}

	/**
	 * 如果没有设定condition
	 * 则跟据setCondition的信息自动设定condition
	 */
	private void setConditionWhileConditionIsNull() {
		if (condition1 == null && condition2 == null) {
			ArrayList<String> lsCondition = ArrayOperate.getArrayListValue(setCondition);
			condition1 = lsCondition.get(0);
			condition2 = lsCondition.get(1);
		}
	}
	/**
	 * 计算每个时期的exon的差值
	 * @param lsExonClusters
	 * @return ls ExonSplicingTest -- ls 每个时期 -- 所涉及到的exon的检验结果，按照pvalue从小到大排序
	 */
	private List<ExonSplicingTest> doTest(List<ExonClusterSite> lsExonClusterSites) {
		List<ExonSplicingTest> lsFilter = new ArrayList<>();
		for (ExonClusterSite exonClusterSite : lsExonClusterSites) {
			
			if (exonClusterSite.getCurrentExonCluster().getParentGene().getName().contains(stopGeneName)) {
				logger.debug("");
			}
			exonClusterSite.setCompareConditionAndCalculate(condition1, condition2);
			List<ExonSplicingTest> lsClusterTest =exonClusterSite.getLsTestResult();
			lsFilter.addAll(lsClusterTest);
		}
		return lsFilter;
	}
	
	/**
	 * 将 mutually exclusive的exon合并起来，就保留一个exon
	 * @param lsTestResult
	 * @return
	 */
	private 	List<ExonSplicingTest> combineMXE(List<ExonSplicingTest> lsTestResult) {
		List<ExonSplicingTest> lsFinal = new ArrayList<>();
		
		ArrayListMultimap<String, ExonSplicingTest> mapJuncInfo2ExonTest = ArrayListMultimap.create();
		//将MXE位点提取出来，用junction的数字作为key
		for (ExonSplicingTest exonSplicingTest : lsTestResult) {
			if (exonSplicingTest.getSplicingType() == SplicingAlternativeType.mutually_exclusive) {
				PvalueCalculate pvalueCalculate = exonSplicingTest.getSpliceTypePvalue();
				String[] treat = pvalueCalculate.getStrInfo(false, false).split("::");
				String[] ctrl = pvalueCalculate.getStrInfo(false, true).split("::");
				String combine = treat[1] + "::" + treat[0] + SepSign.SEP_ID + ctrl[1] + "::" + ctrl[0];
				mapJuncInfo2ExonTest.put(combine, exonSplicingTest);
			} else {
				lsFinal.add(exonSplicingTest);
			}
		}
		if (mapJuncInfo2ExonTest.isEmpty()) {
			return lsFinal;
		}
		Map<ExonSplicingTest, ExonSplicingTest> mapKey2Value = new HashMap<>();
		Set<ExonSplicingTest> setExonTest = new HashSet<>();//去重复用
		//找出配对的MXE位点。筛选方法是mxe位点的reads数是相关的，如下：
		//
		for (ExonSplicingTest exonSplicingTest : mapJuncInfo2ExonTest.values()) {
			if (setExonTest.contains(exonSplicingTest)) continue;
			
			PvalueCalculate pvalueCalculate = exonSplicingTest.getSpliceTypePvalue();
			String[] treat = pvalueCalculate.getStrInfo(false, false).split("::");
			String[] ctrl = pvalueCalculate.getStrInfo(false, true).split("::");
			//注意跟上面方向相反
			String combine = treat[0] + "::" + treat[1] + SepSign.SEP_ID + ctrl[0] + "::" + ctrl[1];
			
			List<ExonSplicingTest> lsExonSplicingTests = mapJuncInfo2ExonTest.get(combine);
			if (lsExonSplicingTests == null) {
				mapKey2Value.put(exonSplicingTest, null);
				setExonTest.add(exonSplicingTest);
			} else {
				int midExon = middle(exonSplicingTest);
				int distance = Integer.MAX_VALUE;
				ExonSplicingTest exonTestPair = null;
				for (ExonSplicingTest test : lsExonSplicingTests) {
					if (!setExonTest.contains(test) && Math.abs(midExon - middle(test)) < distance) {
						distance = Math.abs(midExon - middle(test));
						exonTestPair = test;
					}
				}
				if (exonTestPair != null) {
					setExonTest.add(exonTestPair);
				}
				mapKey2Value.put(exonSplicingTest, exonTestPair);
			}			
		}
		
		for (ExonSplicingTest keyTest : mapKey2Value.keySet()) {
			ExonSplicingTest value = mapKey2Value.get(keyTest);
			if (value == null) {
				lsFinal.add(keyTest);
			} else {
				Align a = keyTest.getSpliceSiteAlignDisplay();
				Align b = value.getSpliceSiteAlignDisplay();
				if (a == null && b == null) {
					continue;
				} else if (a == null) {
					lsFinal.add(value);
				} else if (b == null) {
					lsFinal.add(keyTest);
				} else {
					int start = Math.min(a.getStartAbs(), b.getStartAbs());
					int end = Math.max(a.getEndAbs(), b.getEndAbs());
					Align alignDisplay = new Align(keyTest.getExonCluster().getRefID(), start, end);
					alignDisplay.setCis5to3(a.isCis5to3());
					if (keyTest.getAndCalculatePvalue() < value.getAndCalculatePvalue()) {
						keyTest.setAlignDisplay(alignDisplay);
						lsFinal.add(keyTest);
					} else {
						value.setAlignDisplay(alignDisplay);
						lsFinal.add(value);
					}
				}
			}
		}
		
		//按照pvalue从小到大排序
		Collections.sort(lsFinal, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getAndCalculatePvalue().compareTo(o2.getAndCalculatePvalue());
			}
		});
		return lsFinal;
	}
	
	/** 去除重复的multise */
	private 	List<ExonSplicingTest> combineMultiSE(List<ExonSplicingTest> lsTestResult) {
		if (lsTestResult.get(0).getExonCluster().getParentGene().getName().contains(stopGeneName)) {
			logger.debug("");
		}
		
		List<ExonSplicingTest> lsResult = new ArrayList<>();
		
		List<Align> lsAlignMultiSE = new ArrayList<>();
		Map<Align, ExonSplicingTest> mapAlign2Exon = new HashMap<>();
		for (ExonSplicingTest exonSplicingTest : lsTestResult) {
			if (exonSplicingTest.getSplicingType() == SplicingAlternativeType.cassette_multi) {
				Align alignMultiSE = exonSplicingTest.getSpliceSiteAlignDisplay();
				alignMultiSE.setCis5to3(exonSplicingTest.getExonCluster().isCis5to3());
				mapAlign2Exon.put(alignMultiSE, exonSplicingTest);
				lsAlignMultiSE.add(alignMultiSE);
			} else {
				lsResult.add(exonSplicingTest);
			}
		}
		
		if (lsAlignMultiSE.isEmpty()) {
			return lsResult;
		}
		
		if (lsTestResult.get(0).getExonCluster().isCis5to3()) {
			Collections.sort(lsAlignMultiSE, new CompCisAlign());
		} else {
			Collections.sort(lsAlignMultiSE, new CompTransAlign());
		}
		
		/** 将multiSE进行分组，因为是这个样子的
		 * --------------10==20---------------50==60-------------------
		 * 在本组内就存在 10==20 和 50==60 以及 10==60 三组，这时候我们需要将最长的那个挑出来
		 */
		List<int[]> lsSep = ListAbs.getLsElementSep(lsTestResult.get(0).getExonCluster().isCis5to3(), lsAlignMultiSE);
		ArrayListMultimap<int[], Align> mapSite2LsAlign = ArrayListMultimap.create();
		for (int[] is : lsSep) {
			for (Align align : lsAlignMultiSE) {
				if (align.getStartAbs() >= is[0] && align.getEndAbs() <= is[1]) {
					mapSite2LsAlign.put(is, align);
				}
			}
		}
		
		for (int[] is : mapSite2LsAlign.keys()) {
			List<Align> lsAligns = mapSite2LsAlign.get(is);
			Align alignMax = lsAligns.get(0);
			for (Align align : lsAligns) {
				if (alignMax.getLength() < align.getLength()) {
					alignMax = align;
				}
			}
			lsResult.add(mapAlign2Exon.get(alignMax));
		}
		
		return lsResult;
	}
	
	static class CompCisAlign implements Comparator<Align> {
		public int compare(Align o1, Align o2) {
			Integer o1start = o1.getStartAbs(), o2start = o2.getStartAbs();
			Integer o1end = o1.getEndAbs(), o2end = o2.getEndAbs();
			if (o1start != o2start) {
				return o1start.compareTo(o2start);
			} else {
				return o1end.compareTo(o2end);
			}
		}
	}
	
	static class CompTransAlign implements Comparator<Align> {
		public int compare(Align o1, Align o2) {
			Integer o1start = o1.getStartCis(), o2start = o2.getStartCis();
			Integer o1end = o1.getEndCis(), o2end = o2.getEndCis();
			if (o1start != o2start) {
				return -o1start.compareTo(o2start);
			} else {
				return -o1end.compareTo(o2end);
			}
		}
	}
	
	private int middle(ExonSplicingTest exonSplicingTest) {
		return (exonSplicingTest.getExonCluster().getStartAbs() + exonSplicingTest.getExonCluster().getEndAbs())/2;
	}
	
	/** 写入文本 */
	public void writeToFile(String fileName, List<ExonSplicingTest> lsResult) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		
		boolean isGetSeq = seqHash == null ? false : true;
		TxtReadandWrite txtOutSeq = null;
		if (isGetSeq) {
			txtOutSeq = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_Seq", "fasta.gz"), true);
		}
		
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2));
		for (ExonSplicingTest chisqTest : lsResult) {
			//TODO 设定断点
			if (chisqTest.getExonCluster().getParentGene().getName().contains(stopGeneName)) {
				logger.debug("stop");
			}
			try {
				txtOut.writefileln(chisqTest.toStringArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		txtOut.close();

		if (isGetSeq) {
			for (ExonSplicingTest chisqTest : lsResult) {
				chisqTest.setGetSeq(seqHash);
				txtOutSeq.writefileln(chisqTest.toStringSeq());
			}
			txtOutSeq.close();
		}

		Map<SplicingAlternativeType, int[]> mapSplicingType2Num = statisticsSplicingEvent();
		TxtReadandWrite txtStatistics = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_statistics", "txt"), true);
		
		txtStatistics.writefileln("############################# CASH Report #############################");
		txtStatistics.writefileln("======================================================================");
		txtStatistics.writefileln("SplicingEvent\tSignificantNum\tAllNum");
		txtStatistics.writefileln("======================================================================");
		for (Entry<SplicingAlternativeType, int[]> exonSplicingInfo : mapSplicingType2Num.entrySet()) {
			SplicingAlternativeType type = exonSplicingInfo.getKey();
			if (!mapSplicingType2Num.containsKey(type)) {
				continue;
			}
			String tmpResult = exonSplicingInfo.getKey().toString() + "\t" + exonSplicingInfo.getValue()[0] + "\t" +  exonSplicingInfo.getValue()[1];
			txtStatistics.writefileln(tmpResult);
		}
		txtStatistics.writefileln("======================================================================");
		txtStatistics.writefileln("################# Report Legend ######################################");

		txtStatistics.writefileln("SplicingEvent: Types of AS event");
		txtStatistics.writefileln("\tCassette: cassette exon, i.e. SE, Skipped exon");
		txtStatistics.writefileln("\tCassette_multi: mutiple adjacent Cassette exons");
		txtStatistics.writefileln("\tMXE: Mutually exclusive exon");
		txtStatistics.writefileln("\tA5SS: Alternative 5' splice site");
		txtStatistics.writefileln("\tA3SS: Alternative 3' splice site");
		txtStatistics.writefileln("\tAltStart: Alternative start exon");
		txtStatistics.writefileln("\tAltEnd: Alternative end exon");
		txtStatistics.writefileln("\tMXE: Mutually exclusive exons");
		txtStatistics.writefileln("\tIR: intron retention");
		
		txtStatistics.writefileln("SignificantNum:	number of significant AS events between samples according to FDR (false discovery rate) < " + pvalue);
		txtStatistics.writefileln("AllNum:	total number of events detected between samples");
		txtStatistics.writefileln("sample_Skip::Others:\tThe numbers in sample_Skip::Others indicate the number of exclusion reads and the number of inclusion reads for sample_1 or for sample_2");
		txtStatistics.writefileln("sampleExp:\tThe numbers in sampleExp indicate the sequencing coverage of the splicing region relative to the whole gene for sample_1 or for sample_2");
		txtStatistics.writefileln("######################################################################");
		txtStatistics.close();
	}
		
	/** 统计可变剪接事件的map
	 * key：可变剪接类型
	 * value：int[2]
	 * 0： 差异可变剪接数量
	 * 1： 全体可变剪接数量
	 *  */
	private Map<SplicingAlternativeType, int[]> statisticsSplicingEvent() {
		Map<SplicingAlternativeType, int[]> mapSplicingType2Num = new LinkedHashMap<SplicingAlternativeType, int[]>();
		for (SplicingAlternativeType exonSplicingType : SplicingAlternativeType.values()) {
			if (SplicingAlternativeType.getSetExclude().contains(exonSplicingType)) {
				continue;
			}
			mapSplicingType2Num.put(exonSplicingType, new int[2]);
		}
		SplicingAlternativeType exonSplicingType = null;
		for (ExonSplicingTest exonSplicingTest : lsResult) {
			exonSplicingType = exonSplicingTest.getSplicingType();
			int[] tmpInfo = new int[]{0, 0};
			if (mapSplicingType2Num.containsKey(exonSplicingType)) {
				tmpInfo = mapSplicingType2Num.get(exonSplicingType);
			} else {
				continue;
//				mapSplicingType2Num.put(exonSplicingType, tmpInfo);
			}
			tmpInfo[1] ++;
			if (exonSplicingTest.getfdr() <= pvalue) {
				tmpInfo[0] ++;
			}
		}
		return mapSplicingType2Num;
	}
	
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		super.threadStop();
		for (String condition : mapCond2SamReader.keySet()) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadStop();
			}
		}
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadSuspend() {
		super.threadSuspend();
		for (String condition : mapCond2SamReader.keySet()) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadSuspend();
			}
		}
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadResume() {
		super.threadResume();
		for (String condition : mapCond2SamReader.keySet()) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadResume();
			}
		}
	}
	
	public void clear() {
		condition1 = null; condition2 = null;
		lsResult = null;
		lsSplicingTests= null;
		mapCond2SamFile.clear();
		mapCond2SamReader.clear();
		setCondition.clear();
		tophatJunction = new TophatJunction();
	}
	
	
	/** 提取某个exon周边的序列的，吴文武要的东西 */
	private static void getSeqfasta(List<String[]> lsInfo, GffChrAbs gffChrAbs, String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String[] strings : lsInfo) {
			String name = strings[0] + "_" + strings[1];
			String chrId = "chr" + strings[1].split(":")[0];
			int start = Integer.parseInt(strings[1].split(":")[1].split("-")[0]);
			int end = Integer.parseInt(strings[1].split(":")[1].split("-")[1]);
			SeqFasta seqFasta = getSeqfasta(name, chrId, start, end, gffChrAbs);
			if (seqFasta == null) {
				continue;
			}
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
	}
	
	private static SeqFasta getSeqfasta(String name, String chrId, int start, int end, GffChrAbs gffChrAbs) {
		int codMid = (start + end)/2;
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(chrId, codMid);
		if (gffCodGene == null || !gffCodGene.isInsideLoc()) {
			return null;
		}
		int exonNum = 0;
		GffGeneIsoInfo isoGetSeq = null;
		for (GffGeneIsoInfo iso : gffCodGene.getGffDetailThis().getLsCodSplit()) {
			int exonNumThis = iso.getNumCodInEle(codMid);
			if (exonNumThis > 0) {
				exonNum = exonNumThis;
				isoGetSeq = iso;
				break;
			}
		}
		if (isoGetSeq == null) {
			return null;
		}
		int exonStart = exonNum - 2;
		if (exonStart < 0) exonStart = 0;
		int exonEnd = exonNum + 1;
		if (exonEnd > isoGetSeq.size()) {
			exonEnd = isoGetSeq.size();
		}
		List<ExonInfo> lsSub = isoGetSeq.subList(exonStart, exonEnd);
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(StrandType.isoForward, isoGetSeq.getRefID(), lsSub, true);
		seqFasta.setName(name);
		return seqFasta;
	}
	
}
