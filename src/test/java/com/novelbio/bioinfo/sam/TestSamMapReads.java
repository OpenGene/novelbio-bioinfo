package com.novelbio.bioinfo.sam;

import java.util.List;

import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamMapReads;

import junit.framework.TestCase;

public class TestSamMapReads extends TestCase {
	SamFile samFile;
	SamMapReads samMapReads;
	GffHashGene gffHashGene;
	@Override
	protected void setUp() {
		samFile = new SamFile("/home/zong0jie/Desktop/paper/chicken/DT40KO.bam");
		gffHashGene = new GffHashGene(GffType.GTF, "/home/zong0jie/Desktop/paper/chicken/gal4-merged.gtf");
		samMapReads = new SamMapReads(samFile, StrandSpecific.NONE);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		int i = 0;
		List<GffGene> lsGffDetailGenes = gffHashGene.getLsGffDetailGenes();
		for (GffGene gffDetailGene : lsGffDetailGenes) {
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (!samFile.getMapChrIDLowcase2Length().containsKey(gffGeneIsoInfo.getRefIDlowcase().toLowerCase())) {
					continue;
				}
				List<double[]> lsInfoOld = samMapReads.getRangeInfoLsOld(gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.getLsElement());
				List<double[]> lsInfo = samMapReads.getRangeInfoLs(gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.getLsElement());
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
