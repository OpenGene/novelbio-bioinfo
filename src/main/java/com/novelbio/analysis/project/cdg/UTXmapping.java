package com.novelbio.analysis.project.cdg;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.BedPeakSicer;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.SAMtools;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class UTXmapping {
	public static void main(String[] args) {
		mappingTmp();
	}
	/**
	 * 徐龙勇补测的数据，合并后进行mapping
	 */
	private static void mappingAdd()
	{
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		String fastqKEGZ = parentFile + "KEall.fq";
		String peakFileKE = parentFile + "peakcalling/KE_ALL_SICER.xls";
//		pipleChIPLineSICER(fastqKEGZ, peakFileKE);
		
		String fastqWEGZ = parentFile + "WEall.fq";
		String peakFileWE = parentFile + "peakcalling/KE_ALL_SICER.xls";
		try {
//			pipleChIPLineSICER(fastqWEGZ, peakFileWE);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		String fastqK4GZ = parentFile + "K4all.fq";
		String peakFileK4 = parentFile + "peakcalling/K4_ALL_SICER.xls";
		try {
			pipleChIPLineSICER(false,fastqK4GZ, peakFileK4);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		String fastqW4GZ = parentFile + "W4all.fq";
		String peakFileW4 = parentFile + "peakcalling/W4_ALL_SICER.xls";
		try {
			pipleChIPLineSICER(false,fastqW4GZ, peakFileW4);
		} catch (Exception e) {
			// TODO: handle exception
		}
	
	}
	/**
	 * 零时给陈德桂做的东西
	 */
	private static void mappingTmp()
	{
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
		String peakFileK4 = parentFile + "peakcalling/K4_ALL_SICER.xls";
		BedPeakSicer bedPeakSicer = new BedPeakSicer("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/K4all_SE.bed");
		bedPeakSicer.setChIPType(BedPeakSicer.HISTONE_TYPE_H3K27);
		bedPeakSicer.setEffectiveGenomeSize(85);
		bedPeakSicer.peakCallling(null, BedPeakSicer.SPECIES_MOUSE, FileOperate.getParentPathName(peakFileK4), FileOperate.getFileName(peakFileK4));
		
	}
	
	public static void pipleChIPLineSICER(Boolean boofilter, String fastqGZ, String PeakFile) {
		FastQ fastQ = new FastQ(fastqGZ, FastQ.QUALITY_MIDIAN);
//		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		String filter = "";
		if (boofilter) {
			filter = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "fq");
			fastQ.filterReads(filter);
		}
		else {
			filter = fastqGZ;
		}
 
		String mapping = FileOperate.changeFileSuffix(fastqGZ, "_filtered", "sam");
		FastQMapBwa fastQMapBwa = new FastQMapBwa(filter, FastQ.QUALITY_MIDIAN,mapping , true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta");
		String bedFile = FileOperate.changeFileSuffix(fastqGZ, "_SE", "bed");
		
		String bedFilePE = FileOperate.changeFileSuffix(fastqGZ, "_Extend", "bed");
		fastQMapBwa.getBedFileSE(bedFile);
		fastQMapBwa.getBedFileSE(bedFilePE);
		
		BedPeakSicer bedPeakSicer = new BedPeakSicer(bedFile);
		bedPeakSicer.setChIPType(BedPeakSicer.HISTONE_TYPE_H3K27);
		bedPeakSicer.setEffectiveGenomeSize(85);
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
