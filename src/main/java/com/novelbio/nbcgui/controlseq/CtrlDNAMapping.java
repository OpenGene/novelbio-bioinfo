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
import com.novelbio.analysis.seq.mapping.MapDNA;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class CtrlDNAMapping {
	private static Logger logger = Logger.getLogger(CtrlDNAMapping.class);
	
	public static final int LIBRARY_SINGLE_END = 128;
	public static final int LIBRARY_PAIR_END = 256;
	public static final int LIBRARY_MATE_PAIR = 512;
	
	public static final int MAP_TO_CHROM = 8;
	public static final int MAP_TO_REFSEQ = 4;
	public static final int MAP_TO_REFSEQ_LONGEST_ISO = 2;
	
	private String outFilePrefix = "";
	
	private HashMap<String, FastQ[]> mapCondition2CombFastQLRFiltered = new LinkedHashMap<String, FastQ[]>();
	
	boolean mapping = false;
	int gapLen = 5;
	double mismatch = 2;
	int thread = 4;
	
	String chrIndexFile;
	Species species;
	int map2Index = MAP_TO_CHROM;
	
	SoftWare softMapping = SoftWare.bwa;
	
	SoftWareInfo softWareInfo = new SoftWareInfo();
	
	TxtReadandWrite txtReport;
	/** 
	 * @param species
	 * @param map2Index mapping到什么上面去，有chrom，refseq和refseqLongestIso三种
	 */
	public void setSpecies(Species species, int map2Index) {
		this.species = species;
		this.map2Index = map2Index;
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
	public void setSoftMapping(SoftWare softMapping) {
		this.softMapping = softMapping;
	}
	public void setThread(int thread) {
		this.thread = thread;
	}

	public void running() {
		txtReport = new TxtReadandWrite(outFilePrefix + "reportInfo", true);
		
		setMapCondition2LsFastQLR();

		txtReport.writefileln("Sample Mapping Infomation");
		txtReport.writefile("", true);
		mapping();
		
		txtReport.close();
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
	
	private void mapping() {
		softWareInfo.setName(softMapping);
		for (Entry<String, FastQ[]> entry : mapCondition2CombFastQLRFiltered.entrySet()) {
			String prefix = entry.getKey();
			FastQ[] fastQs = entry.getValue();
			MapDNA mapSoftware = MapDNA.creatMapDNA(softMapping);
			
			if (species.getTaxID() == 0) {
				mapSoftware.setExePath(softWareInfo.getExePath());
				mapSoftware.setChrFile(chrIndexFile);
			}
			else {
				if (map2Index == MAP_TO_CHROM) {
					mapSoftware.setExePath(softWareInfo.getExePath());
					mapSoftware.setChrFile(species.getIndexChr(softMapping));
				} else if (map2Index == MAP_TO_REFSEQ) {
					mapSoftware.setExePath(softWareInfo.getExePath());
					mapSoftware.setChrFile(species.getIndexRef(softMapping));
				} else if (map2Index == MAP_TO_REFSEQ_LONGEST_ISO) {
					mapSoftware.setExePath(softWareInfo.getExePath());
					mapSoftware.setChrFile(species.getRefseqLongestIsoNrFile());
				}
			}

			mapSoftware.setFqFile(fastQs[0], fastQs[1]);
			mapSoftware.setOutFileName(outFilePrefix + prefix);
			mapSoftware.setGapLength(gapLen);
			mapSoftware.setMismatch(mismatch);
			mapSoftware.setSampleGroup(prefix, prefix, prefix, null);
			mapSoftware.setMapLibrary(libraryType);
			mapSoftware.setThreadNum(thread);
			SamFileStatistics samFileStatistics = new SamFileStatistics();
			mapSoftware.addAlignmentRecorder(samFileStatistics);
			mapSoftware.mapReads();
			
			ArrayList<String[]> lsStatistics = samFileStatistics.getMappingInfo();
			txtReport.writefileln(prefix);
			for (String[] strings : lsStatistics) {
				txtReport.writefileln(strings);
			}
			txtReport.flash();
		}
	}
	
	public static HashMap<String, Integer> getMapStr2Index() {
		HashMap<String, Integer> mapStr2Index = new HashMap<String, Integer>();
		mapStr2Index.put("chromosome", MAP_TO_CHROM);
		mapStr2Index.put("refseq", MAP_TO_REFSEQ);
		mapStr2Index.put("refseq Longest Iso", MAP_TO_REFSEQ_LONGEST_ISO);
		return mapStr2Index;
	}
}
