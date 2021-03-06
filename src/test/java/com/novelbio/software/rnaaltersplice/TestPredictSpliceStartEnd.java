package com.novelbio.software.rnaaltersplice;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.software.rnaaltersplice.ExonClusterExtract;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAlt3;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAlt5;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAltEnd;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAltStart;
import com.novelbio.software.rnaaltersplice.splicetype.PredictAltStartEnd;
import com.novelbio.software.rnaaltersplice.splicetype.PredictCassette;
import com.novelbio.software.rnaaltersplice.splicetype.PredictME;
import com.novelbio.software.rnaaltersplice.splicetype.SpliceTypePredict;
import com.novelbio.software.rnaaltersplice.splicetype.SpliceTypePredict.SplicingAlternativeType;

/** 
 * 专门测start和end
 * @author novelbio
 *
 */
public class TestPredictSpliceStartEnd extends TestCase {
	GffGene gffDetailGeneCis= new GffGene("chr1", "geneCis", true);
	GffGene gffDetailGeneTrans = new GffGene("chr1", "geneTrans", false);
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}

	@Test
	public void testStartEndTrue1() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------------------20--31--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-----------60--72
		//<------10--16-------20--30--------------40-50----------60-70-------------------------80---90----<
		isoTrans1.add(new ExonInfo( true, 20, 31));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 60, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeAlt5= new PredictAlt5(exonCluster);
				assertEquals(true, spliceTypeAlt5.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
		
		List<? extends Alignment> lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 40), SplicingAlternativeType.altstart, gffDetailGeneCis);
		List<Align> lsExp = Lists.newArrayList(new Align("chr1", 10, 16), new Align("chr1", 20, 31));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
		
		lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 39), SplicingAlternativeType.altend, gffDetailGeneCis);
		lsExp = Lists.newArrayList(new Align("chr1", 40, 50), new Align("chr1", 60, 72), new Align("chr1", 80, 90));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
	}
	
	@Test
	public void testStartEndTrue1_1() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------------------20--31--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-------------65-----72
		//<------10--16-------20--30--------------40-50----------60----70-------------------------80---90----<
		isoTrans1.add(new ExonInfo( true, 20, 31));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 65, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(true, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeAlt5= new PredictAlt5(exonCluster);
				assertEquals(true, spliceTypeAlt5.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndTrue1_2() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans4 = GffIso.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------------------20--31--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-------------65-----72
		//<------10--16-------20--30--------------40-50-------------65-----72
		//<------10--16-------20--30--------------40-50----------60----70------------------80---90----<
		isoTrans1.add(new ExonInfo( true, 20, 31));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 65, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 65, 72));
		
		isoTrans4.add(new ExonInfo( true, 10, 16));
		isoTrans4.add(new ExonInfo( true, 20, 30));
		isoTrans4.add(new ExonInfo( true, 40, 50));
		isoTrans4.add(new ExonInfo( true, 60, 70));
		isoTrans4.add(new ExonInfo( true, 80, 90));
		
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		gffDetailGeneCis.addIso(isoTrans4);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(true, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeAlt5= new PredictAlt5(exonCluster);
				assertEquals(true, spliceTypeAlt5.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndTrue1_3() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans4 = GffIso.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans5 = GffIso.createGffGeneIso("Iso5", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------------------20--31--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-------------65-----72
		//<------10--16-------20--30--------------40-50-------------65-----72
		//<------10--16-------20--30--------------40-50----------60----70------------------80---90----<
		//<------10--16-------20--30--------------40-50-------------65-----72--------------80---90---<

		isoTrans1.add(new ExonInfo( true, 20, 31));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 65, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 65, 72));
		
		isoTrans4.add(new ExonInfo( true, 10, 16));
		isoTrans4.add(new ExonInfo( true, 20, 30));
		isoTrans4.add(new ExonInfo( true, 40, 50));
		isoTrans4.add(new ExonInfo( true, 60, 70));
		isoTrans4.add(new ExonInfo( true, 80, 90));
		
		isoTrans5.add(new ExonInfo( true, 10, 16));
		isoTrans5.add(new ExonInfo( true, 20, 30));
		isoTrans5.add(new ExonInfo( true, 40, 50));
		isoTrans5.add(new ExonInfo( true, 65, 72));
		isoTrans5.add(new ExonInfo( true, 80, 90));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		gffDetailGeneCis.addIso(isoTrans4);
		gffDetailGeneCis.addIso(isoTrans5);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeAlt5= new PredictAlt5(exonCluster);
				assertEquals(true, spliceTypeAlt5.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndTrue2() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<------------------18------30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50----------60--72
		//<------10--16-------20--30--------------40-50----------60-70-------------------------80---90----<
		isoTrans1.add(new ExonInfo( true, 18, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 60, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 18) {
				SpliceTypePredict spliceTypeAlt3 = new PredictAlt3(exonCluster);
				assertEquals(false, spliceTypeAlt3.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(false, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndTransTrue2() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);

		//<------------------18------30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50--------55------72
		//<------10--16-------20--30--------------40-50--------------60-70----------------------80---90----<
		isoTrans1.add(new ExonInfo( false, 800, 900));
		isoTrans1.add(new ExonInfo( false, 40, 50));
		isoTrans1.add(new ExonInfo( false, 18, 30));
		
		isoTrans2.add(new ExonInfo( false, 55, 72));
		isoTrans2.add(new ExonInfo( false, 40, 50));
		isoTrans2.add(new ExonInfo( false, 10, 16));

		isoTrans3.add(new ExonInfo( false, 800, 900));
		isoTrans3.add(new ExonInfo( false, 60, 70));
		isoTrans3.add(new ExonInfo( false, 40, 50));
		isoTrans3.add(new ExonInfo( false, 20, 30));
		isoTrans3.add(new ExonInfo( false, 10, 16));
		
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 18) {
				SpliceTypePredict spliceTypeAlt3 = new PredictAlt3(exonCluster);
				assertEquals(false, spliceTypeAlt3.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeS.isSpliceType());
			}
		}
		
		List<? extends Alignment> lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 38), SplicingAlternativeType.altstart, gffDetailGeneCis);
		List<Align> lsExp = Lists.newArrayList(new Align("chr1", 72, 55), new Align("chr1", 50, 40));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
		
		lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 38), SplicingAlternativeType.altend, gffDetailGeneCis);
		lsExp = Lists.newArrayList(new Align("chr1", 30, 18), new Align("chr1", 16, 10));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
	}
	
	@Test
	public void testStartEndTransTrue3() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);

		//<------------------18------30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50--------55------72
		//<------10--16-------20--30--------------40-50--------------60-70----------------------80---90----<
		isoTrans1.add(new ExonInfo( false, 80, 90));
		isoTrans1.add(new ExonInfo( false, 40, 50));
		isoTrans1.add(new ExonInfo( false, 18, 30));
		
		isoTrans2.add(new ExonInfo( false, 55, 72));
		isoTrans2.add(new ExonInfo( false, 40, 50));
		isoTrans2.add(new ExonInfo( false, 10, 16));

		isoTrans3.add(new ExonInfo( false, 80, 90));
		isoTrans3.add(new ExonInfo( false, 60, 70));
		isoTrans3.add(new ExonInfo( false, 40, 50));
		isoTrans3.add(new ExonInfo( false, 20, 30));
		isoTrans3.add(new ExonInfo( false, 10, 16));
		isoTrans3.add(new ExonInfo( false, 5, 8));

		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		List<? extends Alignment> lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 38), SplicingAlternativeType.altstart, gffDetailGeneCis);
		List<Align> lsExp = Lists.newArrayList(new Align("chr1", 90, 80), new Align("chr1", 72, 55), new Align("chr1", 50, 40));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
		
		lsAligns = PredictAltStartEnd.getBGSite(new Align("chr1", 35, 38), SplicingAlternativeType.altend, gffDetailGeneCis);
		lsExp = Lists.newArrayList(new Align("chr1", 30, 18), new Align("chr1", 16, 10), new Align("chr1", 8, 5));
		assertEquals(lsExp.size(), lsAligns.size());
		for (int i = 0; i < lsExp.size(); i++) {
			Alignment align1 = lsAligns.get(i);
			Align align2 = lsExp.get(i);
			assertEquals(align1.getStartAbs(), align2.getStartAbs());
			assertEquals(align1.getEndAbs(), align2.getEndAbs());
		}
	}
	
	@Test
	public void testStartEndFalse() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------------------20--30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-----------60--72
		//<------10--16-------20--30--------------40-50----------60-70-------------------------80---90----<
		isoTrans1.add(new ExonInfo( true, 20, 30));
		isoTrans1.add(new ExonInfo( true, 40, 50));
		isoTrans1.add(new ExonInfo( true, 80, 90));
		
		isoTrans2.add(new ExonInfo( true, 10, 16));
		isoTrans2.add(new ExonInfo( true, 40, 50));
		isoTrans2.add(new ExonInfo( true, 60, 72));
		
		isoTrans3.add(new ExonInfo( true, 10, 16));
		isoTrans3.add(new ExonInfo( true, 20, 30));
		isoTrans3.add(new ExonInfo( true, 40, 50));
		isoTrans3.add(new ExonInfo( true, 60, 70));
		isoTrans3.add(new ExonInfo( true, 80, 90));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 60 && exonCluster.getEndAbs() == 72) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 20) {
				SpliceTypePredict spliceTypeAS= new PredictAlt5(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(false, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testCassetteCisEnd() {
		gffDetailGeneCis.clearIso();
		GffIso isoTrans1 = GffIso.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans2 = GffIso.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans3 = GffIso.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffIso isoTrans4 = GffIso.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------------------------------80-90---<
		//<----------20--30--------------40-50----------60--70
		//<----------20--30--------------40-50----------60--70---------------------80---90----<
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
		
		isoTrans4.add(new ExonInfo( true, 10, 20));
		isoTrans4.add(new ExonInfo( true, 20, 30));
		isoTrans4.add(new ExonInfo( true, 40, 50));
		isoTrans4.add(new ExonInfo( true, 60, 70));
		isoTrans4.add(new ExonInfo( true, 80, 90));
		gffDetailGeneCis.addIso(isoTrans1);
		gffDetailGeneCis.addIso(isoTrans2);
		gffDetailGeneCis.addIso(isoTrans3);
		gffDetailGeneCis.addIso(isoTrans4);

		ExonClusterExtract exonClusterExtract = new ExonClusterExtract(gffDetailGeneCis);
		List<ExonCluster> lsExonClusters = exonClusterExtract.getLsDifExon();
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				assertEquals(SplicingAlternativeType.cassette_multi, spliceTypeCS.getType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 60) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(false, spliceTypeME.isSpliceType());
				
				SpliceTypePredict spliceTypeAS= new PredictAltEnd(exonCluster);
				assertEquals(false, spliceTypeAS.isSpliceType());
			}
		}
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
}
