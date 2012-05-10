package com.novelbio.analysis.seq;

import com.novelbio.analysis.seq.chipseq.PeakMacs;
import com.novelbio.analysis.seq.mapping.FastQMap;
import com.novelbio.base.fileOperate.FileOperate;

public class ChIPSeqComb {
	
	public static void main(String[] args) 
	{
		ChIPSeqPeakCallingBWA("/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/GSM307619_ES.H3K27me3.fasq", 
				"/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta", "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/nature2007/k27/result", "nature2007K27", "mm");
	}
	/**
	 * mapping到peakCalling的部分，直接采用bwa
	 */
	public static void ChIPSeqPeakCallingBWA(String fq, String chrFile, String resultPath, String prix, String species) {
		String parentPath = FileOperate.addSep(resultPath);
		String mapping = FileOperate.addSep(FileOperate.createFolders(parentPath,"Mapping"));
		String peakCalling = FileOperate.addSep(FileOperate.createFolders(parentPath,"PeakCalling"));
		String outFile = mapping + prix + "bwaMapping";
		String bedFileSE =mapping + prix +"se.bed";
		String bedFileSEsort = mapping + prix + "seSort.bed";
		String bedFileExtend =mapping + prix +  "Extend.bed";
		String bedFileExtendSort =mapping + prix +  "ExtendSort.bed";
		
		FastQ fastQ = new FastQ(fq, FastQ.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.mapReads();
		fastQMap.setMapQ(10);
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, bedFileSEsort, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFileExtend);
		bedSeq = bedSeq.sortBedFile(1, bedFileExtendSort, 2,3);
		PeakMacs bedPeakMacs = new PeakMacs(bedFileSEsort);
		bedPeakMacs.peakCallling(".", null, species, peakCalling, prix);
	}
	
	
	
	
	
}
