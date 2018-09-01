package com.novelbio.bioinfo.gff;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.database.domain.modgeneid.GeneType;

public class TestGffGeneIsoInfoSort {	
	
	@Test
	public void testsortOnly() {
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 3300));
		gffGeneIsoInfo.add(new ExonInfo(true, 3301, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5200, 5800));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfo.sortAndCombine();
		
		GffIso gffGeneIsoInfoExp = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExp.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExp.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfoExp.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExp.add(new ExonInfo(true, 7000, 8000));
		
		Assert.assertEquals(gffGeneIsoInfoExp, gffGeneIsoInfo);
		
		gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfo.add(new ExonInfo(false, 7000, 7200));
		gffGeneIsoInfo.add(new ExonInfo(false, 7201, 8000));
		gffGeneIsoInfo.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(false, 5200, 5300));
		gffGeneIsoInfo.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfo.sortAndCombine();
		
		gffGeneIsoInfoExp = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoExp.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoExp.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoExp.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoExp.add(new ExonInfo(false, 1000, 2000));
		Assert.assertEquals(gffGeneIsoInfoExp, gffGeneIsoInfo);
	}

	@Test
	public void testGetAtg() {
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfo.setATG(1200);
		gffGeneIsoInfo.sortOnly();
		
		List<ExonInfo> lsAtg = gffGeneIsoInfo.getATGLoc();
		Assert.assertEquals(lsAtg.size(), 1);
		Assert.assertEquals(new ExonInfo(gffGeneIsoInfo, true, 1200, 1202), lsAtg.get(0));
		
		
		gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfo.setATG(3999);
		gffGeneIsoInfo.sortOnly();
		
		lsAtg = gffGeneIsoInfo.getATGLoc();
		Assert.assertEquals(lsAtg.size(), 2);
		Assert.assertEquals(new ExonInfo(gffGeneIsoInfo, true, 3999, 4000), lsAtg.get(0));
		Assert.assertEquals(new ExonInfo(gffGeneIsoInfo, true, 5000, 5000), lsAtg.get(1));
		
		
		gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfo.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfo.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfo.setATG(5000);
		gffGeneIsoInfo.setUAG(3000);

		gffGeneIsoInfo.sortOnly();
		
		lsAtg = gffGeneIsoInfo.getATGLoc();
		Assert.assertEquals(lsAtg.size(), 2);
		Assert.assertEquals(new ExonInfo(gffGeneIsoInfo, false, 5000, 5000), lsAtg.get(0));
		Assert.assertEquals(new ExonInfo(gffGeneIsoInfo, false, 4000, 3999), lsAtg.get(1));
		System.out.println(gffGeneIsoInfo.getGTFformat(null, ""));
	}
}
