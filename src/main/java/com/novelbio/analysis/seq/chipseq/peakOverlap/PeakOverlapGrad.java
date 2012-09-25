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
 * �޶�A�ļ�����B�ļ��������ӣ��ݶȼ��㸲�Ƕ�
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
	 * �������ļ���һ����coverageFile��һ����addFile��������֪��������addFile�����ӣ�Ʃ�����score���ӣ���coverageFile���ж���Peak��addFile������
	 * @param coverageFile ���� ��һ���Ǳ����� chrID�� startID��endID
	 * @param addFile ���� ��һ���Ǳ����� chrID��startID��endID��scoreID
	 * @param txtPeakOverlapFile �����ÿ��peakOverlap��ϸ��
	 */
	public void getPeakOverLapInfo(String coverageFile, String addFile, String txtPeakOverlapFile, int stepNum) {
		//�Ȱ�txt�ı��е�peak��Ϣ��ȡ
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		int[] colNum=new int[3];
		colNum[0]=1; colNum[1]=2; colNum[2]=3;
		ArrayList<String[]> lsSearch = ExcelTxtRead.readLsExcelTxt(coverageFile,colNum, 2,-1);
		
		int[] colNum2=new int[4];
		colNum2[0]=1; colNum2[1]=2; colNum2[2]=3; colNum2[3]=4;
		ArrayList<String[]> lsPeak = ExcelTxtRead.readLsExcelTxt(addFile, colNum2, 2, -1);
		
		////����ʱlist��������,���Ȱ���Chr����Ȼ���վ�����������
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
	     ////����ʱlist��������,����score���򣬴ӵ���С����
	     Collections.sort(lsPeak,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            		Double a0 = Double.parseDouble(arg0[3]);
	            		Double a1 = Double.parseDouble(arg1[3]);
	            		return -a0.compareTo(a1);//�ӵ���С����
	            }});
		/////////////////////////////////////////////////////////////////////////////
	     ArrayList<String[]> lsStepPeak = new ArrayList<String[]>();
	     lsStepPeak.add(lsPeak.get(0));
	     for (int i = 1; i < lsPeak.size(); i++) {
	    	 //ÿstepnum������װ���µ�lsPeak
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
	 * ��lsSearch ȥ����lsPeak����󷵻�lsSearch�ж��ٱ�lsPeak������
	 * @param lsPeak ����ν����
	 * @param lsSearch �����Ź��򣬲����Ǻϲ���Ҳ���Ǽ򵥵ı���peak��������
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
