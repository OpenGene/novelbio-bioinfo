package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class TestStringTie {
	
	@Test
	public void testCmdExeStr() {
	List<String> lsCmd = new ArrayList<>();
	
	String workPath = "/home/novelbio/bianlianle/tmp/";
	String softwarePath = "/home/novelbio/bianlianle/software/";
	String gtfFile = "/home/novelbio/bianlianle/tmp/hg19.gtf";
	String output = "/home/novelbio/bianlianle/tmp/stringtie.result.gtf";
	String inputbam = "/home/novelbio/bianlianle/tmp/sample1.bam";
	String outputDir = "/home/novelbio/bianlianle/tmp/result/";
	String prefix = "sample1";
	int insertSize = 200;
	int gapToNewIso = 50;
	boolean IsJustNovTran = false;
	int minAnchorJuncLen = 10;
	int minJuncCoverage = 2;
	int threadNum = 8;
	int minIsoLen = 200;
	List<String> lsInputFile = new ArrayList<>();
	lsInputFile.add(inputbam);
//	lsInputFile.add("/home/novelbio/bianlianle/tmp/sample2.bam");
//	lsInputFile.add("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_sample_3.bam");
	List<String> lsPrefix = new ArrayList<>();
	lsPrefix.add("sample1");
//	lsPrefix.add("sample2");
//	lsPrefix.add("sample3");
	StringTie stringTie = new StringTie();
	
	stringTie.setGtfFile(gtfFile);
//	stringTie.setIsJustNovTran(IsJustNovTran);
	stringTie.setMinIsoLen(minIsoLen);
	stringTie.setGapToNewIso(gapToNewIso);
	stringTie.setThreadNum(threadNum);

	stringTie.setMinAnchorJuncLen(minAnchorJuncLen);
	stringTie.setMinJuncCoverage(minJuncCoverage);

	String cmd = stringTie.getLsCmd(inputbam).toString();   
//	System.out.println("cmd is " + cmd);
	Assert.assertEquals(softwarePath + "stringtie  -G " + gtfFile +" -j 2 -m 200 -p 1 -g 50 -a 10 -o " + output + " " + workPath + "sample1.bam", cmd);
}
	
	
}
