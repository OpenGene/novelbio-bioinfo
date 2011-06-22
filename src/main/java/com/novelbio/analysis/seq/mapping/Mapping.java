package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;

/**
 * 准备进行mapping的fastQ文件
 * @author zong0jie
 *
 */
public abstract class Mapping extends FastQ{
	
	public Mapping(String seqFile1, int QUALITY) {
		super(seqFile1, QUALITY);
		// TODO Auto-generated constructor stub
	}
	public Mapping(String seqFile1,String seqFile2, int QUALITY) {
		super(seqFile1, seqFile2, QUALITY);
		// TODO Auto-generated constructor stub
	}
	public Mapping(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理
	 * @param fileName 最后的文件名
	 * 实验组 fileName+"_Treat_SoapMap";
	 * @return 返回reads的总数，也就是测序量，<b>双端的话不乘以2</b>
	 */
	public abstract BedSeq mapReads();
	
}
