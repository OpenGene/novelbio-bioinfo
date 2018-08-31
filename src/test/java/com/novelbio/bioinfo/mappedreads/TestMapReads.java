package com.novelbio.bioinfo.mappedreads;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.mappedreads.MapReadsAddAlignRecord;

public class TestMapReads {
	
	@Test
	public void testSetStartCodCis() {
		List<Alignment> lsAligns = new ArrayList<>();
		lsAligns.add(new Align("chr1:2340-2350"));
		lsAligns.add(new Align("chr1:2360-2370"));
		lsAligns.add(new Align("chr1:2380-2390"));

		List<? extends Alignment> lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 8, true);
		Assert.assertEquals(1, lsResult.size());
		Assert.assertEquals(2340, lsResult.get(0).getStartCis());
		Assert.assertEquals(2347, lsResult.get(0).getEndCis());
		
		lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 18, true);
		Assert.assertEquals(2, lsResult.size());
		Assert.assertEquals(2340, lsResult.get(0).getStartCis());
		Assert.assertEquals(2366, lsResult.get(1).getEndCis());
		
		lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 38, true);
		Assert.assertEquals(3, lsResult.size());
		Assert.assertEquals(2340, lsResult.get(0).getStartCis());
		Assert.assertEquals(2395, lsResult.get(2).getEndCis());
	}
	@Test
	public void testSetStartCodTrans() {
		List<Alignment> lsAligns = new ArrayList<>();
		lsAligns.add(new Align("chr1:2340-2350"));
		lsAligns.add(new Align("chr1:2360-2370"));
		lsAligns.add(new Align("chr1:2380-2390"));

		List<? extends Alignment> lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 8, false);
		Assert.assertEquals(1, lsResult.size());
		Assert.assertEquals(2383, lsResult.get(0).getStartCis());
		Assert.assertEquals(2390, lsResult.get(0).getEndCis());
		
		lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 18, false);
		Assert.assertEquals(2, lsResult.size());
		Assert.assertEquals(2364, lsResult.get(0).getStartCis());
		Assert.assertEquals(2390, lsResult.get(1).getEndCis());
		
		lsResult = MapReadsAddAlignRecord.setStartCod(lsAligns, 38, false);
		Assert.assertEquals(3, lsResult.size());
		Assert.assertEquals(2335, lsResult.get(0).getStartCis());
		Assert.assertEquals(2390, lsResult.get(2).getEndCis());
	}
}
