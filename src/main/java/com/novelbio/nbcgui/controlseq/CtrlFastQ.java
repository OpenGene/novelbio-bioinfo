package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQC;
import com.novelbio.analysis.seq.fastq.FastQReadingChannel;
import com.novelbio.analysis.seq.fastq.FastQRecordFilter;
import com.novelbio.base.fileOperate.FileOperate;

@Component
@Scope("prototype")
public class CtrlFastQ {
	private static Logger logger = Logger.getLogger(CtrlFastQ.class);
	
	boolean filter = true;
	boolean trimNNN = false;
	int fastqQuality = FastQ.QUALITY_MIDIAN;
	int readsLenMin = 18;
	String adaptorLeft = "";
	String adaptorRight = "";
	boolean adaptorLowercase =false;
	
	//以下为输入文件
	/** 排列顺序与lsFastQfileLeft和lsFastQfileRight相同 
	 * 表示分组
	 * */
	ArrayList<String> lsCondition = new ArrayList<String>();
	ArrayList<String> lsFastQfileLeft = new ArrayList<String>();
	ArrayList<String> lsFastQfileRight = new ArrayList<String>();
	
	String outFilePrefix = "";
	
	/**
	 * 前缀和该前缀所对应的一系列fastq文件。
	 * 如果是单端，则Fastq[]长度为1，如果是双端，则Fastq[]长度为2
	 */
	Map<String, List<FastQ[]>> mapCondition2LsFastQLR = new LinkedHashMap<String, List<FastQ[]>>();
	/** 过滤好的结果 */
	Map<String, List<FastQ[]>> mapCondition2LRFiltered = new LinkedHashMap<String, List<FastQ[]>>();
		
	/** 过滤前质控 */
	Map<String, FastQC[]> mapCond2FastQCBefore;
	/** 过滤前是否要QC，不要就只计数 */
	boolean qcBefore = true;
	/** 过滤后质控 */
	Map<String, FastQC[]> mapCond2FastQCAfter;
	/** 过滤后是否要QC，不要就只计数 */
	boolean qcAfter = true;
	
	public void setAdaptorLeft(String adaptorLeft) {
		this.adaptorLeft = adaptorLeft;
	}
	public void setAdaptorLowercase(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	public void setAdaptorRight(String adaptorRight) {
		this.adaptorRight = adaptorRight;
	}
	public void setFastqQuality(int fastqQuality) {
		this.fastqQuality = fastqQuality;
	}
	/** 是否过滤，如果不过滤则直接合并 */
	public void setFilter(boolean filter) {
		this.filter = filter;
	}
	public void setReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	
	public void setOutFilePrefix(String outFilePrefix) {
		this.outFilePrefix = outFilePrefix;
	}
	
	public String getOutFilePrefix() {
		return outFilePrefix;
	}
	
	public void setFastQC(boolean QCbeforeFilter, boolean QCafterFilter) {
		this.qcBefore = QCbeforeFilter;
		this.qcAfter = QCafterFilter;
	}
	
	public boolean isQcBefore() {
		return qcBefore;
	}
	public boolean isQcAfter() {
		return qcAfter;
	}
	public Map<String, FastQC[]> getMapCond2FastQCBefore() {
		return mapCond2FastQCBefore;
	}
	public Map<String, FastQC[]> getMapCond2FastQCAfter() {
		return mapCond2FastQCAfter;
	}
	
	/**
	 * arraylist - string[3]: <br>
	 * 0: fastqFile <br>
	 * 1: prefix <br>
	 * 2: group
	 */
	public void setLsFastQfileLeft(ArrayList<String> lsFastQfileLeft) {
		this.lsFastQfileLeft = lsFastQfileLeft;
	}
	public void setLsFastQfileRight(ArrayList<String> lsFastQfileRight) {
		this.lsFastQfileRight = lsFastQfileRight;
	}
	/**必须对每个文件都有一个前缀 */
	public void setLsPrefix(ArrayList<String> lsPrefix) {
		this.lsCondition = lsPrefix;
	}
	
	public void running() {
		if (!setMapCondition2LsFastQLR()) {
			return;
		}
		//过滤以及合并reads
		filteredAndCombineReads();
	}
	
	/** 当设定好lsCondition和lsLeft和lsRight后，可以不filter直接获得该项目<br>
	 * 这时候获得的就是所有没过滤的fastq文件
	 */
	public Map<String, List<FastQ[]>> getFilteredMap() {
		if (mapCondition2LRFiltered.size() == 0) {
			if (mapCondition2LsFastQLR.size() == 0) {
				setMapCondition2LsFastQLR();
			}
			return mapCondition2LsFastQLR;
		}
		return mapCondition2LRFiltered;
	}
	
	/**
	 * 将输入文件整理成
	 * map Prefix--leftList  rightList
	 * 的形式
	 * @return 内部会判定同一类的Fastq文件是否都是双端或都是单端
	 */
	private boolean setMapCondition2LsFastQLR() {
		mapCondition2LsFastQLR.clear();
		mapCondition2LRFiltered.clear();
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
	
	private void filteredAndCombineReads() {
		mapCond2FastQCBefore = new LinkedHashMap<String, FastQC[]>();
		mapCond2FastQCAfter = new LinkedHashMap<String, FastQC[]>();
		
		HashSet<String> setPrefix = new LinkedHashSet<String>();
		for (String string : lsCondition) {
			setPrefix.add(string);
		}
		for (String prefix : setPrefix) {
			List<FastQ[]> lsFastQLR = mapCondition2LsFastQLR.get(prefix);
			if (!filter && lsFastQLR.size() < 2) {
				mapCondition2LRFiltered.put(prefix, lsFastQLR);
				continue;
			}

			FastQReadingChannel fastQReadingChannel = new FastQReadingChannel();
			fastQReadingChannel.setFastQRead(lsFastQLR);
			//QC before Filter
			FastQC[] fastQCsBefore = getFastQC(lsFastQLR, prefix, qcBefore);
			fastQReadingChannel.setFastQC(fastQCsBefore[0], fastQCsBefore[1]);
			mapCond2FastQCBefore.put(prefix, fastQCsBefore);
			//Filter
			fastQReadingChannel.setFilter(getFastQParameter(), lsFastQLR.get(0)[0].getOffset());
			//QC after Filter
			FastQC[] fastQCsAfter = getFastQC(lsFastQLR, prefix, qcAfter);				
			fastQReadingChannel.setFastQC(fastQCsAfter[0], fastQCsAfter[1]);
			mapCond2FastQCAfter.put(prefix, fastQCsAfter);
			
			FastQ[] fastqWrite = createCombineFastq(prefix, lsFastQLR);
			fastQReadingChannel.setFastQWrite(fastqWrite[0], fastqWrite[1]);
			fastQReadingChannel.setThreadNum(8);
			fastQReadingChannel.run();
			List<FastQ[]> lsFastQs = new ArrayList<FastQ[]>();
			lsFastQs.add(fastqWrite);
			mapCondition2LRFiltered.put(prefix, lsFastQs);
		}
	}
	
	private FastQ[] createCombineFastq(String condition, List<FastQ[]> lsFastq) {
		FastQ[] fastQs = new FastQ[2];
		if (filter) condition = condition + "_filtered";
		if (lsFastq.size() > 1) condition = condition + "_Combine";
		
		if (lsFastq.get(0)[1] == null) {
			fastQs[0] = new FastQ(outFilePrefix + condition + ".fq", true);
		} else {
			fastQs[0] = new FastQ(outFilePrefix + condition + "_1.fq", true);
			fastQs[1] = new FastQ(outFilePrefix + condition + "_2.fq", true);
		}
		return fastQs;
	}
	
	private FastQC[] getFastQC(List<FastQ[]> lsFastQLR, String prefix, boolean qc) {
		FastQC[] fastQCs = new FastQC[2];
		if (lsFastQLR.get(0).length == 1) {
			fastQCs[0] = new FastQC(prefix, qc);
		} else {
			fastQCs[0] = new FastQC(prefix + "_Left", qc);
			fastQCs[1] = new FastQC(prefix + "_Right", qc);
		}
		return fastQCs;
	}

	private FastQRecordFilter getFastQParameter() {
		FastQRecordFilter fastQfilterRecord = new FastQRecordFilter();
		if (filter) {
			fastQfilterRecord.setFilterParamAdaptorLeft(adaptorLeft.trim());
			fastQfilterRecord.setFilterParamAdaptorRight(adaptorRight.trim());
			fastQfilterRecord.setFilterParamAdaptorLowercase(adaptorLowercase);
			fastQfilterRecord.setFilterParamReadsLenMin(readsLenMin);
			fastQfilterRecord.setQualityFilter(this.fastqQuality);
			fastQfilterRecord.setFilterParamTrimNNN(trimNNN);
		} else {
			fastQfilterRecord.setIsFiltered(false);
		}

		return fastQfilterRecord;
	}
}
