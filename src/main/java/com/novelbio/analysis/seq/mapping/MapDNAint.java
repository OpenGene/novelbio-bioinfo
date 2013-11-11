package com.novelbio.analysis.seq.mapping;

import java.util.List;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public interface MapDNAint {
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders);
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder);
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
	
	public void setChrIndex(String chrFile);
	
	public void setSortNeed(boolean isNeedSort);
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
	 * 构建索引
	 * @param force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	public boolean IndexMake(boolean force);
	/**
	 * mapping
	 * @return
	 */
	public SamFile mapReads();
	
	public List<AlignmentRecorder> getLsAlignmentRecorders();
	
	/**
	 * 设定次级版本，如bowtie，bowtie2等
	 * @param bowtieVersion
	 */
	public void setSubVersion(SoftWare bowtieVersion);
}
