package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 设定了自动化建索引的方法，并且在mapping失败后会再次建索引
 * 但是还需要补充别的方法，譬如mapping失败后，用一个标准fq文件去做mapping，如果成功则说明索引没问题。
 * 这样才能最好的提高效率
 * @author zong0jie
 *
 */
public abstract class MapDNA {
	/**
	 * 超时时间，意思如果mapping时间大于该时间，index就不太会出错了
	 */
	static int overTime = 50000;
	
	SamFileStatistics samFileStatistics;
	
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
	 * mapping
	 * @return
	 */
	public SamFile mapReads() {
		boolean isIndexMake = IndexMake(false);
		boolean isMappingSucess = mapping();
		if (!isMappingSucess && !isIndexMake) {
			IndexMake(true);
			mapping();
		}
		return copeAfterMapping();
	}
	
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
	
	public SamFileStatistics getStatistics() {
		return samFileStatistics;
	}
	
	/**
	 * 是否顺利执行
	 * 实际上只要mapping能执行起来，譬如运行个10s没出错，就说明索引没问题了
	 * @return
	 */
	protected abstract boolean mapping();
	
	/**
	 * 构建索引
	 * @param force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	protected abstract boolean IndexMake(boolean force);

	protected abstract SamFile copeAfterMapping();
	
	/**
	 * 如果文件后缀名不是.sam，则在文件末尾添加.sam
	 * @param outFileName
	 * @return
	 */
	protected static String addSamToFileName(String outFileName) {
		if (outFileName.endsWith(".sam")) {
			return outFileName;
		} else if (outFileName.endsWith(".")) {
			return outFileName + "sam";
		} else {
			return outFileName + ".sam";
		}
	}
}
