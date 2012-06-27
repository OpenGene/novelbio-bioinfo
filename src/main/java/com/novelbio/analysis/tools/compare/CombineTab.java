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
 * 选定多个表，
 * 每个表中选定几列需要选择的列
 * 最后将这些表中的几列合并到一个table中，合并的ID为选中的ID列
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
	/** ColCompareComb：将待查找的列合并起来，用"_"连接<br>
	 * ColCompareSep：分开的待查找的列
	 * */
	LinkedHashMap<String, String[]> mapColCompareComb_To_ColCompareSep = new LinkedHashMap<String,String[]>();
	
	HashMap<String, LinkedHashMap<String, String[]>> mapFileName_To_ColCompareComb2ExtractCol = new LinkedHashMap<String, LinkedHashMap<String,String[]>>();
	/**
	 * 文件名---具体要包含哪几列，不含比较列
	 */
	LinkedHashMap<String, int[]> mapFileName2ExtractColNum = new LinkedHashMap<String, int[]>();

	/** 需要比较那几列 */
	int[] colCompareOverlapID;
	/** 并集里面的空格填充什么 */
	String strNull = "";
	
	ArrayList<String[]> lsResultUnion = new ArrayList<String[]>();
	ArrayList<String[]> lsResultIntersection = new ArrayList<String[]>();
	boolean runningFlag = true;
	/**
	 * 空格用什么字符串填充，默认为"";
	 * @param strNull
	 */
	public void setStrNull(String strNull) {
		this.strNull = strNull;
	}
	/**
	 * 待取交集的ID列
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
		//先排个序
		Collections.sort(lsColID);
		colCompareOverlapID = new int[lsColID.size()];
		for (int i = 0; i < colCompareOverlapID.length; i++) {
			colCompareOverlapID[i] = lsColID.get(i)-1;
		}
		runningFlag = false;
	}

	/**
	 * 获得每个文件名, 对于每个文件，设定它的ID列
	 * @param condTxt 文本名
	 * @param codName 该文本的简称
	 * @param colDetail 该文本具体获取哪几列
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
	 *  获得每个文件名, 对于每个文件，设定它的ID列
	 *  那么文件的简称由文本名生成
	 * @param condTxt 文本名
	 * @param colDetai 该文本具体获取哪几列
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
	 * 取并集
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
	 * 读取指定文本的信息
	 * 包含标题列
	 * @param cond
	 * @return
	 * 获得的结果已经按照输入的colID顺序经过排序了
	 */
	private ArrayList<String[]> getFileInfoAllCols(String readFile) {
		int[] colExtract = mapFileName2ExtractColNum.get(readFile);
		int[] colReadFromFile = new int[colCompareOverlapID.length + colExtract.length];
		//合并列
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
	 * 设定唯一列的信息，然后将具体的信息装入具体的hash表中
	 * @param lsTmpInfo 具体的list信息，包括flag列
	 * @param colIDLen 头几行是colID
	 * 自动去冗余，保留第一次出现的ID
	 */
	private void set_MapCompareComb_And_MapFileNamel(String fileName, List<String[]> lsTmpInfo) {
		//本表的colID2colDetail信息
		LinkedHashMap<String, String[]> mapColCompareID2ExtractInfo = new LinkedHashMap<String, String[]>();
		mapFileName_To_ColCompareComb2ExtractCol.put(fileName, mapColCompareID2ExtractInfo);
		if (colCompareOverlapID.length > lsTmpInfo.get(0).length) {
			logger.error("输入列名长度有问题");
		}
		for (String[] strings : lsTmpInfo) {
			String colIDcombineStr = ""; String[] colIDarray = new String[colCompareOverlapID.length];
			//flag列的信息
			for (int i = 0; i < colCompareOverlapID.length; i++) {
				colIDcombineStr = colIDcombineStr + SepSign.SEP_ID + strings[i];
				colIDarray[i] = strings[i];
			}
			//删除flag列的信息
			String[] tmpExtractColInfo = new String[strings.length - colCompareOverlapID.length];
			for (int i = colCompareOverlapID.length; i < strings.length; i++) {
				tmpExtractColInfo[i - colCompareOverlapID.length] = strings[i];
			}
			//已经有了就跳过
			if (mapColCompareID2ExtractInfo.containsKey(colIDcombineStr)) {
				continue;
			}
			mapColCompareID2ExtractInfo.put(colIDcombineStr, tmpExtractColInfo);
			//不重复的所有ID，为取并集做准备
			mapColCompareComb_To_ColCompareSep.put(colIDcombineStr,colIDarray);
		}
	}
	/**
	 * 获得取并集的结果
	 * @return
	 */
	private void combInfo() {
		lsResultUnion = new ArrayList<String[]>();
		lsResultIntersection = new ArrayList<String[]>();
		
		for (String colCompareComb : mapColCompareComb_To_ColCompareSep.keySet()) {
			String[] colCompareSep = mapColCompareComb_To_ColCompareSep.get(colCompareComb);
			boolean flagInterSection = true;
			//每个ID在所有多个表中全部查找一遍
			for (String fileName : mapFileName2ConditionAbbr.keySet()) {
				LinkedHashMap<String, String[]> mapColCompareComb2ExtractCol = mapFileName_To_ColCompareComb2ExtractCol.get(fileName);
				String[] extractCol = mapColCompareComb2ExtractCol.get(colCompareComb);
				//没找到，就用空格替换
				if (extractCol == null) {
					flagInterSection = false;
					extractCol = new String[mapFileName2ExtractColNum.get(fileName).length];
					for (int i = 0; i < extractCol.length; i++) {
						extractCol[i] = strNull;
					}
				}
				//合并列
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
