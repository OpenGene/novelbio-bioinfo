package com.novelbio.analysis.seq.snphgvs;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;

import junit.framework.Assert;

public class TestSnpHgvsp {
	// static GffChrAbs gffchrAbs;
	static GffHashGene gffHashGene;
	static SeqHash seqHash;

	@BeforeClass
	public static void beforeClass() {
		 Species species = new Species(9606, "hg19_GRCh37");
		 GffChrAbs gffchrAbs = new GffChrAbs(species);
		 gffHashGene = gffchrAbs.getGffHashGene();
		 seqHash = gffchrAbs.getSeqHash();
		 
//		gffHashGene = new GffHashGene(
//				"/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gff3.gz");
//		seqHash = new SeqHash("/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/ChromFa/chrAll.fa");

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
		Assert.assertEquals("c.598_*8del", snpRefAltHgvsc.getHgvsc());

		SnpIsoHgvsp snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.598_*8del", snpRefAltHgvsc.getHgvsc());
		Assert.assertEquals("p.*200delext*?", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpInfo("chr1", 173470236, "A", "AC");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		Assert.assertEquals("c.3372-1dup", snpRefAltHgvsc.getHgvsc());
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
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
		
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltInfo = new SnpInfo("chr1", 173472457, "T", "TGACTGAGGC");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("c.3311-1_3318dup", snpRefAltHgvsc.getHgvsc());
		Assert.assertEquals("p.A1104_V1106dup", snpRefAltHgvsp.getHgvsp());
		
		
		iso = gffHashGene.searchISO("NM_001130440");
		snpRefAltInfo = new SnpInfo("chr1", 225971007, "TGTG", "T");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
		Assert.assertEquals("p.V28del", snpRefAltHgvsp.getHgvsp());
		
		iso = gffHashGene.searchISO("NM_014458");
		snpRefAltInfo = new SnpInfo("chr1", 173685183, "A", "AGCGA");
		snpRefAltInfo.initial(seqHash);

		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp.setNeedAA3(false);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		snpRefAltHgvsp.setNeedAA3(false);
//		Assert.assertFalse(snpRefAltHgvsp.isNeedHgvsp());
		
		snpRefAltInfo = new SnpInfo("chr1", 173685184, "T", "TACCGAT");
		snpRefAltInfo.initial(seqHash);
		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		Assert.assertFalse(snpRefAltHgvsp.isNeedHgvsp());
//		System.out.println(snpRefAltHgvsp.getHgvsp());
		
		snpRefAltInfo = new SnpInfo("chr1", 173685183, "A", "AGCGA");
		snpRefAltInfo.initial(seqHash);
		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		Assert.assertFalse(snpRefAltHgvsp.isNeedHgvsp());

		snpRefAltInfo = new SnpInfo("chr1", 173685185, "G", "GGCATG");
		snpRefAltInfo.initial(seqHash);
		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		Assert.assertFalse(snpRefAltHgvsp.isNeedHgvsp());
		
		snpRefAltInfo = new SnpInfo("chr1", 173469559, "CAGTCTTGTTGAGATGAAAAGGAATAATGGGTTCTAATAATAAATAGACAACCATAAAACTCAACTT"
				+ "TATTTGATTTTTAAAAATAAAATGAAAGGTGACATCAAACAGTACTATGCAATATGAGAATATTAATTTCTCCTAAATTTCACTGAAAGCAAAAGTTACTT"
				+ "ACTTACTGAAAGTTACTAAGATTGTGGAATATAATTCTGTTTTTCAATATTTACCAAACTGGGAAGGGGAGAATAGCTATACTCGGGAAAATATTAGGG"
				+ "AGGCACTAATTATAACAATTTCACAAATGAAAATTACAGGTTCTATGCTTTAGAGAGTAACATATCAGTTATACAACACTTTTTATAATGCACTATGTTAA"
				+ "AATTTGCTCTTAGCTGGAAACAGGGTTTCATTTGGTAGGACATATGTTTCAGTGTATTTTATATTATCCTGAAGACTTTTTTAACATCCCAAATATAATGC"
				+ "AACTACTTAAAATTAAGAAGTTTTACAGAAGTCCATCCATTGCTTATAAACTAAATGCAGCAGTAACCTGTTTCAGTAGCTTCTAAGTAAACTCCTTGCA"
				+ "GATAATCCTTTAGTATTTGAGCGAGGAAAGTAGTTTGGTCTTTAACCTGACTCCACACATCATATTTGTATCATTAATACCCTTTTCTAAAATGGTATCCAG"
				+ "TTTTCAACTATAAAAGAAAGT", "C");
		snpRefAltInfo.initial(seqHash);
		iso = gffHashGene.searchISO("NM_178527");
		snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
		snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
		Assert.assertFalse(snpRefAltHgvsp.isNeedHgvsp());
	}
	
	/** 从vep的结果文件中读取相应的信息并比较 */
	@Test
	public void testAnnoVep() {
		TxtReadandWrite txtRead = new TxtReadandWrite("src/test/resources/test_file/hgvs/snp-types26.vep.txt");
		for (String content : txtRead.readlines()) {
			System.out.println(content);
			if (content.trim().startsWith("#") || content.trim().equals("")) {
				continue;
			}
			if (content.contains("chr1	225971007	TGTG")) {
				System.out.println();
			}
			String[] ss = content.split("\t");
			SnpInfo snpRefAltInfo = new SnpInfo(ss[0], Integer.parseInt(ss[1]), ss[2], ss[3]);
			snpRefAltInfo.initial(seqHash);
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
		

			SnpIsoHgvsc snpRefAltHgvsc = new SnpIsoHgvsc(snpRefAltInfo, iso);
			if (ss.length >= 5 && !StringOperate.isRealNull(ss[4])) {
				Assert.assertEquals(ss[4], snpRefAltHgvsc.getHgvsc());
			}
			SnpIsoHgvsp snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
			snpRefAltHgvsp.setNeedAA3(true);
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
	
	/** 从vep的结果文件中读取相应的信息并比较 */
	@Test
	public void testAnnoSnpEffVarClass() {
		TxtReadandWrite txtRead = new TxtReadandWrite("src/test/resources/test_file/hgvs/var.txt");
		for (String content : txtRead.readlines()) {
			System.out.println(content);
			if (content.trim().startsWith("#") || content.trim().equals("")) {
				continue;
			}
			if (content.contains("chr1	173470230	T")) {
				System.out.println();
			}
			String[] ss = content.split("\t");
			Set<String> setVar = new HashSet<>();
			for (String varClass : content.split("\t")[4].split("&")) {
				setVar.add(varClass);
			}
			
			SnpInfo snpRefAltInfo = new SnpInfo(ss[0], Integer.parseInt(ss[1]), ss[2], ss[3]);
			snpRefAltInfo.initial(seqHash);
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

			SnpIsoHgvsp snpRefAltHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso, seqHash);
			snpRefAltHgvsp.setNeedAA3(true);
			if (snpRefAltHgvsp.isNeedHgvsp()) {
				snpRefAltHgvsp.getHgvsp();
			}
			Set<EnumVariantClass> setVarReal = VariantTypeDetector.getSetVarType(iso, snpRefAltInfo);
			setVarReal.addAll(snpRefAltHgvsp.getSetVarType());
			Set<String> setReal = new HashSet<>();
			for (EnumVariantClass enumVariantClass : setVarReal) {
				setReal.add(enumVariantClass.toString());
			}
			assertEquals(setVar, setReal);
			
		}
		txtRead.close();
	}
}
