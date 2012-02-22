package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 统计交集里面的密度，每100个peak取一次样，统计这一百个里面有多少有交集多少没交集最后输出txt
 * @author zong0jie
 *
 */
public class CmpPeakStatistic {
	
	public static void main(String[] args) {
		CmpPeakStatistic cmpPeakStatistic = new CmpPeakStatistic();
		String file = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/comparePeak/W4vsK4-W200-G600-E100.scoreisland.xls";
		cmpPeakStatistic.getOverlapNum(file,
				100, 5, 4, FileOperate.changeFileSuffix(file, "_statistic", "xls"));
		file = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/comparePeak/W4vsKE-W200-G600-E100.scoreisland.xls";
		cmpPeakStatistic.getOverlapNum(file,
				100, 5, 4, FileOperate.changeFileSuffix(file, "_statistic", "xls"));
		file = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/comparePeak/WEvsKE-W200-G600-E100.scoreisland.xls";
		cmpPeakStatistic.getOverlapNum(file,
				100, 5, 4, FileOperate.changeFileSuffix(file, "_statistic", "xls"));
		file = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/comparePeak/WEvsW4-W200-G600-E100.scoreisland.xls";
		cmpPeakStatistic.getOverlapNum(file,
				100, 5, 4, FileOperate.changeFileSuffix(file, "_statistic", "xls"));
	}
	
	public void getOverlapNum(String fileName, int colNum, int colOverlap, int colScore, String outName) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(fileName, 1, 0, 1, 0);
		for (int i = 0; i < lsInfo.size(); i++) {
			String[] strings = lsInfo.get(i);
			if (strings == null || strings[colScore-1].trim().equals("")) {
			lsInfo.remove(i);
			}
		}
		ArrayList<int[]> lsResult = getOverlapNum(lsInfo, colNum, colOverlap, colScore);

		TxtReadandWrite txtOut = new TxtReadandWrite(outName, true);
		for (int[] is : lsResult) {
			txtOut.writefileln(is[0] + "\t" + is[1]);
		}
		txtOut.close();
		
	}
	
	/**
	 * @param lsInfo
	 * @param colNum 每多少列取样
	 * @param colOverlap overlap的信息在哪一列
	 * @param colScore 列的分数，从大到小排序
	 * @return  ls-int[2] 0：数量，1：overlap不为0的个数
	 */
	private ArrayList<int[]> getOverlapNum(ArrayList<String[]> lsInfo,final int colNum,  int colOverlap,  int colScore)
	{
		colOverlap --;
		final int colscore = colScore - 1;
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		//排序
		Collections.sort(lsInfo, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				Double score1 = Double.parseDouble(o1[colscore]);
				Double score2 = Double.parseDouble(o2[colscore]);
				return score2.compareTo(score1);
			}
		});
		int[] tmpInfo = new int[2]; int noneOverlapNum = 0;
		for (int i = 0; i < lsInfo.size(); i++) {
			if (i > 0 && i % colNum == 0) {
				tmpInfo = new int[2];
				tmpInfo[0] = i; tmpInfo[1] = noneOverlapNum;
				lsResult.add(tmpInfo);
				noneOverlapNum = 0;
			}
			if (!(Double.parseDouble(lsInfo.get(i)[colOverlap].trim()) ==0)) {
				noneOverlapNum++;
			}
		}
		return lsResult;
	}
}
