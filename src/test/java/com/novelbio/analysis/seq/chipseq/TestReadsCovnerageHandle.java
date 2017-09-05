package com.novelbio.analysis.seq.chipseq;

import org.junit.Assert;
import org.junit.Test;


public class TestReadsCovnerageHandle {
	
	@Test
	public void testComb() {
		ReadsCovnerageHandle readsCovnerageHandle = new ReadsCovnerageHandle();
		readsCovnerageHandle.setBinNum(4);
		readsCovnerageHandle.setValue(new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
		Assert.assertArrayEquals(convertToDouble(new double[]{2.5, 6.5, 10.5, 14}), convertToDouble(readsCovnerageHandle.combineInputValues()));
		
		readsCovnerageHandle.setBinNum(4);
		readsCovnerageHandle.setValue(new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14});
		Assert.assertArrayEquals(convertToDouble(new double[]{2.5, 6.5, 10.5}), convertToDouble(readsCovnerageHandle.combineInputValues()));
	}
	
	private Double[] convertToDouble(double[] values) {
		Double[] result = new Double[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values[i];
		}
		return result;
	}
	
}
