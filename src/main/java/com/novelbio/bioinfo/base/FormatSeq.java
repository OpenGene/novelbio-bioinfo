package com.novelbio.bioinfo.base;

import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.bed.BedFile;
import com.novelbio.bioinfo.sam.SamFile;

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
		 String suffix = FileOperate.getFileSuffix(fileName);
		 if (StringOperate.isEqualIgnoreCase(suffix, "bam")) {
			return FormatSeq.BAM;
		} else if (StringOperate.isEqualIgnoreCase(suffix, "sam")) {
			return FormatSeq.SAM;
		}
		 if (StringOperate.isEqualIgnoreCase(suffix, "gz")) {
			String fileNameRemoveGz = FileOperate.getFileNameSep(fileName)[0];
			suffix = FileOperate.getFileSuffix(fileNameRemoveGz);
		}
		 
		 if (StringOperate.isEqualIgnoreCase(suffix, "fastq") || StringOperate.isEqualIgnoreCase(suffix, "fq")) {
			return FormatSeq.FASTQ;
		} else if (StringOperate.isEqualIgnoreCase(suffix, "bed")) {
			return FormatSeq.BED;
		}
		 
		 if (BedFile.isBedFile(fileName)) {
			return FormatSeq.BED;
		 }
		 return FormatSeq.UNKNOWN;
	 }
}
