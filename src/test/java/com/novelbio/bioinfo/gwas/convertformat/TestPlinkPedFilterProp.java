package com.novelbio.bioinfo.gwas.convertformat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPlinkPedFilterProp {
	
	@Test
	public void testIsNeed() {
		char[] site = new char[] {'A', 'A', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T'};

		PlinkPedFilterProp pedFilterProp = new PlinkPedFilterProp();
		pedFilterProp.setProp(0.2);
		assertEquals(true, pedFilterProp.isNeedSite(site));
		
		pedFilterProp.setProp(0.3);
		assertEquals(false, pedFilterProp.isNeedSite(site));
		
		site = new char[] {'A', 'A', 'T', 'T', 'T', 'N', 'N', 'N', 'N', 'N'};
		pedFilterProp.setProp(0.3);
		assertEquals(true, pedFilterProp.isNeedSite(site));
		
		site = new char[] {'N', 'A', 'T', 'T', 'T', 'N', 'N', 'N', 'N', 'N'};
		pedFilterProp.setProp(0.3);
		assertEquals(false, pedFilterProp.isNeedSite(site));
	}
	
}
