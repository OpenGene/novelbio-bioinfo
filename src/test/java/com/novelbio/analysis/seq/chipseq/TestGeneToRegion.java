package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.model.modgeneid.GeneType;

public class TestGeneToRegion {
	@Test
	public void testGetLsAligns() {
		GffGeneIsoInfo iso = GffGeneIsoInfo.createGffGeneIso("iso1", "gene1", GeneType.mRNA, true);
		iso.add(new ExonInfo(true, 100, 200));
		iso.add(new ExonInfo(true, 300, 400));
		iso.add(new ExonInfo(true, 1000, 2000));
		iso.add(new ExonInfo(true, 5000, 6000));
		iso.add(new ExonInfo(true, 7000, 8000));
		iso.add(new ExonInfo(true, 8500, 9000));
		iso.add(new ExonInfo(true, 9500, 9800));
		iso.setATG(1200); iso.setUAG(7300);
		List<Align> lsAligns = Gene2Region.getLsAligns(GeneStructure.ALLLENGTH, iso);
		List<Align> lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.ATG, iso);
		lsAlignExp = new ArrayList<>();
		Align align = new Align("", 1200, 1200); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);

		lsAligns = Gene2Region.getLsAligns(GeneStructure.CDS, iso);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 1200, 2000));
		lsAlignExp.add(new Align("", 5000, 6000));
		lsAlignExp.add(new Align("", 7000, 7300));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.EXON, iso);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 200));
		lsAlignExp.add(new Align("", 300, 400));
		lsAlignExp.add(new Align("", 1000, 2000));
		lsAlignExp.add(new Align("", 5000, 6000));
		lsAlignExp.add(new Align("", 7000, 8000));
		lsAlignExp.add(new Align("", 8500, 9000));
		lsAlignExp.add(new Align("", 9500, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.INTRON, iso);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 201, 299));
		lsAlignExp.add(new Align("", 401, 999));
		lsAlignExp.add(new Align("", 2001, 4999));
		lsAlignExp.add(new Align("", 6001, 6999));
		lsAlignExp.add(new Align("", 8001, 8499));
		lsAlignExp.add(new Align("", 9001, 9499));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.TES, iso);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 9800, 9800); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UAG, iso);
		lsAlignExp = new ArrayList<>();
		align = new Align("", 7300, 7300); align.setCis5to3(iso.isCis5to3());
		lsAlignExp.add(align);
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR5, iso);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 100, 200));
		lsAlignExp.add(new Align("", 300, 400));
		lsAlignExp.add(new Align("", 1000, 1199));
		Assert.assertEquals(lsAlignExp, lsAligns);
		
		lsAligns = Gene2Region.getLsAligns(GeneStructure.UTR3, iso);
		lsAlignExp = new ArrayList<>();
		lsAlignExp.add(new Align("", 7301, 8000));
		lsAlignExp.add(new Align("", 8500, 9000));
		lsAlignExp.add(new Align("", 9500, 9800));
		Assert.assertEquals(lsAlignExp, lsAligns);
	}
}
