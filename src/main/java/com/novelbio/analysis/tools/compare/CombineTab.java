package com.novelbio.analysis.tools.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * ѡ�������
 * ÿ������ѡ��������Ҫѡ�����
 * �����Щ���еļ��кϲ���һ��table�У��ϲ���IDΪѡ�е�ID��
 * @author zong0jie
 */
public class CombineTab {
	
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/peakcalling/";
		String file1 = parentFile + "K4all_SE-W200-G600-E100_anno_-2k+2k.xls";
		String file2 = parentFile + "KEall_SE-W200-G600-E100_anno_-2k+2k.xls";
		String file3 = parentFile + "W4all_SE-W200-G600-E100_anno_-2k+2k.xls";
		String file4 = parentFile + "WEall_SE-W200-G600-E100_anno_-2k+2k.xls";
		CombineTab comb = new CombineTab();
		comb.setColExtractDetai(file1, "K4", 4,6,7);
		comb.setColExtractDetai(file2, "KE", 4,6,7);
		comb.setColExtractDetai(file3, "W4", 4,6,7);
		comb.setColExtractDetai(file4, "WE", 4,6,7);
		comb.setColCompareOverlapID(5);
	}
	private static Logger logger = Logger.getLogger(CombineTab.class);
	
	LinkedHashMap<String, String> mapFileName2ConditionAbbr = new LinkedHashMap<String, String>();
	/** ColCompareComb���������ҵ��кϲ���������"_"����<br>
	 * ColCompareSep���ֿ��Ĵ����ҵ���
	 * */
	LinkedHashMap<String, String[]> mapColCompareComb_To_ColCompareSep = new LinkedHashMap<String,String[]>();
	
	HashMap<String, LinkedHashMap<String, String[]>> mapFileName_To_ColCompareComb2ExtractCol = new LinkedHashMap<String, LinkedHashMap<String,String[]>>();
	/**
	 * �ļ���---����Ҫ�����ļ��У������Ƚ���
	 */
	LinkedHashMap<String, int[]> mapFileName2ExtractColNum = new LinkedHashMap<String, int[]>();

	/** ��Ҫ�Ƚ��Ǽ��� */
	int[] colCompareOverlapID;
	/** ��������Ŀո����ʲô */
	String strNull = "";
	
	ArrayList<String[]> lsResultUnion = new ArrayList<String[]>();
	ArrayList<String[]> lsResultIntersection = new ArrayList<String[]>();
	boolean runningFlag = true;
	/**
	 * �ո���ʲô�ַ�����䣬Ĭ��Ϊ"";
	 * @param strNull
	 */
	public void setStrNull(String strNull) {
		this.strNull = strNull;
	}
	/**
	 * ��ȡ������ID��
	 * @param colID
	 */
	public void setColCompareOverlapID(int... colID) {
		for (int i = 0; i < colID.length; i++) {
			colID[i] = colID[i] - 1;
		}
		this.colCompareOverlapID = colID;
		runningFlag = false;
	}
	
	public void setColCompareOverlapID(ArrayList<Integer> lsColID) {
		//���Ÿ���
		Collections.sort(lsColID);
		colCompareOverlapID = new int[lsColID.size()];
		for (int i = 0; i < colCompareOverlapID.length; i++) {
			colCompareOverlapID[i] = lsColID.get(i)-1;
		}
		runningFlag = false;
	}

	/**
	 * ���ÿ���ļ���, ����ÿ���ļ����趨����ID��
	 * @param condTxt �ı���
	 * @param codName ���ı��ļ��
	 * @param colDetail ���ı������ȡ�ļ���
	 */
	public void setColExtractDetai(String condTxt, String codName, int... colDetail) {
		for (int i = 0; i < colDetail.length; i++) {
			colDetail[i] = colDetail[i] - 1;
		}
		mapFileName2ExtractColNum.put(condTxt, colDetail);
		mapFileName2ConditionAbbr.put(condTxt,codName);
		runningFlag = false;
	}
	/**
	 * 
	 *  ���ÿ���ļ���, ����ÿ���ļ����趨����ID��
	 *  ��ô�ļ��ļ�����ı�������
	 * @param condTxt �ı���
	 * @param colDetai ���ı������ȡ�ļ���
	 */
	@Deprecated
	public void setColDetai(String condTxt,int... colDetai) {
		for (int i = 0; i < colDetai.length; i++) {
			colDetai[i] = colDetai[i] - 1;
		}
		mapFileName2ExtractColNum.put(condTxt, colDetai);
		mapFileName2ConditionAbbr.put(condTxt,FileOperate.getFileNameSep(condTxt)[0]);
		runningFlag = false;
	}
 
	/**
	 * ȡ����
	 * @return
	 */
	private void exeToFile() {
		if (runningFlag && lsResultUnion.size() > 0) {
			return;
		}
		String title[] = new String[0];
		for (Entry<String, String> entry : mapFileName2ConditionAbbr.entrySet()) {
			String filename = entry.getKey();
			String conditionAbbr = entry.getValue();
			ArrayList<String[]> lsInfoCodAllCols = getFileInfoAllCols(filename);
			if (title.length == 0) {
				title = new String[colCompareOverlapID.length];
				for (int i = 0; i < title.length; i++) {
					title[i] = lsInfoCodAllCols.get(0)[i];
				}
			}
			String[] subTitle = new String[mapFileName2ExtractColNum.get(filename).length];
			for (int i = 0; i < subTitle.length; i++) {
				subTitle[i] = lsInfoCodAllCols.get(0)[i + colCompareOverlapID.length] + "_" + conditionAbbr;
			}
			title = ArrayOperate.combArray(title, subTitle, 0);
			set_MapCompareComb_And_MapFileNamel(filename, lsInfoCodAllCols.subList(1, lsInfoCodAllCols.size()));
		}
		combInfo();
		lsResultUnion.add(0,title);
		lsResultIntersection.add(0, title);
		runningFlag = true;
	}
	
	
	/**
	 * ��ȡָ���ı�����Ϣ
	 * ����������
	 * @param cond
	 * @return
	 * ��õĽ���Ѿ����������colID˳�򾭹�������
	 */
	private ArrayList<String[]> getFileInfoAllCols(String readFile) {
		int[] colExtract = mapFileName2ExtractColNum.get(readFile);
		int[] colReadFromFile = new int[colCompareOverlapID.length + colExtract.length];
		//�ϲ���
		for (int i = 0; i < colCompareOverlapID.length; i++) {
			colReadFromFile[i] = colCompareOverlapID[i] + 1;
		}
		for (int i = 0; i < colExtract.length; i++) {
			colReadFromFile[colCompareOverlapID.length+i] = colExtract[i] + 1;
		}
		
		ArrayList<String[]> lsTmpInfo = ExcelTxtRead.readLsExcelTxt(readFile, colReadFromFile, 1, -1, true);
		return lsTmpInfo;
	}
	
	/**
	 * �趨Ψһ�е���Ϣ��Ȼ�󽫾������Ϣװ������hash����
	 * @param lsTmpInfo �����list��Ϣ������flag��
	 * @param colIDLen ͷ������colID
	 * �Զ�ȥ���࣬������һ�γ��ֵ�ID
	 */
	private void set_MapCompareComb_And_MapFileNamel(String fileName, List<String[]> lsTmpInfo) {
		//�����colID2colDetail��Ϣ
		LinkedHashMap<String, String[]> mapColCompareID2ExtractInfo = new LinkedHashMap<String, String[]>();
		mapFileName_To_ColCompareComb2ExtractCol.put(fileName, mapColCompareID2ExtractInfo);
		if (colCompareOverlapID.length > lsTmpInfo.get(0).length) {
			logger.error("������������������");
		}
		for (String[] strings : lsTmpInfo) {
			String colIDcombineStr = ""; String[] colIDarray = new String[colCompareOverlapID.length];
			//flag�е���Ϣ
			for (int i = 0; i < colCompareOverlapID.length; i++) {
				colIDcombineStr = colIDcombineStr + SepSign.SEP_ID + strings[i];
				colIDarray[i] = strings[i];
			}
			//ɾ��flag�е���Ϣ
			String[] tmpExtractColInfo = new String[strings.length - colCompareOverlapID.length];
			for (int i = colCompareOverlapID.length; i < strings.length; i++) {
				tmpExtractColInfo[i - colCompareOverlapID.length] = strings[i];
			}
			//�Ѿ����˾�����
			if (mapColCompareID2ExtractInfo.containsKey(colIDcombineStr)) {
				continue;
			}
			mapColCompareID2ExtractInfo.put(colIDcombineStr, tmpExtractColInfo);
			//���ظ�������ID��Ϊȡ������׼��
			mapColCompareComb_To_ColCompareSep.put(colIDcombineStr,colIDarray);
		}
	}
	/**
	 * ���ȡ�����Ľ��
	 * @return
	 */
	private void combInfo() {
		lsResultUnion = new ArrayList<String[]>();
		lsResultIntersection = new ArrayList<String[]>();
		
		for (String colCompareComb : mapColCompareComb_To_ColCompareSep.keySet()) {
			String[] colCompareSep = mapColCompareComb_To_ColCompareSep.get(colCompareComb);
			boolean flagInterSection = true;
			//ÿ��ID�����ж������ȫ������һ��
			for (String fileName : mapFileName2ConditionAbbr.keySet()) {
				LinkedHashMap<String, String[]> mapColCompareComb2ExtractCol = mapFileName_To_ColCompareComb2ExtractCol.get(fileName);
				String[] extractCol = mapColCompareComb2ExtractCol.get(colCompareComb);
				//û�ҵ������ÿո��滻
				if (extractCol == null) {
					flagInterSection = false;
					extractCol = new String[mapFileName2ExtractColNum.get(fileName).length];
					for (int i = 0; i < extractCol.length; i++) {
						extractCol[i] = strNull;
					}
				}
				//�ϲ���
				colCompareSep = ArrayOperate.combArray(colCompareSep, extractCol, 0);
			}
			lsResultUnion.add(colCompareSep);
			if (flagInterSection) {
				lsResultIntersection.add(colCompareSep);
			}
		}
	}
	
	public ArrayList<String[]> getResultLsIntersection() {
		exeToFile();
		return lsResultIntersection;
	}
	public ArrayList<String[]> getResultLsUnion() {
		exeToFile();
		return lsResultUnion;
	}
}
