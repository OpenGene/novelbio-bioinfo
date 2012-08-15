package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.fastq.FastQfilterRecord;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.analysis.seq.mapping.SamFileStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class CtrlFastQMapping {

	private static Logger logger = Logger.getLogger(CtrlFastQMapping.class);
	
	public static final int LIBRARY_SINGLE_END = 128;
	public static final int LIBRARY_PAIR_END = 256;
	public static final int LIBRARY_MATE_PAIR = 512;
	
	boolean filter = true;
	boolean trimNNN = false;
	int fastqQuality = FastQ.QUALITY_MIDIAN;
	boolean uniqMapping = true;
	int readsLenMin = 18;
	int libraryType = LIBRARY_SINGLE_END;
	String adaptorLeft = "";
	String adaptorRight = "";
	boolean adaptorLowercase =false;
	
	/** 排列顺序与lsFastQfileLeft和lsFastQfileRight相同 
	 * 表示分组
	 * */
	ArrayList<String> lsCondition = new ArrayList<String>();
	ArrayList<String> lsFastQfileLeft = new ArrayList<String>();
	ArrayList<String> lsFastQfileRight = new ArrayList<String>();
	
	String outFilePrefix = "";
	
	HashMap<String, ArrayList<FastQ[]>> mapCondition2LsFastQLR = new HashMap<String, ArrayList<FastQ[]>>();
	
	HashMap<String, FastQ[]> mapCondition2CombFastQLRFiltered = new HashMap<String, FastQ[]>();
	
	boolean mapping = false;
	int gapLen = 5;
	double mismatch = 2;
	int thread = 4;
	String chrIndexFile;
	
	Species species;
	SoftWareInfo softWareInfo = new SoftWareInfo();
	
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
	public void setFilter(boolean filter) {
		this.filter = filter;
	}
	public void setLibraryType(int libraryType) {
		this.libraryType = libraryType;
	}
	public void setReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	public void setUniqMapping(boolean uniqMapping) {
		this.uniqMapping = uniqMapping;
	}
	public void setChrIndexFile(String chrIndexFile) {
		if (FileOperate.isFileExistAndBigThanSize(chrIndexFile, 10)) {
			this.chrIndexFile = chrIndexFile;
		}
	}
	public void setMapping(boolean mapping) {
		this.mapping = mapping;
	}
	public void setOutFilePrefix(String outFilePrefix) {
		if (FileOperate.isFileDirectory(outFilePrefix)) {
			outFilePrefix = FileOperate.addSep(outFilePrefix);
		}
		this.outFilePrefix = outFilePrefix;
	}
	public void setGapLen(int gapLen) {
		this.gapLen = gapLen;
	}
	public void setMismatch(Double mismatch) {
		this.mismatch = mismatch;
	}
	public void setThread(int thread) {
		this.thread = thread;
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
		txtReport = new TxtReadandWrite(outFilePrefix + "reportInfo", true);
		
		setMapCondition2LsFastQLR();
		
		if (filter) {
			txtReport.writefileln("Sample\tAllReads\tFilteredReads");
			txtReport.writefile("", true);
			filteredReads();
		}
		combineAllFastqFile();
		if (mapping) {
			txtReport.writefileln("Sample");
			txtReport.writefile("", true);
			mapping();
		}
		txtReport.close();
	}
	
	private void mapping() {
		softWareInfo.setName(SoftWare.bwa);
		for (Entry<String, FastQ[]> entry : mapCondition2CombFastQLRFiltered.entrySet()) {
			String prefix = entry.getKey();
			FastQ[] fastQs = entry.getValue();
			MapBwa mapBwa = new MapBwa();
			
			if (chrIndexFile != null)
				mapBwa.setExePath(softWareInfo.getExePath(), chrIndexFile);
			else
				mapBwa.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bwa));

			mapBwa.setFqFile(fastQs[0], fastQs[1]);
			mapBwa.setOutFileName(outFilePrefix + prefix);
			mapBwa.setGapLength(gapLen);
			mapBwa.setMismatch(mismatch);
			mapBwa.setSampleGroup(prefix, null, null, null);
			if (libraryType == LIBRARY_MATE_PAIR) {
				mapBwa.setInsertSize(200, 4000);
			}
			else if (libraryType == LIBRARY_PAIR_END) {
				mapBwa.setInsertSize(150, 500);
			}
			mapBwa.setThreadNum(thread);
			SamFile samFile = mapBwa.mapReads();
			SamFileStatistics samFileStatistics = samFile.getStatistics();
			
			txtReport.writefileln(prefix);
			txtReport.ExcelWrite(samFileStatistics.getMappingInfo());
			txtReport.writefile("", true);
		}
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
			setFastqLR(tmpFastQLR, lsFastQfileLeft, lsFastQfileRight, i);
			lsPrefixFastQLR.add(tmpFastQLR);
		}
	}
	private void setFastqLR(FastQ[] tmpFastQLR, ArrayList<String> lsFastqL, ArrayList<String> lsFastqR, int num) {
		String fastqL = "", fastqR = "";
		String compressType = TxtReadandWrite.TXT;
		if (lsFastqL.size() > num) {
			fastqL = lsFastqL.get(num);
		}
		if (lsFastqR.size() > num) {
			fastqR = lsFastqR.get(num);
		}
		if (FileOperate.isFileExistAndBigThanSize(fastqL, 10)) {
			if (fastqL.endsWith(".gz")) {
				compressType = TxtReadandWrite.GZIP;
			}
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqR, 10)) {
			if (fastqR.endsWith(".gz")) {
				compressType = TxtReadandWrite.GZIP;
			}
		}
		if (FileOperate.isFileExistAndBigThanSize(fastqL, 10) && FileOperate.isFileExistAndBigThanSize(fastqR, 10)) {
			tmpFastQLR[0] = new FastQ(fastqL);
			tmpFastQLR[1] = new FastQ(fastqR);;
			setFastQParameter(tmpFastQLR[0], compressType);
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqL, 10)) {
			tmpFastQLR[0] = new FastQ(fastqL);
			setFastQParameter(tmpFastQLR[0], compressType);
		}
		else if (FileOperate.isFileExistAndBigThanSize(fastqR, 10)) {
			tmpFastQLR[0] = new FastQ(fastqR);
			setFastQParameter(tmpFastQLR[0], compressType);
		}
	}
	private void filteredReads() {
		HashSet<String> setPrefix = new HashSet<String>();
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
			
			if (lsFastQLR.get(0)[1] != null)
				txtReport.writefileln(prefix + "\t" + allReads*2 + "\t" + filteredReads*2);
			else
				txtReport.writefileln(prefix + "\t" + allReads + "\t" + filteredReads);

		}
		txtReport.writefileln();
	}
	private FastQ[] filteredFastQFile(FastQ fastq1, FastQ fastq2) {
		FastQ[] fasQFiltered = new FastQ[2];
		if (filter) {
			if (fastq2 != null) {
				fasQFiltered = fastq1.filterReads(fastq2);
			}
			else {
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
	private void setFastQParameter(FastQ fastQ, String compressType) {
		FastQfilterRecord fastQfilterRecord = new FastQfilterRecord();
		fastQfilterRecord.setFilterParamAdaptorLeft(adaptorLeft.trim());
		fastQfilterRecord.setFilterParamAdaptorRight(adaptorRight.trim());
		fastQfilterRecord.setFilterParamAdaptorLowercase(adaptorLowercase);
		fastQfilterRecord.setFilterParamReadsLenMin(readsLenMin);
		fastQfilterRecord.setFilterParamQuality(this.fastqQuality);
		fastQfilterRecord.setFilterParamTrimNNN(trimNNN);
		fastQ.setFilterParam(fastQfilterRecord);
		fastQ.setCompressType(compressType, TxtReadandWrite.TXT);
	}
	
	public static HashMap<String, Integer> getMapLibrary() {
		HashMap<String, Integer> mapReadsQualtiy = new LinkedHashMap<String, Integer>();
		mapReadsQualtiy.put("SingleEnd", LIBRARY_SINGLE_END);
		mapReadsQualtiy.put("PairEnd", LIBRARY_PAIR_END);
		mapReadsQualtiy.put("MatePair", LIBRARY_MATE_PAIR);
		return mapReadsQualtiy;
	}
}
