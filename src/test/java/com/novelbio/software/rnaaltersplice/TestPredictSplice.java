package com.novelbio.software.rnaaltersplice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonClusterSite;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.software.rnaaltersplice.ExonClusterExtract;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAlt3;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAlt5;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAltEnd;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAltStart;
import com.novelbio.software.rnaaltersplice.splicetype.PredictCassette;
import com.novelbio.software.rnaaltersplice.splicetype.PredictME;
import com.novelbio.software.rnaaltersplice.splicetype.PredictRetainIntron;
import com.novelbio.software.rnaaltersplice.splicetype.SpliceTypePredict;
import com.novelbio.software.rnaaltersplice.splicetype.SpliceTypePredict.SplicingAlternativeType;

import junit.framework.TestCase;

/** 不测start和end */
public class TestPredictSplice extends TestCase {
	GffGene gffDetailGeneCis= new GffGene("chr1", "geneCis", true);
	GffGene gffDetailGeneTrans = new GffGene("chr1", "geneTrans", false);
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	/**
	 * 
	 */
	@Test
	public void testCassetteCis() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		//<----------20--30--------------40-50------------------60-70--------------80-90---<
		//<----------20---33--------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 60, 70));
		isoTrans1.add(new ExonInfo( true, 80, 90));

		isoTrans2.add(new ExonInfo( true, 20, 33));
		isoTrans2.add(new ExonInfo( true, 55, 69));
		isoTrans2.add(new ExonInfo( true, 80, 92));

		gffDetailGeneCis.addIso(isoTrans1); gffDetailGeneCis.addIso(isoTrans2);
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(false, spliceTypeCS.isSpliceType());
			}
			
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
			}
		}
	}
	
	
	@Test
	public void testCassetteCis2() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50------------------60-70--------------80-90----------100-110---<
		//<----------20--30-------------------------------------60-70-----------------80-90----------100-110---<
		//<----------20--30--------------------------------------------------------------80-90----------100-110---<
		//<----------20--30--------------------------------------------------------------------------------100-110---<//TODO 这个还没加入
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 60, 70));
		isoTrans1.add(new ExonInfo( true, 80, 90));

		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 60, 70));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		
		isoTrans3.add(new ExonInfo( true, 10, 20));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.cassette, spliceTypeCS.getType());
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 60) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.cassette_multi, spliceTypeCS.getType());
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
		}
	}
	
	@Test
	public void testCassetteCis3() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------------------------------80-90---<
		//<----------20--30--------------40-50----------60-70
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));

		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		
		isoTrans3.add(new ExonInfo( true, 10, 20));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.cassette, spliceTypeCS.getType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 60) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(false, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(true, spliceTypeAS.isSpliceType());
				
			}
		}
	}
	
	@Test
	public void testCassetteCis5() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans4 = GffIso.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans5 = GffIso.createGffGeneIso("Iso5", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans6 = GffIso.createGffGeneIso("Iso6", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans7 = GffIso.createGffGeneIso("Iso7", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans8 = GffIso.createGffGeneIso("Iso8", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans9 = GffIso.createGffGeneIso("Iso9", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans10 = GffIso.createGffGeneIso("Iso10", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//1<----------20++30--------------40+++++++++50------------------------------------------------80+++++++90----100++++++++110----------------------<
		//2<----------20++30---------------------------------------------------------------------------------------80+++++++90----100++++++++110----------------<
		//3<----------20++30--------------40+++++++++50--------60+++70
		//4<----------20++30--------------40++++++++++++++++++++++++70-------------------80++++++++++++++++++++++++110---------------<
		//5<----------20++30--------------40++++++++++++++++++++++++++75---------------80+83-86+90----100+103-105+110-<
		//6<----------20++30--------------40+43-45+++50---------------------------------------------------80+83-86+90----100+103-105+110-<
		//7<----------20++30------------------------45+++50-------------------------------------------------------------86+90------------------105+110-<
		//8<----------20++30------------------------45+++50---------------------------------------------------80+83----------------------------105+110-<
		//9<---------------------------------------------49+++50---------------------------------------------------80+83-86+90----100+103-105+110-<
		//10<--------20++30--------------40+43-45+++50---------------------------------------------------80+83---------------100+101----<

		isoTrans1.add(new ExonInfo( true, 10, 15));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		isoTrans1.add(new ExonInfo( true, 100, 110));
		
		isoTrans2.add(new ExonInfo( true, 10, 15));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		isoTrans1.add(new ExonInfo( true, 100, 110));

		isoTrans3.add(new ExonInfo( true, 10, 15));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		
		isoTrans4.add(new ExonInfo( true, 10, 15));
		isoTrans4.add(new ExonInfo( true, 20, 30));
		isoTrans4.add(new ExonInfo( true, 40, 70));
		isoTrans4.add(new ExonInfo( true, 80, 110));
		
		isoTrans5.add(new ExonInfo( true, 10, 15));
		isoTrans5.add(new ExonInfo( true, 20, 30));
		isoTrans5.add(new ExonInfo( true, 40, 75));
		isoTrans5.add(new ExonInfo( true, 80, 83));
		isoTrans5.add(new ExonInfo( true, 86, 90));
		isoTrans5.add(new ExonInfo( true, 100, 103));
		isoTrans5.add(new ExonInfo( true, 105, 110));
		
		isoTrans6.add(new ExonInfo( true, 10, 15));
		isoTrans6.add(new ExonInfo( true, 20, 30));
		isoTrans6.add(new ExonInfo( true, 40, 43));
		isoTrans6.add(new ExonInfo( true, 45, 50));
		isoTrans6.add(new ExonInfo( true, 80, 83));
		isoTrans6.add(new ExonInfo( true, 86, 90));
		isoTrans6.add(new ExonInfo( true, 100, 103));
		isoTrans6.add(new ExonInfo( true, 105, 110));
		
		isoTrans7.add(new ExonInfo( true, 10, 15));
		isoTrans7.add(new ExonInfo( true, 20, 30));
		isoTrans7.add(new ExonInfo( true, 45, 50));
		isoTrans7.add(new ExonInfo( true, 86, 90));
		isoTrans7.add(new ExonInfo( true, 105, 110));
		
		isoTrans8.add(new ExonInfo( true, 10, 15));
		isoTrans8.add(new ExonInfo( true, 20, 30));
		isoTrans8.add(new ExonInfo( true, 45, 50));
		isoTrans8.add(new ExonInfo( true, 80, 83));
		isoTrans8.add(new ExonInfo( true, 105, 110));
		
		isoTrans9.add(new ExonInfo( true, 49, 50));
		isoTrans9.add(new ExonInfo( true, 80, 83));
		isoTrans9.add(new ExonInfo( true, 86, 90));
		isoTrans9.add(new ExonInfo( true, 100, 103));
		isoTrans9.add(new ExonInfo( true, 105, 110));
		
		isoTrans10.add(new ExonInfo( true, 10, 15));
		isoTrans10.add(new ExonInfo( true, 20, 30));
		isoTrans10.add(new ExonInfo( true, 40, 43));
		isoTrans10.add(new ExonInfo( true, 45, 50));
		isoTrans10.add(new ExonInfo( true, 80, 83));
		isoTrans10.add(new ExonInfo( true, 100, 101));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		gffDetailGeneCis.addIso(isoTrans4);
		gffDetailGeneCis.addIso(isoTrans5);
		gffDetailGeneCis.addIso(isoTrans6);
		gffDetailGeneCis.addIso(isoTrans7);
		gffDetailGeneCis.addIso(isoTrans8);
		gffDetailGeneCis.addIso(isoTrans9);
		gffDetailGeneCis.addIso(isoTrans10);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		assertEquals(11, lsExonClusters.size());
		Set<Align> setAlign = new HashSet<>();
		setAlign.add(new Align("chr1", 80, 83)); setAlign.add(new Align("chr1", 40, 50));
		setAlign.add(new Align("chr1", 80, 110)); setAlign.add(new Align("chr1", 100, 103));
		setAlign.add(new Align("chr1", 80, 90)); setAlign.add(new Align("chr1", 45, 50));
		setAlign.add(new Align("chr1", 40, 43)); setAlign.add(new Align("chr1", 100, 110));
		setAlign.add(new Align("chr1", 86, 90)); setAlign.add(new Align("chr1", 40, 75));
		setAlign.add(new Align("chr1", 60, 70));
		
		Set<Align> setAlignReal = new HashSet<>();
		for (ExonCluster exonCluster : lsExonClusters) {
	        	setAlignReal.add(new Align(exonCluster.getRefID(), exonCluster.getStartAbs(), exonCluster.getEndAbs()));
        }
		assertEquals(setAlign, setAlignReal);
		
		boolean isDetectedSE = false;
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40 && exonCluster.getEndAbs() >= 70) {
				SpliceTypePredict spliceTypeCS = new PredictRetainIntron(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.retain_intron, spliceTypeCS.getType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 40 && exonCluster.getEndAbs() == 50) {
				isDetectedSE = true;
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.cassette_multi, spliceTypeCS.getType());
			}
		}
		assertEquals(true, isDetectedSE);
	}
	
	@Test
	public void testCassetteME() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------55-69----------------80-90---<
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		isoTrans1.add(new ExonInfo( true, 100, 110));
		
		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 55, 69));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		isoTrans2.add(new ExonInfo( true, 100, 110));
		
//		isoTrans3.add(new ExonInfo(isoTrans1, true, 10, 20));
//		isoTrans3.add(new ExonInfo(isoTrans2, true, 20, 30));
//		isoTrans3.add(new ExonInfo(isoTrans2, true, 80, 90));
//		isoTrans3.add(new ExonInfo(isoTrans1, true, 100, 110));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
//		gffDetailGeneCis.addIso(isoTrans3);
		
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(false, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(true, spliceTypeME.isSpliceType());
			}
		}
	}
	
	@Test
	public void testCassetteME2() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------55-69----------------80-90---<
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		isoTrans1.add(new ExonInfo( true, 100, 110));
		
		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 40, 45));
		isoTrans2.add(new ExonInfo( true, 55, 69));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		isoTrans2.add(new ExonInfo( true, 100, 110));
		
//		isoTrans3.add(new ExonInfo(isoTrans1, true, 10, 20));
//		isoTrans3.add(new ExonInfo(isoTrans2, true, 20, 30));
//		isoTrans3.add(new ExonInfo(isoTrans2, true, 80, 90));
//		isoTrans3.add(new ExonInfo(isoTrans1, true, 100, 110));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
//		gffDetailGeneCis.addIso(isoTrans3);
		
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
			
			if (exonCluster.getStartAbs() == 55) {
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
		}
	}
	
	@Test
	public void testCassetteMix() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------55-69----------------80-90---<
		//<----------20--30--------------------------------------------------------------80-90---<
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		isoTrans1.add(new ExonInfo( true, 100, 110));
		
		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 30));
		isoTrans2.add(new ExonInfo( true, 55, 69));
		isoTrans2.add(new ExonInfo( true, 80, 90));
		isoTrans2.add(new ExonInfo( true, 100, 110));
		
		isoTrans3.add(new ExonInfo( true, 10, 20));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		isoTrans3.add(new ExonInfo( true, 100, 110));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(true, spliceTypeME.isSpliceType());
			}
		}
	}
	
	@Test
	public void testCassetteAlt5and3() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--35---------------------------------------------------------75-----90---<
		//<----------20--30--------------40-50----------60-70
		isoTrans1.add(new ExonInfo( true, 10, 20));
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));

		isoTrans2.add(new ExonInfo( true, 10, 20));
		isoTrans2.add(new ExonInfo( true, 20, 35));
		isoTrans2.add(new ExonInfo( true, 75, 90));
		
		isoTrans3.add(new ExonInfo( true, 10, 20));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeCS = new PredictAlt5(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.alt5, spliceTypeCS.getType());
				
				SpliceTypePredict spliceTypeAltStart = new PredictAltStart(exonCluster);
				assertEquals(false, spliceTypeAltStart.isSpliceType());
			}
			
			if (exonCluster.getStartAbs() == 75) {
				SpliceTypePredict spliceTypeCS = new PredictAlt3(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.alt3, spliceTypeCS.getType());
				
				SpliceTypePredict spliceTypeAltEnd= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAltEnd.isSpliceType());
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
}
