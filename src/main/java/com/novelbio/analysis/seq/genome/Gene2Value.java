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

/** ������ṹ��ȡ����Ҫ���ڻ�tssͼ�� */
public class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
		
	GffGeneIsoInfo gffGeneIsoInfo;
	double value;
	
	/** tss��tes����չ����һ�㲸�鶯��Ϊ -5000��5000 */
	int[] plotTssTesRegion = new int[]{-5000, 5000};
	
	int splitNum = 1000;
	
	/** ��ȡ��exon��intron���ǵ���һ���Ϊһ���أ�����ͷβ������Ϊһ�� */
	boolean pileupExonIntron = false;
	
	
	/** �趨��Ҫ��ȡ������ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron
	 * null �Ͳ�����
	 * Ϊʵ������
	 * -1Ϊ������һ��
	 * -2Ϊ�����ڶ���
	 */
	ArrayList<Integer> lsExonIntronNumGetOrExclude;
	/** ����lsExonIntronNumGetOrExcludeѡ��get����exclude��trueΪget��falseΪexclude */
	boolean getOrExclude = true;
	
	/**
	 * @param plotTssTesRegion tss��tes����չ����һ�㲸�鶯��Ϊ -5000��5000
	 */
	public void setPlotTssTesRegion(int[] plotTssTesRegion) {
		this.plotTssTesRegion = plotTssTesRegion;
	}
	/**
	 * �����ȡ����exon����intron��������Ϊexon��intronÿ�����򶼲��ǵȳ��ģ�����Ҫ�趨���ֵķ���.
	 * �����tss��tes����Ҳ��Ҫ���ֳ�ָ���ķ���
	 * @param splitNumExonIntron Ĭ��Ϊ500��
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
	
	/** ��ȡ��exon��intron���ǵ���һ���Ϊһ���أ�����ͷβ������Ϊһ�� */
	public void setExonIntronPileUp(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	/** �趨��Ҫ��ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron 
	 * �������ʵ��������Ʃ��1��ʾ��һ��exon��intron
	 * @param lsExonIntronNumGetOrExclude ����ȡ��Ҫ��exon��intron���ǲ���ȥ��Ҫ��exon��intron
	 * 1��һ���� 2 �ڶ���
	 * -1������һ����-2�����ڶ���
	 * @param getOrExclude true����ȡ�� false ����ȡ
	 * */
	public void setGetNum(ArrayList<Integer> lsExonIntronNumGetOrExclude, boolean getOrExclude) {
		this.lsExonIntronNumGetOrExclude = lsExonIntronNumGetOrExclude;
		if (lsExonIntronNumGetOrExclude != null) {
			//�Ÿ��� 1��2��3��4........-4��-3��-2��-1
			Collections.sort(lsExonIntronNumGetOrExclude, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					if (o1 * o2 > 0) { // ������ͬ
						return o1.compareTo(o2);
					} else if (o1 * o2 < 0) { // �����෴
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
	 * ���û�У�Ʃ��û��intron����ô�ͷ���һ��null
	 * �����tss��tes���ִ�0��ģ�splitNum�����1
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
				if (info.length < 5) {
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
	/** �����趨��lsExonIntronNumGetOrExclude��getOrExclude������ѡ���exoninfo
	 * <b>��¶������������</b>
	 */
	public List<ExonInfo> getSelectLsExonInfo(ArrayList<ExonInfo> lsExonInfos) {
		HashSet<ExonInfo> setLocation = new HashSet<ExonInfo>();//ȥ�ظ��õģ���ֹlsSelect�������ظ���exoninfo
		List<ExonInfo> lsSelect = new ArrayList<ExonInfo>();
		if (lsExonIntronNumGetOrExclude == null || lsExonIntronNumGetOrExclude.size() == 0) {
			lsSelect = lsExonInfos;
			return lsSelect;
		}
		for (Integer i : lsExonIntronNumGetOrExclude) {
			//������ȡ
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
			//������ȡ
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
	 * ��������������Ȩ�أ�����Gene2Value��list
	 * �����MapInfo.isMin2max()�ı�ǩ��ȷ�������ظ���ѡ���Ļ���С��
	 * @param tssTesRange
	 * @param gffChrAbs
	 * @param colSiteInfo
	 * @param geneStructure
	 * @return
	 */
	public static ArrayList<Gene2Value> getLsGene2Vale(int[] tssTesRange, GffChrAbs gffChrAbs, Collection<MapInfo> colSiteInfo, GeneStructure geneStructure) {
		//�洢���Ļ����Ȩ��
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
	 * �����������򣬷��ظ�peak�����ǵ�GffDetailGene
	 * @param tsstesRange ���Ƕȣ�tss��tes�ķ�Χ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS�ȡ������gene body���򣬾ͷ�����������
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
	 * ��ȡȫ������
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