package com.novelbio.test.analysis.seq.genome.gffOperate;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoTrans;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.modgeneid.GeneType;

public class TestGffGeneIsoInfo extends TestCase{
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
		GffGeneIsoTrans isoTrans1 = new GffGeneIsoTrans("Iso1", GeneType.mRNA);
		GffGeneIsoTrans isoTrans2 = new GffGeneIsoTrans("Iso2", GeneType.mRNA);
		//<----------20--30--------------40-50------------------60-70--------------80-90---<
		//<----------20---33--------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(isoTrans1, false, 80, 90));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 20, 30));
		
		isoTrans2.add(new ExonInfo(isoTrans2, false, 80, 92));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 20, 33));
		ArrayList<GffGeneIsoInfo> lsIso = new ArrayList<GffGeneIsoInfo>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffGeneIsoInfo.getExonCluster(false, lsIso);
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
		GffGeneIsoTrans isoTrans1 = new GffGeneIsoTrans("Iso1", GeneType.mRNA);
		GffGeneIsoTrans isoTrans2 = new GffGeneIsoTrans("Iso2", GeneType.mRNA);
		//<----------20--30--------------40-50----52-54--------------60-70--------------80-90---<
		//<----------20---33-----------------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(isoTrans1, false, 80, 90));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 52, 54));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 20, 30));
		
		isoTrans2.add(new ExonInfo(isoTrans2, false, 80, 92));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 20, 33));
		ArrayList<GffGeneIsoInfo> lsIso = new ArrayList<GffGeneIsoInfo>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffGeneIsoInfo.getExonCluster(false, lsIso);
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
		GffGeneIsoCis isoTrans1 = new GffGeneIsoCis("Iso1", GeneType.mRNA);
		GffGeneIsoCis isoTrans2 = new GffGeneIsoCis("Iso2", GeneType.mRNA);
		//>--1-3-4-5-----------20--30-----------------40-50----52-54--------------60----70----------80-------90--->
		//>---------------7-8---20---33----35-36----------------------------------55------69-------71---82---84--92--->
		isoTrans1.add(new ExonInfo(isoTrans1, true, 1, 3));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 4, 5));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 20, 30));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 52, 54));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, true, 80, 90));
		
		isoTrans2.add(new ExonInfo(isoTrans2, true, 7, 8));
		isoTrans2.add(new ExonInfo(isoTrans2, true, 20, 33));
		isoTrans2.add(new ExonInfo(isoTrans2, true, 35, 36));
		isoTrans2.add(new ExonInfo(isoTrans2, true, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, true, 71, 82));
		isoTrans2.add(new ExonInfo(isoTrans2, true, 84, 92));
		
		ArrayList<GffGeneIsoInfo> lsIso = new ArrayList<GffGeneIsoInfo>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<ExonCluster> lsSep = GffGeneIsoInfo.getExonCluster(true, lsIso);
		int num = 0;
		ExonCluster exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 1, 3), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 4, 5), exonCluster.getMapIso2LsExon().get(isoTrans1).get(1));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans2).size());
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans1).size());
		assertEquals(new ExonInfo(isoTrans2, true, 7, 8), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 20, 30), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 20, 33), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans1).size());
		assertEquals(new ExonInfo(isoTrans2, true, 35, 36), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 40, 50), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 52, 54), exonCluster.getMapIso2LsExon().get(isoTrans1).get(1));
		assertEquals(0, exonCluster.getMapIso2LsExon().get(isoTrans2).size());
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 60, 70), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 55, 69), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		num++;
		exonCluster = lsSep.get(num);
		assertEquals(new ExonInfo(isoTrans1, true, 80, 90), exonCluster.getMapIso2LsExon().get(isoTrans1).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 71, 82), exonCluster.getMapIso2LsExon().get(isoTrans2).get(0));
		assertEquals(new ExonInfo(isoTrans1, true, 84, 92), exonCluster.getMapIso2LsExon().get(isoTrans2).get(1));

	}

}
