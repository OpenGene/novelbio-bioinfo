package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class SamReader {
	private static Logger logger = Logger.getLogger(SamReader.class);
	/**
	 * 读取sam文件的类，最好不要直接用，用getSamFileReader()方法代替
	 */
	SAMFileReader samFileReader;
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
	protected SAMFileHeader getsamfilehead() {
		return getSamFileReader().getFileHeader();
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
	/**
	 * 迭代读取文件
	 */
	public Iterable<SamRecord> readLines() {
		final SAMRecordIterator samRecordIterator = getSamFileReader().iterator();
		return new Iterable<SamRecord>() {
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
						} catch (Exception e) {
							logger.error("出错");
							return next();
						}
						
						SamRecord samRecordThis = new SamRecord(samRecord);
						return samRecordThis;
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
		return samFileReader;
	}
	public void close() {
		try {
			samFileReader.close();
		} catch (Exception e) {  }
	}
}
