package com.novelbio.analysis.seq.snphgvs;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;

import junit.framework.Assert;

public class TestSnpRefAltHgvsp {
	// static GffChrAbs gffchrAbs;
	static GffHashGene gffHashGene;
	static SeqHash seqHash;

	@BeforeClass
	public static void beforeClass() {
		// Species species = new Species(9606, "hg19_GRCh37");
		gffHashGene = new GffHashGene(
				"/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gff3.gz");
		seqHash = new SeqHash("/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/ChromFa/chrAll.fa");
		// gffchrAbs = new GffChrAbs(species);
		// gffHashGene = gffchrAbs.getGffHashGene();
		// seqHash = gffchrAbs.getSeqHash();
	}

	@AfterClass
	public static void afterClass() {
		// gffchrAbs.close();
	}

	@Test
	public void testAnno() {
		GffGeneIsoInfo iso = gffHashGene.searchISO("NM_005092");
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 173010498, "ATCAAGTCTCTA", "A");
		snpRefAltInfo.setSeqHash(seqHash);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		SnpRefAltHgvsc snpRefAltHgvsc = new SnpRefAltHgvsc(snpRefAltInfo, iso);
		SnpRefAltHgvsp snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.598_*8del", snpRefAltHgvsc.getHgvs());
		Assert.assertEquals("p.*200Ffs*?", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 173470236, "A", "AC");
		snpRefAltInfo.setSeqHash(seqHash);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		snpRefAltHgvsc = new SnpRefAltHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.3372-1dup", snpRefAltHgvsc.getHgvs());
		Assert.assertEquals("p.S1124Rfs*13", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpRefAltInfo("chr1", 173472459, "A", "ACTGAGGC");
		snpRefAltInfo.setSeqHash(seqHash);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		snpRefAltHgvsc = new SnpRefAltHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.3311-1_3316dup", snpRefAltHgvsc.getHgvs());
		Assert.assertEquals("p.V1106Gfs*8", snpRefAltHgvsp.getHgvsp());
	}
	
	/** 从vep的结果文件中读取相应的信息并比较 */
	@Test
	public void testAnnoVep() {
		TxtReadandWrite txtRead = new TxtReadandWrite("src/test/resources/test_file/hgvs/snp-types26.vep.txt");
		for (String content : txtRead.readlines()) {
			if (content.trim().startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo(ss[0], Integer.parseInt(ss[1]), ss[2], ss[3]);
			GffGeneIsoInfo iso = gffHashGene.searchLocation(snpRefAltInfo.getRefId(), snpRefAltInfo.getStartReal(), snpRefAltInfo.getEndReal()).getCoveredOverlapGffGene().iterator().next().getLongestSplitMrna();
			snpRefAltInfo.setSeqHash(seqHash);
			snpRefAltInfo.copeInputVar();
			snpRefAltInfo.setVarHgvsType();
			SnpRefAltHgvsc snpRefAltHgvsc = new SnpRefAltHgvsc(snpRefAltInfo, iso);
			SnpRefAltHgvsp snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
			snpRefAltHgvsp.setNeedAA3(true);
			System.out.println(content);
			if (ss.length >= 5 && !StringOperate.isRealNull(ss[4])) {
				Assert.assertEquals(ss[4], snpRefAltHgvsc.getHgvs());
			}
			if (ss.length >= 6 && !StringOperate.isRealNull(ss[5])) {
				Assert.assertEquals(ss[5], snpRefAltHgvsp.getHgvsp());
			}
		}
		txtRead.close();
	}
}
