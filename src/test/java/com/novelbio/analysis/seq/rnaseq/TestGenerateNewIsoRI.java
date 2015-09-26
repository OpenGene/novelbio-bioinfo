package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.types.resources.Sort;
import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.base.SepSign;
import com.novelbio.database.model.modgeneid.GeneType;

/** 主要测试重建RI部分 */
public class TestGenerateNewIsoRI {
	
	@Test
	public void testGetLsJuncUnitNoOverlap() {
		testGenerateGene(true);
		testGenerateGene(false);
	}
	
	private void testGenerateGene(boolean isCis5To3) {

		// ---------500===600-------------------------------------900===1000------------------1200========1400------------------------------1900===2000------
		// -----------------550===680------700===800-------900===1000------------------1200===1300-------------------------------1850==1960----------
		// ---------500===600--------------700===800-------900==================1250---------------------------------------1850=========2000------
		GffDetailGene gffDetailGene1Cis = new GffDetailGene("chr1", "Test", isCis5To3);		
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, isCis5To3);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1200, 1400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1900, 2000));
		gffGeneIsoInfo1.sort();
		gffDetailGene1Cis.addIso(gffGeneIsoInfo1);
		
		GffGeneIsoInfo gffGeneIsoInfo2 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, isCis5To3);
		gffGeneIsoInfo2.add(new ExonInfo(gffGeneIsoInfo2.isCis5to3(), 550, 680));
		gffGeneIsoInfo2.add(new ExonInfo(gffGeneIsoInfo2.isCis5to3(), 700, 800));
		gffGeneIsoInfo2.add(new ExonInfo(gffGeneIsoInfo2.isCis5to3(), 900, 1000));
		gffGeneIsoInfo2.add(new ExonInfo(gffGeneIsoInfo2.isCis5to3(), 1200, 1300));
		gffGeneIsoInfo2.add(new ExonInfo(gffGeneIsoInfo2.isCis5to3(), 1850, 1960));
		gffGeneIsoInfo2.sort();
		gffDetailGene1Cis.addIso(gffGeneIsoInfo2);
	
		GffGeneIsoInfo gffGeneIsoInfo3 = GffGeneIsoInfo.createGffGeneIso("test3", "test", GeneType.mRNA, isCis5To3);
		gffGeneIsoInfo3.add(new ExonInfo(gffGeneIsoInfo3.isCis5to3(), 500, 600));
		gffGeneIsoInfo3.add(new ExonInfo(gffGeneIsoInfo3.isCis5to3(), 700, 800));
		gffGeneIsoInfo3.add(new ExonInfo(gffGeneIsoInfo3.isCis5to3(), 900, 1250));
		gffGeneIsoInfo3.add(new ExonInfo(gffGeneIsoInfo3.isCis5to3(), 1850, 2000));
		gffGeneIsoInfo3.sort();
		gffDetailGene1Cis.addIso(gffGeneIsoInfo3);
		
		GenerateNewIso generateNewIso = new GenerateNewIso(null, null, null, true);
		generateNewIso.setGffDetailGene(gffDetailGene1Cis);
		List<JunctionUnit> lsJunctionUnits = generateNewIso.getLsJuncUnitNoOverlap();
		Set<String> setJuncInfo = getJuncKey(lsJunctionUnits);
		
		List<JunctionUnit> lsJuncUnitExpect = new ArrayList<>();
		lsJuncUnitExpect.add(new JunctionUnit("chr1", 681, 699, isCis5To3));
		lsJuncUnitExpect.add(new JunctionUnit("chr1", 801, 899, isCis5To3));
		lsJuncUnitExpect.add(new JunctionUnit("chr1", 1001, 1199, isCis5To3));
		lsJuncUnitExpect.add(new JunctionUnit("chr1", 1401, 1899, isCis5To3));
		
		Set<String> setJuncExp = getJuncKey(lsJuncUnitExpect);
		Assert.assertEquals(setJuncExp, setJuncInfo);
		
		List<JunctionUnit> lsJuncNeedReconstruct = new ArrayList<>();
		lsJuncNeedReconstruct.add(new JunctionUnit("chr1", 681, 699, isCis5To3));
		lsJuncNeedReconstruct.add(new JunctionUnit("chr1", 801, 899, isCis5To3));
		lsJuncNeedReconstruct.add(new JunctionUnit("chr1", 1001, 1199, isCis5To3));
		lsJuncNeedReconstruct.add(new JunctionUnit("chr1", 1401, 1899, isCis5To3));
		
		Map<JunctionUnit, GffGeneIsoInfo> mapJunc2Iso = generateNewIso.getMapJunc2IsoNeedReconstruct(lsJuncNeedReconstruct);
		Assert.assertEquals(3, mapJunc2Iso.size());
		List<JunctionUnit> lsJunUnits = new ArrayList<>(mapJunc2Iso.keySet());
		Collections.sort(lsJunUnits, new Comparator<JunctionUnit>() {
			public int compare(JunctionUnit o1, JunctionUnit o2) {
				Integer o1Start = o1.getStartAbs(), o2Start = o2.getStartAbs();
				return o1Start.compareTo(o2Start);
			}
		});
		Iterator<JunctionUnit> itJunc = lsJunUnits.iterator();
		JunctionUnit juncUnit1 = itJunc.next();
		Assert.assertEquals("681@699", juncUnit1.getStartAbs() + "@" + juncUnit1.getEndAbs());
		Assert.assertEquals("test2", mapJunc2Iso.get(juncUnit1).getName());
		GffGeneIsoInfo gffGeneIsoInfo = GenerateNewIso.reconstructIso(juncUnit1, mapJunc2Iso.get(juncUnit1));
		Assert.assertEquals(4, gffGeneIsoInfo.size());
		int num = isCis5To3? 0 : 3;
		ExonInfo exonInfo = gffGeneIsoInfo.get(num);
		Assert.assertEquals("550@800", exonInfo.getStartAbs() + "@" + exonInfo.getEndAbs());
		
		JunctionUnit juncUnit2 = itJunc.next();
		Assert.assertEquals("801@899", juncUnit2.getStartAbs() + "@" + juncUnit2.getEndAbs());
		Assert.assertEquals("test2", mapJunc2Iso.get(juncUnit2).getName());
		gffGeneIsoInfo = GenerateNewIso.reconstructIso(juncUnit2, mapJunc2Iso.get(juncUnit2));
		Assert.assertEquals(4, gffGeneIsoInfo.size());
		num = isCis5To3? 1 : 2;
		exonInfo = gffGeneIsoInfo.get(num);
		Assert.assertEquals("700@1000", exonInfo.getStartAbs() + "@" + exonInfo.getEndAbs());
		
		JunctionUnit juncUnit3 = itJunc.next();
		Assert.assertEquals("1401@1899", juncUnit3.getStartAbs() + "@" + juncUnit3.getEndAbs());
		Assert.assertEquals("test1", mapJunc2Iso.get(juncUnit3).getName());
		gffGeneIsoInfo = GenerateNewIso.reconstructIso(juncUnit3, mapJunc2Iso.get(juncUnit3));
		Assert.assertEquals(3, gffGeneIsoInfo.size());
		num = isCis5To3? 2 : 0;
		exonInfo = gffGeneIsoInfo.get(num);
		Assert.assertEquals("1200@2000", exonInfo.getStartAbs() + "@" + exonInfo.getEndAbs());

	}
	
	private Set<String> getJuncKey(List<JunctionUnit> lsJunctionUnits) {
		Set<String> setJuncInfo = new HashSet<>();
		for (JunctionUnit juncUnit : lsJunctionUnits) {
			setJuncInfo.add(juncUnit.getStartCis() + SepSign.SEP_ID + juncUnit.getEndCis() + SepSign.SEP_ID + juncUnit.isCis5to3());
		}
		return setJuncInfo;
	}
}
