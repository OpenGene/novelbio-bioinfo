package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceRecord;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

public class SamReader {
	private static final Logger logger = Logger.getLogger(SamReader.class);
	boolean initial = false;
	/**
	 * 读取sam文件的类，最好不要直接用，用getSamFileReader()方法代替
	 */
	SAMFileReader samFileReader;
	SAMFileHeader samFileHeader;
	SAMRecordIterator samRecordIterator;
	/** 小写的chrID与samFileHeader中的chrID的对照表 */
	HashMap<String, String> mapChrIDlowCase2ChrID = new LinkedHashMap<String, String>();
	HashMap<String, Long> mapChrIDlowCase2Length = new LinkedHashMap<String, Long>();
	
	String fileName;
	String fileIndex;
	
	Boolean pairend;
		
	public SamReader(String fileName) {
		this.fileName = fileName;
	}
	
	public SamReader(String fileName, String fileIndex) {
		this.fileName = fileName;
		this.fileIndex = fileIndex;
	}
	
	public void setFileIndex(String fileIndex) {
		this.fileIndex = fileIndex;
	}
	
	public void initial() {
		if (!initial) {
			initialSamHeadAndReader(fileIndex);
			initial = true;
		}
	}
	/**
	 * 双端数据是否获得连在一起的bed文件
	 * 如果输入是单端数据，则将序列延长返回bed文件
	 * 注意：如果是双端文件，<b>不能预先排序</b>
	 * @param getPairedBed
	 */
	protected boolean isPairend() {
		if (pairend != null) {
			 return pairend;
		}
		int countAll = 1000;
		int countLines = 0;
		pairend = false;
		for (SamRecord samRecord : readLines()) {
			countLines++;
			if (countLines > countAll) {
				break;
			}
			if (samRecord.isHavePairEnd()) {
				pairend = true;
				break;
			}
		}
		return pairend;
	}
	
	private void initialSamHeadAndReader(String fileIndex) {
		if (samFileHeader != null && samFileReader != null
				&& 
				(
						(fileIndex == null && !samFileReader.hasIndex())
						|| 
						samFileReader.hasIndex()
						)
				) {
			return;
		}
		close();
		File file = new File(fileName);
		File index = null;
		if (fileIndex != null && FileOperate.isFileExistAndBigThanSize(fileIndex, 0)) {
			index = new File(fileIndex);
			samFileReader = new SAMFileReader(file, index);
		} else {
			samFileReader = new SAMFileReader(file);
		}
		samFileHeader = samFileReader.getFileHeader();
		mapChrIDlowCase2ChrID = new HashMap<String, String>();
		mapChrIDlowCase2Length = new HashMap<String, Long>();
		//获得reference的序列信息
		List<SAMSequenceRecord> lsSamSequenceRecords = samFileHeader.getSequenceDictionary().getSequences();
		for (SAMSequenceRecord samSequenceRecord : lsSamSequenceRecords) {
			mapChrIDlowCase2ChrID.put(samSequenceRecord.getSequenceName().toLowerCase(), samSequenceRecord.getSequenceName());
			mapChrIDlowCase2Length.put(samSequenceRecord.getSequenceName().toLowerCase(), (long) samSequenceRecord.getSequenceLength());
		}
	}
	
	protected SAMFileHeader getSamFileHead() {
		return samFileHeader;
	}
	
	protected SAMFileReader getSamFileReader() {
		return samFileReader;
	}
	
	/**
	 * 获得该bam文件中染色体的长度信息，注意key都为小写
	 * @return
	 */
	public HashMap<String, Long> getMapChrIDlowCase2Length() {
		return mapChrIDlowCase2Length;
	}
	/**
	 * 注意大小写区分
	 * @param ReadName reads的名字，只要写关键词就行了
	 * @return 没找到就返回null
	 */
	public SamRecord getReads(String ReadName) {
		for (SamRecord samRecord : readLines()) {
			if (samRecord.getName().contains(ReadName)) {
				close();
				return samRecord;
			}
		}
		return null;
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<SamRecord> readLines(int lines) {
		lines = lines - 1;
		Iterable<SamRecord> itContent = readLines();
		if (lines > 0) {
			for (int i = 0; i < lines; i++) {
				itContent.iterator().hasNext();
			}
		}
		return itContent;
	}
	protected boolean isSamBamFile() {
		int num = 0;
		int allNum = 100;
		boolean isSamBamFile = false;
		for (SamRecord samRecord : readLines()) {
			if (samRecord.getName() == null || samRecord.getName().equals("")) {
				num ++;
			} else {
				isSamBamFile = true;
				break;
			} if (num > allNum) {
				break;
			}
		}
		close();
		return isSamBamFile;
	}
	/** 迭代读取文件
	 * 单线程，不能同时开启两个读写线程
	 */
	public Iterable<SamRecord> readLines() {
		closeIterate();
		
		samRecordIterator = getSamFileReader().iterator();
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	/**
	 * 单线程，不能同时开启两个读写线程
	 * each SAMRecord returned is will have its alignment completely contained in the interval of interest. 
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesContained(String chrID, int start, int end) {
		closeIterate();
		
		chrID = chrID.toLowerCase();
		if (!mapChrIDlowCase2ChrID.containsKey(chrID)) {
			logger.error("出现未知reference");
			return null;
		}
		samRecordIterator = samFileReader.queryContained(mapChrIDlowCase2ChrID.get(chrID), start, end);
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	/**
	 * 单线程，不能同时开启两个读写线程
	 * the alignment of the returned SAMRecords need only overlap the interval of interest.
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesOverlap(String chrID, int start, int end) {
		closeIterate();
		
		chrID = chrID.toLowerCase();
		if (!mapChrIDlowCase2ChrID.containsKey(chrID)) {
			logger.error("出现未知reference");
			return null;
		}
		samRecordIterator = samFileReader.queryOverlapping(mapChrIDlowCase2ChrID.get(chrID), start, end);
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	private void closeIterate() {
		try {
			samRecordIterator.close();
		} catch (Exception e) { }
	}
	public boolean isBinary() {
		return samFileReader.isBinary();
	}
	
	public void close() {
		initial = false;
		try {
			samFileReader.close();
			samFileReader = null;
		} catch (Exception e) {  }
	}
	
}

class ReadSamIterable implements Iterable<SamRecord> {
	ReadSamIterator readSamIterator;
	public ReadSamIterable(SAMRecordIterator samRecordIterator, SAMFileHeader samFileHeader) {
		readSamIterator = new ReadSamIterator(samRecordIterator, samFileHeader);
	}
	@Override
	public Iterator<SamRecord> iterator() {
		return readSamIterator;
	}
}

class ReadSamIterator implements Iterator<SamRecord> {
	private static Logger logger = Logger.getLogger(ReadSamIterable.class);
	
	SAMRecordIterator samRecordIterator;
	SAMFileHeader samFileHeader;
	int correctLineNum = 0;
	int errorFormateLineNum = 0;
	
	public ReadSamIterator(SAMRecordIterator samRecordIterator, SAMFileHeader samFileHeader) {
		this.samRecordIterator = samRecordIterator;
		this.samFileHeader = samFileHeader;
	}
	
	@Override
	public boolean hasNext() {
		return samRecordIterator.hasNext();
	}

	@Override
	public SamRecord next() {
		SAMRecord samRecord = null;
		try {
			samRecord = samRecordIterator.next();
			if (correctLineNum < 100000) {
				correctLineNum ++;
			}
		} catch (SAMFormatException e) {
			logger.error(e);
			if (e.toString().contains("Error parsing text SAM file. Non-numeric value in POS column")) {
				errorFormateLineNum++;
			}
			if (errorFormateLineNum > 100 && correctLineNum < 5) {
				return null;
			}
			logger.error(e.toString());
			return getErrorSamRecord();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return getErrorSamRecord();
		}
		SamRecord samRecordThis = new SamRecord(samRecord);
		return samRecordThis;
	}
	
	private SamRecord getErrorSamRecord() {
		SAMRecord samRecorderror = new SAMRecord(samFileHeader);
		samRecorderror.setMappingQuality(0);
		samRecorderror.setFlags(4);
		SamRecord samRecordError = new SamRecord(samRecorderror);
		return samRecordError;
	}
	@Override
	public void remove() {
		samRecordIterator.remove();
	}

}
