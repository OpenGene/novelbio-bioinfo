package com.novelbio.analysis.seq.chipseq;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUpType;
import com.novelbio.analysis.seq.mapping.Align;

public class TestRegionBed {
	
	@Test
	public void testGetRegionInfo() {
		MapReadsStub mapReadsStub = new MapReadsStub();
		//标准化到相同长度然后堆叠
		RegionBed regionBed = new RegionBed("tp53\tchr1:1-10;chr1:31-39;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		RegionValue regionValue = regionBed.getRegionInfo(mapReadsStub);
		double[] values = new double[]{83, 86, 89, 92, 95, 98, 101, 104, 107, 70};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//标准化到相同长度然后堆叠
		regionBed = new RegionBed("tp53\tchr1:1-20;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{92.5, 94.5, 96.5, 98.5, 100.5, 102.5, 104.5, 106.5, 108.5, 110.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并截短
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_cut, 12);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{ 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 11, 12};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并延长
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60",  EnumTssPileUpType.pileup_cut, 17);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{ 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 11, 12, 13, 14, 15, 0, 0};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠
		regionBed = new RegionBed("tp53\tchr1:1-10;chr1:40-36;ch1:56-60", EnumTssPileUpType.pileup_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{97.5, 99.5, 53.5, 7.5, 9.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:31-35;ch1:51-55", EnumTssPileUpType.connect_cut, 11);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{1, 2, 3, 4, 5, 31, 32, 33, 34, 35, 51};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起并标准化
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:6-10", EnumTssPileUpType.connect_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub);
		values = new double[]{1.5, 3.5, 5.5, 7.5, 9.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
	}
	
	@Test
	public void testToString() {
		RegionBed regionBed = new RegionBed("tp53");
		regionBed.addAlign(new Align("chr1", 2345,3456));
		regionBed.addAlign(new Align("chr1", 4567,3460));
		regionBed.addAlign(new Align("chr1", 5678,6789));
		Assert.assertEquals("tp53\tchr1:2345-3456;chr1:4567-3460;chr1:5678-6789", regionBed.toString());
	}
}
