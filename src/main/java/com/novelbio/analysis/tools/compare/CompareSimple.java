package com.novelbio.analysis.tools.compare;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

public class CompareSimple {
	/** 文件1是否有title */
	boolean isHaveTitleFile1 = true;
	/** 文件2是否有title */
	boolean isHaveTitleFile2 = true;
	
	String file1;
	String file2;
	
	String title1;
	String title2;
	
	/** 待比较的列，从0开始计数 */
	List<Integer> lsCompareCols = new ArrayList<>();
	
	LinkedHashMap<String, List<String[]>> mapAccId2LsLineFile1;
	LinkedHashMap<String, List<String[]>> mapAccId2LsLineFile2;
	List<String> lsKeys2 = new ArrayList<>();
	
	/** file1的accId在第几列，从1开始 */
	public void setCompareColNum(int compareColNum) {
		lsCompareCols.clear();
		lsCompareCols.add(compareColNum - 1);
	}
	/** file1的accId在第几列，从1开始 */
	public void setCompareColNum(String compareColStr) {
		List<Integer> lsCompare = CombineTab.getLsIntegers(compareColStr);
		lsCompareCols.clear();
		for (Integer integer : lsCompare) {
			lsCompareCols.add(integer - 1);
		}
	}
	
	private void readFiles() {
		mapAccId2LsLineFile1 = readFile(file1);
		mapAccId2LsLineFile2 = readFile(file2);
		
		if (isHaveTitleFile1) {
			title1 = TxtReadandWrite.readFirstLine(file1);
		}
		if (isHaveTitleFile2) {
			title2 = TxtReadandWrite.readFirstLine(file2);
		}
	}
	
	
	
	/**
	 * 获得取好交集的结果，交集列放在第一列
	 * @return
	 */
	private List<String[]> getLsOverlapInfo() {
		List<String[]> lsInfos = new ArrayList<>();
		for (String key : getLsOverlapIds()) {
			List<String[]> lsFile1Info = removeOverlapCols(mapAccId2LsLineFile1.get(key));
			List<String[]> lsFile2Info = removeOverlapCols(mapAccId2LsLineFile2.get(key));
			String[] colCompare = getCompareColInfo(lsFile1Info.get(0));
			for (String[] info1 : lsFile1Info) {
				for (String[] info2 : lsFile2Info) {
					String resultTmp[] = combineStringArray(colCompare, info1, info2);
					lsInfos.add(resultTmp);
				}
			}
		}
		return lsInfos;
	}
	
	/** 获得overlap的key */
	private Set<String> getLsOverlapIds() {
		Set<String> setOverlapKeys = new LinkedHashSet<>();
		for (String colKey : mapAccId2LsLineFile1.keySet()) {
			if (mapAccId2LsLineFile2.containsKey(colKey)) {
				setOverlapKeys.add(colKey);
			}
		}
		return setOverlapKeys;
	}
	
	private LinkedHashMap<String, List<String[]>> readFile(String file) {
		LinkedHashMap<String, List<String[]>> mapAccId2LsLineFile = new LinkedHashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(file);
		int start = isHaveTitleFile1? 2 : 1;
		for (String content : txtRead.readlines(start)) {
			String[] ss = content.split("\t");
			StringBuilder sBuilder = new StringBuilder();
			for (Integer colNum : lsCompareCols) {
				sBuilder.append(ss[colNum]);
				sBuilder.append(SepSign.SEP_ID);
			}
			String key = sBuilder.toString();
			
			List<String[]> lsLines = mapAccId2LsLineFile.get(key);
			if (lsLines == null) {
				mapAccId2LsLineFile.put(key, lsLines);
			}
			lsLines.add(ss);
		}
		txtRead.close();
		return mapAccId2LsLineFile;
	}
	
	
	
	/** 删除取交集的那几列 */
	private List<String[]> removeOverlapCols(List<String[]> lsInfo) {
		List<String[]> removeOverlap = new ArrayList<>();
		for (String[] strings : lsInfo) {
			String[] info = ArrayOperate.deletElement(strings, lsCompareCols);
			removeOverlap.add(info);
		}
		return removeOverlap;
	}
	
	/** 合并一系列的string数组 */
	private String[] combineStringArray(String[]... info) {
		List<String> lsFinal = new ArrayList<>();
		for (String[] infos : info) {
			for (String infoOne : infos) {
				lsFinal.add(infoOne);
			}
		}
		return lsFinal.toArray(new String[0]);
	}
	
	private String[] getCompareColInfo(String[] infos) {
		String[] compareCol = new String[lsCompareCols.size()];
		int i = 0;
		for (Integer integer : lsCompareCols) {
			compareCol[i++] = infos[integer];
		}
		return compareCol;
	}
}
