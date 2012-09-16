package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * @author zong0jie
 *
 */
public class ExonJunction {
	
	public static void main(String[] args) {
		mouse();
		mouse2();
	}
	GffHashGene gffHashGene = null;

	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	HashMap<String, MapReads> mapCondition2MapReads = new HashMap<String, MapReads>();
	String condition1, condition2;
	public static void mouse() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_1/junctions.bed", "K2"); 
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_2/junctions.bed", "K2");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_1/junctions.bed", "WT2");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_2/junctions.bed", "WT2");
		exonJunction.addBamFile_Sorted("K2", parentFile + "MEFK02da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K2vsWT2outDifResult_test_bam.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public static void mouse2() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "MEFK00da14m1_1/junctions.bed", "K0"); 
		exonJunction.setIsoJunFile(parentFile + "MEFK00da14m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT0da14m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT0da14m1_2/junctions.bed", "WT0");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK00da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT0da14m1_1/accepted_hits.bam");
		Species species = new Species(10090);
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K0vsWT0outDifResult_test_bam.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public static void mouseHeart() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "heartK0a14m1_1/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "heartK0a14m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "heartWTa14m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "heartWTa14m1_2/junctions.bed", "WT0");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "heartK0a14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "heartWTa14m1_1/accepted_hits.bam");
		exonJunction.loadingBamFile(new Species(10090));

		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/HeartK0vsWT0outDifResult_bam.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public static void chicken() {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);

		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile(parentFile + "tophatK5a15m1_1/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatK5a15m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT5a15m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT5a15m1_2/junctions.bed", "WT0");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT5a15m1_1/accepted_hits.bam");
		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5outDifResult_No_modifyJunctionReads.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		txtOut.writefileln(ExonSplicingTest.getTitle("K0", "WT0"));
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void setCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	/**
	 * 设定junction文件以及所对应的时期
	 * 目前只能做两个时期的比较
	 * @param junctionFile
	 * @param condition
	 */
	public void setIsoJunFile(String junctionFile, String condition) {
		tophatJunction.setJunFile(junctionFile, condition);
		setCondition.add(condition);
	}
	public void addBamFile_Sorted(String condition, String sortedBamFile) {
		MapReads mapReads = new MapReads();
		SamFile samFile = new SamFile(sortedBamFile);
		mapReads.setAlignSeqReader(samFile);
		mapCondition2MapReads.put(condition, mapReads);
	}
	public void loadingBamFile(Species species) {
		for (MapReads mapReads : mapCondition2MapReads.values()) {
			mapReads.setInvNum(15);
			mapReads.setMapChrID2Len(species.getMapChromInfo());
			mapReads.run();
			mapReads.setNormalType(MapReads.NORMALIZATION_NO);
		}
	}
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		ArrayList<ExonSplicingTest> lsSplicingTests = getDifIsoGene();
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2));
		for (ExonSplicingTest chisqTest : lsSplicingTests) {
			//TODO 一般是15::5	13::0	这种形式
			//但有时候会出现15	13 这种明显不是转录本的
			//所以在这里检查该值并且删除，但是这时治标不治本的做法，搞清楚为啥发生
			if (chisqTest.exonCluster.getExonSplicingType() == ExonSplicingType.cassette 
					&& chisqTest.mapCondition2Counts.entrySet().iterator().next().getValue().length <=1) {
				continue;
			}
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public ArrayList<ExonSplicingTest> getDifIsoGene() {
		setConditionWhileConditionIsNull();

		ArrayList<ExonSplicingTest> lsChisqTests = new ArrayList<ExonSplicingTest>();
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			
			if (gffDetailGene.getLsCodSplit().size() <= 1)
				continue;
			if (isOnlyOneIso(gffDetailGene))
				continue;
			
			ArrayList<ExonSplicingTest> lsExonSplicingTest = calGeneDifIso(gffDetailGene);
			if (lsExonSplicingTest.size() == 0) {
				continue;
			}
			lsChisqTests.add(lsExonSplicingTest.get(0));
		}
		Collections.sort(lsChisqTests);
		return lsChisqTests;
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
		if (tmpIso <= 1) {
			return true;
		}
		return false;
	}

	private ArrayList<ExonSplicingTest> calGeneDifIso(GffDetailGene gffDetailGene) {
		//如果没有差异的exon，就返回
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		if (lsExonClusters == null || lsExonClusters.size() == 0) {
			return new ArrayList<ExonSplicingTest>();
		}
		//外显子的具体坐标
		ArrayList<String> lsLocation = new ArrayList<String>();
		return calDifExonJun(lsExonClusters, lsLocation);
	}
	
	/**
	 * 输入有差异的exon的列表，计算每个时期的exon的差值
	 * @param lsExonClusters
	 * @param lsLocation 输入一个空的list，在里面填充差异exon的坐标
	 * @return ls ExonSplicingTest -- ls 每个时期 -- 所涉及到的exon的检验结果，按照pvalue从小到大排序
	 */
	private ArrayList<ExonSplicingTest> calDifExonJun(ArrayList<ExonCluster> lsExonClusters, ArrayList<String> lsLocation) {		
		ArrayList<ExonSplicingTest> lsExonSplicingTest = new ArrayList<ExonSplicingTest>();
		for (ExonCluster exonCluster : lsExonClusters) {
			ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster, setCondition, tophatJunction);
			exonSplicingTest.setMapCondition2MapReads(mapCondition2MapReads);
			exonSplicingTest.setCondition(condition1, condition2);
			lsExonSplicingTest.add(exonSplicingTest);
		}
		//按照pvalue从小到大排序
		Collections.sort(lsExonSplicingTest, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getPvalue().compareTo(o2.getPvalue());
			}
		});
		return lsExonSplicingTest;
	}
	
	private void setConditionWhileConditionIsNull() {
		if (condition1 == null && condition2 == null) {
			ArrayList<String> lsCondition = ArrayOperate.getArrayListValue(setCondition);
			condition1 = lsCondition.get(0);
			condition2 = lsCondition.get(1);
		}
	}
}
