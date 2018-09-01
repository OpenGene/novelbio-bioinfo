package com.novelbio.test.base.datastructure.listoperate;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.bioinfo.base.binarysearch.ListAbs;
import com.novelbio.bioinfo.gff.ExonClusterOperator;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;

import junit.framework.TestCase;
/**
 * @author zong0jie
 *
 */
public class TestListAbs extends TestCase {
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
		
		List<int[]> lsSep = ExonClusterOperator.getCombSep(false, lsIso, false);
		int num = 0;
		assertEquals(80, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(50, lsSep.get(num)[1]);
		num++;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
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
		
		List<int[]> lsSep = ExonClusterOperator.getCombSep(false, lsIso, false);
		int num = 0;
		assertEquals(80, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
	}
	
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testCisIsoCasstteDouble() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, true);
		//<----------20--30-----------------40-50----52-54--------------60----70----------80-------90---<
		//<----------20---33----35-36----------------------------------55------69-------71---82---84--92---<
		isoTrans1.add(new ExonInfo(true, 20, 30));
		isoTrans1.add(new ExonInfo(true, 40, 50));
		isoTrans1.add(new ExonInfo(true, 52, 54));
		isoTrans1.add(new ExonInfo(true, 60, 70));
		isoTrans1.add(new ExonInfo(true, 80, 90));
		
		isoTrans2.add(new ExonInfo(true, 20, 33));
		isoTrans2.add(new ExonInfo(true, 35, 36));
		isoTrans2.add(new ExonInfo(true, 55, 69));
		isoTrans2.add(new ExonInfo(true, 71, 82));
		isoTrans2.add(new ExonInfo(true, 84, 92));
		
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		List<int[]> lsSep = ExonClusterOperator.getCombSep(true, lsIso, false);
		int num = 0;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
		num++;
		assertEquals(35, lsSep.get(num)[0]);
		assertEquals(36, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(71, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
	}
	
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testCisIsoSep() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, true);
		//<----------20--30-----------------40-50----52-54--------------60----70----------80-------90---<
		//<----------20---33----35-36----------------------------------55------69-------71---82---84--92---<
		isoTrans1.add(new ExonInfo(true, 20, 30));
		isoTrans1.add(new ExonInfo(true, 40, 50));
		isoTrans1.add(new ExonInfo(true, 52, 54));
		isoTrans1.add(new ExonInfo(true, 60, 70));
		isoTrans1.add(new ExonInfo(true, 80, 90));
		
		isoTrans2.add(new ExonInfo(true, 20, 33));
		isoTrans2.add(new ExonInfo(true, 35, 36));
		isoTrans2.add(new ExonInfo(true, 55, 69));
		isoTrans2.add(new ExonInfo(true, 71, 82));
		isoTrans2.add(new ExonInfo(true, 84, 92));
		
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		List<int[]> lsSep = ExonClusterOperator.getCombSep(true, lsIso, true);
		int num = 0;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
		num++;
		assertEquals(35, lsSep.get(num)[0]);
		assertEquals(36, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(50, lsSep.get(num)[1]);
		num++;
		assertEquals(52, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(71, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
	}
	
	/**
	 * 测试ListAbs.getCombSep
	 */
	@Test
	public void testTransIsoSep() {
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "Iso1", GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "Iso2", GeneType.mRNA, false);
		//<----------20--30-----------------40-50----52-54--------------60----70----------80-------90---<
		//<----------20---33----35-36----------------------------------55------69-------71---82---84--92---<
		isoTrans1.add(new ExonInfo(false, 20, 30));
		isoTrans1.add(new ExonInfo(false, 40, 50));
		isoTrans1.add(new ExonInfo(false, 52, 54));
		isoTrans1.add(new ExonInfo(false, 60, 70));
		isoTrans1.add(new ExonInfo(false, 80, 90));
		isoTrans1.sortOnly();
		
		isoTrans2.add(new ExonInfo(false, 20, 33));
		isoTrans2.add(new ExonInfo(false, 35, 36));
		isoTrans2.add(new ExonInfo(false, 55, 69));
		isoTrans2.add(new ExonInfo(false, 71, 82));
		isoTrans2.add(new ExonInfo(false, 84, 92));
		isoTrans2.sortOnly();
		
		ArrayList<GffIso> lsIso = new ArrayList<GffIso>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		List<int[]> lsSep = ExonClusterOperator.getCombSep(false, lsIso, false);
		int num = 0;
		assertEquals(71, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(35, lsSep.get(num)[0]);
		assertEquals(36, lsSep.get(num)[1]);
		num++;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
	}
}
