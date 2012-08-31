package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.commons.collections.functors.IfClosure;
import org.apache.commons.math.stat.inference.TestUtils;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.NovelBioConst;

/**
 * �õ�ÿ��gene��Junction�󣬿�ʼ������ɱ���ӵĲ���
 * @author zong0jie
 *
 */
public class ExonJunction {
	
	public static void main(String[] args) {
		chicken();
	}
	static GffHashGene gffHashGene = null;

	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> hashCond = new LinkedHashSet<String>();
	
	public static void mouse() {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf");
		ExonJunction exonJunction = new ExonJunction();
		String parentFile = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/";
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_1/junctions.bed", "K0"); 
		exonJunction.setIsoJunFile(parentFile + "MEFK02da14m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "MEFWT2da14m1_2/junctions.bed", "WT0");
		
		String outResult = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/MEF_K2vsWT2outDifResult_test.xls";
		ArrayList<ChisqTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ChisqTest chisqTest : lsResult) {
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
		ArrayList<ChisqTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ChisqTest chisqTest : lsResult) {
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
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5outDifResult.xls";
		ArrayList<ChisqTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ChisqTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}

	/**
	 * �趨junction�ļ��Լ�����Ӧ��ʱ��
	 * Ŀǰֻ��������ʱ�ڵıȽ�
	 * @param junctionFile
	 * @param condition
	 */
	public void setIsoJunFile(String junctionFile, String condition) {
		tophatJunction.setJunFile(junctionFile, condition);
		hashCond.add(condition);
	}
	
	public ArrayList<ChisqTest> getDifIsoGene() {
		ArrayList<ChisqTest> lsChisqTests = new ArrayList<ChisqTest>();
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			
			if (gffDetailGene.getLsCodSplit().size() <= 1)
				continue;
			if (isOnlyOneIso(gffDetailGene))
				continue;
			
			ArrayList<ChisqTest> lsChisqTestTmp = calGeneDifIso(gffDetailGene);
			if (lsChisqTestTmp.size() == 0) {
				continue;
			}
			lsChisqTests.add(lsChisqTestTmp.get(0));
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

	private ArrayList<ChisqTest> calGeneDifIso(GffDetailGene gffDetailGene) {
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		if (lsExonClusters == null || lsExonClusters.size() == 0) {
			return new ArrayList<ChisqTest>();
		}
		//�����ӵľ�������
		ArrayList<String> lsLocation = new ArrayList<String>();
		ArrayList<ArrayList<long[]>> lsExonInfo = calDifExonJun(gffDetailGene, lsLocation);
		return calLsExonInfo(gffDetailGene.getNameSingle(),lsExonInfo, lsLocation);
	}
	/**
	 * ����ת¼���ķֲ����������pvalue��ָ��
	 * �������pvalue�������
	 * @param lsExonInfo
	 */
	private ArrayList<ChisqTest> calLsExonInfo(String geneID, ArrayList<ArrayList<long[]>> lsExonInfo, ArrayList<String> lsLocation) {
		ArrayList<ChisqTest> lsResult = new ArrayList<ChisqTest>();
		for (int i = 0; i < lsExonInfo.size(); i++) {
			ArrayList<long[]> arrayList = lsExonInfo.get(i);
			ChisqTest chisqTest = new ChisqTest(geneID, arrayList.get(0), arrayList.get(1), lsLocation.get(i));
			lsResult.add(chisqTest);
		}
		Collections.sort(lsResult);
		return lsResult;
	}
	/**
	 * �����в����exon���б�����ÿ��ʱ�ڵ�exon�Ĳ�ֵ
	 * @param lsExonClusters
	 * @param lsLocation ����һ���յ�list��������������exon������
	 * @return ls ExonCluster -- ls ÿ��ʱ�� -- ���漰����exon��reads��
	 * ����������ÿ��ʱ�ڵ�exon�ķֲ�����
	 */
	private ArrayList<ArrayList<long[]>> calDifExonJun(GffDetailGene gffDetailGene, ArrayList<String> lsLocation) {
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		//����ʱ�ڱ���exon�ļ������
		ArrayList<ArrayList<long[]>> lsCondJun = new ArrayList<ArrayList<long[]>>();
		
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.isSameExon()) {
				continue;
			}
			ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
			int junc = 0;//�����exon��iso�Ƿ���ڣ�0�����ڣ�1����
			if (exonCluster.getHashIsoName2ExonNum().size() > 0) {
				junc = 1;
			}
			ArrayList<long[]> lsExonTmp = new ArrayList<long[]>();
			for (String condition : hashCond) {
				long[] counts = new long[lsExon.size() + junc];
				for (int i = 0; i < lsExon.size(); i++) {
					ExonInfo exon = lsExon.get(i);
					counts[i] = tophatJunction.getJunctionSite(gffDetailGene.getParentName(), exon.getStartCis(), condition) + tophatJunction.getJunctionSite(gffDetailGene.getParentName(), exon.getEndCis(), 	condition);
				}
				if (junc == 1) {
					counts[counts.length - 1] = getJunReadsNum(gffDetailGene, exonCluster, condition);
				}
				lsExonTmp.add(counts);
			}
			lsCondJun.add(lsExonTmp);
			lsLocation.add(exonCluster.getLocInfo());
		}
		return lsCondJun;
	}
	/**
	 * ���������exonCluster���readsNum
	 * @param gffDetailGene
	 * @param exonCluster
	 * @param condition
	 * @return
	 */
	private long getJunReadsNum(GffDetailGene gffDetailGene, ExonCluster exonCluster, String condition) {
		long result = 0;
		HashMap<String, Integer> hashTmp = exonCluster.getHashIsoName2ExonNum();
		for (Entry<String, Integer> entry : hashTmp.entrySet()) {
			String isoName = entry.getKey();
			int exonNum = entry.getValue();
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getIsolist(isoName);
			if (exonNum >= gffGeneIsoInfo.size()-1) {
				continue;
			}
			//TODO ��鱾���Ƿ���ȷ
			result = result + tophatJunction.getJunctionSite(gffDetailGene.getParentName(), gffGeneIsoInfo.get(exonNum).getEndCis(), gffGeneIsoInfo.get(exonNum+1).getStartCis(), condition);
		}
		return result;
	}
}

class ChisqTest implements Comparable<ChisqTest> {
	String location = "";
	String geneID = "";
	long[] cond1;
	long[] cond2;
	double pvalue= 100;
	public ChisqTest(String geneID, long[] lsLong1, long[] lsLong2, String location) {
		cond1 = lsLong1;
		cond2 = lsLong2;
		this.geneID = geneID;
		this.location = location;
		getPvalue();
	}
	
	public Double getPvalue() {
		if (pvalue < 1) {
			return pvalue;
		}
		long mean1 = MathComput.mean(cond1);
		long mean2 = MathComput.mean(cond2);
		long[] testcond1 = new long[cond1.length];
		for (int i = 0; i < cond1.length; i++) {
			testcond1[i] = cond1[i] + mean1;
		}
		long[] testcond2 = new long[cond2.length];
		for (int i = 0; i < cond2.length; i++) {
			testcond2[i] = cond2[i] + mean2;
		}
		modifyInfo(testcond1);
		modifyInfo(testcond2);
		try {
			pvalue = TestUtils.chiSquareTestDataSetsComparison(testcond1, testcond2);
		} catch (Exception e) {
			pvalue = 1;
		}
		return pvalue;
	}
	
	private void modifyInfo(long[] condition) {
		int value = 200;//���ڸ�ֵ�Ϳ�ʼ����
		long meanValue = MathComput.mean(condition);
		if (meanValue < value) {
			return;
		}
		else {
			for (int i = 0; i < condition.length; i++) {
				condition[i] = (long) ((double)condition[i]/meanValue * value);
			}
		}
	}
	
	@Override
	public int compareTo(ChisqTest o) {
		return getPvalue().compareTo(o.getPvalue());
	}
	
	public String toString() {
		String result = geneID + "\t" + location +"\t"+ cond1[0]+ "";
		for (int i = 1; i < cond1.length; i++) {
			result = result + "::" + cond1[i];
		}
		result = result + "\t" + cond2[0];
		for (int i = 1; i < cond2.length; i++) {
			result = result + "::" + cond2[i];
		}
		result = result + "\t" + getPvalue();
		return result;
	}
}

