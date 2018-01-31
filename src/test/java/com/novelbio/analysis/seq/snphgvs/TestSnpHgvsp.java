package com.novelbio.analysis.seq.snphgvs;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

import junit.framework.Assert;

public class TestSnpHgvsp {
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
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 173010498, "ATCAAGTCTCTA", "A");
		snpRefAltInfo.initial(seqHash);
		SnpIsoHgvsc snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		SnpIsoHgvsp snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.598_*8del", snpRefAltHgvsc.getHgvsc());
		Assert.assertEquals("p.*200delext*?", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpInfo("chr1", 173470236, "A", "AC");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.3372-1dup", snpRefAltHgvsc.getHgvsc());
		Assert.assertEquals("p.S1124Rfs*13", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpInfo("chr1", 173472459, "A", "ACTGAGGC");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.3311-1_3316dup", snpRefAltHgvsc.getHgvsc());
		Assert.assertEquals("p.V1106Gfs*8", snpRefAltHgvsp.getHgvsp());
	}
	
	/** 从vep的结果文件中读取相应的信息并比较 */
	@Test
	public void testAnnoVep() {
		TxtReadandWrite txtRead = new TxtReadandWrite("src/test/resources/test_file/hgvs/snp-types26.vep.txt");
		for (String content : txtRead.readlines()) {
			System.out.println(content);
			if (content.trim().startsWith("#")) {
				continue;
			}
			if (content.contains("chr1	173470237	C	A")) {
				System.out.println();
			}
			String[] ss = content.split("\t");
			SnpInfo snpRefAltInfo = new SnpInfo(ss[0], Integer.parseInt(ss[1]), ss[2], ss[3]);
			GffCodGeneDU gffCodDu = gffHashGene.searchLocation(snpRefAltInfo.getRefId(), snpRefAltInfo.getStartReal(), snpRefAltInfo.getEndReal());
			Set<GffDetailGene> setGene = gffCodDu.getCoveredOverlapGffGene();
			if (setGene.isEmpty()) {
				gffCodDu.setTss(new int[]{-1000, 1000}); gffCodDu.setTes(new int[]{-1000, 1000});
				 setGene = gffCodDu.getCoveredOverlapGffGene();
			}
			GffDetailGene gene = setGene.iterator().next();
			
			GffGeneIsoInfo iso = gene.getLongestSplitMrna();
			if (!iso.getName().startsWith("NM")) {
				int len = 0;
				for (GffGeneIsoInfo iso2 : gene.getLsCodSplit()) {
					if (iso2.getName().startsWith("NM") && iso2.getLenExon(0) > len) {
						iso = iso2;
					}
				}
			}
		
			snpRefAltInfo.initial(seqHash);

			SnpIsoHgvsc snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
			SnpIsoHgvsp snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
			snpRefAltHgvsp.setNeedAA3(true);
			if (ss.length >= 5 && !StringOperate.isRealNull(ss[4])) {
				Assert.assertEquals(ss[4], snpRefAltHgvsc.getHgvsc());
			}
			if ((ss.length < 6 || ss.length >= 6 && StringOperate.isRealNull(ss[5])) && !snpRefAltHgvsp.isNeedHgvsp()) {
				continue;
			}
			if (ss.length >= 6 && !StringOperate.isRealNull(ss[5])) {
				Assert.assertTrue(snpRefAltHgvsp.isNeedHgvsp());
				Assert.assertEquals(ss[5], snpRefAltHgvsp.getHgvsp());
			}
		}
		txtRead.close();
	}
}
