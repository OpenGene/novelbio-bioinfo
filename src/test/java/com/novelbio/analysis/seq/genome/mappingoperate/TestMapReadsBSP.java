package com.novelbio.analysis.seq.genome.mappingoperate;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.shell.PathData;
import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.mappingoperate.MapReadsBSP;
import com.novelbio.analysis.seq.genome.mappingoperate.MapReadsBSP.CpGCalculator;
import com.novelbio.analysis.seq.genome.mappingoperate.MapReadsBSP.EnumCpGmethyType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestMapReadsBSP {
	
	@Test
	public void testReadBspFile() {
		MapReadsBSP mapReadsBSP = new MapReadsBSP();
		Map<String, Long> mapChrID2Len = new HashMap<>();
		String bedFileBSP = "/tmp/bspfile" + DateUtil.getDateAndRandom();
		TxtReadandWrite txtWrite = new TxtReadandWrite(bedFileBSP, true);
		txtWrite.writefileln("chr1	1	-	0	0	CHH	CNN");
		txtWrite.writefileln("chr1	2	-	20	0	CHG	CNN");
		txtWrite.writefileln("chr1	10	+	0	20	CHG	CNN");
		txtWrite.writefileln("chr1	14	+	120	3120	CG	CNN");
		txtWrite.writefileln("chr1	20	+	12320	1230	CHH	CNN");
		txtWrite.writefileln("chr1	21	-	210	10	CG	CNN");
		
		txtWrite.writefileln("chr2	14	+	120	3120	CG	CNN");
		txtWrite.writefileln("chr2	18	-	12320	1230	CHH	CNN");
		txtWrite.writefileln("chr2	20	-	210	10	CG	CNN");
		txtWrite.close();
		mapChrID2Len.put("chr1", 12345L);
		mapChrID2Len.put("chr2", 12345L);

		mapReadsBSP.setMapChrID2Len(mapChrID2Len);
		mapReadsBSP.setReadsInfoFile(bedFileBSP);
		mapReadsBSP.run();
		
		double[] tmpResult = mapReadsBSP.getRangeInfo(new Align("chr1:1-10"), 0);
		double[] expect = new double[]{-3, -2000002, 0, 0, 0, 0, 0, 0, 0, 202,};
		Assert.assertArrayEquals(expect, tmpResult, 0.0001);
		
		tmpResult = mapReadsBSP.getRangeInfo(new Align("chr1:13-22"), 0);
		expect = new double[]{0, 12031201, 0, 0, 0, 0, 0, 999909983, -21000101, 0};
		Assert.assertArrayEquals(expect, tmpResult, 0.0001);
		
		tmpResult = mapReadsBSP.getRangeInfo(new Align("chr2:13-21"), 0);
		expect = new double[]{0, 12031201, 0, 0, 0, -999909983, 0, -21000101, 0};
		Assert.assertArrayEquals(expect, tmpResult, 0.0001);
		
		FileOperate.deleteFileFolder(bedFileBSP);
	}
	
	@Test
	public void testCalculateCpGInfo() {
		CpGCalculator calculator = new CpGCalculator();
		calculator.setCpGmethyType(null);
		double[] tmpResult = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		double[] result = calculator.calculateCpGInfo(tmpResult, 3);
		Assert.assertArrayEquals(new double[]{2, 5, 8}, result, 0.00001);
			
		result = calculator.calculateCpGInfo(tmpResult, 5);
		Assert.assertArrayEquals(new double[]{1.5, 3.5, 5.5, 7.5, 9.5}, result, 0.00001);
		
		result = calculator.calculateCpGInfo(tmpResult, 20);
		Assert.assertArrayEquals(new double[]{1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10}, result, 0.00001);
	}
	
	@Test
	public void testFilterCpGType() {
		CpGCalculator calculator = new CpGCalculator();
		calculator.setCpGmethyType(EnumCpGmethyType.CG);
		int[] cpGValues = new int[]{0,1001212,-2,1001211, 200002};
		int[] cpGResult = calculator.filterCpGType(cpGValues);
		Assert.assertArrayEquals(new int[]{0, 0, 0, 1001211, 0}, cpGResult);

		calculator.setCpGmethyType(EnumCpGmethyType.ALL);
		calculator.setCoverageFilter(10);
		cpGValues = new int[]{0,1001212,-2,1001211, 200002};
		cpGResult = calculator.filterCpGType(cpGValues);
		Assert.assertArrayEquals(new int[]{0, 1001212, 0, 1001211, 0}, cpGResult);
		
		calculator = new CpGCalculator();
		calculator.setCpGmethyType(EnumCpGmethyType.CHH);
		calculator.setIsCis5To3(true);
		calculator.setCoverageFilter(0);
		cpGValues = new int[]{0,1001213, 53, -1001213, 200002};
		cpGResult = calculator.filterCpGType(cpGValues);
		Assert.assertArrayEquals(new int[]{0, 1001213, 53, 0, 0}, cpGResult);
		
		calculator = new CpGCalculator();
		calculator.setCpGmethyType(EnumCpGmethyType.CHH);
		calculator.setIsCis5To3(true);
		calculator.setCoverageFilter(10);
		cpGValues = new int[]{0,1001213, 53, -1001213, 200002};
		cpGResult = calculator.filterCpGType(cpGValues);
		Assert.assertArrayEquals(new int[]{0, 1001213, 0, 0, 0}, cpGResult);
	}
}
