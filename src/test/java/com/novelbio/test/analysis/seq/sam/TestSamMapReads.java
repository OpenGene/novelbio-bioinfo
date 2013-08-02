package com.novelbio.test.analysis.seq.sam;

import java.util.List;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapReads;

public class TestSamMapReads extends TestCase {
	SamFile samFile;
	SamMapReads samMapReads;
	GffHashGene gffHashGene;
	@Override
	protected void setUp() {
		samFile = new SamFile("/home/zong0jie/Desktop/paper/chicken/DT40KO.bam");
		gffHashGene = new GffHashGene(GffType.GTF, "/home/zong0jie/Desktop/paper/chicken/gal4-merged.gtf");
		samMapReads = new SamMapReads(samFile);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		int i = 0;
		List<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (!samFile.getMapChrIDLowcase2Length().containsKey(gffGeneIsoInfo.getRefID().toLowerCase())) {
					continue;
				}
				List<double[]> lsInfoOld = samMapReads.getRangeInfoLsOld(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo);
				List<double[]> lsInfo = samMapReads.getRangeInfoLs(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo);
				for (int j = 0; j < lsInfo.size(); j++) {
					double[] tmpInfo = lsInfo.get(j);
					double[] tmpInfo2 = lsInfoOld.get(j);
					for (int k = 0; k < tmpInfo.length; k++) {
						double tmpDetail1 = tmpInfo[k];
						double tmpDetail2 = tmpInfo2[k];
						assertEquals(tmpDetail1, tmpDetail2);
					}
				}
			}
			i++;
			System.out.println(gffDetailGene.isCis5to3());
			System.out.println(i);
			if (i > 200) {
				break;
			}
		}
	}
	
	
}
