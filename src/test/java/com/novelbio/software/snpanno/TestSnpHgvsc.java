package com.novelbio.software.snpanno;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.software.snpanno.SnpInfo;
import com.novelbio.software.snpanno.SnpIsoHgvsc;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

public class TestSnpHgvsc {
	
	@Test
	public void testGetStartEndCis() {
		GffIso isoCis = GffIso.createGffGeneIso("Iso1", "geneCis", null, GeneType.mRNA, true);
		//<---20-30------40-50-------60-70-------80-90-----100-110-----120-125<
		isoCis.add(new ExonInfo( true, 20, 30));
		isoCis.add(new ExonInfo( true, 40, 50));
		isoCis.add(new ExonInfo( true, 60, 70));
		isoCis.add(new ExonInfo( true, 80, 90));
		isoCis.add(new ExonInfo( true, 100, 110));
		isoCis.add(new ExonInfo( true, 120, 125));
		isoCis.setATG(65);
		isoCis.setUAG(85);

		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 10, "A", "T");
		snpRefAltInfo.setAlignRef(new Align("chr1", 10, 24));
		SnpIsoHgvsc snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("-27-u10", snpRefAltIso.getStartPosCis());
		assertEquals("-23", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 33, 37));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("-17+3", snpRefAltIso.getStartPosCis());
		assertEquals("-16-3", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 45, 65));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("-11", snpRefAltIso.getStartPosCis());
		assertEquals("1", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 75, 105));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("6+5", snpRefAltIso.getStartPosCis());
		assertEquals("*11", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 112, 119));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("*16+2", snpRefAltIso.getStartPosCis());
		assertEquals("*17-1", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 122, 127));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("*19", snpRefAltIso.getStartPosCis());
		assertEquals("*22+d2", snpRefAltIso.getEndPosCis());
		
		//TAGAC(30)----TAGAC----A(31)
		snpRefAltInfo = new SnpInfo("chr1", 30, "C", "CTAGAC");
		snpRefAltInfo.setAlignRef(new Align("chr1", 30, 31));
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		assertEquals(26, snpRefAltInfo.getStartPosition());
		assertEquals(30, snpRefAltInfo.getEndPosition());

		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoCis);
		assertEquals("-21", snpRefAltIso.getStartPosCis());
		assertEquals("-17", snpRefAltIso.getEndPosCis());
	}
	
	@Test
	public void testGetStartEndTrans() {
		GffIso isoTrans = GffIso.createGffGeneIso("Iso1", "geneTrans", null, GeneType.mRNA, false);
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

		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 10, "A", "T");
		snpRefAltInfo.setAlignRef(new Align("chr1", 10, 24));
		SnpIsoHgvsc snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("*23", snpRefAltIso.getStartPosCis());
		assertEquals("*27+d10", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 33, 37));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("*16+3", snpRefAltIso.getStartPosCis());
		assertEquals("*17-3", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 45, 65));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("12", snpRefAltIso.getStartPosCis());
		assertEquals("*11", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 75, 105));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("-11", snpRefAltIso.getStartPosCis());
		assertEquals("6+5", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 112, 119));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("-17+1", snpRefAltIso.getStartPosCis());
		assertEquals("-16-2", snpRefAltIso.getEndPosCis());
		
		snpRefAltInfo.setAlignRef(new Align("chr1", 122, 127));
		snpRefAltIso = new SnpIsoHgvsc(snpRefAltInfo, isoTrans);
		assertEquals("-22-u2", snpRefAltIso.getStartPosCis());
		assertEquals("-19", snpRefAltIso.getEndPosCis());
	}
}
