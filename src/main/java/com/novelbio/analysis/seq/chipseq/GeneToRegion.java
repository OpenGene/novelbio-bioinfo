package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUpType;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/** 给定指定基因，返回相应的RegionBed信息，用来绘制Tss图。
 * 需要和TssPlot配合使用
 */
public class GeneToRegion {
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
		for (GffDetailGene gffGene : gffHashGene.getLsGffDetailGenes()) {
			setGeneName.add(gffGene.getNameSingle());
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
			GffGeneIsoInfo iso = gffHashGene.searchISO(geneName);
			List<Align> lsAligns = getLsAligns(geneStructure, iso);
			if (lsAligns.isEmpty()) continue;
			
			RegionBed regionBed = new RegionBed(geneName);
			regionBed.setLsAligns(lsAligns);
			lsRegionBed.add(regionBed);
		}
	}
	
	@VisibleForTesting
	protected static List<Align> getLsAligns(GeneStructure geneStructure, GffGeneIsoInfo iso) {
		List<Align> lsAligns = new ArrayList<>();
		if (geneStructure == GeneStructure.ALLLENGTH) {
			Align align = new Align(iso.getRefID(), iso.getStart(), iso.getEnd());
			align.setCis5to3(iso.isCis5to3());
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.ATG) {
			Align align = new Align(iso.getRefID(), iso.getATGsite(), iso.getATGsite());
			align.setCis5to3(iso.isCis5to3());
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.UAG) {
			Align align = new Align(iso.getRefID(), iso.getUAGsite(), iso.getUAGsite());
			align.setCis5to3(iso.isCis5to3());
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.TSS) {
			Align align = new Align(iso.getRefID(), iso.getTSSsite(), iso.getTSSsite());
			align.setCis5to3(iso.isCis5to3());
			lsAligns.add(align);
		} else if (geneStructure == GeneStructure.TES) {
			Align align = new Align(iso.getRefID(), iso.getTESsite(), iso.getTESsite());
			align.setCis5to3(iso.isCis5to3());
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
