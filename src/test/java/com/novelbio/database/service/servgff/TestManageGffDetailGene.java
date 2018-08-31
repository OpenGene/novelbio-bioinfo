package com.novelbio.database.service.servgff;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.TestCase;

import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.database.domain.species.Species;

public class TestManageGffDetailGene extends TestCase {
	public static void main(String[] args) {
		MgmtGffDetailGene manageGffDetailGene = MgmtGffDetailGene.getInstance();
		Species species = new Species(9606);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
//		manageGffDetailGene.saveGffChrInterval(gffChrAbs.getGffHashGene());
	}
	public void testCase() {
		MgmtGffDetailGene manageGffDetailGene = MgmtGffDetailGene.getInstance();
//		List<GffDetailGene> ls = manageGffDetailGene.searchRegionOverlap(9606, "hg19_NCBI", "ncbi", "chr1", 10954, 11507);
//		System.out.println("run");
//		assertEquals(1, ls.size());
//		assertEquals("LOC100506145", ls.get(0).getNameSingle());
		Species species = new Species(9606);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
	}
}
