package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQOld;

public interface FastQMapInt {
	/**
	 * 设定插入片段长度，默认是solexa的长度，150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen);
	/**
	 * @param exeFile 运行文件绝对路径
	 * @param chrFile 序列文件绝对路径
	 */
	public void setFilePath(String exeFile, String chrFile);
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理
	 * @param fileName 最后的文件名
	 * 实验组 fileName+"_Treat_SoapMap";
	 * @return 返回reads的总数，也就是测序量，<b>双端的话不乘以2</b>
	 */
	public SamFile mapReads();
	/**
	 * bwa才用到
	 * 设定mapping质量，仅在bwa中有用，默认为20
	 * 一般30以下，也可以设置到15或者12
	 * @param mapQ
	 */
	public abstract void setMapQ(int mapQ);
}
