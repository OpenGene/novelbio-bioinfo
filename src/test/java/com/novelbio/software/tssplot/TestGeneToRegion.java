package com.novelbio.software.tssplot;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.software.tssplot.Gene2Region;

public class TestGeneToRegion {
	
	/**
	 * 测试给定一个转录本，去获取其指定区域，如tss，tes，cds等的坐标是否正确
	 * 这个转录本的方向是正向
	 */
	@Test
	public void testGetLsAlignsCis() {
		GffIso iso = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		iso.add(new ExonInfo(true, 100, 200));
		iso.add(new ExonInfo(true, 300, 400));
		iso.add(new ExonInfo(true, 1000, 2000));
		iso.add(new ExonInfo(true, 5000, 6000));
		iso.add(new ExonInfo(true, 7000, 8000));
		iso.add(new ExonInfo(true, 8500, 9000));
		iso.add(new ExonInfo(true, 9500, 9800));
		iso.setATG(1200); iso.setUAG(7300);
		
		
		int[] startEndExtend0 = new int[]{0, 0};
		int[] startEndExtend_500_1k = new int[]{-500, 1000};
		
		List<Align> lsAligns = Gene2Region.getLsAligns(GeneStructure.ALLLENGTH, iso, startEndExtend0);
		List<Align> lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ALLLENGTH, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", -400, 10800));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ATG, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		Align align = new Align("", 1200, 1200); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ATG, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 700, 2200); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.TES, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 9800, 9800); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.TES, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 9300, 10800); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UAG, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 7300, 7300); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UAG, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 6800, 8300); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.CDS, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 1200, 2000));
		lsAlignExp.add(new Align("", 5000, 6000));
		lsAlignExp.add(new Align("", 7000, 7300));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.EXON, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 200));
		lsAlignExp.add(new Align("", 300, 400));
		lsAlignExp.add(new Align("", 1000, 2000));
		lsAlignExp.add(new Align("", 5000, 6000));
		lsAlignExp.add(new Align("", 7000, 8000));
		lsAlignExp.add(new Align("", 8500, 9000));
		lsAlignExp.add(new Align("", 9500, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.INTRON, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 201, 299));
		lsAlignExp.add(new Align("", 401, 999));
		lsAlignExp.add(new Align("", 2001, 4999));
		lsAlignExp.add(new Align("", 6001, 6999));
		lsAlignExp.add(new Align("", 8001, 8499));
		lsAlignExp.add(new Align("", 9001, 9499));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR5, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 200));
		lsAlignExp.add(new Align("", 300, 400));
		lsAlignExp.add(new Align("", 1000, 1199));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR3, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 7301, 8000));
		lsAlignExp.add(new Align("", 8500, 9000));
		lsAlignExp.add(new Align("", 9500, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
	}
	
	
	
	/**
	 * 测试给定一个转录本，去获取其指定区域，如tss，tes，cds等的坐标是否正确
	 * 这个转录本的方向是反向
	 */
	@Test
	public void testGetLsAlignsTrans() {
		GffIso iso = GffIso.createGffGeneIso("iso1", "gene1", GeneType.mRNA, false);
		iso.add(new ExonInfo(false, 9500, 9800));
		iso.add(new ExonInfo(false, 8500, 9000));
		iso.add(new ExonInfo(false, 7000, 8000));
		iso.add(new ExonInfo(false, 5000, 6000));
		iso.add(new ExonInfo(false, 1000, 2000));
		iso.add(new ExonInfo(false, 300, 400));
		iso.add(new ExonInfo(false, 100, 200));
		iso.setATG(7300); iso.setUAG(1200);
		
		
		int[] startEndExtend0 = new int[]{0, 0};
		int[] startEndExtend_500_1k = new int[]{-500, 1000};
		
		List<Align> lsAligns = Gene2Region.getLsAligns(GeneStructure.ALLLENGTH, iso, startEndExtend0);
		List<Align> lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 9800, 100));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ALLLENGTH, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 10300, -900));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ATG, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		Align align = new Align("", 7300, 7300); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ATG, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 7800, 6300); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.TES, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 100, 100); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.TES, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 600,  -900); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UAG, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 1200, 1200); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UAG, iso, startEndExtend_500_1k);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 1700, 200); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.CDS, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 7300, 7000));
		lsAlignExp.add(new Align("", 6000, 5000));
		lsAlignExp.add(new Align("", 2000, 1200));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.EXON, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 9800, 9500));
		lsAlignExp.add(new Align("", 9000, 8500));
		lsAlignExp.add(new Align("", 8000, 7000));
		lsAlignExp.add(new Align("", 6000, 5000));
		lsAlignExp.add(new Align("", 2000, 1000));
		lsAlignExp.add(new Align("", 400, 300));
		lsAlignExp.add(new Align("", 200, 100));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.INTRON, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 9499, 9001));
		lsAlignExp.add(new Align("", 8499, 8001));
		lsAlignExp.add(new Align("", 6999, 6001));
		lsAlignExp.add(new Align("", 4999, 2001));
		lsAlignExp.add(new Align("", 999, 401));
		lsAlignExp.add(new Align("", 299, 201));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR5, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 9800, 9500));
		lsAlignExp.add(new Align("", 9000, 8500));
		lsAlignExp.add(new Align("", 8000, 7301));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR3, iso, startEndExtend0);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 1199, 1000));
		lsAlignExp.add(new Align("", 400, 300));
		lsAlignExp.add(new Align("", 200, 100));
		Assert.assertEquals(lsAlignExp, lsAligns);

	}
}
