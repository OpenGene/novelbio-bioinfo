package com.novelbio.analysis.seq.genome.mappingOperate;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP.EnumCpGmethyType;

public class TestMapReadsBSP {
	
	@Test
	public void testCalculateCpGInfo() {
		double[] tmpResult = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		double[] result = MapReadsBSP.calculateCpGInfo(tmpResult, 3, null, null);
		Assert.assertArrayEquals(new double[]{2, 5, 8}, result, 0.00001);
			
		result = MapReadsBSP.calculateCpGInfo(tmpResult, 5, null, null);
		Assert.assertArrayEquals(new double[]{1.5, 3.5, 5.5, 7.5, 9.5}, result, 0.00001);
		
		result = MapReadsBSP.calculateCpGInfo(tmpResult, 20, null, null);
		Assert.assertArrayEquals(new double[]{1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10}, result, 0.00001);
	}
	
	@Test
	public void testFilterCpGType() {
		int[] cpGValues = new int[]{0,1001212,-2,1001211};
		int[] cpGResult = CpGInfo.filterCpGType(cpGValues, EnumCpGmethyType.CG);
		Assert.assertArrayEquals(new int[]{0, 1001212, -2, 0}, cpGResult);

	}
}
