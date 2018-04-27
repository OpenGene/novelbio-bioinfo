package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoTrans;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.domain.species.Species;

import junit.framework.TestCase;

public class TestLncInfo extends TestCase {
	public void testCalculate() {
		GffDetailGene gffDetailGene = new GffDetailGene("chr1", "testGene", true);
		gffDetailGene.setStartAbs(1000);
		gffDetailGene.setEndAbs(2000);
		
		GffGeneIsoInfo iso1 = new GffGeneIsoCis("testIso1", "testGene", GeneType.mRNA);
		iso1.add(new ExonInfo(true, 1000, 1100));
		iso1.add(new ExonInfo(true, 1200, 1300));
		iso1.add(new ExonInfo(true, 1500, 1600));
		iso1.add(new ExonInfo(true, 1900, 2000));
		
		GffGeneIsoInfo iso2 = new GffGeneIsoCis("testIso2", "testGene", GeneType.mRNA);
		iso2.add(new ExonInfo(true, 1000, 1100));
		iso2.add(new ExonInfo(true, 1200, 1300));
		iso2.add(new ExonInfo(true, 1700, 1800));
		iso2.add(new ExonInfo(true, 1900, 2000));

		
		GffGeneIsoInfo iso3 = new GffGeneIsoCis("testIso3", "testGene", GeneType.ncRNA);
		iso3.add(new ExonInfo(true, 1000, 1120));
		iso3.add(new ExonInfo(true, 1210, 1300));
		iso3.add(new ExonInfo(true, 1810, 1930));
		iso3.add(new ExonInfo(true, 1980, 2000));
//		
		GffGeneIsoInfo iso4 = new GffGeneIsoTrans("testIso4", "testGene", GeneType.ncRNA);
		iso4.add(new ExonInfo(false, 1000, 1100));
		iso4.add(new ExonInfo(false, 1200, 1300));
		iso4.add(new ExonInfo(false, 1500, 1600));
		iso4.add(new ExonInfo(false, 1900, 2000));
		
		GffGeneIsoInfo iso5 = new GffGeneIsoTrans("testIso5", "testGene", GeneType.mRNA);
		iso5.add(new ExonInfo(false, 1000, 1100));
		iso5.add(new ExonInfo(false, 1200, 1300));
		iso5.add(new ExonInfo(false, 1500, 1600));
		iso5.add(new ExonInfo(false, 1900, 2000));
		
		gffDetailGene.addIso(iso1);
		gffDetailGene.addIso(iso5);
		gffDetailGene.addIso(iso2);
		gffDetailGene.addIso(iso3);
		gffDetailGene.addIso(iso4);
		
		Set<GffGeneIsoInfo> setLncExpect = new HashSet<>();
		setLncExpect.add(iso3);
//		setLncExpect.add(iso4);
//		setLncExpect.add(iso5);
		
//		List<GffGeneIsoInfo> lsLnc = LncInfo.getLncIso(gffDetailGene);
//		Set<GffGeneIsoInfo> setLncResult = new HashSet<>(lsLnc);
//		
//		
//		assertEquals(setLncExpect, setLncResult);
		
	}
}
