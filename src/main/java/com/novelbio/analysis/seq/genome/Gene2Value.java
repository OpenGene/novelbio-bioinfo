package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataStructure.MathComput;

/** 基因与结构提取，主要用于画tss图等 */
public class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
		
	GffGeneIsoInfo gffGeneIsoInfo;
	double value;
	
	/** tss或tes的扩展区域，一般哺乳动物为 -5000到5000 */
	int[] plotTssTesRegion = new int[]{-5000, 5000};
	
	int splitNum = 1000;
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	boolean pileupExonIntron = false;
	
	
	/** 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 */
	ArrayList<Integer> lsExonIntronNumGetOrExclude;
	/** 对于lsExonIntronNumGetOrExclude选择get还是exclude，true为get，false为exclude */
	boolean getOrExclude = true;
	
	/**
	 * @param plotTssTesRegion tss或tes的扩展区域，一般哺乳动物为 -5000到5000
	 */
	public void setPlotTssTesRegion(int[] plotTssTesRegion) {
		this.plotTssTesRegion = plotTssTesRegion;
	}
	/**
	 * 如果提取的是exon或者intron的区域，因为exon和intron每个基因都不是等长的，所以要设定划分的分数.
	 * 如果是tss和tes区域，也需要划分成指定的份数
	 * @param splitNumExonIntron 默认为500份
	 */
	public void setSplitNum(int splitNum) {
		this.splitNum = splitNum;
	}
	
	public void setGffGeneIsoInfo(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	public void setExonIntronPileUp(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	/** 设定需要提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron 
	 * 输入的是实际数量，譬如1表示第一个exon或intron
	 * @param lsExonIntronNumGetOrExclude 是提取想要的exon，intron还是不提去想要的exon，intron
	 * 1第一个， 2 第二个
	 * -1倒数第一个，-2倒数第二个
	 * @param getOrExclude true：提取， false 不提取
	 * */
	public void setGetNum(ArrayList<Integer> lsExonIntronNumGetOrExclude, boolean getOrExclude) {
		this.lsExonIntronNumGetOrExclude = lsExonIntronNumGetOrExclude;
		if (lsExonIntronNumGetOrExclude != null) {
			//排个序 1，2，3，4........-4，-3，-2，-1
			Collections.sort(lsExonIntronNumGetOrExclude, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					if (o1 * o2 > 0) { // 符号相同
						return o1.compareTo(o2);
					} else if (o1 * o2 < 0) { // 符号相反
						return -o1.compareTo(o2);
					} else {
						return 0;
					}
				}
			});
		}
		this.getOrExclude = getOrExclude;
	}

	/**
	 * 如果没有，譬如没有intron，那么就返回一个null
	 * 如果是tss，tes这种带0点的，splitNum会加上1
	 * @param mapReads
	 * @param geneStructure
	 * @return
	 */
	public MapInfo getMapInfo(MapReads mapReads, GeneStructure geneStructure) {
		boolean sucess = true;
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(), value, gffGeneIsoInfo.getName());
		mapInfo.setCis5to3(gffGeneIsoInfo.isCis5to3());
		int upstream = plotTssTesRegion[0]; int downstream = plotTssTesRegion[1];
		if (!gffGeneIsoInfo.isCis5to3()) {
			upstream = -upstream; downstream = -downstream;
		}
		
		if (geneStructure == GeneStructure.TSS) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + upstream, gffGeneIsoInfo.getTSSsite() + downstream);
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.TES) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + upstream, gffGeneIsoInfo.getTESsite() + downstream);
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.EXON) {
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo);
		} else if (geneStructure == GeneStructure.INTRON) {
			if (gffGeneIsoInfo.getLsIntron().size() == 0) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getLsIntron());
		} else if (geneStructure == GeneStructure.ALLLENGTH) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs());
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.CDS) {
			if (!gffGeneIsoInfo.ismRNA()) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoInfoCDS());
		} else if (geneStructure == GeneStructure.UTR3) {
			if (gffGeneIsoInfo.getLenUTR3() < 20) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getUTR3seq());
		} else if (geneStructure == GeneStructure.UTR5) {
			if (gffGeneIsoInfo.getLenUTR5() < 20) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getUTR5seq());
		} else {
			return null;
		}
		
		if (!sucess || mapInfo.getDouble() == null) {
			return null;
		}
		return mapInfo;
	}
	
	private boolean setMapInfo(MapInfo mapInfo, MapReads mapReads, String chrID, ArrayList<ExonInfo> lsExonInfos) {
		double[] result = new double[splitNum];
		List<ExonInfo> lsNew = getSelectLsExonInfo(lsExonInfos);
		if (lsNew.size() == 0) {
			return false;
		}
		
		if (pileupExonIntron) {
			ArrayList<double[]> lsResult = new ArrayList<double[]>();
			for (Alignment alignment : lsNew) {
				double[] info = mapReads.getRangeInfo(chrID, alignment.getStartAbs(), alignment.getEndAbs(), 0);
				if (info == null || info.length < 5) {
					continue;
				}
				info = MathComput.mySpline(info, splitNum, 0, 0, 0);
				lsResult.add(info);
			}
			for (double[] ds : lsResult) {
				for (int i = 0; i < ds.length; i++) {
					result[i] = result[i] + ds[i];
				}
			}
		} else {
			try {
				result = mapReads.getRangeInfo(chrID, lsNew);
			} catch (Exception e) {
				result = mapReads.getRangeInfo(chrID, lsNew);
			}
			if (result == null || result.length < 10) {
				return false;
			}
			result = MathComput.mySpline(result, splitNum, 0, 0, 0);
		}
		
		mapInfo.setDouble(result);
		return true;
	}
	/** 根据设定的lsExonIntronNumGetOrExclude和getOrExclude，返回选择的exoninfo
	 * <b>暴露出来仅供测试</b>
	 */
	public List<ExonInfo> getSelectLsExonInfo(ArrayList<ExonInfo> lsExonInfos) {
		HashSet<ExonInfo> setLocation = new HashSet<ExonInfo>();//去重复用的，防止lsSelect里面有重复的exoninfo
		List<ExonInfo> lsSelect = new ArrayList<ExonInfo>();
		if (lsExonIntronNumGetOrExclude == null || lsExonIntronNumGetOrExclude.size() == 0) {
			lsSelect = lsExonInfos;
			return lsSelect;
		}
		for (Integer i : lsExonIntronNumGetOrExclude) {
			//正向提取
			if (i > 0) {
				i = i - 1;
				if (i < lsExonInfos.size()) {
					ExonInfo exonInfo = lsExonInfos.get(i);
					if (setLocation.contains(exonInfo)) {
						continue;
					}
					setLocation.add(exonInfo);
					lsSelect.add(exonInfo);
				}
			}
			//反向提取
			else {
				if (Math.abs(i) <= lsExonInfos.size()) {
					ExonInfo exonInfo = lsExonInfos.get(lsExonInfos.size() - Math.abs(i));
					if (setLocation.contains(exonInfo)) {
						continue;
					}
					setLocation.add(exonInfo);
					lsSelect.add(exonInfo);
				}
			}
		}
		if (!getOrExclude) {
			ArrayList<ExonInfo> lsExclude = new ArrayList<ExonInfo>();
			for (ExonInfo exonInfo : lsExonInfos) {
				if (setLocation.contains(exonInfo)) {
					continue;
				} else {
					lsExclude.add(exonInfo);
				}
			}
			lsSelect = lsExclude;
		}
		return lsSelect;
	}
	
	/**
	 * 根据输入的坐标和权重，返回Gene2Value的list
	 * 会根据MapInfo.isMin2max()的标签来确定遇到重复项选择大的还是小的
	 * @param tssTesRange
	 * @param gffChrAbs
	 * @param colSiteInfo
	 * @param geneStructure
	 * @return
	 */
	public static ArrayList<Gene2Value> getLsGene2Vale(int[] tssTesRange, GffChrAbs gffChrAbs, Collection<MapInfo> colSiteInfo, GeneStructure geneStructure) {
		//存储最后的基因和权重
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : colSiteInfo) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene(tssTesRange, gffChrAbs, mapInfo, geneStructure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				if (hashGffDetailGenes.containsKey(gffDetailGene)) {
					if (MapInfo.isMin2max()) {
						if (mapInfo.getScore() < hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					} else {
						if (mapInfo.getScore() > hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
				} else {
					hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
				}
			}
		}
		ArrayList<Gene2Value> lsGene2Values = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : hashGffDetailGenes.keySet()) {
			Gene2Value gene2Value = new Gene2Value();
			gene2Value.setGffGeneIsoInfo(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGffDetailGenes.get(gffDetailGene));
			lsGene2Values.add(gene2Value);
		}
		return lsGene2Values;
	}
	
	/**
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
	 * @param tsstesRange 覆盖度，tss或tes的范围
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS等。如果是gene body区域，就返回整个基因
	 * @return
	 */
	private static Set<GffDetailGene> getPeakStructureGene(int[] tssTesRange, GffChrAbs gffChrAbs, SiteInfo siteInfo, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs());
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		gffCodGeneDU.cleanFilter();
		if (structure.equals(GeneStructure.TSS)) {
			gffCodGeneDU.setTss(tssTesRange);
			return gffCodGeneDU.getCoveredGffGene();
		}
		else if (structure.equals(GeneStructure.TES)) {
			gffCodGeneDU.setTes(tssTesRange);
			return gffCodGeneDU.getCoveredGffGene();
		}
		else {
			return gffCodGeneDU.getCoveredGffGene();
		}
	}
	/**
	 * 读取全基因组
	 * @param gffChrAbs
	 * @return
	 */
	public static ArrayList<Gene2Value> readGeneMapInfoAll(GffChrAbs gffChrAbs) {
		ArrayList<Gene2Value> lsGene2Value = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getGffDetailAll()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
			Gene2Value gene2Value = new Gene2Value();
			gene2Value.setGffGeneIsoInfo(gffGeneIsoInfo);
			lsGene2Value.add(gene2Value);
		}
		return lsGene2Value;
	}
}
