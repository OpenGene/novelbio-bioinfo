package com.novelbio.analysis.project.zhy;

import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 用散点图的方法来比较多个甲基化之间的差异
 * @author zong0jie
 *
 */
public class ScatterPlot {
	public static void main(String[] args) {
//		copePeakFile("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/peakcallingNew/H3K27all_SE-W200-G600-E100.scoreisland.xls");
		String peakFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/peakcallingNew/peakCompare/H3K27all_SE-W200-G600-E100.scoreisland_CombPeak.xls";
		String mapFile = "";
		String prix = "";
		
		peakFile = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/annotation_fastq_Bwa_map_filter_rRNA_sorted_coped3.bed";
		
		mapFile = "/media/winE/NBC/Project/Project_ZHY_Lab/" +
				"MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/Nextend_sort.bed";
		prix = "N_mirNumVsMethy";
		getMethyInfo(mapFile, prix, peakFile, null);
		
//		filterRRNA();

	}
	
	/**
	 * 过滤掉rRNA
	 */
	private static void filterRRNA()
	{
		String bedFile = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/annotation_fastq_Bwa_map.bed";
		TxtReadandWrite txtRead = new TxtReadandWrite(bedFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(bedFile, "_filter_rRNA", null), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if(ss[7].contains("rRNA"))
			{
				continue;
			}
			txtOut.writefileln(string);
		}
		txtRead.close();
		txtOut.close();
	}
	
	/**
	 * 将已经经过排序的peak合并
	 * @param peakFile
	 */
	private static void copePeakFile(String peakFile)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(peakFile, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(peakFile, "_CombPeak", null), true);
		String chrIDold = ""; int startOld = 0; int endOld = 0;
		for (String string : txtRead.readlines(2)) {
			String[] ss = string.split("\t");
			String chrID = ss[0];
			int start = Integer.parseInt(ss[1]);
			int end = Integer.parseInt(ss[2]);
			if (!chrID.equals(chrIDold) && !chrIDold.equals("")) {
				txtWrite.writefileln(new String[]{chrIDold, startOld+"", endOld+""});
				chrIDold = chrID;
				startOld = start;
				endOld = end;
			}
			else if (start < endOld) {
				endOld = end;
			}
			else if (start > endOld) {
				txtWrite.writefileln(new String[]{chrIDold, startOld+"", endOld+""});
				chrIDold = chrID;
				startOld = start;
				endOld = end;
			}
		}
		txtRead.close();
		txtWrite.close();
	}

	private static void getMethyInfo(String mapFile, String prix, String PeakFile, String correctFile)
	{
		TxtReadandWrite txtRead;
		TxtReadandWrite txtWrite;
		GffChrMap gffChrMap = new GffChrMap(
				NovelBioConst.GENOME_GFF_TYPE_TIGR,
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE,
				NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, mapFile, 10);
		gffChrMap.loadChrFile();
		gffChrMap.setMapCorrect(correctFile);
		gffChrMap.loadMapReads();
		
		
		txtRead = new TxtReadandWrite(PeakFile, false);
		txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(PeakFile, prix, null), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String mirNum = ss[3];
			MapInfo mapInfo = new MapInfo(ss[0], Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
			gffChrMap.getRegion(mapInfo, 10, 0);
			double methyScore = 0;
			try {
				methyScore = mapInfo.getMean();
			} catch (Exception e) {
				System.out.println(string);
				continue;
			}
			txtWrite.writefileln(new String[]{ss[0],ss[1],ss[2],mirNum, methyScore + ""});
		}
		
	}
}


