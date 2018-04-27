package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.database.domain.modgeneid.GeneType;


public class GffGeneTypeStatistics {
//	/** 全体基因数量 */
//	int gene;
//	/** 编码mRNA的基因数量 */
//	int geneMrna;
//	
//	int mRNA;
//	int miRNA;
//	/** snoRNA的数量 */
//	int snoRNA;
//	int snRNA;
//	
//	/** miRNA前体的数量 */
//	int precursorMirna;
//
//	int tRNA;
//	int ncRNA;
//	int miscRNA;
//
//
//	int antisenseRNA;
//	int rRNA;
	GeneExpTable geneExpTable;
	/** 时期，譬如hg19_p9 这种 */
	String condition;
	GffHashGeneInf gffHash;
	
	/**
	 * @param gffHash
	 * @param geneExpTable 如果为null，就new一个
	 * @param condition 时期，譬如hg19_p9 这种
	 */
	public GffGeneTypeStatistics(String condition, GffHashGeneInf gffHash, GeneExpTable geneExpTable) {
		this.condition = condition;
		this.gffHash = gffHash;
		this.geneExpTable = geneExpTable;
	}
	
	/** 运行完后就会将 gffHash 设置为null 以释放内存 */
	public void statistics() {
		clear();
		geneExpTable.setCurrentCondition(condition);
		Collection<String> setGene = new HashSet<>();
		Collection<String> setGeneMrna = new HashSet<>();
		for (GffDetailGene gffDetailGene : gffHash.getLsGffDetailGenes()) {
			setGene.add(gffDetailGene.getNameSingle());
			if (gffDetailGene.isMRNAgeneType()) {
				setGeneMrna.add(gffDetailGene.getNameSingle());
			}
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				try {
					geneExpTable.addGeneExp(iso.getGeneType().toString(), 1);
				} catch (Exception e) {
				}
			}
		}
		geneExpTable.addGeneExp("gene", setGene.size());
		geneExpTable.addGeneExp("geneMrna", setGeneMrna.size());
		
		gffHash = null;
	}
	
	public GeneExpTable getStatisticTable() {
		return geneExpTable;
	}
	
	private void clear() {
		if (geneExpTable == null) {
			geneExpTable = new GeneExpTable("Gene Annotation Statistics");
			geneExpTable.addGeneName("gene");
			geneExpTable.addGeneName("geneMrna");
			geneExpTable.addGeneName(GeneType.mRNA.toString());
			geneExpTable.addGeneName(GeneType.miRNA.toString());
			geneExpTable.addGeneName(GeneType.snoRNA.toString());
			geneExpTable.addGeneName(GeneType.snRNA.toString());
			geneExpTable.addGeneName(GeneType.Precursor_miRNA.toString());
			geneExpTable.addGeneName(GeneType.tRNA.toString());
			geneExpTable.addGeneName(GeneType.ncRNA.toString());
			geneExpTable.addGeneName(GeneType.miscRNA.toString());
			geneExpTable.addGeneName(GeneType.antisense_RNA.toString());
			geneExpTable.addGeneName(GeneType.rRNA.toString());
		}		
//		gene = 0;
//		/** 编码mRNA的基因数量 */
//		geneMrna = 0;
//		/** snoRNA的数量 */
//		snoRNA = 0;
//		/** miRNA前体的数量 */
//		precursorMirna = 0;
//		miRNA = 0;
//		tRNA = 0;
//		ncRNA = 0;
//		miscRNA = 0;
//		mRNA = 0;
//		snRNA = 0;
//		antisenseRNA = 0;
//		rRNA = 0;
	}
}
