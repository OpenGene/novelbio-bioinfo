package com.novelbio.analysis.seq.resequencing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.model.modgeneid.GeneType;

public class TestSnpRefAltIso {
	
	@Test
	public void test() {
		GffGeneIsoInfo isoCis = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", null, GeneType.mRNA, true);
		//<---20-30------40-50-------60-70-------80-90-----100-110-----120-125<
		isoCis.add(new ExonInfo( true, 20, 30));
		isoCis.add(new ExonInfo( true, 40, 50));
		isoCis.add(new ExonInfo( true, 60, 70));
		isoCis.add(new ExonInfo( true, 80, 90));
		isoCis.add(new ExonInfo( true, 100, 110));
		isoCis.add(new ExonInfo( true, 120, 125));
		isoCis.setATG(65);
		isoCis.setUAG(85);

		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 10, "A", "T");
		snpRefAltInfo.setAlignRef(new Align("chr1", 10, 24));
		SnpRefAltIso snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("-27-u10", snpRefAltIso.getStartPosCis());
		assertEquals("-23", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 33, 37));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("-17+3", snpRefAltIso.getStartPosCis());
		assertEquals("-16-3", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 45, 65));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("-11", snpRefAltIso.getStartPosCis());
		assertEquals("1", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 75, 105));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("6+5", snpRefAltIso.getStartPosCis());
		assertEquals("*11", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 112, 119));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("*16+2", snpRefAltIso.getStartPosCis());
		assertEquals("*17-1", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 122, 127));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoCis);
		assertEquals("*19", snpRefAltIso.getStartPosCis());
		assertEquals("*22+d2", snpRefAltIso.getEndPosCis());
	}
}
