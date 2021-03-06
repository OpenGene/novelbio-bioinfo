package com.novelbio.bioinfo.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.fileOperate.FileOperate;

public class SamWriter {
	private static final Logger logger = LoggerFactory.getLogger(SamWriter.class);
	String fileName;
	SAMFileWriter samFileWriter;
	SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
	
	int errorReadsNum = 0;
	int maxErrorReadsNum = 10000;
	
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile) {
		this(presorted, samFileHeader, outSamFile, !outSamFile.endsWith(".sam"));
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile, boolean writeToBam) {
		this.fileName = outSamFile;
		OutputStream outputStream = null;
		try {
			outputStream = FileOperate.getOutputStream(fileName);	
		} catch (Exception e) {
			throw new RuntimeException("fileName=" + fileName, e);
		}
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, outputStream);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, outputStream);
		}
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, OutputStream os, boolean writeToBam) {
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, os);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, os);
		}
	}
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, OutputStream os, boolean writeToBam, int maxRecordsInRam) {
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.setMaxRecordsInRam(maxRecordsInRam).makeBAMWriter(samFileHeader, presorted, os);
		} else {
			samFileWriter = samFileWriterFactory.setMaxRecordsInRam(maxRecordsInRam).makeSAMWriter(samFileHeader, presorted, os);
		}
	}
	public void writeToSamFileln(SamRecord samRecord) {
		if (samRecord == null || samRecord.samRecord == null) return;
		
		try {
			samFileWriter.addAlignment(samRecord.getSamRecord());
		} catch (Exception e) {
			errorReadsNum++;
			if (errorReadsNum <= 100) {
				e.printStackTrace();
				logger.error("write error: " + samRecord.toString() , e);
			}
			if (errorReadsNum > 10000 ) {
				e.printStackTrace();
				logger.error("to much reads error, more than 10000", e);
				close();
				throw new ExceptionSamError(e);
			}
		}
	}
	
	public void writeToSamFileln(SAMRecord samRecord) {
		if (samRecord == null) return;

		try {
			samFileWriter.addAlignment(samRecord);
		} catch (Exception e) {
			errorReadsNum++;
			if (errorReadsNum <= 100) {
				logger.error("write error: " + new SamRecord(samRecord).toString() , e);
			}
			if (errorReadsNum > 10000 ) {
				logger.error("to much reads error, more than 10000", e);
				close();
				throw new ExceptionSamError(e);
			}
		}
	}
	
	public void close() {
		try {
			errorReadsNum = 0;
			samFileWriter.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
