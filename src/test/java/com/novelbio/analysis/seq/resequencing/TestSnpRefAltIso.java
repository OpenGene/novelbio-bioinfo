package com.novelbio.analysis.seq.resequencing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.model.modgeneid.GeneType;

public class TestSnpRefAltIso {
	
	@Test
	public void testGetStartEndCis() {
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
	
	@Test
	public void testGetStartEndTrans() {
		GffGeneIsoInfo isoTrans = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneTrans", null, GeneType.mRNA, false);
		//>---20-30------40-50-------60-65-70-------80-85-90-----100-110-----120-125-->
		//>---125-120------110-100-------90-85-80-------70-65-60----50-40-----30-20-->
		isoTrans.add(new ExonInfo( false, 120, 125));
		isoTrans.add(new ExonInfo( false, 100, 110));
		isoTrans.add(new ExonInfo( false, 80, 90));
		isoTrans.add(new ExonInfo( false, 60, 70));
		isoTrans.add(new ExonInfo( false, 40, 50));
		isoTrans.add(new ExonInfo( false, 20, 30));
		isoTrans.setATG(85);
		isoTrans.setUAG(65);

		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 10, "A", "T");
		snpRefAltInfo.setAlignRef(new Align("chr1", 10, 24));
		SnpRefAltIso snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("*23", snpRefAltIso.getStartPosCis());
		assertEquals("*27+d10", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 33, 37));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("*16+3", snpRefAltIso.getStartPosCis());
		assertEquals("*17-3", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 45, 65));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("12", snpRefAltIso.getStartPosCis());
		assertEquals("*11", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 75, 105));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("-11", snpRefAltIso.getStartPosCis());
		assertEquals("6+5", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 112, 119));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("-17+1", snpRefAltIso.getStartPosCis());
		assertEquals("-16-2", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 122, 127));
		snpRefAltIso = new SnpRefAltIsoInsert(snpRefAltInfo, isoTrans);
		assertEquals("-22-u2", snpRefAltIso.getStartPosCis());
		assertEquals("-19", snpRefAltIso.getEndPosCis());
	}
}
