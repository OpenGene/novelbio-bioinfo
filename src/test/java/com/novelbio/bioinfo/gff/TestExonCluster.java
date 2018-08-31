package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.listoperate.ListAbs;

public class TestExonCluster extends TestCase {
	@Before
	public void setUp() {
		
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testTransIsoNorm() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, false);
		//<----------20--30--------------40-50------------------60-70--------------80-90---<
		//<----------20---33--------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(false, 80, 90));
		isoTrans1.add(new ExonInfo(false, 60, 70));
		isoTrans1.add(new ExonInfo(false, 40, 50));
		isoTrans1.add(new ExonInfo(false, 20, 30));
		
		isoTrans2.add(new ExonInfo(false, 80, 92));
		isoTrans2.add(new ExonInfo(false, 55, 69));
		isoTrans2.add(new ExonInfo(false, 20, 33));
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffIso.getExonCluster(false, lsIso);
		int num = 0;
		ExonCluster exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, false, 80, 90), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 80, 92), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, false, 60, 70), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 55, 69), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, false, 40, 50), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans2).size());
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, false, 20, 30), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 20, 33), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		System.out.println(num);
	}
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testTransIsoCasstteDouble() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, false);
		//<----------20--30--------------40-50----52-54--------------60-70--------------80-90---<
		//<----------20---33-----------------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(false, 80, 90));
		isoTrans1.add(new ExonInfo(false, 60, 70));
		isoTrans1.add(new ExonInfo(false, 52, 54));
		isoTrans1.add(new ExonInfo(false, 40, 50));
		isoTrans1.add(new ExonInfo(false, 20, 30));
		
		isoTrans2.add(new ExonInfo(false, 80, 92));
		isoTrans2.add(new ExonInfo(false, 55, 69));
		isoTrans2.add(new ExonInfo(false, 20, 33));
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffIso.getExonCluster(false, lsIso);
		int num = 0;
		ExonCluster exonCluster = lsSep.get(num);
		assertEquals(null, exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans1));
		assertEquals(new ExonInfo(isoTrans1, false, 80, 90), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 80, 92), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(null, exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans2));
		assertEquals(new ExonInfo(isoTrans1, false, 60, 70), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 55, 69), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new Integer(1), exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans2));
		assertEquals(new ExonInfo(isoTrans1, false, 52, 54), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, false, 40, 50), exonCluster.getMapIso2LsExon().get(isoTrans1).get(1));
		
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans2).size());
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, false, 20, 30), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, false, 20, 33), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		System.out.println(num);
	}
	
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testCisIsoCasstteDouble() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, true);
		//>--1-3-4-5-----------20--30-----------------40-50----52-54--------------60----70----------80-------90--->
		//>---------------7-8---20---33----35-36----------------------------------55------69-------71---82---84--92--->
		isoTrans1.add(new ExonInfo(true, 1, 3));
		isoTrans1.add(new ExonInfo(true, 4, 5));
		isoTrans1.add(new ExonInfo(true, 20, 30));
		isoTrans1.add(new ExonInfo(true, 40, 50));
		isoTrans1.add(new ExonInfo(true, 52, 54));
		isoTrans1.add(new ExonInfo(true, 60, 70));
		isoTrans1.add(new ExonInfo(true, 80, 90));
		
		isoTrans2.add(new ExonInfo(true, 7, 8));
		isoTrans2.add(new ExonInfo(true, 20, 33));
		isoTrans2.add(new ExonInfo(true, 35, 36));
		isoTrans2.add(new ExonInfo(true, 55, 69));
		isoTrans2.add(new ExonInfo(true, 71, 82));
		isoTrans2.add(new ExonInfo(true, 84, 92));
		
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffIso.getExonCluster(true, lsIso);
		int num = 0;
		ExonCluster exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 1, 3), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 4, 5), exonCluster.getMapIso2LsExon().get(isoTrans1).get(1));
		assertEquals(null, exonCluster.getMapIso2LsExon().get(isoTrans2));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(null, exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans2));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans1).size());
		assertEquals(new ExonInfo(isoTrans2, true, 7, 8), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 20, 30), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, true, 20, 33), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new Integer(2), exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans1));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans1).size());
		assertEquals(new ExonInfo(isoTrans2, true, 35, 36), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new Integer(2), exonCluster.getMapIso2ExonIndexSkipTheCluster().get(isoTrans2));
		assertEquals(new ExonInfo(isoTrans1, true, 40, 50), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 52, 54), exonCluster.getMapIso2LsExon().get(isoTrans1).get(1));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans2).size());
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 60, 70), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, true, 55, 69), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 80, 90), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans2, true, 71, 82), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		assertEquals(new ExonInfo(isoTrans2, true, 84, 92), exonCluster.getMapIso2LsExon().get(isoTrans2).get(1));

	}
	
	/**
	 * 测试getExonNumInfo
	 */
	@Test
	public void testCisExonOnNum() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, true);
		//>--1-3-4-5-----------20--30-----------------40-50----52-54--------------60----70---------79-------90--->
		//>---------------7-8---20---33----35-36----------------------------------55------69-------71---81---84--92--->
		isoTrans1.add(new ExonInfo(true, 1, 3));
		isoTrans1.add(new ExonInfo(true, 4, 5));
		isoTrans1.add(new ExonInfo(true, 20, 30));
		isoTrans1.add(new ExonInfo(true, 40, 50));
		isoTrans1.add(new ExonInfo(true, 52, 54));
		isoTrans1.add(new ExonInfo(true, 60, 70));
		isoTrans1.add(new ExonInfo(true, 79, 90));
		
		isoTrans2.add(new ExonInfo(true, 7, 8));
		isoTrans2.add(new ExonInfo(true, 20, 33));
		isoTrans2.add(new ExonInfo(true, 35, 36));
		isoTrans2.add(new ExonInfo(true, 55, 69));
		isoTrans2.add(new ExonInfo(true, 71, 81));
		isoTrans2.add(new ExonInfo(true, 84, 92));
		
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		List<int[]> lsBound = ListAbs.getSep(true, lsIso);
		int result = ExonCluster.getExonNumInfo(true, 37, 38, lsBound);
		assertEquals(-5, result);

		result = ExonCluster.getExonNumInfo(true, 80 ,82, lsBound);
		assertEquals(9, result);
		
		result = ExonCluster.getExonNumInfo(true, 82 ,83, lsBound);
		assertEquals(-9, result);
		
		result = ExonCluster.getExonNumInfo(true, -2 ,-1, lsBound);
		assertEquals(0, result);
		
		result = ExonCluster.getExonNumInfo(true, 94 ,95, lsBound);
		assertEquals(11, result);
	}
	
	/**
	 * 测试getExonNumInfo
	 */
	@Test
	public void testTransExonOnNum() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, false);
		//>--1-3-4-5-----------20--30-----------------40-50----52-54--------------60----70---------79-------90--->
		//>---------------7-8---20---33----35-36----------------------------------55------69-------71---81---84--92--->
		isoTrans1.add(new ExonInfo(false, 1, 3));
		isoTrans1.add(new ExonInfo(false, 4, 5));
		isoTrans1.add(new ExonInfo(false, 20, 30));
		isoTrans1.add(new ExonInfo(false, 40, 50));
		isoTrans1.add(new ExonInfo(false, 52, 54));
		isoTrans1.add(new ExonInfo(false, 60, 70));
		isoTrans1.add(new ExonInfo(false, 79, 90));
		isoTrans1.sort();
		
		isoTrans2.add(new ExonInfo(false, 7, 8));
		isoTrans2.add(new ExonInfo(false, 20, 33));
		isoTrans2.add(new ExonInfo(false, 35, 36));
		isoTrans2.add(new ExonInfo(false, 55, 69));
		isoTrans2.add(new ExonInfo(false, 71, 81));
		isoTrans2.add(new ExonInfo(false, 84, 92));
		isoTrans2.sort();
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		List<int[]> lsBound = ListAbs.getSep(false, lsIso);
		int result = ExonCluster.getExonNumInfo(false, 37, 38, lsBound);
		assertEquals(-5, result);

		result = ExonCluster.getExonNumInfo(false, 80 ,82, lsBound);
		assertEquals(2, result);
		
		result = ExonCluster.getExonNumInfo(false, 82 ,83, lsBound);
		assertEquals(-1, result);
		
		result = ExonCluster.getExonNumInfo(false, 95 ,96, lsBound);
		assertEquals(0, result);
		
		result = ExonCluster.getExonNumInfo(false, -2 ,-1, lsBound);
		assertEquals(11, result);
	}
}
