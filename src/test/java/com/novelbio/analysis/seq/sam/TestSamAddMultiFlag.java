package com.novelbio.analysis.seq.sam;

import junit.framework.Assert;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordFactory;
import htsjdk.samtools.ValidationStringency;

import org.junit.Test;

/** sam文件添加 非 unique mapping 的标签 */
public class TestSamAddMultiFlag {
	@Test
	public void testMultiFlagPE() {
		String refseq = "/home/novelbio/testJava/chrome/arabidopsis_sub/chrAll.fa";
		SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
		SAMFileHeader header = getHeader(refseq);
		// 100---150  166---190   179--290
		SamRecord samRecord1 = generateRecord(header, "samName1", "chr1", 100, 150, true, "chr1", 166, true);
		SamRecord samRecord2 = generateRecord(header, "samName1", "chr1", 166, 190, true, "chr1", 100, false);
		SamRecord samRecord3 = generateRecord(header, "samName1", "chr1", 179, 290, true, "chr1", 100, false);
		
		//180----190   195---210
		//550----655   667---755 790-820
		SamRecord samRecord4 = generateRecord(header, "samName2", "chr1", 180, 190, true, "chr1", 195, true);
		SamRecord samRecord5 = generateRecord(header, "samName2", "chr1", 195, 210, true, "chr1", 180, false);
		SamRecord samRecord6 = generateRecord(header, "samName2", "chr1", 550, 655, true, "chr1", 790, true);
		SamRecord samRecord7 = generateRecord(header, "samName2", "chr1", 667, 755, true, "chr1", 550, false);
		SamRecord samRecord8 = generateRecord(header, "samName2", "chr1", 790, 820, true, "chr1", 550, false);
		
		//179----290   357---879
		SamRecord samRecord9 = generateRecord(header, "samName1", "chr1", 179, 290, true, "chr1", 357, true);
		SamRecord samRecord10 = generateRecord(header, "samName1", "chr1", 357, 879, true, "chr1", 179, false);
		
		samAddMultiFlag.setPairend(true);
		samAddMultiFlag.addSamRecord(samRecord1);
		samAddMultiFlag.addSamRecord(samRecord2);
		samAddMultiFlag.addSamRecord(samRecord3);
		samAddMultiFlag.addSamRecord(samRecord4);
		samAddMultiFlag.addSamRecord(samRecord5);
		samAddMultiFlag.addSamRecord(samRecord6);
		samAddMultiFlag.addSamRecord(samRecord7);
		samAddMultiFlag.addSamRecord(samRecord8);
		samAddMultiFlag.addSamRecord(samRecord9);
		samAddMultiFlag.addSamRecord(samRecord10);
		
		samAddMultiFlag.finish();
		
		Assert.assertEquals(true, samRecord1.isUniqueMapping());
		Assert.assertEquals(true, samRecord2.isUniqueMapping());
//		Assert.assertEquals(true, samRecord3.isUniqueMapping());
		
		Assert.assertEquals(false, samRecord4.isUniqueMapping());
		Assert.assertEquals(false, samRecord5.isUniqueMapping());
		Assert.assertEquals(false, samRecord6.isUniqueMapping());
		Assert.assertEquals(false, samRecord7.isUniqueMapping());
//		Assert.assertEquals(false, samRecord8.isUniqueMapping());
		
		Assert.assertEquals(true, samRecord9.isUniqueMapping());
		Assert.assertEquals(true, samRecord10.isUniqueMapping());
		
		Assert.assertEquals(1, samRecord1.getMappedReadsWeight());
		Assert.assertEquals(1, samRecord2.getMappedReadsWeight());
		Assert.assertEquals(1, samRecord3.getMappedReadsWeight());
		
		Assert.assertEquals(2, samRecord4.getMappedReadsWeight());
		Assert.assertEquals(2, samRecord5.getMappedReadsWeight());
		Assert.assertEquals(2, samRecord6.getMappedReadsWeight());
		
		Assert.assertEquals(1, samRecord9.getMappedReadsWeight());
		Assert.assertEquals(1, samRecord10.getMappedReadsWeight());
		
		Assert.assertEquals(8, samAddMultiFlag.queueSamRecords.size());
		for (SamRecord samRecord : samAddMultiFlag.readlines()) {
			System.out.println(samRecord);
		}
		Assert.assertEquals(0, samAddMultiFlag.queueSamRecords.size());
	}
	
	@Test
	public void testMultiFlagSE() {
		String refseq = "/home/novelbio/testJava/chrome/arabidopsis_sub/chrAll.fa";
		SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
		SAMFileHeader header = getHeader(refseq);
		// 100---150  166---190   179--290
		SamRecord samRecord1 = generateRecordSE(header, "samName1", "chr1", 100, 150);
		SamRecord samRecord2 = generateRecordSE(header, "samName1", "chr1", 166, 190);
		SamRecord samRecord3 = generateRecordSE(header, "samName1", "chr1", 179, 290);
		
		//180----190   195---210
		//550----655   667---755 790-820
		SamRecord samRecord4 = generateRecordSE(header, "samName2", "chr1", 180, 190);
		SamRecord samRecord5 = generateRecordSE(header, "samName2", "chr1", 195, 210);
		SamRecord samRecord6 = generateRecordSE(header, "samName2", "chr1", 550, 655);
		SamRecord samRecord7 = generateRecordSE(header, "samName2", "chr1", 667, 755);
		SamRecord samRecord8 = generateRecordSE(header, "samName2", "chr1", 790, 820);
		
		//179----290   357---879
		SamRecord samRecord9 = generateRecordSE(header, "samName3", "chr1", 179, 290);
		SamRecord samRecord10 = generateRecordSE(header, "samName4", "chr1", 357, 879);
		
		samAddMultiFlag.setPairend(true);
		samAddMultiFlag.addSamRecord(samRecord1);
		samAddMultiFlag.addSamRecord(samRecord2);
		samAddMultiFlag.addSamRecord(samRecord3);
		samAddMultiFlag.addSamRecord(samRecord4);
		samAddMultiFlag.addSamRecord(samRecord5);
		samAddMultiFlag.addSamRecord(samRecord6);
		samAddMultiFlag.addSamRecord(samRecord7);
		samAddMultiFlag.addSamRecord(samRecord8);
		samAddMultiFlag.addSamRecord(samRecord9);
		samAddMultiFlag.addSamRecord(samRecord10);
		
		samAddMultiFlag.finish();
		
		Assert.assertEquals(false, samRecord1.isUniqueMapping());
		Assert.assertEquals(false, samRecord2.isUniqueMapping());
		Assert.assertEquals(false, samRecord3.isUniqueMapping());
		
		Assert.assertEquals(false, samRecord4.isUniqueMapping());
		Assert.assertEquals(false, samRecord5.isUniqueMapping());
		Assert.assertEquals(false, samRecord6.isUniqueMapping());
		Assert.assertEquals(false, samRecord7.isUniqueMapping());
		Assert.assertEquals(false, samRecord8.isUniqueMapping());
		
		Assert.assertEquals(true, samRecord9.isUniqueMapping());
		Assert.assertEquals(true, samRecord10.isUniqueMapping());
		
		Assert.assertEquals(3, samRecord1.getMappedReadsWeight());
		Assert.assertEquals(3, samRecord2.getMappedReadsWeight());
		Assert.assertEquals(3, samRecord3.getMappedReadsWeight());
		
		Assert.assertEquals(5, samRecord4.getMappedReadsWeight());
		Assert.assertEquals(5, samRecord5.getMappedReadsWeight());
		Assert.assertEquals(5, samRecord6.getMappedReadsWeight());
		
		Assert.assertEquals(1, samRecord9.getMappedReadsWeight());
		Assert.assertEquals(1, samRecord10.getMappedReadsWeight());
		
		Assert.assertEquals(10, samAddMultiFlag.queueSamRecords.size());
		for (SamRecord samRecord : samAddMultiFlag.readlines()) {
			System.out.println(samRecord);
		}
		Assert.assertEquals(0, samAddMultiFlag.queueSamRecords.size());
	}
	
	private SAMFileHeader getHeader(String refseq) {
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refseq);
		return samHeadCreater.generateHeader();
	}
	
	private SamRecord generateRecord(SAMFileHeader header, String samName, String refId, int start, 
			int end, boolean isPairend, String matRefId, int mateStart, boolean isfirst) {
		String samRecordTxt = "HWI-D00175:261:C6L59ANXX:7:1101:10344:3948	16	chr1	75585	49	40M6D3M2I3M3I72M	*	0	0	CGTACGAGATGAAATTCTCATATACGGTTCTCGGAGGGGGGTTCGGGTTAGTTACCTATCTCAATAAAGTATATGATTGGTTTGAGGAACGTCTTGAGATTCAGGCAATTGCAGATGATATAA	DGDGGGGEGGGGGGGGGGGGGGGGGGGFBD<@GGGGGGGGGGGDGGGGGGGGEGGGGE0GGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGFGGGGGFGGGGGGCCCCC	NM:i:19	MD:Z:0T24A5T8^AGTAAC0C24T11C2A20G16	AS:i:53	XS:i:0";
		SAMRecordFactory samRecordFactory = new DefaultSAMRecordFactory();
		SAMLineParser parser = new SAMLineParser(samRecordFactory, ValidationStringency.STRICT, header, null, null);
		SAMRecord samRecord = null;
		try {
			samRecord = parser.parseLine(samRecordTxt);
		} catch (Exception e) {
			// TODO: handle exception
		}
		samRecord.setReadName(samName);
		samRecord.setReferenceName(refId);
		samRecord.setAlignmentStart(start);
//		samRecord.setAlignmentEnd(end);
		if (isPairend) {
			samRecord.setReadPairedFlag(isPairend);
			samRecord.setMateReferenceName(matRefId);
			samRecord.setMateAlignmentStart(mateStart);
			samRecord.setFirstOfPairFlag(isfirst);
			samRecord.setSecondOfPairFlag(!isfirst);
		}
	
		return new SamRecord(samRecord);
	}
	
	private SamRecord generateRecordSE(SAMFileHeader header, String samName, String refId, int start, 
			int end) {
		String samRecordTxt = "HWI-D00175:261:C6L59ANXX:7:1101:10344:3948	16	chr1	75585	49	40M6D3M2I3M3I72M	*	0	0	CGTACGAGATGAAATTCTCATATACGGTTCTCGGAGGGGGGTTCGGGTTAGTTACCTATCTCAATAAAGTATATGATTGGTTTGAGGAACGTCTTGAGATTCAGGCAATTGCAGATGATATAA	DGDGGGGEGGGGGGGGGGGGGGGGGGGFBD<@GGGGGGGGGGGDGGGGGGGGEGGGGE0GGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGFGGGGGFGGGGGGCCCCC	NM:i:19	MD:Z:0T24A5T8^AGTAAC0C24T11C2A20G16	AS:i:53	XS:i:0";
		SAMRecordFactory samRecordFactory = new DefaultSAMRecordFactory();
		SAMLineParser parser = new SAMLineParser(samRecordFactory, ValidationStringency.STRICT, header, null, null);
		SAMRecord samRecord = null;
		try {
			samRecord = parser.parseLine(samRecordTxt);
		} catch (Exception e) {
			// TODO: handle exception
		}
		samRecord.setReadName(samName);
		samRecord.setReferenceName(refId);
		samRecord.setAlignmentStart(start);
//		samRecord.setAlignmentEnd(end);
		return new SamRecord(samRecord);
	}
}
