package com.novelbio.test.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.analysis.seq.rnaseq.TophatJunctionOld;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.SepSign;

//TODO 还没写完
public class TestTophatJunction extends TestCase {
	TophatJunction tophatJunction = new TophatJunction();
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertEquals(145, tophatJunction.getJunctionSite("KO", "chr1", 4782733));
		assertEquals(146, tophatJunction.getJunctionSite("WT", "chr1", 4782733));
	}
	public void test2() {
		AlignSamReading alignSamReading = new AlignSamReading(new SamFile("/media/winE/NBC/Project/Project_FY/paper/KOod.bam"));
		List<Align> lsAlignments = new ArrayList<>();
		lsAlignments.add(new Align("chrX", 159840368, 159840811));
		alignSamReading.setLsAlignments(lsAlignments);
		
		TophatJunctionOld tophatJunctionOld = new TophatJunctionOld();
		tophatJunctionOld.setCondition("test1");
		alignSamReading.addAlignmentRecorder(tophatJunctionOld);
		
		TophatJunction tophatJunction = new TophatJunction();
		tophatJunction.setCondition("test1");
		alignSamReading.addAlignmentRecorder(tophatJunction);

		alignSamReading.run();
		tophatJunction.conclusion();
		
		for (String string : tophatJunctionOld.getMapCond_To_JuncPair2ReadsNum().get("test1").keySet()) {
			String[] ss = string.split(SepSign.SEP_INFO);
			String chrID = ss[0].split(SepSign.SEP_INFO_SAMEDB)[0];
			int locStartSite = Integer.parseInt(ss[0].split(SepSign.SEP_INFO_SAMEDB)[1]);
			int locEndSite =  Integer.parseInt(ss[1].split(SepSign.SEP_INFO_SAMEDB)[1]);
			int numOld = tophatJunctionOld.getJunctionSite("test1", chrID, locStartSite, locEndSite);
			int numOld2 = tophatJunction.getJunctionSite("test1", chrID, locStartSite);
			int numOld3 = tophatJunction.getJunctionSite("test1", chrID, locEndSite);
			
			int numNew = tophatJunction.getJunctionSite("test1", chrID, locStartSite, locEndSite);
			int numNew2 = tophatJunction.getJunctionSite("test1", chrID, locStartSite);
			int numNew3 = tophatJunction.getJunctionSite("test1", chrID, locEndSite);
			System.out.println();
			numOld = tophatJunctionOld.getJunctionSite("test1", chrID, locStartSite, locEndSite);
			numNew = tophatJunction.getJunctionSite("test1", chrID, locStartSite, locEndSite);
		}
		
		
	}
}
