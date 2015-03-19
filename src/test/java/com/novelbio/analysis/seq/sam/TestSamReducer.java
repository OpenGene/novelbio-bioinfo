package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;

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
	@Test
	public void testSamFile() {
		String outFile = "/home/novelbio/testJava/samReducer/result.bam";
		String samRecordTxt = "HWI-ST1033:92:D0VACACXX:1:1101:8144:31766	419	chr1	17533	11	100M1S	=	17646	213	TGATGCCCTGGGTCCCCACTAAGCCAGGCCGGGCCTCCCGCCCACACCCCTCGGCCCTGCCTTCTGGCCATACAGGTTCTCGGTGGTGTTGAAAAGCAGCN	?=@?BD??:A;A<CCGEHG>?ECEDBG381))08DH9=BA4;4'5A-5=9?A<3598<9<9A:ACD((++9:<@AC@>>:A8?+2<-82<?A:>4983<??	MD:Z:61C31G6	XG:i:0	NH:i:4	HI:i:3	NM:i:2	XM:i:2	XN:i:0	XO:i:0	AS:i:187	XS:i:188	YS:i:187	YT:Z:CP";
		String refSeq = "/home/novelbio/testJava/chrome/ara/chrAll.fa";
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refSeq);
		samHeadCreater.setAttr("@HD\tVN:1.4\tSO:coordinate");
		samHeadCreater.addReadGroup("@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64");
		samHeadCreater.addProgram("@PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz");
		SAMFileHeader samFileHeader = samHeadCreater.generateHeader();
			
		SamReducer samReducer = new SamReducer();
		samReducer.setSamHeader(samFileHeader);
		samReducer.setIsWriteOs(false);
		samReducer.setOutFile(outFile);
		samReducer.initial();
		
		samReducer.addSamRecordTxt(samRecordTxt);
		samReducer.close();
		
		SamFile samFile = new SamFile(outFile);
		SamRecord samRecord = samFile.readFirstLine();
		
		Assert.assertEquals("chr1", samRecord.getRefID());
		Assert.assertEquals("HWI-ST1033:92:D0VACACXX:1:1101:8144:31766", samRecord.getName());
		Assert.assertEquals(samRecordTxt, samRecord.toString());
	}
	
	/** Reducer输出为标准输出流 */
	@Test
	public void testSamHeader() {
		String outFile = "/home/novelbio/testJava/samReducer/result_os.bam";
		String samRecordTxt = "HWI-ST1033:92:D0VACACXX:1:1101:8144:31766	419	chr1	17533	11	100M1S	=	17646	213	TGATGCCCTGGGTCCCCACTAAGCCAGGCCGGGCCTCCCGCCCACACCCCTCGGCCCTGCCTTCTGGCCATACAGGTTCTCGGTGGTGTTGAAAAGCAGCN	?=@?BD??:A;A<CCGEHG>?ECEDBG381))08DH9=BA4;4'5A-5=9?A<3598<9<9A:ACD((++9:<@AC@>>:A8?+2<-82<?A:>4983<??	MD:Z:61C31G6	XG:i:0	NH:i:4	HI:i:3	NM:i:2	XM:i:2	XN:i:0	XO:i:0	AS:i:187	XS:i:188	YS:i:187	YT:Z:CP";
		String refSeq = "/home/novelbio/testJava/chrome/ara/chrAll.fa";
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
		
		SamReducer samReducer = new SamReducer();
		samReducer.setSamHeader(samFileHeader);
		samReducer.setIsWriteOs(true);
		samReducer.setOutStream(os, true);
		samReducer.initial();
		samReducer.addSamRecordTxt(samRecordTxt);
		samReducer.close();

		SamFile samFile = new SamFile(outFile);
		SamRecord samRecord = samFile.readFirstLine();
		
		Assert.assertEquals("chr1", samRecord.getRefID());
		Assert.assertEquals("HWI-ST1033:92:D0VACACXX:1:1101:8144:31766", samRecord.getName());
		Assert.assertEquals(samRecordTxt, samRecord.toString());
	}
	
	/** 测试本类最重要的功能，reducer的功能 */
	@Test
	public void testReducer() {
		String outFile = "/home/novelbio/testJava/samReducer/result_test_all.sam";
		String refSeq = "/home/novelbio/testJava/chrome/ara/chrAll.fa";
		String pgLine = "@PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n";
		String rgLine = "@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64";
		
		SamReducer samReducer = new SamReducer();
		InputStream inputStream = null;
		try {
			inputStream = FileOperate.getInputStream("/home/novelbio/testJava/samReducer/hadoop-mr-sam-small");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		samReducer.setIsWriteOs(true);
//		try {
//			samReducer.setOutStream(new FileOutputStream(new File(outFile)), false);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		samReducer.setOutStream(System.out, false);
		
		samReducer.readInStream(refSeq, pgLine, rgLine, inputStream);
		samReducer.close();
		SamFile samFile = new SamFile(outFile);
		SamRecord samRecord = samFile.readFirstLine();
		System.out.println(samRecord.toString());
	}
	
}
