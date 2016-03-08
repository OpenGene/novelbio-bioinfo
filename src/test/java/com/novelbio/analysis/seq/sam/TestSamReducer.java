package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMProgramRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;

public class TestSamReducer {
	/** Reducer输出为文件 */
//	@Test
	public void testSamFile() {
		String outFile = "src/test/resources/test_file/sam/mr_result_os.bam";
		String samRecordTxt = "mchr1_@_10019193_@_HWI-D00175:261:C6L59ANXX:7:1101:10429:21613	HWI-D00175:261:C6L59ANXX:7:1101:10429:21613	133	chr1	10019193	0	*	=	10019193	0	CCGCGGCTCCGCCTCCCCAATCCGTCCCGTCTCCTCGCCTGCCGCTGGCATCCTGCTAGCAGAGGTGGCGCCAGCGTTCTCCTGCTCCCGCCGCATTCGCTCGCCGGTGAGCATCCTAATCCATC	CCCCCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGAGGGGGGEFEGGGFGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGAGGAEADGGGGGGGGGGG	NH:i:1	HI:i:1	AS:i:0	XS:i:0";
		String refSeq = "src/test/resources/test_file/reference/arabidopsis_sub/chrAll.fa";
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refSeq);
		samHeadCreater.setAttr("@HD\tVN:1.4\tSO:coordinate");
		samHeadCreater.addReadGroup("@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64");
		samHeadCreater.addProgram("@PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz");
		SAMFileHeader samFileHeader = samHeadCreater.generateHeader();
			
		final SamReducer samReducer = new SamReducer();
		samReducer.setHeader(samFileHeader);
		samReducer.setOutFileName(outFile);
		samReducer.initial();
		
		samReducer.addSamRecordTxt(samRecordTxt);
		samReducer.finish();
		SamFile samFile = new SamFile(outFile);
		SamRecord samRecord = samFile.readFirstLine();
		
		Assert.assertEquals("chr1", samRecord.getRefID());
		Assert.assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:10429:21613", samRecord.getName());
		Assert.assertEquals(samRecordTxt, "mchr1_@_10019193_@_HWI-D00175:261:C6L59ANXX:7:1101:10429:21613\t" + samRecord.toString());
		samFile.close();
		FileOperate.DeleteFileFolder(outFile);
	}
	
	/** Reducer输出为标准输出流 */
	@Test
	public void testSamHeader() {
		String outFile = "src/test/resources/test_file/sam/mr_result_os.bam";
		String samRecordTxt = "mchr1_@_10019193_@_HWI-D00175:261:C6L59ANXX:7:1101:10429:21613	HWI-D00175:261:C6L59ANXX:7:1101:10429:21613	133	chr1	10019193	0	*	=	10019193	0	CCGCGGCTCCGCCTCCCCAATCCGTCCCGTCTCCTCGCCTGCCGCTGGCATCCTGCTAGCAGAGGTGGCGCCAGCGTTCTCCTGCTCCCGCCGCATTCGCTCGCCGGTGAGCATCCTAATCCATC	CCCCCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGAGGGGGGEFEGGGFGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGAGGAEADGGGGGGGGGGG	NH:i:1	HI:i:1	AS:i:0	XS:i:0";
		String refSeq = "src/test/resources/test_file/reference/arabidopsis_sub/chrAll.fa";
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refSeq);
		samHeadCreater.setAttr("@HD\tVN:1.4\tSO:coordinate");
		samHeadCreater.addReadGroup("@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64");
		samHeadCreater.addProgram("@PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz");
		SAMFileHeader samFileHeader = samHeadCreater.generateHeader();
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(new File(outFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final SamReducer samReducer = new SamReducer();
		samReducer.setHeader(samFileHeader);
		samReducer.setOutStream(System.out);
		samReducer.initial();
		samReducer.addSamRecordTxt(samRecordTxt);
		samReducer.finish();
		
//		SamFile samFile = new SamFile(outFile);
//		SamRecord samRecord = samFile.readFirstLine();
//		
//		Assert.assertEquals("chr1", samRecord.getRefID());
//		Assert.assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:10429:21613", samRecord.getName());
//		Assert.assertEquals(samRecordTxt, "mchr1_@_10019193_@_HWI-D00175:261:C6L59ANXX:7:1101:10429:21613\t" + samRecord.toString());
//		FileOperate.DeleteFileFolder(outFile);
	}
	
	/** 测试本类最重要的功能，reducer的功能 
	 * @throws IOException */
//	@Test
	public void testReducer() throws IOException {
		String outFile = "src/test/resources/test_file/sam/mr_result_os.bam";
		String refSeq = "src/test/resources/test_file/reference/arabidopsis_sub/chrAll.fa";
		
		final SamReducer samReducer = new SamReducer();
		InputStream inputStream = null;
		inputStream = FileOperate.getInputStream("/home/novelbio/git/NBCplatform/src/test/resources/test_file/sam/mr_mapping.txt");
		
		samReducer.setOutFileName(outFile);
		samReducer.readInStream(refSeq, inputStream);
		
		Assert.assertEquals(true, FileOperate.isFileExist(FileOperate.changeFileSuffix(outFile, "_tmp", null)));
		samReducer.finish();
		Assert.assertEquals(false, FileOperate.isFileExist(FileOperate.changeFileSuffix(outFile, "_tmp", null)));
		
		SamFile samFile = new SamFile(outFile);
		SAMProgramRecord samProgramRecord = samFile.getHeader().getProgramRecords().get(0);
		Assert.assertEquals("bwa", samProgramRecord.getProgramName());
		Assert.assertEquals("0.7.8-r455", samProgramRecord.getProgramVersion());
		Assert.assertEquals("bwa mem -t 8 -p -P chrAll.fa -", samProgramRecord.getCommandLine());
		Assert.assertEquals("bwa", samProgramRecord.getProgramGroupId());
		int i = 0;
		for (SamRecord samRecord : samFile.readLines()) {
			i++;
		}
		Assert.assertEquals(48, i);
		samFile.close();
		FileOperate.DeleteFileFolder(outFile);
	}
	
}
