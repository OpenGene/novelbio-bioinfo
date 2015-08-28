package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.fastq.ExceptionFastq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;

/** 将输入的fastq的list按照prefix整理起来，最后返回map<br>
 * 要么全是双端数据，要么全是单端数据
 * @author zong0jie
 *
 */
public class CopeFastq {

	//以下为输入文件
	/** 排列顺序与lsFastQfileLeft和lsFastQfileRight相同 
	 * 表示分组
	 * */
	List<String> lsCondition = new ArrayList<String>();
	List<String> lsFastQfileLeft = new ArrayList<String>();
	List<String> lsFastQfileRight = new ArrayList<String>();
	/**
	 * 前缀和该前缀所对应的一系列fastq文件。
	 * 如果是单端，则Fastq[]长度为1，如果是双端，则Fastq[]长度为2
	 */
	Map<String, List<String[]>> mapCondition2LsFastQLR = new LinkedHashMap<>();
	/** 前缀和该前缀所对应的一系列fastq文件。
	 * 一个prefix对应两个list，分别是左端fq文件名的list 和 右端fq文件名的list
	 */
	Map<String, List<List<String>>> mapCondition2LslsFastq = new LinkedHashMap<>();
	boolean checkFileIsExist = true;
	
	public void setLsCondition(List<String> lsCondition) {
		this.lsCondition = lsCondition;
	}
	public void setLsFastQfileLeft(List<String> lsFastQfileLeft) {
		if (lsFastQfileLeft == null) {
			return;
		}
		this.lsFastQfileLeft = lsFastQfileLeft;
	}
	public void setLsFastQfileRight(List<String> lsFastQfileRight) {
		if (lsFastQfileRight == null) {
			return;
		}
		this.lsFastQfileRight = lsFastQfileRight;
	}
	/** 是否校验输入的文件，默认为true */
	public void setCheckFileIsExist(boolean checkFileIsExist) {
		this.checkFileIsExist = checkFileIsExist;
	}
	/**
	 * <b>先运行{@link #setMapCondition2LsFastQLR()}</b>
	 * 返回整理好的结果
	 * 前缀和该前缀所对应的一系列fastq文件。
	 * 如果是单端，则Fastq[]长度为1，如果是双端，则Fastq[]长度为2
	 * @return null 表示没有东西
	 */
	public Map<String, List<String[]>> getMapCondition2LsFastQLR() {
		return mapCondition2LsFastQLR;
	}
	/**
	 * <b>先运行{@link #setMapCondition2LsFastQLR()}</b>
	 * 返回整理好的结果
	 * @return null 表示没有东西
	 */
	public Map<String, List<List<String>>> getMapCondition2LslsFastq() {
		return mapCondition2LslsFastq;
	}
	/** 返回去重复后的prefix */
	public List<String> getLsPrefix() {
		return new ArrayList<>(mapCondition2LsFastQLR.keySet());
	}
	/**
	 * 将输入文件整理成
	 * map Prefix--leftList  rightList
	 * 的形式
	 * @return 内部会判定同一类的Fastq文件是否都是双端或都是单端
	 */
	public void setMapCondition2LsFastQLR() {
		mapCondition2LsFastQLR.clear();
		mapCondition2LslsFastq.clear();
		
		for (int i = 0; i < lsCondition.size(); i++) {
			String prefix = lsCondition.get(i);
			List<String[]> lsPrefixFastQsLR = getLsPrefixFastqLR(mapCondition2LsFastQLR, prefix);
			String[] tmpFastQLR = null;
			String fastqL = getFastqFile(lsFastQfileLeft, i);
			String fastqR = getFastqFile(lsFastQfileRight, i);
			setFastqLR(lsPrefixFastQsLR, tmpFastQLR, fastqL, fastqR);
			
			List<List<String>> lsLsFastQLR = getLsFastqLR(mapCondition2LslsFastq, prefix);
			if (FileOperate.isFileExistAndBigThanSize(fastqL, 1)) {
				lsLsFastQLR.get(0).add(fastqL);
			}
			
			if (lsFastQfileRight.size() > i) {
				if (FileOperate.isFileExistAndBigThanSize(fastqR, 1)) {
					lsLsFastQLR.get(1).add(fastqR);
				}
			}
		}
	}
	
	private List<String[]> getLsPrefixFastqLR(Map<String, List<String[]>> mapCondition2LsFastQLR, String prefix) {
		List<String[]> lsPrefixFastQLR = new ArrayList<String[]>();
		if (mapCondition2LsFastQLR.containsKey(prefix)) {
			lsPrefixFastQLR = mapCondition2LsFastQLR.get(prefix);
		} else {
			mapCondition2LsFastQLR.put(prefix, lsPrefixFastQLR);
		}
		return lsPrefixFastQLR;
	}
	
	private List<List<String>> getLsFastqLR(Map<String, List<List<String>>> mapCondition2LslsFastq, String prefix) {
		List<List<String>> lsFastqLR = null;
		if (mapCondition2LslsFastq.containsKey(prefix)) {
			lsFastqLR = mapCondition2LslsFastq.get(prefix);
		}
		else {
			lsFastqLR = new ArrayList<>();
			lsFastqLR.add(new ArrayList<String>());
			lsFastqLR.add(new ArrayList<String>());
			mapCondition2LslsFastq.put(prefix, lsFastqLR);
		}
		return lsFastqLR;
	}
	/**
	 * 主要是怕lsFastqRight可能没东西
	 * @param lsFastq
	 * @param num
	 * @return
	 */
	private String getFastqFile(List<String> lsFastq, int num) {
		if (lsFastq.size() > num) {
			return lsFastq.get(num);
		}
		return null;
	}

	/**
	 * 往list中添加Fastq文件，如果list中为双端数据，则fastqL和fastqR都必须存在。
	 * 如果list为单端，则只能存在fastqL
	 * @param lsPrefixFastQLR 将输入的fastq文件加到该list中
	 * @param tmpFastQLR
	 * @param fastqL
	 * @param fastqR
	 * @return
	 */
	private void setFastqLR(List<String[]> lsPrefixFastQLR, String[] tmpFastQLR, String fastqL, String fastqR) {
		boolean isFqLExist = fileIsExist(checkFileIsExist, fastqL);
		boolean isFqRExist = fileIsExist(checkFileIsExist, fastqR);
		if (isFqLExist && isFqRExist) {
			tmpFastQLR = new String[2];
			tmpFastQLR[0] = fastqL;
			tmpFastQLR[1] = fastqR;
		}
		else if (isFqLExist) {
			tmpFastQLR = new String[1];
			tmpFastQLR[0] = fastqL;
		}
		else if (isFqRExist) {
			tmpFastQLR = new String[1];
			tmpFastQLR[0] = fastqR;
		}
		
		if (lsPrefixFastQLR.size() > 0 && (tmpFastQLR == null ||  lsPrefixFastQLR.get(0).length != tmpFastQLR.length)) {
			throw new ExceptionFastq("prefix number is not equals to fastq file number" +
		"prefix number is:" + lsPrefixFastQLR.size() + "  fastq file number is:" + tmpFastQLR.length);
		}
		lsPrefixFastQLR.add(tmpFastQLR);
	}
	
	private boolean fileIsExist(boolean checkFileIsExist, String fileName) {
		return checkFileIsExist? FileOperate.isFileExistAndBigThanSize(fileName, 1.0)
				: (fileName != null && !fileName.equals(""));
	}
	
	/** 将输入的文件数组转化为FastQ数组 */
	public static FastQ[] convertFastqFile(String[] fastqFile) {
		if (fastqFile == null) return null;
		
		FastQ[] fastQs = new FastQ[fastqFile.length];
		for (int i = 0; i < fastqFile.length; i++) {
			fastQs[i] = new FastQ(fastqFile[i]);
		}
		return fastQs;
	}
	/** 将输入的文件数组转化为FastQ数组 */
	public static List<FastQ> convertFastqFile(List<String> lsFastqFileName) {
		if (lsFastqFileName == null) return null;
		
		List<FastQ> lsFastQs = new ArrayList<>();
		for (String string : lsFastqFileName) {
			lsFastQs.add(new FastQ(string));
		}
		return lsFastQs;
	}
}
