package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.Hashtable;

import org.junit.Before;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.genome.gffOperate.GffDetail;
import com.novelbio.base.genome.gffOperate.GffHash;
import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;

public class GffUCSCTest {
	@Before
	public void setUp() throws Exception
	{
		GffHash gffHashUCSC = new GffHashUCSCgene();
		gffHashUCSC.ReadGffarray(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
	}
}
