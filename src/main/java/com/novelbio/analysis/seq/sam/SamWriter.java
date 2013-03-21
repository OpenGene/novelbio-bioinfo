package com.novelbio.analysis.seq.sam;

import java.io.File;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;

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
		File samFile = new File(outSamFile);
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, samFile, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, samFile);
		}
	}
	
	/** 默认写入bam文件 */
	public SamWriter(boolean presorted, SAMFileHeader samFileHeader, String outSamFile, boolean writeToBam) {
		this.fileName = outSamFile;
		File samFile = new File(outSamFile);
		if (writeToBam) {
			samFileWriter = samFileWriterFactory.makeBAMWriter(samFileHeader, presorted, samFile, 7);
		} else {
			samFileWriter = samFileWriterFactory.makeSAMWriter(samFileHeader, presorted, samFile);
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
			e.printStackTrace();
			logger.error("samRecord出错" + samRecord.toString());
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
