package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.database.model.modgeneid.GeneType;

public class TestGffGeneIsoInfo {
	
	@Test
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
		Assert.assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
		
		List<ExonInfo> lsExons = gffGeneIsoInfo.searchLocationDu(1500, 7500).getCoveredElement();
		ExonInfo exonInfo1 = new ExonInfo(true, 3000, 4000); exonInfo1.setParentListAbs(gffGeneIsoInfo);
		ExonInfo exonInfo2 = new ExonInfo(true, 5000, 6000); exonInfo2.setParentListAbs(gffGeneIsoInfo);
		List<ExonInfo> lsExonsExp = Lists.newArrayList(exonInfo1, exonInfo2);
		Assert.assertEquals(lsExonsExp, lsExons);
		
		lsExons = gffGeneIsoInfo.searchLocationDu(3000, 6000).getCoveredElement();
		exonInfo1 = new ExonInfo(true, 3000, 4000); exonInfo1.setParentListAbs(gffGeneIsoInfo);
		exonInfo2 = new ExonInfo(true, 5000, 6000); exonInfo2.setParentListAbs(gffGeneIsoInfo);
		lsExonsExp = Lists.newArrayList(exonInfo1, exonInfo2);
		Assert.assertEquals(lsExonsExp, lsExons);
	}
	@Test
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
		Assert.assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	@Test
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
		Assert.assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	@Test
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
		
		List<ExonInfo> lsExons = gffGeneIsoInfo.searchLocationDu(1500, 7500).getCoveredElement();
		ExonInfo exonInfo1 = new ExonInfo(false, 5000, 6000); exonInfo1.setParentListAbs(gffGeneIsoInfo);
		ExonInfo exonInfo2 = new ExonInfo(false, 3000, 4000); exonInfo2.setParentListAbs(gffGeneIsoInfo);
		List<ExonInfo> lsExonsExp = Lists.newArrayList(exonInfo1, exonInfo2);
		Assert.assertEquals(lsExonsExp, lsExons);
		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		Assert.assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	@Test
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
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
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
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		Assert.assertEquals(gffGeneIsoInfoSubExpected.getLsElement(), isoRaw.getRangeIsoOnExon(1500, 7800));

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
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 100, 200));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 300, 400));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 8500, 9000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 9500, 9600));
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.subGffGeneIso(20, 9600);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		isoRaw = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.subGffGeneIso(20, 30);
		Assert.assertEquals(0, gffSub.size());
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
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
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
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
	}

}
