package com.novelbio.analysis.project.cdg;

import java.awt.Color;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;

public class HeatMap {
	public static void main(String[] args) {
		plotHeatMap();
		
	}
	public static void plotHeatMap() {
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/WEextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_WE_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/2Wextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_2W_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/4Wextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_4W_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_KE_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/2Kextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_2K_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_bivalent/bivalentK27/K27Only_Bivalent.xls";
//			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
//					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
//					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/4Kextend_sort.bed", 10);
//			gffChrMap.loadChrFile();
//			gffChrMap.loadMapReads();
//			gffChrMap.setPlotRegion(5000, 5000);
//			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
//			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
//					1, 3, 2, 0,2,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K27_4K_TSS_max1.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		///////////////////////////////////////////////////////
		////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////
		try {
			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_bivalent/bivalentK4/K4_Only_bivalent.xls";
			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/W0_extend_sort.bed", 10);
			gffChrMap.loadChrFile();
			gffChrMap.loadMapReads();
			gffChrMap.setPlotRegion(5000, 5000);
			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
					1,3, 2, 0,10,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_W0_TSS_max5.png");
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_bivalent/bivalentK4/K4_Only_bivalent.xls";
			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/W4_extend_sort.bed", 10);
			gffChrMap.loadChrFile();
			gffChrMap.loadMapReads();
			gffChrMap.setPlotRegion(5000, 5000);
			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
					1, 3, 2, 0,10,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_W4_TSS_max5.png");
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_bivalent/bivalentK4/K4_Only_bivalent.xls";
			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/k0_extend_sort.bed", 10);
			gffChrMap.loadChrFile();
			gffChrMap.loadMapReads();
			gffChrMap.setPlotRegion(5000, 5000);
			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
					1, 3, 2, 0,10,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_k0_TSS_max5.png");
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String geneFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_bivalent/bivalentK4/K4_Only_bivalent.xls";
			GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
					NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, 
					"/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/k4_extend_sort.bed", 10);
			gffChrMap.loadChrFile();
			gffChrMap.loadMapReads();
			gffChrMap.setPlotRegion(5000, 5000);
			gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
			gffChrMap.plotTssTesHeatMap(Color.blue,true,geneFile,
					1, 3, 2, 0,10,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/heatmap/K4_k4_TSS_max5.png");
		} catch (Exception e) {
			// TODO: handle exception
		}
	 
		
	
	}
}
