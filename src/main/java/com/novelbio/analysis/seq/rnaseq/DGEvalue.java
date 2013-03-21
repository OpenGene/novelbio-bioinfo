package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 输入的文件必须经过排序
 * 不过暂时没用到
 * @author zong0jie
 *
 */
public class DGEvalue implements AlignmentRecorder  {
	
	/** 样本名 */
	List<String> lsSample = new ArrayList<String>();
	/**
	 * 每个样本中
	 * 每个基因的表达值
	 */
	List<Map<String, Integer>> ls_MapAccID2Value = new ArrayList<Map<String,Integer>>();
	
	//中间变量
	Map<String, Integer> mapAccID2Value;
	List<int[]> lsTmpExpValue;
	AlignRecord alignRecordLast;
	int[] tmpCount;
	
	/**
	 * DGE会mapping至多个位置，是选择基因的最高堆叠reads还是总reads数呢
	 * 因为DGE一般是一个基因只有ployA这一个位置有标签，所以我们一般选择最高位置作为其表达量
	 */
	boolean allTags = false;
	
	/**
	 * 默认是false
	 * @param allTags
	 */
	public void setAllTags(boolean allTags) {
		this.allTags = allTags;
	}
	
	public void setCurrentSample(String sampleName) {
		mapAccID2Value = new HashMap<String, Integer>();
		ls_MapAccID2Value.add(mapAccID2Value);
		lsSample.add(sampleName);

		lsTmpExpValue = new ArrayList<int[]>();
		alignRecordLast = null;
		tmpCount = new int[] {0};
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {		
		// mapping到互补链上的，是假的信号
		if (!alignRecord.isCis5to3()) {
			return;
		}
		if (alignRecordLast != null && !alignRecordLast.getRefID().equals(alignRecord.getRefID())) {
			if (allTags) {
				mapAccID2Value.put(alignRecordLast.getRefID(), sum(lsTmpExpValue));
			} else {
				mapAccID2Value.put(alignRecordLast.getRefID(), max(lsTmpExpValue));
			}
			lsTmpExpValue.clear();
			tmpCount = new int[] { 0 };
		} else if (alignRecordLast == null || alignRecord.getStartAbs() > alignRecordLast.getEndAbs()) {
			tmpCount = new int[] { 0 };
			lsTmpExpValue.add(tmpCount);
		}
		tmpCount[0]++;
		alignRecordLast = alignRecord;
	}

	@Override
	public void summary() {
		if (allTags) {
			mapAccID2Value.put(alignRecordLast.getRefID(), sum(lsTmpExpValue));
		} else {
			mapAccID2Value.put(alignRecordLast.getRefID(), max(lsTmpExpValue));
		}
	}

	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private static int max(List<int[]> lsReads) {
		int max = lsReads.get(0)[0];
		for (int[] is : lsReads) {
			if (is[0] > max) {
				max = is[0];
			}
		}
		return max;
	}

	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private static int sum(List<int[]> lsReads) {
		int sum = 0;
		for (int[] is : lsReads) {
			sum = sum + is[0];
		}
		return sum;
	}
	
	/**
	 * 将bed文件转化成DGE所需的信息
	 * @param result
	 * @param bedFile
	 */
	public Map<String, List<Integer>> getDGEvalue() {
		Map<String, List<Integer>> mapResult = combineHashDGEvalue(ls_MapAccID2Value);
		return mapResult;
	}
	
	/**
	 * 将bed文件转化成DGE所需的信息，可以写入文本
	 * @param result
	 * @param sort 
	 * @param allTags 是否获得全部的正向tag，false的话，只选择最多的正向tag的数量
	 * @param bedFile
	 */
	public ArrayList<String[]> getDGEvalueTab() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsTitle = getLsSample();
		lsTitle.add(0, TitleFormatNBC.AccID.toString());
		lsResult.add(lsTitle.toArray(new String[0]));
		
		Map<String, List<Integer>> mapResult = combineHashDGEvalue(ls_MapAccID2Value);
		for (String geneName : mapResult.keySet()) {
			List<Integer> lsInfo = mapResult.get(geneName);
			String[] tmpResult = new String[lsInfo.size() + 1];
			tmpResult[0] = geneName;
			for (int i = 0; i < lsInfo.size(); i++) {
				tmpResult[i+1] = lsInfo.get(i) + "";
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	
	/** 实验的样本 */
	public List<String> getLsSample() {
		return lsSample;
	}
	
	/**
	 * 给定一组hash表，key：locID   value：expressValue
	 * 将他们合并成一个表
	 * @param lsDGEvalue
	 * @return
	 */
	private static Map<String, List<Integer>> combineHashDGEvalue(List<Map<String, Integer>> lsDGEvalue) {
		Map<String, List<Integer>> mapGeneID2LsValue = new HashMap<String, List<Integer>>();
		for (Map<String, Integer> mapGeneID2Value : lsDGEvalue) {
			
			for (Entry<String, Integer> entry : mapGeneID2Value.entrySet()) {
				String loc = entry.getKey(); int value = entry.getValue();
				
				if (mapGeneID2LsValue.containsKey(loc)) {
					List<Integer> lsValue = mapGeneID2LsValue.get(loc);
					lsValue.add(value);
				} else {
					List<Integer> lsValue = new ArrayList<Integer>();
					lsValue.add(value);
					mapGeneID2LsValue.put(loc, lsValue);
				}
			}
		}
		return mapGeneID2LsValue;
	}
	
	public void clear() {
		lsSample = new ArrayList<String>();
		ls_MapAccID2Value = new ArrayList<Map<String,Integer>>();
		allTags = false;
	}
}
