package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math.optimization.univariate.BracketFinder;
import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceRecord;

public class SamReader {
	private static Logger logger = Logger.getLogger(SamReader.class);
	/**
	 * ��ȡsam�ļ����࣬��ò�Ҫֱ���ã���getSamFileReader()��������
	 */
	SAMFileReader samFileReader;
	SAMFileHeader samFileHeader;
	
	/** Сд��chrID��samFileHeader�е�chrID�Ķ��ձ� */
	HashMap<String, String> mapChrIDlowCase2ChrID = new LinkedHashMap<String, String>();
	HashMap<String, Long> mapChrIDlowCase2Length = new LinkedHashMap<String, Long>();
	String fileName;
	String fileIndex;
	Boolean pairend;
	
	boolean isOpen = false;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
		close();
		samFileReader = null;
		samFileHeader = null;
	}
	public void setFileIndex(String fileIndex) {
		if (FileOperate.isFileExistAndBigThanSize(fileIndex, 0)) {
			this.fileIndex = fileIndex;
		}
	}
	
	public String getName() {
		SAMFileHeader samFileHeader = getSamFileReader().getFileHeader();
		return samFileHeader.toString();
	}
	/**
	 * ˫�������Ƿ�������һ���bed�ļ�
	 * ��������ǵ������ݣ��������ӳ�����bed�ļ�
	 * ע�⣺�����˫���ļ���<b>����Ԥ������</b>
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
	protected SAMFileHeader getSamFileHead() {
		initialSamHeadAndReader();
		return samFileHeader;
	}
	
	protected SAMFileReader getSamFileReader() {
		initialSamHeadAndReader();
		return samFileReader;
	}
	
	private void initialSamHeadAndReader() {
		if (samFileHeader != null && samFileReader != null && isOpen == true 
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
		isOpen = true;
		File file = new File(fileName);
		File index = null;
		if (fileIndex != null) {
			index = new File(fileIndex);
		}
		samFileReader = new SAMFileReader(file, index);
		samFileHeader = samFileReader.getFileHeader();
		mapChrIDlowCase2ChrID = new HashMap<String, String>();
		mapChrIDlowCase2Length = new HashMap<String, Long>();
		//���reference��������Ϣ
		List<SAMSequenceRecord> lsSamSequenceRecords = samFileHeader.getSequenceDictionary().getSequences();
		for (SAMSequenceRecord samSequenceRecord : lsSamSequenceRecords) {
			mapChrIDlowCase2ChrID.put(samSequenceRecord.getSequenceName().toLowerCase(), samSequenceRecord.getSequenceName());
			mapChrIDlowCase2Length.put(samSequenceRecord.getSequenceName().toLowerCase(), (long) samSequenceRecord.getSequenceLength());
		}
	}
	/**
	 * ��ø�bam�ļ���Ⱦɫ��ĳ�����Ϣ��ע��key��ΪСд
	 * @return
	 */
	public HashMap<String, Long> getMapChrIDlowCase2Length() {
		initialSamHeadAndReader();
		return mapChrIDlowCase2Length;
	}
	/**
	 * ע���Сд����
	 * @param ReadName reads�����֣�ֻҪд�ؼ��ʾ�����
	 * @return û�ҵ��ͷ���null
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	/** ������ȡ�ļ� */
	public Iterable<SamRecord> readLines() {
		SAMRecordIterator samRecordIterator = getSamFileReader().iterator();
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	/**
	 * each SAMRecord returned is will have its alignment completely contained in the interval of interest. 
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesContained(String chrID, int start, int end) {
		getSamFileReader();
		chrID = chrID.toLowerCase();
		if (!mapChrIDlowCase2ChrID.containsKey(chrID)) {
			logger.error("����δ֪reference");
			return null;
		}
		SAMRecordIterator samRecordIterator = samFileReader.queryContained(mapChrIDlowCase2ChrID.get(chrID), start, end);
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	/**
	 * the alignment of the returned SAMRecords need only overlap the interval of interest.
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterable<SamRecord> readLinesOverlap(String chrID, int start, int end) {
		getSamFileReader();
		chrID = chrID.toLowerCase();
		if (!mapChrIDlowCase2ChrID.containsKey(chrID)) {
			logger.error("����δ֪reference");
			return null;
		}
		SAMRecordIterator samRecordIterator = samFileReader.queryOverlapping(mapChrIDlowCase2ChrID.get(chrID), start, end);
		return new ReadSamIterable(samRecordIterator, samFileHeader);
	}
	
	public boolean isBinary() {
		initialSamHeadAndReader();
		return samFileReader.isBinary();
	}
	
	public void close() {
		isOpen = false;
		try {
			samFileReader.close();
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
