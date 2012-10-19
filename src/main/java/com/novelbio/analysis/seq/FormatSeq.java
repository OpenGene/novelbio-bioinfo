package com.novelbio.analysis.seq;

import com.novelbio.analysis.seq.sam.SamFile;

/**
 * �����ļ���ö��
 * @author zong0jie
 *
 */
public enum FormatSeq {
	 FASTQ, SAM, BAM, BED, UNKNOWN;
	 
	 public static FormatSeq getFileType(String fileName) {
		 if (BedSeq.isBedFile(fileName)) {
			return FormatSeq.BED;
		 }
		 FormatSeq formatSeq = SamFile.isSamBamFile(fileName);
		 if (formatSeq != UNKNOWN) {
			return formatSeq;
		 }
		 else {
			 //TODO ���Լ���fastq���ж�
			return formatSeq;
		}
	 }
}
