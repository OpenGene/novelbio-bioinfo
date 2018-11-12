package com.novelbio.software.rnaqc;

import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.sam.AlignSeqReading;
import com.novelbio.bioinfo.sam.SamFile;

public class MainJunctionSaturation {
	
	public static void main(String[] args) {
		JunctionSaturationJava junctionSaturation = new JunctionSaturationJava();
//		Species species = new Species(9606);
//		species.setVersion("hg19_GRCh37");
//		GffChrAbs gffChrAbs = new GffChrAbs(species);
		junctionSaturation.setGffHashGene(new GffHashGene("/run/media/novelbio/A/bianlianle/project/software_test/ref_GRCh37.p13_top_level.gff3.gtf"));
		AlignSeqReading alignSeqReading = new AlignSeqReading(new SamFile("/run/media/novelbio/A/bianlianle/project/software_test/rseqc/H2-SLEP_mapsplice.bam"));
		alignSeqReading.addAlignmentRecorder(junctionSaturation);
		alignSeqReading.run();
		junctionSaturation.setSavePath("/run/media/novelbio/A/bianlianle/project/software_test/rseqc/saturation.png");
		junctionSaturation.plot();
	}
	
}
