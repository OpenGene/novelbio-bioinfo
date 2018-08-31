package com.novelbio.software.tssplot;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.software.tssplot.RegionValue;
import com.novelbio.software.tssplot.TssPlot;

public class TestTssPlot {
	String fileSameLen = "/tmp/testTssSameLen.txt";
	String fileDifLen = "/tmp/testTssDifLen.txt";

	@Before
	public void generateTxt() {
		TxtReadandWrite txtWriteSamLen = new TxtReadandWrite(fileSameLen, true);
		txtWriteSamLen.writefileln("#xaxis");
		txtWriteSamLen.writefileln("#1 2 3 4 5");
		txtWriteSamLen.writefileln("#normalized_type pnl");
		txtWriteSamLen.writefileln("tp53\tchr1:1-10;chr1:21-30");
		txtWriteSamLen.writefileln("tp53\tch1:31-40");
		txtWriteSamLen.close();
		
		TxtReadandWrite txtWriteDifLen = new TxtReadandWrite(fileDifLen, true);
		txtWriteDifLen.writefileln("#xaxis");
		txtWriteDifLen.writefileln("#1 2 3 4 5");
		txtWriteDifLen.writefileln("#normalized_type plnl");
		txtWriteDifLen.writefileln("tp53\tchr1:1-10;chr1:21-30");
		txtWriteDifLen.writefileln("tp53\tch1:31-34");
		txtWriteDifLen.close();
	}
	
	@After
	public void deleteFile() {
		FileOperate.deleteFileFolder(fileSameLen);
	}
	
	@Test
	public void testGetLsSiteRegionTxt() {
		MapReadsStub mapReadsStub = new MapReadsStub();
		TssPlot tssPlot = new TssPlot();
		tssPlot.setMapReads(mapReadsStub);
		tssPlot.readRegionFile(fileSameLen);
	
		List<RegionValue> lsValues = tssPlot.getLsSiteRegion();
		Assert.assertArrayEquals(new double[]{23, 27, 31, 35, 39}, lsValues.get(0).values, 0.01);
		Assert.assertArrayEquals(new double[]{31.5, 33.5, 35.5, 37.5, 39.5}, lsValues.get(1).values, 0.01);
		Assert.assertArrayEquals(new double[]{54.5, 60.5, 66.5, 72.5, 78.5}, tssPlot.getMergedSiteRegion().values, 0.01);

		
		tssPlot = new TssPlot();
		tssPlot.setMapReads(mapReadsStub);
		tssPlot.readRegionFile(fileDifLen);
	
		lsValues = tssPlot.getLsSiteRegion();
		Assert.assertArrayEquals(new double[]{23, 27, 31, 35, 39}, lsValues.get(0).values, 0.01);
		Assert.assertArrayEquals(new double[]{31, 32, 33, 34, 0}, lsValues.get(1).values, 0.01);
		Assert.assertArrayEquals(new double[]{54, 59, 64, 69, 39}, tssPlot.getMergedSiteRegion().values, 0.01);
	}
	
}
