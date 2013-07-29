package com.novelbio.test.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.rnaseq.GenerateNewIso;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.TestCase;

public class TestGenerateNewIso  extends TestCase {
	GenerateNewIso  generateNewIso;
	
	// 100-200----300-400----500-600----700-800----900-1000----1100-1200
	//  100-240----320-450-------------------720-810---910-950----1100-1200
	GffDetailGene gffDetailGene1 = new GffDetailGene("chr1", "Test", true);
	List<JunctionUnit> lsJun1 = new ArrayList<>();
	

	GffDetailGene gffDetailGene2 = new GffDetailGene("chr1", "Test", true);
	List<JunctionUnit> lsJun2 = new ArrayList<>();

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setGffGene1();
		setJun1();
		
		setGffGene2();
		setJun2();
	}
	
	private void setGffGene1() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene1.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene1.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene1.addIso(gffGeneIsoInfo1);
	}
	private void setJun1() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 450, 720);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 810, 910);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 950, 1100);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit3.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit4.addJunAfterAbs(null);
		lsJun1.add(junctionUnit1); lsJun1.add(junctionUnit2); lsJun1.add(junctionUnit3); lsJun1.add(junctionUnit4);
	}
	
	private void setGffGene2() {
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2.addIso(gffGeneIsoInfo1);
		
		gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test2", "test", GeneType.mRNA, true);
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
//		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene2.addIso(gffGeneIsoInfo1);
	}
	private void setJun2() {
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
		lsJun2.add(junctionUnit1); lsJun2.add(junctionUnit2); lsJun2.add(junctionUnit3); lsJun2.add(junctionUnit4);
		lsJun2.add(junctionUnit5);lsJun2.add(junctionUnit6);
	}
	@Test
	public void testInfo1() {
		generateNewIso = new GenerateNewIso();
		generateNewIso.setGffDetailGene(gffDetailGene1);
		for (JunctionUnit junctionUnit : lsJun1) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		System.out.println();
	}
	
	// 100-200----300-400----500-600-------------700--800----900-1000----1100-1200
	//  100-240----320-450----520-600---650-700----750-810---910-950----1100-1200
	@Test
	public void testInfo2() {
		generateNewIso = new GenerateNewIso();
		generateNewIso.setGffDetailGene(gffDetailGene2);
		for (JunctionUnit junctionUnit : lsJun2) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		gffDetailGene2.removeDupliIso();
		System.out.println();
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	
}
