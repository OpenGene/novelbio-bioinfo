package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;

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
			GffDetailGene gffDetailGene= (GffDetailGene)hashGffDetail.get(loc);
			int atgsite = gffDetailGene.getLongestSplit().getATGSsite();
			int tsssite = gffDetailGene.getLongestSplit().getTSSsite();
			int tessite = gffDetailGene.getLongestSplit().getTESsite();
			int uagsite = gffDetailGene.getLongestSplit().getUAGsite();
			gffDetailGene.setCoord(atgsite);
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplit();
			
			if ( gffDetailGene.getLongestSplit().ismRNA()) {
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(atgsite, tsssite), gffDetailGene.getLongestSplit().getLenUTR5());
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(tessite, uagsite), gffDetailGene.getLongestSplit().getLenUTR3());
			}
		
			assertEquals(0, gffGeneIsoSearch.getCod2ATG());
			
			int atg2tes = -gffGeneIsoSearch.getLocDistmRNA(atgsite, tessite);
			int cod2tes = gffGeneIsoSearch.getCod2TESmRNA();
			assertEquals(atg2tes , cod2tes);
			
			int atg2tss = gffGeneIsoSearch.getLocDistmRNA(atgsite, tsssite);
			int cod2tss = gffGeneIsoSearch.getCod2TSSmRNA();
			assertEquals(atg2tss , cod2tss);
			
			int atg2uag = -gffGeneIsoSearch.getLocDistmRNA(atgsite, uagsite);
			int cod2uag = -gffGeneIsoSearch.getCod2UAGmRNA();
			if (atg2uag == 918) {
				System.out.println("ok");
				gffDetailGene.setCoord(atgsite+1);
				GffGeneIsoInfo gffGeneIsoSearchtest = gffDetailGene.getLongestSplit();
				System.out.println(gffGeneIsoSearchtest.getCod2UAGmRNA());
				
			}
			assertEquals(atg2uag , cod2uag);
			
			int atg2atg = gffGeneIsoSearch.getLocDistmRNA(atgsite, atgsite);
			int cod2atg = -gffGeneIsoSearch.getCod2ATG();
			assertEquals(0, atg2atg);
			assertEquals(atg2atg, cod2atg);
			
			int coordatgup = gffGeneIsoSearch.getLocDistmRNASite(atgsite, -300);
			int coordatgdown = gffGeneIsoSearch.getLocDistmRNASite(atgsite, 500);
			if (coordatgup > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(coordatgup, atgsite);
				assertEquals(-300, cod2atg2);
				getCod2Site(gffDetailGene, coordatgup, atgsite, uagsite, tsssite, tessite);
			}
			
			if (coordatgdown > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(coordatgdown, atgsite);
				assertEquals(500, cod2atg2);
				getCod2Site(gffDetailGene, coordatgdown, atgsite, uagsite, tsssite, tessite);
			}
			
			int coordatgup2 = gffGeneIsoSearch.getLocDistmRNASite(uagsite, -500);
			int coordatgdown2 = gffGeneIsoSearch.getLocDistmRNASite(uagsite, 300);
			
			if (coordatgup2 > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(coordatgup2, uagsite);
				assertEquals(-500, cod2atg2);
			}
			if (coordatgdown2 > 0) {
				int cod2atg2 = gffGeneIsoSearch.getLocDistmRNA(coordatgdown2, uagsite);
				assertEquals(300, cod2atg2);
			}
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
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(coord,atgsite), gffGeneIsoSearchCod.getCod2ATGmRNA());
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(coord,uagsite), gffGeneIsoSearchCod.getCod2UAGmRNA());
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(coord,tsssite), gffGeneIsoSearchCod.getCod2TSSmRNA());
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(coord,tessite), -gffGeneIsoSearchCod.getCod2TESmRNA());		
	}
	@After
	public void  clear() {
		gffHashUCSC = null;
		gffCodInfoUCSCgenechr1_1385068 = null;
	}
	
}
