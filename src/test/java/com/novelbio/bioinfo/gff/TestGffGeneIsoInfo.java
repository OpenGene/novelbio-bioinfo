package com.novelbio.bioinfo.gff;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneType;

public class TestGffGeneIsoInfo {
	
	@Test
	public void testExtenUtrCis1() {
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffIso gffGeneIsoInfoExtend1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 8000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffIso gffGeneIsoInfoExtendExpect1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
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
		
		List<ExonInfo> lsExons = gffGeneIsoInfo.searchLocationDu(1500, 7500).getAllGffDetail();
		ExonInfo exonInfo1 = new ExonInfo(true, 3000, 4000); exonInfo1.setParentListAbs(gffGeneIsoInfo);
		ExonInfo exonInfo2 = new ExonInfo(true, 5000, 6000); exonInfo2.setParentListAbs(gffGeneIsoInfo);
		List<ExonInfo> lsExonsExp = Lists.newArrayList(exonInfo1, exonInfo2);
		Assert.assertEquals(lsExonsExp, lsExons);
		
		lsExons = gffGeneIsoInfo.searchLocationDu(3000, 6000).getAllGffDetail();
		exonInfo1 = new ExonInfo(true, 3000, 4000); exonInfo1.setParentListAbs(gffGeneIsoInfo);
		exonInfo2 = new ExonInfo(true, 5000, 6000); exonInfo2.setParentListAbs(gffGeneIsoInfo);
		lsExonsExp = Lists.newArrayList(exonInfo1, exonInfo2);
		Assert.assertEquals(lsExonsExp, lsExons);
	}
	@Test
	public void testExtenUtrCis2() {
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffIso gffGeneIsoInfoExtend1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 1200, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 7900));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffIso gffGeneIsoInfoExtendExpect1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtendExpect1.add(new ExonInfo(true, 7000, 8000));
		
		
		gffGeneIsoInfo.extendUtr(gffGeneIsoInfoExtend1);
		Assert.assertEquals(gffGeneIsoInfoExtendExpect1, gffGeneIsoInfo);
	}
	@Test
	public void testExtenUtrCis3() {
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1000, 2000));
		gffGeneIsoInfo.add(new ExonInfo(true, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(true, 7000, 8000));
		
		GffIso gffGeneIsoInfoExtend1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 800, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 7000, 8200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(true, 9500, 9800));
		
		GffIso gffGeneIsoInfoExtendExpect1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
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
		GffIso gffGeneIsoInfo = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfo.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfo.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfo.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfo.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfo.sort();
		
		GffIso gffGeneIsoInfoExtend1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 100, 200));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 300, 400));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 8500, 9000));
		gffGeneIsoInfoExtend1.add(new ExonInfo(false, 9500, 9800));
		gffGeneIsoInfoExtend1.sort();

		GffIso gffGeneIsoInfoExtendExpect1 = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
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
		GffIso isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		GffIso gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoSubExpected.sort();
		

		
		GffIso gffSub = isoRaw.getSubGffGeneIso(500, 8300);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		//=================================================//
		isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 7800));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1500, 2000));
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.getSubGffGeneIso(1500, 7800);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		Assert.assertEquals(gffGeneIsoInfoSubExpected.getLsElement(), isoRaw.getRangeIsoOnExon(1500, 7800));

		isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 100, 200));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 300, 400));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1000, 2000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 8000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 8500, 9000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 9500, 9600));
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.getSubGffGeneIso(20, 9600);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.getSubGffGeneIso(20, 30);
		Assert.assertEquals(0, gffSub.size());
		//=================================================//
		isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 7800));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1500, 2000));
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.getSubGffGeneIso(1500, 7800);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
		
		//=================================================//
		isoRaw = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		isoRaw.add(new ExonInfo(false, 100, 200));
		isoRaw.add(new ExonInfo(false, 300, 400));
		isoRaw.add(new ExonInfo(false, 1000, 2000));
		isoRaw.add(new ExonInfo(false, 3000, 4000));
		isoRaw.add(new ExonInfo(false, 5000, 6000));
		isoRaw.add(new ExonInfo(false, 7000, 8000));
		isoRaw.add(new ExonInfo(false, 8500, 9000));
		isoRaw.add(new ExonInfo(false, 9500, 9800));
		isoRaw.sort();
		
		gffGeneIsoInfoSubExpected = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 7000, 7800));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 5000, 6000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 3000, 4000));
		gffGeneIsoInfoSubExpected.add(new ExonInfo(false, 1500, 2000));
		gffGeneIsoInfoSubExpected.sort();
		
		gffSub = isoRaw.getSubGffGeneIso(1500, 7800);
		Assert.assertEquals(gffGeneIsoInfoSubExpected, gffSub);
	}

}
