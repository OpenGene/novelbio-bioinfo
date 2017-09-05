package com.novelbio.analysis.seq.genome.mappingOperate;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.StringOperate;

public class TestCpGInfo {
	
	@Test
	public void testEncode() {
		CpGInfo cpGInfo = new CpGInfo("Chr7	3000254	-	0	0	CHH	CTC");
		int info = cpGInfo.encodeCpg2Int();
		Assert.assertEquals(-3, info);
		CpGInfo cpGInfo2 = CpGInfo.decodeInt2Cpg(info);
		cpGInfo2.setChrId("Chr7");
		cpGInfo2.setStartEnd(3000254);
		Assert.assertTrue(isEqual(cpGInfo, cpGInfo2));
		
		cpGInfo = new CpGInfo("Chr7	3000254	+	250	190	CHH	CTC");
		info = cpGInfo.encodeCpg2Int();
		Assert.assertEquals(25001903, info);
		cpGInfo2 = CpGInfo.decodeInt2Cpg(info);
		cpGInfo2.setChrId("Chr7");
		cpGInfo2.setStartEnd(3000254);
		Assert.assertTrue(isEqual(cpGInfo, cpGInfo2));
		
		cpGInfo = new CpGInfo("Chr7	3000254	+	25000	19000	CHH	CTC");
		info = cpGInfo.encodeCpg2Int();
		Assert.assertEquals(999975993, info);
		cpGInfo2 = CpGInfo.decodeInt2Cpg(info);
		cpGInfo2.setChrId("Chr7");
		cpGInfo2.setStartEnd(3000254);
		Assert.assertTrue(isEqual(cpGInfo, cpGInfo2));
		
		cpGInfo = new CpGInfo("Chr7	3000254	+	25000	2	CHH	CTC");
		info = cpGInfo.encodeCpg2Int();
		Assert.assertEquals(999900013, info);
		cpGInfo2 = CpGInfo.decodeInt2Cpg(info);
		cpGInfo2.setChrId("Chr7");
		cpGInfo2.setStartEnd(3000254);
		Assert.assertTrue(isEqual(cpGInfo, cpGInfo2));
	}
	
	private static boolean isEqual(CpGInfo info1, CpGInfo info2) {
		return StringOperate.isEqual(info1.chrId, info2.chrId)
				&& info1.isCis5To3 == info2.isCis5To3
				&& info1.depthMethy == info2.depthMethy
				&& info1.depthNonMethy == info2.depthNonMethy
				&& info1.enumCpGmethyType == info2.enumCpGmethyType
				&& info1.startEnd == info2.startEnd;
	}
}
