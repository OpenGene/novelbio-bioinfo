package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.BedPeakSicer;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class UTXmapping {
	public static void main(String[] args) {
		String parent = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/";
		String fastqFHE = parent + "FHE.clean.fq.gz";
		String fastqGZW4 = parent + "HSZ_W-4.clean.fq.gz";
//		pipleChIPLine(fastqGZK4, parent + "PeakCalling/K4sicer");
		pipleChIPLineMACS(fastqFHE, parent + "PeakCalling/FHE");
	}
	
	
	public static void pipleChIPLineSICER(String fastqGZ, String PeakFile) {
		FastQ fastQ = new FastQ(fastqGZ, FastQ.QUALITY_MIDIAN);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		String filter = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "fq");
		fastQ.filterReads(filter);
		String mapping = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "sam");
		FastQMapBwa fastQMapBwa = new FastQMapBwa(filter, FastQ.QUALITY_MIDIAN,mapping , true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta");
		String bedFile = FileOperate.changeFileSuffix(fastqGZ, "_SE", "bed");
		
		String bedFilePE = FileOperate.changeFileSuffix(fastqGZ, "_Extend", "bed");
		fastQMapBwa.getBedFileSE(bedFile);
		fastQMapBwa.getBedFileSE(bedFilePE);
		
		BedPeakSicer bedPeakSicer = new BedPeakSicer(bedFile);
		bedPeakSicer.setChIPType(BedPeakSicer.HISTONE_TYPE_H3K27);
		bedPeakSicer.setEffectiveGenomeSize(78);
		bedPeakSicer.peakCallling(null, BedPeakSicer.SPECIES_MOUSE, FileOperate.getParentPathName(PeakFile), FileOperate.getFileName(PeakFile));
	}
	
	public static void pipleChIPLineMACS(String fastqGZ, String PeakFile) {
		FastQ fastQ = new FastQ(fastqGZ, FastQ.QUALITY_MIDIAN);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		String filter = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "fq");
		fastQ.filterReads(filter);
		String mapping = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "sam");
		FastQMapBwa fastQMapBwa = new FastQMapBwa(filter, FastQ.QUALITY_MIDIAN,mapping , true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta");
		String bedFile = FileOperate.changeFileSuffix(fastqGZ, "_SE", "bed");
		
		String bedFilePE = FileOperate.changeFileSuffix(fastqGZ, "_Extend", "bed");
		fastQMapBwa.getBedFileSE(bedFile);
		fastQMapBwa.getBedFileSE(bedFilePE);
		
		BedPeakMacs bedPeakMacs = new BedPeakMacs(bedFile);
//		bedPeakMacs.setNoLambda();
		bedPeakMacs.peakCallling(null, BedPeakMacs.SPECIES_MOUSE, FileOperate.getParentPathName(PeakFile), FileOperate.getFileName(PeakFile));
	}
}
