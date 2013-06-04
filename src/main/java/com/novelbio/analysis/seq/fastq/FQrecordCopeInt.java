package com.novelbio.analysis.seq.fastq;


public interface FQrecordCopeInt {
	/**
	 * 成功过滤则返回 true，并且输入的fastQRecord也被过滤
	 * reads质量太差或过滤失败则返回false
	 * @return
	 */
	public boolean copeReads(FastQRecord fastQRecord);

}
