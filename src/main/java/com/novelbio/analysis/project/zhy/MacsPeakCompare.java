package com.novelbio.analysis.project.zhy;

import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.generalConf.NovelBioConst;

public class MacsPeakCompare {

	
	
	
	
	public static void main(String[] args) {
		MacsPeakCompare macsPeakCompare = new MacsPeakCompare();
		macsPeakCompare.annotationRegionTssGeneBodySingle();
	}
	
	/**
	 * 将SICER比较的peak覆盖到tss区域的基因挑选出来
	 */
	public void annotationRegionTssGeneBody() {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE);
		gffChrAnno.setFilterTssTes(new int[]{-1500,0}, null);
		gffChrAnno.setFilterGeneBody(true, false, false);
		String parentFIle = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareSICER/";
		
		String fileIn = parentFIle + "2NseSort-and-3NseSort-W200-G200-summary.xls";
		String fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", null);
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		
//		fileIn = parentFIle + "NseSort-and-2NseSort-W200-G200-summary.xls";
//		fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", null);
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		
		fileIn = parentFIle + "NseSort-and-3NseSort-W200-G200-summary.xls";
		fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", null);
		gffChrAnno.annoFile(fileIn, 1, 2, 3, fileOut);
	}
	
	/**
	 * 将SICERpeak覆盖到tss区域的基因挑选出来
	 */
	public void annotationRegionTssGeneBodySingle() {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE);
		gffChrAnno.setFilterTssTes(new int[]{-1500,0}, null);
		gffChrAnno.setFilterGeneBody(true, false, false);
		String parentFIle = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareSICER/single/";
//		
		String fileIn = parentFIle + "2NseSort-W200-G200-E100.scoreisland";
		String fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", "xls");
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
//		
//		fileIn = parentFIle + "NseSort-W200-G200-E100.scoreisland";
//		fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", null);
//		gffChrAnno.annotation(fileIn, 1, 2, 3, fileOut);
		
		fileIn = parentFIle + "3NseSort-W200-G200-E100.scoreisland";
		fileOut = FileOperate.changeFileSuffix(fileIn, "-1.5k-GeneBody_anno", null);
		gffChrAnno.annoFile(fileIn, 1, 2, 3, fileOut);
		
	}
	
	
	
	
	
	/**
	 * 绘制Tss附近区域与表达关联的heatmap图
	 */
	public void TssRegionDGE() {
		String parentPathDGE = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/DGEexpress/";
		String parentPathBed = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
		String parentPathOut = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTesHeatMap/";

		String dgeFile = parentPathDGE + "dgeexpress";
		String bedReads = parentPathBed + "Nextend_sort.bed";
		String outFile = "";
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE,
				NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, null, 10);
		gffChrMap.setPlotRegion(4000, 4000);
		gffChrMap.loadChrFile();

		
		bedReads = parentPathBed + "Nextend_sort.bed";
		outFile = parentPathOut + "N_TSS_ExpMax2Min_-4k+4k.png";
		gffChrMap.setMapReads(bedReads, 3);
		gffChrMap.loadMapReads();
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 2, 2, 0, 500, 1.6, GffDetailGene.TSS, 1000, outFile);
		outFile = parentPathOut + "N_TES_ExpMax2Min_-4k+4k.png";
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 2, 2, 0, 500, 1.6, GffDetailGene.TES, 1000, outFile);
		
		
		bedReads = parentPathBed + "2Nextend_sort.bed";
		outFile = parentPathOut + "2N_TSS_ExpMax2Min_-4k+4k.png";
		gffChrMap.setMapReads(bedReads, 3);
		gffChrMap.loadMapReads();
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 3, 2, 0, 500, 1.6, GffDetailGene.TSS, 1000, outFile);
		outFile = parentPathOut + "2N_TES_ExpMax2Min_-4k+4k.png";
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 3, 2, 0, 500, 1.6, GffDetailGene.TES, 1000, outFile);
		
		bedReads = parentPathBed + "3Nextend_sort.bed";
		outFile = parentPathOut + "3N_TSS_ExpMax2Min_-4k+4k.png";
		gffChrMap.setMapReads(bedReads, 3);
		gffChrMap.loadMapReads();
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 4, 2, 0, 500, 1.6, GffDetailGene.TSS, 1000, outFile);
		outFile = parentPathOut + "3N_TES_ExpMax2Min_-4k+4k.png";
		gffChrMap.plotTssTesHeatMap(false, dgeFile, 1, 4, 2, 0, 500, 1.6, GffDetailGene.TES, 1000, outFile);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
