package com.novelbio.analysis.gwas;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.novelbio.analysis.gwas.combinesnp.LDcalculate;

import smile.clustering.HierarchicalClustering;
import smile.clustering.SIB;
import smile.clustering.linkage.Linkage;
import smile.clustering.linkage.UPGMALinkage;

public class TestLDcalculate {
	
	@Test
	public void testR21() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A A A A a a a a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B b b b B B B b b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		assertEquals(0, lDcalculate.getR2(), 0.01);
		assertEquals(0, lDcalculate.getDdot(), 0.01);
	}
	
	@Test
	public void testR22() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A A A A A A A a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B b b b b b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		assertEquals(0.33, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	
	@Test
	public void testR23() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A A A A A A A a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B b b b b b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		assertEquals(0.333, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	
	@Test
	public void testR24() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A A A A A A A A a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B b b b b b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		
		assertEquals(0.2, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	@Test
	public void testR25() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A a a a A A A a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B B B B b b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		
		assertEquals(0.333, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	@Test
	public void testR26() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A a a a A A A a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B B B B B b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		
		assertEquals(0.2, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	@Test
	public void testR27() {
		List<String[]> lsRef2AltSite1 = getLsSnps("A A A A A A A A A a a a");
		List<String[]> lsRef2AltSite2 = getLsSnps("B B B B B B B B B B b b");
		LDcalculate lDcalculate = generateLDinfo(lsRef2AltSite1, lsRef2AltSite2);
		
		assertEquals(0.6, lDcalculate.getR2(), 0.01);
		assertEquals(1, lDcalculate.getDdot(), 0.01);
	}
	
	private List<String[]> getLsSnps(String info) {
		List<String[]> lsResult = new ArrayList<>();
		String[] ss = info.split(" ");
		for (String string : ss) {
			lsResult.add(new String[] {string, string});
		}
		return lsResult;
	}
	
	private LDcalculate generateLDinfo(List<String[]> lsRef2AltSite1, List<String[]> lsRef2AltSite2) {
		LDcalculate lDcalculate = new LDcalculate();
		lDcalculate.setRefa("A");
		lDcalculate.setRefb("B");
		lDcalculate.setLsRef2AltSite1(lsRef2AltSite1);
		lDcalculate.setLsRef2AltSite2(lsRef2AltSite2);
		lDcalculate.calculate();
		return lDcalculate;
	}

}
