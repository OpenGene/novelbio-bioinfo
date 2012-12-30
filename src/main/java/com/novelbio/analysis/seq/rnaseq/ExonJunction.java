package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileReading;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * �õ�ÿ��gene��Junction�󣬿�ʼ������ɱ���ӵĲ���
 * @author zong0jie
 */
public class ExonJunction extends RunProcess<ExonJunctionGuiInfo> {
	private static Logger logger = Logger.getLogger(ExonJunction.class);

	GffHashGene gffHashGene = null;
	/** ȫ���������������
	 * ls--
	 * ls��gene
	 * ExonSplicingTest��difexon
	 *  */
	ArrayList<ArrayList<ExonSplicingTest>> lsSplicingTests;
	
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	String condition1, condition2;
	
	ArrayListMultimap<String, SamFileReading> mapCond2SamReader = ArrayListMultimap.create();
	
	
	/** ͳ�ƿɱ�����¼���map
	 * key���ɱ��������
	 * value��int[2]
	 * 0�� ����ɱ��������
	 * 1�� ȫ��ɱ��������
	 *  */
	HashMap<ExonSplicingType, int[]> mapSplicingType2Num = new LinkedHashMap<ExonSplicingType, int[]>();
	double pvalue = 0.05;//��ʾ����ɱ���ӵ��¼���pvalue��ֵ
	
	/** 
	 * һ����������ж���ɱ�����¼����������ǿ���ֻ��ѡ�������������Ǹ��ɱ�����¼�
	 * Ҳ�������ȫ���Ŀɱ�����¼�
	 * ÿ������ֻ��һ���ɱ�����¼�
	 */
	boolean oneGeneOneSpliceEvent = true;
	
	SeqHash seqHash;
	
	
	/**
	 * ��ʾ����ɱ���ӵ��¼���pvalue��ֵ��������ͳ�Ʋ���ɱ�����¼��������������ڿɱ���ӵ�ɸѡ
	 * @param pvalue
	 */
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	/** 
	 * һ����������ж���ɱ�����¼����������ǿ���ֻ��ѡ�������������Ǹ��ɱ�����¼�
	 * Ҳ�������ȫ���Ŀɱ�����¼�
	 * @param oneGeneOneSpliceEvent true:  ÿ������ֻ��һ���ɱ�����¼�, Ĭ��Ϊtrue
	 * false: ÿ���������ȫ���ɱ�����¼�
	 */
	public void setOneGeneOneSpliceEvent(boolean oneGeneOneSpliceEvent) {
		this.oneGeneOneSpliceEvent = oneGeneOneSpliceEvent;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		lsSplicingTests = new ArrayList<ArrayList<ExonSplicingTest>>();
		fillLsAll_Dif_Iso_Exon();
	}
	public void setSeqHash(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	private void fillLsAll_Dif_Iso_Exon() {
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
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
				if (exonCluster.isAtEdge() || exonCluster.isNotSameTss_But_SameEnd()) {
					continue;
				}
				ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster);
				lsExonSplicingTestResult.add(exonSplicingTest);
			}
		}
		return lsExonSplicingTestResult;
	}

	public void setCompareGroups(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	/**
	 * �趨junction�ļ��Լ�����Ӧ��ʱ��
	 * Ŀǰֻ��������ʱ�ڵıȽ�
	 * @param condition
	 * @param junctionFile
	 */
	public void setIsoJunFile(String condition,String junctionFile) {
		tophatJunction.setJunFile(condition,junctionFile);
		setCondition.add(condition);
	}
	
	public void addBamSorted(String condition, String sortedBamFile) {
		SamFileReading samFileReading = new SamFileReading(new SamFile(sortedBamFile));
		mapCond2SamReader.put(condition, samFileReading);
	}
	
	public void running() {
		loadBamFile();
		getTestResult_FromIso();
	}
	
	public void loadBamFile() {
		TophatJunction tophatJunction = new TophatJunction();
		for (String condition : mapCond2SamReader.keySet()) {
			tophatJunction.setCondition(condition);
			List<SamFileReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (SamFileReading samFileReading : lsSamFileReadings) {
				MapReads mapReads = new MapReads();
				mapReads.setInvNum(15);
				mapReads.setNormalType(MapReads.NORMALIZATION_NO);

				//TODO ���Կ��Ǵ�gtf�ļ��л�ȡ�����鳤��Ȼ���MapReadsʹ��
				if (seqHash != null) {
					mapReads.setMapChrID2Len(seqHash.getMapChrLength());
				}
				samFileReading.addAlignmentRecorder(tophatJunction);
				samFileReading.addAlignmentRecorder(mapReads);
				
				samFileReading.setRunGetInfo(runGetInfo);
				samFileReading.run();
				addMapReadsInfo(condition, mapReads);
				mapReads = null;
				
				ExonJunctionGuiInfo exonJunctionGuiInfo = new ExonJunctionGuiInfo();
				//TODO
				setRunInfo(exonJunctionGuiInfo);
			}
		}
	}
	
	/** �������Ϣ����ͳ�� */
	private void addMapReadsInfo(String condition, MapReads mapReads) {
		for (ArrayList<ExonSplicingTest> lsExonTest : lsSplicingTests) {
			for (ExonSplicingTest exonSplicingTest : lsExonTest) {
				exonSplicingTest.addMapCondition2MapReads(condition, mapReads);
			}
		}
	}

	/** ��ü�����ϵ�test */
	public ArrayList<ExonSplicingTest> getTestResult_FromIso() {
		initialMapSplicingType2Num();
		setConditionWhileConditionIsNull();

		ArrayList<ExonSplicingTest> lsResult = new ArrayList<ExonSplicingTest>();
		for (ArrayList<ExonSplicingTest> lsIsoExonSplicingTests : lsSplicingTests) {
			doTest_And_StatisticSplicingEvent(lsIsoExonSplicingTests);
			lsIsoExonSplicingTests = filterExon(lsIsoExonSplicingTests);
			if (oneGeneOneSpliceEvent) {
				lsResult.add(lsIsoExonSplicingTests.get(0));
			} else {
				lsResult.addAll(lsIsoExonSplicingTests);
			}
		}
		sortLsExonTest_Use_Pvalue(lsResult);
		return lsResult;
	}
	
	private void initialMapSplicingType2Num() {
		mapSplicingType2Num.clear();
		for (ExonSplicingType exonSplicingType : ExonSplicingType.getMapName2SplicingEvents().values()) {
			mapSplicingType2Num.put(exonSplicingType, new int[2]);
		}
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
	private void doTest_And_StatisticSplicingEvent(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			exonSplicingTest.setConditionsetAndJunction(setCondition, tophatJunction);
			exonSplicingTest.setCompareCondition(condition1, condition2);
		}
		//����pvalue��С��������
		sortLsExonTest_Use_Pvalue(lsExonSplicingTest);
		statisticsSplicingEvent(lsExonSplicingTest);
	}
	
	/**
	 *  һ����15::5	13::0	������ʽ
	 * ����ʱ������15	13 �������Բ���ת¼����
	 * �������������ֵ����ɾ�������������α겻�α��������������Ϊɶ����
	 * @param lsIsoExonSplicingTests
	 * @return
	 */
	private ArrayList<ExonSplicingTest> filterExon(ArrayList<ExonSplicingTest> lsIsoExonSplicingTests) {
		ArrayList<ExonSplicingTest> lsResult = new ArrayList<ExonSplicingTest>();
		for (ExonSplicingTest exonSplicingTest : lsIsoExonSplicingTests) {
			if (exonSplicingTest.getExonCluster().getExonSplicingType() == ExonSplicingType.cassette 
					&& exonSplicingTest.mapCondition2Counts.entrySet().iterator().next().getValue().length <=1) {
				continue;
			}
			lsResult.add(exonSplicingTest);
		}
		return lsResult;
	}

	/**
	 * ͳ�ƿɱ�����¼�
	 * @param lsExonSplicingTest
	 */
	private void statisticsSplicingEvent(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		ExonSplicingType exonSplicingType = null;
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			exonSplicingType = exonSplicingTest.getExonCluster().getExonSplicingType();
			int[] tmpInfo = new int[]{0, 0};
			if (mapSplicingType2Num.containsKey(exonSplicingType)) {
				tmpInfo = mapSplicingType2Num.get(exonSplicingType);
			} else {
				mapSplicingType2Num.put(exonSplicingType, tmpInfo);
			}
			tmpInfo[1] ++;
			if (exonSplicingTest.getAndCalculatePvalue() <= pvalue) {
				tmpInfo[0] ++;
			}
		}
	}
	
	/** д���ı� */
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		ArrayList<ExonSplicingTest> lsSplicingTests = getTestResult_FromIso();
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2,true));
		for (ExonSplicingTest chisqTest : lsSplicingTests) {
			chisqTest.setGetSeq(seqHash);
			txtOut.writefileln(chisqTest.toStringArray());
		}
		txtOut.close();
		
		TxtReadandWrite txtStatistics = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_statistics", "txt"), true);
		txtStatistics.writefileln("SplicingEvent\tSignificantNum\tAllNum");
		for (Entry<ExonSplicingType, int[]> exonSplicingInfo : mapSplicingType2Num.entrySet()) {
			String tmpResult = exonSplicingInfo.getKey() + "\t" + exonSplicingInfo.getValue()[0] + "\t" +  exonSplicingInfo.getValue()[1];
			txtStatistics.writefileln(tmpResult);
		}
		txtStatistics.close();
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

class ExonJunctionGuiInfo {
	String labInfo;
	double readInfo;
}