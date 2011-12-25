package com.novelbio.analysis.project.zhy;

import java.awt.Color;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;

public class HeatMap {
	public static void main(String[] args) {
		plotHeatMapN();
		plotHeatMap2N();
		plotHeatMap3N();
	}
	
	
	static GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE,
			NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, 
			null, 10);
	static String dgeNhigh2small = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/difgene/ZHYnewDGE.xls";
	static String outParent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTesHeatMap/";
	static String bedParent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
	private static void plotHeatMapN() {
		String bedFile = bedParent + "Nextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(4000, 4000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TSS, 1000, outParent+"Ntss-4k+4k.png");
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TES, 1000, outParent+"Ntes-4k+4k.png");

	}
	
	private static void plotHeatMap2N() {
		String bedFile = bedParent + "2Nextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(4000, 4000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TSS, 1000, outParent+"2Ntss-4k+4k.png");
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TES, 1000, outParent+"2Ntes-4k+4k.png");
	}
	
	private static void plotHeatMap3N() {
		String bedFile = bedParent + "3Nextend_sort.bed";
		gffChrMap.setMapReads(bedFile, 5);
		gffChrMap.loadChrFile();
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_ALL_READS);
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(4000, 4000);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TSS, 1000, outParent+"3Ntss-4k+4k.png");
		gffChrMap.plotTssTesHeatMap(Color.blue,false,dgeNhigh2small, 1, 2, 2, 0,20,1,GffDetailGene.TES, 1000, outParent+"3Ntes-4k+4k.png");
	}
	
	
	
}
