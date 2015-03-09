package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.resequencing.Pindel;
import com.novelbio.base.cmd.CmdOperate;

import junit.framework.TestCase;

public class TestPindel extends TestCase {
	
//	public void testCmdExeStr() {
//		List<String> lsCmd = new ArrayList<>();
//		String path = "/media/hdfs/nbCloud/staff/bianlianle/software/pindel/";
//		String reference = path + "demo/simulated_reference.fa";
//		int insertSize = 200;
//		List<String> lsInputFile = new ArrayList<>();
//		lsInputFile.add("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_sample_1.bam");
//		lsInputFile.add("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_sample_2.bam");
//		lsInputFile.add("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_sample_3.bam");
//		List<String> lsPrefix = new ArrayList<>();
//		lsPrefix.add("sample1");
//		lsPrefix.add("sample2");
//		lsPrefix.add("sample3");
//		Pindel pindel = new Pindel();
//		pindel.setReference(reference);
//		pindel.setInputFile(lsInputFile);
//		pindel.setLsPrefix(lsPrefix);
//		pindel.setInsertSize(insertSize);
////		pindel.setConfigFile("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_config.txt");
//		pindel.setMinInversionSize(60);
//		pindel.setMinNTSize(60);
//		pindel.setNumberOfThreads(3);
////		pindel.setOutputPrefix("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/output/ref");
//		String cmd = pindel.getCmdExeStr().toString();   
//		assertEquals(path + "pindel -f " +path+ "demo/simulated_reference.fa  -i " + path +"demo/simulated_config.txt -v 60 -n 60 -T 3 -o "+ path + "demo/output/ref", cmd);
//	}

}
