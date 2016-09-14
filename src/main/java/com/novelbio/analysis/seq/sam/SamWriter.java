package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;

import java.io.File;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

public class SamWriter {
	private static final Logger logger = Logger.getLogger(SamWriter.class);
	String fileName;
	SAMFileWriter samFileWriter;
	SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
	
	int errorReadsNum = 0;
	int maxErrorReadsNum = 10000;
	
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile) {
		this.fileName = outSamFile;
		boolean writeToBam = true;
		if (outSamFile.endsWith(".sam")) {
			writeToBam = false;
		}
		File file = FileOperate.getFile(fileName);
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, file, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, file);
		}
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile, boolean writeToBam) {
		this.fileName = outSamFile;
		File file = FileOperate.getFile(fileName);
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, file, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, file);
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
	
	public void writeToSamFileln(SamRecord samRecord) {
		if (samRecord == null || samRecord.samRecord == null) return;
		
		try {
			samFileWriter.addAlignment(samRecord.getSamRecord());
		} catch (Exception e) {
			errorReadsNum++;
			if (errorReadsNum <= 100) {
				logger.error("write error: " + samRecord.toString() , e);
			}
			if (errorReadsNum > 10000 ) {
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
