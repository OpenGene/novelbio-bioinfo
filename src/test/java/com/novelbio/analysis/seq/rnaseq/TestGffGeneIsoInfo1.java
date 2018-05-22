package com.novelbio.analysis.seq.rnaseq;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.rnaseq.GffGeneCluster;
import com.novelbio.database.domain.modgeneid.GeneType;

/** 主要测试该方法 getAlmostSameIso */
public class TestGffGeneIsoInfo1 extends TestCase {

	GffGeneCluster gffGeneCluster = new GffGeneCluster();
	
	GffDetailGene gffDetailGeneCis = new GffDetailGene("chr1", "test", true);
	GffDetailGene gffDetailGeneTrans = new GffDetailGene("chr1", "test", false);
	
	GffGeneIsoInfo gffGeneIsoInfoRefCis = GffGeneIsoInfo.createGffGeneIso("CisRef", gffDetailGeneCis.getNameSingle(), gffDetailGeneCis, GeneType.mRNA, true);
	GffGeneIsoInfo gffGeneIsoInfoThisCis = GffGeneIsoInfo.createGffGeneIso("CisThis", gffDetailGeneCis.getNameSingle(), gffDetailGeneCis, GeneType.mRNA, true);

	GffGeneIsoInfo gffGeneIsoInfoRefTrans = GffGeneIsoInfo.createGffGeneIso("CisRef", gffDetailGeneTrans.getNameSingle(), gffDetailGeneTrans, GeneType.mRNA, false);
	GffGeneIsoInfo gffGeneIsoInfoThisTrans = GffGeneIsoInfo.createGffGeneIso("CisThis", gffDetailGeneTrans.getNameSingle(), gffDetailGeneTrans, GeneType.mRNA, false);
	
	@Override
	protected void setUp() throws Exception {
//		gffGeneIsoInfoRefCis.se(listName);
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	@Test
	public void testCompareIso() {
		assertFalse();
		assertFalse2();
		assertFalse3();
		assertTrue();
		assertTrue2();
		assertTrue3();
		assertTrue4();
		assertTrue5();
	}
	
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertFalse() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 55));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 110));

		assertEquals(false,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertFalse2() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 89, 110));

		assertEquals(false,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertFalse3() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-------5--------20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 81));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));

		assertEquals(false,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertTrue() {
		boolean cis = true;
		//------------------------------------35--40----------------50-----60------------------------70----80-------------------90-----100---------------
		//--------10-----20-------------30-----40----------------50-----60------------------------70-----80------------------90-----100---------------
	
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
//		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 35, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 99));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));

		assertEquals(true,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertTrue2() {
		boolean cis = true;
		//---------------------------------30-----40----------------50-----60------------------------70----80-------------------90-----100---------------
		//--------10-----20-------------30-----40----------------50-----60------------------------70-----80-----------------------------------------------
	
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
//		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));

		assertEquals(true,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertTrue3() {
		boolean cis = true;
		//---------------------------------30-----40----------------50-----60------------------------70-----80-------------------90-----100---------------
		//--------10-----20-------------30-----40----------------50-----60------------------------70-----80-------------------90-----100---------------
	
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
//		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));

		assertEquals(true,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}

	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertTrue4() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 110));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));

		assertEquals(true,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertTrue5() {
		boolean cis = true;
		//----------------20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 5, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 110));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));

		assertEquals(true,  GffGeneIsoInfo.isExonEdgeSame_NotConsiderBound( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis));
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

}
