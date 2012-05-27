package com.novelbio.analysis.seq;

import org.apache.commons.httpclient.methods.multipart.FilePart;

import com.novelbio.analysis.seq.chipseq.PeakMacs;
import com.novelbio.analysis.seq.mapping.FastQMap;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlSeq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ZHY();
		ZHY2N();
		ZHY3N();
		CDG2K();
		CDG2W();
		CDGFX2();
		CDGKE();
		CDGWE();
	}
	
	
	public static void ZHY() {
		String parentPath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/N/";
		String fq = parentPath + "RawData/N.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1.con";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/Nse.bed";
		String bedFile =parentPath +  "result/Nextend.bed";
		String sortBedFile = parentPath + "result/NseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "os", peakCallingFile, "ZHY");
	}
	public static void ZHY2N() {
		String parentPath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/2N/";
		String fq = parentPath + "RawData/2N.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1.con";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/2Nse.bed";
		String bedFile =parentPath +  "result/2Nextend.bed";
		String sortBedFile = parentPath + "result/2NseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "os", peakCallingFile, "ZHY");
	}
	public static void ZHY3N() {
		String parentPath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/3N/";
		String fq = parentPath + "RawData/3N.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1.con";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/3Nse.bed";
		String bedFile =parentPath +  "result/3Nextend.bed";
		String sortBedFile = parentPath + "result/3NseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "os", peakCallingFile, "ZHY");
	}
	
	public static void CDGWE() {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/";
		String fq = parentPath + "WE.clean.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/WEse.bed";
		String bedFile =parentPath +  "result/WEextend.bed";
		String sortBedFile = parentPath + "result/WEseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "mm", peakCallingFile, "WE");
	}
	public static void CDGKE() {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/KE.clean.fq/";
		String fq = parentPath + "KE.clean.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/KEse.bed";
		String bedFile =parentPath +  "result/KEextend.bed";
		String sortBedFile = parentPath + "result/KEseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "mm", peakCallingFile, "KE");
	}
	public static void CDGFX2() {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/FX2.clean.fq/";
		String fq = parentPath + "FX2.clean.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/FX2se.bed";
		String bedFile =parentPath +  "result/FX2extend.bed";
		String sortBedFile = parentPath + "result/FX2seSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "mm", peakCallingFile, "FX2");
	}
	public static void CDG2W() {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/2W.clean.fq/";
		String fq = parentPath + "2W.clean.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/2Wse.bed";
		String bedFile =parentPath +  "result/2Wextend.bed";
		String sortBedFile = parentPath + "result/2WseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "mm", peakCallingFile, "2W");
	}
	public static void CDG2K() {
		String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/2K.clean.fq/";
		String fq = parentPath + "2K.clean.fq";
		String chrFile = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/Index/bwa_Index/mm9.fasta";
		String outFile = parentPath + "result/bwaMapping";
		String bedFileSE =parentPath +  "result/2Kse.bed";
		String bedFile =parentPath +  "result/2Kextend.bed";
		String sortBedFile = parentPath + "result/2KseSort.bed";
		String peakCallingFile = parentPath + "result/peakCalling";
		
		FastQOld fastQ = new FastQOld(fq, FastQOld.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.setMapQ(15);
//		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		bedSeq.sortBedFile(FileOperate.changeFileSuffix(bedFile, "_sort", null));
		PeakMacs bedPeakMacs = new PeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "mm", peakCallingFile, "2K");
	}
}
