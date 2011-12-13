package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class StatisticMap {
	
	GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
			NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null,5);
	
	
	public static void main(String[] args) {
//		StatisticMap stat = new StatisticMap();
//		String bedFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/KEextend_sort.bed";
//		String outFile = FileOperate.changeFileSuffix(bedFile, "_statistic2", "txt");
//		stat.getStatic(bedFile, outFile);
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String bedFile = parent +  "4Kextend_sort.bed";
		BedSeq bedSeq = new BedSeq(bedFile);
		bedSeq.extend(240, parent + "4Kextend_sorted.bed");
		
		bedFile = parent + "4Wextend_sort.bed";
		bedSeq = new BedSeq(bedFile);
		bedSeq.extend(240, parent + "4Wextend_sorted.bed");
 	}
	
	public void getStatic(String bedFile, String outFile)
	{
		gffChrMap.setMapReads(bedFile, 10);
		gffChrMap.loadMapReads();
		ArrayList<String[]> lsInfo = gffChrMap.getChrLenInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsInfo, "\t", 1, 1);
		txtOut.close();
	}
}
