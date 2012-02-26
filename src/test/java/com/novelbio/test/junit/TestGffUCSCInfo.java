package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.generalConf.NovelBioConst;

public class TestGffUCSCInfo extends TestCase{
	GffHashGene gffHashUCSC;
	GffCodGene gffCodInfoUCSCgenechr1_1385068;
	ArrayList<String> lsAllLoc;
	HashMap<String, GffDetailGene> hashGffDetail;
	
	@Before
	public void setUp() throws Exception
	{
		//UCSC test
		gffHashUCSC = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		gffCodInfoUCSCgenechr1_1385068 = (GffCodGene) gffHashUCSC.searchLocation("chr1", 1385069);//
		lsAllLoc = gffHashUCSC.getLOCIDList();
		hashGffDetail = gffHashUCSC.getLocHashtable();
	}
	@Test
	public void testchr1_1385068()
	{
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.findCod());
		assertEquals("NM_022834/NM_199121",gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().getLocString());
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().isCis5to3());
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.getGffDetailDown().isCis5to3());
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.isInsideLoc());
		assertEquals(-995, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplit().getCod2ATG());
		assertEquals(-18841, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplit().getCod2UAG());
		assertEquals(-20469, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplit().getCod2Tes());
		assertEquals(0, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplit().getCod2Tss());
		assertEquals(GffGeneIsoInfo.COD_LOC_EXON, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplit().getCodLoc());
	}
	
	@Test
	public void test()
	{
		for (String loc : lsAllLoc) {
			GffDetailGene gffDetailGene= gffHashUCSC.searchLOC(loc);
			int atgsite = gffDetailGene.getLongestSplit().getATGSsite();
			int tsssite = gffDetailGene.getLongestSplit().getTSSsite();
			int tessite = gffDetailGene.getLongestSplit().getTESsite();
			int uagsite = gffDetailGene.getLongestSplit().getUAGsite();
			gffDetailGene.setCoord(atgsite);
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplit();
			
			if ( gffDetailGene.getLongestSplit().ismRNA()) {
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(tsssite, atgsite), gffDetailGene.getLongestSplit().getLenUTR5());
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(uagsite,tessite), gffDetailGene.getLongestSplit().getLenUTR3());
			}
			
			assertEquals(0, gffGeneIsoSearch.getCod2ATG());
			
			int atg2tes = gffGeneIsoSearch.getLocDistmRNA(tessite,atgsite);
			int cod2tes = gffGeneIsoSearch.getCod2TESmRNA();
			assertEquals(atg2tes , cod2tes);
			
			int atg2tss = gffGeneIsoSearch.getLocDistmRNA(tsssite, atgsite);
			int cod2tss = gffGeneIsoSearch.getCod2TSSmRNA();
			assertEquals(atg2tss , cod2tss);
			
			int atg2uag = gffGeneIsoSearch.getLocDistmRNA(uagsite, atgsite);
			int cod2uag = gffGeneIsoSearch.getCod2UAGmRNA();
			if (atg2uag == 918) {
				System.out.println("ok");
				gffDetailGene.setCoord(atgsite+1);
				GffGeneIsoInfo gffGeneIsoSearchtest = gffDetailGene.getLongestSplit();
				System.out.println(gffGeneIsoSearchtest.getCod2UAGmRNA());
				
			}
			assertEquals(atg2uag , cod2uag);
			
			int atg2atg = gffGeneIsoSearch.getLocDistmRNA(atgsite, atgsite);
			int cod2atg = gffGeneIsoSearch.getCod2ATG();
			assertEquals(0, atg2atg);
			assertEquals(atg2atg, cod2atg);
			
			int coordatgup = gffGeneIsoSearch.getLocDistmRNASite(atgsite, -300);
			int coordatgdown = gffGeneIsoSearch.getLocDistmRNASite(atgsite, 500);
			if (coordatgup > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(atgsite, coordatgup);
				assertEquals(-300, cod2atg2);
				getCod2Site(gffDetailGene, coordatgup, atgsite, uagsite, tsssite, tessite);
			}
			
			if (coordatgdown > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(atgsite,coordatgdown);
				assertEquals(500, cod2atg2);
				getCod2Site(gffDetailGene, coordatgdown, atgsite, uagsite, tsssite, tessite);
			}
			
			int coordatgup2 = gffGeneIsoSearch.getLocDistmRNASite(uagsite, -500);
			int coordatgdown2 = gffGeneIsoSearch.getLocDistmRNASite(uagsite, 300);
			
			if (coordatgup2 > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(uagsite, coordatgup2);
				if (gffGeneIsoSearch.getIsoName().equals("NM_015658")) {
					System.out.println(gffGeneIsoSearch.getIsoName());
					coordatgup2 = gffGeneIsoSearch.getLocDistmRNASite(uagsite, -500);
					cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(uagsite, coordatgup2);
				}
				assertEquals(-500, cod2atg2);
			}
			if (coordatgdown2 > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(uagsite,coordatgdown2);
				assertEquals(300, cod2atg2);
			}
			testAAsite(gffDetailGene);
		}
	}
	
	private void testAAsite(GffDetailGene gffDetailGene)
	{
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		int atgsite = gffGeneIsoInfo.getATGSsite();
		int site2 = gffGeneIsoInfo.getLocDistmRNASite(atgsite, 2);
		if (site2 > 0) {
			if (gffGeneIsoInfo.isCis5to3()) {
				assertEquals(atgsite + 2, site2);
			}
			else {
				assertEquals(atgsite - 2, site2);
			}
		}
		
		int site500 = gffGeneIsoInfo.getLocDistmRNASite(atgsite, 500);
		if (atgsite - gffGeneIsoInfo.getUAGsite() == 0) {
			return;
		}
		if (site500 > 0 ) {
			int aaSite = gffGeneIsoInfo.getAAsiteNum(site500);
			assertEquals(167, aaSite);
		}
		int site501 = gffGeneIsoInfo.getLocDistmRNASite(atgsite, 501);
		if (site501 > 0) {
			int aaSite1 = gffGeneIsoInfo.getAAsiteNum(site501);
			assertEquals(168, aaSite1);
		}
		int site502 = gffGeneIsoInfo.getLocDistmRNASite(atgsite, 502);
		if (site502 > 0) {
			int aaSite2 = gffGeneIsoInfo.getAAsiteNum(site502);
			assertEquals(168, aaSite2);
		}
	}
	private void getCod2Site(GffDetailGene gffDetailGene, int coord, int atgsite, int uagsite, int tsssite, int tessite) {
		
		if (gffDetailGene.getLongestSplit().getIsoName().equals("NM_020710")) {
			System.out.println("ss");
		}

		gffDetailGene.setCoord(coord);
		GffGeneIsoInfo gffGeneIsoSearchCod = gffDetailGene.getLongestSplit();
//		if (gffGeneIsoSearchCod.getCod2ATGmRNA() == 528) {
//			System.out.println("stop");
//		}
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(atgsite, coord), gffGeneIsoSearchCod.getCod2ATGmRNA());
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(uagsite, coord), gffGeneIsoSearchCod.getCod2UAGmRNA());
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(tsssite, coord), gffGeneIsoSearchCod.getCod2TSSmRNA());
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(tessite, coord), gffGeneIsoSearchCod.getCod2TESmRNA());		
	}
	@After
	public void  clear() {
		gffHashUCSC = null;
		gffCodInfoUCSCgenechr1_1385068 = null;
	}
	
}
