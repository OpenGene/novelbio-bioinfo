package com.novelbio.bioinfo.gff;

import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gffchr.GffChrAbs;

public class TestGffCodGeneDU extends TestCase {
	GffHashGene gffHashGene;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		gffHashGene = new GffChrAbs(9606).getGffHashGene(); 
	}
	
	@Test
	public void testCodDu() {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation("chr1",153599872, 153602222);
		gffCodGeneDU.setGeneBody(false);
		gffCodGeneDU.setExon(true);
		Set<GffGene> lsGffGene = gffCodGeneDU.getCoveredOverlapGffGene();
		for (GffGene gffDetailGene : lsGffGene) {
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				System.out.println(gffGeneIsoInfo.getName());
			}
		}
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}
	
	
}
