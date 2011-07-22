package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashUCSCgene;

public class TestGffUCSCInfo extends TestCase{
	GffHashUCSCgene gffHashUCSC;
	GffCodGene gffCodInfoUCSCgenechr1_1385068;
	@Before
	public void setUp() throws Exception
	{
		//UCSC test
		gffHashUCSC = new GffHashUCSCgene(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		gffCodInfoUCSCgenechr1_1385068 = (GffCodGene) gffHashUCSC.searchLocation("chr1", 1385068);//
	}
	@Test
	public void testchr1_1385068()
	{
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.findCod());
		assertEquals("NM_022834/NM_199121",gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().getLocString());
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.getGffDetailUp().getCis5to3());
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.getGffDetailDown().getCis5to3());
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.locatInfo());
		assertEquals(-995, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getCoordSearchLongest().getCod2ATG());
		assertEquals(-18842, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getCoordSearchLongest().getCod2UAG());
		assertEquals(-20470, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getCoordSearchLongest().getCod2Tes());
		assertEquals(0, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getCoordSearchLongest().getCod2Tss());
		assertEquals(GffGeneIsoSearch.COD_LOC_EXON, gffCodInfoUCSCgenechr1_1385068.getGffDetailThis().getCoordSearchLongest().getCodLoc());
		
		
		
	}
	
	@After
	public void  clear() {
		gffHashUCSC = null;
		gffCodInfoUCSCgenechr1_1385068 = null;
	}
	
}
