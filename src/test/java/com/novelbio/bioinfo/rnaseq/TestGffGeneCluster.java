package com.novelbio.bioinfo.rnaseq;

import org.junit.Test;

import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffIsoCis;
import com.novelbio.bioinfo.gff.GffIsoTrans;
import com.novelbio.bioinfo.rnaseq.GffGeneCluster;
import com.novelbio.database.domain.modgeneid.GeneType;

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
	
	GffGene gffDetailGeneCis = new GffGene("chr1", "test", true);
	GffGene gffDetailGeneTrans = new GffGene("chr1", "test", false);
	
	GffIso gffGeneIsoInfoRefCis = GffIso.createGffGeneIso("CisRef", gffDetailGeneCis.getNameSingle(), gffDetailGeneCis, GeneType.mRNA, true);
	GffIso gffGeneIsoInfoThisCis = GffIso.createGffGeneIso("CisThis", gffDetailGeneCis.getNameSingle(), gffDetailGeneCis, GeneType.mRNA, true);

	GffIso gffGeneIsoInfoRefTrans = GffIso.createGffGeneIso("CisRef", gffDetailGeneTrans.getNameSingle(), gffDetailGeneTrans, GeneType.mRNA, false);
	GffIso gffGeneIsoInfoThisTrans = GffIso.createGffGeneIso("CisThis", gffDetailGeneTrans.getNameSingle(), gffDetailGeneTrans, GeneType.mRNA, false);
	
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
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----95---------------
		//-----------------------------------35---40----------------50-----60-----------------------70-----80-------------------90-----120---------------
		//--------10----20--------------30-----40----------------50-----60-----------------------70-----80-------------------90-----120---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 95));
		
//		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 35, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 120));

		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 120), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoNull() {
		boolean cis = true;
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		gffGeneIsoInfoThisCis = GffIso.createGffGeneIso("CisThis", gffDetailGeneCis.getNameSingle(), gffDetailGeneCis, GeneType.mRNA, true);
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));

		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, null);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHeadHomo() {
		boolean cis = true;
		//---------------------------------35--40---------------50------60-----------------------70-----80-------------------90-----100---------------
		//--------15-----20-------------30-----40---------------50------60-----------------------70-----80-------------------91---------120---------------
		//--------15-----20-------------30-----40---------------50------60-----------------------70-----80-------------------91---------120---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 35, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 15, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 91, 120));

		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 15, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 40), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 91, 120), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95----100----125-----------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90----95----100----125----------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		

		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 79));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 5, 20));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 95));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 100, 125));
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(6, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 5, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 95), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 100, 125), gffGeneIsoInfoResult.get(5));
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//---------------------------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//--------10-----20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 75));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 95));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 75), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHead2() {
		boolean cis = true;
		//--------10-----20--------------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//----------------------25--26---------30---35---------------45--------60-----------------------70-----80-----83--84---------------------------------
		//----------------------25--26---------30--35----------------45--------60------------------------70----80-----83--84---------------------------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 25, 26));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 83, 84));

		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(7, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 25, 26), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 83, 84), gffGeneIsoInfoResult.get(5));
		

		//-----------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--5--6----------------------------30---35--------------- 45-------60-----------------------70-----80----------------------------------------110---120------------
		//--5--6----------------------------30---35----------------45--------60-----------------------70----80----------------------------------------110---120----------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 5, 6));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 110, 120));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 5, 6), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 110, 120), gffGeneIsoInfoResult.get(4));
		
	}
	/**
	 * 装载gffGeneIsoInfoRefCis和gffGeneIsoInfoThisCis
	 */
	private void assertCisIsoHead() {
		boolean cis = true;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//---------------------------------30---35---------------45--------60-----------------------70-----80------------------------------------------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 70, 80));

		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		

		//--------10-----20-------------30-----40-------------------50----60---------------------70---------80--------------------------------------------------
		//-----------------------------------35--36-----------------45-------60----------------------72-----79----------------------90-----100---------------------
		//--------10-----20-------------30---36------------------45-------60----------------------72--------80-------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 35, 36));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 72, 79));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 90, 100));
		gffGeneCluster.setBoundMaxFalseGapBp(2);
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 36), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 72, 79), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40-------------------50----60---------------------70-------80--------------------90-----100---------------------
		//-----------------------------------35--36-----------------45-------60-----------------------75---77
		//--------10-----20-------------30---36------------------45-------60-----------------------75-----80--------------------90-----100---------------
		gffGeneIsoInfoRefCis.clearElements();
		gffGeneIsoInfoThisCis.clearElements();
		
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefCis.add(new ExonInfo(cis, 90, 100));

		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 35, 36));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisCis.add(new ExonInfo(cis, 75, 77));

		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefCis, gffGeneIsoInfoThisCis);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 36), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(4));
	}
	
	
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoMid() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25-----35----------------45-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 25, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 100));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneCluster.setBoundMaxFalseGapBp(5);
		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 25, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----39----------------49----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25---------40---------------50-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 25, 39));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 49, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 100));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 25, 40), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//--------10-----20---------25-----35-------------------49----55----------------------------70---75---------------------90-----100---------------
		//--------10-----20---------25-----35--------------------50-----55----------------------------70---75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 25, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 49, 55));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 100));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 50, 55), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 25, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
	}
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoHeadHomo() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----------15--20-------------30---35---------------45--------60-----------------------70-----80-------------------90--------110---------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90--------110---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 15, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 110));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 110), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70-----80-------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 95));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		
		
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90------100---------------
		//-----5----------20-------------30---35--------------- 45-------60-----------------------70---75---------------------90----95---------------
		//-----5----------20-------------30---35----------------45--------60-----------------------70--75---------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 5, 20));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 75));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 90, 95));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 5, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 75), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
	}
	
	/**
	 * 装载gffGeneIsoInfoRefTrans和gffGeneIsoInfoThisTrans
	 */
	private void assertTransIsoHead() {
		boolean cis = false;
		//--------10-----20-------------30-----40----------------50-----60-----------------------70-----80-------------------90-----100---------------
		//---------------------------------30---35---------------45--------60-----------------------70-----80------------------------------------------------
		//--------10-----20-------------30--35----------------45--------60------------------------70----80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 30, 35));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		GffIso gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 35), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 70, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		

		//--------10-----20----------30-----------40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//                                  35--36-------------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20----------30------36--------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 35, 36));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 72, 79));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(5, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 30, 36), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 72, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
		
		
		//--------10-----20-----------------30------40-------------------50----60---------------------70-------80-------------------90-----100---------------
		//--------------------------25--26----35--36-----------------45-------60----------------------72-----79----------------------------------------------
		//--------10-----20------25--26----35--36------------------45-------60----------------------72------80-------------------90-----100---------------
		gffGeneIsoInfoRefTrans.clearElements();
		gffGeneIsoInfoThisTrans.clearElements();
		
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 10, 20));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 30, 40));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 50, 60));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 70, 80));
		gffGeneIsoInfoRefTrans.add(new ExonInfo(cis, 90, 100));
		
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 25, 26));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 35, 36));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 45, 60));
		gffGeneIsoInfoThisTrans.add(new ExonInfo(cis, 72, 79));
		gffGeneIsoInfoThisTrans.sortOnly();
		gffGeneIsoInfoRefTrans.sortOnly();
		
		gffGeneIsoInfoResult = gffGeneCluster.compareIso( gffGeneIsoInfoRefTrans, gffGeneIsoInfoThisTrans);
		assertEquals(6, gffGeneIsoInfoResult.size());
		
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 10, 20), gffGeneIsoInfoResult.get(5));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 25, 26), gffGeneIsoInfoResult.get(4));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 35, 36), gffGeneIsoInfoResult.get(3));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 45, 60), gffGeneIsoInfoResult.get(2));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 72, 80), gffGeneIsoInfoResult.get(1));
		assertEquals(new ExonInfo(gffGeneIsoInfoResult, cis, 90, 100), gffGeneIsoInfoResult.get(0));
	}
	
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
