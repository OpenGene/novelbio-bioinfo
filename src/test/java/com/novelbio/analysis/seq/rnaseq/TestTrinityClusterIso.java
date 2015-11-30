package com.novelbio.analysis.seq.rnaseq;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.SepSign;

public class TestTrinityClusterIso {
	
	@Test
	public void testGetGeneName2IsoName() {
		String[] ss = TrinityClusterIso.getGeneName2IsoName("TRINITY_DN22439_c0_g1_i1");
		Assert.assertEquals("DN22439_c0_g1", ss[0]);
		Assert.assertEquals("DN22439_c0_g1" + SepSign.SEP_INFO_SIMPLE + "i1", ss[1]);
	}
}
