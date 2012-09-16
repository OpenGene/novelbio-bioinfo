package com.novelbio.analysis.taqman;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class InvDataExtract {
	/**
	 * 如果一个miRNA有超过0.6的没有被检出，那么就删除该miRNA
	 */
	double filterProp = 0.6;
	/**
	 * 遇到CT为该单词的就记为没有检出，如果一列里面超过filterProp比例的的CT含有该数字，就删除该miRNA<br>
	 * 暂时设定为 Undetermined
	 */
	String flagFilter = "Undetermined";
	/**
	 * CT
	 */
	String flagCT = "Ct";
	/**
	 * average delta ct
	 */
	String flagAvgCT = "Avg Delta Ct";
	
	
	
	
	public static void main(String[] args) {
		InvDataExtract invDataExtract = new InvDataExtract();
		String parentPath = "/media/winE/NBC/Project/TaqMan_CXD_111230/";
		String excelTaqman = parentPath + "Human MicroRNA Array CT值统计报告.xls";
		String out = parentPath + "deltaCT.txt";
		invDataExtract.getInfo(excelTaqman, out, 2);
	}
	
	
	
	
	
	
	
	/**
	 * 给定英骏的taqman探针文件，将CT提取出来
	 * 遇到CT为 Undetermined单词的就记为没有检出，如果一列里面超过filterProp比例的的CT含有该数字，就删除该miRNA<br>
	 * @param excelFile
	 * @param OutData
	 * @param startRow
	 */
	public void getInfo(String excelFile, String OutData, int startRow)
	{
		ExcelOperate excelOperate = new ExcelOperate(excelFile);
		ArrayList<String[]> lsAllData = excelOperate.ReadLsExcel(1, 1, -1, -1);
		ArrayList<Integer> lsCTcol = getCT_AvgCT_col(lsAllData.get(1), flagCT);
		ArrayList<Integer> lsDeltaCTcol = getCT_AvgCT_col(lsAllData.get(1), flagAvgCT);
		ArrayList<String[]> lsResult = filterData(lsAllData, startRow, lsCTcol, lsDeltaCTcol);
		TxtReadandWrite txtOut = new TxtReadandWrite(OutData, true);
		txtOut.ExcelWrite(lsResult);
	}
	
	
	
	
	
	
	/**
	 * 输入Invitrogen的tapman文件得到的list，进行初步筛选，返回CT检出的基因 <br>
	 * @param lsData 读取得到的数据list
	 * @param startRow 从第几行开始读，实际行
	 * @param lsCTcol 读取哪几列，也就是CT列
	 */
	private ArrayList<String[]> filterData(ArrayList<String[]> lsData, int startRow,List<Integer> lsCTcol, List<Integer> lsDeltaCTcol)
	{
		int filterNum = (int)(filterProp * lsDeltaCTcol.size()+0.5); 
		startRow-- ; 
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		/**
		 * 标题去冗余用的，同样的标题后面加上_1标记
		 */
		HashSet<String> hashRowName = new HashSet<String>();
		
		for (int i = startRow; i < lsData.size(); i ++) {
			String[] tmpValue = lsData.get(i);
			int filterNumThis = 0;
			for (Integer integer : lsCTcol) {
				if (tmpValue[integer].equals(flagFilter))
					filterNumThis ++;
			}
			
			if (filterNumThis >= filterNum)
				continue;
			
			String[] tmpResult = new String[lsDeltaCTcol.size() + 1];
			String tmpName = tmpValue[1].replace("#", "&");
			int id = 1; String tmpName2 = tmpName;
			boolean flag = true;
			while (flag) {
				if (! hashRowName.contains(tmpName2)) {
					hashRowName.add(tmpName2);
					flag = false;
				}
				else {
					tmpName2 = tmpName + "_" + id;
					id ++;
				}
			}
			tmpResult[0] = tmpName2;
			for (int j = 1; j < tmpResult.length; j++) {
				tmpResult[j] = tmpValue[lsDeltaCTcol.get(j-1)];
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	/**
	 * 指定标题列，返回所有是 指定string  的列的num <br>
	 * 标题列类似Array Card Name \t Assay \t Task \t Ct \t Avg Delta Ct \t Ct \t Avg Delta Ct \t Ct	Avg Delta Ct \t Ct \t Avg Delta Ct \t Ct \t Avg Delta Ct <br>
	 * 如果一个都没找到，返回null <br>
	 * @param strTitle 标题列
	 * @param flagStr 用equal该字符的列来检测是否检出,可以为 Ct 或者 Avg Delta Ct
	 * @return
	 */
	private ArrayList<Integer> getCT_AvgCT_col(String[] strTitle, String flagStr)
	{
		/**
		 * 含有CT的列
		 */
		ArrayList<Integer> lsCT = new ArrayList<Integer>();
		for (int i = 0; i < strTitle.length; i++) {
			if (strTitle[i]!= null && strTitle[i].equals(flagStr)) {
				lsCT.add(i);
			}
		}
		if (lsCT.size() == 0) {
			return null;
		}
		return lsCT;
	}
	
	
}
