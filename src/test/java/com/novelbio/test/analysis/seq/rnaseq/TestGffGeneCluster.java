package com.novelbio.test.analysis.seq.rnaseq;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoTrans;
import com.novelbio.analysis.seq.rnaseq.GffGeneCluster;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.TestCase;
/**
 * 首先设定
	int boundMaxFalseGapBp = 1;

	int boundMaxFalseGapBpTail = 1;
	
 * @author zong0jie
 *
 */
public class TestGffGeneCluster extends TestCase{
	GffGeneCluster gffGeneCluster = new GffGeneCluster();
	
	GffDetailGene gffDetailGeneCis = new GffDetailGene("chr1", "test", true);
	GffDetailGene gffDetailGeneTrans = new GffDetailGene("chr1", "test", false);
	
	GffGeneIsoInfo gffGeneIsoInfoRefCis = new GffGeneIsoCis("CisRef", gffDetailGeneCis, GeneType.mRNA);
	GffGeneIsoInfo gffGeneIsoInfoThisCis = new GffGeneIsoCis("CisThis", gffDetailGeneCis, GeneType.mRNA);
	
	GffGeneIsoInfo gffGeneIsoInfoRefTrans = new GffGeneIsoTrans("TransRef", gffDetailGeneTrans, GeneType.mRNA);
	GffGeneIsoInfo gffGeneIsoInfoThisTrans = new GffGeneIsoTrans("TransThis", gffDetailGeneTrans, GeneType.mRNA);
	
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
		assertCisIsoHead2();
		assertTransIsoMid();
		assertTransIsoHead();
		assertTransIsoHeadHomo();
		assertCisIsoNull();
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
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 25, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 55));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 75));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 90, 100));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 25, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 75), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoNull() {
		boolean cis = true;
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		gffGeneIsoInfoThisCis = new GffGeneIsoCis("CisThis", gffDetailGeneCis, GeneType.mRNA);
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, null);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHeadHomo() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----------15--20-------------30---35---------------45--------60-----------------------70-----80-------------------91--------110---------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90--------110---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 15, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 91, 110));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 110), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 90, 95));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 5, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 75));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 90, 95));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 5, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 75), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHead2() {
		boolean cis = true;
		//--------10-----20--------------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//----------------------25--26---------30---35---------------45--------60-----------------------70-----80-----83--84---------------------------------
		//--------10-----20--25--26---------30--35----------------45--------60------------------------70----80-----83--84-----90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 25, 26));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 83, 84));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(7, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 25, 26), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 83, 84), gffGeneIsoInfoResult.get(5));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(6));
		

		//-----------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--5--6----------------------------30---35--------------- 45-------60-----------------------70-----80----------------------------------------110---120------------
		//--5--6----------------------------30---35----------------45--------60-----------------------70----80----------------------------------------110---120----------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 5, 6));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 110, 120));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 5, 6), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 110, 120), gffGeneIsoInfoResult.get(4));
		
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
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 70, 80));

		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40-------------------50----60---------------------70---------80--------------------------------------------------
		//-----------------------------------35--36-----------------45-------60----------------------72-----79----------------------90-----100---------------------
		//--------10-----20-------------30---36------------------45-------60----------------------72--------80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 35, 36));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 72, 79));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 36), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 72, 79), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40-------------------50----60---------------------70-------80--------------------90-----100---------------------
		//-----------------------------------35--36-----------------45-------60-----------------------75---77
		//--------10-----20-------------30---36------------------45-------60-----------------------75-----80--------------------90-----100---------------
		gffGeneIsoInfoRefCis.clear();
		gffGeneIsoInfoThisCis.clear();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100));

		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 35, 36));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(gffGeneIsoInfoThisCis, cis, 75, 77));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 30, 36), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 75, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefCis, cis, 90, 100), gffGeneIsoInfoResult.get(4));
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
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 25, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 100));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 25, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----39----------------49----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25---------40---------------50-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 25, 39));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 49, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 100));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 25, 40), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35-------------------49----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25-----35--------------------50-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 25, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 49, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 100));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 25, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
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
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 15, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 110));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 110), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 95));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 90, 95));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
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
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 70, 80));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20----------30-----------40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//                                  35--36-------------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20----------30------36--------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 35, 36));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 72, 79));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 36), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 72, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		
		
		//--------10-----20-----------------30------40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//--------------------------25--26----35--36-----------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20------25--26----35--36------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clear();
		gffGeneIsoInfoThisTrans.clear();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 25, 26));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 35, 36));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(gffGeneIsoInfoThisTrans, cis, 72, 79));
		gffGeneIsoInfoThisTrans.sort();
		gffGeneIsoInfoRefTrans.sort();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(6, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 10, 20), gffGeneIsoInfoResult.get(5));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 25, 26), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 35, 36), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 72, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoRefTrans, cis, 90, 100), gffGeneIsoInfoResult.get(0));
	}
	
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
