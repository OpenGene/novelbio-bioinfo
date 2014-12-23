package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;

import junit.framework.TestCase;

public class TestPindel extends TestCase {
	
	public void testCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		// /media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo
		String path = "/media/hdfs/nbCloud/staff/bianlianle/software/pindel/";
		Pindel pindel = new Pindel();
		pindel.setInputFile("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_reference.fa");
		pindel.setConfigFile("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/simulated_config.txt");
		pindel.setMinInversionSize(60);
		pindel.setMinNTSize(60);
		pindel.setNumberOfThreads(3);
		pindel.setOutputPrefix("/media/hdfs/nbCloud/staff/bianlianle/software/pindel/demo/output/ref");
		String cmd = pindel.getCmdExeStr().toString();   
		assertEquals(path + "pindel -f " +path+ "demo/simulated_reference.fa  -i " + path +"demo/simulated_config.txt -v 60 -n 60 -T 3 -o "+ path + "demo/output/ref", cmd);
	}

}
