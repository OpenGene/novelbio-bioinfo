package com.novelbio.analysis.seq.resequencing;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.RefSiteSnpIndel;
import com.novelbio.analysis.seq.snphgvs.SnpRefAltInfo;
import com.novelbio.database.model.species.Species;

import junit.framework.TestCase;

public class TestMapInfoSnpIndel_RefAA_ThisAA extends TestCase{
	RefSiteSnpIndel mapInfoSnpIndel;
	SnpRefAltInfo siteSnpIndelInfo;
	GffChrAbs gffChrAbs;
	String referenceSeq = "GTGGCTC";
	String thisSeq = "G";
	@Override
	protected void setUp() throws Exception {
		Species species = new Species(9606);
//		species.setGffDB("ucsc");
		gffChrAbs = new GffChrAbs(species);
	}
	@Override
	protected void tearDown() throws Exception {
		
	}
	public void test() {
		assertAtgSiteForword();
		assertNormForword();
		assertUagSiteBackrword();
		assertSpliceSiteBackword();
	}
	@Test
	public void assertAtgSiteForword() {
		referenceSeq = "TGTC";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759777);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("ATC", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("Near_ATG Distance_To_Splice_Site_Is:_1_bp", siteSnpIndelInfo.getSplitTypeEffected().toString());
	
		referenceSeq = "ATGTC";
		thisSeq = "A";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759776);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AC", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "A";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759776);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TTG", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "T";
		thisSeq = "G";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759777);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AGG", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GAGA";
		thisSeq = "G";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759773);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("GTG", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GAGATGT";
		thisSeq = "G";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152759773);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("GCC", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	
	@Test
	public void assertSpliceSiteBackword() {
		referenceSeq = "G";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 153924738);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("AAC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AAA", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("Near_ATG Distance_To_Splice_Site_Is:_1_bp", siteSnpIndelInfo.getSplitTypeEffected().toString());
		assertEquals(-4590, mapInfoSnpIndel.getGffIso().getCod2Tes(153924738));
		assertEquals(6394, mapInfoSnpIndel.getGffIso().getCod2Tss(153924738));
		
		assertEquals(2, mapInfoSnpIndel.getGffIso().getCod2ExInEnd(153924741));
		assertEquals(131, mapInfoSnpIndel.getGffIso().getCod2ExInStart(153924741));
		assertEquals(236, mapInfoSnpIndel.getGffIso().getCod2ExInEnd(153924730));
		assertEquals(8, mapInfoSnpIndel.getGffIso().getCod2ExInStart(153924730));
		
		
		referenceSeq = "TGC";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 153924737);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("AACATC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AAATC", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	
	@Test
	public void assertUagSiteBackrword() {
		referenceSeq = "TCAGTGCTTGAAA";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 153270432);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("CATTTCAAGCACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("CAA", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TCAGTGCT";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 153270432);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("AAGCACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AA", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TCAG";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 153270432);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("CACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("CAA", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	@Test
	public void assertNormForword() {
		referenceSeq = "GTGGCTC";
		thisSeq = "G";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGTGGCTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TGC", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GT";
		thisSeq = "G";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGT", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TG", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TG";
		thisSeq = "T";
		mapInfoSnpIndel = new RefSiteSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.getAndAddAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGT", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TT", siteSnpIndelInfo.getThisAAnr().toString());
//		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	
}
 
