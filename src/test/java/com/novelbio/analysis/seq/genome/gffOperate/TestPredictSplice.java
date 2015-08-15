package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictAltEnd;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictCassette;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictME;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.TestCase;

public class TestPredictSplice extends TestCase {
	GffDetailGene gffDetailGeneCis= new GffDetailGene("chr1", "geneCis", true);
	GffDetailGene gffDetailGeneTrans = new GffDetailGene("chr1", "geneTrans", false);
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
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
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
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
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
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50------------------60-70--------------80-90---<
		//<----------20--30-------------------------------------60-70--------------80-90---<
		//<----------20--30--------------------------------------------------------80-90---<
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
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
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
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

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
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
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
	public void testCassetteCis4() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso3", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans4 = GffGeneIsoInfo.createGffGeneIso("Iso4", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

		//<----------20--30--------------40-50----------------------------------------80-90---<
		//<----------20--30--------------------------------------------------------------80-90---<
		//<----------20--30--------------40-50----------60-70
		//<----------20--30--------------40-50----------60-70-------------------------80---90----<
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
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
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
				assertEquals(true, spliceTypeAS.isSpliceType());
				
			}
		}
	}
	
	@Test
	public void testCassetteME() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

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
		
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
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
	public void testCassetteMix() {
		gffDetailGeneCis.clearIso();
		GffGeneIsoInfo isoTrans1 = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans2 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);
		GffGeneIsoInfo isoTrans3 = GffGeneIsoInfo.createGffGeneIso("Iso2", "geneCis", gffDetailGeneCis, GeneType.mRNA, true);

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
		
		List<ExonCluster> lsExonClusters = new ArrayList<>(gffDetailGeneCis.getDifExonMapLoc2Cluster());
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getStartAbs() == 40) {
				SpliceTypePredict spliceTypeCS = new PredictCassette(exonCluster);
				assertEquals(true, spliceTypeCS.isSpliceType());
				
				SpliceTypePredict spliceTypeME= new PredictME(exonCluster);
				assertEquals(true, spliceTypeME.isSpliceType());
			}
		}
	}
	
	

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
}
