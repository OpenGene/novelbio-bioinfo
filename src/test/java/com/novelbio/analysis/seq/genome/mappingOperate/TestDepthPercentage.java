package com.novelbio.analysis.seq.genome.mappingOperate;

import org.junit.Assert;
import org.junit.Test;

public class TestDepthPercentage {
	
	@Test
	public void testCpG() {
		DepthPercentage depthPercentage = new DepthPercentage();
		
		depthPercentage.addCoverageSite(CpGInfo.decodeInt2Cpg(100011));
		depthPercentage.addCoverageSite(CpGInfo.decodeInt2Cpg(200021));
		depthPercentage.addCoverageSite(CpGInfo.decodeInt2Cpg(300031));
		depthPercentage.addCoverageSite(CpGInfo.decodeInt2Cpg(400011));
		depthPercentage.addCoverageSite(CpGInfo.decodeInt2Cpg(500011));
		double[] values = depthPercentage.getCoverageInfo();
		
		Assert.assertArrayEquals(new double[]{1, 1, 1, 0.8, 0.8, 0.6, 0.4}, values, 0.01);
	}
	
}
