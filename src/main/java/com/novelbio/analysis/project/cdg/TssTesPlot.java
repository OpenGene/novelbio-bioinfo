package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.chipseq.regDensity.RegDensity;
import com.novelbio.generalConf.NovelBioConst;

public class TssTesPlot {
	/**
	 * 20120214
	 * 补测的数据
	 */
	private void NewData()
	{
		 int[] columnID=new int[3]; columnID[0] = 1; columnID[1] = 2; columnID[2] = 3;//读取peak文件，读取哪几列，依次为 chrID， 起点， 终点
		 int[] colMap = new int[3]; colMap[0] = 1; colMap[1] =2; colMap[2] =3; //mapping 文件中 chr 起点 终点的位置 常规bed文件 1，2，3 王从茂的文件，0，1，2
		 int binNum = 5; //精度
		 int range = 5000;//上下游多少距离
		 String mapparentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
//		 String PeakparentFile = "/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/";//+ "peakCalling/";
		try {
			String mapFilePath=mapparentFIle+"K4all_SE.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/high.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "Nhigh";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
