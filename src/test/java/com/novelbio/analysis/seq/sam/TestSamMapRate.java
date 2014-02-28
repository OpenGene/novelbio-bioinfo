package com.novelbio.analysis.seq.sam;

import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestSamMapRate extends TestCase {
	SamMapRate samMapRate = new SamMapRate();
	
	public void testSam() {
//		samMapRate.setAllReads("aa", 15000);
//		samMapRate.setAllReads("bb", 14000);

		SamFileStatistics samFileStatistics = new SamFileStatistics("aa");
		samFileStatistics.allReadsNum = 15000;
		samFileStatistics.mappedReadsNum = 12000;
		samMapRate.addMapInfo("human", samFileStatistics);
		
		samFileStatistics = new SamFileStatistics("aa");
		samFileStatistics.allReadsNum = 15000;
		samFileStatistics.mappedReadsNum = 14000;
		samMapRate.addMapInfo("mouse", samFileStatistics);
		
		samFileStatistics = new SamFileStatistics("aa");
		samFileStatistics.allReadsNum = 15000;
		samFileStatistics.mappedReadsNum = 10000;
		samMapRate.addMapInfo("fish", samFileStatistics);
		
		
		samFileStatistics = new SamFileStatistics("bb");
		samFileStatistics.allReadsNum = 18000;
		samFileStatistics.mappedReadsNum = 11000;
		samMapRate.addMapInfo("human", samFileStatistics);
		
		samFileStatistics = new SamFileStatistics("bb");
		samFileStatistics.allReadsNum = 18000;
		samFileStatistics.mappedReadsNum = 1720300;
		samMapRate.addMapInfo("mouse", samFileStatistics);
		
		samFileStatistics = new SamFileStatistics("bb");
		samFileStatistics.allReadsNum = 18000;
		samFileStatistics.mappedReadsNum = 15000;
		samMapRate.addMapInfo("fish", samFileStatistics);
		
		for (String[] result : samMapRate.getLsResult()) {
			System.out.println(ArrayOperate.cmbString(result, "\t"));
		}
	}
}
