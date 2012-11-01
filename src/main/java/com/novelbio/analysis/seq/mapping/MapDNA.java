package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;

public interface MapDNA {
	
	/** 输入已经过滤好的fastq文件 */
	public void setFqFile(FastQ leftFq, FastQ rightFq);
	
	/**
	 * @param outFileName 结果文件名，后缀自动改为sam
	 */
	public void setOutFileName(String outFileName);
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatch
	 */
	public void setMismatch(double mismatch);
	
	public void setChrFile(String chrFile);
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePath);
	
	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum);

	public void setMapLibrary(MapLibrary mapLibrary);
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform);
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public void setGapLength(int gapLength);

	/**
	 * 参数设定不能用于solid
	 */
	public SamFile mapReads();
}
