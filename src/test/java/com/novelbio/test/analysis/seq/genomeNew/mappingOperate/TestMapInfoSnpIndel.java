package com.novelbio.test.analysis.seq.genomeNew.mappingOperate;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;

import junit.framework.TestCase;

public class TestMapInfoSnpIndel extends TestCase{
	MapInfoSnpIndel mapInfoSnpIndel;
	@Override
	protected void setUp() throws Exception {
		GffChrAbs gffChrAbs = new GffChrAbs(9606);
		MapInfoSnpIndel.getSiteInfo(sampleName, mapChrID2SortedLsMapInfo, samToolsPleUpFile, gffChrAbs);
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, chrID, refSnpIndelStart);
		// TODO Auto-generated method stub
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public 
	
}
 