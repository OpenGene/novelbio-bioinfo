package com.novelbio.software.tssplot;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.mappedreads.MapReadsBSP.CpGCalculator;
import com.novelbio.bioinfo.mappedreads.MapReadsBSP.EnumBspCpGCalculateType;
import com.novelbio.bioinfo.mappedreads.MapReadsBSP.EnumCpGmethyType;
import com.novelbio.software.tssplot.RegionBed;
import com.novelbio.software.tssplot.RegionValue;
import com.novelbio.software.tssplot.RegionBed.EnumTssPileUpType;
import com.novelbio.software.tssplot.RegionBed.ReadsCoverageHandleFactory;

public class TestRegionBed {
	
	@Test
	public void testGetRegionInfoCis() {
		MapReadsStub mapReadsStub = new MapReadsStub();
		//标准化到相同长度然后堆叠
		RegionBed regionBed = new RegionBed("tp53\tchr1:1-10;chr1:31-39;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		RegionValue regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		double[] values = new double[]{83, 86, 89, 92, 95, 98, 101, 104, 107, 70};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//标准化到相同长度然后堆叠
		regionBed = new RegionBed("tp53\tchr1:1-20;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{92.5, 94.5, 96.5, 98.5, 100.5, 102.5, 104.5, 106.5, 108.5, 110.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//标准化到相同长度然后堆叠
		regionBed = new RegionBed("tp53\tchr1:1-20;chr1:40-36;ch1:51-60", EnumTssPileUpType.pileup_norm_to_length, 10);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{92.5, 95.5, 97.5, 100.5, 102.5, 105.5, 107.5, 110.5, 112.5, 115.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并截短
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_cut, 12);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{ 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 11, 12};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并延长
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60",  EnumTssPileUpType.pileup_cut, 17);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{ 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 11, 12, 13, 14, 15, 0, 0};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠
		regionBed = new RegionBed("tp53\tchr1:1-10;chr1:40-36;ch1:56-60", EnumTssPileUpType.pileup_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{97.5, 99.5, 53.5, 7.5, 9.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:31-35;ch1:51-55", EnumTssPileUpType.connect_cut, 11);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{1, 2, 3, 4, 5, 31, 32, 33, 34, 35, 51};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起并标准化
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:6-10", EnumTssPileUpType.connect_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		values = new double[]{1.5, 3.5, 5.5, 7.5, 9.5};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
	}
	
	@Test
	public void testGetRegionInfoTrans() {
		MapReadsStub mapReadsStub = new MapReadsStub();
		//标准化到相同长度然后堆叠
		RegionBed regionBed = new RegionBed("tp53\tch1:60-51;chr1:39-31;chr1:10-1", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		RegionValue regionValue = regionBed.getRegionInfo(mapReadsStub, new ReadsCoverageHandleFactory());
		double[] values = new double[]{109, 106,103, 100, 97, 94, 91, 88, 85, 52};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
	}
	
	@Test
	public void testGetRegionInfoCisMeth() {
		CpGCalculator cpGCalculator = new CpGCalculator();
		cpGCalculator.setCpGCalculateType(EnumBspCpGCalculateType.CpGRatio);
		cpGCalculator.setCpGmethyType(EnumCpGmethyType.ALL);

		ReadsCoverageHandleFactory readsCoverageHandleFactory = new ReadsCoverageHandleFactory();
		readsCoverageHandleFactory.setCpGCalculator(cpGCalculator);
		MapReadsBSPStub mapReadsStub = new MapReadsBSPStub();
		//标准化到相同长度然后堆叠
		RegionBed regionBed = new RegionBed("tp53\tchr1:1-10;chr1:31-39;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		RegionValue regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		double[] values = new double[]{0.79, 0.84, 0.08, 0.92, 0.81, 0.98, 0.19, 1.02, 0.82, 0.66};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//标准化到相同长度然后堆叠
		regionBed = new RegionBed("tp53\tchr1:1-20;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.86, 0.91, 0.56, 0.61, 1.05, 1.06, 0.67, 0.72, 1.12, 1.13};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//标准化到相同长度然后堆叠
		regionBed = new RegionBed("tp53\tchr1:1-20;chr1:40-36;ch1:51-60", EnumTssPileUpType.pileup_norm_to_length, 10);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.86, 0.91, 0.56, 1.01, 1.05, 1.07, 0.28, 0.72, 1.13, 1.14};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并截短
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60", EnumTssPileUpType.pileup_cut, 12);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.81, 0.86, 0.48, 0.54, 0.81, 0.98, 0.57, 0.63, 0.80, 1.04, 0.25, 0.26};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠并延长
		regionBed = new RegionBed("tp53\tchr1:1-15;chr1:40-31;ch1:51-60",  EnumTssPileUpType.pileup_cut, 17);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.81, 0.86, 0.48, 0.54, 0.81, 0.98, 0.57, 0.63, 0.80, 1.04, 0.25, 0.26, 0.0, 0.28, 0.29, 0.0, 0.0};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//直接堆叠
		regionBed = new RegionBed("tp53\tchr1:1-10;chr1:40-36;ch1:56-60", EnumTssPileUpType.pileup_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.63, 0.72, 0.49, 0.20, 0.12};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:31-35;ch1:51-55", EnumTssPileUpType.connect_cut, 11);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.0, 0.05, 0.08, 0.12, 0.0, 0.38, 0.38, 0.0, 0.38, 0.39, 0.42};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
		
		//连接到一起并标准化
		regionBed = new RegionBed("tp53\tchr1:1-5;chr1:6-10", EnumTssPileUpType.connect_norm, 5);
		regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		values = new double[]{0.05, 0.09, 0.17, 0.19, 0.24};
		Assert.assertArrayEquals(values, regionValue.values, 0.01);
	}
	
	@Test
	public void testGetRegionInfoTransMeth() {
		CpGCalculator cpGCalculator = new CpGCalculator();
		cpGCalculator.setCpGCalculateType(EnumBspCpGCalculateType.CpGRatio);
		cpGCalculator.setCpGmethyType(EnumCpGmethyType.CG);
		ReadsCoverageHandleFactory readsCoverageHandleFactory = new ReadsCoverageHandleFactory();
		readsCoverageHandleFactory.setCpGCalculator(cpGCalculator);
		
		MapReadsBSPStub mapReadsStub = new 		MapReadsBSPStub();
		//标准化到相同长度然后堆叠
		RegionBed regionBed = new RegionBed("tp53\tch1:60-51;chr1:39-31;chr1:10-1", EnumTssPileUpType.pileup_long_norm_to_length, 10);
		RegionValue regionValue = regionBed.getRegionInfo(mapReadsStub, readsCoverageHandleFactory);
		double[] values = new double[]{0.24, 0.39, 0.43, 0.0, 0.17, 0.38, 0.42, 0.0, 0.045, 0.0};
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
