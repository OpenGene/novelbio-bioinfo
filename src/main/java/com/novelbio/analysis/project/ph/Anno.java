package com.novelbio.analysis.project.ph;

import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class Anno {
	public static void main(String[] args) {
		anno();
	}
	
	private static void anno() {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG18_GFF_REFSEQ);
		gffChrAnno.setFilterTssTes(new int[]{-2000,2000}, null);
		String parent = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/";
		String txtFile = parent + "K4all_SE-W200-G600-E100.scoreisland";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
//		
//		txtFile = parent + "HSZ_W-4.clean.fq_SE-W200-G600-E100.scoreisland_score35.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
//		txtFile = parent + "k4sort-W200-G200-E100.scoreisland_score35.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
//		txtFile = parent + "W4sort-W200-G200-E100.scoreisland_score35.xls";
//		gffChrAnno.annoFile(txtFile, 1, 2, 3, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
		
		txtFile = parent + "out8vs9_Filtered_OneProbe.txt";
		gffChrAnno.annoFile(txtFile, 1, 3, 4, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
		
		txtFile = parent + "out89vs234567_Filtered_OneProbe.txt";
		gffChrAnno.annoFile(txtFile, 1, 3, 4, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
		
		txtFile = parent + "out456vs23789_Filtered_OneProbe.txt";
		gffChrAnno.annoFile(txtFile, 1, 3, 4, FileOperate.changeFileSuffix(txtFile, "_anno_-2k+2k", "xls"));
		
		
//		String parent1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentKO_K4K27Down_yulu_method20111208/";
//		String parent2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/bivalentWT_K4K27Down_yulu_method20111208/";
//
//		anno anno = new anno();
//		anno.cmpAccID(10090, parent1 + "KO_0d_bivalent_-2k+2k.xls",
//				parent2 + "bivalent_anno_WE_-2k+2kTss.xls"
//		);
//		anno.getAccID(10090, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/TKO-D4 vs FH-D42.txt", "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/TKO-D4 vs FH-D4_changeID.txt");
		
		
	}
}
