package com.novelbio.analysis.seq.genome.gffOperate;

import org.junit.Assert;
import org.junit.Test;

public class TestGffHashGTF {
	@Test
	public void testGetIsoName2GeneName() {
		String[] iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5DL_91B56C21D\";transcript_id \"Traes_5DL_91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL_91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5DL_91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5D; L; _91B56C21D\";transcript_id \"Traes_5DL _91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL _91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5D; L; _91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5D; L; _91B56C21D\";\texonnum \"1\"transcript_id \"Traes_5DL _91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL _91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5D; L; _91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("\"gene_id\"\"Contig10000\"; \"transcript_id\"\"Contig100001\"", null);
		Assert.assertEquals("Contig100001", iso2Gene[0]);
		Assert.assertEquals("Contig10000", iso2Gene[1]);
	}
}
