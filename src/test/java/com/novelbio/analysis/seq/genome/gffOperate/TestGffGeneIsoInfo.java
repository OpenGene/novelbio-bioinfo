package com.novelbio.analysis.seq.genome.gffOperate;

import junit.framework.TestCase;

import com.novelbio.database.model.modgeneid.GeneType;

public class TestGffGeneIsoInfo extends TestCase{
	public void testExtenUtrCis1() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffGeneIsoInfo gffGeneIsoInfoExtend1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffGeneIsoInfo gffGeneIsoInfoExtendExpect1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 9500, 9800));
		
		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	
	public void testExtenUtrCis2() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffGeneIsoInfo gffGeneIsoInfoExtend1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 1200, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 7900));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffGeneIsoInfo gffGeneIsoInfoExtendExpect1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 7000, 8000));
		
		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	
	public void testExtenUtrCis3() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffGeneIsoInfo gffGeneIsoInfoExtend1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 800, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 8200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffGeneIsoInfo gffGeneIsoInfoExtendExpect1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 800, 2000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 7000, 8200));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 9500, 9800));
		
		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	
	public void testExtenUtrTran1() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfo.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfo.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfo.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoExtend1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 9500, 9800));
		gffGeneIsoInfoExtend1.sort();

		GffGeneIsoInfo gffGeneIsoInfoExtendExpect1 = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 100, 200));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 300, 400));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 8500, 9000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(false, 9500, 9800));
		gffGeneIsoInfoExtendExpect1.sort();

		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	
	public void testSubList() {
		GffGeneIsoInfo isoRaw = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		GffGeneIsoInfo gffGeneIsoInfoSubExpected = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoSubExpected.sort();
		

		
		GffGeneIsoInfo gffSub = isoRaw.subGffGeneIso(500, 8300);
		assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		//=================================================//
		isoRaw = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 7800));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1500, 2000));
		gffGeneIsoInfoSubExpected.sort();
		

		
		gffSub = isoRaw.subGffGeneIso(1500, 7800);
		assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		
		//=================================================//
		isoRaw = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 7800));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1500, 2000));
		gffGeneIsoInfoSubExpected.sort();
		

		
		gffSub = isoRaw.subGffGeneIso(1500, 7800);
		assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		
		
		
	}
}
