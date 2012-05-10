package com.novelbio.analysis.seq;
/**
 * 测序文件的枚举
 * @author zong0jie
 *
 */
public enum FormatSeq {
	 FASTQ, SAM, BAM, BED;

	 public static void main(String[] args) {
		 System.out.println( FormatSeq.SAM == FormatSeq.BED);
	}
}
