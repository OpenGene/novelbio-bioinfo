package com.novelbio.analysis.seq.mapping;

import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;

public interface MapDNAint extends IntCmdSoft {
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders);
	/** 内部会对SamFileStatistics添加chr信息 */
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder);
	/** 输入已经过滤好的fastq文件 */
	public void setFqFile(FastQ leftFq, FastQ rightFq);
	
	/**
	 * @param outFileName 结果文件名，后缀自动改为sam
	 */
	public void setOutFileName(String outFileName);
	
	public void setChrIndex(String chrFile);
	
	public void setSortNeed(boolean isNeedSort);
	
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
	 * 构建索引
	 * @return
	 */
	public void IndexMake();
	/**
	 * mapping
	 * @return
	 */
	public SamFile mapReads();
	
	public List<AlignmentRecorder> getLsAlignmentRecorders();
	
	void setPrefix(String prefix);
	void setLeftFq(List<FastQ> lsLeftFastQs);
	void setRightFq(List<FastQ> lsRightFastQs);
	String getOutNameCope();
	/** 是否写入bam文件，默认写入
	 * 有时候mapping但不需要写入文件，譬如过滤掉rrna reads的时候，
	 * 只需要将没有mapping的reads输出即可，并不需要要把bam文件输出
	 * @param writeToBam
	 */
	void setWriteToBam(boolean writeToBam);
}
