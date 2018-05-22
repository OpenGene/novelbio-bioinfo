package com.novelbio.analysis.seq.genome.gffoperate;

import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;

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
		Set<GffDetailGene> lsGffGene = gffCodGeneDU.getCoveredOverlapGffGene();
		for (GffDetailGene gffDetailGene : lsGffGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				System.out.println(gffGeneIsoInfo.getName());
			}
		}
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}
	
	
}
