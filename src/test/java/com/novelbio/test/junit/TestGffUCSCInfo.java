package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodInfoUCSCgene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.analysis.seq.genome.gffOperate.GffsearchUCSCgene;

public class TestGffUCSCInfo extends TestCase{
	GffHashUCSCgene gffHashUCSC;
	GffsearchUCSCgene gffsearchUCSCgene;
	GffCodInfoUCSCgene gffCodInfoUCSCgenechr1_1385068;
	@Before
	public void setUp() throws Exception
	{
		gffHashUCSC = new GffHashUCSCgene();
		gffHashUCSC.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		gffsearchUCSCgene = new GffsearchUCSCgene();
		gffCodInfoUCSCgenechr1_1385068 = (GffCodInfoUCSCgene) gffsearchUCSCgene.searchLocation("chr1", 1385068, gffHashUCSC);
	}
	@Test
	public void test1()
	{
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.begincis5to3);
		assertEquals(false,gffCodInfoUCSCgenechr1_1385068.endcis5to3);
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.insideLOC);
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.result);
		
		assertEquals(-995, gffCodInfoUCSCgenechr1_1385068.codToATG[0]);
		assertEquals(-1000000000, gffCodInfoUCSCgenechr1_1385068.codToATG[1]);
		
		assertEquals(20470, gffCodInfoUCSCgenechr1_1385068.distancetoLOCEnd[0]);
		assertEquals(-1, gffCodInfoUCSCgenechr1_1385068.distancetoLOCEnd[1]);
		
		assertEquals(0, gffCodInfoUCSCgenechr1_1385068.distancetoLOCStart[0]);
		assertEquals(-1, gffCodInfoUCSCgenechr1_1385068.distancetoLOCStart[1]);
	}
	@Test
	public void test2()
	{
		assertEquals(true,gffCodInfoUCSCgenechr1_1385068.begincis5to3);
		assertEquals(false,gffCodInfoUCSCgenechr1_1385068.endcis5to3);
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.insideLOC);
		assertEquals(true, gffCodInfoUCSCgenechr1_1385068.result);
		
		assertEquals(-995, gffCodInfoUCSCgenechr1_1385068.codToATG[0]);
		assertEquals(-1000000000, gffCodInfoUCSCgenechr1_1385068.codToATG[1]);
		
		assertEquals(20470, gffCodInfoUCSCgenechr1_1385068.distancetoLOCEnd[0]);
		assertEquals(-1, gffCodInfoUCSCgenechr1_1385068.distancetoLOCEnd[1]);
		
		assertEquals(0, gffCodInfoUCSCgenechr1_1385068.distancetoLOCStart[0]);
		assertEquals(-1, gffCodInfoUCSCgenechr1_1385068.distancetoLOCStart[1]);
	}
 
	
	
	@After
	public void  clear() {
		gffHashUCSC = null;
		gffsearchUCSCgene = null;
		gffCodInfoUCSCgenechr1_1385068 = null;
	}
	
}
