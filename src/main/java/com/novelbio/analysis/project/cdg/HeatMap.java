package com.novelbio.analysis.project.cdg;

import java.awt.Color;
import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class HeatMap {
	public static void main(String[] args) {
//		plotHeatMapK27_FHE();
//		plotHeatMapK27_FX2();
//		plotHeatMapK27_K2();
//		plotHeatMapK27_K4();
//		plotHeatMapK27_KE();
//		plotHeatMapK27_W2();
//		plotHeatMapK27_W4();
//		plotHeatMapK27_WE();
//		plotHeatMapK4_K0();
//		plotHeatMapK4_K4();
//		plotHeatMapK4_W0();
//		plotHeatMapK4_W4();
		plotHeatMapK27_WT0vsKO0();
		
	}
	
	
	static GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
			NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
			null, 10);
	static String fileK4me3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/SortbyK4_WT/W0sort-W200-G200-E100_anno_-2k+2k.xls";
	static String fileK27me3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/2WseSort-W200-G600-E100.scoreisland_score35_anno_-2k+2k.xls";

	static String outParent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/SortbyK4_WT/";
	static String bedParentK4 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
	static String bedParentK27 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
	private static void plotHeatMapK4_W0() {
		String bedFile = bedParentK4 + "W0_extend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,20,1,GffDetailGene.TSS, 1000, outParent+"K4_W0.png");
	}
	private static void plotHeatMapK27_WT0vsKO0() {
		String bedFileWT = bedParentK27 + "4Wextend_sort.bed";
		String bedFileKO = bedParentK27 + "4Kextend_sort.bed";
		gffChrMap.setMapReads(bedFileWT, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		ArrayList<MapInfo> lsReadsWT = gffChrMap.readFileGeneMapInfo(fileK4me3, 1, 2, 2, GffDetailGene.TSS, 10);
		
		gffChrMap.setMapReads(bedFileKO, 5);
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		ArrayList<MapInfo> lsReadsKO = gffChrMap.readFileGeneMapInfo(fileK27me3, 5, 4, 2, GffDetailGene.TSS, 10);
		
		GffChrMap.plotHeatMapMinus(lsReadsWT, lsReadsKO, FileOperate.getParentPathName(fileK27me3)+"heatmap.jpg", -2, 2);
	}
	private static void plotHeatMapK4_K0() {
		String bedFile = bedParentK4 + "k0_extend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,10,1,GffDetailGene.TSS, 1000, outParent+"K4_K0.png");
	}
	
	private static void plotHeatMapK4_W4() {
		String bedFile = bedParentK4 + "W4_extend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,10,1,GffDetailGene.TSS, 1000, outParent+"K4_W4.png");
	}
	
	private static void plotHeatMapK4_K4() {
		String bedFile = bedParentK4 + "k4_extend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,10,1,GffDetailGene.TSS, 1000, outParent+"K4_K4.png");
	}
	
	
	private static void plotHeatMapK27_WE() {
		String bedFile = bedParentK27 + "WEextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_WE.png");
	}
	private static void plotHeatMapK27_W2() {
		String bedFile = bedParentK27 + "2Wextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_W2.png");
	}
	private static void plotHeatMapK27_W4() {
		String bedFile = bedParentK27 + "4Wextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_W4.png");
	}
	
	
	private static void plotHeatMapK27_KE() {
		String bedFile = bedParentK27 + "KEextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_KE.png");
	}
	private static void plotHeatMapK27_K2() {
		String bedFile = bedParentK27 + "2Kextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_K2.png");
	}
	private static void plotHeatMapK27_K4() {
		String bedFile = bedParentK27 + "4Kextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"K27_K4.png");
	}
	
	private static void plotHeatMapK27_FX2() {
		String bedFile = bedParentK27 + "FX2extend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"FX2.png");
	}
	private static void plotHeatMapK27_FHE() {
		String bedFile = bedParentK27 + "FHE.clean.fq_ExtendSort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,fileK4me3, 1, 2, 2, 0,2.5,1,GffDetailGene.TSS, 1000, outParent+"FHE.png");
	}
	
	
}
