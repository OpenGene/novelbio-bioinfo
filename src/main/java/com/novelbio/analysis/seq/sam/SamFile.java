package com.novelbio.analysis.seq.sam;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SAMTextHeaderCodec;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedStreamConstants;
import net.sf.samtools.util.StringLineReader;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 提取为bed文件时，仅仅考虑f-r情况
 * 待检查，特别是转换成bed文件时是否精确到了1bp，譬如起点是否为开区间
 * @author zong0jie
 *
 */
public class SamFile implements AlignSeq {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winF/NBC/Project/Project_FY/paper/KOod.bam");
		samFile.close();
		for (SamRecord samRecord : samFile.readLines()) {
			System.out.println(samRecord);
		}
	}
	private static Logger logger = Logger.getLogger(SamFile.class);

	public static final int MAPPING_ALLREADS = 2;
	public static final int MAPPING_ALLMAPPEDREADS = 4;
	public static final int MAPPING_UNMAPPED = 8;
	public static final int MAPPING_UNIQUE = 16;
	public static final int MAPPING_REPEAT = 32;
	
	static SoftWareInfo softWareInfoSamtools = new SoftWareInfo();
	static SoftWareInfo softWareInfoGATK = new SoftWareInfo();
	static SoftWareInfo softWareInfoPicard = new SoftWareInfo();
	
	String fileName = "";
	/**
	 * 非unique mapping的序列是否只随机抽取一条
	 * @param notUniqueRandomSelectReads
	 */
	boolean uniqueRandomSelectReads = true;
	/** mapping质量为0 */
	int mapQualityFilter = 0;
	/**
	 * 读取sam文件的类，最好不要直接用，用getSamFileReader()方法代替
	 */
	SamReader samReader = new SamReader();
//	SAMFileWriter samFileWriter;
	SamWriter samWriter;
	
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	/** 是否为bam文件 */
	boolean bamFile = false;
	
	boolean uniqMapping = false;
	boolean isRealigned = false;
	
	SamFileStatistics samFileStatistics;
	
	String referenceFileName;
	
	/**读取已有文件
	 * 如果有索引会自动读取索引
	 */
	public SamFile(String samBamFile) {
		setSamFileRead(samBamFile);
	}
	/** 创建新的sambam文件，根据文件名
	 * 默认输入的序列没有经过排序
	 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader) {
		setSamFileNew(samFileHeader, samBamFile, false);
		initialSoftWare();
	}
	/** 创建新的sambam文件，根据文件名 */
	public SamFile(String samBamFile, SAMFileHeader samFileHeader, boolean preSorted) {
		setSamFileNew(samFileHeader, samBamFile, preSorted);
		initialSoftWare();
	}
	private void setSamFileRead(String samBamFile) {
		String bamindex = samBamFile + ".bai";
		if (!FileOperate.isFileExistAndBigThanSize(samBamFile, 0)) {
			bamindex = null;
		}
		setSamFileRead(samBamFile, bamindex);
		initialSoftWare();
	}
	
	private void setSamFileRead(String samFileExist, String fileIndex) {
		this.fileName = samFileExist;
		FormatSeq formatSeq = isSamBamFile(samFileExist);
		if (formatSeq == FormatSeq.UNKNOWN) {
			return;
		}
		if (formatSeq == FormatSeq.BAM) {
			bamFile = true;
		}
		samReader.setFileName(samFileExist);
		samReader.setFileIndex(fileIndex);
	}
	/** 
	 * 创建新的sam文件
	 * @param samFileHeader
	 * @param samFileCreate
	 * @param preSorted 输入的文件是否经过排序
	 */
	private void setSamFileNew(SAMFileHeader samFileHeader, String samFileCreate, boolean preSorted) {
		this.fileName = samFileCreate;
		samWriter = new SamWriter(preSorted, samFileHeader, samFileCreate);
	}
	
	private static void initialSoftWare() {
		try {
			if (softWareInfoSamtools.getName() == null) {
				softWareInfoSamtools.setName(SoftWare.samtools);
			}
			if (softWareInfoGATK.getName() == null) {
				softWareInfoGATK.setName(SoftWare.GATK);
			}
			if (softWareInfoPicard.getName() == null) {
				softWareInfoPicard.setName(SoftWare.picard);
			}
		} catch (Exception e) {}

	}
	/** 比对到的reference的文件名 */
	public void setReferenceFileName(String referenceFileName) {
		this.referenceFileName = referenceFileName;
		faidxRefsequence();
	}
	
	private String faidxRefsequence() {
		if (FileOperate.isFileExist(referenceFileName) && !FileOperate.isFileExist(referenceFileName+".fai")) {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
			samIndexRefsequence.setExePath(softWareInfoSamtools.getExePath());
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
		return fileName;
	}
	
	/**
	 * 是否为uniqMapping，默认为true
	 * @param uniqMapping
	 */
	public void setUniqMapping(boolean uniqMapping) {
		this.uniqMapping = uniqMapping;
	}
	/**
	 * 非unique mapping的序列是否只随机抽取一条
	 * @param notUniqueRandomSelectReads
	 */
	public void setUniqueRandomSelectOneRead(boolean uniqueRandomSelectReads) {
		this.uniqueRandomSelectReads = uniqueRandomSelectReads;
	}
	/**
	 * 双端数据是否获得连在一起的bed文件
	 * 如果输入是单端数据，则将序列延长返回bed文件
	 * 注意：如果是双端文件，<b>不能预先排序</b>
	 * @param getPairedBed
	 */
	public boolean isPairend() {
		return samReader.isPairend();
	}
	//TODO 未实现
	/**
	 * 未实现
	 * @param pairendExtend
	 * @param mapQuality
	 * @param uniqMapping
	 */
	public void setBedInfo(boolean pairendExtend, int mapQuality, int uniqMapping) {
		
	}
	/**
	 * 默认为10，也可设定为0
	 * @param mapQuality
	 */
	public void setMapQuality(int mapQuality) {
		this.mapQualityFilter = mapQuality;
	}


	public SamFileStatistics getStatistics() {
		samFileStatistics = new SamFileStatistics();
		samFileStatistics.setSamFile(this);
		samFileStatistics.statistics();
		return samFileStatistics;
	}
	
	/** 
	 * the alignment of the returned SAMRecords need only overlap the interval of interest.
	 * @param chrID 无所谓大小写
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesOverlap(String chrID, int start, int end) {
		return samReader.readLinesOverlap(chrID, start, end);
	}
	/**
	 * each SAMRecord returned is will have its alignment completely contained in the interval of interest. 
	 * @param chrID 无所谓大小写
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesContained(String chrID, int start, int end) {
		return samReader.readLinesContained(chrID, start, end);
	}
	
	public Iterable<SamRecord> readLines() {
		return samReader.readLines();
	}
	public Iterable<SamRecord> readLines(int num) {
		return samReader.readLines(num);
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
		return samReader.getReads(ReadName);
	}
	
	/** 默认不排序 */
	public SAMFileHeader getHeader() {
		return getHeader(false);
	}
	
	/** 是否需要排序 */
	public SAMFileHeader getHeader(boolean isNeedSort) {
		SAMFileHeader samFileHeader = samReader.getSamFileHead();
		if (isNeedSort) {
			samFileHeader.setSortOrder(SAMFileHeader.SortOrder.coordinate);
			PathDetail.setTmpDir(FileOperate.getParentPathName(getFileName()));
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
		for (SamRecord samRecord : samReader.readLines()) {
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
	public SamFile getSingleUnMappedReads() {
		String out = FileOperate.changeFileSuffix(fileName, "_SingleFile", null);
		return getSingleUnMappedReads(out);
	}

	/**
	 * 将那种一头mapping上，一头没有mapping上的序列，两头都提取出来写入一个sam文件
	 */
	public SamFile getSingleUnMappedReads(String outSamFile) {
		if (!isPairend()) {
			return null;
		}
	
		SAMFileHeader samFileHeader = samReader.getSamFileHead();
		SamFile samFile = new SamFile(outSamFile, samFileHeader);
		LinkedHashMap<String, SamRecord> mapName2Record = new LinkedHashMap<String, SamRecord>();
		for (SamRecord samRecord : samReader.readLines()) {
			if (!samRecord.isHavePairEnd()) {
				continue;
			}
			//将一对samRecord写入文件
			if (mapName2Record.containsKey(samRecord.getName())) {
				SamRecord samRecord1 = mapName2Record.get(samRecord.getName());
				if (samRecord1.isPaireReads(samRecord)) {
					samFile.writeSamRecord(samRecord1);
					samFile.writeSamRecord(samRecord);
					mapName2Record.remove(samRecord.getName());
					continue;
				}
			}
			//找出一个mapping一个没有mapping的记录
			if (samRecord.isMapped() ^ samRecord.isMateMapped() ) {
				mapName2Record.put(samRecord.getName(), samRecord);
			}
			removeMap(5000, mapName2Record);
		}
		samFile.close();
		return samFile;
	}
	/** 将多的序列删除，以节约内存 */
	private void removeMap(int remainNum, LinkedHashMap<String, SamRecord> mapName2Record) {
		if (mapName2Record.size() <= remainNum) {
			return;
		}
		int num = mapName2Record.size() - remainNum;
		int count = 0;
		ArrayList<String> lsName = new ArrayList<String>();
		for (String recordName : mapName2Record.keySet()) {
			if (count > num) {
				break;
			}
			lsName.add(recordName);
			count++;
		}
		for (String recordName : lsName) {
			mapName2Record.remove(recordName);
		}
	}

    public SamFile sort() {
    	SamFile samFile = convertToBam();
    	if (samFile.getFileName().endsWith("_sorted.bam")) {
			return samFile;
		}
    	String outName = FileOperate.changeFileSuffix(getFileName(), "_sorted", "bam");
    	return samFile.sort(outName);
    }
	 /**
	  * 排序，输出为bam形式
	  * @param outFile
	  */
    public SamFile sort(String outFile) {
		BamSort bamSort = new BamSort();
    	if (!bamFile) {
    		SamFile bamFile = convertToBam();
    		bamSort.setSamFile(bamFile);
    	} else {
    		bamSort.setSamFile(this);
		}

    	String outSortedBamName = bamSort.sortSamtools(outFile);
		SamFile samFile = new SamFile(outSortedBamName);
		setParamSamFile(samFile);
		return samFile;
	}
    
    private void setParamSamFile(SamFile samFile) {
    	samFile.mapQualityFilter = mapQualityFilter;
    	samFile.referenceFileName = referenceFileName;
    	samFile.uniqMapping = uniqMapping;
    	samFile.uniqueRandomSelectReads = uniqueRandomSelectReads;
    	samFile.isRealigned = isRealigned;
    }
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 */
	public SamFile convertToBam() {
		return convertToBam(new ArrayList<AlignmentRecorder>());
	}
	
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 * @param lsAlignmentRecorders
	 * 因为convertToBam的时候会遍历Bam文件，所以可以添加一系列这种监听器，同时进行一系列的统计工作
	 */
	public SamFile convertToBam(List<AlignmentRecorder> lsAlignmentRecorders) {
		String outName = FileOperate.changeFilePrefix(fileName, "", "bam");
		return convertToBam(lsAlignmentRecorders, outName);
	}
	/**
	 * 用samtools实现了
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 * @param lsAlignmentRecorders 因为convertToBam的时候会遍历Bam文件，所以可以添加一系列这种监听器，同时进行一系列的统计工作
	 * @param outFile 输出文件
	 */
	public SamFile convertToBam(List<AlignmentRecorder> lsAlignmentRecorders, String outFile) {
		if (bamFile) {
			return this;
		}
		if (!outFile.endsWith(".bam")) {
			FileOperate.changeFileSuffix(outFile, "", ".bam");
		}
		
		SamFile samFile = new SamFile(outFile, getHeader());
		for (SamRecord samRecord : readLines()) {
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				try {
					alignmentRecorder.addAlignRecord(samRecord);
				} catch (Exception e) { }
				
			}
			samFile.writeSamRecord(samRecord);
		}
		close();
		samFile.setSamFileRead(samFile.fileName);
		samFile.close();

//		SamToBam samToBam = new SamToBam();
//		samToBam.setExePath(softWareInfoSamtools.getExePath());
//		samToBam.setSamFile(fileName);
//		samToBam.setSeqFai(faidxRefsequence());
//		String fileOutName = samToBam.convertToBam(outFile);
//		SamFile samFile = new SamFile(fileOutName);
//
//		setParamSamFile(samFile);
		return samFile;
	}
	/**
	 * 待检查
	 */
	public void indexMake() {
		if (FileOperate.isFileExist(getFileName() + ".bai")) {
			return;
		}
		BamIndex bamIndex = new BamIndex(this);
		bamIndex.setExePath(softWareInfoSamtools.getExePath());
		bamIndex.setBamFile(getFileName());
		String index = bamIndex.index();
		samReader.setFileIndex(index);
		bamIndex = null;
	}
	public SamFile realign() {
		String outFile = FileOperate.changeFileSuffix(fileName, "_realign", "bam");
		return realign(outFile);
	}
	
	/**
	 * 待检查
	 */
	public SamFile realign(String outFile) {
		BamRealign bamRealign = new BamRealign();
		bamRealign.setExePath(softWareInfoGATK.getExePath());
		bamRealign.setBamFile(getFileName());
		bamRealign.setRefSequenceFile(referenceFileName);
		String outSamFile = bamRealign.realign(outFile);
		SamFile samFile = new SamFile(outSamFile);
		setParamSamFile(samFile);
		samFile.isRealigned = true;
		return samFile;
	}
	
	public SamFile recalibrate() {
		String outFile = FileOperate.changeFileSuffix(fileName, "recalibrate", "bam");
		return recalibrate(outFile);
	}
	/**
	 * 待检查
	 */
	public SamFile recalibrate(String outFile) {
		BamRecalibrate bamRecalibrate = new BamRecalibrate();
		bamRecalibrate.setExePath(softWareInfoGATK.getExePath());
		bamRecalibrate.setBamFile(getFileName());
		bamRecalibrate.setRefSequenceFile(referenceFileName);
		String outSamFile = bamRecalibrate.reCalibrate(outFile);
		SamFile samFile = new SamFile(outSamFile);
		setParamSamFile(samFile);
		return samFile;
	}
	public SamFile removeDuplicate() {
		String outFile = FileOperate.changeFileSuffix(fileName, "_removeDuplicate", "bam");
		return removeDuplicate(outFile);
	}
	/**
	 * 待检查
	 */
	public SamFile removeDuplicate(String outFile) {
		BamRemoveDuplicate bamRemoveDuplicate = new BamRemoveDuplicate();
		bamRemoveDuplicate.setExePath(softWareInfoSamtools.getExePath());
		bamRemoveDuplicate.setBamFile(getFileName());
		String outSamFile = bamRemoveDuplicate.removeDuplicate(outFile);
		SamFile samFile = new SamFile(outSamFile);
		setParamSamFile(samFile);
		return samFile;
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
		SamFile samFile = convertToBam();
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
	public void pileup() {
		String pileupFile = FileOperate.changeFileSuffix(getFileName(), "_pileup", "gz");
		pileup(pileupFile);
	}
	
	public void pileup(String outPileUpFile) {
		SamFile bamFile = convertToBam();
		BamPileup bamPileup = new BamPileup();
		bamPileup.setBamFile(bamFile.getFileName());
		bamPileup.setMapQuality(mapQualityFilter);
		bamPileup.setReferenceFile(referenceFileName);
		bamPileup.setRealign(!isRealigned);
		bamPileup.setExePath(softWareInfoSamtools.getExePath());
		bamPileup.pileup(outPileUpFile);
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
			samRecord.setRefID(samRecord.getRefID().toLowerCase());
			samFileOut.writeSamRecord(samRecord);
		}
		samFileOut.close();
		return samFileOut;
	}
	
	public BedSeq toBedSingleEnd() {
		return toBedSingleEnd(TxtReadandWrite.TXT, FileOperate.changeFileSuffix(getFileName(), "", "bed"));
	}
	/**
	 * <b>没有考虑bed文件的起点是0还是1</b>
	 *<b>非uniq mapping只支持bwa的结果</b>
	 * 返回单端
	 * bed文件的score列为mapping quality
	 * <b>不能挑选跨染色体的融合基因</b>
	 * @param bedFileCompType bed文件的压缩格式，TxtReadandWrite.TXT等设定
	 * @param bedFile 最后产生的bedFile
	 * @param extend 是否延长bed文件
	 * @return
	 */
	public BedSeq toBedSingleEnd(String bedFileCompType, String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile, true);
		bedSeq.setCompressType(null, bedFileCompType);
		for (SamRecord samRecord : readLines()) {
			if (!samRecord.isMapped() || samRecord.getMapQuality() < mapQualityFilter
					|| (uniqMapping && !samRecord.isUniqueMapping()) ) {
				continue;
			}
			if (uniqueRandomSelectReads) {
				BedRecord bedRecord = samRecord.toBedRecordSE();
				bedSeq.writeBedRecord(bedRecord);
				bedRecord = null;
			} else {
				ArrayList<BedRecord> lsBedRecord = samRecord.toBedRecordSELs();
				for (BedRecord bedRecord : lsBedRecord) {
					bedSeq.writeBedRecord(bedRecord);
				}
				lsBedRecord = null;
			}
			samRecord = null;
		}
		bedSeq.closeWrite();
		close();
		return bedSeq;
	}
	
	/**
	 * 获得该bam文件中染色体的长度信息，注意key都为小写
	 * @return
	 */
	public HashMap<String, Long> getChrID2LengthMap() {
		return samReader.getMapChrIDlowCase2Length();
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
		samReader.close();
		if (samWriter != null) {
			samWriter.close();
		}
	}

	public static SamFile mergeBamFile(String outBamFile, Collection<SamFile> lsBamFile) {
		BamMerge bamMerge = new BamMerge();
		ArrayList<String> lsBamFileName = new ArrayList<String>();
		for (SamFile samFile : lsBamFile) {
			lsBamFileName.add(samFile.getFileName());
		}
		initialSoftWare();
		bamMerge.setExePath(softWareInfoSamtools.getExePath());
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
		SamReader samReader = new SamReader();
		samReader.setFileName(samBamFile);
		if (!samReader.isSamBamFile()) {
			return thisFormate;
		}
		samReader.close();
		thisFormate = FormatSeq.SAM;
		File file = new File(samBamFile);
		BufferedInputStream bufferedStream = null;
		InputStream instream = null;
		if (file != null) {
			try {
				instream = new FileInputStream(file);
				bufferedStream = new BufferedInputStream(instream);
				if(isBAMFile(bufferedStream)) {
					thisFormate = FormatSeq.BAM;
				}
				bufferedStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try { instream.close(); } catch (IOException e) { }
		try { bufferedStream.close(); } catch (IOException e) { }
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
	
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ() {
		String outFileName = FileOperate.changeFileSuffix(getFileName(), "", "fastq");
		return getFastQ(outFileName);
	}
	/**
	 * 从含有序列的bed文件获得fastQ文件
	 * @param outFileName fastQ文件全名（包括路径）
	 * @throws Exception
	 */
	public FastQ getFastQ(String outFileName) {
		FastQ fastQ = new FastQ(outFileName, true);
		String compressOutType = TxtReadandWrite.TXT;
		if (outFileName.endsWith("gz")) {
			compressOutType = TxtReadandWrite.GZIP;
		}
		fastQ.setCompressType(compressOutType, compressOutType);
		for (SamRecord samRecord : readLines()) {
			FastQRecord fastQRecord = new FastQRecord();
			fastQRecord.setName(samRecord.getName());
			fastQRecord.setFastaQuality(samRecord.getReadsQuality());
			fastQRecord.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
			fastQRecord.setSeq(samRecord.getSeqFasta().toString());
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.close();
		return fastQ;
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
