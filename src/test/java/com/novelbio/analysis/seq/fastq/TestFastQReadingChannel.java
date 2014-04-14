package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import junit.framework.TestCase;

public class TestFastQReadingChannel extends TestCase {
	List<FastQ[]> lsFastQs = new ArrayList<>();
	String parentPath = "/media/hdfs/nbCloud/public/test/fastq/";
	
	@Before
	public void beforeCope() {
		List<String[]> lsFastQLR = new ArrayList<>();
		lsFastQLR.add(new String[]{parentPath + "798B_CGATGT_L004_R1_001.fastq.gz", parentPath + "798B_CGATGT_L004_R2_001.fastq.gz"});
		for (String[] strings : lsFastQLR) {
			FastQ[] fastQs = convertFastqFile(strings);
			this.lsFastQs.add(fastQs);
		}
	}
	/** 将输入的文件数组转化为FastQ数组 */
	private static FastQ[] convertFastqFile(String[] fastqFile) {
		if (fastqFile == null) return null;
		
		FastQ[] fastQs = new FastQ[fastqFile.length];
		for (int i = 0; i < fastqFile.length; i++) {
			fastQs[i] = new FastQ(fastqFile[i]);
		}
		return fastQs;
	}
	
	public void testRunning() {
		beforeCope();
		FastQ fastQWrite1 = new FastQ(parentPath + "filter1.fq", true);
		FastQ fastQWrite2 = new FastQ(parentPath + "filter2.fq", true);
		
		FastQReadingChannel fastQReadingChannel = new FastQReadingChannel();
		fastQReadingChannel.setCheckFormat(true);

		fastQReadingChannel.setFastQRead(lsFastQs);
		// QC before Filter
//		fastQReadingChannel.setFastQC(fastQCbefore[0], fastQCbefore[1]);
		fastQReadingChannel.setOutputResult(true);
		FastQFilter fastQfilterRecord = new FastQFilter();
		fastQfilterRecord.setQualityFilter(FastQ.QUALITY_LOW);
		fastQReadingChannel.setFilter(fastQfilterRecord, lsFastQs.get(0)[0].getOffset());
		// QC after Filter
		fastQReadingChannel.setFastQWrite(fastQWrite1, fastQWrite2);
		
		fastQReadingChannel.setThreadNum(8);
		fastQReadingChannel.run();
	}
}