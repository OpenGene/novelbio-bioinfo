package uk.ac.babraham.FastQC.Modules;

import junit.framework.TestCase;

public class TestBasicStats extends TestCase {
	
	public void testRead() {
		String fileName = "/home/novelbio/NBCsource/test/fastqc/1WT_BeforeFilterbasicStats.xls";
		BasicStats basicStats = new BasicStats();
		basicStats.readTable(fileName, true);
		
		assertEquals(4676342687l, basicStats.getBaseNum());
		assertEquals(37411683, basicStats.getReadsNum());
		assertEquals(50.0, basicStats.getGCpersentage());
		assertEquals(125, basicStats.getMinLength());
		assertEquals(125, basicStats.getMaxLength());
		assertEquals("Sanger / Illumina 1.9", basicStats.getEncoding());
		
		basicStats = new BasicStats();
		basicStats.readTable(fileName, false);
		
		assertEquals(4676016074l, basicStats.getBaseNum());
		assertEquals(37411683, basicStats.getReadsNum());
		assertEquals(45.32, basicStats.getGCpersentage());
		assertEquals(121, basicStats.getMinLength());
		assertEquals(213, basicStats.getMaxLength());
		assertEquals("Sanger / Illumina 1.9", basicStats.getEncoding());
	}
}
