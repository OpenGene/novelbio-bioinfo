package com.novelbio.analysis.seq.chipseq.peakOverlap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;

/**
 * 限定A文件，让B文件慢慢增加，梯度计算覆盖度
 * @author zong0jie
 *
 */
public class PeakOverlapGrad {

	public static void main(String[] args) {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/compare2paper/gradientCompare/";
		String peakFileScore = parentPath + "WEall_SE-W200-G600-E100.scoreisland";
		String peakFileSearch = parentPath + "nature2007K27seSort-W200-G600-E100_egDefault.scoreisland";
		String txtPeakOverlapFile = parentPath + "WEall_vs_nature2007K27Default.scoreisland";
		PeakOverlapGrad peakOverlapGrad = new PeakOverlapGrad();
		peakOverlapGrad.getPeakOverLapInfo(peakFileSearch, peakFileScore, txtPeakOverlapFile, 100);
		
		
		peakFileSearch = parentPath + "nature2007K27seSort-W200-G600-E100_eg68.scoreisland";
		txtPeakOverlapFile = parentPath + "WEall_vs_nature2007K2768.scoreisland";
		peakOverlapGrad.getPeakOverLapInfo(peakFileSearch, peakFileScore, txtPeakOverlapFile, 100);
	}
	
	/**
	 * 有两个文件，一个是coverageFile，一个是addFile，我们想知道，随着addFile的增加（譬如根据score增加），coverageFile中有多少Peak被addFile所覆盖
	 * @param coverageFile 负链 第一行是标题列 chrID， startID，endID
	 * @param addFile 正链 第一行是标题列 chrID，startID，endID，scoreID
	 * @param txtPeakOverlapFile 输出的每个peakOverlap的细节
	 */
	public void getPeakOverLapInfo(String coverageFile, String addFile, String txtPeakOverlapFile, int stepNum) {
		//先把txt文本中的peak信息读取
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		int[] colNum=new int[3];
		colNum[0]=1; colNum[1]=2; colNum[2]=3;
		ArrayList<String[]> lsSearch = ExcelTxtRead.readLsExcelTxt(coverageFile,colNum, 2,-1);
		
		int[] colNum2=new int[4];
		colNum2[0]=1; colNum2[1]=2; colNum2[2]=3; colNum2[3]=4;
		ArrayList<String[]> lsPeak = ExcelTxtRead.readLsExcelTxt(addFile, colNum2, 2, -1);
		
		////对临时list进行排序,首先按照Chr排序，然后按照具体坐标排序
	     Collections.sort(lsSearch,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            	int i=arg0[0].compareTo(arg1[0]);
	            	if(i==0){
	            		Integer a0 = Integer.parseInt(arg0[1]);
	            		Integer a1 =  Integer.parseInt(arg1[1]);
	            		return a0.compareTo(a1);
	            	}
	               return i;
	            }});
	     /////////////////////////////////////////////////////////////////////////////
	     ////对临时list进行排序,按照score排序，从到到小排序
	     Collections.sort(lsPeak,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            		Double a0 = Double.parseDouble(arg0[3]);
	            		Double a1 = Double.parseDouble(arg1[3]);
	            		return -a0.compareTo(a1);//从到到小排序
	            }});
		/////////////////////////////////////////////////////////////////////////////
	     ArrayList<String[]> lsStepPeak = new ArrayList<String[]>();
	     lsStepPeak.add(lsPeak.get(0));
	     for (int i = 1; i < lsPeak.size(); i++) {
	    	 //每stepnum个将其装入新的lsPeak
			if (i%stepNum == 0 || i == lsPeak.size()-1) {
				String[] info = getOverlapInfo(lsStepPeak, lsSearch);
				lsResult.add(info);
			}
			lsStepPeak.add(lsPeak.get(i));
	     }
	     TxtReadandWrite txtOut = new TxtReadandWrite(txtPeakOverlapFile, true);
	     txtOut.ExcelWrite(lsResult);
	}
	
	
	
	/**
	 * 用lsSearch 去查找lsPeak，最后返回lsSearch有多少被lsPeak所覆盖
	 * @param lsPeak 无所谓排序
	 * @param lsSearch 必须排过序，不考虑合并，也就是简单的遍历peak进行搜索
	 * 0: chrID 1:start 2:end
	 * @return 0: Num of lsPeak   1:Num of lsSearchOverlap
	 */
	private String[] getOverlapInfo(ArrayList<String[]> lsPeak, ArrayList<String[]> lsSearch)
	{
		int overlapPeakNum = 0;
		ListHashBin gffHashPeak = new ListHashBin();
		gffHashPeak.ReadGff(lsPeak);
		for (String[] strings : lsSearch) {
			String chrID = strings[0]; int start = Integer.parseInt(strings[1]); int end = Integer.parseInt(strings[2]);
			ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> gffCodPeakDU = gffHashPeak.searchLocation(chrID, start, end);
			if (gffCodPeakDU == null) {
				continue;
			}
			if (gffCodPeakDU.getAllGffDetail().size() > 0) {
				overlapPeakNum++;
			}
		}
		String[] result = new String[]{lsPeak.size() + "", overlapPeakNum + "", (double)overlapPeakNum/lsSearch.size()*100 + ""};
		return result;
	}
	
}
