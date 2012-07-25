package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

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
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * @author zong0jie
 *
 */
public class ExonJunction {
	
	public static void main(String[] args) {
		mouse();
	}
	static GffHashGene gffHashGene = null;
	
	public static void mouse() {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/mouseNovelbioRefGTF/novelbioModify_a15m1bf_All_highAll.GTF");
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
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/mouseNovelbioRefGTF/novelbioModify_a15m1bf_All_highAll.GTF");
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
				"/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/new/novelbioModify_a15m1bf_All_highAll20111220.GTF");
		ExonJunction exonJunction = new ExonJunction();
		String parentFile = "/media/winF/NBC/Project/Project_FY/chicken/scripture/";
		exonJunction.setIsoJunFile(parentFile + "tophatK0a15m1_1/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatK0a15m1_2/junctions.bed", "K0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT0a15m1_1/junctions.bed", "WT0");
		exonJunction.setIsoJunFile(parentFile + "tophatWT0a15m1_2/junctions.bed", "WT0");
		
		String outResult = "/media/winF/NBC/Project/Project_FY/chicken/chickenK0vsWT0outDifResult.xls";
		ArrayList<ChisqTest> lsResult = exonJunction.getDifIsoGene();
		TxtReadandWrite txtOut = new TxtReadandWrite(outResult, true);
		for (ChisqTest chisqTest : lsResult) {
			txtOut.writefileln(chisqTest.toString());
		}
		txtOut.close();
	}

	TophatJunction tophatJunction = new TophatJunction();
	HashSet<String> hashCond = new LinkedHashSet<String>();
	/**
	 * 设定junction文件以及所对应的时期
	 * 目前只能做两个时期的比较
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
	private ArrayList<ChisqTest> calGeneDifIso(GffDetailGene gffDetailGene) {
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		if (lsExonClusters == null || lsExonClusters.size() == 0) {
			return new ArrayList<ChisqTest>();
		}
		//外显子的具体坐标
		ArrayList<String> lsLocation = new ArrayList<String>();
		ArrayList<ArrayList<long[]>> lsExonInfo = calDifExonJun(gffDetailGene, lsLocation);
		return calLsExonInfo(gffDetailGene.getNameSingle(),lsExonInfo, lsLocation);
	}
	/**
	 * 给定转录本的分布情况，计算pvalue等指标
	 * 结果按照pvalue结果排序
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
	 * 输入有差异的exon的列表，计算每个时期的exon的差值
	 * @param lsExonClusters
	 */
	private ArrayList<ArrayList<long[]>> calDifExonJun(GffDetailGene gffDetailGene, ArrayList<String> lsLocation) {
		ArrayList<ExonCluster> lsExonClusters = gffDetailGene.getDifExonCluster();
		//按照时期保存exon的剪接情况
		ArrayList<ArrayList<long[]>> lsCondJun = new ArrayList<ArrayList<long[]>>();
		
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.isSameExon()) {
				continue;
			}
			ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
			int junc = 0;//跨过该exon的iso是否存在，0不存在，1存在
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
	 * 获得跳过该exonCluster组的readsNum
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
			//TODO 检查本步是否正确
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
	
	public Double getPvalue()
	{
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
		try {
			pvalue = TestUtils.chiSquareTestDataSetsComparison(testcond1, testcond2);
		} catch (Exception e) {
			pvalue = 1;
		}
		return pvalue;
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

