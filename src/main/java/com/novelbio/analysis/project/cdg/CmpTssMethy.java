package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrCmpBed;

/**
 * 用用余璐的方法比较TSS里面甲基化的区别
 * 具体：获得基因TSS左右若干bp范围，比较两个基因这个范围内的reads平均数
 * @author zong0jie
 *
 */
public class CmpTssMethy {
	public static void main(String[] args) {
		CmpTssMethy cmpTssMethy = new CmpTssMethy();
		cmpTssMethy.cmpTssK4W0_W4();
		cmpTssMethy.cmpTssK4K0_K4();
		cmpTssMethy.cmpTssWTK4W0_W4();
		cmpTssMethy.cmpTssWTK4K0_K4();
	}
	
	public void cmpTssK4W0_W4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"W4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"W0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "KO_0d_bivalent_-2k+2k.xls";
		String txtOutFile = parentOut + "KO_0d_bivalent_-2k+2k_K4_W0toW4up.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
 
		txtExcelFile = parentOut + "KO_0d_bivalent_-2k+2k.xls";
		txtOutFile = parentOut + "KO_0d_bivalent_-2k+2k_K4_W0toW4nochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
	public void cmpTssK4K0_K4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"k4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"k0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "KO_0d_bivalent_-2k+2k.xls";
		String txtOutFile = parentOut + "KO_0d_bivalent_-2k+2k_K4_k0tok4up.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
 
		txtExcelFile = parentOut + "KO_0d_bivalent_-2k+2k.xls";
		txtOutFile = parentOut + "KO_0d_bivalent_-2k+2k_K4_k0tok4nochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
	public void cmpTssWTK4W0_W4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_WT_final_anno/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"W4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"W0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalent_anno_WE_-2k+2kTss.xls";
		String txtOutFile = parentOut + "WT_0d_bivalent_-2k+2k_K4_W0toW4up.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
 
		txtExcelFile = parentOut + "bivalent_anno_WE_-2k+2kTss.xls";
		txtOutFile = parentOut + "WT_0d_bivalent_-2k+2k_K4_W0toW4nochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
	public void cmpTssWTK4K0_K4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_WT_final_anno/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"k4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"k0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalent_anno_WE_-2k+2kTss.xls";
		String txtOutFile = parentOut + "WT_0d_bivalent_-2k+2k_K4_k0tok4up.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
 
		txtExcelFile = parentOut + "bivalent_anno_WE_-2k+2kTss.xls";
		txtOutFile = parentOut + "WT_0d_bivalent_-2k+2k_K4_k0tok4nochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void cmpTssK27W0_W4() {
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/WEextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/4Wextend_sort.bed", 5);
		String txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_WEto4Wdown.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
		
		txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_WEto4Wnochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false, txtOutFile);
	}
	
	public void cmpTssK27k0_k2() {
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_Sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/2Kextend_sort.bed", 5);
		
		
		String txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_KEto2Kdown.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
		
		
		txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		 txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_KEto2Knochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}

	public void cmpTssK27k0_k4() {
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_Sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/4Kextend_sort.bed", 5);
		String txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_KEto4Kdown.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
		
		
		txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/KO_0d_bivalent_-2k+2k.xls";
		 txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/KO_0d_bivalent_-2k+2k_K27_KEto4Knochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
	public void cmpTssK4k0_k4() {
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_Sort.bed", 5);
		gffChrCmpBed.setMapReadsCmp("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/4Kextend_sort.bed", 5);
		
		gffChrCmpBed.loadMapReads();
		String txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentK4K27Down&FH20111208/bivalent_anno_-2k+2kTss.xls";
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentK4K27Down&FH20111208/bivalent_anno_K4_KEto4Kdown.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, 1.5, true, txtOutFile);
		
		
		gffChrCmpBed.loadMapReads();
		txtExcelFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentK4K27Down&FH20111208/bivalent_anno_-2k+2kTss.xls";
		 txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent_anno_K4_KEto4Knochange.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 6, new int[]{-2000,2000}, 1.15, false,txtOutFile);
	}
	
}































