package com.novelbio.test.analysis.seq.genome;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.Gene2Value;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.TestCase;
/**
 * 测试
 * @author zong0jie
 *
 */
public class TestGene2Value extends TestCase {
	Gene2Value gene2Value = new Gene2Value(null);
	GffGeneIsoInfo gffGeneIsoInfoCis = new GffGeneIsoCis("test", GeneType.mRNA);
	@Override
	protected void setUp() throws Exception {
		gffGeneIsoInfoCis = new GffGeneIsoCis("test", GeneType.mRNA);
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 100, 200));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 300, 400));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 500, 600));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 700, 800));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 900, 1000));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 1100, 1200));
		gffGeneIsoInfoCis.add(new ExonInfo(gffGeneIsoInfoCis, true, 1300, 1400));
		
		gene2Value.setGffGeneIsoInfo(gffGeneIsoInfoCis);
		
		super.setUp();
	
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		getTest();
		getTest2();
		getOverlapTest();
		
		removeTest();
		removeTest2();
		removeOverlapTest();
	}
	
	private void getTest() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(-1);
		gene2Value.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 1), lsExonInfo.get(2));
	}
	
	private void getTest2() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(-2);
		lsGet.add(-1);
		gene2Value.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
		assertEquals(4, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(0), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(1), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 2), lsExonInfo.get(2));
		assertEquals(gffGeneIsoInfoCis.get(gffGeneIsoInfoCis.size() - 1), lsExonInfo.get(3));
	}
	
	private void getOverlapTest() {
		ArrayList<Integer> lsGet = new ArrayList<Integer>();
		lsGet.add(1);
		lsGet.add(2);
		lsGet.add(6);
		lsGet.add(-2);
		lsGet.add(-1);
		gene2Value.setGetNum(lsGet, true);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
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
		gene2Value.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
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
		gene2Value.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
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
		gene2Value.setGetNum(lsGet, false);
		List<ExonInfo> lsExonInfo = gene2Value.getSelectLsExonInfo(gffGeneIsoInfoCis);
		
		assertEquals(3, lsExonInfo.size());
		assertEquals(gffGeneIsoInfoCis.get(2), lsExonInfo.get(0));
		assertEquals(gffGeneIsoInfoCis.get(3), lsExonInfo.get(1));
		assertEquals(gffGeneIsoInfoCis.get(4), lsExonInfo.get(2));
	}

}
