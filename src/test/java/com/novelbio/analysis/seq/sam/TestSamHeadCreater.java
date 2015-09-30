package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMProgramRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMTextHeaderCodec;

import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

public class TestSamHeadCreater {
	
	@Test
	public void testCreaterAraBwa() {
		String refSeq = "src/test/resources/test_file/reference/ara/chrAll.fa";
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refSeq);
		samHeadCreater.setAttr("@HD\tVN:1.4\tSO:coordinate");
		samHeadCreater.addReadGroup("@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64");
		samHeadCreater.addReadGroup("@RG\tID:Shill64\tPL:Illumina\tLB:Shill64\tSM:Shill64");

		samHeadCreater.addProgram("@PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz");
		SAMFileHeader samFileHeader = samHeadCreater.generateHeader();
		
		Assert.assertEquals(SortOrder.coordinate, samFileHeader.getSortOrder());
		Assert.assertEquals("1.4", samFileHeader.getVersion());
		SAMSequenceDictionary dictionary = samFileHeader.getSequenceDictionary();
		SAMProgramRecord pg = samFileHeader.getProgramRecord("bwa");
		Assert.assertEquals("0.7.8-r455", pg.getAttribute("VN"));
		Assert.assertEquals("bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz", pg.getAttribute("CL"));
		System.out.println(samFileHeader.toString());
		
		
		SAMTextHeaderCodec samTextHeaderCodec = new SAMTextHeaderCodec();
		StringWriter stringWriter = new StringWriter();
		samTextHeaderCodec.encode(stringWriter, samFileHeader);
		System.out.println(stringWriter.toString());
	}
	
}
