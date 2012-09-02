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
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
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
		chicken();
	}
	static GffHashGene gffHashGene = null;

	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	HashMap<String, MapReads> mapCondition2MapReads = new HashMap<String, MapReads>();
	
	public static void mouse() {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_1/junctions.bed", "K0"); 
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_2/junctions.bed", "WT0");
		exonJunction.addBamFile_Sorted("K0", parentFile + "MEFK02da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
		exonJunction.loadingBamFile(new Species(10090));
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K2vsWT2outDifResult_test_bam.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public static void mouseHeart() {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "heartK0a14m1_1/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "heartK0a14m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "heartWTa14m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "heartWTa14m1_2/junctions.bed", "WT0");
		
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/HeartK0vsWT0outDifResult.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	public static void chicken() {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile(parentFile + "tophatK5a15m1_1/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatK5a15m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT5a15m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT5a15m1_2/junctions.bed", "WT0");
		
		exonJunction.addBamFile_Sorted("K0", parentFile + "tophatK5a15m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT0", parentFile + "tophatWT5a15m1_1/accepted_hits.bam");
		exonJunction.loadingBamFile(new Species(9013));
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5outDifResult.xls";
		ArrayList<ExonSplicingTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		txtOut.writefileln(ExonSplicingTest.getTitle("K0", "WT0"));
		for (ExonSplicingTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
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
	public ArrayList<ExonSplicingTest> getDifIsoGene() {
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
			ArrayList<String> lsCondition = ArrayOperate.getArrayListValue(setCondition);
			exonSplicingTest.setCondition(lsCondition.get(0), lsCondition.get(1));
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

}
