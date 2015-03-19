package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;

import java.io.OutputStream;

import org.apache.log4j.Logger;

public class SamWriter {
	private static final Logger logger = Logger.getLogger(SamWriter.class);
	String fileName;
	SAMFileWriter samFileWriter;
	SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
	
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile) {
		this.fileName = outSamFile;
		boolean writeToBam = true;
		if (outSamFile.endsWith(".sam")) {
			writeToBam = false;
		}
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, outSamFile, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, outSamFile);
		}
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile, boolean writeToBam) {
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, outSamFile, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, outSamFile);
		}
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, OutputStream os, boolean writeToBam) {
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, os, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, os);
		}
	}
	
	public void writeToSamFileln(SamRecord samRecord) {
		if (samRecord == null || samRecord.samRecord == null) {
			logger.error("samRecord为null");
			return;
		}
		try {
			samFileWriter.addAlignment(samRecord.samRecord);
		} catch (Exception e) {
			logger.error("samRecord出错" + samRecord.toString(), e);
		}
	}
	
	public void writeToSamFileln(SAMRecord samRecord) {
		if (samRecord == null) {
			logger.error("samRecord为null");
			return;
		}
		try {
			samFileWriter.addAlignment(samRecord);
		} catch (Exception e) {
			logger.error("samRecord出错" + samRecord.toString(), e);
		}
	}
	
	public void close() {
		try {
			samFileWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
