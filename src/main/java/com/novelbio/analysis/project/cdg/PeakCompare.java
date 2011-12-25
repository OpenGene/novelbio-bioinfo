package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.chipseq.peakOverlap.PeakOverlap;

public class PeakCompare {
	public static void main(String[] args) {
		PeakCompare peakCompare = new PeakCompare();
		peakCompare.cmpPeaksWT4d_K4vsWTK27();
		peakCompare.cmpPeaksKO4d_K4vsWTK27();
	}
	
	
	/**
	 * 比较我们的WE与nature2007，从peak层面
	 */
	public void cmpPeaksTest()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
			String fileWE = parentFile1 + "2KseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
			String fileNature = parentFile2 + "2KseSort-W200-G600-E100single.scoreisland";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "2Kse_vs_2KseSingle";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "2Kse_vs_2KseSingle_statistic";
			 

			PeakOverlap.PeakOverLap(fileWE, fileNature, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("2Kse", "2KseSingle", fileWE, fileNature,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 比较我们的WE与nature2007，从peak层面
	 */
	public void cmpPeaksWE2Nature2007()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/";
			String fileWE = parentFile1 + "WEseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER/";
			String fileNature = parentFile2 + "nature2007K27seSort-W200-G600-E100.scoreisland";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/compareXLYWEvsPaper/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WEsicer_vs_Nature2007sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WEsicer_vs_Nature2007sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWE, fileNature, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WE", "Nature2007", fileWE, fileNature,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 比较我们的WE与nature2007，从peak层面
	 */
	public void cmpPeaksWEvsW2()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/";
			String fileWE = parentFile1 + "WEseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER/";
			String fileNature = parentFile2 + "nature2007K27seSort-W200-G600-E100.scoreisland";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/compareXLYWEvsPaper/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WEsicer_vs_Nature2007sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WEsicer_vs_Nature2007sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWE, fileNature, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WE", "Nature2007", fileWE, fileNature,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
	
	
	
	/**
	 * 比较我们的WE与W0，从peak层面
	 * 也就是找出潜在的bivalent
	 */
	public void cmpPeaksWT_K4vsWTK27()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35.xls";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K4sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K4sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK4", "WTK27", fileWTK4, fileWTK27,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
		
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35.xls";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K27sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K27sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK27", "WTK4", fileWTK27, fileWTK4,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}


/**
 * 比较我们的WE与W0，从peak层面
 * 也就是找出潜在的bivalent
 */
	public void cmpPeaksKO_K4vsWTK27() {
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/";
			String fileKO_K4 = parentFile1 + "K4-k0sort-W200-G200-E100.scoreisland_score35.xls";

			String parentFile2 = parentFile1;
			String fileKO_K27 = parentFile2 + "K27-KEseSort-W200-G600-E100.scoreisland_score35.xls";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = parentFile1;
			String txtPeakOverlapFileWE2Nature = parentFile3 + "KO_bivalent_K4sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "KObivalent_K4sicer_statistic";

			PeakOverlap.PeakOverLap(fileKO_K4, fileKO_K27,
					txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("KO_K4", "KO_K27", fileKO_K4, fileKO_K27,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}

		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/score35_KO_final_anno/";
			String fileKO_K4 = parentFile1 + "K4-k0sort-W200-G200-E100.scoreisland_score35.xls";

			String parentFile2 = parentFile1;
			String fileKO_K27 = parentFile2 + "K27-KEseSort-W200-G600-E100.scoreisland_score35.xls";
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = parentFile1;
			String txtPeakOverlapFileWE2Nature = parentFile3 + "KO_bivalent_K27sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "KObivalent_K27sicer_statistic";

			PeakOverlap.PeakOverLap(fileKO_K27, fileKO_K4,
					txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("KO_K27", "KO_K4", fileKO_K27, fileKO_K4,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
	
	
	
	
	
	
	
	/**
	 * 比较我们的WE与W0，从peak层面
	 * 也就是找出潜在的bivalent
	 */
	public void cmpPeaksWT4d_K4vsWTK27()
	{
		try {

			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";
			String fileWTK4 = parentFile1 + "W4sort-W200-G200-E100.scoreisland_score35.xls";
			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/K4_W4/PeakCalling/";
			String fileWTK27 = parentFile2 + "HSZ_W-4.clean.fq_SE-W200-G600-E100.scoreisland_score35.xls";
 
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WT4d_bivalent_K4sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WT4d_bivalent_K4sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK4", "WTK27", fileWTK4, fileWTK27,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
		
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";
			String fileWTK4 = parentFile1 + "W4sort-W200-G200-E100.scoreisland_score35.xls";
			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/K4_W4/PeakCalling/";
			String fileWTK27 = parentFile2 + "HSZ_W-4.clean.fq_SE-W200-G600-E100.scoreisland_score35.xls";
 
			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WT4d_bivalent_K27sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WT4d_bivalent_K27sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK27", "WTK4", fileWTK27, fileWTK4,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
	/**
	 * 比较我们的WE与W0，从peak层面
	 * 也就是找出潜在的bivalent
	 */
	public void cmpPeaksKO4d_K4vsWTK27()
	{
		try {
			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/K4_W4/PeakCalling/";
			String fileWTK27 = parentFile2 + "HSZ_K-4.clean.fq_SE-W200-G600-E100.scoreisland_score_35.xls";
 
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";
			String fileWTK4 = parentFile1 + "k4sort-W200-G200-E100.scoreisland_score35.xls";

			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "KO4d_bivalent_K4sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "KO4d_bivalent_K4sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK4", "WTK27", fileWTK4, fileWTK27,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
		
		try {
			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/K4_W4/PeakCalling/";
			String fileWTK27 = parentFile2 + "HSZ_K-4.clean.fq_SE-W200-G600-E100.scoreisland_score_35.xls";
 
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";
			String fileWTK4 = parentFile1 + "k4sort-W200-G200-E100.scoreisland_score35.xls";

			/**
			 * 每个peakOverlap的细节
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "KO4d_bivalent_K27sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "KO4d_bivalent_K27sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK27", "WTK4", fileWTK27, fileWTK4,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
	}
 
}