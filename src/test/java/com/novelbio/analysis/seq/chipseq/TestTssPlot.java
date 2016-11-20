package com.novelbio.analysis.seq.chipseq;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestTssPlot {
	
	@Before
	public void generateTxt() {
		TxtReadandWrite txtWrite = new TxtReadandWrite("/tmp/testTss.txt", true);
		txtWrite.writefileln("#xaxis");
		txtWrite.writefileln("#1 2 3 4 5");
		txtWrite.writefileln("#normalized_type pnl");
		txtWrite.writefileln("tp53\tchr1:1-10;chr1:21-30");
		txtWrite.writefileln("tp53\tch1:31-40");
		txtWrite.close();
	}
	@After
	public void deleteFile() {
		FileOperate.deleteFileFolder("/tmp/testTss.txt");
	}
	
	@Test
	public void testGetLsSiteRegionTxt() {
		MapReadsStub mapReadsStub = new MapReadsStub();
		TssPlot tssPlot = new TssPlot();
		tssPlot.setMapReads(mapReadsStub);
		tssPlot.setLsRegions("/tmp/testTss.txt");
	
		List<RegionValue> lsValues = tssPlot.getLsSiteRegion();
		Assert.assertArrayEquals(new double[]{23, 27, 31, 35, 39}, lsValues.get(0).values, 0.01);
		Assert.assertArrayEquals(new double[]{31.5, 33.5, 35.5, 37.5, 39.5}, lsValues.get(1).values, 0.01);
		Assert.assertArrayEquals(new double[]{54.5, 60.5, 66.5, 72.5, 78.5}, tssPlot.getMergedSiteRegion().values, 0.01);

	}
	
}
