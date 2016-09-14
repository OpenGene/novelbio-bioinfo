package com.novelbio.analysis.tools.compare;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 给两个文本取交集，给定第一列，然后取交集
 * @author zong0jie
 * @data 2016-07-25
 */
public class CompareSimple {
	/** 文件1是否有title */
	boolean isHaveTitleFile1 = true;
	/** 文件2是否有title */
	boolean isHaveTitleFile2 = true;
	
	String file1;
	String prefix1 = "";
	String file2;
	String prefix2 = "";
	
	String[] title1;
	String[] title2;
	
	/** 待比较的列，从0开始计数 */
	List<Integer> lsCompareCols = new ArrayList<>();
	List<Integer> lsCompareCols2 = new ArrayList<>();

	LinkedHashMap<String, List<String[]>> mapAccId2LsLineFile1;
	LinkedHashMap<String, List<String[]>> mapAccId2LsLineFile2;
	List<String> lsKeys2 = new ArrayList<>();
	
	
	/**
	 * 是否把比较的那一列提取到第一列
	 * 譬如有时候我们会把A表第三列和B表第四列取交集，那么是否要把A表第三列和B表第四列提取到第一列
	 */
	boolean isExtractColToFirstCol = true;
	
	/**
	 * 是否把比较的那一列提取到第一列
	 * 譬如有时候我们会把A表第三列和B表第四列取交集，那么是否要把A表第三列和B表第四列提取到第一列
	 */
	public void setExtractColToFirstCol(boolean isExtractColToFirstCol) {
		this.isExtractColToFirstCol = isExtractColToFirstCol;
	}
	
	/** file1的accId在第几列，从1开始 */
	public void setCompareColNum(int compareColNum) {
		lsCompareCols.clear();
		lsCompareCols.add(compareColNum - 1);
	}
	/** file1的accId在第几列，从1开始 */
	public void setCompareColNum(List<Integer> lsCompareCols) {
		this.lsCompareCols.clear();
		for (Integer integer : lsCompareCols) {
			this.lsCompareCols.add(integer - 1);
		}
	}
	/** file2的accId在第几列，从1开始 */
	public void setCompareColNum2(int compareColNum) {
		lsCompareCols2.clear();
		lsCompareCols2.add(compareColNum - 1);
	}
	/** file2的accId在第几列，从1开始 */
	public void setCompareColNum2(List<Integer> lsCompareCols2) {
		this.lsCompareCols2.clear();
		for (Integer integer : lsCompareCols2) {
			this.lsCompareCols2.add(integer - 1);
		}
	}
	/** file1的accId在第几列，从1开始 */
	public void setCompareColNum(String compareColStr) {
		List<Integer> lsCompare = CombineTab.getLsIntegers(compareColStr);
		lsCompareCols.clear();
		for (Integer integer : lsCompare) {
			lsCompareCols.add(integer - 1);
		}
	}
	
	/**
	 * @param file1
	 * prefix为文件名，会在取好交集后在每一列的title上加上 prefix_
	 */
	public void setFile1(String file1) {
		this.file1 = file1;
		this.prefix1 = FileOperate.getFileNameSep(file1)[0] + "_";
	}
	/**
	 * @param file2
	 * prefix为文件名，会在取好交集后在每一列的title上加上 prefix_
	 */
	public void setFile2(String file2) {
		this.file2 = file2;
		this.prefix2 = FileOperate.getFileNameSep(file2)[0] + "_";
	}
	
	/**
	 * @param file1
	 * @param prefix1 前缀，会在取好交集后在每一列的title上加上 prefix_
	 */
	public void setFile1(String file1, String prefix1) {
		this.file1 = file1;
		if (!StringOperate.isRealNull(prefix1)) {
			this.prefix1 = prefix1 + "_";
		}
	}
	/**
	 * @param file2
	 * @param prefix1 前缀，会在取好交集后在每一列的title上加上 prefix_
	 */
	public void setFile2(String file2, String prefix2) {
		this.file2 = file2;
		if (!StringOperate.isRealNull(prefix2)) {
			this.prefix2 = prefix2 + "_";
		}
	}
	
	public void readFiles() {
		if (lsCompareCols2.isEmpty()) {
			lsCompareCols2 = lsCompareCols;
		}
		Path path1 = FileOperate.getPath(file1);
		Path path2 = FileOperate.getPath(file2);
		mapAccId2LsLineFile1 = readFile(path1, lsCompareCols);
		mapAccId2LsLineFile2 = readFile(path2, lsCompareCols2);
		
		if (isHaveTitleFile1) {
			String title1Str = TxtReadandWrite.readFirstLine(path1);
			this.title1 = modifyTitle(mapAccId2LsLineFile1.values().iterator().next().get(0), title1Str);
		}
		if (isHaveTitleFile2) {
			String title2Str = TxtReadandWrite.readFirstLine(path2);
			this.title2 = modifyTitle(mapAccId2LsLineFile2.values().iterator().next().get(0), title2Str);
		}
	}
	
	public List<String[]> getLsOverlapInfoWithTitle() {
		String[] colCompare = isExtractColToFirstCol ? getCompareColInfo(title1, lsCompareCols) : new String[0];
		String[] title1Sub = isExtractColToFirstCol ? ArrayOperate.deletElement(title1, lsCompareCols) : title1;
		String[] title2Sub = ArrayOperate.deletElement(title2, lsCompareCols2);
		for (int i = 0; i < title1Sub.length; i++) {
			title1Sub[i] = prefix1 + title1Sub[i];
		}
		for (int i = 0; i < title2Sub.length; i++) {
			title2Sub[i] = prefix2 + title2Sub[i];
		}
		String resultTitle[] = combineStringArray(colCompare, title1Sub, title2Sub);
		List<String[]> lsResult = getLsOverlapInfoWithoutTitle();
		lsResult.add(0, resultTitle);
		return lsResult;
	}
	
	/**
	 * 获得取好交集的结果，交集列放在第一列
	 * @return
	 */
	public List<String[]> getLsOverlapInfoWithoutTitle() {
		List<String[]> lsInfos = new ArrayList<>();
		for (String key : getLsOverlapIds()) {
			List<String[]> lsFile1Info = mapAccId2LsLineFile1.get(key);
			List<String[]> lsFile2Info = mapAccId2LsLineFile2.get(key);
			String[] colCompare = getCompareColInfo(lsFile1Info.get(0), lsCompareCols);
			if (isExtractColToFirstCol) {
				lsFile1Info = removeOverlapCols(lsFile1Info, lsCompareCols);
			}
			lsFile2Info = removeOverlapCols(lsFile2Info, lsCompareCols2);

			for (String[] info1 : lsFile1Info) {
				for (String[] info2 : lsFile2Info) {
					String resultTmp[] = isExtractColToFirstCol ?  combineStringArray(colCompare, info1, info2) : combineStringArray(info1, info2);
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
	
	private LinkedHashMap<String, List<String[]>> readFile(Path file, List<Integer> lsCompareCols) {
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
				lsLines = new ArrayList<>();
				mapAccId2LsLineFile.put(key, lsLines);
			}
			lsLines.add(ss);
		}
		txtRead.close();
		return mapAccId2LsLineFile;
	}
	
	/** 删除取交集的那几列 */
	private static List<String[]> removeOverlapCols(List<String[]> lsInfo, List<Integer> lsCompareCols) {
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
	
	private String[] getCompareColInfo(String[] infos, List<Integer> lsCompareCols) {
		String[] compareCol = new String[lsCompareCols.size()];
		int i = 0;
		for (Integer integer : lsCompareCols) {
			compareCol[i++] = infos[integer];
		}
		return compareCol;
	}
	
	/** 把title修改为跟内容一致的长度 */
	private static String[] modifyTitle(String[] info, String title) {
		String[] ss = title.split("\t");
		List<String> lsTtileFinal = new ArrayList<>();
		for (int i = 0; i < info.length; i++) {
			if (i < ss.length) {
				lsTtileFinal.add(ss[i]);
			} else {
				lsTtileFinal.add("");
			}
		}
		return lsTtileFinal.toArray(new String[0]);
	}
}
