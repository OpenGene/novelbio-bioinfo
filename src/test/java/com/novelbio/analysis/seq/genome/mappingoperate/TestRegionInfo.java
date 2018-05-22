package com.novelbio.analysis.seq.genome.mappingoperate;

import com.novelbio.analysis.seq.genome.mappingoperate.RegionInfo;
import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestRegionInfo extends TestCase {
	public void testToStringcis() {
		RegionInfo regionInfo = new RegionInfo("chr1", 21300, 21500);
		regionInfo.setCis5to3(true);
		
		double[] value = new double[]{1.0,2.0,3.0};
		regionInfo.setDouble(value);
		
		String out = regionInfo.toString();
		
		RegionInfo regionInfo2 = new RegionInfo();
		regionInfo2.readFromStr(out);
		
		assertEquals(ArrayOperate.cmbString(new String[]{"null","chr1", "21300", "21500", "true", "0.0", "0", "1.0", "2.0", "3.0"}, "\t"), regionInfo.toString());
		assertEquals(true, regionInfo.equals(regionInfo2));
	}
	
	public void testToStringtrans() {
		RegionInfo regionInfo = new RegionInfo("chr1", 21300, 21500);
		regionInfo.setCis5to3(false);
		regionInfo.setName("test");
		double[] value = new double[]{1.0,2.0,3.0};
		regionInfo.setDouble(value);
		
		String out = regionInfo.toString();
		
		RegionInfo regionInfo2 = new RegionInfo();
		regionInfo2.readFromStr(out);
		
		assertEquals(ArrayOperate.cmbString(new String[]{"test","chr1", "21300", "21500", "false", "0.0", "0", "3.0", "2.0", "1.0"}, "\t"), regionInfo.toString());
		assertEquals(true, regionInfo.equals(regionInfo2));
	}
}
