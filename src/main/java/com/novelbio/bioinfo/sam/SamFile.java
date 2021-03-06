package com.novelbio.bioinfo.sam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.AlignSeq;
import com.novelbio.bioinfo.base.FormatSeq;
import com.novelbio.bioinfo.bed.BedFile;
import com.novelbio.bioinfo.fasta.FastaDictMake;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQRecord;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedStreamConstants;

/**
 * 提取为bed文件时，仅仅考虑f-r情况
 * 待检查，特别是转换成bed文件时是否精确到了1bp，譬如起点是否为开区间
 * @author zong0jie
 *
 */
public class SamFile implements AlignSeq {
	private static final Logger logger = Logger.getLogger(SamFile.class);
	static {
		SamReaderFactory.setDefaultValidationStringency(ValidationStringency.SILENT);
	}
	public static void main(String[] args) {
		String parentPath = "/hdfs:/nbCloud/public/AllProject/project_55079d2ce4b0b3b73a8e2003/task_55093d79e4b0b3b73a8e2093/MiRNASeqAnalysis_result/TmpMapping/";
		List<SamFile> lsSamFiles = new ArrayList<>();
		lsSamFiles.add(new SamFile(parentPath + "CM1_Genome.bam"));
		lsSamFiles.add(new SamFile(parentPath + "IM1_Genome.bam"));

		SamFile.mergeBamFile(parentPath + "merge.test", lsSamFiles);
	}
	
	
	public static final int MAPPING_ALLREADS = 2;
	public static final int MAPPING_ALLMAPPEDREADS = 4;
	public static final int MAPPING_UNMAPPED = 8;
	public static final int MAPPING_UNIQUE = 16;
	public static final int MAPPING_REPEAT = 32;
	
//	static SoftWareInfo softWareInfoSamtools = new SoftWareInfo();
	
	boolean read = true;

	SamReader samReader;
	SamWriter samWriter;
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	/** 是否为bam文件 */
	boolean bamFile = false;
	
	boolean isRealigned = false;
		
	String referenceFileName;
	
	/**读取已有文件
	 * 如果有索引会自动读取索引
	 */
	public SamFile(String samBamFile) {
		setSamFileRead(samBamFile);
	}

	/**直接读取流，不支持判定文件格式，不支持索引 */
	public SamFile(InputStream inputStream) {
		setSamRead(inputStream);
	}
	/** 创建新的sambam文件，根据文件名
	 * 根据samFileHeader来定义默认输入的序列是否已经经过排序。
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader) {
		setSamFileNew(samFileHeader, samBamFile, samFileHeader.getSortOrder() != SortOrder.unsorted);
	}
	
	/**读取已有文件
	 * 如果有索引会自动读取索引
	 */
	public SamFile(OutputStream os, SAMFileHeader samFileHeader, boolean isBam) {
		setSamFileNew(samFileHeader, os, isBam, samFileHeader.getSortOrder() != SortOrder.unsorted);
	}
	
	/**
	 * @param os
	 * @param samFileHeader
	 * @param isBam
	 * @param preSorted 输入的文件是否经过排序
	 */
	public SamFile(OutputStream os, SAMFileHeader samFileHeader, boolean isBam, boolean preSorted) {
		setSamFileNew(samFileHeader, os, isBam, preSorted);
	}
	
	/** 创建新的sambam文件'
	 * @param samBamFile
	 * @param samFileHeader
	 * @param preSorted 输入的文件是否已经排序了
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader, boolean preSorted) {
		setSamFileNew(samFileHeader, samBamFile, preSorted);
	}
	
	/** 创建新的sambam文件'
	 * @param samBamFile
	 * @param samFileHeader
	 * @param isBam 输出是否为bam格式
	 * @param preSorted 输入的文件是否已经排序了
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader, boolean isBam, Boolean preSorted) {
		setSamFileNew(samFileHeader, samBamFile, isBam, preSorted);
	}
	
	private void setSamFileRead(String samBamFile) {
		String bamindex = samBamFile + ".bai";
		if (!FileOperate.isFileExistAndBigThanSize(bamindex, 0)) {
			bamindex = null;
		}
		FormatSeq formatSeq = FormatSeq.UNKNOWN;
		if (samBamFile.toLowerCase().endsWith("bam")) {
			formatSeq = FormatSeq.BAM;
		} else if (samBamFile.toLowerCase().endsWith("sam")) {
			formatSeq = FormatSeq.SAM;
		}
		setSamFileRead(formatSeq, samBamFile, bamindex);
	}
	
	public SamReader getSamReader() {
		if (samReader != null) {
			samReader.initial();
		}
		return samReader;
	}
	public boolean isIndexed() {
		if (samReader != null) {
			samReader.initial();
		}
		return samReader.isIndexed;
	}
	private void setSamRead(InputStream inputStream) {
		samReader = new SamReader(inputStream);
		read = true;
	}
	
	private void setSamFileRead(FormatSeq formatSeq, String samFileExist, String fileIndex) {
		if (formatSeq == null || formatSeq == FormatSeq.UNKNOWN) {
			formatSeq = isSamBamFile(samFileExist);
		}
		if (formatSeq != FormatSeq.BAM && formatSeq != FormatSeq.SAM) {
			return;
		}
		if (formatSeq == FormatSeq.BAM) {
			bamFile = true;
			samReader = new SamReader(samFileExist, fileIndex);
		} else {
			samReader = new SamReader(samFileExist);
		}
		samReader.initial();
		read = true;
	}
	
	/** 
	 * 创建新的sam文件
	 * @param samFileHeader
	 * @param samFileCreate
	 * @param preSorted 输入的文件是否经过排序
	 */
	private void setSamFileNew(SAMFileHeader samFileHeader, String samFileCreate, boolean preSorted) {
		read = false;
		samWriter = new SamWriter(preSorted, samFileHeader, samFileCreate);
		if (samFileCreate.toLowerCase().endsWith("sam")) {
			bamFile = false;
		} else {
			bamFile = true;
		}
	}
	/** 
	 * 
	 * 创建新的sam文件
	 * @param samFileHeader
	 * @param samFileCreate
	 * @param isBam 是否输出bam文件
	 * @param preSorted 输入的文件是否经过排序
	 */
	private void setSamFileNew(SAMFileHeader samFileHeader, String samFileCreate, boolean isBam, Boolean preSorted) {
		if (preSorted == null) {
			preSorted = samFileHeader.getSortOrder() != SortOrder.unsorted;
		}
		read = false;
		samWriter = new SamWriter(preSorted, samFileHeader, samFileCreate, isBam);
		bamFile = isBam;
	}
	/** 
	 * 创建新的sam文件
	 * @param samFileHeader
	 * @param os
	 * @param isBam
	 * @param preSorted 输入的文件是否经过排序
	 */
	private void setSamFileNew(SAMFileHeader samFileHeader, OutputStream os, boolean isBam, boolean preSorted) {
		read = false;
		samWriter = new SamWriter(preSorted, samFileHeader, os, isBam);
		bamFile = isBam;
	}
		
	/** 比对到的reference的文件名，用于realign等 */
	public void setReferenceFileName(String referenceFileName) {
		this.referenceFileName = referenceFileName;
		faidxRefsequence();
		FastaDictMake fastadictMake = new FastaDictMake(referenceFileName, FileOperate.changeFileSuffix(referenceFileName, "", "dict"));
		fastadictMake.makeDict();
	}
	
	//TODO 把这个独立出来作为单独的类，并且用picard中的代码实现，摆脱samtools
	private String faidxRefsequence() {
		if (FileOperate.isFileExist(referenceFileName) && !FileOperate.isFileExist(referenceFileName+".fai")) {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
//			samIndexRefsequence.setExePath(softWareInfoSamtools.getExePath());
			samIndexRefsequence.setRefsequence(referenceFileName);
			samIndexRefsequence.indexSequence();
			return referenceFileName+".fai";
		}
		if (FileOperate.isFileExist(referenceFileName+".fai")) {
			return referenceFileName+".fai";
		}
		return "";
	}

	public String getFileName() {
		if (read) {
			return samReader.fileName;
		} else {
			return samWriter.fileName;
		}
	}

	/**
	 * 双端数据是否获得连在一起的bed文件
	 * 如果输入是单端数据，则将序列延长返回bed文件
	 * 注意：如果是双端文件，<b>不能预先排序</b>
	 * @param getPairedBed
	 */
	public boolean isPairend() {
		return getSamReader().isPairend();
	}
	
	/** 
	 * the alignment of the returned SAMRecords need only overlap the interval of interest.
	 * @param chrID 无所谓大小写
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesOverlap(String chrID, int start, int end) {
		return getSamReader().readLinesOverlap(chrID, start, end);
	}
	/**
	 * each SAMRecord returned is will have its alignment completely contained in the interval of interest. 
	 * @param chrID 无所谓大小写
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesContained(String chrID, int start, int end) {
		return getSamReader().readLinesContained(chrID, start, end);
	}
	
	
	/** 读取的具体长度，出错返回 -1 */
	public long getReadByte() {
		if (samReader != null) {
			return samReader.getReadByte();
		}
		return -1;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		if (samReader != null) {
			return samReader.getReadPercentage();
		}
		return -1;
	}
	
	public Iterable<SamRecord> readLines() {
		return getSamReader().readLines();
	}
	public Iterable<SamRecord> readLines(int num) {
		return getSamReader().readLines(num);
	}
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public ArrayList<SamRecord> readHeadLines(int num) {
		ArrayList<SamRecord> lsResult = new ArrayList<SamRecord>();
		int i = 0;
		for (SamRecord samRecord : readLines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(samRecord);
		}
		close();
		return lsResult;
	}
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public SamRecord readFirstLine() {
		SamRecord samRecord = readLines().iterator().next();
		close();
		return samRecord;
	}
	/**
	 * 注意大小写区分
	 * @param ReadName reads的名字，只要写关键词就行了
	 * @return 没找到就返回null
	 */
	public SamRecord getReads(String ReadName) {
		return getSamReader().getReads(ReadName);
	}
	
	/** 默认不排序 */
	public SAMFileHeader getHeader() {
		return getHeader(false);
	}
	
	/** 是否需要排序 */
	public SAMFileHeader getHeader(boolean isNeedSort) {
		SAMFileHeader samFileHeader = getSamReader().getSamFileHead();
		if (isNeedSort) {
			samFileHeader.setSortOrder(SAMFileHeader.SortOrder.coordinate);
//			PathDetail.setTmpDir(FileOperate.getParentPathName(getFileName()));
		}
		return samFileHeader;
	}
	/**
	 * 提取sam文件中没有mapping上的reads，将其保存为单个fastq文件，序列质量默认为中等
	 * @param getNonUniq 是否将非uniq的也提取出来
	 * @return
	 */
	public FastQ getUnMappedReads(boolean getNonUniq, String outFastQfile) {
		FastQ fastQ = new FastQ(outFastQfile, true);
		for (SamRecord samRecord : getSamReader().readLines()) {
			if (!samRecord.isMapped() || (getNonUniq && !samRecord.isUniqueMapping())) {
				FastQRecord fastQRecord = samRecord.toFastQRecord();
				fastQ.writeFastQRecord(fastQRecord);
				fastQRecord = null;
			}
		}
		fastQ.close();
		close();
		return fastQ;
	}
	
	/** 添加group信息，如果已经有group信息了，则返回 */
	public SamFile addGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_rgroup", null);
		return addGroup(outFile, false, sampleID, LibraryName, SampleName, Platform);
	}
	/** 添加group信息，如果已经有group信息了，则返回 */
	public SamFile addGroup(String outFileName, String sampleID, String LibraryName, String SampleName, String Platform) {
		return addGroup(outFileName, false, sampleID, LibraryName, SampleName, Platform);
	}
	 /**
	  * 添加group信息，如果已经有group信息了，则覆盖
	  */
	public SamFile addGroupOverlap(String sampleID, String LibraryName, String SampleName, String Platform) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_rgroup", null);
		return addGroup(outFile, true, sampleID, LibraryName, SampleName, Platform);
	}
	 /**
	  * 添加group信息，如果已经有group信息了，则覆盖
	  */
	public SamFile addGroupOverlap(String outFileName, String sampleID, String LibraryName, String SampleName, String Platform) {
		return addGroup(outFileName, true, sampleID, LibraryName, SampleName, Platform);
	}
	

	 /**
	  * 添加group信息，如果已经有group信息了，则返回
	  * @param outFile
	  */
	private SamFile addGroup(String outFile, boolean overlap, String sampleID, String LibraryName, String SampleName, String Platform) {
		boolean isAddGroup = false;
		if (overlap) {
			isAddGroup = true;
		} else {
			List<SAMReadGroupRecord> lSamRGroups = getHeader().getReadGroups();
			Set<String> setGroupID = new HashSet<String>();
			for (SAMReadGroupRecord samReadGroupRecord : lSamRGroups) {
				setGroupID.add(samReadGroupRecord.getId());
			}
			int i = 0;
			for (SamRecord samRecord : readLines()) {
				if (i > 100000) {
					break;
				}
				if (samRecord.getReadGroup() == null || !setGroupID.contains(samRecord.getReadGroup().getID())) {
					isAddGroup = true;
					break;
				}
				i++;
			}
		}
		if (!isAddGroup) {
			close();
			return this;
		}
		SamRGroup samRGroup = new SamRGroup(sampleID, LibraryName, SampleName, Platform);
		return setGroup(outFile, samRGroup);
	}
	
	 /**
	  * 添加group信息
	  * @param outFile
	  */
	public SamFile setGroup(String outFile, SamRGroup samRGroup) {
		FileOperate.createFolders(FileOperate.getPathName(outFile));
		List<SAMReadGroupRecord> lsReadGroupRecords = new ArrayList<SAMReadGroupRecord>();
		lsReadGroupRecords.add(samRGroup.getSamReadGroupRecord());
		
		SAMFileHeader samHeader = getHeader();
		samHeader.setReadGroups(lsReadGroupRecords);
		
		SamFile samFileWrite = new SamFile(outFile, getHeader());
		for (SamRecord samRecord : readLines()) {
			samRecord.setReadGroup(samRGroup);
			samFileWrite.writeSamRecord(samRecord);
		}
		samFileWrite.close();
		setParamSamFile(samFileWrite);
		samFileWrite.read = true;
		return samFileWrite;
	}
	
    public SamFile sort() {
    	return sort(false);
    }
    
    public static boolean isSorted(SamFile samFile) {
    	if (samFile.getFileName() != null && samFile.getFileName().endsWith("sorted.bam")) {
			return true;
		} else if (samFile.getHeader().getSortOrder() == SortOrder.coordinate) {
			samFile.close();
			return true;
		}
    	return false;
    }
    
    /** 是否在过滤的同时去除非uniquemapped reads */
    public SamFile sort(boolean isFilterUnique) {
    	if (isSorted(this)) {
			return this;
		}
    	
    	String outName = FileOperate.changeFileSuffix(getFileName(), ".sorted", "bam");
    	return sort(outName, isFilterUnique);
    }
    
    public SamFile sortByChrIds(SAMSequenceDictionary samSequenceDictionary, boolean isFilterUnique) {
    	if (isSorted(this)) {
			return this;
		}
    	
    	String outName = FileOperate.changeFileSuffix(getFileName(), ".sorted", "bam");
    	return sort(samSequenceDictionary, outName, isFilterUnique);
    }
	 /**
	  * 排序，输出为bam形式
	  * @param outFile
	  */
    public SamFile sort(String outFile, boolean isFilterUnique) {
		return sort(null, outFile, isFilterUnique);
	}
    
	 /**
	  * 排序，输出为bam形式
	  * @param lsChrId 如果有lsChrId存在，会根据该顺序来调整samHeader中的顺序
	  * @param outFile
	  * @return
	  */
   public SamFile sort(SAMSequenceDictionary samSequenceDictionary, String outFile, boolean isFilterUnique) {
		BamSort bamSort = new BamSort();
		bamSort.setSamSequenceDictionary(samSequenceDictionary);
		if (!bamFile) {
			SamFile bamFile = convertToBam(false);
			bamSort.setSamFile(bamFile);
		} else {
			bamSort.setSamFile(this);
		}

		String outSortedBamName = bamSort.sortJava(outFile, isFilterUnique);
		SamFile samFile = new SamFile(outSortedBamName);
		setParamSamFile(samFile);
		samFile.read = true;
		return samFile;
	}
   /** 用当前samFile的参数，来设定 输入的samFile参数 */
    protected void setParamSamFile(SamFile samFile) {
    	samFile.referenceFileName = referenceFileName;
    	samFile.isRealigned = isRealigned;
    	samFile.read = read;
    }
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 * @param addMultiHitFlag 是否添加比对到多个位置的flag，目前仅bowtie2需要该功能
	 */
	public SamFile convertToBam(boolean addMultiHitFlag) {
		return convertToBam(new ArrayList<AlignmentRecorder>(), addMultiHitFlag);
	}
	
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 * @param lsAlignmentRecorders
	 * @param addMultiHitFlag 是否添加比对到多个位置的flag，目前仅bowtie2需要该功能
	 * 因为convertToBam的时候会遍历Bam文件，所以可以添加一系列这种监听器，同时进行一系列的统计工作
	 */
	public SamFile convertToBam(List<AlignmentRecorder> lsAlignmentRecorders, boolean addMultiHitFlag) {
		String outName = FileOperate.changeFilePrefix(getFileName(), "", "bam");
		return convertToBam(lsAlignmentRecorders, outName, addMultiHitFlag);
	}
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 * @param lsAlignmentRecorders 因为convertToBam的时候会遍历Bam文件，所以可以添加一系列这种监听器，同时进行一系列的统计工作
	 * @param outFile 输出文件
	 * @param addMultiHitFlag 是否添加比对到多个位置的flag，目前仅bowtie2需要该功能
	 */
	public SamFile convertToBam(List<AlignmentRecorder> lsAlignmentRecorders, String outFile, boolean addMultiHitFlag) {
		if (bamFile) {
			return this;
		}
		if (!outFile.endsWith(".bam")) {
			FileOperate.changeFileSuffix(outFile, "", ".bam");
		}
		SamToBamSort samToBam = new SamToBamSort(outFile, this, isPairend());
		samToBam.setAddMultiHitFlag(addMultiHitFlag);
		samToBam.setLsAlignmentRecorders(lsAlignmentRecorders);
		samToBam.convertAndFinish();
		return samToBam.getSamFileBam();
	}
	/**
	 * 待检查
	 */
	public String indexMake() {
		if (FileOperate.isFileExistAndBigThanSize(getFileName() + ".bai",10)) {
			return getFileName() + ".bai";
		}
		BamIndex bamIndex = new BamIndex(this);
//		bamIndex.setExePath(softWareInfoSamtools.getExePath());
		String index = bamIndex.index();
		FormatSeq formatSeq = FormatSeq.BAM;
		if (!bamFile) {
			formatSeq = FormatSeq.SAM;
		}
		setSamFileRead(formatSeq, getFileName(), index);
		bamIndex = null;
		return index;
	}

	/**
	 * 获得该bam文件中染色体的长度信息，注意key都为小写
	 * @return
	 */
	public HashMap<String, Long> getMapChrIDLowcase2Length() {
		return getSamReader().getMapChrIDlowCase2Length();
	}
	public Map<String, Long> getMapChrID2Length() {
		return getSamReader().getMapChrID2Length();
	}
	
	/** sam文件的chr和输入的reference是不是同一个 */
	public boolean isSameChr() {
		Map<String, Long> mapChrID2LenSam = getMapChrID2Length();
		SeqHash seqHash = new SeqHash(referenceFileName);
		Map<String, Long> mapChrID2LenSeq = seqHash.getMapChrLength();
		seqHash.close();
		return mapChrID2LenSeq.equals(mapChrID2LenSam);
	}
	/**
	 * tobe checked
	 * 返回双端，如果是单端文件，则返回延长的单端
	 * 将sam文件改为bed文件，根据mapping质量和正反向进行筛选
	 */
	public BedFile sam2bedPairEnd(String bedFileCompType, String bedFile) {
		//TODO
		return null;
	}
	/** 注意如果外面samRecord修改了chrID，那么这个samRecord的head也要重新设定 */
	public void writeSamRecord(SamRecord samRecord) {
		samWriter.writeToSamFileln(samRecord);
	}
	
	/** 注意如果外面samRecord修改了chrID，那么这个samRecord的head也要重新设定 */
	public void writeSamRecord(SAMRecord samRecord) {
		samWriter.writeToSamFileln(samRecord);
	}
	
	public void close() {
		if (samReader != null) {
			samReader.close();
		}
		if (samWriter != null) {
			samWriter.close();
		}
		String bamFile = getFileName();
		if (bamFile == null) {
			return;
		}
		String bamindex = bamFile + ".bai";
		if (!bamFile.toLowerCase().endsWith("bam") || !FileOperate.isFileExistAndBigThanSize(bamindex, 0)) {
			bamindex = null;
		}
		
		samReader = new SamReader(bamFile, bamindex);
		read = true;
	}
	
	/**
	 * 从含有序列的bed文件获得fastQ文件，<b>注意如果是bowtie的结果，bam文件务必没有排过序</b><br>
	 * @param outFileName fastQ文件全名（包括路径）
	 */
	public FastQ getFastQ() {
		String outFileName = FileOperate.changeFileSuffix(getFileName(), "", "fastq.gz");
		return getFastQ(outFileName);
	}
	/**
	 * 从含有序列的bed文件获得fastQ文件，<b>注意如果是bowtie的结果，bam文件务必没有排过序</b><br>
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ(String outFileName) {
		FastQ fastQ = new FastQ(outFileName, true);
		int i = 1;
		for (SamRecord samRecord : readLines()) {
			FastQRecord fastQRecord = null;
			if (samRecord.getMappedReadsWeight() == 1) {
				fastQRecord = samRecord.toFastQRecord();
			} else {
				if (i == 1) {
					fastQRecord = samRecord.toFastQRecord();
					i++;
				} else if (i < samRecord.getMappedReadsWeight()) {
					i++;
					continue;
				} else if (i == samRecord.getMappedReadsWeight()) {
					i = 1;
					continue;
				}
			}
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.close();
		return fastQ;
	}
	
	public static SamFile mergeBamFile(String outBamFile, Collection<SamFile> lsBamFile) {
		BamMergeInt bamMerge = new BamMergeJava();
		ArrayList<String> lsBamFileName = new ArrayList<String>();
		for (SamFile samFile : lsBamFile) {
			lsBamFileName.add(samFile.getFileName());
		}
//		initialSoftWare();
//		bamMerge.setExePath(softWareInfoSamtools.getExePath());
		bamMerge.setOutFileName(outBamFile);
		bamMerge.setLsBamFile(lsBamFileName);
		return bamMerge.mergeSam();
	}
	
	/** 返回是sam文件，bam文件还是未知文件 */
	public static FormatSeq isSamBamFile(String samBamFile) {
		FormatSeq thisFormate = FormatSeq.UNKNOWN;
		if (!FileOperate.isFileExist(samBamFile)) {
			return thisFormate;
		}
		SamReader samReader = new SamReader(samBamFile);
		samReader.initial();
		if (!samReader.isSamBamFile()) {
			return thisFormate;
		}
		thisFormate = FormatSeq.SAM;
		if (samBamFile.endsWith("bam")) {
			thisFormate = FormatSeq.BAM;
		}
		samReader.close();
		return thisFormate;
	}
    /**
     * @param stream stream.markSupported() must be true
     * @return true if this looks like a BAM file.
     */
    private static boolean isBAMFile(final InputStream stream) throws IOException {
        if (!BlockCompressedInputStream.isValidFile(stream)) {
          return false;
        }
        final int buffSize = BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE;
        stream.mark(buffSize);
        final byte[] buffer = new byte[buffSize];
        readBytes(stream, buffer, 0, buffSize);
        stream.reset();
        final byte[] magicBuf = new byte[4];
        final int magicLength = readBytes(new BlockCompressedInputStream(new ByteArrayInputStream(buffer)), magicBuf, 0, 4);
        return magicLength == BAMFileConstants.BAM_MAGIC.length && Arrays.equals(BAMFileConstants.BAM_MAGIC, magicBuf);
    }
    
    private static int readBytes(final InputStream stream, final byte[] buffer, final int offset, final int length)
    		throws IOException {
    	int bytesRead = 0;
    	while (bytesRead < length) {
    		final int count = stream.read(buffer, offset + bytesRead, length - bytesRead);
    		if (count <= 0) {
    			break;
    		}
    		bytesRead += count;
    	}
    	return bytesRead;
    }
    
}
/**
 * Constants used in reading & writing BAM files
 */
class BAMFileConstants {
    /** The beginning of a BAMRecord is a fixed-size block of 8 int32s */
    static final int FIXED_BLOCK_SIZE = 8 * 4;
    /**
     * BAM file magic number.  This is what is present in the gunzipped version of the file,
     * which never exists on disk.
     */
    static final byte[] BAM_MAGIC = "BAM\1".getBytes();
    /** BAM index file magic number. */
    static final byte[] BAM_INDEX_MAGIC = "BAI\1".getBytes();
}
