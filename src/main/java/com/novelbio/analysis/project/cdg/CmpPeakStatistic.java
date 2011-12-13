package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.condHostInfoEnt;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ͳ�ƽ���������ܶȣ�ÿ100��peakȡһ������ͳ����һ�ٸ������ж����н�������û����������txt
 * @author zong0jie
 *
 */
public class CmpPeakStatistic {
	
	public static void main(String[] args) {
		CmpPeakStatistic cmpPeakStatistic = new CmpPeakStatistic();
		cmpPeakStatistic.getOverlapNum("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/WEseSort-W200-G600-E100vsNature2007.scoreisland",
				100, 7, 4, "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/peakCalling/SICER/WEseSort-W200-G600-E100vsNature2007_statistic.scoreisland");
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
	 * @param colNum ÿ������ȡ��
	 * @param colOverlap overlap����Ϣ����һ��
	 * @param colScore �еķ������Ӵ�С����
	 * @return  ls-int[2] 0��������1��overlap��Ϊ0�ĸ���
	 */
	private ArrayList<int[]> getOverlapNum(ArrayList<String[]> lsInfo,final int colNum,  int colOverlap,  int colScore)
	{
		colOverlap --;
		final int colscore = colScore - 1;
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		//����
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
			if (!lsInfo.get(i)[colOverlap].trim().equals("0")) {
				noneOverlapNum++;
			}
		}
		return lsResult;
	}
}
