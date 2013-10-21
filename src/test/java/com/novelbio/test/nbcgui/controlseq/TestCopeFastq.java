package com.novelbio.test.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class TestCopeFastq extends TestCase {
	CopeFastq copeFastq;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		copeFastq = new CopeFastq();
	}
	
	@Test
	public void test() {
		List<String> lsCondition = new ArrayList<>();
		List<String> lsLeft = new ArrayList<>();
		List<String> lsRight = new ArrayList<>();
		lsCondition.add("aa");lsCondition.add("aa");lsCondition.add("aa");
		lsCondition.add("bb");lsCondition.add("bb");lsCondition.add("bb");
		lsCondition.add("cc");lsCondition.add("cc");lsCondition.add("cc");
		
		lsLeft.add("/home/zong0jie/Test/fastq/798B_CGATGT_L004_R1_001.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_001.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_002.fastq.gz");
		
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_003.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_004.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_005.fastq.gz");
		
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_006.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_007.fastq.gz");
		lsLeft.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R1_008.fastq.gz");
		
		
		lsRight.add("/home/zong0jie/Test/fastq/798B_CGATGT_L004_R2_001.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_001.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_002.fastq.gz");
		
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_003.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_004.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_005.fastq.gz");
		
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_006.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_007.fastq.gz");
		lsRight.add("/home/zong0jie/Test/fastq/shnc_GGCTAC_L004_R2_008.fastq.gz");
		
		copeFastq.setLsCondition(lsCondition);
		copeFastq.setLsFastQfileLeft(lsLeft);
		copeFastq.setLsFastQfileRight(lsRight);
		copeFastq.setMapCondition2LsFastQLR();
		
		for (String prefix : copeFastq.getLsPrefix()) {
			List<String[]> lsInfo = copeFastq.getMapCondition2LsFastQLR().get(prefix);
			for (String[] strings : lsInfo) {
				System.out.println(prefix + " " + strings[0] + " " + strings[1]);
			}
		}
	
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
