package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class Bivalent {
	

	
	public static void main(String[] args) {
		Bivalent bivalent = new Bivalent();
		bivalent.anno();
	}
	
	GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
	int[] filterTss = new int[]{-2000,2000};
	private void anno() {
		String txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/K27_WE-W200-G600-E100_score35_overlap_filter.xls";
		String outFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/K27_WE-W200-G600-E100_score35_overlap_filter_anno.xls";
		gffChrAnno.setFilterTssTes(filterTss, null);
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, outFile);
		
//		txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/K4-W0-W200-G200-E100_score35_overlap_filter.xls";
//		outFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/K4-W0-W200-G200-E100_score35_overlap_filter_anno.xls";
//		gffChrAnno.setFilterTssTes(filterTss, null);
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, outFile);
		
		
		txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_WT_final_anno/W0sort-W200-G200-E100.scoreisland_score35.xls";
		outFile = FileOperate.changeFileSuffix(txtFile, "_anno", null);
		gffChrAnno.setFilterTssTes(filterTss, null);
		gffChrAnno.annoFile(txtFile, 1, 2, 3, outFile);
		
//		txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/K27W0-W2/WEseSort-and-2WseSort-W200-G600-summary_2foldUp.xls";
//		outFile = FileOperate.changeFileSuffix(txtFile, "_anno", null);
//		gffChrAnno.setFilterTssTes(filterTss, null);
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, outFile);
	}
	
	
}
