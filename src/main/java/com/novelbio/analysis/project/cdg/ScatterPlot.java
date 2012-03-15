package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;

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
		
		peakFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/peakcallingNew/peakCompare/H3K27all_SE-W200-G600-E100.scoreisland_CombPeak.xls";
		
		mapFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/W4all_sorted_extend.bed";
		prix = "W4_correct0int";
		getMethyInfo(mapFile, prix, peakFile, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/regressByLocation/correctFileW4Liner0int");
		
//		mapFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/WEall_sorted_extend.bed";
//		prix = "WE";
//		getMethyInfo(mapFile, prix, peakFile, null);
		
		mapFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/K4all_sorted_extend.bed";
		prix = "K4_correct0int";
		getMethyInfo(mapFile, prix, peakFile, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/regressByLocation/correctFileK4Liner0int");
		
//		mapFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/KEall_sorted_extend.bed";
//		prix = "KE";
//		getMethyInfo(mapFile, prix, peakFile, null);
	}
	
	public static void main2(String[] args) {
		getMethInfo();
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
				NovelBioConst.GENOME_GFF_TYPE_UCSC,
				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, mapFile, 10);
		gffChrMap.loadChrFile();
		gffChrMap.setMapCorrect(correctFile);
		gffChrMap.loadMapReads();
		
		
		txtRead = new TxtReadandWrite(PeakFile, false);
		txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(PeakFile, prix, null), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			MapInfo mapInfo = new MapInfo(ss[0], Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
			gffChrMap.getRegion(mapInfo, 10, 0);
			double methyScore = 0;
			try {
				methyScore = mapInfo.getMean();
			} catch (Exception e) {
				System.out.println(string);
				continue;
			}
			txtWrite.writefileln(new String[]{ss[0],ss[1],ss[2], methyScore + ""});
		}
		
	}
	
	
	
	
	
	
	/**
	 * 给定基因，附带一个qPCR的值。以及tss的起点和终点 获得基因，qPCR的值，以及该区域mapping的reads中位数
	 */
	private static void getMethInfo() {
		String mapFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/K4all_sorted_extend.bed";
		GffChrMap gffChrMap = new GffChrMap(
				NovelBioConst.GENOME_GFF_TYPE_UCSC,
				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
				NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, mapFile, 10);
		gffChrMap.loadChrFile();
		gffChrMap.setMapCorrect(null);
		gffChrMap.loadMapReads();
		
		
		String txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/H3K27_D4_ChIP-qPCR_normalized_to_Gapdh_v2.txt";
		String out = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/H3K27_D4_ChIP-qPCR_normalized_K4.txt";
		String prix = "K4";
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		txtOut.writefileln(txtReadandWrite.readFirstLine() +"\t" + prix + "000\t" + prix + "100\t" + prix+"150\t"+
		prix+"200\t"+prix+"250\t"+prix+"300\t"+prix+"350\t"+prix+"400\t"+prix+"450\t"+prix+"500\t"
				+prix+"550\t"+prix+"600\t"+prix+"650\t"+prix+"700\t"+prix+"750\t"+prix+"800\t"+prix+"1000\t"+prix+"1200\t"+prix+"1400\t"+prix+"1600\t");
		for (String string : txtReadandWrite.readlines(2)) {
			String[] ss = string.split("\t");
			int start = Integer.parseInt(ss[2]); int end = Integer.parseInt(ss[3]);
			int mid = (start + end)/2;
			MapInfo mapInfo = null;

			String[] tmpResult = new String[27];
			for (int i = 0; i < ss.length; i++) {
				tmpResult[i] = ss[i];
			}
			
			mapInfo = new MapInfo(ss[1], start, end);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[7] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 100, mid + 100);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[8] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 150, mid + 150);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[9] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 200, mid + 200);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[10] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 250, mid + 250);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[11] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 300, mid + 300);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[12] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 350, mid + 350);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[13] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 400, mid + 400);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[14] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 450, mid + 450);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[15] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 500, mid + 500);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[16] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 550, mid + 550);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[17] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 600, mid + 600);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[18] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 650, mid + 650);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[19] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 700, mid + 700);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[20] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 750, mid + 750);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[21] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 800, mid + 800);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[22] = mapInfo.getMean() + "";
			
			
			mapInfo = new MapInfo(ss[1], mid - 1000, mid + 1000);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[23] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 1200, mid + 1200);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[24] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 1400, mid + 1400);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[25] = mapInfo.getMean() + "";
			
			mapInfo = new MapInfo(ss[1], mid - 1600, mid + 1600);
			gffChrMap.getRegion(mapInfo, gffChrMap.getThisInv(), 0);
			tmpResult[26] = mapInfo.getMean() + "";
			
	
			txtOut.writefileln(tmpResult);
			
		}
		txtOut.close();
	}
}

