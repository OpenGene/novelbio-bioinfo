package com.novelbio.test.analysis.seq.resequencing;

import org.junit.Test;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;

import junit.framework.TestCase;

public class TestMapInfoSnpIndel_RefAA_ThisAA extends TestCase{
	MapInfoSnpIndel mapInfoSnpIndel;
	SiteSnpIndelInfo siteSnpIndelInfo;
	GffChrAbs gffChrAbs;
	String referenceSeq = "GTGGCTC";
	String thisSeq = "G";
	@Override
	protected void setUp() throws Exception {
		gffChrAbs = new GffChrAbs(9606);
	}
	@Override
	protected void tearDown() throws Exception {
		
	}
	public void test() {
		assertAtgSiteForword();
		assertNormForword();
		assertUagSiteBackrword();
	}
	@Test
	public void assertAtgSiteForword() {
		referenceSeq = "TGTC";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759777);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("ATC", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "ATGTC";
		thisSeq = "A";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759776);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AC", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "A";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759776);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TTG", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "T";
		thisSeq = "G";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759777);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AGG", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GAGA";
		thisSeq = "G";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759773);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATG", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("GTG", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GAGATGT";
		thisSeq = "G";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152759773);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("ATGTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("GCC", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("atg", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	@Test
	public void assertUagSiteBackrword() {
		referenceSeq = "TCAGTGCTTGAA";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 153270435);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TTCAAGCACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("T", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TCAGTGCT";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 153270435);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("AAGCACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("AA", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TCAG";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 153270436);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("CACTGA", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("CAA", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("uag", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	@Test
	public void assertNormForword() {
		referenceSeq = "GTGGCTC";
		thisSeq = "G";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGTGGCTCC", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TGC", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "GT";
		thisSeq = "G";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGT", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TG", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
		
		referenceSeq = "TG";
		thisSeq = "T";
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, "chr1", 152749002);
		mapInfoSnpIndel.setSampleName("test");
		mapInfoSnpIndel.addAllenInfo(referenceSeq, thisSeq);
		siteSnpIndelInfo = mapInfoSnpIndel.getSnpIndel(referenceSeq, thisSeq);
		assertEquals("TGT", siteSnpIndelInfo.getRefAAnr().toString());
		assertEquals("TT", siteSnpIndelInfo.getThisAAnr().toString());
		assertEquals("none", siteSnpIndelInfo.getSplitTypeEffected().toString());
	}
	
}
 