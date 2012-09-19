package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

/**
 * �õ�ÿ��gene��Junction�󣬿�ʼ������ɱ���ӵĲ���
 * @author zong0jie
 *
 */
public class ExonJunction {
	private static Logger logger = Logger.getLogger(ExonJunction.class);
	public static void main(String[] args) {
		mouse();
//		mouse2();
//		mouseHeart();
//		chicken();
	}
	GffHashGene gffHashGene = null;
	/** ȫ���������������
	 * ls--
	 * ls��gene
	 * ExonSplicingTest��difexon
	 *  */
	ArrayList<ArrayList<ExonSplicingTest>> lsSplicingTests;
	
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	HashMapLsValue<String, MapReads> mapCondition2MapReads = new HashMapLsValue<String, MapReads>();
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
		exonJunction.addBamFile_Sorted("K2", parentFile + "MEFK02da14m1_2/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_1/accepted_hits.bam");
		exonJunction.addBamFile_Sorted("WT2", parentFile + "MEFWT2da14m1_2/accepted_hits.bam");
		Species species = new Species(10090);
		System.out.println(species.getVersionAll().get(1));
		species.setVersion(species.getVersionAll().get(1));
		exonJunction.loadingBamFile(species);
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K2vsWT2outDifResult_test_bamNew.xls";
		exonJunction.writeToFile(outResult);
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
		exonJunction.writeToFile(outResult);
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
		exonJunction.writeToFile(outResult);
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
		exonJunction.writeToFile(outResult);
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		lsSplicingTests = new ArrayList<ArrayList<ExonSplicingTest>>();
		setLsAll_Dif_Iso_Exon();
	}
	public void setLsAll_Dif_Iso_Exon() {
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			
			if (gffDetailGene.getLsCodSplit().size() <= 1)
				continue;
			if (isOnlyOneIso(gffDetailGene))
				continue;
			
			ArrayList<ExonSplicingTest> lsExonSplicingTest = getGeneDifExon(gffDetailGene);
			if (lsExonSplicingTest.size() == 0) {
				continue;
			}
			lsSplicingTests.add(lsExonSplicingTest);
		}
	}
	/**
	 * ���������ж���iso��gffDetailGeneͬ�������ֻ��һ�����������û���
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
	 * ���ÿ�� gffDetailGene�е����в���exon����װ�� LsExonSplicingTest ������
	 * @param gffDetailGene
	 * @return
	 */
	private ArrayList<ExonSplicingTest> getGeneDifExon(GffDetailGene gffDetailGene) {
		ArrayList<ExonSplicingTest> lsExonSplicingTestResult = new ArrayList<ExonSplicingTest>();
		
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		if (lsExonClusters != null && lsExonClusters.size() != 0) {
			for (ExonCluster exonCluster : lsExonClusters) {
				ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster);
				lsExonSplicingTestResult.add(exonSplicingTest);
			}
		}
		return lsExonSplicingTestResult;
	}
	
	public void setCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	/**
	 * �趨junction�ļ��Լ�����Ӧ��ʱ��
	 * Ŀǰֻ��������ʱ�ڵıȽ�
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
		for (Entry<String, ArrayList<MapReads>> cond2MapReads : mapCondition2MapReads.entrySet()) {
			String condition = cond2MapReads.getKey();
			ArrayList<MapReads> lsMapReads = cond2MapReads.getValue();
			for (MapReads mapReads : lsMapReads) {
				mapReads.setInvNum(15);
				mapReads.setMapChrID2Len(species.getMapChromInfo());
				mapReads.run();
				mapReads.setNormalType(MapReads.NORMALIZATION_NO);
				for (ArrayList<ExonSplicingTest> lsExonTest : lsSplicingTests) {
					for (ExonSplicingTest exonSplicingTest : lsExonTest) {
						exonSplicingTest.setMapCondition2MapReads(condition, mapReads);
					}
				}
				mapReads = new MapReads();
			}
		}
	}
	/**
	 * ��ü�����ϵ�test
	 * @return
	 */
	public ArrayList<ExonSplicingTest> getTestResult_OneExonPerIso() {
		setConditionWhileConditionIsNull();

		ArrayList<ExonSplicingTest> lsResult = new ArrayList<ExonSplicingTest>();
		for (ArrayList<ExonSplicingTest> lsIsoExonSplicingTests : lsSplicingTests) {
			doTest(lsIsoExonSplicingTests);
			lsResult.add(lsIsoExonSplicingTests.get(0));
		}
		sortLsExonTest_Use_Pvalue(lsResult);
		return lsResult;
	}
	private void setConditionWhileConditionIsNull() {
		if (condition1 == null && condition2 == null) {
			ArrayList<String> lsCondition = ArrayOperate.getArrayListValue(setCondition);
			condition1 = lsCondition.get(0);
			condition2 = lsCondition.get(1);
		}
	}
	/**
	 * ����ÿ��ʱ�ڵ�exon�Ĳ�ֵ
	 * @param lsExonClusters
	 * @return ls ExonSplicingTest -- ls ÿ��ʱ�� -- ���漰����exon�ļ�����������pvalue��С��������
	 */
	private void doTest(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			exonSplicingTest.setConditionsetAndJunction(setCondition, tophatJunction);
			exonSplicingTest.setCompareCondition(condition1, condition2);
		}
		//����pvalue��С��������
		sortLsExonTest_Use_Pvalue(lsExonSplicingTest);
	}
	/** д���ı� */
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		ArrayList<ExonSplicingTest> lsSplicingTests = getTestResult_OneExonPerIso();
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2));
		for (ExonSplicingTest chisqTest : lsSplicingTests) {
			//TODO һ����15::5	13::0	������ʽ
			//����ʱ������15	13 �������Բ���ת¼����
			//�������������ֵ����ɾ����������ʱ�α겻�α��������������Ϊɶ����
			if (chisqTest.exonCluster.getExonSplicingType() == ExonSplicingType.cassette 
					&& chisqTest.mapCondition2Counts.entrySet().iterator().next().getValue().length <=1) {
				continue;
			}
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}
	
	private void sortLsExonTest_Use_Pvalue(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		//����pvalue��С��������
		Collections.sort(lsExonSplicingTest, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getAndCalculatePvalue().compareTo(o2.getAndCalculatePvalue());
			}
		});
	}
}
