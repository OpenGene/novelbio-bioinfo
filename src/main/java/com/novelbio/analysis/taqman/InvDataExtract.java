package com.novelbio.analysis.taqman;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class InvDataExtract {
	/**
	 * ����CTΪ�õ��ʵľͼ�Ϊû�м�������һ�����泬��filterProp�����ĵ�CT���и����֣���ɾ����miRNA<br>
	 * ��ʱ�趨Ϊ Undetermined
	 */
	static String flagFilter = "Undetermined";
	/** CT */
	static String flagCT = "Ct";
	/** average delta ct */
	static String flagAvgCT = "Avg Delta Ct";
	
	
	public static void main(String[] args) {
		InvDataExtract invDataExtract = new InvDataExtract();
		String parentPath = "/media/winE/NBC/Project/TaqMan_CXD_111230/";
		String excelTaqman = parentPath + "Human MicroRNA Array CTֵͳ�Ʊ���.xls";
		String out = parentPath + "deltaCT.txt";
		invDataExtract.loadExcelTxt(excelTaqman);
		invDataExtract.getInfo(out);
	}
	
	/** ���һ��miRNA�г���0.6��û�б��������ô��ɾ����miRNA */
	double filterProp = 0.6;
	int titleRow = 1;
	int startRow = 2;
	ArrayList<String[]> lsInuptData;
	
	public void loadExcelTxt(String excelTxtFile) {
		lsInuptData = ExcelTxtRead.readLsExcelTxt(excelTxtFile, 1);
	}
	/**
	 * ����Ӣ����taqman̽���ļ�����CT��ȡ����
	 * ����CTΪ Undetermined���ʵľͼ�Ϊû�м�������һ�����泬��filterProp�����ĵ�CT���и����֣���ɾ����miRNA<br>
	 * @param excelFile
	 * @param OutData
	 * @param startRow
	 */
	public void getInfo(String OutData) {
		ArrayList<Integer> lsCTcol = getCT_AvgCT_col(lsInuptData.get(titleRow), flagCT);
		ArrayList<Integer> lsDeltaCTcol = getCT_AvgCT_col(lsInuptData.get(titleRow), flagAvgCT);
		ArrayList<String[]> lsResult = filterData(lsCTcol, lsDeltaCTcol);
		TxtReadandWrite txtOut = new TxtReadandWrite(OutData, true);
		txtOut.ExcelWrite(lsResult);
	}
	/**
	 * ����Invitrogen��tapman�ļ��õ���list�����г���ɸѡ������CT����Ļ��� <br>
	 * @param lsData ��ȡ�õ�������list
	 * @param startRow �ӵڼ��п�ʼ����ʵ����
	 * @param lsCTcol ��ȡ�ļ��У�Ҳ����CT��
	 */
	private ArrayList<String[]> filterData(List<Integer> lsCTcol, List<Integer> lsDeltaCTcol) {
		int filterNum = (int)(filterProp * lsDeltaCTcol.size()+0.5); 
		startRow-- ; 
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		/**
		 * ����ȥ�����õģ�ͬ���ı���������_1���
		 */
		HashSet<String> mapRowName = new HashSet<String>();
		
		for (int i = startRow; i < lsInuptData.size(); i ++) {
			String[] tmpValue = lsInuptData.get(i);
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
				if (! mapRowName.contains(tmpName2)) {
					mapRowName.add(tmpName2);
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
	 * ָ�������У����������� ָ��string  ���е�num <br>
	 * ����������Array Card Name \t Assay \t Task \t Ct \t Avg Delta Ct \t Ct \t Avg Delta Ct \t Ct	Avg Delta Ct \t Ct \t Avg Delta Ct \t Ct \t Avg Delta Ct <br>
	 * ���һ����û�ҵ�������null <br>
	 * @param strTitle ������
	 * @param flagStr ��equal���ַ�����������Ƿ���,����Ϊ Ct ���� Avg Delta Ct
	 * @return
	 */
	private ArrayList<Integer> getCT_AvgCT_col(String[] strTitle, String flagStr) {
		/**
		 * ����CT����
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
