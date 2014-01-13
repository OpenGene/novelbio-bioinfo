package com.novelbio.analysis.seq.sam;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;

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
	
	public void close() {
		try {
			samFileWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
