package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;

import junit.framework.TestCase;

//TODO 还没写完
public class TestTophatJunction extends TestCase {
	TophatJunction tophatJunction = new TophatJunction();
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	@Test
	public void test2() {
		AlignSamReading alignSamReading = new AlignSamReading(new SamFile("/media/winE/NBC/Project/Project_FY/paper/KOod.bam"));
		List<Align> lsAlignments = new ArrayList<>();
		lsAlignments.add(new Align("chr1", 4780733, 4789733));
		alignSamReading.setLsAlignments(lsAlignments);
		
		TophatJunction tophatJunction = new TophatJunction();
		tophatJunction.setCondition("test1", "1");
		alignSamReading.addAlignmentRecorder(tophatJunction);

		alignSamReading.run();
		tophatJunction.conclusion();

//		List<Double> num1 = tophatJunction.getJunctionSite("test1", true, "chr1", 4782733);
		System.out.println();
		
	}
}
