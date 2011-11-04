package com.novelbio.analysis.project.cdg;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.fileOperate.FileOperate;

public class SicerPeakcalling {
	
	GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
	
	
	public static void main(String[] args) {
		SicerPeakcalling sicerpeakcalling = new SicerPeakcalling();
		sicerpeakcalling.TssRegionDGE2sample2();
	}
	
	/**
	 * 将peak覆盖到tss区域的基因挑选出来
	 */
	public void annotationRegionTss() {
		gffChrAnno.setFilterTssTes(new int[]{-2000,2000}, null);
		
		
//		String parentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/compareSICER/compareResult/";
//		String fileIn = parentFIle + "2KseSort-and-2WseSort-W200-G600-summary";
//		String fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
//	
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
//		fileIn = parentFIle + "2KseSort-and-KEseSort-W200-G600-summary";
//		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
//		fileIn = parentFIle + "2WseSort-and-WEseSort-W200-G600-summary";
//		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
//		fileIn = parentFIle + "KEseSort-and-WEseSort-W200-G600-summary";
//		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		
		
		String parentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
		String fileIn = parentFIle + "2KseSort-W200-G600-E100.scoreisland";
		String fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
	
		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		fileIn = parentFIle + "2WseSort-W200-G600-E100.scoreisland";
		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		fileIn = parentFIle + "KEseSort-W200-G600-E100.scoreisland";
		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		fileIn = parentFIle + "WEseSort-W200-G600-E100.scoreisland";
		fileOut = FileOperate.changeFileSuffix(fileIn, "_anno", "txt");
		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		
		
		
		
		
		
		
		
	}
	/**
	 * 绘制Tss附近区域与FX2关联的heatmap图
	 */
	public void TssRegionDGE() {
		String parentPathDGE = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/";
		String parentPathBed = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG101011/mapping/";
		String parentPathOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/regionreads/heatmap/";

		String dgeFile = parentPathDGE + "mouseRefSeqBGnoDuplication.txt";
		String bedReads = parentPathBed + "";
		String outFile = "";
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null, 10);
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.loadChrFile();

		
		bedReads = parentPathBed + "sortEK.filt.bed";
		outFile = parentPathOut + "EK_Test_TSS_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotTssTesHeatMap(Color.blue, true, dgeFile, 1, 2, 2, 0, 7, 1.1, GffDetailGene.TSS, 1000, outFile);
		
		bedReads = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/2Wextend_sort.bed";
		outFile = parentPathOut + "2W_test_TSS_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotTssTesHeatMap(Color.blue,false, dgeFile, 1, 2, 2, 0, 30, 1.1, GffDetailGene.TSS, 1000, outFile);
//
//		bedReads = parentPathBed + "KEextend_sort.bed";
//		outFile = parentPathOut + "KE_TSS_FX2Max2Min_-5k+5k.png";
//		gffChrMap.setMapReads(bedReads, 5);
//		gffChrMap.loadMapReads();
//		gffChrMap.plotTssTesHeatMap(false, dgeFile, 10, 11, 2, 0, 20, 1, GffDetailGene.TSS, 1000, outFile);
//		
//		bedReads = parentPathBed + "WEextend_sort.bed";
//		outFile = parentPathOut + "WE_TSS_FX2Max2Min_-5k+5k.png";
//		gffChrMap.setMapReads(bedReads, 5);
//		gffChrMap.loadMapReads();
//		gffChrMap.plotTssTesHeatMap(false, dgeFile, 10, 11, 2, 0, 20, 1, GffDetailGene.TSS, 1000, outFile);
	}
	
	public void TssRegionDGE2sample() {
		
		String parentPathDGE = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/";
		String parentPathBed1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentPathBed2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		
		String parentPathOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/regionreads/heatmap/";

		String dgeFile = parentPathDGE + "K0m2svsKEs2m.txt";
		String bedReads =  "";
		String outFile =parentPathOut + "";
		
		
		
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null, 10);
//		gffChrMap.setMapNormType(MapReads.NORMALIZATION_PER_GENE);
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.loadChrFile();
		gffChrMap.setPlotRegion(5000, 5000);
		
		bedReads = parentPathBed1 + "k0_extend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		ArrayList<MapInfo> lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "KEextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		ArrayList<MapInfo> lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "K0_4vsKE_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 100, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed1 + "W0_extend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "WEextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "W0_4vsWE_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 100, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed1 + "k4_extend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "2Kextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "K4_4vs2K_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 100, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed1 + "W4_extend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "2Wextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 1, 2, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "W4_4vs2W_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 100, 0, 10);
		
	}
	
	
	public void TssRegionDGE2sample2() {
		
		String parentPathDGE = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/FX2.clean.fq/result/annotation/";
		String parentPathBed1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String parentPathBed2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		
		String parentPathOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/regionreads/heatmap/";

		String dgeFile = parentPathDGE + "FX2_peaks_summit_+2k-2k_filterAnnotation_sort.txt";
		String bedReads =  "";
		String outFile =parentPathOut + "";
		
		
		
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null, 10);
//		gffChrMap.setMapNormType(MapReads.NORMALIZATION_PER_GENE);
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.loadChrFile();
		gffChrMap.setPlotRegion(5000, 5000);
		
		bedReads = parentPathBed2 + "KEextend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		ArrayList<MapInfo> lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "WEextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		ArrayList<MapInfo> lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "KE_27vsWE_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 10, 0, 10);
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos1, outFile+"_test.png", 0, 10, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed2 + "2Kextend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "2Wextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "2K_27vs2W_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 10, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed2 + "KEextend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "2Kextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "KE_27vs2K_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 10, 0, 10);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		bedReads = parentPathBed2 + "WEextend_sort.bed";
		
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		MapInfo.sortPath(true);
		 lsMapInfos1 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		
		bedReads = parentPathBed2 + "2Wextend_sort.bed";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		 lsMapInfos2 = gffChrMap.readFileGeneMapInfo(dgeFile, 10, 11, 2, GffDetailGene.TSS, 1000);
		MapInfo.sortPath(true);
		Collections.sort(lsMapInfos1);
		outFile =parentPathOut + "WE_27vs2W_27_NormPerGene.png";
		//第一个red第二个green
		gffChrMap.plotHeatMap2(lsMapInfos1, lsMapInfos2, outFile, 0, 10, 0, 10);
		
	}
	
	
	
	
	/**
	 * 绘制FX2附近reads分布的heatmap图
	 */
	public void SummitRegionDGE() {
		String parentPathSummit = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/FX2.clean.fq/result/annotation/";
		String parentPathBed = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String parentPathOut = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/regionreads/heatmap/";

		String dgeFile = parentPathSummit + "FX2_peaks_summit.xls";
		String bedReads = parentPathBed + "";
		String outFile = "";
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null, 10);
		gffChrMap.setPlotRegion(5000, 5000);
		gffChrMap.loadChrFile();

		
		bedReads = parentPathBed + "2Kextend_sort.bed";
		outFile = parentPathOut + "2K_Summit_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotSummitHeatMap(true, dgeFile, 1, 6, 5000, 10, 2, 0, 20, 1, 1000, outFile);
		
		bedReads = parentPathBed + "2Wextend_sort.bed";
		outFile = parentPathOut + "2W_Summit_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotSummitHeatMap(true, dgeFile, 1, 6, 5000, 10, 2, 0, 20, 1, 1000, outFile);

		bedReads = parentPathBed + "KEextend_sort.bed";
		outFile = parentPathOut + "KE_Summit_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotSummitHeatMap(true, dgeFile, 1, 6, 5000, 10, 2, 0, 20, 1, 1000, outFile);
		
		bedReads = parentPathBed + "WEextend_sort.bed";
		outFile = parentPathOut + "WE_Summit_FX2Max2Min_-5k+5k.png";
		gffChrMap.setMapReads(bedReads, 5);
		gffChrMap.loadMapReads();
		gffChrMap.plotSummitHeatMap(true, dgeFile, 1, 6, 5000, 10, 2, 0, 20, 1, 1000, outFile);
	}
}
