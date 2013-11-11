package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SeekableHDFSstream;
import net.sf.samtools.seekablestream.SeekableFileStream;
import net.sf.samtools.seekablestream.SeekableStream;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.HdfsBase;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class SamReader {
	private static final Logger logger = Logger.getLogger(SamReader.class);
	boolean initial = false;
	
	long filesize = 0;
	
	/** 仅用来计算读取进度 */
	InputStream inputStream;
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
	boolean isIndexed = false;
	boolean firstReadLine = true;
	public SamReader(String fileName) {
		this.fileName = fileName;
	}
	
	public SamReader(String fileName, String fileIndex) {
		this.fileName = fileName;
		this.fileIndex = fileIndex;
	}
	
	/** 不设定文件名，也不支持索引<br>
	 * <b>不需要初始化</b>
	 */
	public SamReader(InputStream inputStream) {
		samFileReader = new SAMFileReader(inputStream);
		setSamHeader(inputStream);
		initial = true;
	}
	
	public void setFileIndex(String fileIndex) {
		if (!FileOperate.isFileExistAndBigThanSize(fileIndex, 0)) return;
		
		this.fileIndex = fileIndex;
		try {
			initialSamHeadAndReader(fileIndex);
			initial = true;
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	public void initial() {
		if (!initial) {
			try {
				initialSamHeadAndReader(fileIndex);
				initial = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		close();
		return pairend;
	}
	
	private void initialSamHeadAndReader(String fileIndex) throws IOException {
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
		
		isIndexed = false;
		if (fileIndex != null && FileOperate.isFileExistAndBigThanSize(fileIndex, 0)) {
			isIndexed = true;
		}
		
		if (fileName != null) initialStream();
		
		if (isIndexed) {
			if (HdfsBase.isHdfs(fileIndex)) {
				FileHadoop fileHadoopIndex = new FileHadoop(fileIndex);
				SeekableHDFSstream seekableIndex = new SeekableHDFSstream(fileHadoopIndex);
				samFileReader = new SAMFileReader((SeekableStream)inputStream, seekableIndex, false);
			} else {
				samFileReader = new SAMFileReader((SeekableStream)inputStream, new File(fileIndex), false);
			}
		} else {
			samFileReader = new SAMFileReader(inputStream);
		}
		setSamHeader(inputStream);
	}
	
	private void initialStream() throws IOException {
		if (HdfsBase.isHdfs(fileName)) {
			FileHadoop fileHadoop = new FileHadoop(fileName);
			if (isIndexed) {
				inputStream = new SeekableHDFSstream(fileHadoop);
			} else {
				inputStream = fileHadoop.getInputStream();
			}
		} else {
			File file = new File(fileName);
			if (isIndexed) {
				inputStream = new SeekableFileStream(file);
			} else {
				inputStream = new FileInputStream(file);
			}
		}
	}
	
	private void setSamHeader(InputStream inputStream) {
		samFileHeader = samFileReader.getFileHeader();
		mapChrIDlowCase2ChrID = new LinkedHashMap<String, String>();
		mapChrIDlowCase2Length = new LinkedHashMap<String, Long>();
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
	 * 获得该bam文件中染色体的长度信息，注意key为实际key
	 * @return
	 */
	public Map<String, Long> getMapChrID2Length() {
		Map<String, Long> mapChrID2Length = new HashMap<>();
		for (String chrID : mapChrIDlowCase2Length.keySet()) {
			long length = mapChrIDlowCase2Length.get(chrID);
			String chrIDNorm = mapChrIDlowCase2ChrID.get(chrID);
			mapChrID2Length.put(chrIDNorm, length);
		}
		return mapChrID2Length;
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
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		long readByte = getReadByte();
		if (readByte < 0 || filesize < 0) {
			return -1;
		} else {
			return (double)readByte/filesize;
		}
	}
	
	protected InputStream getInputStream() {
		return inputStream;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public long getReadByte() {
		long position = 0;
		try {
			if (isIndexed) {
				SeekableStream seekableStream = (SeekableStream)inputStream;
				position = seekableStream.position();
			} else {
				FileInputStream fileInputStream = (FileInputStream)inputStream;
				position = fileInputStream.getChannel().position();
			}
		} catch (Exception e) {
			return -1;
		}
		return position;
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
	
	/** 内部关闭流 */
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
		if (firstReadLine) {
			firstReadLine = false;
		} else {
			close();
			initial();
		}

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
		try {
			inputStream.close();
			inputStream = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
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
	/** 连续50000条reads出错就报错 */
	static int errorLinNum = 50000;
	int errorContinueNum = 0;
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
		if ((errorFormateLineNum > 500 && correctLineNum < 5)) {
			throw new SamErrorException("sam file has too many error formate lines");
		} else if (errorContinueNum > errorLinNum) {
			throw new SamErrorException("sam file is not end normally");
		}
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
			errorContinueNum = 0;
		} catch (SAMFormatException e) {
			logger.error(e);
			if (e.toString().contains("Error parsing text SAM file. Non-numeric value in POS column")) {
				errorFormateLineNum++;
			}
			logger.error(e.toString());
			return getErrorSamRecord();
		} catch (Exception e) {
			errorContinueNum++;
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
