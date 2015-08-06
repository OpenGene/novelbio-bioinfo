package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.RegionInfo;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;

/** 基因与结构提取，主要用于画tss图等 */
public class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
		
	GffGeneIsoInfo gffGeneIsoInfo;
	/** 权重 */
	double score;
	
	/** tss或tes的扩展区域，一般哺乳动物为 -5000到5000 */
	int[] plotTssTesRegion = new int[]{-5000, 5000};
	
	/**
	 * 如果提取的是exon或者intron的区域，因为exon和intron每个基因都不是等长的，所以要设定划分的分数.
	 * 如果是tss和tes区域，也需要划分成指定的份数
	 * @param splitNumExonIntron 默认为500份
	 * <b>小于0表示不进行划修正，仅考虑MapReads产生那会儿的划分。exon是多少就是多少</b>
	 */
	int splitNum = 1001;
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	boolean pileupExonIntron = false;
	
	/** 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 */
	List<Integer> lsExonIntronNumGetOrExclude;
	/** 对于lsExonIntronNumGetOrExclude选择get还是exclude，true为get，false为exclude */
	boolean getOrExclude = true;
	
	public Gene2Value(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
	}
	/**
	 * @param plotTssTesRegion tss或tes的扩展区域
	 * 默认一般哺乳动物为 -5000到5000
	 */
	public void setPlotTssTesRegion(int[] plotTssTesRegion) {
		this.plotTssTesRegion = plotTssTesRegion;
	}
	/**
	 * 如果提取的是exon或者intron的区域，因为exon和intron每个基因都不是等长的，所以要设定划分的分数.
	 * 如果是tss和tes区域，也需要划分成指定的份数
	 * @param splitNumExonIntron 默认为1000份
	 * <b>小于0表示不进行划修正，仅考虑MapReads产生那会儿的划分。exon是多少就是多少</b>
	 */
	public void setSplitNum(int splitNum) {
		//因为是从0开始计数，所以要+1
		this.splitNum = splitNum + 1;
	}

	/** 设定该基因的权重，譬如表达值等属性 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体
	 * 默认首尾相连
	 */
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
	public void setGetNum(List<Integer> lsExonIntronNumGetOrExclude, boolean getOrExclude) {
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
	public RegionInfo getRegionInfo(MapReadsAbs mapReads, GeneStructure geneStructure) {
		boolean sucess = true;
		RegionInfo regionInfo = new RegionInfo(gffGeneIsoInfo.getRefIDlowcase(), score, gffGeneIsoInfo.getName());
		regionInfo.setCis5to3(gffGeneIsoInfo.isCis5to3());
		int upstream = plotTssTesRegion[0]; int downstream = plotTssTesRegion[1];
		if (!gffGeneIsoInfo.isCis5to3()) {
			upstream = -upstream; downstream = -downstream;
		}
		
		if (geneStructure == GeneStructure.TSS) {
			regionInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + upstream, gffGeneIsoInfo.getTSSsite() + downstream);
			mapReads.getRange(splitNum, regionInfo, 0);
		} else if (geneStructure == GeneStructure.TES) {
			regionInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + upstream, gffGeneIsoInfo.getTESsite() + downstream);
			mapReads.getRange(splitNum, regionInfo, 0);
		} else if (geneStructure == GeneStructure.EXON) {
			sucess = setMapInfo(regionInfo, mapReads, gffGeneIsoInfo.getLsElement());
		} else if (geneStructure == GeneStructure.INTRON) {
			if (gffGeneIsoInfo.getLsIntron().size() == 0) {
				return null;
			}
			sucess = setMapInfo(regionInfo, mapReads, gffGeneIsoInfo.getLsIntron());
		} else if (geneStructure == GeneStructure.ALLLENGTH) {
			regionInfo.setStartEndLoc(gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs());
			mapReads.getRange(splitNum, regionInfo, 0);
		} else if (geneStructure == GeneStructure.CDS) {
			if (!gffGeneIsoInfo.ismRNA()) {
				return null;
			}
			sucess = setMapInfo(regionInfo, mapReads, gffGeneIsoInfo.getIsoInfoCDS());
		} else if (geneStructure == GeneStructure.UTR3) {
			if (gffGeneIsoInfo.getLenUTR3() < 20) {
				return null;
			}
			sucess = setMapInfo(regionInfo, mapReads, gffGeneIsoInfo.getUTR3seq());
		} else if (geneStructure == GeneStructure.UTR5) {
			if (gffGeneIsoInfo.getLenUTR5() < 20) {
				return null;
			}
			sucess = setMapInfo(regionInfo, mapReads, gffGeneIsoInfo.getUTR5seq());
		} else {
			return null;
		}
		
		if (!sucess || regionInfo.getDouble() == null) {
			return null;
		}
		return regionInfo;
	}
	
	/**
	 * 根据指定的lsExonInfos信息，设定mapInfo的value<br>
	 * 会根据splitNum的数量对结果进行标准化
	 * @param regionInfo
	 * @param mapReads
	 * @param lsExonInfos
	 * @return
	 */
	private boolean setMapInfo(RegionInfo regionInfo, MapReadsAbs mapReads, List<ExonInfo> lsExonInfos) {
		double[] result = null;
		List<ExonInfo> lsNew = getSelectLsExonInfo(lsExonInfos);
		if (lsNew.isEmpty()) {
			return false;
		}
		List<Integer> lsCoverage = new ArrayList<Integer>();
		if (pileupExonIntron) {
			List<double[]> lsInfo = mapReads.getRangeInfoLs(regionInfo.getRefID(), lsNew);
			List<double[]> lsResult = new ArrayList<double[]>();
			for (double[] info : lsInfo) {
				if (info == null || info.length < 5) {
					continue;
				}
				if (splitNum > 0) {
					info = MathComput.mySpline(info, splitNum, 0, 0, 0);
				}
				lsResult.add(info);
			}
			result = ArrayOperate.getSumList(lsResult);
			if (result == null || result.length < 1) {
				return false;
			}
			lsCoverage = ArrayOperate.getLsCoverage(lsResult);
		} else {
			result = mapReads.getRangeInfo(regionInfo.getRefID(), lsNew);
			if (result == null || result.length < 10) {
				return false;
			}
			if (splitNum > 0) {
				result = MathComput.mySpline(result, splitNum, 0, 0, 0);
			}
			lsCoverage = getLsCoverage(result.length, 1);
		}
		result = getNormalizedValue(result, lsCoverage);
		regionInfo.setDouble(result);
		return true;
	}
	
	/** 根据设定的lsExonIntronNumGetOrExclude信息，返回选择的exoninfo
	 * <b>暴露出来仅供测试</b>
	 * @param lsExonInfos 输入的exon信息
	 */
	protected List<ExonInfo> getSelectLsExonInfo(List<ExonInfo> lsExonInfos) {
		if (lsExonInfos.isEmpty()) {
			return new ArrayList<>();
        }
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
	 * @param colRegionInfo
	 * @param geneStructure
	 * @return
	 */
	public static List<Gene2Value> getLsGene2Vale(int[] tssTesRange, GffChrAbs gffChrAbs, 
			Collection<RegionInfo> colRegionInfo, GeneStructure geneStructure, int[] exonNumRegion) {
		//存储最后的基因和权重
		ArrayListMultimap<GffDetailGene, Double> mapGene2LsValue = ArrayListMultimap.create();		
		for (RegionInfo mapInfo : colRegionInfo) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene(tssTesRange, gffChrAbs, mapInfo, geneStructure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				mapGene2LsValue.put(gffDetailGene, mapInfo.getScore());
			}
		}
		return combineMapGene2LsValue(mapGene2LsValue, exonNumRegion);
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
	public static List<Gene2Value> getLsGene2Vale(GffChrAbs gffChrAbs, List<String[]> lsGeneValue, int[] exonNumRegion) {		//有权重的就使用这个hash
		ArrayListMultimap<GffDetailGene, Double> mapGene2LsValue = ArrayListMultimap.create();		
		for (String[] strings : lsGeneValue) {
			GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(strings[0]);
			if (gffDetailGene == null) {
				continue;
			}
			//have gene score, using the score as value, when the gene is same, add the score bigger one
			if (strings.length > 1) {
				double score = 0;
				try {
					score = Double.parseDouble(strings[1]);
				} catch (Exception e) {
				}
				mapGene2LsValue.put(gffDetailGene, score);
			}
			//dosen't have score
			else {
				mapGene2LsValue.put(gffDetailGene, 0.0);
			}
		}
		
		return combineMapGene2LsValue(mapGene2LsValue, exonNumRegion);
	}
	
	private static List<Gene2Value> combineMapGene2LsValue(ArrayListMultimap<GffDetailGene, Double> mapGene2LsValue, int[] exonNumRegion) {
		List<Gene2Value> lsGene2Values = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : mapGene2LsValue.keySet()) {
			GffGeneIsoInfo iso = gffDetailGene.getLongestSplitMrna();
			if (!isMatchExonNum(iso, exonNumRegion)) {
	            	continue;
            }
			Gene2Value gene2Value = new Gene2Value(iso);
			List<Double> lsValues = mapGene2LsValue.get(gffDetailGene);
			gene2Value.setScore(MathComput.mean(lsValues));
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
	private static Set<GffDetailGene> getPeakStructureGene(int[] tssTesRange, GffChrAbs gffChrAbs,
			RegionInfo siteInfo, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(siteInfo);
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		gffCodGeneDU.cleanFilter();
		if (structure.equals(GeneStructure.TSS)) {
			gffCodGeneDU.setTss(tssTesRange);
			return gffCodGeneDU.getCoveredOverlapGffGene();
		}
		else if (structure.equals(GeneStructure.TES)) {
			gffCodGeneDU.setTes(tssTesRange);
			return gffCodGeneDU.getCoveredOverlapGffGene();
		}
		else {
			return gffCodGeneDU.getCoveredOverlapGffGene();
		}
	}
	/**
	 * 读取全基因组
	 * @param gffChrAbs
	 * @return
	 */
	public static ArrayList<Gene2Value> readGeneMapInfoAll(GffChrAbs gffChrAbs, int[] exonNumRegion) {
		ArrayList<Gene2Value> lsGene2Value = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getGffDetailAll()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
			if (!isMatchExonNum(gffGeneIsoInfo, exonNumRegion)) {
	            	continue;
            }
			Gene2Value gene2Value = new Gene2Value(gffGeneIsoInfo);
			lsGene2Value.add(gene2Value);
		}
		return lsGene2Value;
	}
	
	private static List<Integer> getLsCoverage(int length, int coverageNum) {
		List<Integer> lsResult = new ArrayList<Integer>();
		for (int i = 0; i < length; i++) {
			lsResult.add(coverageNum);
		}
		return lsResult;
	}
	
	private static double[] getNormalizedValue(double[] value, List<Integer> lsCoverage) {
		double[] result = new double[value.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = value[i]/lsCoverage.get(i);
		}
		return result;
	}
	
	/** 给定iso和所需要的exonNum的区间，返回该iso是否满足条件
	 * @param iso
	 * @param exonNumRegion int[2]<br>
	 *  0：至少多少exon 小于0表示最小1个exon即可<br>
	 *  1：至多多少exon 小于0表示最多可以无限个exon<br>
	 *  <br>
	 *  int[]{2,3}表示本iso的exon数量必须在2-3之间<br>
	 *  int[]{-1,3}表示本iso的exon数量必须小于等于3个<br>
	 *  int[]{2,-1}表示本iso的exon数量必须大于等于2个<br>
	 * @return
	 */
	private static boolean isMatchExonNum(GffGeneIsoInfo iso, int[] exonNumRegion) {
		if (iso == null || iso.getLsElement().isEmpty()) {
			return false;
		}
		return isMatchExonNum(iso.getLsElement().size(), exonNumRegion);
	}
	
	protected static boolean isMatchExonNum(int exonNum, int[] exonNumRegion) {
		if (exonNumRegion == null) return true;
		
		if (exonNumRegion[0] > 0 && exonNum < exonNumRegion[0] ) {
			return false;
        }
		if (exonNumRegion[1] > 0 && exonNum > exonNumRegion[1]) {
			return false;
        }
		return true;
	}
}
