package com.novelbio.analysis.seq;

import org.apache.commons.httpclient.methods.multipart.FilePart;

import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.mapping.FastQMap;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlSeq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ZHY();
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
		
		FastQ fastQ = new FastQ(fq, FastQ.QUALITY_MIDIAN);
		fastQ = fastQ.filterReads(FileOperate.changeFileSuffix(fq, "_filter", "fq"));
		
		FastQMap fastQMap = new FastQMap(FastQMap.MAPPING_BWA, fastQ, outFile, true);
		fastQMap.setFilePath("bwa", chrFile);
		fastQMap.mapReads();
		
		BedSeq bedSeqSE = fastQMap.getBedFileSE(bedFileSE);
		bedSeqSE = bedSeqSE.sortBedFile(1, sortBedFile, 2,3);
		BedSeq bedSeq = fastQMap.getBedFile(bedFile);
		
		BedPeakMacs bedPeakMacs = new BedPeakMacs(sortBedFile);
		bedPeakMacs.peakCallling(".", null, "os", peakCallingFile, "ZHY");
	}
	
}
