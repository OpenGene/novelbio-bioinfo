package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapAbs;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.SAMtools;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;

public class CtrlFastQMapping {
	private static Logger logger = Logger.getLogger(CtrlFastQMapping.class);
	public static final int COPE_FILTERING = 2;
	public static final int COPE_MAPPING = 4;
	public static final int COPE_TOBED = 8;
	public static final int COPE_BEDEXTEND = 16;
	public static final int COPE_BEDSORT = 32;
	
	
	public static final int LIBRARY_SINGLE_END = 128;
	public static final int LIBRARY_PAIR_END = 256;
	public static final int LIBRARY_MATE_PAIR = 512;
	
	public static final int FILEFORMAT_FASTQ = 3;
	public static final int FILEFORMAT_SAM = 6;
	public static final int FILEFORMAT_BED = 9;
	
	/**
	 * 建库方式
	 */
	int libraryType = LIBRARY_SINGLE_END;
	int fileformat = FILEFORMAT_FASTQ;
	/**
	 * 设定文件格式 FILEFORMAT_FASTQ 等等
	 * @param fileformat
	 */
	public void setFileformat(int fileformat) {
		this.fileformat = fileformat;
	}
	/**
	 * 是否删除两端
	 */
	boolean trimNNN = false;

	int fastqQuality = FastQ.QUALITY_MIDIAN;

	boolean uniqMapping = true;
	/**
	 * 将bed文件延长至多少bp
	 * 小于等于0表示不延长
	 */
	int bedExtend = 240;
	int retainBp = 25;
	/**
	 * 读取配置文件，也就是物种与genome序列的对应文件
	 */
	int taxID = 0;
	
	ArrayList<String[]> lsResult = new ArrayList<String[]>();
	
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public void setUniqMapping(boolean uniqMapping) {
		this.uniqMapping = uniqMapping;
	}
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	public void setFastqQuality(int fastqQuality) {
		this.fastqQuality = fastqQuality;
	}

	/**
	 * 将bed文件延长至多少bp
	 * 小于等于0表示不延长
	 */
	public void setBedExtend(int bedExtend) {
		this.bedExtend = bedExtend;
	}
	boolean sortBed = false;
	public void setSortBed(boolean sortBed) {
		this.sortBed = sortBed;
	}
	
	static HashMap<Integer, String> hashBWA = new HashMap<Integer, String>();
	static HashMap<Integer, HashMap<String, Double>> hashChrLen= new HashMap<Integer, HashMap<String,Double>>();
	String outPathAndFileNamePrix = "";
	/**
	 * 输出文件路径和文件名
	 * @param outPathAndFileNamePrix
	 */
	public void setOutPathAndFileNamePrix(String outPathAndFileNamePrix) {
		this.outPathAndFileNamePrix = outPathAndFileNamePrix.trim();
	}
	/**
	 * 文件名，0：文件名，1：文件的prix，2：group
	 * 因为后边涉及到删减
	 */
	LinkedList<String[]> lsFileName = new LinkedList<String[]>();
	TreeSet<Integer> treeCopeInfo = new TreeSet<Integer>();
	/**
	 * whether the sequencing data is single end or pair end
	 * @param singleEnd
	 */
	public void setLibraryType(int libraryType) {
		this.libraryType = libraryType;
	}
	
	/**
	 * 根据双端或者单端返回表格
	 * @return
	 */
	public ArrayList<String[]> getlsGetFileInfo()
	{
		GUIFileOpen guiFileOpen = new GUIFileOpen();
		ArrayList<String> lsFileName = guiFileOpen.openLsFileName("Sequencing File");
		ArrayList<String[]> lsFileResult = new ArrayList<String[]>();
		
		for (String string : lsFileName) {
			String[] tmpResult = null;
			tmpResult = new String[]{string, FileOperate.getFileNameSep(string)[0], "1"};
			lsFileResult.add(tmpResult);
		}
		return lsFileResult;
	}
	
	public void setFile(LinkedList<String[]> lsFile) {
		this.lsFileName = lsFile;
	}
	/**
	 * add cope type
	 * @param copeInfo such as COPE_FILTERING
	 */
	public void setCopeInfo(int copeInfo) {
		treeCopeInfo.add(copeInfo);
	}
	
	public void running() {
		lsResult.clear();
		lsResult.add(new String[]{"SampleName", "SampleFormat", "ReadsNum"});
		sortFile();
		if (fileformat == FILEFORMAT_FASTQ && treeCopeInfo.contains(COPE_FILTERING)) {
			filteredFastQ();
		}
		if (fileformat == FILEFORMAT_FASTQ && treeCopeInfo.contains(COPE_MAPPING)) {
			mapping();
			fileformat = FILEFORMAT_SAM;
		}
		if (fileformat == FILEFORMAT_SAM && treeCopeInfo.contains(COPE_TOBED)) {
			convertToBed();
			fileformat = FILEFORMAT_BED;
		}
		if (fileformat == FILEFORMAT_BED && treeCopeInfo.contains(COPE_BEDEXTEND)) {
			extendBed();
		}
		if (fileformat == FILEFORMAT_BED && treeCopeInfo.contains(COPE_BEDSORT)) {
			sortBed();
		}
		lsFileName.clear();
		lsResult.clear();
		hashBWA.clear();
		treeCopeInfo.clear();
		TxtReadandWrite txtStatistic = new TxtReadandWrite(outPathAndFileNamePrix + "statistics", true);
		txtStatistic.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	
	/**
	 * 相同的group放在一起，如果多个group都相同，则将相同的prix放在一起
	 */
	private void sortFile() {
		Collections.sort(lsFileName, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				int group = o1[2].compareTo(o2[2]);
				if (group == 0) {
					group = o1[1].compareTo(o2[1]);
				}
				if (group == 0) {
					group = o1[0].compareTo(o2[0]);
				}
				return group;
			}
		});
	}
	/**
	 * filter Fastq文件
	 * 然后
	 */
	private void filteredFastQ()
	{
		FastQ fastQ = null;
		if (libraryType == LIBRARY_SINGLE_END) {
			for (int i = 0; i < lsFileName.size(); i ++) {
				String[] ss = lsFileName.get(i);
				String filterOut = outPathAndFileNamePrix + ss[1] + ss[2];
				filterOut = FileOperate.changeFileSuffix(filterOut, "", "fq");
				fastQ = new FastQ(ss[0], fastqQuality);
				
				lsResult.add(new String[]{ss[1] + ss[2], "RawData", fastQ.getSeqNum()+""});
				
				fastQ.setTrimNNN(trimNNN);
				fastQ.setLenReadsMin(retainBp);
				fastQ = fastQ.filterReads(filterOut);
				ss[0] = fastQ.getFileName();
				lsResult.add(new String[]{ ss[1] + ss[2], "FilteredReads",fastQ.getSeqNum()+""});
			}
		}
		else {
			for (int i = 1; i < lsFileName.size(); i = i + 2) {
				String[] ss0 = lsFileName.get(i-1);
				String[] ss1 = lsFileName.get(i);
				String filterOut = outPathAndFileNamePrix + ss0[1] + ss0[2];
				fastQ = new FastQ(ss0[0], ss1[0], fastqQuality);
				
				lsResult.add(new String[]{ss0[1] + ss0[2], "RawData", fastQ.getSeqNum()+""});
				
				fastQ.setTrimNNN(trimNNN);
				fastQ.setLenReadsMin(retainBp);
				fastQ = fastQ.filterReads(filterOut);
				ss0[0] = fastQ.getFileName();
				ss1[0] = fastQ.getSeqFile2();
				
				lsResult.add(new String[]{ ss0[1] + ss0[2], "FilteredReads", fastQ.getSeqNum()+""});
			}
		}
	
	}
	/**
	 * 将lsFileName里面的文件做mapping
	 * mapping后用sam替换lsFileName里面的文件
	 */
	private void mapping()
	{
		String chrFile = hashBWA.get(taxID).trim();		
		if (libraryType == LIBRARY_SINGLE_END) {
			for (int i = 0; i < lsFileName.size(); i ++) {
				String[] ss = lsFileName.get(i);
				String filterOut = outPathAndFileNamePrix + ss[1] + ss[2];
				filterOut = FileOperate.changeFileSuffix(filterOut, "", "sam");
				FastQMapBwa fastQ = new FastQMapBwa(ss[0], fastqQuality, filterOut, uniqMapping);
				
				if (lsResult.size() == 0) {
					lsResult.add(new String[]{ ss[1] + ss[2], "FastQFormat", fastQ.getSeqNum()+""});
				}
				
				fastQ.setFilePath("", chrFile);
				SamFile samFile = fastQ.mapReads();
				ss[0] = samFile.getFileName();
				
				lsResult.add(new String[]{ ss[1] + ss[2], "SamFormat", samFile.getReadsNum(SamFile.MAPPING_UNIQUE) + ""});
				
			}
		}
		else {
			for (int i = 1; i < lsFileName.size(); i = i + 2) {
				String[] ss0 = lsFileName.get(i-1);
				String[] ss1 = lsFileName.get(i);
				String filterOut = outPathAndFileNamePrix + ss0[1] + ss0[2];
				filterOut = FileOperate.changeFileSuffix(filterOut, "", "sam");
				FastQMapBwa fastQ = new FastQMapBwa(ss0[0], ss1[0], fastqQuality, filterOut, uniqMapping);
				
				if (lsResult.size() == 0) {
					lsResult.add(new String[]{ ss0[1] + ss0[2], "FastQFormat", fastQ.getSeqNum()+""});
				}
				
				fastQ.setFilePath("", chrFile);
				SamFile samFile = fastQ.mapReads();
				ss0[0] = samFile.getFileName();
				
				lsResult.add(new String[]{ ss0[1] + ss0[2], "SamFormat",samFile.getReadsNum(SamFile.MAPPING_UNIQUE) + ""});
				
			}
			//TODO 这里可能有错
			for (int i = lsFileName.size() - 1; i >= 0; i = i - 2) {
				lsFileName.remove(i);
			}
		}
	}
	/**
	 * 将sam文件转化为bed文件
	 * @throws Exception 
	 */
	private void convertToBed()
	{
		for (int i = 0; i < lsFileName.size(); i ++) {
			String[] ss = lsFileName.get(i);
			String filterOutSE = outPathAndFileNamePrix + ss[1] + ss[2];
			filterOutSE = FileOperate.changeFileSuffix(filterOutSE, "_SE", "bed");
			SAMtools saMtools = new SAMtools(ss[0], false, 10);
			BedSeq bedSeq = saMtools.sam2bed(TxtReadandWrite.TXT, filterOutSE, uniqMapping);
			
			lsResult.add(new String[]{ ss[1] + ss[2], "BedFormat", bedSeq.getSeqNum()+""});

			ss[0] = bedSeq.getFileName();
		}
	}
	
	private void extendBed()
	{
		if (bedExtend <= 0) {
			return;
		}
		for (int i = 0; i < lsFileName.size(); i ++) {
			String[] ss = lsFileName.get(i);
			String filterOutExtend = outPathAndFileNamePrix + ss[1] + ss[2];
			filterOutExtend = FileOperate.changeFileSuffix(filterOutExtend, "_Extend", "bed");
			BedSeq bedSeq = new BedSeq(ss[0]);
			bedSeq = bedSeq.extend(bedExtend, filterOutExtend);
			ss[0] = bedSeq.getFileName();
		}
	}
	
	private void sortBed()
	{
		for (int i = 0; i < lsFileName.size(); i ++) {
			String[] ss = lsFileName.get(i);
			String filterOutExtend = outPathAndFileNamePrix + ss[1] + ss[2];
			filterOutExtend = FileOperate.changeFileSuffix(filterOutExtend, "_Sorted", "bed");
			BedSeq bedSeq = new BedSeq(ss[0]);
			bedSeq = bedSeq.sortBedFile(filterOutExtend);
			
			double coverage = bedSeq.getCoverage(hashChrLen.get(taxID)).get(BedSeq.ALLMAPPEDREADS)[1];
			lsResult.add(new String[]{ ss[1] + ss[2], "BedFormatCoverage", coverage + ""});
			
			ss[0] = bedSeq.getFileName();
		}
	}
	
}
