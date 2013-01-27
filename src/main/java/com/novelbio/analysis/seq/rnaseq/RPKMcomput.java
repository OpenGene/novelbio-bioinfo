package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.functors.IfClosure;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 根据tophat或者其他mapping至genome的bam结果，计算基因的rpkm
 * 如果是链特异性测序，那就需要考虑方向，可以设定{@link #setConsiderStrand}
 * @author zong0jie
 */
public class RPKMcomput implements AlignmentRecorder {
	
	/** 默认不考虑方向 */
	boolean considerStrand = false;
	
	GffHashGene gffHashGene;
	
	/**
	 * key：基因名<br>
	 * value：<br>
	 * key:condiiton<br>
	 *         value: 0 counts<br>
	 */
	Map<String, Map<String, double[]>> mapGeneName2Cond2ReadsCounts = new HashMap<String, Map<String,double[]>>();
	/**
	 * key: geneName
	 * value: geneLength
	 */
	Map<String, Integer> mapGeneName2Length = new TreeMap<String, Integer>();

	/** 样本时期 和 样本reads num 用来算rpkm */
	Map<String, Double> mapCond2CountsNum = new LinkedHashMap<String, Double>();
	/** 设定当前condition */
	String currentCondition;
	/** 计数器，获得当前样本的总体 reads数， 用来算rpkm的 */
	double currentReadsNum = 0;
	
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		initial();
	}
	public void setGffChrAbs(GffHashGeneAbs gffHashGeneAbs) {
		this.gffHashGene = new GffHashGene(gffHashGeneAbs);
		initial();
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffHashGene = gffChrAbs.getGffHashGene();
		initial();
	}

	private void initial() {
		ArrayList<GffDetailGene> lsGffDetailGene = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			mapGeneName2Length.put(gffDetailGene.getNameSingle(), gffDetailGene.getLongestSplitMrna().getLenExon(0));
		}
	}
	
	/** 是否考虑reads方向，只有链特异性测序才使用 */
	public void setConsiderStrand(boolean considerStrand) {
		this.considerStrand = considerStrand;
	}
	
	/**
	 * 设定样本名，同时清空currentReadsNum这个计数器
	 * 区分大小写
	 * @param currentCondition
	 */
	public void setCurrentCondition(String currentCondition) {
		this.currentCondition = currentCondition;
		this.currentReadsNum = 0;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		boolean cis5to3 = alignRecord.isCis5to3();
		List<Align> lsAligns = alignRecord.getAlignmentBlocks();
		for (Align align : lsAligns) {
			currentReadsNum += 1/alignRecord.getMappingNum();
			int midLoc = (align.getStartAbs() + align.getEndAbs())/2;
			GffCodGene gffCodGene = gffHashGene.searchLocation(align.getRefID(), midLoc);
			String geneName = getName(cis5to3, gffCodGene);
			//找到后就可以跳出了，因为一条reads只要找到1个位置即可
			if (geneName != null) {
				addInMapGeneName2Cond2ReadsCounts(geneName, alignRecord.getMappingNum());
				break;
			}
		}
	}
	
	/**
	 * 返回落到的基因名
	 * 没有落在基因内部就返回null
	 * @param readsCis5to3
	 * @param gffCodGene
	 * @return
	 */
	private String getName(boolean readsCis5to3, GffCodGene gffCodGene) {
		//判定是否落在exon里面，落在intron里面不算
		if (!gffCodGene.isInsideLoc()) {
			return null;
		}
		
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		boolean isStrandSame2GffDetail = (readsCis5to3 == gffDetailGene.isCis5to3());
		String resultName = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getCodLoc(gffCodGene.getCoord()) != GffGeneIsoInfo.COD_LOC_EXON) {
				continue;
			}
			
			if (!considerStrand) {
				resultName = gffDetailGene.getNameSingle();
				break;
			}
			//考虑方向
			if (readsCis5to3 != gffGeneIsoInfo.isCis5to3()) {
				continue;
			}
			//TODO 考虑方向的时候可能不准，最好能挑选出那种最长的iso
			//否则如果两个iso有overlap，那么在比对到非overlap区域的时候就会出现偏差
			//譬如 1------------2
			//                   3----------------4
			//第一个reads比对到了2-4之间
			//第二个reads比对到了3-2之间
			//这时候就会出现偏差
			if (isStrandSame2GffDetail) {
				resultName = gffDetailGene.getNameSingle();
				break;
			} else {
				resultName = gffGeneIsoInfo.getName();
				mapGeneName2Length.put(resultName, gffGeneIsoInfo.getLenExon(0));
				break;
			}
		}
		return resultName;
	}
	
	/**
	 * @param geneName 基因名
	 * @param mapNum 如果是非unique mapping，那么mapping到几个位置上
	 */
	private void addInMapGeneName2Cond2ReadsCounts(String geneName, int mapNum) {
		Map<String, double[]>mapCond2ReadsCounts = null;
		if (mapGeneName2Cond2ReadsCounts.containsKey(geneName)) {
			mapCond2ReadsCounts = mapGeneName2Cond2ReadsCounts.get(geneName);
		} else {
			mapCond2ReadsCounts = new LinkedHashMap<String, double[]>();
			mapGeneName2Cond2ReadsCounts.put(geneName, mapCond2ReadsCounts);
		}
		
		double[] countsNum = null;
		if (mapCond2ReadsCounts.containsKey(currentCondition)) {
			countsNum = mapCond2ReadsCounts.get(currentCondition);
		} else {
			countsNum = new double[1];
			mapCond2ReadsCounts.put(currentCondition, countsNum);
		}
		countsNum[0] += 1/mapNum;
	}
	
	@Override
	public void summary() {
		mapCond2CountsNum.put(currentCondition, currentReadsNum);
	}
	
	/** 返回计算得到的rpm值 */
	public ArrayList<String[]> getLsRPMs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = ArrayOperate.getArrayListKey(mapCond2CountsNum);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] readsCounts = mapCond2Counts.get(conditions);
					if (readsCounts == null) {
						lsTmpResult.add(0 + "");
					} else {
						lsTmpResult.add(readsCounts[0]*1000000/mapCond2CountsNum.get(conditions) + "");
					}
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public ArrayList<String[]> getLsRPKMs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = ArrayOperate.getArrayListKey(mapCond2CountsNum);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] readsCounts = mapCond2Counts.get(conditions);
					if (readsCounts == null) {
						lsTmpResult.add(0 + "");
					} else {
						lsTmpResult.add(readsCounts[0]*1000000*1000/mapCond2CountsNum.get(conditions)/mapGeneName2Length.get(geneName) + "");
					}
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
}
