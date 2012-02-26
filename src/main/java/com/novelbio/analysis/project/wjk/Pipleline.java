package com.novelbio.analysis.project.wjk;

import java.awt.Color;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.chipseq.BedPeak;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class Pipleline {
	public static void main(String[] args) {
		Pipleline pipleline = new Pipleline();
		pipleline.TSSplot();
	}
	
	
	public void mappingBWA() {
		String file = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_all.fq";
		String fileFilter = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_all_filter2.fq";
		String fileOut = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_all.sam";
		String bedFileSE = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allSE.bed";
		String bedFile = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allExtend.bed";
		String bedFileSort = FileOperate.changeFileSuffix(bedFile, "_sort", null);
		FastQMapBwa fastq = new FastQMapBwa(fileFilter, FastQ.QUALITY_MIDIAN, fileOut, true);
		fastq.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
//		fastq = fastq.filterReads(fileFilter);
		fastq.getBedFileSE(bedFileSE);
		
//		fastq.getBedFile(bedFile).sortBedFile(bedFileSort);
		
//		BedPeakMacs bedPeakMacs = new BedPeakMacs(bedFileSE);
//		bedPeakMacs.peakCallling(null, BedPeakMacs.SPECIES_HUMAN, "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input", "DNYY");
//		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, 
//				NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, bedFileSort, 5);
	}
	
	public void TSSplot() {
//		String bedSort = "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allExtend_sort.bed";
//		BedSeq bedSeq = new BedSeq("/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allSE.bed");
//		bedSeq.extend(250, "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allExtend.bed")
//		.sortBedFile(v);
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, 
		NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "/media/winE/NBC/Project/ChIPSeq_WJK100909/ChIP_Seq_WJK100909_Mapping/input/hg_allExtend_sort.bed", 5);
		gffChrMap.loadMapReads();
		String txtExcel = "/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/Peak Information20100926_all_anno_sort.xls";
		
		
		gffChrMap.plotTssTesHeatMap(Color.BLUE, true, txtExcel, 14, 15, 2, 0, 50, 1, GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/HeatMap.png");
		
	}
	
	public void annotation() {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		gffChrAnno.setFilterTssTes(new int[]{-5000, }, null);
		gffChrAnno.annoFile("/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/Peak Information20100926_all.xls", 1, 2, 3,
				"/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/Peak Information20100926_all_anno.xls");
	}
	
}
