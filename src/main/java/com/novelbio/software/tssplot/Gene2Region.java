package com.novelbio.software.tssplot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.software.tssplot.RegionBed.EnumTssPileUpType;

/** 给定指定基因，返回相应的RegionBed信息，用来绘制Tss图。
 * 需要和TssPlot配合使用
 */
public class Gene2Region {
	private static final Logger logger = LoggerFactory.getLogger(Gene2Region.class);
	/** 具体画哪个区域 */
	GeneStructure geneStructure;
	/** 待画图的gene list */
	Set<String> setGeneName = new LinkedHashSet<>();
	
	int[] startEndRegion;
	
	GffHashGene gffHashGene;
	
	List<RegionBed> lsRegionBed = new ArrayList<>();
	
	/** 把区域向前后进行扩展。<br>
	 * 给定tss位点为 chr1: 10000，方向为正向。<br>
	 * startExtend = -1000, endExtend=1000。则扩展后的结果为 9000-11000。<br>
	 * startExtend = 100, endExtend=1000。则扩展后的结果为 10100-11000。<br><br>
	 * 
	 * 给定tss位点为 chr1: 10000，方向为反向。<br>
	 * startExtend = -1000, endExtend=1000。则扩展后的结果为 11000-9000。<br>
	 * startExtend = 100, endExtend=1000。则扩展后的结果为 9900-9000。<br><br>
	 * 
	 * @param startExtend 向前扩展多少bp，小于0 表示向前扩展，大于0表示向后扩展
	 * @param endExtend 向后扩展多少bp，小于0表示向前扩展，大于0表示向后扩展
	 */
	public void setExtend(int startExtend, int endExtend) {
		startEndRegion = new int[]{startExtend, endExtend};
	}
	public void setGeneStructure(GeneStructure geneStructure) {
		this.geneStructure = geneStructure;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void setLsGeneName(List<String> lsGeneName) {
		this.setGeneName = new LinkedHashSet<>(lsGeneName);
	}
	public void addGeneName(String geneName) {
		this.setGeneName.add(geneName);
	}
	/** 设定为获取全体基因，本步骤会清空genelist */
	public void setToGenomeWideGene() {
		setGeneName.clear();
		for (GffGene gffGene : gffHashGene.getLsGffDetailGenes()) {
			setGeneName.add(gffGene.getName());
		}
	}
	
	/**
	 * 把计算得到的geneRegionBed输出为指定的文件，可用于{@link TssPlot}画图
	 * @param regionBedFile
	 * @param xAxis
	 * @param pileUpType
	 */
	public void writeToFile(String regionBedFile, double[] xAxis, EnumTssPileUpType pileUpType) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(regionBedFile, true);
		txtWrite.writefileln(TssPlot.XAXIS_LABEL);
		txtWrite.writefileln("# " + ArrayOperate.cmbString(xAxis, " "));
		txtWrite.writefileln(TssPlot.NORM_TYPE + " " + pileUpType.symbol);
		for (RegionBed regionBed : lsRegionBed) {
			txtWrite.writefileln(regionBed.toString());
		}
		txtWrite.close();
	}
	
	public void fillRegionBed() {
		lsRegionBed.clear();
		for (String geneName : setGeneName) {
			GffIso iso = gffHashGene.searchISOwithoutDB(geneName);
			if (iso == null) {
				logger.error("cannot find gene " + geneName);
				continue;
			}
			List<Align> lsAligns = getLsAligns(geneStructure, iso, startEndRegion);
			if (lsAligns.isEmpty()) continue;
			
			RegionBed regionBed = new RegionBed(geneName);
			regionBed.setLsAligns(lsAligns);
			lsRegionBed.add(regionBed);
		}
	}
	
	/**
	 * 获得指定区域的一系列align
	 * @param geneStructure 有tss, tes, cds, exon 等
	 * @param iso 具体提取某个转录本
	 * @param startEndRegion 起点和终点的扩展，具体见 {@link #startEndRegion}
	 * 注意本参数仅在单个位点如  {@link GeneStructure#ATG}, {@link GeneStructure#UAG},
	 * {@link GeneStructure#TSS}, {@link GeneStructure#TES}以及区段 {@link GeneStructure#ALLLENGTH} 时起作用
	 * 其他的区段不起作用
	 * @return
	 */
	@VisibleForTesting
	protected static List<Align> getLsAligns(GeneStructure geneStructure, GffIso iso, int[] startEndRegion) {
		List<Align> lsAligns = new ArrayList<>();
		if (geneStructure == GeneStructure.ALLLENGTH) {
			Align align = new Align(iso.getRefID(), iso.getStart(), iso.getEnd());
			align.setCis5to3(iso.isCis5to3());
			extendAlign(align, startEndRegion);
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.ATG) {
			Align align = new Align(iso.getRefID(), iso.getATGsite(), iso.getATGsite());
			align.setCis5to3(iso.isCis5to3());
			extendAlign(align, startEndRegion);
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.UAG) {
			Align align = new Align(iso.getRefID(), iso.getUAGsite(), iso.getUAGsite());
			align.setCis5to3(iso.isCis5to3());
			extendAlign(align, startEndRegion);
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.TSS) {
			Align align = new Align(iso.getRefID(), iso.getTSSsite(), iso.getTSSsite());
			align.setCis5to3(iso.isCis5to3());
			extendAlign(align, startEndRegion);
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.TES) {
			Align align = new Align(iso.getRefID(), iso.getTESsite(), iso.getTESsite());
			align.setCis5to3(iso.isCis5to3());
			extendAlign(align, startEndRegion);
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.CDS) {
			lsAligns = getLsAligns(iso.getIsoInfoCDS());
		} else if (geneStructure == GeneStructure.UTR5) {
			lsAligns = getLsAligns(iso.getUTR5seq());
		} else if (geneStructure == GeneStructure.UTR3) {
			lsAligns = getLsAligns(iso.getUTR3seq());
		} else if (geneStructure == GeneStructure.EXON) {
			lsAligns = getLsAligns(iso.getLsElement());
		} else if (geneStructure == GeneStructure.INTRON) {
			lsAligns = getLsAligns(iso.getLsIntron());
		}
		return lsAligns;
	}
	
	private static void extendAlign(Align align, int[] startEndRegion) {
		if (align.isCis5to3()) {
			align.setStartAbs(align.getStartAbs() + startEndRegion[0]);
			align.setEndAbs(align.getEndAbs() + startEndRegion[1]);
		} else {
			align.setStartAbs(align.getStartAbs() - startEndRegion[1]);
			align.setEndAbs(align.getEndAbs() - startEndRegion[0]);
		}
		if (align.getStartAbs() <= 0) {
			align.setStartAbs(1);
		}
		if (align.getEndAbs() <= 0) {
			align.setEndAbs(1);
		}
	}
	
	private static List<Align> getLsAligns(List<ExonInfo> lsExons) {
		List<Align> lsAligns = new ArrayList<>();
		for (ExonInfo exonInfo : lsExons) {
			Align align = new Align(exonInfo.getParent().getRefID(), exonInfo.getStartCis(), exonInfo.getEndCis());
			align.setCis5to3(exonInfo.isCis5to3());
			lsAligns.add(align);
		}
		return lsAligns;
	}
}
