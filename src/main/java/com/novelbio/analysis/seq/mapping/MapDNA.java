package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public abstract class MapDNA {
	
	/** 输入已经过滤好的fastq文件 */
	public abstract void setFqFile(FastQ leftFq, FastQ rightFq);
	
	/**
	 * @param outFileName 结果文件名，后缀自动改为sam
	 */
	public abstract void setOutFileName(String outFileName);
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatch
	 */
	public abstract void setMismatch(double mismatch);
	
	public abstract void setChrFile(String chrFile);
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public abstract void setExePath(String exePath);
	
	/** 线程数量，默认4线程 */
	public abstract void setThreadNum(int threadNum);

	public abstract void setMapLibrary(MapLibrary mapLibrary);
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public abstract void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform);
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public abstract void setGapLength(int gapLength);

	/**
	 * 参数设定不能用于solid
	 */
	public abstract SamFile mapReads();
	
	/**
	 * 目前只有bwa和bowtie2两种
	 * @param softMapping
	 * @return
	 */
	public static MapDNA creatMapDNA(SoftWare softMapping) {
		MapDNA mapSoftware = null;
		if (softMapping == SoftWare.bwa) {
			mapSoftware = new MapBwa();
		} else if (softMapping == SoftWare.bowtie2) {
			mapSoftware = new MapBowtie(softMapping);
		}
		return mapSoftware;
	}
}
