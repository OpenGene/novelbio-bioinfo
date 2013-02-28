package com.novelbio.analysis.seq;

import com.novelbio.analysis.seq.sam.SamFile;

/**
 * 测序文件的枚举
 * @author zong0jie
 *
 */
public enum FormatSeq {
	 FASTQ, SAM, BAM, BED, UNKNOWN;
	 
	 /**
	  * 目前只能判定ben文件和sam，bam文件
	  * @param fileName
	  * @return
	  */
	 public static FormatSeq getFileType(String fileName) {
		 if (BedSeq.isBedFile(fileName)) {
			return FormatSeq.BED;
		 }
		 FormatSeq formatSeq = SamFile.isSamBamFile(fileName);
		 if (formatSeq != UNKNOWN) {
			return formatSeq;
		 }
		 else {
			 //TODO 可以加入fastq的判断
			return formatSeq;
		}
	 }
}
