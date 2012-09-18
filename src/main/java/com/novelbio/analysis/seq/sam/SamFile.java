package com.novelbio.analysis.seq.sam;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeqReader;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

import net.sf.picard.filter.FilteringIterator;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedStreamConstants;
import net.sf.samtools.util.IOUtil;
/**
 * 提取为bed文件时，仅仅考虑f-r情况
 * 待检查，特别是转换成bed文件时是否精确到了1bp，譬如起点是否为开区间
 * @author zong0jie
 *
 */
public class SamFile implements AlignSeqReader {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winF/NBC/Project/Project_HXW/20120705/aaa.sam");
		samFile.setReferenceFileName("/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		samFile.copeSamFile2Snp();
	}
	
	
	public static final int MAPPING_ALLREADS = 2;
	public static final int MAPPING_ALLMAPPEDREADS = 4;
	public static final int MAPPING_UNMAPPED = 8;
	public static final int MAPPING_UNIQUE = 16;
	public static final int MAPPING_REPEAT = 32;
	
	Logger logger = Logger.getLogger(SamFile.class);
	String fileName = "";
	/**
	 * 非unique mapping的序列是否只随机抽取一条
	 * @param notUniqueRandomSelectReads
	 */
	boolean uniqueRandomSelectReads = true;
	/** mapping质量为0 */
	int mapQualityFilter = 13;
	/**
	 * 读取sam文件的类，最好不要直接用，用getSamFileReader()方法代替
	 */
	SamReader samReader = new SamReader();
	SAMFileWriter samFileWriter;
	
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	/** 是否为bam文件 */
	boolean bamFile = false;
	
	boolean uniqMapping = false;
	boolean isRealigned = false;
	
	SamFileStatistics samFileStatistics;
	
	String referenceFileName;
	
	public SamFile() {}
	/**读取已有文件 */
	public SamFile(String samBamFile) {
		setSamFileRead(samBamFile);
	}
	/** 比对到的reference的文件名 */
	public void setReferenceFileName(String referenceFileName) {
		this.referenceFileName = referenceFileName;
	}
	/** 
	 * 创建新的sam文件
	 * @param samFileHeader
	 * @param samFileCreate
	 * @param sorted 输入的文件是否经过排序
	 */
	public void setSamFileNew(SAMFileHeader samFileHeader, String samFileCreate, boolean sorted) {
		this.fileName = samFileCreate;
		SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
		samFileWriter = samFileWriterFactory.makeSAMOrBAMWriter(samFileHeader, sorted, new File(samFileCreate));
	}
	public void setSamFileRead(String samFileExist) {
		this.fileName = samFileExist;
		File file = new File(samFileExist);
		
		final BufferedInputStream bufferedStream;
		if (file != null) {
			try {
				bufferedStream = new BufferedInputStream(new FileInputStream(file));
				this.bamFile = isBAMFile(bufferedStream);
				bufferedStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		samReader.setFileName(samFileExist);
	}
	public String getFileName() {
		return fileName;
	}
	public String getName() {
		return samReader.getName();
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
		return readLines().iterator().next();
	}
	/**
	 * 注意大小写区分
	 * @param ReadName reads的名字，只要写关键词就行了
	 * @return 没找到就返回null
	 */
	public SamRecord getReads(String ReadName) {
		return samReader.getReads(ReadName);
	}
	public SAMFileHeader getHeader() {
		return samReader.getsamfilehead();
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
		SamFile samFile = new SamFile();
		SAMFileHeader samFileHeader = samReader.getsamfilehead();
		samFile.setSamFileNew(samFileHeader, outSamFile, true);
		
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
    /**
     * @param stream stream.markSupported() must be true
     * @return true if this looks like a BAM file.
     */
    private boolean isBAMFile(final InputStream stream)
            throws IOException {
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
    public SamFile sort() {
    	String outName = FileOperate.changeFileSuffix(getFileName(), "_sorted", "bam");
    	return sort(outName);
    }
	 /**
	  * 排序，输出为bam形式
	  * @param outFile
	  */
    public SamFile sort(String outFile) {
    	String bamFileName = "";
    	if (!bamFile) {
    		SamFile bamFile = convertToBam();
    		bamFileName = bamFile.getFileName();
		}
		else {
			bamFileName = getFileName();
		}
		
		BamSort bamSort = new BamSort();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.samtools);
		bamSort.setExePath(softWareInfo.getExePath());
		bamSort.setBamFile(bamFileName);
		String outSortedBamName = bamSort.sort(outFile);
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
	 * 还没实现
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 */
	public SamFile convertToBam() {
		String outName = FileOperate.changeFilePrefix(fileName, "", "bam");
		return convertToBam(outName);
	}
	/**
	 * 还没实现
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 */
	public SamFile convertToBam(String outFile) {
		if (bamFile) {
			return this;
		}
		SamToBam samToBam = new SamToBam();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.samtools);
		samToBam.setExePath(softWareInfo.getExePath());
		samToBam.setSamFile(fileName);
		String fileOutName = samToBam.convertToBam(outFile);
		SamFile samFile = new SamFile(fileOutName);
		setParamSamFile(samFile);
		return samFile;
	}
	/**
	 * 待检查
	 */
	public void index() {
		BamIndex bamIndex = new BamIndex();
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.samtools);
		bamIndex.setExePath(softWareInfo.getExePath());
		bamIndex.setBamFile(getFileName());
		bamIndex.index();
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
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.GATK);
		bamRealign.setExePath(softWareInfo.getExePath());
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
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.GATK);
		bamRecalibrate.setExePath(softWareInfo.getExePath());
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
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.picard);
		bamRemoveDuplicate.setExePath(softWareInfo.getExePath());
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
		if (samFile.getFileName().endsWith("sorted.bam"))
			samFileSort = samFile;
		else
			samFileSort = samFile.sort();
		
//		FileOperate.delFile(samFile.getFileName());
		
		samFileSort.index();
		SamFile samFileRealign = samFileSort.realign();
//		FileOperate.delFile(samFileSort.getFileName());
		
		SamFile samFileRemoveDuplicate = samFileRealign.removeDuplicate();
//		FileOperate.delFile(samFileRealign.getFileName());
		
		samFileRemoveDuplicate.index();
		
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
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.samtools);
		bamPileup.setExePath(softWareInfo.getExePath());
		bamPileup.pileup(outPileUpFile);
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
		for (SamRecord samRecord : samReader.readLines()) {
			if (!samRecord.isMapped() || samRecord.getMapQuality() < mapQualityFilter
					|| (uniqMapping && !samRecord.isUniqueMapping()) ) {
				continue;
			}
			if (uniqueRandomSelectReads) {
				BedRecord bedRecord = samRecord.toBedRecordSE();
				bedSeq.writeBedRecord(bedRecord);
			}
			else {
				ArrayList<BedRecord> lsBedRecord = samRecord.toBedRecordSELs();
				for (BedRecord bedRecord : lsBedRecord) {
					bedSeq.writeBedRecord(bedRecord);
				}
			}
		}
		bedSeq.closeWrite();
		close();
		return bedSeq;
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
	public void writeSamRecord(SamRecord samRecord) {
		if (samRecord == null) {
			return;
		}
		samFileWriter.addAlignment(samRecord.getSamRecord());
	}
	public void close() {
		samReader.close();
		try { samFileWriter.close(); } catch (Exception e) { }
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