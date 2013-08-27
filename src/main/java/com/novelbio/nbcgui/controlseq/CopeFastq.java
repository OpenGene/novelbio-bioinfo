package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;

/** 将输入的fastq的list按照prefix整理起来，最后返回map */
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
	Map<String, List<FastQ[]>> mapCondition2LsFastQLR = new LinkedHashMap<String, List<FastQ[]>>();
	
	public void setLsCondition(List<String> lsCondition) {
		this.lsCondition = lsCondition;
	}
	public void setLsFastQfileLeft(List<String> lsFastQfileLeft) {
		this.lsFastQfileLeft = lsFastQfileLeft;
	}
	public void setLsFastQfileRight(List<String> lsFastQfileRight) {
		this.lsFastQfileRight = lsFastQfileRight;
	}
	/**
	 * <b>先运行{@link #setMapCondition2LsFastQLR()}</b>
	 * 返回整理好的结果
	 * @return null 表示没有东西
	 */
	public Map<String, List<FastQ[]>> getMapCondition2LsFastQLR() {
		return mapCondition2LsFastQLR;
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
	public boolean setMapCondition2LsFastQLR() {
		mapCondition2LsFastQLR.clear();
		for (int i = 0; i < lsCondition.size(); i++) {
			String prefix = lsCondition.get(i);
			List<FastQ[]> lsPrefixFastQLR = new ArrayList<FastQ[]>();
			if (mapCondition2LsFastQLR.containsKey(prefix)) {
				lsPrefixFastQLR = mapCondition2LsFastQLR.get(prefix);
			} else {
				mapCondition2LsFastQLR.put(prefix, lsPrefixFastQLR);
			}
			FastQ[] tmpFastQLR = null;
			String fastqL = getFastqFile(lsFastQfileLeft, i);
			String fastqR = getFastqFile(lsFastQfileRight, i);
			if (!setFastqLR(lsPrefixFastQLR, tmpFastQLR, fastqL, fastqR)) {
				return false;
			}
		}
		return true;
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
	private boolean setFastqLR(List<FastQ[]> lsPrefixFastQLR, FastQ[] tmpFastQLR, String fastqL, String fastqR) {
		if (FileOperate.isFileExistAndBigThanSize(fastqL, 1) && FileOperate.isFileExistAndBigThanSize(fastqR, 1)) {
			tmpFastQLR = new FastQ[2];
			tmpFastQLR[0] = new FastQ(fastqL);
			tmpFastQLR[1] = new FastQ(fastqR);;
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqL, 1)) {
			tmpFastQLR = new FastQ[1];
			tmpFastQLR[0] = new FastQ(fastqL);
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqR, 1)) {
			tmpFastQLR = new FastQ[1];
			tmpFastQLR[0] = new FastQ(fastqR);
		}
		if (lsPrefixFastQLR.size() > 0 && lsPrefixFastQLR.get(0).length != tmpFastQLR.length) {
			return false;
		}
		lsPrefixFastQLR.add(tmpFastQLR);
		return true;
	}
	

}
