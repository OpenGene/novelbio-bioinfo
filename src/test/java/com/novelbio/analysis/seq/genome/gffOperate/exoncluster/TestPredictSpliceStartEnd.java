package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.database.model.modgeneid.GeneType;

/** 
 * 专门测start和end
 * @author novelbio
 *
 */
public class TestPredictSpliceStartEnd extends TestCase {
	GffDetailGene gffDetailGeneCis= new GffDetailGene("chr1", "geneCis", true);
	GffDetailGene gffDetailGeneTrans = new GffDetailGene("chr1", "geneTrans", false);
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}

	@Test
	public void testStartEndTrue1() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

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
	public void testStartEndTrue2() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<------------------18------30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50-----------60--72
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
				assertEquals(true, spliceTypeAS.isSpliceType());
			}
			if (exonCluster.getStartAbs() == 18) {
				SpliceTypePredict spliceTypeAlt3 = new PredictAlt3(exonCluster);
				assertEquals(false, spliceTypeAlt3.isSpliceType());
				SpliceTypePredict spliceTypeS= new PredictAltStart(exonCluster);
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndTransTrue2() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, false);

		//<------------------18------30--------------40-50----------------------------------------80-90---<
		//<------10--16------------------------------40-50----------60---72
		//<------10--16-------20--30--------------40-50----------60-70----------------------80---90----<
		isoTrans1.add(new ExonInfo( false, 80, 90));
		isoTrans1.add(new ExonInfo( false, 40, 50));
		isoTrans1.add(new ExonInfo( false, 18, 30));
		
		isoTrans2.add(new ExonInfo( false, 60, 72));
		isoTrans2.add(new ExonInfo( false, 40, 50));
		isoTrans2.add(new ExonInfo( false, 10, 16));

		isoTrans3.add(new ExonInfo( false, 80, 90));
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
				assertEquals(true, spliceTypeS.isSpliceType());
			}
		}
	}
	
	@Test
	public void testStartEndFalse() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

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
				assertEquals(true, spliceTypeAS.isSpliceType());
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
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans4 = GffGeneIsoInfo.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------------------------------80-90---<
		//<----------20--30--------------40-50----------60--70
		//<----------20--30--------------40-50----------60--70-------------------------80---90----<
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
