package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.mockito.Mockito;
import org.mockito.internal.matchers.AnyVararg;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import com.novelbio.analysis.seq.chipseq.Gene2Value;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.RegionInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.modgeneid.GeneType;

import junit.framework.TestCase;
/**
 * 测试 暂时没有测边界值
 * @author zong0jie
 *
 */
public class TestGene2Value extends TestCase {
	Gene2Value gene2ValueCis = new Gene2Value(null);
	Gene2Value gene2ValueTrans = new Gene2Value(null);

	GffGeneIsoInfo gffGeneIsoInfoCis = GffGeneIsoInfo.createGffGeneIso("test", "test", GeneType.mRNA, true);
	GffDetailGene geneCis = new GffDetailGene("chr1", "test", true);
	GffGeneIsoInfo gffGeneIsoInfoTrans = GffGeneIsoInfo.createGffGeneIso("test", "test", GeneType.mRNA, false);
	GffDetailGene geneTrans = new GffDetailGene("chr1", "test", false);
	
	public static class Test {
		public String getResult(Align align, int run) {
			return run + "";
		}
	}
	
	public void testSelectLsExonInfo() {
		setUpNorm();
		getSelectLsExonInfo();
		getSelectLsExonInfo2();
		getSelectLsExonInfo3();
		getOverlapTest();
		
		removeTest();
		removeTest2();
		removeOverlapTest();
	}
	protected void setUpNorm() {
		gffGeneIsoInfoCis = GffGeneIsoInfo.createGffGeneIso("test", "test", GeneType.mRNA, true);
		gffGeneIsoInfoCis.add(new ExonInfo(true, 3100, 3200));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 3300, 3400));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 3500, 3600));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 3700, 3800));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 3900, 4000));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 4100, 4200));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 4300, 4400));
		gffGeneIsoInfoCis.setGffDetailGeneParent(geneCis);
		gene2ValueCis = new Gene2Value(gffGeneIsoInfoCis);
		
		gffGeneIsoInfoTrans = GffGeneIsoInfo.createGffGeneIso("test", "test", GeneType.mRNA, false);
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 3100, 3200));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 3300, 3400));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 3500, 3600));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 3700, 3800));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 3900, 4000));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 4100, 4200));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 4300, 4400));
		gffGeneIsoInfoTrans.sort();
		gffGeneIsoInfoTrans.setGffDetailGeneParent(geneTrans);
		gene2ValueTrans = new Gene2Value(gffGeneIsoInfoTrans);
	}
	
	private void getSelectLsExonInfo() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 1), lsExonInfo.get(2));
	}
	
	private void getSelectLsExonInfo2() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-2); lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(4, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 2), lsExonInfo.get(2));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 1), lsExonInfo.get(3));
	}
	private void getSelectLsExonInfo3() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(6);
		gene2ValueCis.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 2), lsExonInfo.get(2));
	}
	private void getOverlapTest() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(6);
		lsGet.add(-2);
		lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(4, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 2), lsExonInfo.get(2));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 1), lsExonInfo.get(3));
	}
	
	private void removeTest() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(4, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(2), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(3), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(4), lsExonInfo.get(2));
		assertEquals(gffGeneIsoInfoCis.get(5), lsExonInfo.get(3));
	}
	
	private void removeTest2() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(-2);
		lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(2), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(3), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(4), lsExonInfo.get(2));
	}

	private void removeOverlapTest() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(6);
		lsGet.add(-2);
		lsGet.add(-1);
		gene2ValueCis.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2ValueCis.getSelectLsExonInfo(gffGeneIsoInfoCis.getLsElement());
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(2), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(3), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(4), lsExonInfo.get(2));
	}
	
	public void testGetRegionInfoTssTes() {
		MapReadsStub mapReads = new MapReadsStub();
		setUpNorm();
		
		gene2ValueCis.setSplitNum(-1);
		gene2ValueCis.setPlotTssTesRegion(new int[]{-50,100});
		RegionInfo regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.TSS);
		double[] tssValues = new double[151];
		for (int i = 0; i <= 150; i++) {
			tssValues[i] = i + 3050;
		}
		assertDoubleArray(tssValues, regionInfo.getDoubleRaw());
		
		regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.TES);
		tssValues = new double[151];
		for (int i = 0; i <= 150; i++) {
			tssValues[i] = i + 4350;
		}
		assertDoubleArray(tssValues, regionInfo.getDoubleRaw());
		
		gene2ValueTrans.setSplitNum(-1);
		gene2ValueTrans.setPlotTssTesRegion(new int[]{-50,100});
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.TSS);
		tssValues = new double[151];
		for (int i = 0; i <= 150; i++) {
			tssValues[i] = 150 - i + 4300;
		}
		assertDoubleArray(tssValues, regionInfo.getDouble());
		
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.TES);
		tssValues = new double[151];
		for (int i = 0; i <= 150; i++) {
			tssValues[i] = 150 - i + 3000;
		}
		assertDoubleArray(tssValues, regionInfo.getDouble());
		
	}
	
	public void testGetRegionInfoExon() {
		MapReadsStub mapReads = new MapReadsStub();
		setUpNorm();
		//================== cis ================================
		gene2ValueCis.setSplitNum(-1);
		List<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-1);
		
		gene2ValueCis.setGetNum(lsGet, true);
		gene2ValueCis.setExonIntronPileUp(true);
		RegionInfo regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.EXON);
		double[] exonValue = new double[101];
		for (int i = 0; i <= 100; i++) {
			exonValue[i] = ((double)i*3 + 3100 + 3300 + 4300)/3;
		}
		assertDoubleArray(exonValue, regionInfo.getDoubleRaw());
		
		gene2ValueCis.setGetNum(lsGet, true);
		gene2ValueCis.setExonIntronPileUp(false);
		regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.EXON);
		exonValue = new double[303];
		for (int i = 0; i <= 100; i++) {
			exonValue[i] =  i + 3100;
		}
		for (int i = 101; i <= 201; i++) {
			exonValue[i] =  i - 101 + 3300;
		}
		for (int i = 202; i <= 302; i++) {
			exonValue[i] =  i - 202 + 4300;
		}
		assertDoubleArray(exonValue, regionInfo.getDoubleRaw());
		
		//================== trans ================================
		gene2ValueTrans.setSplitNum(-1);
		lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-1);
		
		gene2ValueTrans.setGetNum(lsGet, true);
		gene2ValueTrans.setExonIntronPileUp(true);
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.EXON);
		exonValue = new double[101];
		for (int i = 0; i <= 100; i++) {
			exonValue[i] = ((double)i*3 + 4300 + 4100 + 3100)/3;
		}
		ArrayOperate.convertArray(exonValue);
		assertDoubleArray(exonValue, regionInfo.getDouble());
		
		gene2ValueTrans.setGetNum(lsGet, true);
		gene2ValueTrans.setExonIntronPileUp(false);
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.EXON);
		exonValue = new double[303];
		for (int i = 0; i <= 100; i++) {
			exonValue[i] =  i + 3100;
		}
		for (int i = 101; i <= 201; i++) {
			exonValue[i] =  i - 101 + 4100;
		}
		for (int i = 202; i <= 302; i++) {
			exonValue[i] =  i - 202 + 4300;
		}
		ArrayOperate.convertArray(exonValue);
		assertDoubleArray(exonValue, regionInfo.getDouble());
	}
	
	public void testGetRegionInfoIntron() {
		MapReadsStub mapReads = new MapReadsStub();
		setUpNorm();
		//================== cis ================================
		gene2ValueCis.setSplitNum(-1);
		List<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-1);
		
		gene2ValueCis.setGetNum(lsGet, true);
		gene2ValueCis.setExonIntronPileUp(true);
		RegionInfo regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.INTRON);
		double[] exonValue = new double[99];
		for (int i = 0; i <= 98; i++) {
			exonValue[i] = ((double)i*3 + 3201 + 3401 + 4201)/3;
		}
		assertDoubleArray(exonValue, regionInfo.getDoubleRaw());
		
		gene2ValueCis.setGetNum(lsGet, true);
		gene2ValueCis.setExonIntronPileUp(false);
		regionInfo = gene2ValueCis.getRegionInfo(mapReads, GeneStructure.INTRON);
		exonValue = new double[297];
		for (int i = 0; i < 99; i++) {
			exonValue[i] =  i + 3201;
		}
		for (int i = 99; i < 198; i++) {
			exonValue[i] =  i - 99 + 3401;
		}
		for (int i = 198; i < 297; i++) {
			exonValue[i] =  i - 198 + 4201;
		}
		assertDoubleArray(exonValue, regionInfo.getDoubleRaw());
		
		//================== trans ================================
		gene2ValueTrans.setSplitNum(-1);
		lsGet = new ArrayList<Integer>();
		lsGet.add(1); lsGet.add(2); lsGet.add(-1);
		
		gene2ValueTrans.setGetNum(lsGet, true);
		gene2ValueTrans.setExonIntronPileUp(true);
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.INTRON);
		exonValue = new double[99];
		for (int i = 0; i <= 98; i++) {
			exonValue[i] = ((double)i*3 + 4201 + 4001 + 3201)/3;
		}
		ArrayOperate.convertArray(exonValue);
		assertDoubleArray(exonValue, regionInfo.getDouble());
		
		gene2ValueTrans.setGetNum(lsGet, true);
		gene2ValueTrans.setExonIntronPileUp(false);
		regionInfo = gene2ValueTrans.getRegionInfo(mapReads, GeneStructure.INTRON);
		exonValue = new double[297];
		for (int i = 0; i < 99; i++) {
			exonValue[i] =  i + 3201;
		}
		for (int i = 99; i < 198; i++) {
			exonValue[i] =  i - 99 + 4001;
		}
		for (int i = 198; i < 297; i++) {
			exonValue[i] =  i - 198 + 4201;
		}
		ArrayOperate.convertArray(exonValue);
		assertDoubleArray(exonValue, regionInfo.getDouble());
	}
	
	private void assertDoubleArray(double[] db0, double[] db1) {
		assertEquals(db0.length, db1.length);
		for (int i = 0; i < db0.length; i++) {
	        assertEquals(db0[i], db1[i]);
        }
		
	}
	
	
	public void testIsMatchExonNum() {
		assertEquals(true, Gene2Value.isMatchExonNum(2, null));
		assertEquals(true, Gene2Value.isMatchExonNum(2, new int[]{-1,-1}));
		assertEquals(true, Gene2Value.isMatchExonNum(2, new int[]{1,2}));
		assertEquals(false, Gene2Value.isMatchExonNum(10, new int[]{2,5}));
		assertEquals(true, Gene2Value.isMatchExonNum(10, new int[]{2,-1}));
		assertEquals(false, Gene2Value.isMatchExonNum(10, new int[]{-1,9}));
		assertEquals(false, Gene2Value.isMatchExonNum(10, new int[]{11,12}));
	}
}
