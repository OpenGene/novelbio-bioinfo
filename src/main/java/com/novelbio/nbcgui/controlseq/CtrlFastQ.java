package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.fastq.FastQRecordFilter;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlFastQ {
	private static Logger logger = Logger.getLogger(CtrlFastQ.class);
	
	boolean filter = true;
	boolean trimNNN = false;
	int fastqQuality = FastQ.QUALITY_MIDIAN;
	int readsLenMin = 18;
	MapLibrary libraryType = MapLibrary.SingleEnd;
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
	
	//以下为开始过滤和过滤后的文件
	HashMap<String, ArrayList<FastQ[]>> mapCondition2LsFastQLR = new LinkedHashMap<String, ArrayList<FastQ[]>>();
	HashMap<String, FastQ[]> mapCondition2CombFastQLRFiltered = new LinkedHashMap<String, FastQ[]>();
	
	TxtReadandWrite txtReport;
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
	public void setLibraryType(MapLibrary libraryType) {
		this.libraryType = libraryType;
	}
	public void setReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	public void setOutFilePrefix(String outFilePrefix) {
		if (FileOperate.isFileDirectory(outFilePrefix)) {
			outFilePrefix = FileOperate.addSep(outFilePrefix);
		}
		this.outFilePrefix = outFilePrefix;
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
		setMapCondition2LsFastQLR();
		if (filter) {
			txtReport = new TxtReadandWrite(outFilePrefix + "reportFastQFilter", true);
			txtReport.writefileln("Sample\tAllReads\tFilteredReads");
			txtReport.writefile("", true);
			filteredReads();
		}
		combineAllFastqFile();
	}
	/** 将输入文件整理成
	 * map Prefix--leftList  rightList
	 * 的形式
	 */
	private void setMapCondition2LsFastQLR() {
		for (int i = 0; i < lsCondition.size(); i++) {
			String prefix = lsCondition.get(i);
			ArrayList<FastQ[]> lsPrefixFastQLR = new ArrayList<FastQ[]>();
			if (mapCondition2LsFastQLR.containsKey(prefix)) {
				lsPrefixFastQLR = mapCondition2LsFastQLR.get(prefix);
			}
			else {
				mapCondition2LsFastQLR.put(prefix, lsPrefixFastQLR);
			}
			FastQ[] tmpFastQLR = new FastQ[2];
			String fastqL = getFastqFile(lsFastQfileLeft, i);
			String fastqR = getFastqFile(lsFastQfileRight, i);
			setFastqLR(tmpFastQLR, fastqL, fastqR);
			lsPrefixFastQLR.add(tmpFastQLR);
		}
	}
	/**
	 * 主要是怕lsFastqRight可能没东西
	 * @param lsFastq
	 * @param num
	 * @return
	 */
	private String getFastqFile(ArrayList<String> lsFastq, int num) {
		if (lsFastq.size() > num) {
			return lsFastq.get(num);
		}
		return null;
	}
	/**
	 * @param tmpFastQLR
	 * @param lsFastqL
	 * @param lsFastqR
	 * @param num lsCondition的编号
	 */
	private void setFastqLR(FastQ[] tmpFastQLR, String fastqL, String fastqR) {
		if (FileOperate.isFileExistAndBigThanSize(fastqL, 1) && FileOperate.isFileExistAndBigThanSize(fastqR, 1)) {
			tmpFastQLR[0] = new FastQ(fastqL);
			tmpFastQLR[1] = new FastQ(fastqR);;
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqL, 1)) {
			tmpFastQLR[0] = new FastQ(fastqL);
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqR, 1)) {
			tmpFastQLR[0] = new FastQ(fastqR);
		}
		setFastQParameter(tmpFastQLR[0]);
	}
	
	private void filteredReads() {
		HashSet<String> setPrefix = new LinkedHashSet<String>();
		for (String string : lsCondition) {
			setPrefix.add(string);
		}
		for (String prefix : setPrefix) {
			long allReads = 0;
			long filteredReads = 0;
			ArrayList<FastQ[]> lsFastQLR = mapCondition2LsFastQLR.get(prefix);
			ArrayList<FastQ[]> lsFiltered = new ArrayList<FastQ[]>();
			for (FastQ[] fastq : lsFastQLR) {
				FastQ[] fastQFiltered = filteredFastQFile(fastq[0], fastq[1]);
				allReads = allReads + fastq[0].getSeqNum();
				filteredReads = filteredReads + fastQFiltered[0].getSeqNum();
				lsFiltered.add(fastQFiltered);
			}
			mapCondition2LsFastQLR.put(prefix, lsFiltered);
			
			if (lsFastQLR.get(0)[1] != null) {
				txtReport.writefileln(prefix + "\t" + allReads*2 + "\t" + filteredReads*2);
			} else {
				txtReport.writefileln(prefix + "\t" + allReads + "\t" + filteredReads);
			}
			txtReport.flash();
		}
		txtReport.writefileln();
	}
	
	private FastQ[] filteredFastQFile(FastQ fastq1, FastQ fastq2) {
		FastQ[] fasQFiltered = new FastQ[2];
		if (filter) {
			if (fastq2 != null) {
				fasQFiltered = fastq1.filterReads(fastq2);
			} else {
				fasQFiltered[0] = fastq1.filterReads();
			}
		}
		else {
			fasQFiltered[0] = fastq1;
			fasQFiltered[1] = fastq2;
		}
		return fasQFiltered;
	}
	private void combineAllFastqFile() {
		for (Entry<String, ArrayList<FastQ[]>> entry : mapCondition2LsFastQLR.entrySet()) {
			combineFastqFile(entry.getKey(), entry.getValue());
		}
	}
	private void combineFastqFile(String condition, ArrayList<FastQ[]> lsFastq) {
		if (lsFastq.size() == 1) {
			mapCondition2CombFastQLRFiltered.put(condition, lsFastq.get(0));
			return;
		}
		
		FastQ fastQL, fastQR;
		boolean PairEnd = false;
		if (filter) condition = condition + "_filtered";
		
		if (lsFastq.get(0)[1] == null) {
			fastQL = new FastQ(outFilePrefix + condition + "_Combine.fq", true);
		}
		else {
			fastQL = new FastQ(outFilePrefix + condition + "_Combine_1.fq", true);
			PairEnd = true;
		}
		fastQR = new FastQ(outFilePrefix + condition + "_Combine_2.fq", true);
		for (FastQ[] fastQs : lsFastq) {
			for (FastQRecord fastQRecord : fastQs[0].readlines()) {
				fastQL.writeFastQRecord(fastQRecord);
			}
			if (PairEnd) {
				for (FastQRecord fastQRecord : fastQs[1].readlines()) {
					fastQR.writeFastQRecord(fastQRecord);
				}
			}
		}
		
		fastQL.close();
		if (PairEnd) fastQR.close();
		
		mapCondition2CombFastQLRFiltered.put(condition, new FastQ[]{fastQL, fastQR});
	}
	
	private void setFastQParameter(FastQ fastQ) {
		FastQRecordFilter fastQfilterRecord = new FastQRecordFilter();
		fastQfilterRecord.setFilterParamAdaptorLeft(adaptorLeft.trim());
		fastQfilterRecord.setFilterParamAdaptorRight(adaptorRight.trim());
		fastQfilterRecord.setFilterParamAdaptorLowercase(adaptorLowercase);
		fastQfilterRecord.setFilterParamReadsLenMin(readsLenMin);
		fastQfilterRecord.setQualityFilter(this.fastqQuality);
		fastQfilterRecord.setFilterParamTrimNNN(trimNNN);
		fastQ.setFilter(fastQfilterRecord);
	}
}
