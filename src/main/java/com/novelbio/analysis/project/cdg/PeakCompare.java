package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.chipseq.peakOverlap.PeakOverlap;

public class PeakCompare {
	public static void main(String[] args) {
		PeakCompare peakCompare = new PeakCompare();
		peakCompare.cmpPeaksWTK4vsWTK27();
	}
	
	
	/**
	 * �Ƚ����ǵ�WE��nature2007����peak����
	 */
	public void cmpPeaksTest()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
			String fileWE = parentFile1 + "2KseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
			String fileNature = parentFile2 + "2KseSort-W200-G600-E100single.scoreisland";
			/**
			 * ÿ��peakOverlap��ϸ��
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
	 * �Ƚ����ǵ�WE��nature2007����peak����
	 */
	public void cmpPeaksWE2Nature2007()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/";
			String fileWE = parentFile1 + "WEseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER/";
			String fileNature = parentFile2 + "nature2007K27seSort-W200-G600-E100.scoreisland";
			/**
			 * ÿ��peakOverlap��ϸ��
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
	 * �Ƚ����ǵ�WE��nature2007����peak����
	 */
	public void cmpPeaksWEvsW2()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/";
			String fileWE = parentFile1 + "WEseSort-W200-G600-E100.scoreisland";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result/PeakCallingSICER/";
			String fileNature = parentFile2 + "nature2007K27seSort-W200-G600-E100.scoreisland";
			/**
			 * ÿ��peakOverlap��ϸ��
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
	 * �Ƚ����ǵ�WE��W0����peak����
	 * Ҳ�����ҳ�Ǳ�ڵ�bivalent
	 */
	public void cmpPeaksWTK4vsWTK27()
	{
		try {
			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35.xls";

			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
			/**
			 * ÿ��peakOverlap��ϸ��
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
			 * ÿ��peakOverlap��ϸ��
			 */
			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K27sicer";
			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K27sicer_statistic";
			 

			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
			PeakOverlap.PeakStatistic("WTK27", "WTK4", fileWTK27, fileWTK4,
					txtPeakOverlapFileWE2NatureStic);
		} catch (Exception e) {
		}
//		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35_extend100.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35_extend100.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K4sicer_extend100";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K4sicer_extend100_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK4_extend100", "WTK27_extend100", fileWTK4, fileWTK27,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}
//		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35_extend100.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35_extend100.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K27sicer_extend100";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K27sicer_extend100_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK27_extend100", "WTK4_extend100", fileWTK27, fileWTK4,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}
		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_all.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K4sicer_K4all";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K4sicer_K4all_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK4", "WTK27", fileWTK4, fileWTK27,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}
//		
//		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_all.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K27sicer_K4all";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K27sicer_K4all_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK27", "WTK4", fileWTK27, fileWTK4,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}
		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35_extend500.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35_extend500.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K4sicer_extend500";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K4sicer_extend500_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK4, fileWTK27, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK4_extend500", "WTK27_extend500", fileWTK4, fileWTK27,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}
//		
//		try {
//			String parentFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK4 = parentFile1 + "W0sort-W200-G200-E100.scoreisland_score35_extend500.xls";
//
//			String parentFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String fileWTK27 = parentFile2 + "WEseSort-W200-G600-E100.scoreisland_score35_extend500.xls";
//			/**
//			 * ÿ��peakOverlap��ϸ��
//			 */
//			String parentFile3 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/bivalent/";
//			String txtPeakOverlapFileWE2Nature = parentFile3 + "WTbivalent_K27sicer_extend500";
//			String txtPeakOverlapFileWE2NatureStic = parentFile3 + "WTbivalent_K27sicer_extend500_statistic";
//			 
//
//			PeakOverlap.PeakOverLap(fileWTK27, fileWTK4, txtPeakOverlapFileWE2Nature);
//			PeakOverlap.PeakStatistic("WTK27_extend500", "WTK4_extend500", fileWTK27, fileWTK4,
//					txtPeakOverlapFileWE2NatureStic);
//		} catch (Exception e) {
//		}

	}
}
