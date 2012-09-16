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
 * �õ�ÿ��gene��Junction�󣬿�ʼ������ɱ���ӵĲ���
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
		if (tmpIso <= 1) {
			return true;
		}
		return false;
	}

	private ArrayList<ExonSplicingTest> calGeneDifIso(GffDetailGene gffDetailGene) {
		//���û�в����exon���ͷ���
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		if (lsExonClusters == null || lsExonClusters.size() == 0) {
			return new ArrayList<ExonSplicingTest>();
		}
		//�����ӵľ�������
		ArrayList<String> lsLocation = new ArrayList<String>();
		return calDifExonJun(lsExonClusters, lsLocation);
	}
	
	/**
	 * �����в����exon���б�����ÿ��ʱ�ڵ�exon�Ĳ�ֵ
	 * @param lsExonClusters
	 * @param lsLocation ����һ���յ�list��������������exon������
	 * @return ls ExonSplicingTest -- ls ÿ��ʱ�� -- ���漰����exon�ļ�����������pvalue��С��������
	 */
	private ArrayList<ExonSplicingTest> calDifExonJun(ArrayList<ExonCluster> lsExonClusters, ArrayList<String> lsLocation) {		
		ArrayList<ExonSplicingTest> lsExonSplicingTest = new ArrayList<ExonSplicingTest>();
		for (ExonCluster exonCluster : lsExonClusters) {
			ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster, setCondition, tophatJunction);
			exonSplicingTest.setMapCondition2MapReads(mapCondition2MapReads);
			exonSplicingTest.setCondition(condition1, condition2);
			lsExonSplicingTest.add(exonSplicingTest);
		}
		//����pvalue��С��������
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
