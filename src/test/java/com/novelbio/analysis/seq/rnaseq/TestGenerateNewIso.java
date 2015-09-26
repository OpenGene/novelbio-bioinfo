package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.GenerateNewIso;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.TestCase;

public class TestGenerateNewIso  extends TestCase {
	GenerateNewIso  generateNewIso;
	
	// 100-200----300-400----500-600----700-800----900-1000----1100-1200
	//  100-240----320-450-------------------720-810---910-950----1100-1200
	GffDetailGene gffDetailGene1Cis = new GffDetailGene("chr1", "Test", true);
	List<JunctionUnit> lsJun1Cis = new ArrayList<>();

	GffDetailGene gffDetailGene1Trans = new GffDetailGene("chr1", "Test", false);
	List<JunctionUnit> lsJun1Trans = new ArrayList<>();
	
	GffDetailGene gffDetailGene2Cis = new GffDetailGene("chr1", "Test", true);
	List<JunctionUnit> lsJun2Cis = new ArrayList<>();
	
	GffDetailGene gffDetailGene2Trans = new GffDetailGene("chr1", "Test", false);
	List<JunctionUnit> lsJun2Trans = new ArrayList<>();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setGffGene1Cis();
		setJun1Cis();
		setGffGene1Trans();
		setJun1Trans();
		
		
		setGffGene2Cis();
		setJun2Cis();
		setGffGene2Trans();
		setJun2Trans();
	}
	
	private void setGffGene1Cis() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene1Cis.addIso(gffGeneIsoInfo1);
		
//		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
//		gffDetailGene1Cis.addIso(gffGeneIsoInfo1);
//		
//		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
//		gffDetailGene1Cis.addIso(gffGeneIsoInfo1);
	}
	private void setJun1Cis() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 400, 500);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 600, 710);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 820, 910);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit3.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit4.addJunAfterAbs(null);
//		lsJun1Cis.add(junctionUnit1);
		lsJun1Cis.add(junctionUnit2); lsJun1Cis.add(junctionUnit3); lsJun1Cis.add(junctionUnit4);
	}
	
	
	private void setGffGene1Trans() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, false);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffGeneIsoInfo1.sort();
		gffDetailGene1Trans.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, false);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffGeneIsoInfo1.sort();
		gffDetailGene1Trans.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, false);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffGeneIsoInfo1.sort();
		gffDetailGene1Trans.addIso(gffGeneIsoInfo1);
	}
	private void setJun1Trans() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 450, 720);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 810, 910);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 950, 1100);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit3.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit4.addJunAfterAbs(null);
		lsJun1Trans.add(junctionUnit1); lsJun1Trans.add(junctionUnit2); lsJun1Trans.add(junctionUnit3); lsJun1Trans.add(junctionUnit4);
	}

	
	private void setGffGene2Cis() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2Cis.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2Cis.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2Cis.addIso(gffGeneIsoInfo1);
	}
	private void setJun2Cis() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 450, 520);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 600, 650);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 700, 750);
		JunctionUnit junctionUnit5 = new JunctionUnit("chr1", 810, 910);
		JunctionUnit junctionUnit6 = new JunctionUnit("chr1", 950, 1100);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit3.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit4.addJunAfterAbs(junctionUnit5);
		junctionUnit5.addJunBeforeAbs(junctionUnit4); junctionUnit5.addJunAfterAbs(junctionUnit6);
		junctionUnit6.addJunBeforeAbs(junctionUnit5); junctionUnit6.addJunAfterAbs(null);
		lsJun2Cis.add(junctionUnit1); lsJun2Cis.add(junctionUnit2); lsJun2Cis.add(junctionUnit3); lsJun2Cis.add(junctionUnit4);
		lsJun2Cis.add(junctionUnit5);lsJun2Cis.add(junctionUnit6);
	}
	
	private void setGffGene2Trans() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, false);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
////		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
//		gffGeneIsoInfo1.sort();
//		gffDetailGene2Trans.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, false);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffGeneIsoInfo1.sort();
		gffDetailGene2Trans.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, false);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffGeneIsoInfo1.sort();
		gffDetailGene2Trans.addIso(gffGeneIsoInfo1);
	}
	private void setJun2Trans() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 450, 520);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 600, 650);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 700, 750);
		JunctionUnit junctionUnit5 = new JunctionUnit("chr1", 810, 910);
		JunctionUnit junctionUnit6 = new JunctionUnit("chr1", 950, 1100);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit3.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit4.addJunAfterAbs(junctionUnit5);
		junctionUnit5.addJunBeforeAbs(junctionUnit4); junctionUnit5.addJunAfterAbs(junctionUnit6);
		junctionUnit6.addJunBeforeAbs(junctionUnit5); junctionUnit6.addJunAfterAbs(null);
		lsJun2Trans.add(junctionUnit1); lsJun2Trans.add(junctionUnit2); lsJun2Trans.add(junctionUnit3); lsJun2Trans.add(junctionUnit4);
		lsJun2Trans.add(junctionUnit5);lsJun2Trans.add(junctionUnit6);
	}
	
	@Test
	public void testInfo1Cis() {
		generateNewIso = new GenerateNewIso(null, null, StrandSpecific.NONE, false);
		generateNewIso.setGffDetailGene(gffDetailGene1Cis);
		for (JunctionUnit junctionUnit : lsJun1Cis) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		System.out.println();
	}
	
	@Test
	public void testInfo1Trans() {
		generateNewIso = new GenerateNewIso(null, null, StrandSpecific.NONE, false);
		generateNewIso.setGffDetailGene(gffDetailGene1Trans);
		for (JunctionUnit junctionUnit : lsJun1Trans) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		System.out.println();
	}
	
	// 100-200----300-400----500-600-------------700--800----900-1000----1100-1200
	//  100-240----320-450----520-600---650-700----750-810---910-950----1100-1200
	//TODＯ 还是有问题
	@Test
	public void testInfo2() {
		generateNewIso = new GenerateNewIso(null, null, StrandSpecific.NONE, false);
		generateNewIso.setGffDetailGene(gffDetailGene2Cis);
		for (JunctionUnit junctionUnit : lsJun2Cis) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		gffDetailGene2Cis.removeDupliIso();
		System.out.println();
	}
	
	@Test
	public void testInfo2Trans() {
		generateNewIso = new GenerateNewIso(null, null, StrandSpecific.NONE, false);
		generateNewIso.setGffDetailGene(gffDetailGene2Trans);
		for (JunctionUnit junctionUnit : lsJun2Trans) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		gffDetailGene2Trans.removeDupliIso();
		System.out.println();
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	
}
