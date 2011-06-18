package com.novelbio.analysis.seq.mapping;

public interface Mapping {
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理
	 * @param fileName 最后的文件名
	 * 实验组 fileName+"_Treat_SoapMap";
	 * @return 返回reads的总数，也就是测序量，<b>双端的话不乘以2</b>
	 */
	public BedSeq mapReads();
	
}
