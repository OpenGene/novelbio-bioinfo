package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.genomeNew.GffChrCmpBed;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 用用余璐的方法比较TSS里面甲基化的区别
 * 具体：获得基因TSS左右若干bp范围，比较两个基因这个范围内的reads平均数
 * @author zong0jie
 *
 */
public class CmpTssMethy {
	public static void main(String[] args) {
		CmpTssMethy cmpTssMethy = new CmpTssMethy();
		cmpTssMethy.cmpTssK27K0_K2();
//		cmpTssMethy.cmpTssK27K0_K4();
		cmpTssMethy.cmpTssK27W0_W2();
//		cmpTssMethy.cmpTssK27W0_W4();
//		cmpTssMethy.cmpTssK4K0_K4();
//		cmpTssMethy.cmpTssK4W0_W4();
	}
	
	public void cmpTssK4W0_W4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"W4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"W0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K4_W0toW4.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
	public void cmpTssK4K0_K4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"k4_extend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"k0_extend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K4_K0toK4.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
	public void cmpTssK27W0_W2() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"2Wextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"WEextend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K27_WEto2W.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
	public void cmpTssK27W0_W4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"4Wextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"WEextend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K27_WEto4W.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
	public void cmpTssK27K0_K2() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"2Kextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"KEextend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K27_KEto2K.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
	public void cmpTssK27K0_K4() {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String parentOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalent/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"4Kextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"KEextend_sort.bed", 5);
		String txtExcelFile = parentOut + "bivalentAll.xls";
		String txtOutFile = parentOut + "bivalentAll_K27_KEtoK4.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-2000,2000}, -100, true, txtOutFile);
	}
}































