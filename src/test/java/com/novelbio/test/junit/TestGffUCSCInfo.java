package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.database.domain.species.Species;

public class TestGffUCSCInfo extends TestCase{
	GffHashGene gffHashUCSC;
	GffCodGene gffCodInfoUCSCgenechr1_1385068;
	ArrayList<String> lsAllLoc;
	HashMap<String, GffDetailGene> hashGffDetail;
	
	@Before
	public void setUp() throws Exception {
		//UCSC test
		Species species = new Species(9606);
		species.setGffDB("UCSC");
		gffHashUCSC = new GffHashGene(species.getGffType(), species.getGffFile());
		gffCodInfoUCSCgenechr1_1385068 = (GffCodGene) gffHashUCSC.searchLocation("chr1", 1385069);//
		lsAllLoc = gffHashUCSC.getLsNameNoRedundent();
		hashGffDetail = gffHashUCSC.getLocHashtable();
	}
	//HG19的案例
	//@Test
	public void testchr1_1385068()
	{
		Boolean aa = true;
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.findCod());
		assertEquals("NM_022834"+SepSign.SEP_ID+"NM_199121",gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().getName());
		assertEquals(aa,gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().isCis5to3());
		assertEquals(aa,gffCodInfoUCSCgenechr1_1385068.getGffDetailDown().isCis5to3());
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.isInsideLoc());
		assertEquals(-995, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplitMrna().getCod2ATG(1385069));
		assertEquals(-18841, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplitMrna().getCod2UAG(1385069));
		assertEquals(-20469, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplitMrna().getCod2Tes(1385069));
		assertEquals(0, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplitMrna().getCod2Tss(1385069));
		assertEquals(GffGeneIsoInfo.COD_LOC_EXON, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getLongestSplitMrna().getCodLoc(1385069));
	}
	
	@Test
	public void test()
	{
		for (String loc : lsAllLoc) {
			GffDetailGene gffDetailGene= gffHashUCSC.searchLOC(loc);
			int atgsite = gffDetailGene.getLongestSplitMrna().getATGsite();
			int tsssite = gffDetailGene.getLongestSplitMrna().getTSSsite();
			int tessite = gffDetailGene.getLongestSplitMrna().getTESsite();
			int uagsite = gffDetailGene.getLongestSplitMrna().getUAGsite();
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplitMrna();
			
			if ( gffDetailGene.getLongestSplitMrna().ismRNA()) {
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(tsssite, atgsite), gffGeneIsoSearch.getLenUTR5());
				assertEquals(gffGeneIsoSearch.getLocDistmRNA(uagsite,tessite), gffGeneIsoSearch.getLenUTR3());
			}
			
			assertEquals(0, gffGeneIsoSearch.getCod2ATG(atgsite));
			
			int atg2tes = gffGeneIsoSearch.getLocDistmRNA(tessite,atgsite);
			int cod2tes = gffGeneIsoSearch.getCod2TESmRNA(atgsite);
			assertEquals(atg2tes , cod2tes);
			
			int atg2tss = gffGeneIsoSearch.getLocDistmRNA(tsssite, atgsite);
			int cod2tss = gffGeneIsoSearch.getCod2TSSmRNA(atgsite);
			assertEquals(atg2tss , cod2tss);
			
			int atg2uag = gffGeneIsoSearch.getLocDistmRNA(uagsite, atgsite);
			int cod2uag = gffGeneIsoSearch.getCod2UAGmRNA(atgsite);
			if (atg2uag == 918) {
				System.out.println("ok");
				GffGeneIsoInfo gffGeneIsoSearchtest = gffDetailGene.getLongestSplitMrna();
				System.out.println(gffGeneIsoSearchtest.getCod2UAGmRNA(atgsite+1));
				
			}
			assertEquals(atg2uag , cod2uag);
			
			int atg2atg = gffGeneIsoSearch.getLocDistmRNA(atgsite, atgsite);
			int cod2atg = gffGeneIsoSearch.getCod2ATG(atgsite);
			assertEquals(0, atg2atg);
//			assertEquals(atg2atg, cod2atg+1);
			
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
				if (gffGeneIsoSearch.getName().equals("NM_015658")) {
					System.out.println(gffGeneIsoSearch.getName());
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
	
	private void testAAsite(GffDetailGene gffDetailGene) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		int atgsite = gffGeneIsoInfo.getATGsite();
		if (atgsite == 7831444) {
			System.out.println("stop");
		}
		int site2 = gffGeneIsoInfo.getLocDistmRNASite(atgsite, 2);
		if (site2 > 0) {
			assertEquals(gffGeneIsoInfo.getLocDistmRNA(atgsite, site2), 2);
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
		
		if (gffDetailGene.getLongestSplitMrna().getName().equals("NM_020710")) {
			System.out.println("ss");
		}

		GffGeneIsoInfo gffGeneIsoSearchCod = gffDetailGene.getLongestSplitMrna();
//		if (gffGeneIsoSearchCod.getCod2ATGmRNA() == 528) {
//			System.out.println("stop");
//		}
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(atgsite, coord), gffGeneIsoSearchCod.getCod2ATGmRNA(coord));
		
		
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(uagsite, coord), gffGeneIsoSearchCod.getCod2UAGmRNA(coord));
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(tsssite, coord), gffGeneIsoSearchCod.getCod2TSSmRNA(coord));
		assertEquals(gffGeneIsoSearchCod.getLocDistmRNA(tessite, coord), gffGeneIsoSearchCod.getCod2TESmRNA(coord));		
	}
	//MM9的案例
	@Test
	public void testCodDu() {
		int[] tss = new int[]{-2000,2000};
		int[] tes = new int[]{-500, 500};
		ArrayList<String[]> lsPeak = ExcelTxtRead.readLsExcelTxt("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/FHE/FHE_peaks.xls", new int[]{1,2,3}, 2, -1);
		long startTime = System.currentTimeMillis();
		for (String[] strings : lsPeak) {
			String chrID = strings[0]; int start = Integer.parseInt(strings[1]); int end = Integer.parseInt(strings[2]);
			GffCodGeneDU gffCodGeneDU = gffHashUCSC.searchLocation(chrID, start, end);
			gffCodGeneDU.setGeneBody(false);
			Set<GffDetailGene> hashSetTss = gffCodGeneDU.getCoveredOverlapGffGene();
			gffCodGeneDU.setTss(tss); gffCodGeneDU.setGeneBody(false);
			
//			HashSet<GffDetailGene> hashSet = new HashSet<GffDetailGene>();
//			for (GffDetailGene gffDetailGene : colGffDetailGenes) {
//				hashSet.add(gffDetailGene);
//			}
//			if (!hashSetTss.equals(hashSet)) {
//				System.out.println("stop");
//			}
//			if (!hashSetTss.equals(hashSet) && hashSet.size() > 0) {
//				System.out.println("stop2");
//			}
//			if (hashSetTss.equals(hashSet) && hashSet.size() > 0) {
//				System.out.println("stop3");
//			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(lsPeak.size() + " " + (endTime - startTime));
	}
	
	
	@After
	public void  clear() {
		gffHashUCSC = null;
		gffCodInfoUCSCgenechr1_1385068 = null;
	}
	
}
