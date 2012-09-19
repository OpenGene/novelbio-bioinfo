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
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * @author zong0jie
 */
public class ExonJunction {
	private static Logger logger = Logger.getLogger(ExonJunction.class);

	GffHashGene gffHashGene = null;
	/** 全体差异基因的外显子
	 * ls--
	 * ls：gene
	 * ExonSplicingTest：difexon
	 *  */
	ArrayList<ArrayList<ExonSplicingTest>> lsSplicingTests;
	
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	HashMapLsValue<String, MapReads> mapCondition2MapReads = new HashMapLsValue<String, MapReads>();
	String condition1, condition2;
	

	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		lsSplicingTests = new ArrayList<ArrayList<ExonSplicingTest>>();
		fillLsAll_Dif_Iso_Exon();
	}
	private void fillLsAll_Dif_Iso_Exon() {
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
	
	public void setCompareGroups(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	/**
	 * 设定junction文件以及所对应的时期
	 * 目前只能做两个时期的比较
	 * @param condition
	 * @param junctionFile
	 */
	public void setIsoJunFile(String condition,String junctionFile) {
		tophatJunction.setJunFile(condition,junctionFile);
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
	 * 获得检验完毕的test
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
	 * 计算每个时期的exon的差值
	 * @param lsExonClusters
	 * @return ls ExonSplicingTest -- ls 每个时期 -- 所涉及到的exon的检验结果，按照pvalue从小到大排序
	 */
	private void doTest(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			exonSplicingTest.setConditionsetAndJunction(setCondition, tophatJunction);
			exonSplicingTest.setCompareCondition(condition1, condition2);
		}
		//按照pvalue从小到大排序
		sortLsExonTest_Use_Pvalue(lsExonSplicingTest);
	}
	/** 写入文本 */
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		ArrayList<ExonSplicingTest> lsSplicingTests = getTestResult_OneExonPerIso();
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
	
	private void sortLsExonTest_Use_Pvalue(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		//按照pvalue从小到大排序
		Collections.sort(lsExonSplicingTest, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getAndCalculatePvalue().compareTo(o2.getAndCalculatePvalue());
			}
		});
	}
}
