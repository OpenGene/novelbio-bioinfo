package com.novelbio.analysis.seq.sam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SAMTextHeaderCodec;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedStreamConstants;
import net.sf.samtools.util.StringLineReader;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.fasta.FastaDictMake;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 提取为bed文件时，仅仅考虑f-r情况
 * 待检查，特别是转换成bed文件时是否精确到了1bp，譬如起点是否为开区间
 * @author zong0jie
 *
 */
public class SamFile implements AlignSeq {
	private static final Logger logger = Logger.getLogger(SamFile.class);
	static {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
	}
	public static void main(String[] args) {
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		SamFile samFile = new SamFile("/media/hdfs/nbCloud/public/AllProject/project_53717abee4b074ba192cd6ec/task_5371957fe4b0a9f14de0fd59/DNASeqMap_result/YYG.bam");
		for (SamRecord samRecord : samFile.readLines()) {
			if (samRecord.getCIGAR().contains("H")) {
//				System.out.println(samRecord.getCIGAR() + "\t" + samRecord.getStartAbs() + "\t" + samRecord.getEndAbs() + "\t"+ samRecord.getSeqFasta().toString());
				System.out.print(samRecord.toString());
			}
		}
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
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		setSamFileRead(samBamFile);
	}
	/**直接读取流，不支持判定文件格式，不支持索引 */
	public SamFile(InputStream inputStream) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		setSamRead(inputStream);
	}
	/** 创建新的sambam文件，根据文件名
	 * 根据samFileHeader来定义默认输入的序列是否已经经过排序。
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		if (samFileHeader.getSortOrder() != SortOrder.unsorted) {
			setSamFileNew(samFileHeader, samBamFile, true);
		} else {
			setSamFileNew(samFileHeader, samBamFile, false);
		}
//		initialSoftWare();
	}

	/** 创建新的sambam文件'
	 * @param samBamFile
	 * @param samFileHeader
	 * @param preSorted 输入的文件是否已经排序了
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader, boolean preSorted) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		setSamFileNew(samFileHeader, samBamFile, preSorted);
//		initialSoftWare();
	}
	private void setSamFileRead(String samBamFile) {
		String bamindex = samBamFile + ".bai";
		if (!FileOperate.isFileExistAndBigThanSize(bamindex, 0)) {
			bamindex = null;
		}
		setSamFileRead(FormatSeq.UNKNOWN, samBamFile, bamindex);
//		initialSoftWare();
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
	
//	private static void initialSoftWare() {
//		try {
//			if (softWareInfoSamtools.getName() == null) {
//				softWareInfoSamtools.setName(SoftWare.samtools);
//			}
//		} catch (Exception e) {}
//
//	}
	
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
    	if (getFileName().endsWith("_sorted.bam")) {
			return this;
		}
    	String outName = FileOperate.changeFileSuffix(getFileName(), "_sorted", "bam");
    	return sort(outName);
    }
    public SamFile sortByChrIds(SAMSequenceDictionary samSequenceDictionary) {
    	if (getFileName().endsWith("_sorted.bam")) {
			return this;
		}
    	String outName = FileOperate.changeFileSuffix(getFileName(), "_sorted", "bam");
    	return sort(samSequenceDictionary, outName);
    }
	 /**
	  * 排序，输出为bam形式
	  * @param outFile
	  */
    public SamFile sort(String outFile) {
		return sort(null, outFile);
	}
    
	 /**
	  * 排序，输出为bam形式
	  * @param lsChrId 如果有lsChrId存在，会根据该顺序来调整samHeader中的顺序
	  * @param outFile
	  * @return
	  */
   public SamFile sort(SAMSequenceDictionary samSequenceDictionary, String outFile) {
		BamSort bamSort = new BamSort();
		bamSort.setSamSequenceDictionary(samSequenceDictionary);
		if (!bamFile) {
			SamFile bamFile = convertToBam(false);
			bamSort.setSamFile(bamFile);
		} else {
			bamSort.setSamFile(this);
		}

		String outSortedBamName = bamSort.sortJava(outFile);
		SamFile samFile = new SamFile(outSortedBamName);
		setParamSamFile(samFile);
		samFile.read = true;
		return samFile;
	}
    protected void setParamSamFile(SamFile samFile) {
    	samFile.referenceFileName = referenceFileName;
    	samFile.isRealigned = isRealigned;
    	samFile.bamFile = bamFile;
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
		SamToBamSort samToBam = new SamToBamSort(outFile, this);
		samToBam.setAddMultiHitFlag(addMultiHitFlag);
		samToBam.setLsAlignmentRecorders(lsAlignmentRecorders);
		samToBam.convert();
		close();
		return samToBam.getSamFileBam();
	}
	/**
	 * 待检查
	 */
	public void indexMake() {
		if (FileOperate.isFileExistAndBigThanSize(getFileName() + ".bai",10)) {
			return;
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
	}
	public SamFile realign() {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_realign", "bam");
		return realign(outFile);
	}
	
	/**
	 * 失败则返回null
	 * @param outFile
	 * @return
	 */
	public SamFile realign(String outFile) {
//		GATKRealign gatkRealign = new GATKRealign(getFileName(), referenceFileName, outFile);
		BamRealign bamRealign = new BamRealign();
		bamRealign.setBamFile(getFileName());
		bamRealign.setRefSequenceFile(referenceFileName);
		outFile = bamRealign.realign(outFile, false);
		if (FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			SamFile samFile = new SamFile(outFile);
			setParamSamFile(samFile);
			samFile.isRealigned = true;
			return samFile;
		}
		return null;
	}
	
	public SamFile recalibrate(List<String> lsVcfFile) {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_recalibrate", "bam");
		return recalibrate(lsVcfFile, outFile);
	}
	/**
	 * 待检查
	 */
	public SamFile recalibrate(List<String> lsVcfFile, String outFile) {
//		GATKRecalibrate gatkRecalibrate = new GATKRecalibrate(getFileName(), referenceFileName);
		BamRecalibrate bamRecalibrate = new BamRecalibrate();
		bamRecalibrate.setBamFile(getFileName());
		bamRecalibrate.setRefSequenceFile(referenceFileName);
		bamRecalibrate.setSnpVcfFile(lsVcfFile);
		outFile = bamRecalibrate.reCalibrate(outFile, false);
		if (FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			SamFile samFile = new SamFile(outFile);
			setParamSamFile(samFile);
			return samFile;
		}
		return null;
	}
	public SamFile removeDuplicate() {
		String outFile = FileOperate.changeFileSuffix(getFileName(), "_dedup", "bam");
		return removeDuplicate(outFile);
	}
	
	/** 出错就返回null */
	public SamFile removeDuplicate(String outFile) {
//		GATKDuplicate gatkDuplicate = new GATKDuplicate(getFileName(), outFile);
		BamRemoveDuplicate bamRemoveDuplicate = new BamRemoveDuplicate();
		bamRemoveDuplicate.setBamFile(getFileName());
		bamRemoveDuplicate.setSamtools(false);
		outFile = bamRemoveDuplicate.removeDuplicate(outFile);
		if (FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
			SamFile samFile = new SamFile(outFile);
			setParamSamFile(samFile);
			return samFile;
		}
		return null;
	}
	
	/**
	 * <b>首先设定reference</b>
	 * snp calling需要的一系列处理<br>
	 * 依次为：convert2bam<br>
	 * sort<br>
	 * index<br>
	 * Realign<br>
	 * RemoveDuplicate<br>
	 * index<br>
	 * recalibrate<br>
	 * index<br>
	 * @return
	 */
	public SamFile copeSamFile2Snp() {
		SamFile samFile = convertToBam(false);
//		FileOperate.delFile(getFileName());
		
		SamFile samFileSort = null;
		if (samFile.getFileName().contains("sort")) {
			samFileSort = samFile;
		} else {
			samFileSort = samFile.sort();
		}
//		FileOperate.delFile(samFile.getFileName());
		
		samFileSort.indexMake();
		SamFile samFileRealign = samFileSort.realign();
//		FileOperate.delFile(samFileSort.getFileName());
		SamFile samFileRemoveDuplicate = samFileRealign.removeDuplicate();
//		FileOperate.delFile(samFileRealign.getFileName());
		
		samFileRemoveDuplicate.indexMake();
		
		//recalibrate在没有snpdb的表的情况下做不了，那就不做了
//		SamFile samFileRecalibrate = samFileRemoveDuplicate.recalibrate();
////		FileOperate.delFile(samFileRemoveDuplicate.getFileName());
////		FileOperate.delFile(samFileRemoveDuplicate.getFileName()+".bai");
//		
//		samFileRecalibrate.index();
		return samFileRemoveDuplicate;
	}
	/**
	 * mapQualityFilter设定为10
	 * 可以用{@link BcfTools#snpcalling()}}的方法来进行snpCalling
	 */
	public String pileup() {
		String pileupFile = FileOperate.changeFileSuffix(getFileName(), "_pileup", "gz");
		pileup(pileupFile, 10);
		return pileupFile;
	}
	public void pileup(String outFileName) {
		pileup(outFileName, 10);
	}
	public void pileup(String outPileUpFile, int mapQualityFilter) {
		SamFile bamFile = convertToBam(false);
		BamPileup bamPileup = new BamPileup();
		bamPileup.setBamFile(bamFile.getFileName());
		bamPileup.setMapQuality(mapQualityFilter);
		bamPileup.setReferenceFile(referenceFileName);
		bamPileup.setRealign(!isRealigned);
//		bamPileup.setExePath(softWareInfoSamtools.getExePath());
		bamPileup.pileup(outPileUpFile, false);
	}
	
	/** 用GATK来call snp，同时返回结果vcf的文本名 */
	public String snpCalling(String dbSnpFile) {
		String vcfFileName = FileOperate.changeFileSuffix(getFileName(), "_GATKsnp", "vcf");
		return snpCalling(dbSnpFile, vcfFileName);
	}
	/**
	 * 用GATK来call snp，同时返回结果vcf的文本
	 * @param dbSnpFile dbsnp的vcf文件
	 */
	public String snpCalling(String dbSnpFile, String outSnpVcf) {
		outSnpVcf = FileOperate.changeFileSuffix(outSnpVcf, "", "vcf");
		GATKCalling gatkCalling = new GATKCalling(getFileName(), referenceFileName);
		gatkCalling.setOutputFilePath(outSnpVcf);
		gatkCalling.setSnpDBVcfFilePath(dbSnpFile);
		outSnpVcf = gatkCalling.callingByGATK();
		return outSnpVcf;
	}
	/**
	 * 把该samfile的refID都修正为小写字母
	 * @return
	 */
	public SamFile changeToLowcase() {
		SAMFileHeader samFileHeader = getHeader();
		boolean isLowcase = true;
		for (SAMSequenceRecord samSequenceRecord : samFileHeader.getSequenceDictionary().getSequences()) {
			if (!samSequenceRecord.getSequenceName().equals(samSequenceRecord.getSequenceName().toLowerCase())) {
				isLowcase = false;
				break;
			}
		}
		if (isLowcase) {
			return this;
		}
		
		String textHead = samFileHeader.getTextHeader();
		String[] ss = textHead.split("@SQ\t");
		for (int i = 1; i < ss.length; i++) {
			String[] ss2 = ss[i].split("\t");
			String[] ss3 = ss2[0].split(":");
			ss3[1] = ss3[1].toLowerCase();
			ss2[0] = ArrayOperate.cmbString(ss3, ":");
			ss[i] = ArrayOperate.cmbString(ss2, "\t");
		}
		textHead = ArrayOperate.cmbString(ss, "@SQ\t");
		SAMTextHeaderCodec headerCodec = new SAMTextHeaderCodec();
		SAMFileHeader mFileHeaderLowcase = headerCodec.decode(new StringLineReader(textHead), null);
		
		SamFile samFileOut = new SamFile(FileOperate.changeFileSuffix(getFileName(), "_Lowcase", null), mFileHeaderLowcase, mFileHeaderLowcase.getSortOrder() != SortOrder.unsorted);
		for (SamRecord samRecord : readLines()) {
			samRecord.setReferenceName(samRecord.getRefID().toLowerCase());
			samFileOut.writeSamRecord(samRecord);
		}
		samFileOut.close();
		return samFileOut;
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
	public BedSeq sam2bedPairEnd(String bedFileCompType, String bedFile) {
		//TODO
		return null;
	}
	/** 注意如果外面samRecord修改了chrID，那么这个samRecord的head也要重新设定 */
	public void writeSamRecord(SamRecord samRecord) {
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
	 * @throws Exception
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
		BamMerge bamMerge = new BamMerge();
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
