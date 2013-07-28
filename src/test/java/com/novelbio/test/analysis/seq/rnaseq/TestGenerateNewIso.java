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
	GffDetailGene gffDetailGene = new GffDetailGene("chr1", "Test", true);
	List<JunctionUnit> lsJun = new ArrayList<>();
	// 100-200----300-400----500-600----700-800----900-1000----1100-1200
	//  100-240----320-450-------------------720-810---910-950----1100-1200
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		GffGeneIsoInfo gffGeneIsoInfo1 = GffGeneIsoInfo.createGffGeneIso("test1", "test", GeneType.mRNA, true);
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 100, 200));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 300, 400));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 500, 600));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 700, 800));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 900, 1000));
		gffGeneIsoInfo1.add(new ExonInfo(gffGeneIsoInfo1, gffGeneIsoInfo1.isCis5to3(), 1100, 1200));
		gffDetailGene.addIso(gffGeneIsoInfo1);
		
		setJun();
	}
	
	private void setJun() {
		JunctionUnit junctionUnit1 = new JunctionUnit("chr1", 240, 320);
		JunctionUnit junctionUnit2 = new JunctionUnit("chr1", 450, 720);
		JunctionUnit junctionUnit3 = new JunctionUnit("chr1", 810, 910);
		JunctionUnit junctionUnit4 = new JunctionUnit("chr1", 950, 1100);
		junctionUnit1.addJunBeforeAbs(null); junctionUnit1.addJunAfterAbs(junctionUnit2);
		junctionUnit2.addJunBeforeAbs(junctionUnit1); junctionUnit2.addJunAfterAbs(junctionUnit3);
		junctionUnit3.addJunBeforeAbs(junctionUnit2); junctionUnit2.addJunAfterAbs(junctionUnit4);
		junctionUnit4.addJunBeforeAbs(junctionUnit3); junctionUnit2.addJunAfterAbs(null);
		lsJun.add(junctionUnit1); lsJun.add(junctionUnit2); lsJun.add(junctionUnit3); lsJun.add(junctionUnit4);
	}
	
	@Test
	public void testInfo() {
		generateNewIso = new GenerateNewIso();
		generateNewIso.setGffDetailGene(gffDetailGene);
		for (JunctionUnit junctionUnit : lsJun) {
			generateNewIso.reconstructIso(junctionUnit);
		}
		System.out.println();
	}
	
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	
}
