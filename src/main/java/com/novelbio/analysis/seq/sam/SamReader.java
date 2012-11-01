package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.math.optimization.univariate.BracketFinder;
import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class SamReader {
	private static Logger logger = Logger.getLogger(SamReader.class);
	/**
	 * ��ȡsam�ļ����࣬��ò�Ҫֱ���ã���getSamFileReader()��������
	 */
	SAMFileReader samFileReader;
	SAMFileHeader samFileHeader;
	String fileName;
	
	Boolean pairend;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
		if (samFileHeader == null) {
			getSamFileReader();
		}
		return samFileHeader;
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
			}
			else {
				isSamBamFile = true;
				break;
			}
			if (num > allNum) {
				break;
			}
		}
		close();
		return isSamBamFile;
	}
	/**
	 * ������ȡ�ļ�
	 */
	public Iterable<SamRecord> readLines() {
		final SAMRecordIterator samRecordIterator = getSamFileReader().iterator();
		return new Iterable<SamRecord>() {
			int errorFormateLineNum = 0;
			int correctLineNum = 0;
			public Iterator<SamRecord> iterator() {
				return new Iterator<SamRecord>() {
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
						SAMRecord samRecorderror = new SAMRecord(getSamFileHead());
						samRecorderror.setMappingQuality(0);
						samRecorderror.setFlags(4);
						SamRecord samRecordError = new SamRecord(samRecorderror);
						return samRecordError;
					}
					@Override
					public void remove() {
						samRecordIterator.remove();
					}
				};
			}
		};
	}

	private SAMFileReader getSamFileReader() {
		close();
		File file = new File(fileName);
		samFileReader = new SAMFileReader(file);
		samFileHeader = samFileReader.getFileHeader();
		return samFileReader;
	}
	public void close() {
		try {
			samFileReader.close();
		} catch (Exception e) {  }
	}
}
