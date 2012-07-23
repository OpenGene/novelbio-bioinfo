package com.novelbio.test.analysis.seq.rnaseq;

import org.junit.Test;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoTrans;
import com.novelbio.analysis.seq.rnaseq.GffGeneClusterNew;

import junit.framework.TestCase;

public class TestGffGeneCluster extends TestCase{
	GffGeneClusterNew gffGeneCluster = new GffGeneClusterNew();
	
	GffDetailGene gffDetailGeneCis = new GffDetailGene("chr1", "test", true);
	GffDetailGene gffDetailGeneTrans = new GffDetailGene("chr1", "test", false);
	
	GffGeneIsoInfo gffGeneIsoInfoRefCis = new GffGeneIsoCis("CisRef", gffDetailGeneCis, GffGeneIsoInfo.TYPE_GENE_MRNA);
	GffGeneIsoInfo gffGeneIsoInfoThisCis = new GffGeneIsoCis("CisThis", gffDetailGeneCis, GffGeneIsoInfo.TYPE_GENE_MRNA);
	
	GffGeneIsoInfo gffGeneIsoInfoRefTrans = new GffGeneIsoTrans("TransRef", gffDetailGeneTrans, GffGeneIsoInfo.TYPE_GENE_MRNA);
	GffGeneIsoInfo gffGeneIsoInfoThisTrans = new GffGeneIsoTrans("TransThis", gffDetailGeneTrans, GffGeneIsoInfo.TYPE_GENE_MRNA);
	
	@Override
	protected void setUp() throws Exception {
//		gffGeneIsoInfoRefCis.se(listName);
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	@Test
	public void testCompareIso() {
		assertCisIsoMid();
		assertCisIsoHeadHomo();
		assertCisIsoHead();
		assertTransIsoMid();
		assertTransIsoHead();
		assertTransIsoHeadHomo();
	}
	
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoMid() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 25, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 55));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 70, 75));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 90, 100));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 25, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 75), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHeadHomo() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----------15--20-------------30---35---------------45--------60-----------------------70-----80-------------------90--------110---------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90--------110---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 15, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 90, 110));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 110), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 90, 95));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 5, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 70, 75));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 90, 95));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 5, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 75), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHead() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//---------------------------------30---35---------------45--------60-----------------------70-----80------------------------------------------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90--------110---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 70, 80));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//-----------------------------------35--36-----------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20-------------30---36------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 35, 36));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis.getName(), cis, 72, 79));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefCis.getName(), gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 30, 36), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 72, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	
	
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoMid() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 25, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 90, 100));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 25, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoHeadHomo() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----------15--20-------------30---35---------------45--------60-----------------------70-----80-------------------90--------110---------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90--------110---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 15, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 90, 110));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 110), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 90, 95));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(0));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 90, 95));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(0));
	}
	
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoHead() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//---------------------------------30---35---------------45--------60-----------------------70-----80------------------------------------------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20-------------30-----40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//-----------------------------------35--36-----------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20-------------30---36------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 35, 36));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans.getName(), cis, 72, 79));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso(gffGeneIsoInfoRefTrans.getName(), gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 30, 36), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 72, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans.getName(), cis, 90, 100), gffGeneIsoInfoResult.get(0));
	}
	
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
