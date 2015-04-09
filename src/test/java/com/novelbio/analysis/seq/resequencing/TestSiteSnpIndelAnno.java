package com.novelbio.analysis.seq.resequencing;

import org.junit.Assert;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.model.species.Species;

public class TestSiteSnpIndelAnno {
	GffChrAbs gffChrAbs;
	
	public void before() {
		Species species = new Species(9606);
		species.setVersion("hg19_GRCh37");
		gffChrAbs = new GffChrAbs(species);
	}
	
	public void testSnpAnnoNormCis() {
		SiteSnpIndelAnno siteSnpIndelAnno = new SiteSnpIndelAnno(gffChrAbs);
		String chrId = "chr1";
		int startLoc = 218578574;
		String refNr = "T", thisNr = "A";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("ATG", siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("AAG", siteSnpIndelAnno.getThisNr().toString());
		
		chrId = "chr1";
		startLoc = 218578573;
		refNr = "A"; thisNr = "G";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("ATG".toLowerCase(), siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("GTG".toLowerCase(), siteSnpIndelAnno.getThisNr().toString());
		
		chrId = "chr1";
		startLoc = 218578575;
		refNr = "G"; thisNr = "C";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("ATG".toLowerCase(), siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("ATC".toLowerCase(), siteSnpIndelAnno.getThisNr().toString());
	}
	
	public void testSnpAnnoNormTrans() {
		SiteSnpIndelAnno siteSnpIndelAnno = new SiteSnpIndelAnno(gffChrAbs);
		String chrId = "chr17";
		int startLoc = 7577534;
		String refNr = "C", thisNr = "A";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("AGG", siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("AGT", siteSnpIndelAnno.getThisNr().toString());
		
		chrId = "chr1";
		startLoc = 7577535;
		refNr = "C"; thisNr = "G";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("AGG".toLowerCase(), siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("ACG".toLowerCase(), siteSnpIndelAnno.getThisNr().toString());
		
		chrId = "chr1";
		startLoc = 7577536;
		refNr = "T"; thisNr = "C";
		siteSnpIndelAnno.setSite(chrId, refNr, thisNr);
		siteSnpIndelAnno.anno();
		Assert.assertEquals("AGG".toLowerCase(), siteSnpIndelAnno.getRefNr().toString());
		Assert.assertEquals("GGG".toLowerCase(), siteSnpIndelAnno.getThisNr().toString());
	}
	
	
	
	
}
