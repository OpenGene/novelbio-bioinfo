package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.BedPeakSicer;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 分析nature2007里面的甲基化数据
 * @author zong0jie
 *
 */
public class Nature2007 {
	
	
	public static void main(String[] args) {
		Nature2007 nature2007 = new Nature2007();
		nature2007.MacsPeakCalling();
	}
	
	public void SicerPeakCalling() {
		String parString = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/Mapping/";
		BedPeakSicer bedPeakSicer = new BedPeakSicer(parString + "nature2007K27seSort.bed");
		bedPeakSicer.setGffFile(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		bedPeakSicer.setChIPType(BedPeakSicer.HISTONE_TYPE_H3K27);
		bedPeakSicer.setFilterTssTes(new int[]{-2000,2000}, null);
		bedPeakSicer.peakCallling(null, BedPeakSicer.SPECIES_MOUSE, "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER", "nature2007Sicer");
		//// annotation ////
		String parString2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER/";
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		gffChrAnno.setFilterTssTes(new int[]{-3000,3000}, null);
		gffChrAnno.setFilterGeneBody(true, false, false);
		String peakFile = parString2 + "nature2007K27seSort-W200-G600-E100.scoreisland";
		gffChrAnno.annoFile(peakFile, 1, 2, 3, FileOperate.changeFileSuffix(peakFile, "_anno_-3k_body", "xls"));
	}
	
	
	/**
	 * 用macs做nature2007的peakcalling
	 */
	public void MacsPeakCalling() {
//		String parString = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/Mapping/";
//		BedPeakMacs bedPeakMacs = new BedPeakMacs(parString + "nature2007K27seSort.bed");
//		bedPeakMacs.setNoLambda();
//		bedPeakMacs.peakCallling(null, BedPeakMacs.SPECIES_MOUSE, "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingMacs", "nature2007macs");
		//// annotation ////
		String parString2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingMacs/";
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		gffChrAnno.setFilterTssTes(new int[]{-3000,3000}, null);
		gffChrAnno.setFilterGeneBody(true, false, false);
		String peakFile = parString2 + "nature2007macs_peaks_new.csv";
		gffChrAnno.annoFile(peakFile, 1, 2, 3, FileOperate.changeFileSuffix(peakFile, "_anno_-3k_body", "xls"));
		
		
		
	}
	
	
}
