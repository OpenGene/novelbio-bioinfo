package com.novelbio.aoplog;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;

public class TestAopDNAMapping extends TestCase {
	AopDNAMapping aopDNAMapping = new AopDNAMapping();
	SamFile samFile;
	MapDNAint mapDNAint;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		samFile = new SamFile("/home/zong0jie/Atmp/Test/Novelbio Result/1、quality-control_result/MappingInfoaaa.bam");
		mapDNAint = new MapBwaAln();
		SamFileStatistics samFileStatistics = new SamFileStatistics();
		AlignSeqReading alignSeqReading = new AlignSeqReading(samFile);
		alignSeqReading.addAlignmentRecorder(samFileStatistics);
		alignSeqReading.run();
		mapDNAint.addAlignmentRecorder(samFileStatistics);
	}
	
	@Test
	public void test() {
		aopDNAMapping.test(samFile, mapDNAint);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
